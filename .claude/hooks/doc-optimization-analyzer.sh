#!/bin/bash
# Documentation Optimization A/B Testing Analysis  
# Analyzes metrics to determine efficiency gains from Claude-optimized documentation
# Enhanced for task-protocol.md A/B testing comparison

METRICS_FILE="/workspace/branches/main/code/.claude/metrics/workflow_metrics.jsonl"
RESULTS_DIR="/workspace/branches/main/code/.claude/analysis"
TIMESTAMP=$(date -u +"%Y%m%d_%H%M%S")

# Ensure results directory exists
mkdir -p "$RESULTS_DIR"

# Analysis functions
analyze_documentation_efficiency() {
	echo "🔍 DOCUMENTATION EFFICIENCY ANALYSIS - $(date -u)"
	echo "============================================================"
	
	if [[ ! -f "$METRICS_FILE" ]]; then
		echo "❌ No metrics file found at $METRICS_FILE"
		return 1
	fi
	
	# Extract documentation access patterns
	local doc_accesses=$(grep '"event":"doc_access"' "$METRICS_FILE" 2>/dev/null | wc -l)
	local context_breaks=$(grep '"event":"context_break"' "$METRICS_FILE" 2>/dev/null | wc -l)
	local efficiency_records=$(grep '"event":"doc_efficiency"' "$METRICS_FILE" 2>/dev/null | wc -l)
	
	echo "📊 METRICS SUMMARY:"
	echo "   Documentation accesses: $doc_accesses"
	echo "   Context breaks detected: $context_breaks" 
	echo "   Efficiency records: $efficiency_records"
	echo
	
	if [[ $doc_accesses -eq 0 ]]; then
		echo "⚠️  No documentation access data yet. Metrics collection may be recent."
		echo "   Run some code style tasks to generate comparison data."
		return 0
	fi
	
	# Analyze access patterns by document type
	echo "📋 ACCESS PATTERNS BY DOCUMENT:"
	
	# Traditional human-readable accesses
	local traditional_accesses=$(grep '"event":"doc_access"' "$METRICS_FILE" 2>/dev/null | \
		grep '"doc_file":"docs/code-style-human.md"' | wc -l)
	
	# Claude-optimized accesses  
	local optimized_accesses=$(grep '"event":"doc_access"' "$METRICS_FILE" 2>/dev/null | \
		grep -E '"doc_file":"docs/code-style/.*-claude.md"' | wc -l)
	
	# Task protocol document access patterns
	local original_protocol=$(grep '"event":"doc_access"' "$METRICS_FILE" 2>/dev/null | \
		grep '"doc_file":"docs/project/task-protocol.md"' | wc -l)
	local optimized_protocol=$(grep '"event":"doc_access"' "$METRICS_FILE" 2>/dev/null | \
		grep '"doc_file":"docs/project/task-protocol-claude-optimized.md"' | wc -l)
	
	# Context-breaking external references (deprecated)
	local external_refs=$(grep '"event":"context_break"' "$METRICS_FILE" 2>/dev/null | \
		grep -E '"target_doc":".*style-guides/' | wc -l)
	
	echo "   Traditional hub (code-style-human.md): $traditional_accesses accesses"
	echo "   Claude-optimized (code-style/*-claude.md): $optimized_accesses accesses"
	echo "   Context-breaking external refs (deprecated): $external_refs breaks"
	echo
	echo "📋 TASK PROTOCOL A/B COMPARISON:"
	echo "   Original task-protocol.md: $original_protocol accesses"
	echo "   Claude-optimized version: $optimized_protocol accesses"
	
	# Task protocol efficiency analysis
	if [[ $original_protocol -gt 0 ]] && [[ $optimized_protocol -gt 0 ]]; then
		local protocol_total=$((original_protocol + optimized_protocol))
		local optimized_percentage=$((optimized_protocol * 100 / protocol_total))
		echo "   Claude preference: ${optimized_percentage}% optimized vs $((100 - optimized_percentage))% original"
		
		if [[ $optimized_percentage -gt 60 ]]; then
			echo "   ✅ Strong preference for Claude-optimized version"
		elif [[ $optimized_percentage -gt 40 ]]; then
			echo "   📊 Mixed usage - both versions have merit"
		else
			echo "   ⚠️  Original version still preferred - may need optimization refinement"
		fi
	elif [[ $optimized_protocol -gt 0 ]]; then
		echo "   ✅ Only Claude-optimized version accessed - strong adoption"
	elif [[ $original_protocol -gt 0 ]]; then
		echo "   📋 Only original version accessed - promote optimized version"
	else
		echo "   📊 No task protocol access detected yet"
	fi
	echo
	
	# Calculate efficiency metrics
	if [[ $traditional_accesses -gt 0 ]] && [[ $optimized_accesses -gt 0 ]]; then
		echo "🎯 EFFICIENCY COMPARISON:"
		
		# Estimate tool calls saved (external refs avoided)
		local tool_calls_saved=$((external_refs))
		local efficiency_improvement=$((tool_calls_saved * 100 / (traditional_accesses + external_refs)))
		
		echo "   Estimated tool calls saved: $tool_calls_saved"
		echo "   Efficiency improvement: ~${efficiency_improvement}%"
		echo "   Context preservation: Claude-optimized eliminates dependency chain"
		echo
	fi
	
	# Recent access trends  
	echo "📈 RECENT TRENDS (Last 24 hours):"
	local yesterday=$(date -u -d "yesterday" +"%Y-%m-%d")
	local recent_traditional=$(grep '"event":"doc_access"' "$METRICS_FILE" 2>/dev/null | \
		grep "$yesterday\|$(date -u +"%Y-%m-%d")" | \
		grep '"doc_file":"docs/code-style-human.md"' | wc -l)
	local recent_optimized=$(grep '"event":"doc_access"' "$METRICS_FILE" 2>/dev/null | \
		grep "$yesterday\|$(date -u +"%Y-%m-%d")" | \
		grep -E '"doc_file":"docs/code-style/.*-claude.md"' | wc -l)
	
	echo "   Traditional (recent): $recent_traditional"
	echo "   Optimized (recent): $recent_optimized"
	
	if [[ $recent_optimized -gt $recent_traditional ]]; then
		echo "   ✅ Trend: Claude choosing optimized documentation more frequently"
	elif [[ $recent_traditional -gt $recent_optimized ]]; then
		echo "   ⚠️  Trend: Still using traditional documentation more often"  
	else
		echo "   📊 Trend: Equal usage, need more data for comparison"
	fi
	
	echo
	echo "💡 RECOMMENDATIONS:"
	
	if [[ $context_breaks -gt 5 ]]; then
		echo "   🔴 HIGH: $context_breaks context breaks detected - consolidation beneficial"
	elif [[ $context_breaks -gt 0 ]]; then
		echo "   🟡 MEDIUM: $context_breaks context breaks - some consolidation benefit"  
	else
		echo "   🟢 LOW: Minimal context breaks detected"
	fi
	
	if [[ $optimized_accesses -gt $traditional_accesses ]]; then
		echo "   ✅ Claude prefers optimized documentation - good adoption"
	elif [[ $traditional_accesses -gt 0 ]] && [[ $optimized_accesses -eq 0 ]]; then
		echo "   📋 Consider promoting code-style/*-claude.md files more prominently"
	fi
	
	# Save detailed analysis
	local report_file="$RESULTS_DIR/doc_optimization_report_${TIMESTAMP}.md"
	generate_detailed_report > "$report_file"
	echo "   📄 Detailed report saved: $report_file"
	
	echo
	echo "🔄 Run this analysis periodically to track optimization effectiveness."
}

generate_detailed_report() {
	echo "# Documentation Optimization Analysis Report"
	echo "**Generated**: $(date -u)"
	echo
	echo "## Summary"
	echo "Analysis of Claude's documentation access patterns to measure optimization effectiveness."
	echo
	echo "## Raw Metrics"
	echo '```json'
	grep -E '"event":"(doc_access|context_break|doc_efficiency)"' "$METRICS_FILE" 2>/dev/null | tail -20 || echo "No metrics data available"
	echo '```'
	echo
	echo "## Next Steps"
	echo "1. Continue collecting metrics during normal Claude usage"
	echo "2. Run analysis weekly to identify trends"
	echo "3. Adjust documentation strategy based on empirical evidence"
	echo "4. Consider expanding optimized documentation to other domains"
}

# Main execution
case "${1:-analyze}" in
	"analyze")
		analyze_documentation_efficiency
		;;
	"report")
		generate_detailed_report
		;;
	*)
		echo "Usage: $0 [analyze|report]"
		echo "  analyze: Run full efficiency analysis"
		echo "  report:  Generate detailed markdown report"
		exit 1
		;;
esac