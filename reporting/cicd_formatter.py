"""
CI/CD integration formatter for automated pipelines.
Provides machine-readable output with actionable metrics and thresholds.
"""
from typing import List, Dict, Any
from datetime import datetime
from ..models import RemediationReport, PrioritizedFinding


class CICDFormatter:
    """Formats reports for CI/CD pipeline integration."""
    
    def format(self, report: RemediationReport, fail_on_critical: bool = True) -> Dict[str, Any]:
        """
        Format report for CI/CD integration.
        
        Args:
            report: RemediationReport to format
            fail_on_critical: Whether to mark build as failed if critical issues exist
            
        Returns:
            Dictionary with CI/CD-friendly structure
        """
        # Determine build status
        build_status = self._determine_build_status(report, fail_on_critical)
        
        # Calculate quality gate metrics
        quality_gate = self._calculate_quality_gate(report)
        
        # Generate actionable items
        action_items = self._generate_action_items(report)
        
        # Create CI/CD report
        cicd_report = {
            "version": "1.0",
            "generated_at": report.generated_at,
            "build_status": build_status,
            "summary": {
                "total_findings": report.total_findings,
                "critical_count": report.critical_count,
                "high_count": report.high_count,
                "medium_count": report.medium_count,
                "low_count": report.low_count,
                "average_risk_score": report.summary.get('average_score', 0) if report.summary else 0,
                "highest_risk_score": report.summary.get('highest_score', 0) if report.summary else 0
            },
            "quality_gate": quality_gate,
            "action_items": action_items,
            "findings": self._format_findings_for_cicd(report.prioritized_findings),
            "metrics": self._calculate_metrics(report),
            "recommendations": self._generate_recommendations(report)
        }
        
        return cicd_report
    
    def _determine_build_status(self, report: RemediationReport, fail_on_critical: bool) -> Dict[str, Any]:
        """Determine if build should pass or fail."""
        status = "PASS"
        reason = "No critical security issues found"
        
        if fail_on_critical and report.critical_count > 0:
            status = "FAIL"
            reason = f"Found {report.critical_count} critical security issue(s)"
        elif report.critical_count > 0:
            status = "WARNING"
            reason = f"Found {report.critical_count} critical security issue(s) - review required"
        elif report.high_count > 5:
            status = "WARNING"
            reason = f"Found {report.high_count} high priority issues - review recommended"
        
        return {
            "status": status,
            "reason": reason,
            "should_block_deployment": status == "FAIL"
        }
    
    def _calculate_quality_gate(self, report: RemediationReport) -> Dict[str, Any]:
        """Calculate quality gate metrics."""
        # Define thresholds
        thresholds = {
            "max_critical": 0,
            "max_high": 5,
            "max_average_score": 10.0
        }
        
        avg_score = report.summary.get('average_score', 0) if report.summary else 0
        
        # Check each threshold
        gates = {
            "critical_threshold": {
                "passed": report.critical_count <= thresholds["max_critical"],
                "current": report.critical_count,
                "threshold": thresholds["max_critical"],
                "message": f"Critical issues: {report.critical_count} (max: {thresholds['max_critical']})"
            },
            "high_threshold": {
                "passed": report.high_count <= thresholds["max_high"],
                "current": report.high_count,
                "threshold": thresholds["max_high"],
                "message": f"High priority issues: {report.high_count} (max: {thresholds['max_high']})"
            },
            "average_score_threshold": {
                "passed": avg_score <= thresholds["max_average_score"],
                "current": round(avg_score, 2),
                "threshold": thresholds["max_average_score"],
                "message": f"Average risk score: {avg_score:.2f} (max: {thresholds['max_average_score']})"
            }
        }
        
        all_passed = all(gate["passed"] for gate in gates.values())
        
        return {
            "passed": all_passed,
            "gates": gates,
            "summary": "All quality gates passed" if all_passed else "Some quality gates failed"
        }
    
    def _generate_action_items(self, report: RemediationReport) -> List[Dict[str, Any]]:
        """Generate prioritized action items for the team."""
        action_items = []
        
        # Critical items - must fix immediately
        for pf in report.prioritized_findings:
            if pf.risk_score.priority_level == "CRITICAL":
                action_items.append({
                    "priority": "CRITICAL",
                    "action": "FIX_IMMEDIATELY",
                    "title": pf.finding.title,
                    "algorithm": pf.finding.algorithm.value,
                    "context": pf.finding.usage_context.value,
                    "location": pf.finding.file_path or "Unknown",
                    "risk_score": round(pf.risk_score.final_score, 2),
                    "estimated_effort": pf.estimated_effort or "Unknown",
                    "guidance": pf.remediation_guidance or "Review and remediate"
                })
        
        # High priority items - fix in current sprint
        for pf in report.prioritized_findings:
            if pf.risk_score.priority_level == "HIGH":
                action_items.append({
                    "priority": "HIGH",
                    "action": "FIX_IN_SPRINT",
                    "title": pf.finding.title,
                    "algorithm": pf.finding.algorithm.value,
                    "context": pf.finding.usage_context.value,
                    "location": pf.finding.file_path or "Unknown",
                    "risk_score": round(pf.risk_score.final_score, 2),
                    "estimated_effort": pf.estimated_effort or "Unknown",
                    "guidance": pf.remediation_guidance or "Review and remediate"
                })
        
        return action_items
    
    def _format_findings_for_cicd(self, findings: List[PrioritizedFinding]) -> List[Dict[str, Any]]:
        """Format findings in CI/CD-friendly structure."""
        cicd_findings = []
        
        for pf in findings:
            cicd_findings.append({
                "id": pf.finding.id,
                "rank": pf.risk_score.priority_rank,
                "priority": pf.risk_score.priority_level,
                "title": pf.finding.title,
                "description": pf.finding.description,
                "risk_score": {
                    "final": round(pf.risk_score.final_score, 2),
                    "base": round(pf.risk_score.base_score, 2),
                    "multiplier": round(pf.risk_score.context_multiplier, 2)
                },
                "algorithm": pf.finding.algorithm.value,
                "usage_context": pf.finding.usage_context.value,
                "severity": pf.finding.severity.value,
                "location": {
                    "file": pf.finding.file_path or "Unknown",
                    "line": pf.finding.line_number
                },
                "impact_factors": {
                    "data_sensitivity": pf.risk_score.impact_factors.data_sensitivity,
                    "exposure_duration": pf.risk_score.impact_factors.exposure_duration,
                    "exploitability": pf.risk_score.impact_factors.exploitability,
                    "blast_radius": pf.risk_score.impact_factors.blast_radius,
                    "algorithm_weakness": pf.risk_score.impact_factors.algorithm_weakness
                },
                "remediation": {
                    "guidance": pf.remediation_guidance or "Review and remediate",
                    "estimated_effort": pf.estimated_effort or "Unknown"
                },
                "cwe_ids": pf.finding.cwe_ids,
                "references": pf.finding.references
            })
        
        return cicd_findings
    
    def _calculate_metrics(self, report: RemediationReport) -> Dict[str, Any]:
        """Calculate additional metrics for tracking."""
        metrics = {
            "total_findings": report.total_findings,
            "by_priority": {
                "critical": report.critical_count,
                "high": report.high_count,
                "medium": report.medium_count,
                "low": report.low_count
            },
            "by_algorithm": report.summary.get('by_algorithm', {}) if report.summary else {},
            "by_context": report.summary.get('by_context', {}) if report.summary else {},
            "risk_scores": {
                "average": round(report.summary.get('average_score', 0), 2) if report.summary else 0,
                "highest": round(report.summary.get('highest_score', 0), 2) if report.summary else 0,
                "lowest": round(report.summary.get('lowest_score', 0), 2) if report.summary else 0
            },
            "effort_distribution": self._calculate_effort_distribution(report)
        }
        
        return metrics
    
    def _calculate_effort_distribution(self, report: RemediationReport) -> Dict[str, int]:
        """Calculate distribution of findings by effort level."""
        distribution = {
            "small": 0,
            "medium": 0,
            "large": 0,
            "unknown": 0
        }
        
        for pf in report.prioritized_findings:
            if pf.estimated_effort:
                if 'Small' in pf.estimated_effort or '1-2 days' in pf.estimated_effort:
                    distribution["small"] += 1
                elif 'Large' in pf.estimated_effort or 'weeks' in pf.estimated_effort.lower():
                    distribution["large"] += 1
                else:
                    distribution["medium"] += 1
            else:
                distribution["unknown"] += 1
        
        return distribution
    
    def _generate_recommendations(self, report: RemediationReport) -> List[str]:
        """Generate high-level recommendations."""
        recommendations = []
        
        # Critical issues
        if report.critical_count > 0:
            recommendations.append(
                f"🚨 URGENT: Address {report.critical_count} critical security issue(s) immediately. "
                "These pose significant risk and should block deployment."
            )
        
        # High priority issues
        if report.high_count > 0:
            recommendations.append(
                f"⚠️ HIGH PRIORITY: Plan remediation for {report.high_count} high-priority issue(s) "
                "in the current sprint."
            )
        
        # Algorithm-specific recommendations
        if report.summary and 'by_algorithm' in report.summary:
            algos = report.summary['by_algorithm']
            if 'MD5' in algos and algos['MD5'] > 0:
                recommendations.append(
                    f"🔑 Replace MD5 with SHA-256 or SHA-3 in {algos['MD5']} location(s). "
                    "MD5 is cryptographically broken."
                )
            if 'SHA1' in algos and algos['SHA1'] > 0:
                recommendations.append(
                    f"🔑 Upgrade from SHA-1 to SHA-256 in {algos['SHA1']} location(s). "
                    "SHA-1 is deprecated and vulnerable to collision attacks."
                )
            if 'DES' in algos or '3DES' in algos:
                des_count = algos.get('DES', 0) + algos.get('3DES', 0)
                if des_count > 0:
                    recommendations.append(
                        f"🔐 Migrate from DES/3DES to AES-256 in {des_count} location(s). "
                        "DES is obsolete and 3DES is deprecated."
                    )
        
        # Context-specific recommendations
        if report.summary and 'by_context' in report.summary:
            contexts = report.summary['by_context']
            if 'authentication' in contexts and contexts['authentication'] > 0:
                recommendations.append(
                    f"🔐 Review authentication mechanisms: {contexts['authentication']} issue(s) found. "
                    "Consider implementing MFA and using bcrypt/Argon2 for password hashing."
                )
            if 'key_storage' in contexts and contexts['key_storage'] > 0:
                recommendations.append(
                    f"🔑 Improve key storage: {contexts['key_storage']} issue(s) found. "
                    "Use HSM or key management services for sensitive keys."
                )
        
        # General recommendations
        avg_score = report.summary.get('average_score', 0) if report.summary else 0
        if avg_score > 10:
            recommendations.append(
                f"📊 Average risk score is {avg_score:.2f}/20 (HIGH). "
                "Prioritize remediation efforts to reduce overall risk exposure."
            )
        
        if not recommendations:
            recommendations.append(
                "✅ No critical issues found. Continue monitoring for new vulnerabilities."
            )
        
        return recommendations


class GitHubActionsFormatter:
    """Formats reports specifically for GitHub Actions integration."""
    
    def format_annotations(self, report: RemediationReport) -> List[Dict[str, Any]]:
        """
        Format findings as GitHub Actions annotations.
        
        Args:
            report: RemediationReport to format
            
        Returns:
            List of annotation dictionaries
        """
        annotations = []
        
        for pf in report.prioritized_findings:
            # Map priority to annotation level
            if pf.risk_score.priority_level == "CRITICAL":
                level = "error"
            elif pf.risk_score.priority_level == "HIGH":
                level = "warning"
            else:
                level = "notice"
            
            annotation = {
                "level": level,
                "message": f"{pf.finding.title} (Risk Score: {pf.risk_score.final_score:.2f})",
                "title": f"[{pf.risk_score.priority_level}] {pf.finding.algorithm.value} in {pf.finding.usage_context.value}",
                "file": pf.finding.file_path or "unknown",
                "line": pf.finding.line_number or 1,
                "annotation_level": level
            }
            
            annotations.append(annotation)
        
        return annotations
    
    def format_step_summary(self, report: RemediationReport) -> str:
        """
        Format report as GitHub Actions step summary (Markdown).
        
        Args:
            report: RemediationReport to format
            
        Returns:
            Markdown formatted string
        """
        lines = []
        
        # Header
        lines.append("# 🔐 Crypto Vulnerability Analysis")
        lines.append("")
        
        # Summary table
        lines.append("## Summary")
        lines.append("")
        lines.append("| Priority | Count |")
        lines.append("|----------|-------|")
        lines.append(f"| 🚨 CRITICAL | {report.critical_count} |")
        lines.append(f"| ⚠️ HIGH | {report.high_count} |")
        lines.append(f"| 📋 MEDIUM | {report.medium_count} |")
        lines.append(f"| ℹ️ LOW | {report.low_count} |")
        lines.append(f"| **TOTAL** | **{report.total_findings}** |")
        lines.append("")
        
        # Top findings
        if report.prioritized_findings:
            lines.append("## Top 5 Priority Findings")
            lines.append("")
            for pf in report.prioritized_findings[:5]:
                emoji = "🚨" if pf.risk_score.priority_level == "CRITICAL" else "⚠️"
                lines.append(f"### {emoji} #{pf.risk_score.priority_rank} - {pf.finding.title}")
                lines.append(f"- **Risk Score:** {pf.risk_score.final_score:.2f}/20")
                lines.append(f"- **Algorithm:** {pf.finding.algorithm.value}")
                lines.append(f"- **Context:** {pf.finding.usage_context.value}")
                if pf.finding.file_path:
                    lines.append(f"- **Location:** `{pf.finding.file_path}`")
                if pf.estimated_effort:
                    lines.append(f"- **Effort:** {pf.estimated_effort}")
                lines.append("")
        
        return "\n".join(lines)


class JenkinsFormatter:
    """Formats reports for Jenkins CI integration."""
    
    def format_junit_xml(self, report: RemediationReport) -> str:
        """
        Format report as JUnit XML for Jenkins.
        
        Args:
            report: RemediationReport to format
            
        Returns:
            JUnit XML formatted string
        """
        from xml.etree.ElementTree import Element, SubElement, tostring
        from xml.dom import minidom
        
        # Create test suite
        testsuite = Element('testsuite')
        testsuite.set('name', 'Crypto Vulnerability Analysis')
        testsuite.set('tests', str(report.total_findings))
        testsuite.set('failures', str(report.critical_count + report.high_count))
        testsuite.set('errors', '0')
        testsuite.set('time', '0')
        
        # Add test cases for each finding
        for pf in report.prioritized_findings:
            testcase = SubElement(testsuite, 'testcase')
            testcase.set('name', f"{pf.finding.algorithm.value} in {pf.finding.usage_context.value}")
            testcase.set('classname', 'CryptoVulnerability')
            testcase.set('time', '0')
            
            # Add failure for critical and high priority
            if pf.risk_score.priority_level in ['CRITICAL', 'HIGH']:
                failure = SubElement(testcase, 'failure')
                failure.set('type', pf.risk_score.priority_level)
                failure.set('message', pf.finding.title)
                failure.text = f"""
Risk Score: {pf.risk_score.final_score:.2f}/20
Location: {pf.finding.file_path or 'Unknown'}
Guidance: {pf.remediation_guidance or 'Review and remediate'}
"""
        
        # Pretty print XML
        xml_str = minidom.parseString(tostring(testsuite)).toprettyxml(indent="  ")
        return xml_str