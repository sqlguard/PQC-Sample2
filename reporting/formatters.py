"""
Formatters for generating reports in different formats.
"""
from typing import List
from ..models import RemediationReport, PrioritizedFinding


class MarkdownFormatter:
    """Formats reports as Markdown."""
    
    def format(self, report: RemediationReport) -> str:
        """
        Format report as Markdown.
        
        Args:
            report: RemediationReport to format
            
        Returns:
            Markdown formatted string
        """
        lines = []
        
        # Header
        lines.append("# Crypto Vulnerability Remediation Prioritization Report")
        lines.append("")
        lines.append(f"**Generated:** {report.generated_at}")
        lines.append(f"**Total Findings:** {report.total_findings}")
        lines.append("")
        
        # Summary statistics
        lines.append("## 📊 Summary")
        lines.append("")
        lines.append("### Priority Distribution")
        lines.append("")
        lines.append(f"- 🚨 **CRITICAL:** {report.critical_count}")
        lines.append(f"- ⚠️ **HIGH:** {report.high_count}")
        lines.append(f"- 📋 **MEDIUM:** {report.medium_count}")
        lines.append(f"- ℹ️ **LOW:** {report.low_count}")
        lines.append("")
        
        if report.summary:
            lines.append("### Risk Score Statistics")
            lines.append("")
            lines.append(f"- **Average Score:** {report.summary.get('average_score', 0):.2f}")
            lines.append(f"- **Highest Score:** {report.summary.get('highest_score', 0):.2f}")
            lines.append(f"- **Lowest Score:** {report.summary.get('lowest_score', 0):.2f}")
            lines.append("")
            
            # Algorithm distribution
            if 'by_algorithm' in report.summary:
                lines.append("### Findings by Algorithm")
                lines.append("")
                for algo, count in sorted(
                    report.summary['by_algorithm'].items(),
                    key=lambda x: x[1],
                    reverse=True
                ):
                    lines.append(f"- **{algo}:** {count}")
                lines.append("")
            
            # Context distribution
            if 'by_context' in report.summary:
                lines.append("### Findings by Usage Context")
                lines.append("")
                for context, count in sorted(
                    report.summary['by_context'].items(),
                    key=lambda x: x[1],
                    reverse=True
                ):
                    lines.append(f"- **{context}:** {count}")
                lines.append("")
        
        # Prioritized findings
        lines.append("## 🎯 Prioritized Findings")
        lines.append("")
        
        for pf in report.prioritized_findings:
            lines.extend(self._format_finding(pf))
        
        return "\n".join(lines)
    
    def _format_finding(self, pf: PrioritizedFinding) -> List[str]:
        """Format a single prioritized finding."""
        lines = []
        
        # Priority emoji
        priority_emoji = {
            'CRITICAL': '🚨',
            'HIGH': '⚠️',
            'MEDIUM': '📋',
            'LOW': 'ℹ️'
        }
        emoji = priority_emoji.get(pf.risk_score.priority_level, '•')
        
        # Finding header
        lines.append(f"### {emoji} #{pf.risk_score.priority_rank} - {pf.finding.title}")
        lines.append("")
        
        # Priority and score
        lines.append(f"**Priority:** {pf.risk_score.priority_level} | "
                    f"**Risk Score:** {pf.risk_score.final_score:.2f} "
                    f"(Base: {pf.risk_score.base_score:.2f} × "
                    f"Multiplier: {pf.risk_score.context_multiplier:.2f})")
        lines.append("")
        
        # Details
        lines.append("**Details:**")
        lines.append(f"- **Algorithm:** {pf.finding.algorithm.value}")
        lines.append(f"- **Usage Context:** {pf.finding.usage_context.value}")
        lines.append(f"- **Severity:** {pf.finding.severity.value}")
        
        if pf.finding.file_path:
            location = pf.finding.file_path
            if pf.finding.line_number:
                location += f":{pf.finding.line_number}"
            lines.append(f"- **Location:** `{location}`")
        
        lines.append("")
        
        # Description
        lines.append("**Description:**")
        lines.append(f"{pf.finding.description}")
        lines.append("")
        
        # Impact factors
        lines.append("**Impact Factors:**")
        lines.append(f"- Data Sensitivity: {pf.risk_score.impact_factors.data_sensitivity}/10")
        lines.append(f"- Exposure Duration: {pf.risk_score.impact_factors.exposure_duration}/10")
        lines.append(f"- Exploitability: {pf.risk_score.impact_factors.exploitability}/10")
        lines.append(f"- Blast Radius: {pf.risk_score.impact_factors.blast_radius}/10")
        lines.append(f"- Algorithm Weakness: {pf.risk_score.impact_factors.algorithm_weakness}/10")
        lines.append("")
        
        # Remediation guidance
        if pf.remediation_guidance:
            lines.append("**Remediation Guidance:**")
            lines.append(f"{pf.remediation_guidance}")
            lines.append("")
        
        # Effort estimate
        if pf.estimated_effort:
            lines.append(f"**Estimated Effort:** {pf.estimated_effort}")
            lines.append("")
        
        # CWE IDs
        if pf.finding.cwe_ids:
            lines.append(f"**CWE IDs:** {', '.join(pf.finding.cwe_ids)}")
            lines.append("")
        
        # References
        if pf.finding.references:
            lines.append("**References:**")
            for ref in pf.finding.references:
                lines.append(f"- {ref}")
            lines.append("")
        
        lines.append("---")
        lines.append("")
        
        return lines


class HTMLFormatter:
    """Formats reports as HTML."""
    
    def format(self, report: RemediationReport) -> str:
        """
        Format report as HTML.
        
        Args:
            report: RemediationReport to format
            
        Returns:
            HTML formatted string
        """
        html_parts = []
        
        # HTML header
        html_parts.append(self._get_html_header())
        
        # Title and summary
        html_parts.append('<div class="container">')
        html_parts.append('<h1>🔐 Crypto Vulnerability Remediation Prioritization Report</h1>')
        html_parts.append(f'<p class="meta">Generated: {report.generated_at}</p>')
        html_parts.append(f'<p class="meta">Total Findings: {report.total_findings}</p>')
        
        # Summary section
        html_parts.append('<div class="summary">')
        html_parts.append('<h2>📊 Summary</h2>')
        
        # Priority distribution
        html_parts.append('<div class="priority-grid">')
        html_parts.append(f'<div class="priority-card critical"><h3>{report.critical_count}</h3><p>CRITICAL</p></div>')
        html_parts.append(f'<div class="priority-card high"><h3>{report.high_count}</h3><p>HIGH</p></div>')
        html_parts.append(f'<div class="priority-card medium"><h3>{report.medium_count}</h3><p>MEDIUM</p></div>')
        html_parts.append(f'<div class="priority-card low"><h3>{report.low_count}</h3><p>LOW</p></div>')
        html_parts.append('</div>')
        
        if report.summary:
            html_parts.append('<div class="stats">')
            html_parts.append(f'<p><strong>Average Risk Score:</strong> {report.summary.get("average_score", 0):.2f}</p>')
            html_parts.append(f'<p><strong>Highest Risk Score:</strong> {report.summary.get("highest_score", 0):.2f}</p>')
            html_parts.append('</div>')
        
        html_parts.append('</div>')  # Close summary
        
        # Findings section
        html_parts.append('<h2>🎯 Prioritized Findings</h2>')
        
        for pf in report.prioritized_findings:
            html_parts.append(self._format_finding_html(pf))
        
        html_parts.append('</div>')  # Close container
        html_parts.append('</body></html>')
        
        return '\n'.join(html_parts)
    
    def _get_html_header(self) -> str:
        """Get HTML header with CSS."""
        return '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crypto Vulnerability Remediation Report</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
               line-height: 1.6; color: #333; background: #f5f5f5; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 40px; 
                     border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; margin-bottom: 20px; }
        h2 { color: #34495e; margin: 30px 0 20px; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
        h3 { color: #555; margin: 15px 0 10px; }
        .meta { color: #7f8c8d; font-size: 0.9em; margin: 5px 0; }
        .summary { background: #ecf0f1; padding: 20px; border-radius: 5px; margin: 20px 0; }
        .priority-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); 
                        gap: 15px; margin: 20px 0; }
        .priority-card { padding: 20px; border-radius: 5px; text-align: center; color: white; }
        .priority-card.critical { background: #e74c3c; }
        .priority-card.high { background: #e67e22; }
        .priority-card.medium { background: #f39c12; }
        .priority-card.low { background: #95a5a6; }
        .priority-card h3 { font-size: 2em; margin-bottom: 5px; color: white; }
        .priority-card p { font-size: 0.9em; opacity: 0.9; }
        .stats { margin: 15px 0; }
        .stats p { margin: 5px 0; }
        .finding { background: #fff; border: 1px solid #ddd; border-radius: 5px; 
                  padding: 20px; margin: 20px 0; }
        .finding.critical { border-left: 5px solid #e74c3c; }
        .finding.high { border-left: 5px solid #e67e22; }
        .finding.medium { border-left: 5px solid #f39c12; }
        .finding.low { border-left: 5px solid #95a5a6; }
        .finding-header { display: flex; justify-content: space-between; align-items: center; 
                         margin-bottom: 15px; }
        .finding-title { font-size: 1.2em; font-weight: bold; color: #2c3e50; }
        .priority-badge { padding: 5px 15px; border-radius: 20px; color: white; 
                         font-size: 0.85em; font-weight: bold; }
        .priority-badge.critical { background: #e74c3c; }
        .priority-badge.high { background: #e67e22; }
        .priority-badge.medium { background: #f39c12; }
        .priority-badge.low { background: #95a5a6; }
        .score { font-size: 1.5em; font-weight: bold; color: #3498db; }
        .details { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); 
                  gap: 10px; margin: 15px 0; }
        .detail-item { padding: 10px; background: #f8f9fa; border-radius: 3px; }
        .detail-label { font-weight: bold; color: #555; font-size: 0.85em; }
        .detail-value { color: #333; margin-top: 3px; }
        .description { margin: 15px 0; padding: 15px; background: #f8f9fa; 
                      border-radius: 3px; line-height: 1.8; }
        .impact-factors { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); 
                         gap: 10px; margin: 15px 0; }
        .impact-factor { text-align: center; padding: 10px; background: #e8f4f8; border-radius: 3px; }
        .impact-factor-value { font-size: 1.5em; font-weight: bold; color: #3498db; }
        .impact-factor-label { font-size: 0.85em; color: #555; margin-top: 5px; }
        .guidance { background: #d5f4e6; padding: 15px; border-radius: 3px; 
                   border-left: 4px solid #27ae60; margin: 15px 0; }
        .code { background: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 3px; 
               font-family: 'Courier New', monospace; font-size: 0.9em; overflow-x: auto; }
        .tags { margin: 10px 0; }
        .tag { display: inline-block; padding: 3px 10px; background: #3498db; color: white; 
              border-radius: 3px; font-size: 0.85em; margin: 2px; }
    </style>
</head>
<body>'''
    
    def _format_finding_html(self, pf: PrioritizedFinding) -> str:
        """Format a single finding as HTML."""
        priority_class = pf.risk_score.priority_level.lower()
        
        html = f'<div class="finding {priority_class}">'
        
        # Header
        html += '<div class="finding-header">'
        html += f'<div class="finding-title">#{pf.risk_score.priority_rank} - {pf.finding.title}</div>'
        html += f'<span class="priority-badge {priority_class}">{pf.risk_score.priority_level}</span>'
        html += '</div>'
        
        # Score
        html += f'<div class="score">Risk Score: {pf.risk_score.final_score:.2f}</div>'
        html += f'<p class="meta">Base: {pf.risk_score.base_score:.2f} × Multiplier: {pf.risk_score.context_multiplier:.2f}</p>'
        
        # Details
        html += '<div class="details">'
        html += f'<div class="detail-item"><div class="detail-label">Algorithm</div><div class="detail-value">{pf.finding.algorithm.value}</div></div>'
        html += f'<div class="detail-item"><div class="detail-label">Usage Context</div><div class="detail-value">{pf.finding.usage_context.value}</div></div>'
        html += f'<div class="detail-item"><div class="detail-label">Severity</div><div class="detail-value">{pf.finding.severity.value}</div></div>'
        if pf.estimated_effort:
            html += f'<div class="detail-item"><div class="detail-label">Estimated Effort</div><div class="detail-value">{pf.estimated_effort}</div></div>'
        html += '</div>'
        
        # Location
        if pf.finding.file_path:
            location = pf.finding.file_path
            if pf.finding.line_number:
                location += f":{pf.finding.line_number}"
            html += f'<div class="code">{location}</div>'
        
        # Description
        html += f'<div class="description">{pf.finding.description}</div>'
        
        # Impact factors
        html += '<h4>Impact Factors</h4>'
        html += '<div class="impact-factors">'
        html += f'<div class="impact-factor"><div class="impact-factor-value">{pf.risk_score.impact_factors.data_sensitivity}</div><div class="impact-factor-label">Data Sensitivity</div></div>'
        html += f'<div class="impact-factor"><div class="impact-factor-value">{pf.risk_score.impact_factors.exposure_duration}</div><div class="impact-factor-label">Exposure Duration</div></div>'
        html += f'<div class="impact-factor"><div class="impact-factor-value">{pf.risk_score.impact_factors.exploitability}</div><div class="impact-factor-label">Exploitability</div></div>'
        html += f'<div class="impact-factor"><div class="impact-factor-value">{pf.risk_score.impact_factors.blast_radius}</div><div class="impact-factor-label">Blast Radius</div></div>'
        html += f'<div class="impact-factor"><div class="impact-factor-value">{pf.risk_score.impact_factors.algorithm_weakness}</div><div class="impact-factor-label">Algorithm Weakness</div></div>'
        html += '</div>'
        
        # Remediation guidance
        if pf.remediation_guidance:
            html += f'<div class="guidance"><strong>Remediation Guidance:</strong><br>{pf.remediation_guidance}</div>'
        
        # Tags (CWE IDs)
        if pf.finding.cwe_ids:
            html += '<div class="tags">'
            for cwe in pf.finding.cwe_ids:
                html += f'<span class="tag">{cwe}</span>'
            html += '</div>'
        
        html += '</div>'
        
        return html