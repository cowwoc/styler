#!/usr/bin/env python3
"""
Phase 3: Context.md Generation for Delegated Implementation Protocol

Generates comprehensive context.md files that enable autonomous agent implementation.
Agents use this context to implement changes independently, then converge through
iterative integration.

Usage:
    python generate-context.py --task-name TASK_NAME --task-dir TASK_DIR

Output:
    Creates context.md in task directory with complete implementation context
"""

import argparse
import json
import os
import sys
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import List, Dict, Optional, Set


@dataclass
class FileInfo:
    """Information about a file to be modified"""
    path: str
    current_lines: int
    expected_change_type: str  # 'modify', 'create', 'delete'
    expected_changes: str  # Description of changes
    risk_level: str  # 'high', 'medium', 'low'


@dataclass
class AgentAssignment:
    """Work assignment for a specific agent"""
    agent_type: str
    implementation_scope: str
    files_assigned: List[str]
    review_scope: str  # What this agent reviews (ALL or specific domains)
    dependencies: List[str] = field(default_factory=list)


@dataclass
class TaskContext:
    """Complete context for autonomous agent implementation"""
    task_name: str
    primary_objective: str
    success_criteria: List[str]
    scope_boundaries: List[str]

    # Technical constraints
    architecture_patterns: List[str] = field(default_factory=list)
    api_contracts: List[str] = field(default_factory=list)
    dependency_constraints: List[str] = field(default_factory=list)
    performance_requirements: List[str] = field(default_factory=list)

    # Security requirements
    threat_model: List[str] = field(default_factory=list)
    input_validation: List[str] = field(default_factory=list)
    access_control: List[str] = field(default_factory=list)
    data_protection: List[str] = field(default_factory=list)

    # Quality standards
    style_compliance: List[str] = field(default_factory=list)
    complexity_limits: Dict[str, int] = field(default_factory=dict)
    test_coverage: Dict[str, str] = field(default_factory=dict)
    documentation: List[str] = field(default_factory=list)

    # Integration
    modified_files: List[FileInfo] = field(default_factory=list)
    shared_interfaces: List[str] = field(default_factory=list)
    coordination_requirements: List[str] = field(default_factory=list)
    call_sites: List[str] = field(default_factory=list)

    # Agent assignments
    agent_assignments: List[AgentAssignment] = field(default_factory=list)


class ContextGenerator:
    """Generates context.md files for delegated implementation protocol"""

    def __init__(self, task_dir: Path, task_name: str):
        self.task_dir = task_dir
        self.task_name = task_name
        self.code_dir = task_dir / "code"
        self.todo_file = Path("/workspace/branches/main/code/todo.md")
        self.synthesis_file = task_dir / "synthesis-output.json"

    def extract_task_from_todo(self) -> Dict[str, any]:
        """Extract task information from todo.md"""
        if not self.todo_file.exists():
            raise FileNotFoundError(f"todo.md not found at {self.todo_file}")

        with open(self.todo_file, 'r') as f:
            content = f.read()

        # Find task section - only **READY:** tasks can be worked on
        task_marker = f"**READY:** `{self.task_name}`"
        if task_marker not in content:
            # Check if task exists but is blocked
            if f"`{self.task_name}`" in content:
                raise ValueError(f"Task '{self.task_name}' found in todo.md but is BLOCKED, not READY. Cannot work on blocked tasks.")
            raise ValueError(f"Task '{self.task_name}' not found in todo.md (expected format: '**READY:** `{self.task_name}`')")

        # Extract task details
        task_section_start = content.index(task_marker)
        # Find next task or end of file (tasks start with ### heading or next status marker)
        next_section_markers = ["### ", "- [ ] **READY:**", "- [ ] **BLOCKED:**"]
        next_task = len(content)  # Default to end of file
        for marker in next_section_markers:
            pos = content.find(marker, task_section_start + len(task_marker))
            if pos != -1 and pos < next_task:
                next_task = pos
        if next_task == -1:
            task_section = content[task_section_start:]
        else:
            task_section = content[task_section_start:next_task]

        return self._parse_task_section(task_section)

    def _parse_task_section(self, section: str) -> Dict[str, any]:
        """Parse a task section from todo.md"""
        lines = section.split('\n')
        task_info = {
            'description': '',
            'purpose': '',
            'scope': [],
            'components': [],
            'integration': []
        }

        current_subsection = None
        for line in lines:
            line = line.strip()
            if not line or line.startswith('**READY:**') or line.startswith('**BLOCKED:**'):
                continue

            if line.startswith('- **Purpose:**'):
                current_subsection = 'purpose'
                task_info['purpose'] = line.replace('- **Purpose:**', '').strip()
            elif line.startswith('- **Scope:**'):
                current_subsection = 'scope'
            elif line.startswith('- **Components:**') or line.startswith('- **Features:**'):
                current_subsection = 'components'
            elif line.startswith('- **Integration:**'):
                current_subsection = 'integration'
            elif line.startswith('  -') and current_subsection:
                item = line.replace('  -', '').strip()
                if current_subsection in ['scope', 'components', 'integration']:
                    task_info[current_subsection].append(item)

        return task_info

    def determine_agent_assignments(self, task_info: Dict[str, any],
                                   files: List[FileInfo]) -> List[AgentAssignment]:
        """Determine which agents should work on which aspects"""
        assignments = []

        # Determine if we need each agent based on task requirements
        needs_architect = True  # Always need architect
        task_str = str(task_info).lower()
        needs_security = ('security' in task_str or
                         'validation' in task_str or
                         'authentication' in task_str)
        needs_quality = any(f.path.endswith('.java') for f in files)
        needs_performance = ('performance' in task_str or
                            'optimization' in task_str or
                            'algorithm' in task_str)

        # Technical Architect - always included
        if needs_architect:
            arch_files = [f.path for f in files if f.expected_change_type == 'create' or
                        'interface' in f.path.lower() or 'model' in f.path.lower()]
            assignments.append(AgentAssignment(
                agent_type='technical-architect',
                implementation_scope='Data structures, core interfaces, architectural components',
                files_assigned=arch_files,
                review_scope='ALL',  # Reviews all code for architectural coherence
                dependencies=[]
            ))

        # Security Auditor - if security-related
        if needs_security:
            security_files = [f.path for f in files if 'validator' in f.path.lower() or
                            'security' in f.path.lower() or 'auth' in f.path.lower()]
            assignments.append(AgentAssignment(
                agent_type='security-auditor',
                implementation_scope='Input validation, security controls, sanitization',
                files_assigned=security_files,
                review_scope='ALL',  # Reviews all code for security issues
                dependencies=['technical-architect']  # Depends on interfaces
            ))

        # Code Quality Auditor - if source code changes
        if needs_quality:
            quality_files = [f.path for f in files if f.expected_change_type == 'modify']
            assignments.append(AgentAssignment(
                agent_type='code-quality-auditor',
                implementation_scope='Refactoring, design patterns, code quality improvements',
                files_assigned=quality_files,
                review_scope='ALL',  # Reviews all code for quality
                dependencies=['technical-architect']
            ))

        # Performance Analyzer - if performance-critical
        if needs_performance:
            perf_files = [f.path for f in files if 'algorithm' in f.path.lower() or
                        'cache' in f.path.lower() or 'optimization' in f.path.lower()]
            assignments.append(AgentAssignment(
                agent_type='performance-analyzer',
                implementation_scope='Algorithm optimization, resource efficiency',
                files_assigned=perf_files,
                review_scope='ALL',  # Reviews all code for performance
                dependencies=['technical-architect']
            ))

        # Style Auditor - ALWAYS (review-only, no implementation)
        assignments.append(AgentAssignment(
            agent_type='style-auditor',
            implementation_scope='REVIEW ONLY - No implementation',
            files_assigned=[],  # No files assigned (review only)
            review_scope='ALL',  # Reviews all code for style compliance
            dependencies=[]
        ))

        return assignments

    def read_synthesis_output(self) -> Optional[Dict[str, any]]:
        """Read synthesis output if available"""
        if not self.synthesis_file.exists():
            return None

        with open(self.synthesis_file, 'r') as f:
            return json.load(f)

    def identify_files_to_modify(self, task_info: Dict[str, any],
                                 synthesis: Optional[Dict[str, any]] = None) -> List[FileInfo]:
        """Identify files that will be modified based on task requirements"""
        files = []

        # Use synthesis data if available (preferred)
        if synthesis and 'files_to_create' in synthesis:
            for file_spec in synthesis['files_to_create']:
                files.append(FileInfo(
                    path=file_spec['path'],
                    current_lines=0,  # NEW file
                    expected_change_type='create',
                    expected_changes=file_spec['purpose'],
                    risk_level=file_spec.get('risk_level', 'high')
                ))
            return files

        # Fallback to todo.md extraction (legacy)
        for component in task_info.get('components', []):
            # Try to infer file paths from component descriptions
            # Example: "ValidationService class" -> src/main/java/.../ValidationService.java
            if 'class' in component.lower():
                class_name = component.split()[0]
                # Simplified path construction
                file_path = f"src/main/java/com/example/{class_name}.java"
                files.append(FileInfo(
                    path=file_path,
                    current_lines=0,  # NEW file
                    expected_change_type='create',
                    expected_changes=component,
                    risk_level='high'
                ))

        return files

    def generate_context(self) -> TaskContext:
        """Generate complete task context from todo.md and synthesis output"""
        task_info = self.extract_task_from_todo()
        synthesis = self.read_synthesis_output()

        files = self.identify_files_to_modify(task_info, synthesis)
        agent_assignments = self.determine_agent_assignments(task_info, files)

        # Use synthesis data if available, fallback to task_info
        if synthesis:
            primary_objective = synthesis.get('primary_objective', task_info.get('purpose', ''))
            scope_boundaries = synthesis.get('scope_boundaries', task_info.get('scope', []))
            architecture_patterns = synthesis.get('architecture_patterns', [])
            performance_requirements = synthesis.get('performance_requirements', [])
            security_requirements = synthesis.get('security_requirements', [])
            test_categories = synthesis.get('test_coverage_categories', [])
        else:
            primary_objective = task_info.get('purpose', '')
            scope_boundaries = task_info.get('scope', [])
            architecture_patterns = ["Follow existing project patterns"]
            performance_requirements = []
            security_requirements = []
            test_categories = []

        context = TaskContext(
            task_name=self.task_name,
            primary_objective=primary_objective,
            success_criteria=[
                "All implementation changes compile successfully",
                "All tests pass",
                "All quality gates (checkstyle, PMD, manual rules) pass",
                "Unanimous stakeholder approval"
            ],
            scope_boundaries=scope_boundaries,

            # Technical constraints (from synthesis or project standards)
            architecture_patterns=architecture_patterns if architecture_patterns else [
                "Follow existing project patterns",
                "Maintain stateless design where applicable",
                "Use dependency injection for testability"
            ],
            performance_requirements=performance_requirements,

            # Security (from synthesis if available)
            input_validation=security_requirements,

            # Style compliance (mandatory)
            style_compliance=[
                "checkstyle: All automated checks must pass",
                "PMD: All code quality checks must pass",
                "Manual rules: All detection patterns from docs/code-style/ must pass"
            ],
            complexity_limits={
                'cyclomatic_complexity': 10,
                'nesting_depth': 3
            },
            test_coverage={
                'minimum': '80%',
                'critical_paths': '100%'
            },
            documentation=[
                "JavaDoc required for all public methods",
                "Manual documentation with contextual understanding (no generic templates)"
            ] + (test_categories if test_categories else []),

            modified_files=files,
            agent_assignments=agent_assignments
        )

        return context

    def render_context_md(self, context: TaskContext) -> str:
        """Render TaskContext as markdown"""
        md = f"""# Task Context: {context.task_name}

## 1. Requirements Specification

**Primary Objective:** {context.primary_objective}

**Success Criteria:**
"""
        for criterion in context.success_criteria:
            md += f"- {criterion}\n"

        md += "\n**Scope Boundaries:**\n"
        for boundary in context.scope_boundaries:
            md += f"- {boundary}\n"

        if context.architecture_patterns:
            md += "\n## 2. Technical Constraints\n\n**Architecture Patterns:**\n"
            for pattern in context.architecture_patterns:
                md += f"- {pattern}\n"

        if context.api_contracts:
            md += "\n**API Contracts:**\n"
            for contract in context.api_contracts:
                md += f"- {contract}\n"

        if context.threat_model or context.input_validation:
            md += "\n## 3. Security Requirements\n"
            if context.threat_model:
                md += "\n**Threat Model:**\n"
                for threat in context.threat_model:
                    md += f"- {threat}\n"
            if context.input_validation:
                md += "\n**Input Validation:**\n"
                for validation in context.input_validation:
                    md += f"- {validation}\n"

        md += "\n## 4. Code Quality Standards\n\n**Style Compliance:**\n"
        for style in context.style_compliance:
            md += f"- {style}\n"

        if context.complexity_limits:
            md += "\n**Complexity Limits:**\n"
            for limit, value in context.complexity_limits.items():
                md += f"- {limit}: {value}\n"

        if context.test_coverage:
            md += "\n**Test Coverage:**\n"
            for coverage_type, requirement in context.test_coverage.items():
                md += f"- {coverage_type}: {requirement}\n"

        if context.documentation:
            md += "\n**Documentation:**\n"
            for doc_req in context.documentation:
                md += f"- {doc_req}\n"

        if context.modified_files:
            md += "\n## 5. File Structure\n\n**Files to be Modified:**\n\n"
            for file_info in context.modified_files:
                md += f"### {file_info.path}\n"
                md += f"- **Type:** {file_info.expected_change_type.upper()}\n"
                md += f"- **Current Size:** {file_info.current_lines} lines\n"
                md += f"- **Expected Changes:** {file_info.expected_changes}\n"
                md += f"- **Risk Level:** {file_info.risk_level.upper()}\n\n"

        if context.agent_assignments:
            md += "\n## 6. Agent Work Assignments\n\n"
            md += "### Implementation Agents\n\n"
            md += "| Agent | Implementation Scope | Files Assigned |\n"
            md += "|-------|---------------------|----------------|\n"

            for assignment in context.agent_assignments:
                if assignment.implementation_scope != 'REVIEW ONLY - No implementation':
                    files_str = ', '.join(assignment.files_assigned) if assignment.files_assigned else 'N/A'
                    md += f"| {assignment.agent_type} | {assignment.implementation_scope} | {files_str} |\n"

            md += "\n### Review-Only Agents\n\n"
            md += "| Agent | Review Criteria |\n"
            md += "|-------|----------------|\n"

            for assignment in context.agent_assignments:
                if assignment.implementation_scope == 'REVIEW ONLY - No implementation':
                    md += f"| {assignment.agent_type} | checkstyle, PMD, manual style rules (ALL files) |\n"

            md += "\n### Agent Dependencies and Coordination\n\n"
            for assignment in context.agent_assignments:
                if assignment.dependencies:
                    deps_str = ', '.join(assignment.dependencies)
                    md += f"- **{assignment.agent_type}:** Depends on {deps_str}\n"

            md += "\n### Cross-Domain Review Requirements\n\n"
            md += "**CRITICAL:** ALL agents review ALL code changes for their domain concerns:\n\n"
            for assignment in context.agent_assignments:
                md += f"- **{assignment.agent_type}:** Reviews {assignment.review_scope} "
                md += f"code for {self._get_review_focus(assignment.agent_type)}\n"

        md += "\n## 7. Implementation Protocol\n\n"
        md += "### Phase 4: Autonomous Implementation\n\n"
        md += "Each assigned agent will:\n"
        md += "1. Read this context.md file\n"
        md += "2. Read current codebase files (as needed for their scope)\n"
        md += "3. Implement changes within their assigned scope\n"
        md += "4. Write diff to `../{agent-type}.diff`\n"
        md += "5. Return metadata summary (NOT full diff content)\n\n"

        md += "### Phase 5: Convergence\n\n"
        md += "1. Main agent integrates all diffs\n"
        md += "2. All agents review integrated state\n"
        md += "3. Agents provide APPROVED or REVISE with new diff\n"
        md += "4. Iterate until unanimous approval (max 3 rounds)\n\n"

        md += "### File-Based Communication Protocol\n\n"
        md += "**Agent Output Files:**\n"
        md += "- `../{agent-type}.diff` - Complete unified diff of changes\n"
        md += "- `../{agent-type}-summary.md` - Implementation summary and notes\n\n"

        md += "**Agent Response Format (metadata only, diff written to file):**\n"
        md += "```json\n"
        md += "{\n"
        md += '  "summary": "Brief description of changes",\n'
        md += '  "files_changed": ["file1.java", "file2.java"],\n'
        md += '  "diff_file": "../agent-type.diff",\n'
        md += '  "diff_size_lines": 150,\n'
        md += '  "integration_notes": "Dependencies or conflicts to watch",\n'
        md += '  "tests_added": true,\n'
        md += '  "build_status": "success"\n'
        md += "}\n```\n"

        return md

    def _get_review_focus(self, agent_type: str) -> str:
        """Get review focus description for agent type"""
        focus_map = {
            'technical-architect': 'architectural coherence and design patterns',
            'security-auditor': 'security vulnerabilities and data protection',
            'code-quality-auditor': 'maintainability and best practices',
            'performance-analyzer': 'algorithmic efficiency and resource usage',
            'style-auditor': 'style compliance (checkstyle + PMD + manual rules)'
        }
        return focus_map.get(agent_type, 'domain-specific concerns')

    def write_context_file(self, context: TaskContext):
        """Write context.md to task directory"""
        context_md = self.render_context_md(context)
        context_path = self.task_dir / "context.md"

        with open(context_path, 'w') as f:
            f.write(context_md)

        print(f"✅ Context file created: {context_path}")
        print(f"   - Agents assigned: {len(context.agent_assignments)}")
        print(f"   - Files to modify: {len(context.modified_files)}")

        return context_path


def main():
    parser = argparse.ArgumentParser(
        description='Generate context.md for delegated implementation protocol'
    )
    parser.add_argument('--task-name', required=True, help='Task name from todo.md')
    parser.add_argument('--task-dir', required=True, help='Task directory path')
    parser.add_argument('--verbose', action='store_true', help='Verbose output')

    args = parser.parse_args()

    task_dir = Path(args.task_dir)
    if not task_dir.exists():
        print(f"❌ Error: Task directory not found: {task_dir}", file=sys.stderr)
        sys.exit(1)

    try:
        generator = ContextGenerator(task_dir, args.task_name)
        context = generator.generate_context()
        context_path = generator.write_context_file(context)

        if args.verbose:
            print(f"\n📋 Context Summary:")
            print(f"   Primary Objective: {context.primary_objective}")
            print(f"   Agent Assignments:")
            for assignment in context.agent_assignments:
                print(f"      - {assignment.agent_type}: {assignment.implementation_scope}")

        print(f"\n✅ SUCCESS: Context generation complete")
        print(f"   Agents can now implement autonomously using {context_path}")

    except Exception as e:
        print(f"❌ Error generating context: {e}", file=sys.stderr)
        if args.verbose:
            import traceback
            traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
