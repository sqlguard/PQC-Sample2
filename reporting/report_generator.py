"""
Report generator for creating prioritized remediation reports
in multiple formats (JSON, Markdown, HTML, Dashboard, CI/CD).
"""
from typing import List, Optional, Dict, Any
from datetime import datetime
from pathlib import Path
import json

from ..models import RemediationReport, PrioritizedFinding
from .formatters import MarkdownFormatter, HTMLFormatter
from .dashboard_formatter import DashboardFormatter
from .cicd_formatter import CICDFormatter, GitHubActionsFormatter, JenkinsFormatter


class ReportGenerator:
    """Generates remediation reports in various formats."""
    
    def __init__(self):
        """Initialize the report generator."""
        self.markdown_formatter = MarkdownFormatter()
        self.html_formatter = HTMLFormatter()
        self.dashboard_formatter = DashboardFormatter()
        self.cicd_formatter = CICDFormatter()
        self.github_formatter = GitHubActionsFormatter()
        self.jenkins_formatter = JenkinsFormatter()
    
    def generate_report(
        self,
        prioritized_findings: List[PrioritizedFinding],
        statistics: dict
    ) -> RemediationReport:
        """
        Generate a remediation report from prioritized findings.
        
        Args:
            prioritized_findings: List of prioritized findings
            statistics: Statistics dictionary
            
        Returns:
            RemediationReport object
        """
        report = RemediationReport(
            total_findings=len(prioritized_findings),
            prioritized_findings=prioritized_findings,
            summary=statistics,
            generated_at=datetime.utcnow().isoformat() + 'Z'
        )
        
        return report
    
    def save_json(self, report: RemediationReport, output_path: str) -> None:
        """
        Save report as JSON.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(report.model_dump(), f, indent=2, default=str)
        
        print(f"JSON report saved to: {output_file}")
    
    def save_markdown(self, report: RemediationReport, output_path: str) -> None:
        """
        Save report as Markdown.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        markdown_content = self.markdown_formatter.format(report)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(markdown_content)
        
        print(f"Markdown report saved to: {output_file}")
    
    def save_html(self, report: RemediationReport, output_path: str) -> None:
        """
        Save report as HTML.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        html_content = self.html_formatter.format(report)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html_content)
        
        print(f"HTML report saved to: {output_file}")
    
    def save_dashboard(self, report: RemediationReport, output_path: str) -> None:
        """
        Save report as interactive HTML dashboard.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        dashboard_content = self.dashboard_formatter.format(report)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(dashboard_content)
        
        print(f"Dashboard report saved to: {output_file}")
    
    def save_cicd(self, report: RemediationReport, output_path: str, fail_on_critical: bool = True) -> None:
        """
        Save report in CI/CD format.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
            fail_on_critical: Whether to mark build as failed if critical issues exist
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        cicd_report = self.cicd_formatter.format(report, fail_on_critical)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(cicd_report, f, indent=2, default=str)
        
        print(f"CI/CD report saved to: {output_file}")
        
        # Print build status
        status = cicd_report['build_status']
        if status['status'] == 'FAIL':
            print(f"❌ BUILD STATUS: {status['status']} - {status['reason']}")
        elif status['status'] == 'WARNING':
            print(f"⚠️  BUILD STATUS: {status['status']} - {status['reason']}")
        else:
            print(f"✅ BUILD STATUS: {status['status']} - {status['reason']}")
    
    def save_github_annotations(self, report: RemediationReport, output_path: str) -> None:
        """
        Save GitHub Actions annotations.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        annotations = self.github_formatter.format_annotations(report)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(annotations, f, indent=2)
        
        print(f"GitHub annotations saved to: {output_file}")
    
    def save_github_summary(self, report: RemediationReport, output_path: str) -> None:
        """
        Save GitHub Actions step summary.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        summary = self.github_formatter.format_step_summary(report)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(summary)
        
        print(f"GitHub step summary saved to: {output_file}")
    
    def save_jenkins_junit(self, report: RemediationReport, output_path: str) -> None:
        """
        Save Jenkins JUnit XML report.
        
        Args:
            report: RemediationReport to save
            output_path: Path to output file
        """
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        junit_xml = self.jenkins_formatter.format_junit_xml(report)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(junit_xml)
        
        print(f"Jenkins JUnit XML saved to: {output_file}")
    
    def save_all_formats(
        self,
        report: RemediationReport,
        base_path: str,
        formats: Optional[List[str]] = None
    ) -> None:
        """
        Save report in multiple formats.
        
        Args:
            report: RemediationReport to save
            base_path: Base path for output files (without extension)
            formats: List of formats to generate (default: all)
        """
        if formats is None:
            formats = ['json', 'markdown', 'html', 'dashboard']
        
        base = Path(base_path)
        
        if 'json' in formats:
            self.save_json(report, f"{base}.json")
        
        if 'markdown' in formats or 'md' in formats:
            self.save_markdown(report, f"{base}.md")
        
        if 'html' in formats:
            self.save_html(report, f"{base}.html")
        
        if 'dashboard' in formats:
            self.save_dashboard(report, f"{base}_dashboard.html")
        
        if 'cicd' in formats:
            self.save_cicd(report, f"{base}_cicd.json")
        
        if 'github' in formats:
            self.save_github_annotations(report, f"{base}_github_annotations.json")
            self.save_github_summary(report, f"{base}_github_summary.md")
        
        if 'jenkins' in formats:
            self.save_jenkins_junit(report, f"{base}_junit.xml")
    
    def print_summary(self, report: RemediationReport) -> None:
        """
        Print a summary of the report to console.
        
        Args:
            report: RemediationReport to summarize
        """
        print("\n" + "="*70)
        print("CRYPTO VULNERABILITY REMEDIATION PRIORITIZATION REPORT")
        print("="*70)
        print(f"\nGenerated: {report.generated_at}")
        print(f"Total Findings: {report.total_findings}")
        
        print("\n📊 Priority Distribution:")
        print(f"  🚨 CRITICAL: {report.critical_count}")
        print(f"  ⚠️  HIGH:     {report.high_count}")
        print(f"  📋 MEDIUM:   {report.medium_count}")
        print(f"  ℹ️  LOW:      {report.low_count}")
        
        if report.summary:
            avg_score = report.summary.get('average_score', 0)
            highest = report.summary.get('highest_score', 0)
            print(f"\n📈 Risk Scores:")
            print(f"  Average: {avg_score:.2f}")
            print(f"  Highest: {highest:.2f}")
        
        print("\n🔝 Top 5 Priority Findings:")
        for i, pf in enumerate(report.prioritized_findings[:5], 1):
            priority_emoji = {
                'CRITICAL': '🚨',
                'HIGH': '⚠️',
                'MEDIUM': '📋',
                'LOW': 'ℹ️'
            }
            emoji = priority_emoji.get(pf.risk_score.priority_level, '•')
            print(f"\n  {i}. {emoji} [{pf.risk_score.priority_level}] {pf.finding.title}")
            print(f"     Score: {pf.risk_score.final_score:.2f} | "
                  f"Algorithm: {pf.finding.algorithm.value} | "
                  f"Context: {pf.finding.usage_context.value}")
            if pf.finding.file_path:
                print(f"     Location: {pf.finding.file_path}")
            if pf.estimated_effort:
                print(f"     Effort: {pf.estimated_effort}")
        
        print("\n" + "="*70 + "\n")