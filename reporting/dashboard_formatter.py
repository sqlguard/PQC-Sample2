"""
Enterprise-grade security dashboard formatter focused on 5 core dimensions:
- Data Sensitivity: What is being protected?
- Exposure Duration: How long is it exposed?
- Exploitability: How easy is it to exploit?
- Blast Radius: What's the scope of impact?
- Usage Context: Authentication vs. storage, internal vs. external
"""
from typing import List, Dict, Any
from ..models import RemediationReport, PrioritizedFinding


class DashboardFormatter:
    """Formats reports as enterprise-grade security dashboards with progressive disclosure."""
    
    def format(self, report: RemediationReport) -> str:
        """
        Format report as an enterprise-grade security dashboard.
        
        Args:
            report: RemediationReport to format
            
        Returns:
            HTML formatted string with focused visualizations
        """
        html_parts = []
        
        # HTML header with enhanced CSS and Chart.js
        html_parts.append(self._get_html_header())
        
        # Main container
        html_parts.append('<div class="dashboard-container">')
        
        # Header section
        html_parts.append(self._format_header(report))
        
        # Security Scorecard - 5 Core Dimensions (NEW)
        html_parts.append(self._format_security_scorecard(report))
        
        # Immediate Actions - Top 5 Critical Findings (NEW)
        html_parts.append(self._format_immediate_actions(report))
        
        # Tactical Planning Section (Collapsible)
        html_parts.append('<div class="tactical-section">')
        html_parts.append('<h2 class="section-toggle" onclick="toggleSection(\'tactical\')">🗺️ Tactical Planning <span class="toggle-icon">▼</span></h2>')
        html_parts.append('<div id="tactical-content" class="collapsible-content collapsed">')
        
        # Remediation roadmap
        html_parts.append(self._format_remediation_roadmap(report))
        
        html_parts.append('</div>')  # Close tactical-content
        html_parts.append('</div>')  # Close tactical-section
        
        # Detailed Findings Section (Collapsible)
        html_parts.append('<div class="findings-section-wrapper">')
        html_parts.append('<h2 class="section-toggle" onclick="toggleSection(\'findings\')">📝 Detailed Findings <span class="toggle-icon">▼</span></h2>')
        html_parts.append('<div id="findings-content" class="collapsible-content collapsed">')
        html_parts.append(self._format_detailed_findings(report))
        html_parts.append('</div>')  # Close findings-content
        html_parts.append('</div>')  # Close findings-section-wrapper
        
        # JavaScript for interactivity
        html_parts.append(self._get_interactive_scripts(report))
        
        html_parts.append('</div>')  # Close container
        html_parts.append('</body></html>')
        
        return '\n'.join(html_parts)
    
    def _get_html_header(self) -> str:
        """Get HTML header with enhanced CSS and Chart.js for enterprise dashboard."""
        return '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Enterprise Security Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
        /* CSS Variables for Theme Support */
        :root {
            --bg-primary: #020617;
            --bg-secondary: #0f172a;
            --bg-tertiary: #1e293b;
            --text-primary: #e2e8f0;
            --text-secondary: #cbd5e1;
            --text-muted: #94a3b8;
            --border-color: var(--border-color);
            --accent-primary: #06b6d4;
            --accent-secondary: #0891b2;
            --shadow-color: rgba(0, 0, 0, 0.5);
        }
        
        /* Light Mode Theme */
        [data-theme="light"] {
            --bg-primary: #f8fafc;
            --bg-secondary: #ffffff;
            --bg-tertiary: #f1f5f9;
            --text-primary: #0f172a;
            --text-secondary: #334155;
            --text-muted: #64748b;
            --border-color: var(--text-secondary);
            --accent-primary: #0891b2;
            --accent-secondary: #06b6d4;
            --shadow-color: rgba(0, 0, 0, 0.1);
        }
        
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            line-height: 1.6;
            color: var(--text-primary);
            background: var(--bg-primary);
            padding: 20px;
            transition: background-color 0.3s ease, color 0.3s ease;
        }
        .dashboard-container {
            max-width: 1600px;
            margin: 0 auto;
            background: var(--bg-secondary);
            border-radius: 12px;
            box-shadow: 0 10px 40px var(--shadow-color);
            overflow: hidden;
            border: 1px solid var(--border-color);
            transition: all 0.3s ease;
        }
        .header-section {
            background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
            color: var(--accent-primary);
            padding: 30px 40px;
            text-align: center;
            border-bottom: 2px solid var(--accent-primary);
            position: relative;
        }
        .theme-toggle {
            position: absolute;
            top: 20px;
            right: 40px;
            background: var(--bg-tertiary);
            border: 2px solid var(--border-color);
            color: var(--text-primary);
            padding: 10px 20px;
            border-radius: 25px;
            cursor: pointer;
            font-size: 1.2em;
            transition: all 0.3s ease;
            box-shadow: 0 2px 8px var(--shadow-color);
        }
        .theme-toggle:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px var(--shadow-color);
            border-color: var(--accent-primary);
        }
        .header-section::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 2px;
            background: linear-gradient(90deg, transparent, #06b6d4, transparent);
        }
        .header-section h1 {
            font-size: 2.2em;
            margin-bottom: 8px;
            text-shadow: 0 0 20px rgba(6, 182, 212, 0.5);
        }
        .header-meta {
            font-size: 1em;
            opacity: 0.9;
            color: var(--text-muted);
        }
        
        /* Security Scorecard Styles */
        .security-scorecard {
            background: var(--bg-tertiary);
            padding: 40px;
            border-bottom: 1px solid var(--border-color);
        }
        .security-scorecard h2 {
            color: #06b6d4;
            margin-bottom: 25px;
            font-size: 1.9em;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
            text-align: center;
        }
        .scorecard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 25px;
            margin: 25px 0;
        }
        .dimension-card {
            background: var(--bg-secondary);
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.4);
            border: 2px solid var(--border-color);
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .dimension-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 20px rgba(6, 182, 212, 0.3);
        }
        .dimension-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        .dimension-title {
            font-size: 1.1em;
            color: var(--text-secondary);
            font-weight: 600;
        }
        .dimension-icon {
            font-size: 1.8em;
        }
        .dimension-score {
            font-size: 3em;
            font-weight: bold;
            color: #06b6d4;
            text-shadow: 0 0 15px rgba(6, 182, 212, 0.6);
            margin: 10px 0;
        }
        .dimension-bar {
            height: 12px;
            background: var(--bg-tertiary);
            border-radius: 6px;
            overflow: hidden;
            margin: 15px 0;
        }
        .dimension-bar-fill {
            height: 100%;
            background: linear-gradient(90deg, #06b6d4, #0891b2);
            border-radius: 6px;
            transition: width 0.5s ease;
        }
        .dimension-bar-fill.critical { background: linear-gradient(90deg, #dc2626, #b91c1c); }
        .dimension-bar-fill.high { background: linear-gradient(90deg, #ea580c, #c2410c); }
        .dimension-bar-fill.medium { background: linear-gradient(90deg, #eab308, #ca8a04); }
        .dimension-bar-fill.low { background: linear-gradient(90deg, #16a34a, #15803d); }
        .dimension-detail {
            font-size: 0.9em;
            color: var(--text-muted);
            margin-top: 10px;
        }
        
        /* Immediate Actions Styles */
        .immediate-actions {
            background: var(--bg-tertiary);
            padding: 40px;
            border-bottom: 1px solid var(--border-color);
        }
        .immediate-actions h2 {
            color: #dc2626;
            margin-bottom: 25px;
            font-size: 1.9em;
            text-shadow: 0 0 10px rgba(220, 38, 38, 0.3);
            text-align: center;
        }
        .action-cards {
            display: grid;
            gap: 20px;
        }
        .action-card {
            background: var(--bg-secondary);
            border-radius: 10px;
            padding: 25px;
            border-left: 5px solid #dc2626;
            box-shadow: 0 4px 12px rgba(0,0,0,0.4);
            transition: transform 0.2s;
            cursor: pointer;
        }
        .action-card:hover {
            transform: translateX(5px);
            box-shadow: 0 6px 20px rgba(220, 38, 38, 0.3);
        }
        .action-card.high { border-left-color: #ea580c; }
        .action-card.medium { border-left-color: #eab308; }
        .action-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 15px;
        }
        .action-rank {
            font-size: 1.5em;
            font-weight: bold;
            color: #06b6d4;
            margin-right: 15px;
        }
        .action-title {
            flex: 1;
            font-size: 1.2em;
            font-weight: 600;
            color: var(--text-primary);
        }
        .action-badge {
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 0.85em;
            font-weight: bold;
            color: white;
        }
        .action-badge.critical { background: #dc2626; }
        .action-badge.high { background: #ea580c; }
        .action-badge.medium { background: #eab308; color: #000; }
        .action-metrics {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
            margin: 15px 0;
        }
        .action-metric {
            background: var(--bg-tertiary);
            padding: 12px;
            border-radius: 6px;
            border: 1px solid var(--border-color);
        }
        .action-metric-label {
            font-size: 0.8em;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .action-metric-value {
            font-size: 1.1em;
            color: var(--text-secondary);
            font-weight: 600;
            margin-top: 5px;
        }
        .action-buttons {
            display: flex;
            gap: 10px;
            margin-top: 15px;
        }
        .action-btn {
            padding: 10px 20px;
            border-radius: 6px;
            border: none;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
        }
        .action-btn-primary {
            background: #06b6d4;
            color: white;
        }
        .action-btn-primary:hover {
            background: #0891b2;
            box-shadow: 0 0 15px rgba(6, 182, 212, 0.5);
        }
        .action-btn-secondary {
            background: #334155;
            color: var(--text-secondary);
        }
        .action-btn-secondary:hover {
            background: #475569;
        }
        
        /* Collapsible Section Styles */
        .tactical-section, .findings-section-wrapper {
            background: var(--bg-tertiary);
            border-bottom: 1px solid var(--border-color);
        }
        .section-toggle {
            color: #06b6d4;
            padding: 25px 40px;
            font-size: 1.8em;
            cursor: pointer;
            user-select: none;
            display: flex;
            justify-content: space-between;
            align-items: center;
            transition: background 0.2s;
        }
        .section-toggle:hover {
            background: rgba(6, 182, 212, 0.05);
        }
        .toggle-icon {
            font-size: 0.7em;
            transition: transform 0.3s;
        }
        .toggle-icon.rotated {
            transform: rotate(-180deg);
        }
        .collapsible-content {
            max-height: 5000px;
            overflow: hidden;
            transition: max-height 0.5s ease;
            padding: 0 40px 40px 40px;
        }
        .collapsible-content.collapsed {
            max-height: 0;
            padding: 0 40px;
        }
        .key-insights {
            background: var(--bg-secondary);
            padding: 20px;
            border-radius: 8px;
            margin-top: 20px;
            border: 1px solid var(--border-color);
        }
        .key-insights h3 {
            color: #06b6d4;
            margin-bottom: 15px;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
        }
        .insight-item {
            padding: 10px 0;
            border-bottom: 1px solid var(--border-color);
            color: var(--text-secondary);
        }
        .insight-item:last-child { border-bottom: none; }
        .insight-icon {
            display: inline-block;
            width: 30px;
            text-align: center;
            font-size: 1.2em;
        }
        .visualizations-section {
            padding: 40px;
            background: var(--bg-tertiary);
        }
        .visualizations-section h2 {
            color: #06b6d4;
            margin-bottom: 30px;
            font-size: 1.8em;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
        }
        .charts-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
            gap: 30px;
            margin-bottom: 40px;
        }
        .chart-container {
            background: var(--bg-secondary);
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            border: 1px solid var(--border-color);
        }
        .chart-container h3 {
            color: #06b6d4;
            margin-bottom: 20px;
            font-size: 1.2em;
        }
        .chart-canvas {
            max-height: 300px;
        }
        .heatmap-section {
            background: var(--bg-secondary);
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            margin-bottom: 30px;
            border: 1px solid var(--border-color);
        }
        .heatmap-section h3 {
            color: #06b6d4;
            margin-bottom: 20px;
            font-size: 1.3em;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
        }
        .heatmap-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
            gap: 10px;
            margin-top: 20px;
        }
        .heatmap-cell {
            aspect-ratio: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            border-radius: 8px;
            color: white;
            font-weight: bold;
            text-align: center;
            padding: 10px;
            transition: transform 0.2s;
        }
        .heatmap-cell:hover {
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(0,0,0,0.5);
        }
        .heatmap-cell.critical { background: #dc2626; box-shadow: 0 0 15px rgba(220, 38, 38, 0.4); }
        .heatmap-cell.high { background: #ea580c; box-shadow: 0 0 15px rgba(234, 88, 12, 0.4); }
        .heatmap-cell.medium { background: #eab308; box-shadow: 0 0 15px rgba(234, 179, 8, 0.4); }
        .heatmap-cell.low { background: #16a34a; box-shadow: 0 0 15px rgba(22, 163, 74, 0.4); }
        .heatmap-score {
            font-size: 1.8em;
            margin-bottom: 5px;
        }
        .heatmap-label {
            font-size: 0.75em;
            opacity: 0.9;
        }
        .quadrant-section {
            background: var(--bg-secondary);
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            margin-bottom: 30px;
            border: 1px solid var(--border-color);
        }
        .quadrant-section h3 {
            color: #06b6d4;
            margin-bottom: 20px;
            font-size: 1.3em;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
        }
        .quadrant-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin-top: 20px;
        }
        .quadrant {
            padding: 20px;
            border-radius: 8px;
            min-height: 150px;
            background: var(--bg-tertiary);
        }
        .quadrant h4 {
            margin-bottom: 15px;
            font-size: 1.1em;
        }
        .quadrant.q1 {
            border: 2px solid #dc2626;
            box-shadow: 0 0 20px rgba(220, 38, 38, 0.2);
        }
        .quadrant.q1 h4 { color: #dc2626; }
        .quadrant.q2 {
            border: 2px solid #ea580c;
            box-shadow: 0 0 20px rgba(234, 88, 12, 0.2);
        }
        .quadrant.q2 h4 { color: #ea580c; }
        .quadrant.q3 {
            border: 2px solid #16a34a;
            box-shadow: 0 0 20px rgba(22, 163, 74, 0.2);
        }
        .quadrant.q3 h4 { color: #16a34a; }
        .quadrant.q4 {
            border: 2px solid #eab308;
            box-shadow: 0 0 20px rgba(234, 179, 8, 0.2);
        }
        .quadrant.q4 h4 { color: #eab308; }
        .quadrant-item {
            padding: 8px;
            background: var(--bg-secondary);
            border-radius: 4px;
            margin-bottom: 8px;
            font-size: 0.9em;
            color: var(--text-secondary);
            border: 1px solid var(--border-color);
        }
        .roadmap-section {
            background: var(--bg-tertiary);
            padding: 40px;
            border-top: 1px solid var(--border-color);
        }
        .roadmap-section h2 {
            color: #06b6d4;
            margin-bottom: 30px;
            font-size: 1.8em;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
        }
        .sprint-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .sprint-card {
            background: var(--bg-secondary);
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            border: 1px solid var(--border-color);
        }
        .sprint-card h3 {
            color: #06b6d4;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #06b6d4;
        }
        .sprint-item {
            padding: 12px;
            margin: 10px 0;
            background: var(--bg-tertiary);
            border-radius: 4px;
            border-left: 3px solid #06b6d4;
            color: var(--text-secondary);
            border: 1px solid var(--border-color);
        }
        .sprint-item.critical { border-left-color: #dc2626; }
        .sprint-item.high { border-left-color: #ea580c; }
        .sprint-item.medium { border-left-color: #eab308; }
        .effort-badge {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 0.8em;
            font-weight: bold;
            margin-left: 10px;
        }
        .effort-badge.small { background: #16a34a; color: #fff; }
        .effort-badge.medium { background: #ea580c; color: #fff; }
        .effort-badge.large { background: #dc2626; color: #fff; }
        .findings-section {
            padding: 40px;
            background: var(--bg-tertiary);
        }
        .findings-section h2 {
            color: #06b6d4;
            margin-bottom: 30px;
            font-size: 1.8em;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
        }
        .finding {
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 25px;
            margin: 20px 0;
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        }
        .finding.critical { border-left: 5px solid #dc2626; }
        .finding.high { border-left: 5px solid #ea580c; }
        .finding.medium { border-left: 5px solid #eab308; }
        .finding.low { border-left: 5px solid #16a34a; }
        .finding-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .finding-title {
            font-size: 1.3em;
            font-weight: bold;
            color: var(--text-primary);
        }
        .priority-badge {
            padding: 8px 20px;
            border-radius: 20px;
            color: white;
            font-size: 0.9em;
            font-weight: bold;
            box-shadow: 0 0 10px currentColor;
        }
        .priority-badge.critical { background: #dc2626; }
        .priority-badge.high { background: #ea580c; }
        .priority-badge.medium { background: #eab308; color: #000; }
        .priority-badge.low { background: #16a34a; }
        .score-display {
            font-size: 2em;
            font-weight: bold;
            color: #06b6d4;
            margin: 15px 0;
            text-shadow: 0 0 10px rgba(6, 182, 212, 0.5);
        }
        .details-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin: 20px 0;
        }
        .detail-item {
            padding: 15px;
            background: var(--bg-tertiary);
            border-radius: 6px;
            border: 1px solid var(--border-color);
        }
        .detail-label {
            font-weight: bold;
            color: var(--text-muted);
            font-size: 0.85em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .detail-value {
            color: var(--text-secondary);
            margin-top: 5px;
            font-size: 1.05em;
            word-wrap: break-word;
            overflow-wrap: break-word;
            /* Cross-browser support for smart wrapping */
            -webkit-hyphens: manual;
            -moz-hyphens: manual;
            hyphens: manual;
            white-space: normal;
        }
        .impact-factors-grid {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 15px;
            margin: 20px 0;
        }
        .impact-factor {
            text-align: center;
            padding: 15px;
            background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
            color: white;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(6, 182, 212, 0.3);
        }
        .impact-value {
            font-size: 2em;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .impact-label {
            font-size: 0.85em;
            opacity: 0.9;
        }
        .guidance-box {
            background: var(--bg-tertiary);
            padding: 20px;
            border-radius: 8px;
            border-left: 4px solid #16a34a;
            margin: 20px 0;
            border: 1px solid var(--border-color);
        }
        .guidance-box h4 {
            color: #16a34a;
            margin-bottom: 10px;
            font-weight: 600;
        }
        .guidance-box p,
        .guidance-box ul,
        .guidance-box li {
            color: var(--text-primary);
            line-height: 1.8;
        }
        @media print {
            body { background: white; padding: 0; }
            .dashboard-container { box-shadow: none; }
        }
    </style>
</head>
<body>'''
    
    def _format_header(self, report: RemediationReport) -> str:
        """Format dashboard header."""
        return f'''
<div class="header-section">
    <button class="theme-toggle" onclick="toggleTheme()" title="Toggle Dark/Light Mode">
        <span class="theme-icon">🌙</span>
    </button>
    <h1>🔐 Crypto Vulnerability Dashboard</h1>
    <p class="header-meta">Impact-Based Remediation Prioritization Report</p>
    <p class="header-meta">Generated: {report.generated_at}</p>
</div>'''
    
    def _format_executive_summary(self, report: RemediationReport) -> str:
        """Format executive summary section."""
        html = ['<div class="executive-summary">']
        html.append('<h2>📋 Executive Summary</h2>')
        
        # Summary cards
        html.append('<div class="summary-grid">')
        html.append(f'''
<div class="summary-card critical">
    <h3>{report.critical_count}</h3>
    <p>Critical Issues</p>
</div>
<div class="summary-card high">
    <h3>{report.high_count}</h3>
    <p>High Priority</p>
</div>
<div class="summary-card medium">
    <h3>{report.medium_count}</h3>
    <p>Medium Priority</p>
</div>
<div class="summary-card low">
    <h3>{report.low_count}</h3>
    <p>Low Priority</p>
</div>''')
        html.append('</div>')
        
        # Key insights
        html.append('<div class="key-insights">')
        html.append('<h3>🎯 Key Insights</h3>')
        
        if report.summary:
            avg_score = report.summary.get('average_score', 0)
            highest = report.summary.get('highest_score', 0)
            
            html.append(f'<div class="insight-item">')
            html.append(f'<span class="insight-icon">📊</span>')
            html.append(f'<strong>Average Risk Score:</strong> {avg_score:.2f}/20 ')
            if avg_score >= 15:
                html.append('(Critical attention required)')
            elif avg_score >= 10:
                html.append('(High priority remediation needed)')
            else:
                html.append('(Moderate risk level)')
            html.append('</div>')
            
            html.append(f'<div class="insight-item">')
            html.append(f'<span class="insight-icon">🔴</span>')
            html.append(f'<strong>Highest Risk Score:</strong> {highest:.2f}/20')
            html.append('</div>')
            
            # Most common algorithm
            if 'by_algorithm' in report.summary:
                algos = report.summary['by_algorithm']
                if algos:
                    most_common = max(algos.items(), key=lambda x: x[1])
                    html.append(f'<div class="insight-item">')
                    html.append(f'<span class="insight-icon">🔑</span>')
                    html.append(f'<strong>Most Common Weak Algorithm:</strong> {most_common[0]} ({most_common[1]} findings)')
                    html.append('</div>')
            
            # Most common context
            if 'by_context' in report.summary:
                contexts = report.summary['by_context']
                if contexts:
                    most_common = max(contexts.items(), key=lambda x: x[1])
                    html.append(f'<div class="insight-item">')
                    html.append(f'<span class="insight-icon">🎯</span>')
                    html.append(f'<strong>Most Affected Context:</strong> {most_common[0]} ({most_common[1]} findings)')
                    html.append('</div>')
            
            # Immediate action required
            immediate = report.critical_count + report.high_count
            html.append(f'<div class="insight-item">')
            html.append(f'<span class="insight-icon">⚡</span>')
            html.append(f'<strong>Immediate Action Required:</strong> {immediate} findings need urgent attention')
            html.append('</div>')
        
        html.append('</div>')  # Close key-insights
        html.append('</div>')  # Close executive-summary
        
        return '\n'.join(html)
    
    def _format_priority_distribution_chart(self, report: RemediationReport) -> str:
        """Format priority distribution pie chart."""
        return f'''
<div class="chart-container">
    <h3>Priority Distribution</h3>
    <canvas id="priorityChart" class="chart-canvas"></canvas>
</div>'''
    
    def _format_algorithm_distribution_chart(self, report: RemediationReport) -> str:
        """Format algorithm distribution bar chart."""
        return f'''
<div class="chart-container">
    <h3>Findings by Algorithm</h3>
    <canvas id="algorithmChart" class="chart-canvas"></canvas>
</div>'''
    
    def _format_context_distribution_chart(self, report: RemediationReport) -> str:
        """Format context distribution bar chart."""
        return f'''
<div class="chart-container">
    <h3>Findings by Usage Context</h3>
    <canvas id="contextChart" class="chart-canvas"></canvas>
</div>'''
    
    def _format_risk_score_distribution(self, report: RemediationReport) -> str:
        """Format risk score distribution histogram."""
        return f'''
<div class="chart-container">
    <h3>Risk Score Distribution</h3>
    <canvas id="riskScoreChart" class="chart-canvas"></canvas>
</div>'''
    
    def _format_risk_heatmap(self, report: RemediationReport) -> str:
        """Format risk heat map."""
        html = ['<div class="heatmap-section">']
        html.append('<h3>🔥 Risk Heat Map</h3>')
        html.append('<p>Top findings by risk score - darker colors indicate higher risk</p>')
        html.append('<div class="heatmap-grid">')
        
        # Show top 12 findings in heat map
        for pf in report.prioritized_findings[:12]:
            priority_class = pf.risk_score.priority_level.lower()
            html.append(f'''
<div class="heatmap-cell {priority_class}">
    <div class="heatmap-score">{pf.risk_score.final_score:.1f}</div>
    <div class="heatmap-label">#{pf.risk_score.priority_rank}</div>
    <div class="heatmap-label">{pf.finding.algorithm.value}</div>
</div>''')
        
        html.append('</div>')
        html.append('</div>')
        
        return '\n'.join(html)
    
    def _format_priority_quadrant(self, report: RemediationReport) -> str:
        """Format priority quadrant (Impact vs Effort)."""
        html = ['<div class="quadrant-section">']
        html.append('<h3>📍 Priority Quadrant: Impact vs. Effort</h3>')
        html.append('<p>Strategic prioritization based on impact and remediation effort</p>')
        html.append('<div class="quadrant-grid">')
        
        # Categorize findings into quadrants
        q1_items = []  # High Impact, Low Effort - DO FIRST
        q2_items = []  # High Impact, High Effort - PLAN CAREFULLY
        q3_items = []  # Low Impact, Low Effort - DO WHEN POSSIBLE
        q4_items = []  # Low Impact, High Effort - RECONSIDER
        
        for pf in report.prioritized_findings:
            high_impact = pf.risk_score.final_score >= 10
            high_effort = pf.estimated_effort and ('Large' in pf.estimated_effort or 'weeks' in pf.estimated_effort.lower())
            
            item_html = f'<div class="quadrant-item">#{pf.risk_score.priority_rank}: {pf.finding.algorithm.value} in {pf.finding.usage_context.value}</div>'
            
            if high_impact and not high_effort:
                q1_items.append(item_html)
            elif high_impact and high_effort:
                q2_items.append(item_html)
            elif not high_impact and not high_effort:
                q3_items.append(item_html)
            else:
                q4_items.append(item_html)
        
        # Q1: High Impact, Low Effort
        html.append('<div class="quadrant q1">')
        html.append('<h4>🎯 DO FIRST (High Impact, Low Effort)</h4>')
        html.extend(q1_items[:5])
        html.append('</div>')
        
        # Q2: High Impact, High Effort
        html.append('<div class="quadrant q2">')
        html.append('<h4>📋 PLAN CAREFULLY (High Impact, High Effort)</h4>')
        html.extend(q2_items[:5])
        html.append('</div>')
        
        # Q3: Low Impact, Low Effort
        html.append('<div class="quadrant q3">')
        html.append('<h4>✅ DO WHEN POSSIBLE (Low Impact, Low Effort)</h4>')
        html.extend(q3_items[:5])
        html.append('</div>')
        
        # Q4: Low Impact, High Effort
        html.append('<div class="quadrant q4">')
        html.append('<h4>⚠️ RECONSIDER (Low Impact, High Effort)</h4>')
        html.extend(q4_items[:5])
        html.append('</div>')
        
        html.append('</div>')  # Close quadrant-grid
        html.append('</div>')  # Close quadrant-section
        
        return '\n'.join(html)
    
    def _format_remediation_roadmap(self, report: RemediationReport) -> str:
        """Format remediation roadmap organized by sprint/effort."""
        html = ['<div class="roadmap-section">']
        html.append('<h2>🗺️ Remediation Roadmap</h2>')
        html.append('<p>Organized by effort level for sprint planning</p>')
        
        # Categorize by effort
        small_effort = []
        medium_effort = []
        large_effort = []
        
        for pf in report.prioritized_findings:
            if pf.estimated_effort:
                if 'Small' in pf.estimated_effort or '1-2 days' in pf.estimated_effort:
                    small_effort.append(pf)
                elif 'Large' in pf.estimated_effort or 'weeks' in pf.estimated_effort.lower():
                    large_effort.append(pf)
                else:
                    medium_effort.append(pf)
            else:
                medium_effort.append(pf)
        
        html.append('<div class="sprint-container">')
        
        # Sprint 1: Quick Wins (Small Effort)
        html.append('<div class="sprint-card">')
        html.append('<h3>🚀 Sprint 1: Quick Wins (1-2 days each)</h3>')
        for pf in small_effort[:5]:
            priority_class = pf.risk_score.priority_level.lower()
            html.append(f'''
<div class="sprint-item {priority_class}">
    <strong>#{pf.risk_score.priority_rank}</strong>: {pf.finding.algorithm.value} in {pf.finding.usage_context.value}
    <span class="effort-badge small">Quick</span>
    <br><small>Score: {pf.risk_score.final_score:.1f}</small>
</div>''')
        html.append('</div>')
        
        # Sprint 2: Medium Effort
        html.append('<div class="sprint-card">')
        html.append('<h3>📅 Sprint 2-3: Medium Effort (3-5 days each)</h3>')
        for pf in medium_effort[:5]:
            priority_class = pf.risk_score.priority_level.lower()
            html.append(f'''
<div class="sprint-item {priority_class}">
    <strong>#{pf.risk_score.priority_rank}</strong>: {pf.finding.algorithm.value} in {pf.finding.usage_context.value}
    <span class="effort-badge medium">Medium</span>
    <br><small>Score: {pf.risk_score.final_score:.1f}</small>
</div>''')
        html.append('</div>')
        
        # Sprint 3+: Large Effort
        html.append('<div class="sprint-card">')
        html.append('<h3>🏗️ Sprint 4+: Major Projects (1-2 weeks each)</h3>')
        for pf in large_effort[:5]:
            priority_class = pf.risk_score.priority_level.lower()
            html.append(f'''
<div class="sprint-item {priority_class}">
    <strong>#{pf.risk_score.priority_rank}</strong>: {pf.finding.algorithm.value} in {pf.finding.usage_context.value}
    <span class="effort-badge large">Large</span>
    <br><small>Score: {pf.risk_score.final_score:.1f}</small>
</div>''')
        html.append('</div>')
        
        html.append('</div>')  # Close sprint-container
        html.append('</div>')  # Close roadmap-section
        
        return '\n'.join(html)
    
    def _format_detailed_findings(self, report: RemediationReport) -> str:
        """Format detailed findings section."""
        html = ['<div class="findings-section">']
        
        for pf in report.prioritized_findings:
            priority_class = pf.risk_score.priority_level.lower()
            
            html.append(f'<div class="finding {priority_class}">')
            
            # Header
            html.append('<div class="finding-header">')
            html.append(f'<div class="finding-title">#{pf.risk_score.priority_rank} - {pf.finding.title}</div>')
            html.append(f'<span class="priority-badge {priority_class}">{pf.risk_score.priority_level}</span>')
            html.append('</div>')
            
            # Score
            html.append(f'<div class="score-display">Risk Score: {pf.risk_score.final_score:.2f}/20</div>')
            html.append(f'<p style="color: #7f8c8d;">Base: {pf.risk_score.base_score:.2f} × Context Multiplier: {pf.risk_score.context_multiplier:.2f}</p>')
            
            # Details
            html.append('<div class="details-grid">')
            html.append(f'<div class="detail-item"><div class="detail-label">Algorithm</div><div class="detail-value">{pf.finding.algorithm.value}</div></div>')
            html.append(f'<div class="detail-item"><div class="detail-label">Usage Context</div><div class="detail-value">{pf.finding.usage_context.value}</div></div>')
            html.append(f'<div class="detail-item"><div class="detail-label">Severity</div><div class="detail-value">{pf.finding.severity.value}</div></div>')
            if pf.estimated_effort:
                html.append(f'<div class="detail-item"><div class="detail-label">Estimated Effort</div><div class="detail-value">{pf.estimated_effort}</div></div>')
            if pf.finding.file_path:
                location = pf.finding.file_path
                if pf.finding.line_number:
                    location += f":{pf.finding.line_number}"
                # Insert <wbr> tags after path separators for better wrapping in Chrome/Safari
                location_with_breaks = location.replace('/', '/<wbr>').replace(':', ':<wbr>')
                html.append(f'<div class="detail-item"><div class="detail-label">Location</div><div class="detail-value">{location_with_breaks}</div></div>')
            html.append('</div>')
            
            # Description
            html.append(f'<p style="margin: 20px 0; line-height: 1.8;">{pf.finding.description}</p>')
            
            # Impact factors
            html.append('<h4 style="margin: 20px 0 10px;">Impact Factors</h4>')
            html.append('<div class="impact-factors-grid">')
            html.append(f'<div class="impact-factor"><div class="impact-value">{pf.risk_score.impact_factors.data_sensitivity}</div><div class="impact-label">Data Sensitivity</div></div>')
            html.append(f'<div class="impact-factor"><div class="impact-value">{pf.risk_score.impact_factors.exposure_duration}</div><div class="impact-label">Exposure Duration</div></div>')
            html.append(f'<div class="impact-factor"><div class="impact-value">{pf.risk_score.impact_factors.exploitability}</div><div class="impact-label">Exploitability</div></div>')
            html.append(f'<div class="impact-factor"><div class="impact-value">{pf.risk_score.impact_factors.blast_radius}</div><div class="impact-label">Blast Radius</div></div>')
            html.append(f'<div class="impact-factor"><div class="impact-value">{pf.risk_score.impact_factors.algorithm_weakness}</div><div class="impact-label">Algorithm Weakness</div></div>')
            html.append('</div>')
            
            # Remediation guidance
            if pf.remediation_guidance:
                html.append('<div class="guidance-box">')
                html.append('<h4>💡 Remediation Guidance</h4>')
                html.append(f'<p>{pf.remediation_guidance}</p>')
                html.append('</div>')
            
            html.append('</div>')  # Close finding
        
        html.append('</div>')  # Close findings-section
        
        return '\n'.join(html)
    
    def _get_chart_scripts(self, report: RemediationReport) -> str:
        """Generate JavaScript for Chart.js visualizations."""
        # Prepare data
        priority_data = {
            'CRITICAL': report.critical_count,
            'HIGH': report.high_count,
            'MEDIUM': report.medium_count,
            'LOW': report.low_count
        }
        
        algorithm_data = report.summary.get('by_algorithm', {}) if report.summary else {}
        context_data = report.summary.get('by_context', {}) if report.summary else {}
        
        # Risk score distribution
        score_ranges = {'0-5': 0, '5-10': 0, '10-15': 0, '15-20': 0}
        for pf in report.prioritized_findings:
            score = pf.risk_score.final_score
            if score < 5:
                score_ranges['0-5'] += 1
            elif score < 10:
                score_ranges['5-10'] += 1
            elif score < 15:
                score_ranges['10-15'] += 1
            else:
                score_ranges['15-20'] += 1
        
        return f'''
<script>
// Priority Distribution Pie Chart
const priorityCtx = document.getElementById('priorityChart').getContext('2d');
new Chart(priorityCtx, {{
    type: 'doughnut',
    data: {{
        labels: {list(priority_data.keys())},
        datasets: [{{
            data: {list(priority_data.values())},
            backgroundColor: ['#e74c3c', '#e67e22', '#f39c12', '#95a5a6'],
            borderWidth: 2,
            borderColor: '#fff'
        }}]
    }},
    options: {{
        responsive: true,
        maintainAspectRatio: true,
        plugins: {{
            legend: {{
                position: 'bottom'
            }},
            title: {{
                display: false
            }}
        }}
    }}
}});

// Algorithm Distribution Bar Chart
const algorithmCtx = document.getElementById('algorithmChart').getContext('2d');
new Chart(algorithmCtx, {{
    type: 'bar',
    data: {{
        labels: {list(algorithm_data.keys())},
        datasets: [{{
            label: 'Findings',
            data: {list(algorithm_data.values())},
            backgroundColor: '#3498db',
            borderColor: '#2980b9',
            borderWidth: 1
        }}]
    }},
    options: {{
        responsive: true,
        maintainAspectRatio: true,
        plugins: {{
            legend: {{
                display: false
            }}
        }},
        scales: {{
            y: {{
                beginAtZero: true,
                ticks: {{
                    stepSize: 1
                }}
            }}
        }}
    }}
}});

// Context Distribution Bar Chart
const contextCtx = document.getElementById('contextChart').getContext('2d');
new Chart(contextCtx, {{
    type: 'bar',
    data: {{
        labels: {list(context_data.keys())},
        datasets: [{{
            label: 'Findings',
            data: {list(context_data.values())},
            backgroundColor: '#9b59b6',
            borderColor: '#8e44ad',
            borderWidth: 1
        }}]
    }},
    options: {{
        responsive: true,
        maintainAspectRatio: true,
        plugins: {{
            legend: {{
                display: false
            }}
        }},
        scales: {{
            y: {{
                beginAtZero: true,
                ticks: {{
                    stepSize: 1
                }}
            }}
        }}
    }}
}});

// Risk Score Distribution
const riskScoreCtx = document.getElementById('riskScoreChart').getContext('2d');
new Chart(riskScoreCtx, {{
    type: 'bar',
    data: {{
        labels: {list(score_ranges.keys())},
        datasets: [{{
            label: 'Number of Findings',
            data: {list(score_ranges.values())},
            backgroundColor: ['#95a5a6', '#f39c12', '#e67e22', '#e74c3c'],
            borderWidth: 1
        }}]
    }},
    options: {{
        responsive: true,
        maintainAspectRatio: true,
        plugins: {{
            legend: {{
                display: false
            }}
        }},
        scales: {{
            y: {{
                beginAtZero: true,
                ticks: {{
                    stepSize: 1
                }}
            }}
        }}
    }}
</script>'''
    
    def _calculate_dimension_scores(self, report: RemediationReport) -> Dict[str, tuple]:
        """Calculate scores for each of the 5 security dimensions.
        Returns: Dict with dimension name -> (score 0-10, count, severity_class)
        """
        if not report.prioritized_findings:
            return {
                'data_sensitivity': (0.0, 0, 'low'),
                'exposure_duration': (0.0, 0, 'low'),
                'exploitability': (0.0, 0, 'low'),
                'blast_radius': (0.0, 0, 'low'),
                'usage_context': (0.0, 0, 'low')
            }
        
        # Aggregate scores
        total_findings = len(report.prioritized_findings)
        data_sens_sum = sum(pf.risk_score.impact_factors.data_sensitivity for pf in report.prioritized_findings)
        exposure_sum = sum(pf.risk_score.impact_factors.exposure_duration for pf in report.prioritized_findings)
        exploit_sum = sum(pf.risk_score.impact_factors.exploitability for pf in report.prioritized_findings)
        blast_sum = sum(pf.risk_score.impact_factors.blast_radius for pf in report.prioritized_findings)
        algo_sum = sum(pf.risk_score.impact_factors.algorithm_weakness for pf in report.prioritized_findings)
        
        # Calculate averages
        data_sens_avg = data_sens_sum / total_findings
        exposure_avg = exposure_sum / total_findings
        exploit_avg = exploit_sum / total_findings
        blast_avg = blast_sum / total_findings
        usage_avg = algo_sum / total_findings
        
        # Count high-risk items (score >= 7)
        data_sens_count = sum(1 for pf in report.prioritized_findings if pf.risk_score.impact_factors.data_sensitivity >= 7)
        exposure_count = sum(1 for pf in report.prioritized_findings if pf.risk_score.impact_factors.exposure_duration >= 7)
        exploit_count = sum(1 for pf in report.prioritized_findings if pf.risk_score.impact_factors.exploitability >= 7)
        blast_count = sum(1 for pf in report.prioritized_findings if pf.risk_score.impact_factors.blast_radius >= 7)
        
        # Count by usage context
        auth_count = sum(1 for pf in report.prioritized_findings if 'auth' in pf.finding.usage_context.value.lower())
        storage_count = sum(1 for pf in report.prioritized_findings if 'storage' in pf.finding.usage_context.value.lower())
        external_count = sum(1 for pf in report.prioritized_findings if pf.finding.is_external_facing)
        
        def get_severity_class(score: float) -> str:
            if score >= 8: return 'critical'
            elif score >= 6: return 'high'
            elif score >= 4: return 'medium'
            else: return 'low'
        
        return {
            'data_sensitivity': (data_sens_avg, data_sens_count, get_severity_class(data_sens_avg)),
            'exposure_duration': (exposure_avg, exposure_count, get_severity_class(exposure_avg)),
            'exploitability': (exploit_avg, exploit_count, get_severity_class(exploit_avg)),
            'blast_radius': (blast_avg, blast_count, get_severity_class(blast_avg)),
            'usage_context': (usage_avg, auth_count + storage_count + external_count, get_severity_class(usage_avg))
        }
    
    def _format_security_scorecard(self, report: RemediationReport) -> str:
        """Format the 5-dimension security scorecard."""
        dimensions = self._calculate_dimension_scores(report)
        
        html = ['<div class="security-scorecard">']
        html.append('<h2>🎯 Security Posture Scorecard</h2>')
        html.append('<div class="scorecard-grid">')
        
        # Data Sensitivity
        score, count, severity = dimensions['data_sensitivity']
        html.append(f'''
<div class="dimension-card">
    <div class="dimension-header">
        <div class="dimension-title">Data Sensitivity</div>
        <div class="dimension-icon">📊</div>
    </div>
    <div class="dimension-score">{score:.1f}<span style="font-size:0.5em;color:#94a3b8;">/10</span></div>
    <div class="dimension-bar">
        <div class="dimension-bar-fill {severity}" style="width: {score*10}%"></div>
    </div>
    <div class="dimension-detail">{count} findings protecting sensitive data</div>
</div>''')
        
        # Exposure Duration
        score, count, severity = dimensions['exposure_duration']
        html.append(f'''
<div class="dimension-card">
    <div class="dimension-header">
        <div class="dimension-title">Exposure Duration</div>
        <div class="dimension-icon">⏱️</div>
    </div>
    <div class="dimension-score">{score:.1f}<span style="font-size:0.5em;color:#94a3b8;">/10</span></div>
    <div class="dimension-bar">
        <div class="dimension-bar-fill {severity}" style="width: {score*10}%"></div>
    </div>
    <div class="dimension-detail">{count} long-term exposure risks</div>
</div>''')
        
        # Exploitability
        score, count, severity = dimensions['exploitability']
        html.append(f'''
<div class="dimension-card">
    <div class="dimension-header">
        <div class="dimension-title">Exploitability</div>
        <div class="dimension-icon">🎯</div>
    </div>
    <div class="dimension-score">{score:.1f}<span style="font-size:0.5em;color:#94a3b8;">/10</span></div>
    <div class="dimension-bar">
        <div class="dimension-bar-fill {severity}" style="width: {score*10}%"></div>
    </div>
    <div class="dimension-detail">{count} easily exploitable vulnerabilities</div>
</div>''')
        
        # Blast Radius
        score, count, severity = dimensions['blast_radius']
        html.append(f'''
<div class="dimension-card">
    <div class="dimension-header">
        <div class="dimension-title">Blast Radius</div>
        <div class="dimension-icon">💥</div>
    </div>
    <div class="dimension-score">{score:.1f}<span style="font-size:0.5em;color:#94a3b8;">/10</span></div>
    <div class="dimension-bar">
        <div class="dimension-bar-fill {severity}" style="width: {score*10}%"></div>
    </div>
    <div class="dimension-detail">{count} wide-impact vulnerabilities</div>
</div>''')
        
        # Usage Context
        score, count, severity = dimensions['usage_context']
        html.append(f'''
<div class="dimension-card">
    <div class="dimension-header">
        <div class="dimension-title">Usage Context Risk</div>
        <div class="dimension-icon">🔑</div>
    </div>
    <div class="dimension-score">{score:.1f}<span style="font-size:0.5em;color:#94a3b8;">/10</span></div>
    <div class="dimension-bar">
        <div class="dimension-bar-fill {severity}" style="width: {score*10}%"></div>
    </div>
    <div class="dimension-detail">{count} critical context findings</div>
</div>''')
        
        html.append('</div></div>')
        return '\n'.join(html)
    
    def _format_immediate_actions(self, report: RemediationReport) -> str:
        """Format top 5 critical findings requiring immediate action."""
        html = ['<div class="immediate-actions">']
        html.append('<h2>⚡ Immediate Actions Required</h2>')
        html.append('<div class="action-cards">')
        
        # Get top 5 findings
        top_findings = report.prioritized_findings[:5]
        
        for pf in top_findings:
            priority_class = pf.risk_score.priority_level.lower()
            
            html.append(f'<div class="action-card {priority_class}">')
            html.append('<div class="action-header">')
            html.append(f'<div class="action-rank">#{pf.risk_score.priority_rank}</div>')
            html.append(f'<div class="action-title">{pf.finding.title}</div>')
            html.append(f'<span class="action-badge {priority_class}">{pf.risk_score.priority_level}</span>')
            html.append('</div>')
            
            html.append('<div class="action-metrics">')
            html.append(f'<div class="action-metric"><div class="action-metric-label">Risk Score</div><div class="action-metric-value">{pf.risk_score.final_score:.1f}/20</div></div>')
            html.append(f'<div class="action-metric"><div class="action-metric-label">Algorithm</div><div class="action-metric-value">{pf.finding.algorithm.value}</div></div>')
            html.append(f'<div class="action-metric"><div class="action-metric-label">Context</div><div class="action-metric-value">{pf.finding.usage_context.value}</div></div>')
            if pf.estimated_effort:
                html.append(f'<div class="action-metric"><div class="action-metric-label">Effort</div><div class="action-metric-value">{pf.estimated_effort}</div></div>')
            html.append(f'<div class="action-metric"><div class="action-metric-label">Exploitability</div><div class="action-metric-value">{pf.risk_score.impact_factors.exploitability}/10</div></div>')
            html.append('</div>')
            
            html.append('</div>')
        
        html.append('</div></div>')
        return '\n'.join(html)
    
    def _format_risk_matrix(self, report: RemediationReport) -> str:
        """Format interactive risk matrix (Impact vs Exploitability)."""
        # Categorize findings into matrix cells
        matrix = {
            'high_high': [], 'high_med': [], 'high_low': [],
            'med_high': [], 'med_med': [], 'med_low': [],
            'low_high': [], 'low_med': [], 'low_low': []
        }
        
        for pf in report.prioritized_findings:
            impact = pf.risk_score.final_score
            exploit = pf.risk_score.impact_factors.exploitability
            
            impact_level = 'high' if impact >= 13 else 'med' if impact >= 7 else 'low'
            exploit_level = 'high' if exploit >= 7 else 'med' if exploit >= 4 else 'low'
            
            key = f'{impact_level}_{exploit_level}'
            matrix[key].append(pf)
        
        html = ['<div class="risk-matrix">']
        html.append('<h3>Risk Matrix: Impact vs. Exploitability</h3>')
        html.append('<div class="matrix-grid">')
        
        # Labels
        html.append('<div class="matrix-label"></div>')
        html.append('<div class="matrix-label">Low Exploit</div>')
        html.append('<div class="matrix-label">Med Exploit</div>')
        html.append('<div class="matrix-label">High Exploit</div>')
        
        # High Impact Row
        html.append('<div class="matrix-label">High Impact</div>')
        html.append(f'<div class="matrix-cell medium" title="High Impact, Low Exploitability"><div class="matrix-count">{len(matrix["high_low"])}</div><div class="matrix-label-text">Plan Carefully</div></div>')
        html.append(f'<div class="matrix-cell high" title="High Impact, Med Exploitability"><div class="matrix-count">{len(matrix["high_med"])}</div><div class="matrix-label-text">High Priority</div></div>')
        html.append(f'<div class="matrix-cell critical" title="High Impact, High Exploitability"><div class="matrix-count">{len(matrix["high_high"])}</div><div class="matrix-label-text">CRITICAL</div></div>')
        
        # Medium Impact Row
        html.append('<div class="matrix-label">Med Impact</div>')
        html.append(f'<div class="matrix-cell low" title="Med Impact, Low Exploitability"><div class="matrix-count">{len(matrix["med_low"])}</div><div class="matrix-label-text">Low Priority</div></div>')
        html.append(f'<div class="matrix-cell medium" title="Med Impact, Med Exploitability"><div class="matrix-count">{len(matrix["med_med"])}</div><div class="matrix-label-text">Medium</div></div>')
        html.append(f'<div class="matrix-cell high" title="Med Impact, High Exploitability"><div class="matrix-count">{len(matrix["med_high"])}</div><div class="matrix-label-text">High Priority</div></div>')
        
        # Low Impact Row
        html.append('<div class="matrix-label">Low Impact</div>')
        html.append(f'<div class="matrix-cell low" title="Low Impact, Low Exploitability"><div class="matrix-count">{len(matrix["low_low"])}</div><div class="matrix-label-text">Monitor</div></div>')
        html.append(f'<div class="matrix-cell low" title="Low Impact, Med Exploitability"><div class="matrix-count">{len(matrix["low_med"])}</div><div class="matrix-label-text">Low Priority</div></div>')
        html.append(f'<div class="matrix-cell medium" title="Low Impact, High Exploitability"><div class="matrix-count">{len(matrix["low_high"])}</div><div class="matrix-label-text">Review</div></div>')
        
        html.append('</div></div>')
        return '\n'.join(html)
    
    def _format_exposure_timeline(self, report: RemediationReport) -> str:
        """Format exposure timeline chart."""
        html = ['<div class="timeline-chart">']
        html.append('<h3>Exposure Duration Analysis</h3>')
        html.append('<canvas id="timelineChart" class="chart-canvas"></canvas>')
        html.append('</div>')
        return '\n'.join(html)
    
    def _get_interactive_scripts(self, report: RemediationReport) -> str:
        """Generate JavaScript for interactivity and charts."""
        # Prepare timeline data
        exposure_data = {'ephemeral': 0, 'short_term': 0, 'medium_term': 0, 'long_term': 0, 'persistent': 0}
        for pf in report.prioritized_findings:
            if pf.finding.exposure_duration:
                exposure_data[pf.finding.exposure_duration.value] = exposure_data.get(pf.finding.exposure_duration.value, 0) + 1
        
        return f'''
<script>
// Theme Toggle Function
function toggleTheme() {{
    const html = document.documentElement;
    const currentTheme = html.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    const icon = document.querySelector('.theme-icon');
    
    html.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    
    // Update icon
    icon.textContent = newTheme === 'light' ? '☀️' : '🌙';
}}

// Load saved theme on page load
(function() {{
    const savedTheme = localStorage.getItem('theme') || 'dark';
    const icon = document.querySelector('.theme-icon');
    document.documentElement.setAttribute('data-theme', savedTheme);
    if (icon) {{
        icon.textContent = savedTheme === 'light' ? '☀️' : '🌙';
    }}
}})();

// Toggle collapsible sections
function toggleSection(sectionId) {{
    const content = document.getElementById(sectionId + '-content');
    const icon = event.currentTarget.querySelector('.toggle-icon');
    
    content.classList.toggle('collapsed');
    icon.classList.toggle('rotated');
}}

// Filter findings
function filterFindings(priority) {{
    const cards = document.querySelectorAll('.finding-card');
    const buttons = document.querySelectorAll('.filter-btn');
    
    buttons.forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    
    cards.forEach(card => {{
        if (priority === 'all' || card.dataset.priority === priority) {{
            card.style.display = 'block';
        }} else {{
            card.style.display = 'none';
        }}
    }});
}}

// Timeline Chart
const timelineCtx = document.getElementById('timelineChart');
if (timelineCtx) {{
    new Chart(timelineCtx.getContext('2d'), {{
        type: 'bar',
        data: {{
            labels: ['Ephemeral', 'Short Term', 'Medium Term', 'Long Term', 'Persistent'],
            datasets: [{{
                label: 'Number of Findings',
                data: {list(exposure_data.values())},
                backgroundColor: ['#16a34a', '#eab308', '#ea580c', '#dc2626', '#7f1d1d'],
                borderWidth: 0
            }}]
        }},
        options: {{
            responsive: true,
            maintainAspectRatio: true,
            plugins: {{
                legend: {{ display: false }},
                title: {{
                    display: true,
                    text: 'Findings by Exposure Duration',
                    color: '#94a3b8'
                }}
            }},
            scales: {{
                y: {{
                    beginAtZero: true,
                    ticks: {{ stepSize: 1, color: '#94a3b8' }},
                    grid: {{ color: '#334155' }}
                }},
                x: {{
                    ticks: {{ color: '#94a3b8' }},
                    grid: {{ color: '#334155' }}
                }}
            }}
        }}
    }});
}}
</script>'''