#!/usr/bin/env python3
"""
Phase 5: Convergence Algorithm for Delegated Implementation Protocol

Manages iterative integration of agent implementations until unanimous approval.
Implements conflict detection, resolution tracking, and selective review routing.

Usage:
    python convergence.py --task-dir TASK_DIR --round ROUND_NUMBER

Features:
    - Conflict detection between agent diffs
    - Selective agent review (only send changed files)
    - Implicit approval tracking (unchanged files)
    - Round-based state management
"""

import argparse
import json
import os
import subprocess
import sys
from dataclasses import dataclass, field, asdict
from datetime import datetime
from pathlib import Path
from typing import List, Dict, Optional, Set, Tuple


@dataclass
class FileConflict:
    """Represents a conflict between agent changes"""
    file_path: str
    conflict_type: str  # 'overlapping_lines', 'semantic', 'compatible_additions'
    agents_involved: List[str]
    overlapping_lines: List[Tuple[int, int]]  # [(start, end), ...]
    resolvable_automatically: bool
    resolution_strategy: str = ""  # How to resolve


@dataclass
class FileState:
    """State of a file across convergence rounds"""
    file_path: str
    round_modified: int  # Last round this file was modified
    last_content_hash: str  # SHA256 of last content
    authors: List[str]  # Agents who have contributed to this file
    agent_approvals: Set[str] = field(default_factory=set)


@dataclass
class AgentResponse:
    """Agent response from review round"""
    agent_type: str
    decision: str  # 'APPROVED', 'REVISE', 'CONFLICT'
    rationale: str
    revision_diff_file: Optional[str] = None  # Path to revised diff if REVISE
    conflict_description: Optional[str] = None  # Description if CONFLICT


@dataclass
class ConvergenceState:
    """Complete state of convergence process"""
    task_name: str
    current_round: int
    max_rounds: int
    file_states: Dict[str, FileState] = field(default_factory=dict)
    agent_last_review: Dict[str, int] = field(default_factory=dict)
    conflicts_detected: List[FileConflict] = field(default_factory=list)
    round_history: List[Dict] = field(default_factory=list)
    user_checkpoint_rounds: List[int] = field(default_factory=list)  # Rounds where user requested changes


class ConvergenceManager:
    """Manages convergence process for delegated implementation"""

    def __init__(self, task_dir: Path, max_rounds: int = 10):
        self.task_dir = task_dir
        self.code_dir = task_dir / "code"
        self.max_rounds = max_rounds
        self.state_file = task_dir / "convergence-state.json"
        self.state = self._load_or_create_state()

    def _load_or_create_state(self) -> ConvergenceState:
        """Load existing state or create new one"""
        if self.state_file.exists():
            with open(self.state_file, 'r') as f:
                data = json.load(f)
            # Reconstruct state from JSON
            state = ConvergenceState(
                task_name=data['task_name'],
                current_round=data['current_round'],
                max_rounds=data['max_rounds'],
                file_states={
                    path: FileState(
                        file_path=fs['file_path'],
                        round_modified=fs['round_modified'],
                        last_content_hash=fs['last_content_hash'],
                        authors=fs['authors'],
                        agent_approvals=set(fs['agent_approvals'])
                    ) for path, fs in data['file_states'].items()
                },
                agent_last_review=data['agent_last_review'],
                conflicts_detected=[
                    FileConflict(
                        file_path=c['file_path'],
                        conflict_type=c['conflict_type'],
                        agents_involved=c['agents_involved'],
                        overlapping_lines=c['overlapping_lines'],
                        resolvable_automatically=c['resolvable_automatically'],
                        resolution_strategy=c.get('resolution_strategy', '')
                    ) for c in data.get('conflicts_detected', [])
                ],
                round_history=data.get('round_history', []),
                user_checkpoint_rounds=data.get('user_checkpoint_rounds', [])
            )
            return state
        else:
            return ConvergenceState(
                task_name=self.task_dir.name,
                current_round=0,
                max_rounds=self.max_rounds,
                user_checkpoint_rounds=[]
            )

    def save_state(self):
        """Persist convergence state to disk"""
        # Convert state to JSON-serializable format
        data = {
            'task_name': self.state.task_name,
            'current_round': self.state.current_round,
            'max_rounds': self.state.max_rounds,
            'file_states': {
                path: {
                    'file_path': fs.file_path,
                    'round_modified': fs.round_modified,
                    'last_content_hash': fs.last_content_hash,
                    'authors': fs.authors,
                    'agent_approvals': list(fs.agent_approvals)
                } for path, fs in self.state.file_states.items()
            },
            'agent_last_review': self.state.agent_last_review,
            'conflicts_detected': [
                {
                    'file_path': c.file_path,
                    'conflict_type': c.conflict_type,
                    'agents_involved': c.agents_involved,
                    'overlapping_lines': c.overlapping_lines,
                    'resolvable_automatically': c.resolvable_automatically,
                    'resolution_strategy': c.resolution_strategy
                } for c in self.state.conflicts_detected
            ],
            'round_history': self.state.round_history,
            'user_checkpoint_rounds': self.state.user_checkpoint_rounds
        }

        with open(self.state_file, 'w') as f:
            json.dump(data, f, indent=2)

    def detect_conflicts(self, diffs: Dict[str, Path]) -> List[FileConflict]:
        """
        Detect conflicts between agent diffs.

        Args:
            diffs: Dict mapping agent_type -> diff_file_path

        Returns:
            List of FileConflict objects
        """
        conflicts = []

        # Parse all diffs to get file->agents mapping
        file_changes = {}  # file_path -> [(agent, line_ranges)]

        for agent, diff_file in diffs.items():
            file_edits = self._parse_diff_file(diff_file)
            for file_path, line_ranges in file_edits.items():
                if file_path not in file_changes:
                    file_changes[file_path] = []
                file_changes[file_path].append((agent, line_ranges))

        # Check for overlapping changes
        for file_path, agent_changes in file_changes.items():
            if len(agent_changes) <= 1:
                continue  # No conflict possible

            # Check each pair of agents
            for i in range(len(agent_changes)):
                for j in range(i + 1, len(agent_changes)):
                    agent_a, ranges_a = agent_changes[i]
                    agent_b, ranges_b = agent_changes[j]

                    overlap = self._check_line_overlap(ranges_a, ranges_b)
                    if overlap:
                        conflict_type = self._classify_conflict(
                            file_path, ranges_a, ranges_b, agent_a, agent_b
                        )

                        conflicts.append(FileConflict(
                            file_path=file_path,
                            conflict_type=conflict_type,
                            agents_involved=[agent_a, agent_b],
                            overlapping_lines=overlap,
                            resolvable_automatically=(conflict_type == 'compatible_additions'),
                            resolution_strategy=self._get_resolution_strategy(conflict_type)
                        ))

        return conflicts

    def _parse_diff_file(self, diff_file: Path) -> Dict[str, List[Tuple[int, int]]]:
        """
        Parse a unified diff file to extract modified line ranges.

        Returns:
            Dict mapping file_path -> [(start_line, end_line), ...]
        """
        if not diff_file.exists():
            return {}

        file_edits = {}
        current_file = None
        current_range = None

        with open(diff_file, 'r') as f:
            for line in f:
                if line.startswith('diff --git'):
                    # New file
                    parts = line.split()
                    if len(parts) >= 4:
                        current_file = parts[3].lstrip('b/')
                        file_edits[current_file] = []

                elif line.startswith('@@') and current_file:
                    # Extract line range: @@ -1,5 +1,7 @@
                    parts = line.split()
                    if len(parts) >= 3:
                        # Parse "+1,7" format
                        plus_part = parts[2]  # +1,7
                        if ',' in plus_part:
                            start, length = plus_part.lstrip('+').split(',')
                            start = int(start)
                            end = start + int(length) - 1
                            file_edits[current_file].append((start, end))

        return file_edits

    def _check_line_overlap(self, ranges_a: List[Tuple[int, int]],
                           ranges_b: List[Tuple[int, int]]) -> List[Tuple[int, int]]:
        """Check if line ranges overlap"""
        overlaps = []
        for start_a, end_a in ranges_a:
            for start_b, end_b in ranges_b:
                # Check for overlap
                if not (end_a < start_b or end_b < start_a):
                    overlap_start = max(start_a, start_b)
                    overlap_end = min(end_a, end_b)
                    overlaps.append((overlap_start, overlap_end))
        return overlaps

    def _classify_conflict(self, file_path: str, ranges_a, ranges_b,
                          agent_a: str, agent_b: str) -> str:
        """Classify the type of conflict"""
        # Simplified classification
        # In a real implementation, this would analyze the actual changes
        return 'overlapping_lines'  # Default

    def _get_resolution_strategy(self, conflict_type: str) -> str:
        """Get resolution strategy for conflict type"""
        strategies = {
            'compatible_additions': 'merge_both',
            'overlapping_lines': 'manual_resolution',
            'semantic': 'agent_coordination'
        }
        return strategies.get(conflict_type, 'manual_resolution')

    def integrate_diffs(self, diffs: Dict[str, Path], round_num: int) -> List[str]:
        """
        Integrate all agent diffs into code directory.

        Args:
            diffs: Dict mapping agent_type -> diff_file_path
            round_num: Current convergence round

        Returns:
            List of modified file paths
        """
        modified_files = []

        # Change to code directory for git operations
        original_dir = os.getcwd()
        os.chdir(self.code_dir)

        try:
            # Apply diffs in dependency order
            for agent, diff_file in diffs.items():
                if not diff_file.exists():
                    print(f"⚠️  Warning: Diff file not found: {diff_file}", file=sys.stderr)
                    continue

                # Apply diff
                result = subprocess.run(
                    ['git', 'apply', '--check', str(diff_file)],
                    capture_output=True,
                    text=True
                )

                if result.returncode == 0:
                    # Diff can be applied cleanly
                    subprocess.run(['git', 'apply', str(diff_file)], check=True)
                    print(f"✅ Applied {agent} diff cleanly")

                    # Track modified files
                    file_edits = self._parse_diff_file(diff_file)
                    for file_path in file_edits.keys():
                        if file_path not in modified_files:
                            modified_files.append(file_path)

                        # Update file state
                        if file_path not in self.state.file_states:
                            self.state.file_states[file_path] = FileState(
                                file_path=file_path,
                                round_modified=round_num,
                                last_content_hash=self._hash_file(file_path),
                                authors=[agent]
                            )
                        else:
                            fs = self.state.file_states[file_path]
                            fs.round_modified = round_num
                            fs.last_content_hash = self._hash_file(file_path)
                            if agent not in fs.authors:
                                fs.authors.append(agent)

                else:
                    print(f"❌ Failed to apply {agent} diff: {result.stderr}", file=sys.stderr)
                    # Handle conflict (would need manual resolution)

        finally:
            os.chdir(original_dir)

        self.save_state()
        return modified_files

    def _hash_file(self, file_path: str) -> str:
        """Calculate SHA256 hash of file"""
        import hashlib
        full_path = self.code_dir / file_path
        if not full_path.exists():
            return ""

        sha256 = hashlib.sha256()
        with open(full_path, 'rb') as f:
            for chunk in iter(lambda: f.read(4096), b""):
                sha256.update(chunk)
        return sha256.hexdigest()

    def files_for_agent_review(self, agent_name: str, current_round: int) -> List[str]:
        """
        Determine which files an agent needs to review this round.

        Args:
            agent_name: Agent identifier
            current_round: Current round number

        Returns:
            List of file paths requiring review
        """
        last_reviewed = self.state.agent_last_review.get(agent_name, 0)

        changed_files = []
        for file_path, file_state in self.state.file_states.items():
            # File changed since agent's last review?
            if file_state.round_modified > last_reviewed:
                # Check if agent's own unchanged work
                if agent_name in file_state.authors:
                    # Did another agent or parent modify it?
                    # (simplified check - in reality would compare hashes)
                    # For now, include it
                    pass

                changed_files.append(file_path)

        return changed_files

    def mark_agent_reviewed(self, agent_name: str, current_round: int):
        """Record that agent has reviewed current round"""
        self.state.agent_last_review[agent_name] = current_round
        self.save_state()

    def check_unanimous_approval(self, agent_responses: Dict[str, AgentResponse]) -> bool:
        """Check if all agents approved"""
        return all(resp.decision == 'APPROVED' for resp in agent_responses.values())

    def user_requested_changes_at_checkpoint(self):
        """
        Called when user requests changes during review checkpoint.
        Resets the round limit by extending it by 10 from current round.

        Example:
            If at round 3 and user requests changes:
            - New max_rounds = current_round + 10 = 13
            - Agents have until round 13 to reach consensus
        """
        self.state.user_checkpoint_rounds.append(self.state.current_round)
        self.state.max_rounds = self.state.current_round + 10
        self.save_state()

        print(f"✅ User requested changes at round {self.state.current_round}")
        print(f"   Round limit extended to: {self.state.max_rounds}")

    def rounds_remaining(self) -> int:
        """Calculate remaining rounds before reaching limit"""
        return self.state.max_rounds - self.state.current_round

    def should_escalate_to_user(self) -> bool:
        """Check if convergence should escalate to user review checkpoint"""
        remaining = self.rounds_remaining()
        if remaining <= 0:
            return True  # Reached limit, escalate to user
        return False

    def generate_review_summary(self, round_num: int, agent_responses: Dict[str, AgentResponse]) -> Dict:
        """Generate summary of review round results"""
        summary = {
            'round': round_num,
            'timestamp': datetime.now().isoformat(),
            'approved': [agent for agent, resp in agent_responses.items()
                        if resp.decision == 'APPROVED'],
            'revising': [agent for agent, resp in agent_responses.items()
                        if resp.decision == 'REVISE'],
            'conflicts': [agent for agent, resp in agent_responses.items()
                         if resp.decision == 'CONFLICT'],
            'unanimous': self.check_unanimous_approval(agent_responses),
            'rounds_remaining': self.rounds_remaining()
        }

        self.state.round_history.append(summary)
        self.save_state()

        return summary


def main():
    parser = argparse.ArgumentParser(
        description='Manage convergence for delegated implementation protocol'
    )
    parser.add_argument('--task-dir', required=True, help='Task directory path')
    parser.add_argument('--round', type=int, default=1, help='Round number')
    parser.add_argument('--detect-conflicts', action='store_true',
                       help='Detect conflicts in agent diffs')
    parser.add_argument('--integrate', action='store_true',
                       help='Integrate agent diffs')
    parser.add_argument('--agent-diffs', help='JSON file mapping agents to diff files')
    parser.add_argument('--verbose', action='store_true', help='Verbose output')

    args = parser.parse_args()

    task_dir = Path(args.task_dir)
    if not task_dir.exists():
        print(f"❌ Error: Task directory not found: {task_dir}", file=sys.stderr)
        sys.exit(1)

    manager = ConvergenceManager(task_dir)

    if args.detect_conflicts and args.agent_diffs:
        # Load agent diffs
        with open(args.agent_diffs, 'r') as f:
            agent_diffs = json.load(f)

        diffs = {agent: Path(diff_file) for agent, diff_file in agent_diffs.items()}
        conflicts = manager.detect_conflicts(diffs)

        if conflicts:
            print(f"⚠️  Detected {len(conflicts)} conflicts:")
            for conflict in conflicts:
                print(f"   - {conflict.file_path}: {conflict.conflict_type}")
                print(f"     Agents: {', '.join(conflict.agents_involved)}")
                print(f"     Auto-resolvable: {conflict.resolvable_automatically}")
        else:
            print("✅ No conflicts detected")

    elif args.integrate and args.agent_diffs:
        # Load agent diffs
        with open(args.agent_diffs, 'r') as f:
            agent_diffs = json.load(f)

        diffs = {agent: Path(diff_file) for agent, diff_file in agent_diffs.items()}
        modified_files = manager.integrate_diffs(diffs, args.round)

        print(f"✅ Integration complete for round {args.round}")
        print(f"   Modified files: {len(modified_files)}")
        if args.verbose:
            for file_path in modified_files:
                print(f"      - {file_path}")

    else:
        print("Usage: Specify --detect-conflicts or --integrate with --agent-diffs", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
