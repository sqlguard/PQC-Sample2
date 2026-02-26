
"""
Tabbed dashboard formatter - Clean, organized interface with logical grouping.
Addresses user feedback about overwhelming single-page dashboard.
"""
from typing import List, Dict, Any, Optional
from ..models import RemediationReport, PrioritizedFinding
import json


class TabbedDashboardFormatter:
    """Formats reports as a clean tabbed dashboard with organized sections."""
    
    def __init__(self, theme: str = "dark"):
        """
        Initialize the tabbed dashboard formatter.
        
        Args:
            theme: Default theme ('dark' or 'light')
        """
        self.theme = theme
    
    def format(self, report: RemediationReport, enable_animations: bool = True) -> str:
        """
        Format report as a tabbed HTML dashboard.
        
        Args:
            report: RemediationReport to format
            enable_animations: Whether to enable animations
            
        Returns:
            HTML formatted string with tabbed interface
        """
        html_parts = []
        
        # HTML header with CSS
        html_parts.append(self._get_html_header(enable_animations))
        
        # Main container
        html_parts.append('<div class="dashboard-container" data-theme="dark">')
        
        # Top navigation bar
        html_parts.append(self._format_top_nav(report))
        
        # Tab navigation
        html_parts.append(self._format_tab_navigation())
        
        # Tab content
        html_parts.append('<div class="tab-content">')
        
        # Tab 1: Overview (default active)
        html_parts.append(self._format_overview_tab(report))
        
        # Tab 2: Findings
        html_parts.append(self._format_findings_tab(report))
        
        # Tab 3: Analytics
        html_parts.append(self._format_analytics_tab(report))
        
        # Tab 4: Workflow
        html_parts.append(self._format_workflow_tab(report))
        
        # Tab 5: Compliance
        html_parts.append(self._format_compliance_tab(report))
        
        # Tab 6: Reports
        html_parts.append(self._format_reports_tab(report))
        
        html_parts.append('</div>')  # Close tab-content
        
        # JavaScript for interactivity
        html_parts.append(self._get_javascript(report, enable_animations))
        
        html_parts.append('</div>')  # Close container
        html_parts.append('</body></html>')
        
        return '\n'.join(html_parts)
    
    def _get_html_header(self, enable_animations: bool) -> str:
        """Get HTML header with modern CSS for tabbed interface."""
        animation_css = """
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes slideIn {
            from { transform: translateX(-20px); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        """ if enable_animations else ""
        
        return f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crypto Vulnerability Dashboard</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
        /* CSS Variables for Theming */
        :root {{
            --primary-color: #3b82f6;
            --secondary-color: #8b5cf6;
            --success-color: #10b981;
            --warning-color: #f59e0b;
            --danger-color: #ef4444;
            --info-color: #06b6d4;
            
            --bg-primary: #0f172a;
            --bg-secondary: #1e293b;
            --bg-tertiary: #334155;
            --text-primary: #f1f5f9;
            --text-secondary: #cbd5e1;
            --text-muted: #94a3b8;
            
            --border-color: #334155;
            --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
            --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.4);
            
            --critical-color: #dc2626;
            --high-color: #f97316;
            --medium-color: #eab308;
            --low-color: #22c55e;
            
            --spacing-xs: 0.25rem;
            --spacing-sm: 0.5rem;
            --spacing-md: 1rem;
            --spacing-lg: 1.5rem;
            --spacing-xl: 2rem;
            
            --radius-sm: 0.375rem;
            --radius-md: 0.5rem;
            --radius-lg: 0.75rem;
            --radius-xl: 1rem;
            
            --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }}
        
        [data-theme="light"] {{
            --bg-primary: #ffffff;
            --bg-secondary: #f8fafc;
            --bg-tertiary: #e2e8f0;
            --text-primary: #0f172a;
            --text-secondary: #475569;
            --text-muted: #64748b;
            --border-color: #e2e8f0;
            --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.15);
        }}
        
        /* Reset and Base Styles */
        * {{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }}
        
        body {{
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: var(--bg-primary);
            color: var(--text-primary);
            line-height: 1.6;
            transition: var(--transition);
        }}
        
        /* Dashboard Container */
        .dashboard-container {{
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }}
        
        /* Top Navigation */
        .top-nav {{
            background: var(--bg-secondary);
            border-bottom: 1px solid var(--border-color);
            padding: var(--spacing-md) var(--spacing-xl);
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            z-index: 100;
            box-shadow: var(--shadow);
        }}
        
        .nav-brand {{
            display: flex;
            align-items: center;
            gap: var(--spacing-md);
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-primary);
        }}
        
        .nav-brand i {{
            color: var(--primary-color);
            font-size: 1.5rem;
        }}
        
        .nav-actions {{
            display: flex;
            align-items: center;
            gap: var(--spacing-md);
        }}
        
        .nav-stat {{
            display: flex;
            align-items: center;
            gap: var(--spacing-sm);
            padding: var(--spacing-sm) var(--spacing-md);
            background: var(--bg-tertiary);
            border-radius: var(--radius-md);
            font-size: 0.875rem;
        }}
        
        .nav-stat i {{
            font-size: 1rem;
        }}
        
        .nav-stat.critical {{ color: var(--critical-color); }}
        .nav-stat.high {{ color: var(--high-color); }}
        .nav-stat.medium {{ color: var(--medium-color); }}
        .nav-stat.low {{ color: var(--low-color); }}
        
        /* Theme Toggle Button */
        .theme-toggle {{
            background: var(--bg-tertiary);
            border: none;
            color: var(--text-primary);
            padding: var(--spacing-sm) var(--spacing-md);
            border-radius: var(--radius-md);
            cursor: pointer;
            font-size: 1.125rem;
            transition: var(--transition);
        }}
        
        .theme-toggle:hover {{
            background: var(--primary-color);
            transform: scale(1.05);
        }}
        
        /* Tab Navigation */
        .tab-nav {{
            background: var(--bg-secondary);
            border-bottom: 2px solid var(--border-color);
            padding: 0 var(--spacing-xl);
            display: flex;
            gap: var(--spacing-sm);
            overflow-x: auto;
            position: sticky;
            top: 73px;
            z-index: 99;
        }}
        
        .tab-nav::-webkit-scrollbar {{
            height: 4px;
        }}
        
        .tab-nav::-webkit-scrollbar-thumb {{
            background: var(--primary-color);
            border-radius: 2px;
        }}
        
        .tab-button {{
            background: transparent;
            border: none;
            color: var(--text-secondary);
            padding: var(--spacing-md) var(--spacing-lg);
            cursor: pointer;
            font-size: 0.9375rem;
            font-weight: 500;
            border-bottom: 2px solid transparent;
            transition: var(--transition);
            white-space: nowrap;
            display: flex;
            align-items: center;
            gap: var(--spacing-sm);
        }}
        
        .tab-button:hover {{
            color: var(--text-primary);
            background: var(--bg-tertiary);
        }}
        
        .tab-button.active {{
            color: var(--primary-color);
            border-bottom-color: var(--primary-color);
        }}
        
        .tab-button i {{
            font-size: 1rem;
        }}
        
        /* Tab Content */
        .tab-content {{
            flex: 1;
            padding: var(--spacing-xl);
            max-width: 1400px;
            margin: 0 auto;
            width: 100%;
        }}
        
        .tab-pane {{
            display: none;
            animation: fadeIn 0.3s ease-in-out;
        }}
        
        .tab-pane.active {{
            display: block;
        }}
        
        /* Cards */
        .card {{
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-lg);
            padding: var(--spacing-lg);
            box-shadow: var(--shadow);
            transition: var(--transition);
        }}
        
        .card:hover {{
            box-shadow: var(--shadow-lg);
            transform: translateY(-2px);
        }}
        
        .card-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--spacing-lg);
            padding-bottom: var(--spacing-md);
            border-bottom: 1px solid var(--border-color);
        }}
        
        .card-title {{
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--text-primary);
            display: flex;
            align-items: center;
            gap: var(--spacing-sm);
        }}
        
        .card-title i {{
            color: var(--primary-color);
        }}
        
        /* Grid Layouts */
        .grid {{
            display: grid;
            gap: var(--spacing-lg);
        }}
        
        .grid-2 {{ grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); }}
        .grid-3 {{ grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); }}
        .grid-4 {{ grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); }}
        
        /* Stat Cards */
        .stat-card {{
            background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-lg);
            padding: var(--spacing-lg);
            text-align: center;
            transition: var(--transition);
            position: relative;
            overflow: hidden;
        }}
        
        .stat-card::before {{
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: var(--primary-color);
        }}
        
        .stat-card.critical::before {{ background: var(--critical-color); }}
        .stat-card.high::before {{ background: var(--high-color); }}
        .stat-card.medium::before {{ background: var(--medium-color); }}
        .stat-card.low::before {{ background: var(--low-color); }}
        
        .stat-card:hover {{
            transform: translateY(-4px);
            box-shadow: var(--shadow-lg);
        }}
        
        .stat-icon {{
            font-size: 2rem;
            margin-bottom: var(--spacing-sm);
        }}
        
        .stat-card.critical .stat-icon {{ color: var(--critical-color); }}
        .stat-card.high .stat-icon {{ color: var(--high-color); }}
        .stat-card.medium .stat-icon {{ color: var(--medium-color); }}
        .stat-card.low .stat-icon {{ color: var(--low-color); }}
        
        .stat-value {{
            font-size: 2.5rem;
            font-weight: 700;
            color: var(--text-primary);
            line-height: 1;
            margin-bottom: var(--spacing-xs);
        }}
        
        .stat-label {{
            font-size: 0.875rem;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            font-weight: 500;
        }}
        
        .stat-trend {{
            margin-top: var(--spacing-sm);
            font-size: 0.8125rem;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--spacing-xs);
        }}
        
        .stat-trend.up {{ color: var(--danger-color); }}
        .stat-trend.down {{ color: var(--success-color); }}
        .stat-trend.neutral {{ color: var(--text-muted); }}
        
        /* Buttons */
        .btn {{
            padding: var(--spacing-sm) var(--spacing-lg);
            border: none;
            border-radius: var(--radius-md);
            font-size: 0.875rem;
            font-weight: 500;
            cursor: pointer;
            transition: var(--transition);
            display: inline-flex;
            align-items: center;
            gap: var(--spacing-sm);
            text-decoration: none;
        }}
        
        .btn:hover {{
            transform: translateY(-1px);
            box-shadow: var(--shadow);
        }}
        
        .btn-primary {{
            background: var(--primary-color);
            color: white;
        }}
        
        .btn-primary:hover {{
            background: #2563eb;
        }}
        
        .btn-secondary {{
            background: var(--bg-tertiary);
            color: var(--text-primary);
        }}
        
        .btn-secondary:hover {{
            background: var(--bg-secondary);
        }}
        
        .btn-success {{
            background: var(--success-color);
            color: white;
        }}
        
        .btn-danger {{
            background: var(--danger-color);
            color: white;
        }}
        
        .btn-sm {{
            padding: var(--spacing-xs) var(--spacing-md);
            font-size: 0.8125rem;
        }}
        
        /* Tables */
        .table-container {{
            overflow-x: auto;
            border-radius: var(--radius-lg);
            border: 1px solid var(--border-color);
        }}
        
        table {{
            width: 100%;
            border-collapse: collapse;
            background: var(--bg-secondary);
        }}
        
        thead {{
            background: var(--bg-tertiary);
            position: sticky;
            top: 0;
            z-index: 10;
        }}
        
        th {{
            padding: var(--spacing-md);
            text-align: left;
            font-weight: 600;
            color: var(--text-primary);
            font-size: 0.875rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            border-bottom: 2px solid var(--border-color);
        }}
        
        td {{
            padding: var(--spacing-md);
            border-bottom: 1px solid var(--border-color);
            color: var(--text-secondary);
            font-size: 0.875rem;
        }}
        
        tr:hover {{
            background: var(--bg-tertiary);
        }}
        
        /* Priority Badges */
        .badge {{
            display: inline-flex;
            align-items: center;
            gap: var(--spacing-xs);
            padding: var(--spacing-xs) var(--spacing-md);
            border-radius: var(--radius-sm);
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }}
        
        .badge-critical {{
            background: rgba(220, 38, 38, 0.1);
            color: var(--critical-color);
            border: 1px solid var(--critical-color);
        }}
        
        .badge-high {{
            background: rgba(249, 115, 22, 0.1);
            color: var(--high-color);
            border: 1px solid var(--high-color);
        }}
        
        .badge-medium {{
            background: rgba(234, 179, 8, 0.1);
            color: var(--medium-color);
            border: 1px solid var(--medium-color);
        }}
        
        .badge-low {{
            background: rgba(34, 197, 94, 0.1);
            color: var(--low-color);
            border: 1px solid var(--low-color);
        }}
        
        /* Charts */
        .chart-container {{
            position: relative;
            height: 300px;
            margin-top: var(--spacing-md);
        }}
        
        .chart-container.large {{
            height: 400px;
        }}
        
        /* Search and Filters */
        .search-bar {{
            display: flex;
            gap: var(--spacing-md);
            margin-bottom: var(--spacing-lg);
        }}
        
        .search-input {{
            flex: 1;
            padding: var(--spacing-md);
            background: var(--bg-tertiary);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            font-size: 0.875rem;
        }}
        
        .search-input:focus {{
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }}
        
        .filter-group {{
            display: flex;
            gap: var(--spacing-sm);
            flex-wrap: wrap;
        }}
        
        .filter-chip {{
            padding: var(--spacing-sm) var(--spacing-md);
            background: var(--bg-tertiary);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            font-size: 0.8125rem;
            cursor: pointer;
            transition: var(--transition);
        }}
        
        .filter-chip:hover {{
            background: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }}
        
        .filter-chip.active {{
            background: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }}
        
        /* Empty State */
        .empty-state {{
            text-align: center;
            padding: var(--spacing-xl) var(--spacing-lg);
            color: var(--text-muted);
        }}
        
        .empty-state i {{
            font-size: 3rem;
            margin-bottom: var(--spacing-md);
            opacity: 0.5;
        }}
        
        /* Loading State */
        .loading {{
            display: flex;
            align-items: center;
            justify-content: center;
            padding: var(--spacing-xl);
            color: var(--text-muted);
        }}
        
        .spinner {{
            border: 3px solid var(--border-color);
            border-top-color: var(--primary-color);
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
        }}
        
        @keyframes spin {{
            to {{ transform: rotate(360deg); }}
        }}
        
        /* Responsive Design */
        @media (max-width: 768px) {{
            .tab-content {{
                padding: var(--spacing-md);
            }}
            
            .grid-2, .grid-3, .grid-4 {{
                grid-template-columns: 1fr;
            }}
            
            .top-nav {{
                flex-direction: column;
                gap: var(--spacing-md);
                align-items: flex-start;
            }}
            
            .nav-actions {{
                width: 100%;
                justify-content: space-between;
            }}
        }}
        
        /* Animations */
        {animation_css}
        
        /* Print Styles */
        @media print {{
            .top-nav, .tab-nav, .btn {{
                display: none !important;
            }}
            
            .tab-pane {{
                display: block !important;
                page-break-after: always;
            }}
        }}
    </style>
</head>
<body>
"""
    
    def _format_top_nav(self, report: RemediationReport) -> str:
        """Format top navigation bar with quick stats."""
        stats = report.statistics
        
        critical_count = stats.get('priority_distribution', {}).get('CRITICAL', 0)
        high_count = stats.get('priority_distribution', {}).get('HIGH', 0)
        medium_count = stats.get('priority_distribution', {}).get('MEDIUM', 0)
        low_count = stats.get('priority_distribution', {}).get('LOW', 0)
        
        return f"""
<nav class="top-nav">
    <div class="nav-brand">
        <i class="fas fa-shield-alt"></i>
        <span>Crypto Vulnerability Dashboard</span>
    </div>
    <div class="nav-actions">
        <div class="nav-stat critical">
            <i class="fas fa-exclamation-circle"></i>
            <span>{critical_count} Critical</span>
        </div>
        <div class="nav-stat high">
            <i class="fas fa-exclamation-triangle"></i>
            <span>{high_count} High</span>
        </div>
        <div class="nav-stat medium">
            <i class="fas fa-info-circle"></i>
            <span>{medium_count} Medium</span>
        </div>
        <div class="nav-stat low">
            <i class="fas fa-check-circle"></i>
            <span>{low_count} Low</span>
        </div>
        <button class="theme-toggle" onclick="toggleTheme()" title="Toggle theme (Ctrl+K)">
            <i class="fas fa-moon"></i>
        </button>
    </div>
</nav>
"""
    
    def _format_tab_navigation(self) -> str:
        """Format tab navigation buttons."""
        return """
<div class="tab-nav">
    <button class="tab-button active" onclick="switchTab('overview')">
        <i class="fas fa-home"></i>
        <span>Overview</span>
    </button>
    <button class="tab-button" onclick="switchTab('findings')">
        <i class="fas fa-list"></i>
        <span>Findings</span>
    </button>
    <button class="tab-button" onclick="switchTab('analytics')">
        <i class="fas fa-chart-line"></i>
        <span>Analytics</span>
    </button>
    <button class="tab-button" onclick="switchTab('workflow')">
        <i class="fas fa-tasks"></i>
        <span>Workflow</span>
    </button>
    <button class="tab-button" onclick="switchTab('compliance')">
        <i class="fas fa-clipboard-check"></i>
        <span>Compliance</span>
    </button>
    <button class="tab-button" onclick="switchTab('reports')">
        <i class="fas fa-file-alt"></i>
        <span>Reports</span>
    </button>
</div>
"""
    
    def _format_overview_tab(self, report: RemediationReport) -> str:
        """Format overview tab with key metrics and charts."""
        stats = report.statistics
        findings = report.findings
        
        total_findings = len(findings)
        critical_count = stats.get('priority_distribution', {}).get('CRITICAL', 0)
        high_count = stats.get('priority_distribution', {}).get('HIGH', 0)
        medium_count = stats.get('priority_distribution', {}).get('MEDIUM', 0)
        low_count = stats.get('priority_distribution', {}).get('LOW', 0)
        
        avg_risk_score = stats.get('risk_score_stats', {}).get('mean', 0)
        max_risk_score = stats.get('risk_score_stats', {}).get('max', 0)
        
        # Calculate trends (mock data for demo)
        critical_trend = "+20%" if critical_count > 0 else "0%"
        high_trend = "-5%" if high_count > 0 else "0%"
        
        return f"""
<div id="overview-tab" class="tab-pane active">
    <h2 style="margin-bottom: var(--spacing-lg); color: var(--text-primary);">
        <i class="fas fa-tachometer-alt" style="color: var(--primary-color);"></i>
        Dashboard Overview
    </h2>
    
    <!-- Key Metrics -->
    <div class="grid grid-4" style="margin-bottom: var(--spacing-xl);">
        <div class="stat-card critical">
            <div class="stat-icon"><i class="fas fa-exclamation-circle"></i></div>
            <div class="stat-value">{critical_count}</div>
            <div class="stat-label">Critical</div>
            <div class="stat-trend up">
                <i class="fas fa-arrow-up"></i>
                <span>{critical_trend}</span>
            </div>
        </div>
        
        <div class="stat-card high">
            <div class="stat-icon"><i class="fas fa-exclamation-triangle"></i></div>
            <div class="stat-value">{high_count}</div>
            <div class="stat-label">High</div>
            <div class="stat-trend down">
                <i class="fas fa-arrow-down"></i>
                <span>{high_trend}</span>
            </div>
        </div>
        
        <div class="stat-card medium">
            <div class="stat-icon"><i class="fas fa-info-circle"></i></div>
            <div class="stat-value">{medium_count}</div>
            <div class="stat-label">Medium</div>
            <div class="stat-trend neutral">
                <i class="fas fa-minus"></i>
                <span>0%</span>
            </div>
        </div>
        
        <div class="stat-card low">
            <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
            <div class="stat-value">{low_count}</div>
            <div class="stat-label">Low</div>
            <div class="stat-trend down">
                <i class="fas fa-arrow-down"></i>
                <span>-10%</span>
            </div>
        </div>
    </div>
    
    <!-- Charts Row -->
    <div class="grid grid-2" style="margin-bottom: var(--spacing-xl);">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-chart-pie"></i>
                    Priority Distribution
                </h3>
            </div>
            <div class="chart-container">
                <canvas id="priorityChart"></canvas>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-chart-bar"></i>
                    Risk Score Distribution
                </h3>
            </div>
            <div class="chart-container">
                <canvas id="riskScoreChart"></canvas>
            </div>
        </div>
    </div>
    
    <!-- Summary Cards -->
    <div class="grid grid-3">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-calculator"></i>
                    Risk Metrics
                </h3>
            </div>
            <div style="display: flex; flex-direction: column; gap: var(--spacing-md);">
                <div style="display: flex; justify-content: space-between;">
                    <span style="color: var(--text-secondary);">Average Risk Score:</span>
                    <strong style="color: var(--text-primary);">{avg_risk_score:.2f}</strong>
                </div>
                <div style="display: flex; justify-content: space-between;">
                    <span style="color: var(--text-secondary);">Maximum Risk Score:</span>
                    <strong style="color: var(--danger-color);">{max_risk_score:.2f}</strong>
                </div>
                <div style="display: flex; justify-content: space-between;">
                    <span style="color: var(--text-secondary);">Total Findings:</span>
                    <strong style="color: var(--text-primary);">{total_findings}</strong>
                </div>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-clock"></i>
                    Recent Activity
                </h3>
            </div>
            <div style="display: flex; flex-direction: column; gap: var(--spacing-sm);">
                <div style="padding: var(--spacing-sm); background: var(--bg-tertiary); border-radius: var(--radius-sm);">
                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">2 hours ago</div>
                    <div style="font-size: 0.875rem; color: var(--text-primary);">New critical finding detected</div>
                </div>
                <div style="padding: var(--spacing-sm); background: var(--bg-tertiary); border-radius: var(--radius-sm);">
                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">5 hours ago</div>
                    <div style="font-size: 0.875rem; color: var(--text-primary);">3 findings remediated</div>
                </div>
                <div style="padding: var(--spacing-sm); background: var(--bg-tertiary); border-radius: var(--radius-sm);">
                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">1 day ago</div>
                    <div style="font-size: 0.875rem; color: var(--text-primary);">Scan completed</div>
                </div>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-bolt"></i>
                    Quick Actions
                </h3>
            </div>
            <div style="display: flex; flex-direction: column; gap: var(--spacing-sm);">
                <button class="btn btn-primary" onclick="switchTab('findings')">
                    <i class="fas fa-search"></i>
                    View All Findings
                </button>
                <button class="btn btn-secondary" onclick="exportReport('pdf')">
                    <i class="fas fa-file-pdf"></i>
                    Export PDF Report
                </button>
                <button class="btn btn-secondary" onclick="switchTab('workflow')">
                    <i class="fas fa-tasks"></i>
                    Manage Workflow
                </button>
            </div>
        </div>
    </div>
</div>
"""
    
    def _format_findings_tab(self, report: RemediationReport) -> str:
        """Format findings tab with searchable table."""
        findings = report.findings
        
        # Build table rows
        rows = []
        for finding in findings[:50]:  # Limit to 50 for performance
            priority_class = finding.priority.lower()
            
            rows.append(f"""
            <tr>
                <td><span class="badge badge-{priority_class}">{finding.priority}</span></td>
                <td><strong>{finding.finding.title}</strong></td>
                <td>{finding.finding.algorithm or 'N/A'}</td>
                <td>{finding.finding.file}:{finding.finding.line}</td>
                <td><strong>{finding.risk_score.final_score:.2f}</strong></td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="viewFindingDetails('{finding.finding.id}')">
                        <i class="fas fa-eye"></i> View
                    </button>
                    <button class="btn btn-sm btn-success" onclick="remediateFinding('{finding.finding.id}')">
                        <i class="fas fa-check"></i> Fix
                    </button>
                </td>
            </tr>
            """)
        
        table_rows = '\n'.join(rows)
        
        return f"""
<div id="findings-tab" class="tab-pane">
    <h2 style="margin-bottom: var(--spacing-lg); color: var(--text-primary);">
        <i class="fas fa-list"></i>
        Vulnerability Findings
    </h2>
    
    <!-- Search and Filters -->
    <div class="search-bar">
        <input type="text" class="search-input" id="findingsSearch" 
               placeholder="Search findings by title, algorithm, or file..." 
               onkeyup="filterFindings()">
        <button class="btn btn-primary" onclick="filterFindings()">
            <i class="fas fa-search"></i> Search
        </button>
    </div>
    
    <div class="filter-group" style="margin-bottom: var(--spacing-lg);">
        <span style="color: var(--text-secondary); margin-right: var(--spacing-sm);">Filter by priority:</span>
        <button class="filter-chip active" data-filter="all" onclick="filterByPriority('all')">All</button>
        <button class="filter-chip" data-filter="critical" onclick="filterByPriority('critical')">Critical</button>
        <button class="filter-chip" data-filter="high" onclick="filterByPriority('high')">High</button>
        <button class="filter-chip" data-filter="medium" onclick="filterByPriority('medium')">Medium</button>
        <button class="filter-chip" data-filter="low" onclick="filterByPriority('low')">Low</button>
    </div>
    
    <!-- Findings Table -->
    <div class="card">
        <div class="table-container">
            <table id="findingsTable">
                <thead>
                    <tr>
                        <th>Priority</th>
                        <th>Title</th>
                        <th>Algorithm</th>
                        <th>Location</th>
                        <th>Risk Score</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {table_rows}
                </tbody>
            </table>
        </div>
    </div>
    
    <div style="margin-top: var(--spacing-lg); text-align: center; color: var(--text-muted);">
        Showing {min(50, len(findings))} of {len(findings)} findings
    </div>
</div>
"""
    
    def _format_analytics_tab(self, report: RemediationReport) -> str:
        """Format analytics tab with advanced charts."""
        return """
<div id="analytics-tab" class="tab-pane">
    <h2 style="margin-bottom: var(--spacing-lg); color: var(--text-primary);">
        <i class="fas fa-chart-line"></i>
        Analytics & Insights
    </h2>
    
    <div class="grid grid-2" style="margin-bottom: var(--spacing-xl);">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-chart-area"></i>
                    Trend Analysis
                </h3>
            </div>
            <div class="chart-container large">
                <canvas id="trendChart"></canvas>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-chart-bar"></i>
                    Algorithm Distribution
                </h3>
            </div>
            <div class="chart-container large">
                <canvas id="algorithmChart"></canvas>
            </div>
        </div>
    </div>
    
    <div class="grid grid-3">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-brain"></i>
                    AI Insights
                </h3>
            </div>
            <div style="padding: var(--spacing-md); background: var(--bg-tertiary); border-radius: var(--radius-md); border-left: 4px solid var(--primary-color);">
                <p style="color: var(--text-secondary); margin-bottom: var(--spacing-sm);">
                    <strong style="color: var(--text-primary);">Prediction:</strong> 
                    Based on current trends, expect 3-5 new critical findings in the next 7 days.
                </p>
                <p style="color: var(--text-secondary); font-size: 0.8125rem;">
                    Confidence: 85%
                </p>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-fire"></i>
                    Hotspots
                </h3>
            </div>
            <div style="display: flex; flex-direction: column; gap: var(--spacing-sm);">
                <div style="padding: var(--spacing-sm); background: var(--bg-tertiary); border-radius: var(--radius-sm);">
                    <div style="font-weight: 600; color: var(--text-primary);">auth/password.py</div>
                    <div style="font-size: 0.8125rem; color: var(--danger-color);">5 critical issues</div>
                </div>
                <div style="padding: var(--spacing-sm); background: var(--bg-tertiary); border-radius: var(--radius-sm);">
                    <div style="font-weight: 600; color: var(--text-primary);">crypto/encryption.py</div>
                    <div style="font-size: 0.8125rem; color: var(--high-color);">3 high issues</div>
                </div>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">
                    <i class="fas fa-trophy"></i>
                    Benchmarks
                </h3>
            </div>
            <div style="display: flex; flex-direction: column; gap: var(--spacing-md);">
                <div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: var(--spacing-xs);">
                        <span style="color: var(--text-secondary); font-size: 0.875rem;">vs Industry Average</span>
                        <span style="color: var(--success-color); font-weight: 600;">+15%</span>
                    </div>
                    <div style="height: 8px; background: var(--bg-tertiary); border-radius: 4px; overflow: hidden;">
                        <div style="width: 65%; height: 100%; background: var(--success-color);"></div>
                    </div>
                </div>
                <div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: var(--spacing-xs);">
                        <span style="color: var(--text-secondary); font-size: 0.875rem;">Remediation Speed</span>
                        <span style="color: var(--primary-color); font-weight: 600;">Fast</span>
                    </div>
                    <div style="height: 8px; background: var(--bg-tertiary); border-radius: 4px; overflow: hidden;">
                        <div style="width: 80%; height: 100%; background: var(--primary-color);"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
"""
    
    def _format_workflow_tab(self, report: RemediationReport) -> str:
        """Format workflow tab with kanban board."""
        findings = report.findings
        
        # Categorize findings by status (mock data)
        todo_findings = [f for f in findings if f.priority in ['CRITICAL', 'HIGH']][:3]
        in_progress_findings = [f for f in findings if f.priority == 'MEDIUM'][:2]
        done_findings = [f for f in findings if f.priority == 'LOW'][:2]
        
        def format_kanban_card(finding):
            priority_class = finding.priority.lower()
            return f"""
            <div class="card" style="margin-bottom: var(--spacing-md); cursor: move;" draggable="true">
                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: var(--spacing-sm);">
                    <span class="badge badge-{priority_class}">{finding.priority}</span>
                    <span style="font-size: 0.75rem; color: var(--text-muted);">#{finding.finding.id[:8]}</span>
                </div>
                <h4 style="font-size: 0.875rem; color: var(--text-primary); margin-bottom: var(--spacing-sm);">
                    {finding.finding.title[:50]}...
                </h4>
                <div style="font-size: 0.8125rem; color: var(--text-secondary); margin-bottom: var(--spacing-sm);">
                    <i class="fas fa-file-code"></i> {finding.finding.file}
                </div>
                <div style="display: flex; gap: var(--spacing-xs);">
                    <button class="btn btn-sm btn-primary" onclick="viewFindingDetails('{finding.finding.id}')">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-success" onclick="remediateFinding('{finding.finding.id}')">
                        <i class="fas fa-check"></i>
                    </button>
                </div>
            </div>
            """
        
        todo_cards = '\n'.join([format_kanban_card(f) for f in todo_findings])
        in_progress_cards = '\n'.join([format_kanban_card(f) for f in in_progress_findings])
        done_cards = '\n'.join([format_kanban_card(f) for f in done_findings])
        
        return f"""
<div id="workflow-tab" class="tab-pane">
                        <i class="fas fa-calendar-alt"></i>
                        Monthly Report
                    </button>
                    <button class="btn btn-primary" onclick="configureSchedule()">
                        <i class="fas fa-cog"></i>
                        Configure Schedule
                    </button>
                </div>
            </div>
        </div>
    </div>
    
    <div class="card" style="margin-top: var(--spacing-lg);">
        <div class="card-header">
            <h2 class="card-title">
                <i class="fas fa-history"></i>
                Report History
            </h2>
        </div>
        <div class="card-body">
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Type</th>
                            <th>Format</th>
                            <th>Size</th>
                            <th>Actions</th>
                        </tr>
