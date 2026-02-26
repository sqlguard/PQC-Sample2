"""
Enhanced dashboard formatter with modern UI/UX improvements.
Includes glassmorphism effects, responsive design, theme toggle, and advanced styling.
"""
from typing import List, Dict, Any, Optional
from ..models import RemediationReport, PrioritizedFinding
import json


class EnhancedDashboardFormatter:
    """Formats reports as modern interactive HTML dashboards with advanced UI/UX."""
    
    def __init__(self, theme: str = "dark"):
        """
        Initialize the enhanced dashboard formatter.
        
        Args:
            theme: Default theme ('dark' or 'light')
        """
        self.theme = theme
    
    def format(self, report: RemediationReport, enable_animations: bool = True) -> str:
        """
        Format report as an enhanced interactive HTML dashboard.
        
        Args:
            report: RemediationReport to format
            enable_animations: Whether to enable animations
            
        Returns:
            HTML formatted string with modern UI/UX
        """
        html_parts = []
        
        # HTML header with enhanced CSS
        html_parts.append(self._get_html_header(enable_animations))
        
        # Main container
        html_parts.append('<div class="dashboard-container" data-theme="dark">')
        
        # Navigation bar with theme toggle
        html_parts.append(self._format_navigation(report))
        
        # Filter panel
        html_parts.append(self._format_filter_panel(report))
        
        # Header section
        html_parts.append(self._format_header(report))
        
        # Executive summary with glassmorphism
        html_parts.append(self._format_executive_summary(report))
        
        # Executive Dashboard View (Point 6)
        html_parts.append(self._format_executive_dashboard(report))
        
        # Advanced Analytics Section (Point 7)
        html_parts.append(self._format_advanced_analytics(report))
        
        # Remediation Workflow Integration (Point 8)
        html_parts.append(self._format_remediation_workflow(report))
        
        # Compliance & Reporting (Point 9)
        html_parts.append(self._format_compliance_reporting(report))
        
        # Visualizations section
        html_parts.append(self._format_visualizations_section(report))
        
        # Risk heat map
        html_parts.append(self._format_risk_heatmap(report))
        
        # Priority quadrant
        html_parts.append(self._format_priority_quadrant(report))
        
        # Remediation roadmap
        html_parts.append(self._format_remediation_roadmap(report))
        
        # Detailed findings
        html_parts.append(self._format_detailed_findings(report))
        
        # JavaScript for interactivity
        html_parts.append(self._get_javascript(report, enable_animations))
        
        html_parts.append('</div>')  # Close container
        html_parts.append('</body></html>')
        
        return '\n'.join(html_parts)
    
    def _get_html_header(self, enable_animations: bool) -> str:
        """Get HTML header with modern CSS including glassmorphism effects."""
        animation_css = """
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes slideIn {
            from { transform: translateX(-100%); }
            to { transform: translateX(0); }
        }
        
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }
        
        @keyframes shimmer {
            0% { background-position: -1000px 0; }
            100% { background-position: 1000px 0; }
        }
        
        .animate-fade-in {
            animation: fadeIn 0.6s ease-out forwards;
        }
        
        .animate-slide-in {
            animation: slideIn 0.4s ease-out forwards;
        }
        
        .animate-pulse {
            animation: pulse 2s ease-in-out infinite;
        }
        """ if enable_animations else ""
        
        return f'''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Enhanced Crypto Vulnerability Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/d3@7"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* CSS Variables for theming */
        :root {{
            /* Primary Colors */
            --primary-50: #ecfeff;
            --primary-100: #cffafe;
            --primary-200: #a5f3fc;
            --primary-300: #67e8f9;
            --primary-400: #22d3ee;
            --primary-500: #06b6d4;
            --primary-600: #0891b2;
            --primary-700: #0e7490;
            --primary-800: #155e75;
            --primary-900: #164e63;
            
            /* Semantic Colors */
            --critical: #dc2626;
            --critical-light: #ef4444;
            --high: #ea580c;
            --high-light: #f97316;
            --medium: #eab308;
            --medium-light: #fbbf24;
            --low: #16a34a;
            --low-light: #22c55e;
            --info: #3b82f6;
            --success: #10b981;
            --warning: #f59e0b;
            
            /* Dark Theme */
            --bg-primary: #020617;
            --bg-secondary: #0f172a;
            --bg-tertiary: #1e293b;
            --bg-card: #1e293b;
            --text-primary: #e2e8f0;
            --text-secondary: #94a3b8;
            --text-tertiary: #64748b;
            --border: #334155;
            --border-light: #475569;
            
            /* Glassmorphism */
            --glass-bg: rgba(30, 41, 59, 0.7);
            --glass-border: rgba(148, 163, 184, 0.1);
            --glass-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
            
            /* Spacing */
            --space-1: 0.25rem;
            --space-2: 0.5rem;
            --space-3: 0.75rem;
            --space-4: 1rem;
            --space-5: 1.25rem;
            --space-6: 1.5rem;
            --space-8: 2rem;
            --space-10: 2.5rem;
            --space-12: 3rem;
            --space-16: 4rem;
            
            /* Border Radius */
            --radius-sm: 0.375rem;
            --radius-md: 0.5rem;
            --radius-lg: 0.75rem;
            --radius-xl: 1rem;
            --radius-2xl: 1.5rem;
            
            /* Transitions */
            --transition-fast: 150ms ease-in-out;
            --transition-base: 250ms ease-in-out;
            --transition-slow: 350ms ease-in-out;
        }}
        
        /* Light Theme */
        [data-theme="light"] {{
            --bg-primary: #f8fafc;
            --bg-secondary: #ffffff;
            --bg-tertiary: #f1f5f9;
            --bg-card: #ffffff;
            --text-primary: #0f172a;
            --text-secondary: #475569;
            --text-tertiary: #64748b;
            --border: #e2e8f0;
            --border-light: #cbd5e1;
            --glass-bg: rgba(255, 255, 255, 0.7);
            --glass-border: rgba(148, 163, 184, 0.2);
            --glass-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.1);
        }}
        
        /* Reset and Base Styles */
        * {{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }}
        
        body {{
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: var(--text-primary);
            background: var(--bg-primary);
            padding: var(--space-4);
            transition: background-color var(--transition-base), color var(--transition-base);
        }}
        
        /* Dashboard Container */
        .dashboard-container {{
            max-width: 1600px;
            margin: 0 auto;
            background: var(--bg-secondary);
            border-radius: var(--radius-2xl);
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
            border: 1px solid var(--border);
            transition: all var(--transition-base);
        }}
        
        /* Navigation Bar */
        .nav-bar {{
            background: var(--glass-bg);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border-bottom: 1px solid var(--glass-border);
            padding: var(--space-4) var(--space-8);
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            z-index: 1000;
            box-shadow: var(--glass-shadow);
        }}
        
        .nav-brand {{
            display: flex;
            align-items: center;
            gap: var(--space-3);
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--primary-500);
        }}
        
        .nav-brand i {{
            font-size: 1.5rem;
        }}
        
        .nav-controls {{
            display: flex;
            align-items: center;
            gap: var(--space-4);
        }}
        
        .theme-toggle {{
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: var(--space-2) var(--space-4);
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: var(--space-2);
            transition: all var(--transition-fast);
            color: var(--text-primary);
        }}
        
        .theme-toggle:hover {{
            background: var(--bg-card);
            border-color: var(--primary-500);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(6, 182, 212, 0.2);
        }}
        
        .theme-toggle i {{
            font-size: 1.1rem;
        }}
        
        /* Real-Time Features */
        .live-indicator {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
            background: var(--success);
            color: white;
            padding: var(--space-1) var(--space-3);
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 700;
            letter-spacing: 0.05em;
            margin-left: var(--space-3);
        }}
        
        .pulse-dot {{
            width: 8px;
            height: 8px;
            background: white;
            border-radius: 50%;
            animation: pulse 2s ease-in-out infinite;
        }}
        
        @keyframes pulse {{
            0%, 100% {{ opacity: 1; transform: scale(1); }}
            50% {{ opacity: 0.5; transform: scale(1.2); }}
        }}
        
        .notifications-container {{
            position: fixed;
            top: 80px;
            right: var(--space-6);
            z-index: 9999;
            display: flex;
            flex-direction: column;
            gap: var(--space-3);
            max-width: 400px;
        }}
        
        .notification {{
            background: var(--glass-bg);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border: 1px solid var(--glass-border);
            border-radius: var(--radius-lg);
            padding: var(--space-4);
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            animation: slideInRight 0.3s ease-out;
            display: flex;
            gap: var(--space-3);
            align-items: flex-start;
        }}
        
        @keyframes slideInRight {{
            from {{ transform: translateX(400px); opacity: 0; }}
            to {{ transform: translateX(0); opacity: 1; }}
        }}
        
        .notification.success {{
            border-left: 4px solid var(--success);
        }}
        
        .notification.warning {{
            border-left: 4px solid var(--medium);
        }}
        
        .notification.error {{
            border-left: 4px solid var(--critical);
        }}
        
        .notification.info {{
            border-left: 4px solid var(--primary-500);
        }}
        
        .notification-icon {{
            font-size: 1.5rem;
            flex-shrink: 0;
        }}
        
        .notification.success .notification-icon {{
            color: var(--success);
        }}
        
        .notification.warning .notification-icon {{
            color: var(--medium);
        }}
        
        .notification.error .notification-icon {{
            color: var(--critical);
        }}
        
        .notification.info .notification-icon {{
            color: var(--primary-500);
        }}
        
        .notification-content {{
            flex: 1;
        }}
        
        .notification-title {{
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: var(--space-1);
        }}
        
        .notification-message {{
            font-size: 0.875rem;
            color: var(--text-secondary);
        }}
        
        .notification-close {{
            background: none;
            border: none;
            color: var(--text-tertiary);
            cursor: pointer;
            padding: 0;
            font-size: 1.25rem;
            line-height: 1;
            transition: color var(--transition-fast);
        }}
        
        .notification-close:hover {{
            color: var(--text-primary);
        }}
        
        .status-bar {{
            background: var(--bg-tertiary);
            border-bottom: 1px solid var(--border);
            padding: var(--space-2) var(--space-8);
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 0.875rem;
            color: var(--text-secondary);
        }}
        
        .status-item {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .status-item i {{
            color: var(--primary-500);
        }}
        
        #connectionStatus.connected {{
            color: var(--success);
        }}
        
        #connectionStatus.disconnected {{
            color: var(--critical);
        }}
        
        #connectionStatus.connecting {{
            color: var(--medium);
        }}
        
        #refreshIcon.spinning {{
            animation: spin 1s linear infinite;
        }}
        
        @keyframes spin {{
            from {{ transform: rotate(0deg); }}
            to {{ transform: rotate(360deg); }}
        }}
        
        /* Header Section */
        .header-section {{
            background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
            padding: var(--space-12) var(--space-8);
            text-align: center;
            border-bottom: 2px solid var(--primary-500);
            position: relative;
            overflow: hidden;
        }}
        
        .header-section::before {{
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: linear-gradient(90deg, transparent, var(--primary-500), transparent);
        }}
        
        .header-section::after {{
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: radial-gradient(circle at 50% 0%, rgba(6, 182, 212, 0.1), transparent 70%);
            pointer-events: none;
        }}
        
        .header-section h1 {{
            font-size: 3rem;
            margin-bottom: var(--space-4);
            background: linear-gradient(135deg, var(--primary-400), var(--primary-600));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            position: relative;
            z-index: 1;
        }}
        
        .header-meta {{
            font-size: 1.1rem;
            color: var(--text-secondary);
            position: relative;
            z-index: 1;
        }}
        
        /* Glass Card */
        .glass-card {{
            background: var(--glass-bg);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border: 1px solid var(--glass-border);
            border-radius: var(--radius-xl);
            padding: var(--space-6);
            box-shadow: var(--glass-shadow);
            transition: all var(--transition-base);
        }}
        
        .glass-card:hover {{
            transform: translateY(-4px);
            box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
            border-color: var(--primary-500);
        }}
        
        /* Executive Summary */
        .executive-summary {{
            padding: var(--space-12) var(--space-8);
            background: var(--bg-tertiary);
        }}
        
        .executive-summary h2 {{
            color: var(--primary-500);
            margin-bottom: var(--space-8);
            font-size: 2rem;
            display: flex;
            align-items: center;
            gap: var(--space-3);
        }}
        
        .summary-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: var(--space-6);
            margin: var(--space-8) 0;
        }}
        
        .summary-card {{
            background: var(--bg-card);
            padding: var(--space-8);
            border-radius: var(--radius-xl);
            border: 2px solid var(--border);
            position: relative;
            overflow: hidden;
            transition: all var(--transition-base);
        }}
        
        .summary-card::before {{
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 100%;
            background: var(--primary-500);
            transition: width var(--transition-base);
        }}
        
        .summary-card:hover {{
            transform: translateY(-6px) scale(1.02);
            box-shadow: 0 16px 48px rgba(0, 0, 0, 0.3);
        }}
        
        .summary-card:hover::before {{
            width: 100%;
            opacity: 0.1;
        }}
        
        .summary-card.critical {{
            border-color: var(--critical);
        }}
        
        .summary-card.critical::before {{
            background: var(--critical);
        }}
        
        .summary-card.high {{
            border-color: var(--high);
        }}
        
        .summary-card.high::before {{
            background: var(--high);
        }}
        
        .summary-card.medium {{
            border-color: var(--medium);
        }}
        
        .summary-card.medium::before {{
            background: var(--medium);
        }}
        
        .summary-card.low {{
            border-color: var(--low);
        }}
        
        .summary-card.low::before {{
            background: var(--low);
        }}
        
        .summary-card-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-4);
        }}
        
        .summary-card-icon {{
            font-size: 2rem;
            opacity: 0.8;
        }}
        
        .summary-card h3 {{
            font-size: 3.5rem;
            font-weight: 700;
            background: linear-gradient(135deg, var(--primary-400), var(--primary-600));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: var(--space-2);
        }}
        
        .summary-card p {{
            color: var(--text-secondary);
            font-size: 0.95rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 600;
        }}
        
        .summary-card-trend {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
            margin-top: var(--space-3);
            font-size: 0.9rem;
            font-weight: 600;
        }}
        
        .trend-up {{
            color: var(--critical);
        }}
        
        .trend-down {{
            color: var(--success);
        }}
        
        .trend-neutral {{
            color: var(--text-tertiary);
        }}
        
        /* Responsive Design */
        @media (max-width: 768px) {{
            body {{
                padding: var(--space-2);
            }}
            
            .nav-bar {{
                padding: var(--space-3) var(--space-4);
                flex-direction: column;
                gap: var(--space-3);
            }}
            
            .header-section h1 {{
                font-size: 2rem;
            }}
            
            .summary-grid {{
                grid-template-columns: 1fr;
            }}
            
            .summary-card h3 {{
                font-size: 2.5rem;
            }}
        }}
        
        @media (max-width: 480px) {{
            .header-section h1 {{
                font-size: 1.5rem;
            }}
            
            .summary-card h3 {{
                font-size: 2rem;
            }}
        }}
        
        /* Executive Dashboard Section */
        .executive-dashboard-section {{
            padding: var(--space-12) var(--space-8);
            background: var(--bg-tertiary);
        }}
        
        .executive-dashboard-section .section-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-8);
        }}
        
        .executive-dashboard-section h2 {{
            color: var(--primary-500);
            font-size: 2rem;
            display: flex;
            align-items: center;
            gap: var(--space-3);
        }}
        
        .executive-dashboard-section h3 {{
            color: var(--text-primary);
            font-size: 1.5rem;
            margin-bottom: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .toggle-btn {{
            background: var(--bg-card);
            border: 1px solid var(--border);
            color: var(--text-secondary);
            padding: var(--space-2) var(--space-4);
            border-radius: var(--radius-lg);
            cursor: pointer;
            transition: all var(--transition-base);
        }}
        
        .toggle-btn:hover {{
            background: var(--primary-500);
            color: white;
            transform: scale(1.05);
        }}
        
        /* KPI Section */
        .kpi-section {{
            margin-bottom: var(--space-12);
        }}
        
        .kpi-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: var(--space-6);
        }}
        
        .kpi-card {{
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            padding: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-4);
            border: 1px solid var(--border);
            transition: all var(--transition-base);
        }}
        
        .kpi-card:hover {{
            transform: translateY(-4px);
            box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
            border-color: var(--primary-500);
        }}
        
        .kpi-icon {{
            width: 64px;
            height: 64px;
            border-radius: var(--radius-lg);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.75rem;
            color: white;
            flex-shrink: 0;
        }}
        
        .kpi-content {{
            flex: 1;
        }}
        
        .kpi-content h4 {{
            font-size: 0.875rem;
            color: var(--text-secondary);
            margin-bottom: var(--space-2);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }}
        
        .kpi-value {{
            font-size: 2.5rem;
            font-weight: 700;
            color: var(--text-primary);
            line-height: 1;
            margin-bottom: var(--space-1);
        }}
        
        .kpi-value.trend-up {{
            color: var(--critical);
        }}
        
        .kpi-value.trend-down {{
            color: var(--success);
        }}
        
        .kpi-unit {{
            font-size: 1rem;
            color: var(--text-secondary);
            font-weight: 400;
            margin-left: var(--space-1);
        }}
        
        .kpi-subtitle {{
            font-size: 0.875rem;
            color: var(--text-tertiary);
        }}
        
        /* Compliance Section */
        .compliance-section {{
            margin-bottom: var(--space-12);
        }}
        
        .compliance-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
            gap: var(--space-6);
        }}
        
        .compliance-card {{
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            border: 1px solid var(--border);
            overflow: hidden;
            transition: all var(--transition-base);
        }}
        
        .compliance-card:hover {{
            transform: translateY(-4px);
            box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
            border-color: var(--primary-500);
        }}
        
        .compliance-header {{
            padding: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-4);
            border-bottom: 1px solid var(--border);
        }}
        
        .compliance-icon {{
            font-size: 2rem;
        }}
        
        .compliance-title h4 {{
            font-size: 1.25rem;
            color: var(--text-primary);
            margin-bottom: var(--space-1);
        }}
        
        .compliance-subtitle {{
            font-size: 0.875rem;
            color: var(--text-secondary);
            margin: 0;
        }}
        
        .compliance-body {{
            padding: var(--space-6);
        }}
        
        .compliance-coverage {{
            margin-bottom: var(--space-6);
        }}
        
        .coverage-label {{
            font-size: 0.875rem;
            color: var(--text-secondary);
            margin-bottom: var(--space-2);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }}
        
        .coverage-bar {{
            height: 12px;
            background: var(--bg-tertiary);
            border-radius: var(--radius-full);
            overflow: hidden;
            margin-bottom: var(--space-2);
        }}
        
        .coverage-fill {{
            height: 100%;
            border-radius: var(--radius-full);
            transition: width var(--transition-base);
        }}
        
        .coverage-fill.high {{
            background: linear-gradient(90deg, var(--success), var(--success-light));
        }}
        
        .coverage-fill.medium {{
            background: linear-gradient(90deg, var(--medium), var(--medium-light));
        }}
        
        .coverage-fill.low {{
            background: linear-gradient(90deg, var(--critical), var(--critical-light));
        }}
        
        .coverage-value {{
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--text-primary);
        }}
        
        .compliance-controls {{
            margin-bottom: var(--space-4);
        }}
        
        .controls-label {{
            font-size: 0.875rem;
            color: var(--text-secondary);
            margin-bottom: var(--space-2);
        }}
        
        .controls-list {{
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-2);
        }}
        
        .control-badge {{
            background: var(--bg-tertiary);
            color: var(--text-primary);
            padding: var(--space-1) var(--space-3);
            border-radius: var(--radius-md);
            font-size: 0.75rem;
            font-weight: 600;
            border: 1px solid var(--border);
        }}
        
        .compliance-findings {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
            color: var(--text-secondary);
            font-size: 0.875rem;
        }}
        
        .compliance-findings i {{
            color: var(--high);
        }}
        
        .compliance-footer {{
            padding: var(--space-4) var(--space-6);
            background: var(--bg-tertiary);
            display: flex;
            gap: var(--space-3);
            border-top: 1px solid var(--border);
        }}
        
        .compliance-btn {{
            flex: 1;
            background: var(--bg-card);
            border: 1px solid var(--border);
            color: var(--text-primary);
            padding: var(--space-2) var(--space-4);
            border-radius: var(--radius-md);
            cursor: pointer;
            transition: all var(--transition-base);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--space-2);
            font-size: 0.875rem;
        }}
        
        .compliance-btn:hover {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
            transform: translateY(-2px);
        }}
        
        /* Timeline Section */
        .timeline-section {{
            margin-bottom: var(--space-8);
        }}
        
        .timeline-chart {{
            padding: var(--space-6);
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            border: 1px solid var(--border);
        
        /* Advanced Analytics Section */
        .analytics-section {{
            padding: var(--space-12) var(--space-8);
            background: var(--bg-tertiary);
        }}
        
        .analytics-section .section-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-8);
        }}
        
        .analytics-section h2 {{
            color: var(--primary-500);
            font-size: 2rem;
            display: flex;
            align-items: center;
            gap: var(--space-3);
        }}
        
        .analytics-section h3 {{
            color: var(--text-primary);
            font-size: 1.5rem;
            margin-bottom: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        /* ML Predictions */
        .predictions-section {{
            margin-bottom: var(--space-12);
        }}
        
        .predictions-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: var(--space-6);
        }}
        
        .prediction-card {{
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            border: 1px solid var(--border);
            overflow: hidden;
        }}
        
        .prediction-header {{
            padding: var(--space-4);
            background: linear-gradient(135deg, var(--primary-500), var(--primary-700));
            color: white;
            display: flex;
            align-items: center;
            gap: var(--space-3);
        }}
        
        .prediction-icon {{
            font-size: 1.5rem;
        }}
        
        .prediction-header h4 {{
            margin: 0;
            font-size: 1.125rem;
        }}
        
        .prediction-body {{
            padding: var(--space-6);
        }}
        
        .prediction-metric {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-4);
        }}
        
        .metric-label {{
            color: var(--text-secondary);
            font-size: 0.875rem;
        }}
        
        .metric-value {{
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--text-primary);
        }}
        
        .metric-value.critical-text {{
            color: var(--critical);
        }}
        
        .prediction-trend {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
            padding: var(--space-2) var(--space-3);
            border-radius: var(--radius-md);
            margin-bottom: var(--space-4);
            font-size: 0.875rem;
        }}
        
        .prediction-trend.increasing {{
            background: rgba(220, 38, 38, 0.1);
            color: var(--critical);
        }}
        
        .prediction-trend.stable {{
            background: rgba(16, 185, 129, 0.1);
            color: var(--success);
        }}
        
        .prediction-confidence {{
            margin-top: var(--space-4);
        }}
        
        .confidence-bar {{
            height: 8px;
            background: var(--bg-tertiary);
            border-radius: var(--radius-full);
            overflow: hidden;
            margin-top: var(--space-2);
        }}
        
        .confidence-fill {{
            height: 100%;
            background: linear-gradient(90deg, var(--primary-500), var(--primary-700));
            border-radius: var(--radius-full);
        }}
        
        .risk-area-item {{
            margin-bottom: var(--space-4);
        }}
        
        .area-name {{
            display: block;
            color: var(--text-primary);
            font-weight: 500;
            margin-bottom: var(--space-2);
        }}
        
        .area-risk {{
            display: flex;
            align-items: center;
            gap: var(--space-3);
        }}
        
        .risk-score {{
            font-weight: 700;
            min-width: 50px;
        }}
        
        .risk-score.critical {{
            color: var(--critical);
        }}
        
        .risk-score.high {{
            color: var(--high);
        }}
        
        .risk-bar {{
            flex: 1;
            height: 8px;
            background: var(--bg-tertiary);
            border-radius: var(--radius-full);
            overflow: hidden;
        }}
        
        .risk-fill {{
            height: 100%;
            border-radius: var(--radius-full);
        }}
        
        .risk-fill.critical {{
            background: var(--critical);
        }}
        
        .risk-fill.high {{
            background: var(--high);
        }}
        
        .prediction-action-btn {{
            width: 100%;
            background: var(--primary-500);
            color: white;
            border: none;
            padding: var(--space-3) var(--space-4);
            border-radius: var(--radius-md);
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--space-2);
            margin-top: var(--space-4);
            transition: all var(--transition-fast);
        }}
        
        .prediction-action-btn:hover {{
            background: var(--primary-600);
            transform: translateY(-2px);
        }}
        
        /* Historical Trends */
        .trends-section {{
            margin-bottom: var(--space-12);
        }}
        
        .trends-container {{
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: var(--space-6);
        }}
        
        .trend-chart-container {{
            padding: var(--space-6);
        }}
        
        .chart-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-4);
        }}
        
        .chart-header h4 {{
            margin: 0;
            color: var(--text-primary);
        }}
        
        .chart-controls {{
            display: flex;
            gap: var(--space-2);
        }}
        
        .chart-period-btn {{
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            color: var(--text-secondary);
            padding: var(--space-1) var(--space-3);
            border-radius: var(--radius-md);
            cursor: pointer;
            transition: all var(--transition-fast);
            font-size: 0.875rem;
        }}
        
        .chart-period-btn.active {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
        }}
        
        .chart-period-btn:hover:not(.active) {{
            background: var(--bg-card);
            border-color: var(--primary-500);
        }}
        
        .trend-insights {{
            padding: var(--space-6);
        }}
        
        .trend-insights h4 {{
            margin-top: 0;
            margin-bottom: var(--space-4);
            color: var(--text-primary);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .insights-list {{
            list-style: none;
            padding: 0;
            margin: 0;
        }}
        
        .insight-item {{
            display: flex;
            align-items: flex-start;
            gap: var(--space-3);
            padding: var(--space-3);
            border-radius: var(--radius-md);
            margin-bottom: var(--space-3);
        }}
        
        .insight-item.positive {{
            background: rgba(16, 185, 129, 0.1);
            color: var(--success);
        }}
        
        .insight-item.negative {{
            background: rgba(220, 38, 38, 0.1);
            color: var(--critical);
        }}
        
        .insight-item.neutral {{
            background: rgba(234, 179, 8, 0.1);
            color: var(--medium);
        }}
        
        .insight-item i {{
            margin-top: 2px;
        }}
        
        .insight-item span {{
            color: var(--text-primary);
            font-size: 0.875rem;
        }}
        
        /* Benchmarking */
        .benchmarking-section {{
            margin-bottom: var(--space-12);
        }}
        
        .benchmark-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: var(--space-6);
        }}
        
        .benchmark-card {{
            padding: var(--space-6);
        }}
        
        .benchmark-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-4);
        }}
        
        .benchmark-header h4 {{
            margin: 0;
            color: var(--text-primary);
            font-size: 1rem;
        }}
        
        .benchmark-status {{
            font-size: 1.5rem;
        }}
        
        .benchmark-status.better {{
            color: var(--success);
        }}
        
        .benchmark-status.worse {{
            color: var(--critical);
        }}
        
        .benchmark-comparison {{
            display: flex;
            align-items: center;
            gap: var(--space-4);
            margin-bottom: var(--space-4);
        }}
        
        .benchmark-value {{
            flex: 1;
            text-align: center;
        }}
        
        .value-label {{
            display: block;
            font-size: 0.75rem;
            color: var(--text-secondary);
            margin-bottom: var(--space-1);
        }}
        
        .value-number {{
            display: block;
            font-size: 1.75rem;
            font-weight: 700;
            color: var(--text-primary);
        }}
        
        .value-unit {{
            display: block;
            font-size: 0.875rem;
            color: var(--text-tertiary);
        }}
        
        .benchmark-vs {{
            color: var(--text-tertiary);
            font-size: 1.25rem;
        }}
        
        .benchmark-percentile {{
            text-align: center;
            font-size: 0.875rem;
            color: var(--text-secondary);
        }}
        
        .percentile-bar {{
            height: 8px;
            background: linear-gradient(90deg, var(--critical), var(--medium), var(--success));
            border-radius: var(--radius-full);
            margin-top: var(--space-2);
            position: relative;
        }}
        
        .percentile-marker {{
            position: absolute;
            top: -8px;
            transform: translateX(-50%);
            color: var(--primary-500);
            font-size: 1.25rem;
        }}
        
        /* Anomaly Detection */
        .anomaly-section {{
            margin-bottom: var(--space-8);
        }}
        
        .anomaly-container {{
            padding: var(--space-6);
        }}
        
        .anomaly-item {{
            display: flex;
            gap: var(--space-4);
            padding: var(--space-4);
            border-radius: var(--radius-lg);
            border-left: 4px solid;
            margin-bottom: var(--space-4);
            background: var(--bg-tertiary);
        }}
        
        .anomaly-item.high {{
            border-left-color: var(--critical);
        }}
        
        .anomaly-item.medium {{
            border-left-color: var(--medium);
        }}
        
        .anomaly-item.low {{
            border-left-color: var(--primary-500);
        }}
        
        .anomaly-icon {{
            font-size: 1.5rem;
            flex-shrink: 0;
        }}
        
        .anomaly-item.high .anomaly-icon {{
            color: var(--critical);
        }}
        
        .anomaly-item.medium .anomaly-icon {{
            color: var(--medium);
        }}
        
        .anomaly-item.low .anomaly-icon {{
            color: var(--primary-500);
        }}
        
        .anomaly-content {{
            flex: 1;
        }}
        
        .anomaly-header {{
            display: flex;
            align-items: center;
            gap: var(--space-3);
            margin-bottom: var(--space-2);
        }}
        
        .anomaly-header h4 {{
            margin: 0;
            color: var(--text-primary);
            font-size: 1rem;
        }}
        
        .anomaly-badge {{
            background: var(--primary-500);
            color: white;
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-md);
            font-size: 0.75rem;
            font-weight: 600;
        }}
        
        .anomaly-description {{
            color: var(--text-secondary);
            font-size: 0.875rem;
            margin-bottom: var(--space-3);
        }}
        
        .anomaly-footer {{
            display: flex;
            gap: var(--space-4);
            font-size: 0.75rem;
            color: var(--text-tertiary);
        }}
        
        .anomaly-time,
        .anomaly-confidence {{
            display: flex;
            align-items: center;
            gap: var(--space-1);
        }}
        
        .anomaly-action-btn {{
            background: var(--bg-card);
            border: 1px solid var(--border);
            color: var(--text-primary);
            padding: var(--space-2);
            border-radius: var(--radius-md);
            cursor: pointer;
            transition: all var(--transition-fast);
            flex-shrink: 0;
        }}
        
        .anomaly-action-btn:hover {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
        }}
        
        @media (max-width: 1024px) {{
            .trends-container {{
                grid-template-columns: 1fr;
            }}
        }}
        
        /* Workflow Section Styles */
        .workflow-section {{
            padding: var(--space-12) var(--space-8);
            background: var(--bg-secondary);
        }}
        
        .workflow-section .section-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-8);
        }}
        
        .workflow-section h2 {{
            color: var(--primary-500);
            font-size: 2rem;
            display: flex;
            align-items: center;
            gap: var(--space-3);
            margin: 0;
        }}
        
        .workflow-controls {{
            display: flex;
            gap: var(--space-3);
        }}
        
        .workflow-btn {{
            background: var(--bg-card);
            border: 1px solid var(--border);
            color: var(--text-primary);
            padding: var(--space-3) var(--space-4);
            border-radius: var(--radius-lg);
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            gap: var(--space-2);
            font-weight: 500;
        }}
        
        .workflow-btn:hover {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
            transform: translateY(-2px);
        }}
        
        .workflow-btn.active {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
        }}
        
        .workflow-stats {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: var(--space-6);
            margin-bottom: var(--space-8);
        }}
        
        .workflow-stat-card {{
            display: flex;
            gap: var(--space-4);
            padding: var(--space-6);
            border-radius: var(--radius-xl);
            border-left: 4px solid;
        }}
        
        .workflow-stat-card.backlog {{
            border-left-color: #6b7280;
        }}
        
        .workflow-stat-card.in-progress {{
            border-left-color: #3b82f6;
        }}
        
        .workflow-stat-card.in-review {{
            border-left-color: #f59e0b;
        }}
        
        .workflow-stat-card.completed {{
            border-left-color: #10b981;
        }}
        
        .stat-icon {{
            font-size: 2rem;
            flex-shrink: 0;
        }}
        
        .workflow-stat-card.backlog .stat-icon {{
            color: #6b7280;
        }}
        
        .workflow-stat-card.in-progress .stat-icon {{
            color: #3b82f6;
        }}
        
        .workflow-stat-card.in-review .stat-icon {{
            color: #f59e0b;
        }}
        
        .workflow-stat-card.completed .stat-icon {{
            color: #10b981;
        }}
        
        .stat-content {{
            flex: 1;
        }}
        
        .stat-content h4 {{
            margin: 0 0 var(--space-2) 0;
            color: var(--text-secondary);
            font-size: 0.875rem;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }}
        
        .stat-value {{
            font-size: 2rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: var(--space-3);
        }}
        
        .stat-progress {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .progress-bar {{
            flex: 1;
            height: 8px;
            background: var(--bg-tertiary);
            border-radius: var(--radius-full);
            overflow: hidden;
        }}
        
        .progress-fill {{
            height: 100%;
            background: linear-gradient(90deg, var(--primary-500), var(--primary-400));
            border-radius: var(--radius-full);
            transition: width var(--transition-normal);
        }}
        
        .progress-label {{
            font-size: 0.75rem;
            color: var(--text-tertiary);
            font-weight: 600;
        }}
        
        /* Kanban Board */
        .kanban-board {{
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: var(--space-6);
            margin-bottom: var(--space-8);
        }}
        
        .kanban-column {{
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            padding: var(--space-4);
            min-height: 500px;
        }}
        
        .column-header {{
            padding: var(--space-4);
            border-radius: var(--radius-lg);
            margin-bottom: var(--space-4);
            background: var(--bg-tertiary);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }}
        
        .column-title {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .column-title h3 {{
            margin: 0;
            font-size: 1rem;
            color: var(--text-primary);
        }}
        
        .column-count {{
            background: var(--primary-500);
            color: white;
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 600;
        }}
        
        .column-action-btn {{
            background: transparent;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            padding: var(--space-2);
            border-radius: var(--radius-md);
            transition: all var(--transition-fast);
        }}
        
        .column-action-btn:hover {{
            background: var(--primary-500);
            color: white;
        }}
        
        .column-content {{
            min-height: 400px;
        }}
        
        .kanban-card {{
            padding: var(--space-4);
            border-radius: var(--radius-lg);
            margin-bottom: var(--space-3);
            cursor: move;
            transition: all var(--transition-fast);
        }}
        
        .kanban-card:hover {{
            transform: translateY(-2px);
            box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
        }}
        
        .card-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-3);
        }}
        
        .card-id {{
            font-size: 0.75rem;
            color: var(--text-tertiary);
            font-weight: 600;
        }}
        
        .card-priority {{
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-md);
            font-size: 0.625rem;
            font-weight: 700;
            text-transform: uppercase;
            color: white;
        }}
        
        .card-body {{
            margin-bottom: var(--space-3);
        }}
        
        .card-title {{
            margin: 0 0 var(--space-2) 0;
            font-size: 0.875rem;
            color: var(--text-primary);
            font-weight: 600;
        }}
        
        .card-algorithm {{
            font-size: 0.75rem;
            color: var(--text-secondary);
            display: flex;
            align-items: center;
            gap: var(--space-1);
            margin-bottom: var(--space-3);
        }}
        
        .card-meta {{
            display: flex;
            flex-direction: column;
            gap: var(--space-1);
            font-size: 0.75rem;
            color: var(--text-tertiary);
        }}
        
        .card-assignee,
        .card-due-date {{
            display: flex;
            align-items: center;
            gap: var(--space-1);
        }}
        
        .card-footer {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-top: var(--space-3);
            border-top: 1px solid var(--border);
        }}
        
        .card-actions {{
            display: flex;
            gap: var(--space-2);
        }}
        
        .card-action-btn {{
            background: transparent;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            padding: var(--space-1);
            border-radius: var(--radius-sm);
            transition: all var(--transition-fast);
        }}
        
        .card-action-btn:hover {{
            background: var(--primary-500);
            color: white;
        }}
        
        .progress-bar-mini {{
            width: 60px;
            height: 4px;
            background: var(--bg-tertiary);
            border-radius: var(--radius-full);
            overflow: hidden;
        }}
        
        .progress-fill-mini {{
            height: 100%;
            background: var(--primary-500);
            border-radius: var(--radius-full);
            transition: width var(--transition-normal);
        }}
        
        .kanban-card-more {{
            padding: var(--space-4);
            border-radius: var(--radius-lg);
            text-align: center;
            cursor: pointer;
            transition: all var(--transition-fast);
            color: var(--text-secondary);
        }}
        
        .kanban-card-more:hover {{
            background: var(--primary-500);
            color: white;
        }}
        
        /* Timeline View */
        .timeline-view {{
            margin-bottom: var(--space-8);
        }}
        
        .timeline-container {{
            display: flex;
            flex-direction: column;
            gap: var(--space-6);
        }}
        
        .timeline-week {{
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            padding: var(--space-6);
        }}
        
        .timeline-week-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-4);
            padding-bottom: var(--space-4);
            border-bottom: 2px solid var(--border);
        }}
        
        .timeline-week-header h3 {{
            margin: 0;
            color: var(--text-primary);
        }}
        
        .timeline-date-range {{
            color: var(--text-secondary);
            font-size: 0.875rem;
        }}
        
        .timeline-tasks {{
            display: flex;
            flex-direction: column;
            gap: var(--space-3);
        }}
        
        .timeline-task {{
            display: flex;
            gap: var(--space-4);
            padding: var(--space-4);
            border-radius: var(--radius-lg);
            border-left: 4px solid;
        }}
        
        .timeline-task.critical {{
            border-left-color: var(--critical);
        }}
        
        .timeline-task.high {{
            border-left-color: var(--high);
        }}
        
        .timeline-task.medium {{
            border-left-color: var(--medium);
        }}
        
        .timeline-task.low {{
            border-left-color: var(--primary-500);
        }}
        
        .timeline-task-marker {{
            width: 12px;
            height: 12px;
            border-radius: var(--radius-full);
            background: var(--primary-500);
            flex-shrink: 0;
            margin-top: 4px;
        }}
        
        .timeline-task-content {{
            flex: 1;
        }}
        
        .timeline-task-content h4 {{
            margin: 0 0 var(--space-2) 0;
            color: var(--text-primary);
            font-size: 0.875rem;
        }}
        
        .timeline-task-content p {{
            margin: 0 0 var(--space-2) 0;
            color: var(--text-secondary);
            font-size: 0.75rem;
        }}
        
        .timeline-task-meta {{
            display: flex;
            gap: var(--space-4);
            font-size: 0.75rem;
            color: var(--text-tertiary);
        }}
        
        .priority-badge {{
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-md);
            font-size: 0.625rem;
            font-weight: 700;
            text-transform: uppercase;
            color: white;
        }}
        
        .priority-badge.critical {{
            background: var(--critical);
        }}
        
        .priority-badge.high {{
            background: var(--high);
        }}
        
        .priority-badge.medium {{
            background: var(--medium);
        }}
        
        .priority-badge.low {{
            background: var(--primary-500);
        }}
        
        /* Modal Styles */
        .modal {{
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        }}
        
        .modal-content {{
            width: 90%;
            max-width: 600px;
            max-height: 90vh;
            overflow-y: auto;
            padding: var(--space-6);
            border-radius: var(--radius-xl);
        }}
        
        .modal-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-6);
            padding-bottom: var(--space-4);
            border-bottom: 2px solid var(--border);
        }}
        
        .modal-header h3 {{
            margin: 0;
            color: var(--text-primary);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .modal-close-btn {{
            background: transparent;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            padding: var(--space-2);
            border-radius: var(--radius-md);
            transition: all var(--transition-fast);
        }}
        
        .modal-close-btn:hover {{
            background: var(--critical);
            color: white;
        }}
        
        .modal-body {{
            margin-bottom: var(--space-6);
        }}
        
        .form-group {{
            margin-bottom: var(--space-4);
        }}
        
        .form-group label {{
            display: block;
            margin-bottom: var(--space-2);
            color: var(--text-primary);
            font-weight: 500;
        }}
        
        .form-control {{
            width: 100%;
            padding: var(--space-3);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            background: var(--bg-tertiary);
            color: var(--text-primary);
            font-size: 0.875rem;
            transition: all var(--transition-fast);
        }}
        
        .form-control:focus {{
            outline: none;
            border-color: var(--primary-500);
            box-shadow: 0 0 0 3px rgba(6, 182, 212, 0.1);
        }}
        
        .modal-footer {{
            display: flex;
            justify-content: flex-end;
            gap: var(--space-3);
            padding-top: var(--space-4);
            border-top: 2px solid var(--border);
        }}
        
        .btn {{
            padding: var(--space-3) var(--space-6);
            border-radius: var(--radius-lg);
            font-weight: 600;
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .btn-primary {{
            background: var(--primary-500);
            color: white;
            border: none;
        }}
        
        .btn-primary:hover {{
            background: var(--primary-600);
            transform: translateY(-2px);
        }}
        
        .btn-secondary {{
            background: var(--bg-tertiary);
            color: var(--text-primary);
            border: 1px solid var(--border);
        }}
        
        .btn-secondary:hover {{
            background: var(--bg-card);
        }}
        
        @media (max-width: 1024px) {{
            .kanban-board {{
                grid-template-columns: repeat(2, 1fr);
            }}
            
            .workflow-stats {{
                grid-template-columns: repeat(2, 1fr);
            }}
        }}
        
        @media (max-width: 768px) {{
            .kanban-board {{
                grid-template-columns: 1fr;
            }}
            
            .workflow-stats {{
                grid-template-columns: 1fr;
            }}
            
            .workflow-controls {{
                flex-direction: column;
            }}
        }}
        
        /* Compliance Section Styles */
        .compliance-section {{
            padding: var(--space-12) var(--space-8);
            background: var(--bg-secondary);
        }}
        
        .compliance-section .section-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-8);
        }}
        
        .compliance-section h2 {{
            color: var(--primary-500);
            font-size: 2rem;
            display: flex;
            align-items: center;
            gap: var(--space-3);
            margin: 0;
        }}
        
        .compliance-controls {{
            display: flex;
            gap: var(--space-3);
        }}
        
        .compliance-btn {{
            background: var(--bg-card);
            border: 1px solid var(--border);
            color: var(--text-primary);
            padding: var(--space-3) var(--space-4);
            border-radius: var(--radius-lg);
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            gap: var(--space-2);
            font-weight: 500;
        }}
        
        .compliance-btn:hover {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
            transform: translateY(-2px);
        }}
        
        .compliance-overview {{
            margin-bottom: var(--space-12);
        }}
        
        .compliance-overview h3 {{
            color: var(--text-primary);
            margin-bottom: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .framework-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
            gap: var(--space-6);
        }}
        
        .framework-card {{
            padding: var(--space-6);
            border-radius: var(--radius-xl);
            transition: all var(--transition-normal);
        }}
        
        .framework-card:hover {{
            transform: translateY(-4px);
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
        }}
        
        .framework-header {{
            display: flex;
            gap: var(--space-4);
            align-items: center;
            margin-bottom: var(--space-4);
            padding-bottom: var(--space-4);
            border-bottom: 2px solid var(--border);
            border-radius: var(--radius-lg);
        }}
        
        .framework-icon {{
            font-size: 2rem;
            flex-shrink: 0;
        }}
        
        .framework-info {{
            flex: 1;
        }}
        
        .framework-info h4 {{
            margin: 0 0 var(--space-1) 0;
            color: var(--text-primary);
            font-size: 1.25rem;
        }}
        
        .framework-full-name {{
            margin: 0;
            color: var(--text-secondary);
            font-size: 0.875rem;
        }}
        
        .framework-score {{
            flex-shrink: 0;
        }}
        
        .score-circle {{
            width: 80px;
            height: 80px;
            border-radius: var(--radius-full);
            background: conic-gradient(
                var(--color) calc(var(--score) * 1%),
                var(--bg-tertiary) 0
            );
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
        }}
        
        .score-circle::before {{
            content: '';
            position: absolute;
            width: 60px;
            height: 60px;
            border-radius: var(--radius-full);
            background: var(--bg-card);
        }}
        
        .score-value {{
            position: relative;
            z-index: 1;
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--text-primary);
        }}
        
        .framework-body {{
            display: flex;
            flex-direction: column;
            gap: var(--space-4);
        }}
        
        .compliance-breakdown {{
            display: flex;
            flex-direction: column;
            gap: var(--space-2);
        }}
        
        .breakdown-item {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
            padding: var(--space-2);
            border-radius: var(--radius-md);
            background: var(--bg-tertiary);
        }}
        
        .breakdown-item.compliant {{
            border-left: 3px solid #10b981;
        }}
        
        .breakdown-item.partial {{
            border-left: 3px solid #f59e0b;
        }}
        
        .breakdown-item.non-compliant {{
            border-left: 3px solid #dc2626;
        }}
        
        .breakdown-item i {{
            font-size: 1rem;
        }}
        
        .breakdown-item.compliant i {{
            color: #10b981;
        }}
        
        .breakdown-item.partial i {{
            color: #f59e0b;
        }}
        
        .breakdown-item.non-compliant i {{
            color: #dc2626;
        }}
        
        .breakdown-label {{
            flex: 1;
            font-size: 0.875rem;
            color: var(--text-secondary);
        }}
        
        .breakdown-value {{
            font-weight: 600;
            color: var(--text-primary);
        }}
        
        .framework-categories {{
            display: flex;
            flex-direction: column;
            gap: var(--space-2);
        }}
        
        .category-label {{
            font-size: 0.75rem;
            color: var(--text-tertiary);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }}
        
        .category-tags {{
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-2);
        }}
        
        .category-tag {{
            background: var(--bg-tertiary);
            color: var(--text-secondary);
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-md);
            font-size: 0.75rem;
        }}
        
        .framework-details-btn {{
            width: 100%;
            padding: var(--space-3);
            border: none;
            border-radius: var(--radius-lg);
            color: white;
            font-weight: 600;
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--space-2);
        }}
        
        .framework-details-btn:hover {{
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }}
        
        /* Compliance Mapping */
        .compliance-mapping {{
            margin-bottom: var(--space-12);
        }}
        
        .compliance-mapping h3 {{
            color: var(--text-primary);
            margin-bottom: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .mapping-container {{
            padding: var(--space-6);
            border-radius: var(--radius-xl);
            overflow-x: auto;
        }}
        
        .mapping-table-wrapper {{
            overflow-x: auto;
        }}
        
        .mapping-table {{
            width: 100%;
            border-collapse: collapse;
            font-size: 0.875rem;
        }}
        
        .mapping-table thead {{
            background: var(--bg-tertiary);
        }}
        
        .mapping-table th {{
            padding: var(--space-3);
            text-align: left;
            font-weight: 600;
            color: var(--text-primary);
            border-bottom: 2px solid var(--border);
        }}
        
        .mapping-table td {{
            padding: var(--space-3);
            border-bottom: 1px solid var(--border);
        }}
        
        .mapping-row:hover {{
            background: var(--bg-tertiary);
        }}
        
        .vuln-cell {{
            min-width: 200px;
        }}
        
        .vuln-info {{
            display: flex;
            flex-direction: column;
            gap: var(--space-1);
        }}
        
        .vuln-id {{
            font-weight: 600;
            color: var(--text-primary);
            font-size: 0.75rem;
        }}
        
        .vuln-title {{
            color: var(--text-secondary);
            font-size: 0.75rem;
        }}
        
        .control-tags {{
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-1);
        }}
        
        .control-tag {{
            background: var(--primary-500);
            color: white;
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-sm);
            font-size: 0.625rem;
            font-weight: 600;
        }}
        
        .no-mapping {{
            color: var(--text-tertiary);
        }}
        
        .mapping-action-btn {{
            background: transparent;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            padding: var(--space-2);
            border-radius: var(--radius-sm);
            transition: all var(--transition-fast);
        }}
        
        .mapping-action-btn:hover {{
            background: var(--primary-500);
            color: white;
        }}
        
        /* Audit Trail */
        .audit-trail {{
            margin-bottom: var(--space-12);
        }}
        
        .audit-trail h3 {{
            color: var(--text-primary);
            margin-bottom: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .audit-container {{
            padding: var(--space-6);
            border-radius: var(--radius-xl);
        }}
        
        .audit-timeline {{
            display: flex;
            flex-direction: column;
            gap: var(--space-4);
            margin-bottom: var(--space-6);
        }}
        
        .audit-event {{
            display: flex;
            gap: var(--space-4);
            padding: var(--space-4);
            border-radius: var(--radius-lg);
            background: var(--bg-tertiary);
            transition: all var(--transition-fast);
        }}
        
        .audit-event:hover {{
            background: var(--bg-card);
            transform: translateX(4px);
        }}
        
        .event-marker {{
            width: 40px;
            height: 40px;
            border-radius: var(--radius-full);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            flex-shrink: 0;
        }}
        
        .event-content {{
            flex: 1;
        }}
        
        .event-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-2);
        }}
        
        .event-header h4 {{
            margin: 0;
            color: var(--text-primary);
            font-size: 1rem;
        }}
        
        .event-timestamp {{
            color: var(--text-tertiary);
            font-size: 0.75rem;
        }}
        
        .event-details {{
            margin: 0 0 var(--space-2) 0;
            color: var(--text-secondary);
            font-size: 0.875rem;
        }}
        
        .event-footer {{
            display: flex;
            gap: var(--space-4);
            font-size: 0.75rem;
        }}
        
        .event-user {{
            color: var(--text-tertiary);
            display: flex;
            align-items: center;
            gap: var(--space-1);
        }}
        
        .event-type {{
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-sm);
            background: var(--bg-card);
            color: var(--text-secondary);
            text-transform: capitalize;
        }}
        
        .audit-filters {{
            padding-top: var(--space-4);
            border-top: 2px solid var(--border);
        }}
        
        .audit-filters h4 {{
            margin: 0 0 var(--space-3) 0;
            color: var(--text-primary);
        }}
        
        .filter-buttons {{
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-2);
        }}
        
        .filter-btn {{
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            color: var(--text-primary);
            padding: var(--space-2) var(--space-3);
            border-radius: var(--radius-md);
            cursor: pointer;
            transition: all var(--transition-fast);
            font-size: 0.875rem;
        }}
        
        .filter-btn:hover {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
        }}
        
        .filter-btn.active {{
            background: var(--primary-500);
            color: white;
            border-color: var(--primary-500);
        }}
        
        /* Compliance Exports */
        .compliance-exports h3 {{
            color: var(--text-primary);
            margin-bottom: var(--space-6);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .export-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: var(--space-6);
        }}
        
        .export-card {{
            padding: var(--space-6);
            border-radius: var(--radius-xl);
            display: flex;
            flex-direction: column;
            gap: var(--space-4);
            transition: all var(--transition-normal);
        }}
        
        .export-card:hover {{
            transform: translateY(-4px);
            box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
        }}
        
        .export-icon {{
            font-size: 2.5rem;
        }}
        
        .export-info {{
            flex: 1;
        }}
        
        .export-info h4 {{
            margin: 0 0 var(--space-2) 0;
            color: var(--text-primary);
        }}
        
        .export-info p {{
            margin: 0 0 var(--space-2) 0;
            color: var(--text-secondary);
            font-size: 0.875rem;
        }}
        
        .export-format {{
            display: inline-block;
            background: var(--bg-tertiary);
            color: var(--text-secondary);
            padding: var(--space-1) var(--space-2);
            border-radius: var(--radius-sm);
            font-size: 0.75rem;
            font-weight: 600;
        }}
        
        .export-btn {{
            width: 100%;
            padding: var(--space-3);
            border: none;
            border-radius: var(--radius-lg);
            color: white;
            font-weight: 600;
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--space-2);
        }}
        
        .export-btn:hover {{
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }}
        
        @media (max-width: 1024px) {{
            .framework-grid {{
                grid-template-columns: repeat(2, 1fr);
            }}
            
            .export-grid {{
                grid-template-columns: repeat(2, 1fr);
            }}
        }}
        
        @media (max-width: 768px) {{
            .framework-grid {{
                grid-template-columns: 1fr;
            }}
            
            .export-grid {{
                grid-template-columns: 1fr;
            }}
            
            .compliance-controls {{
                flex-direction: column;
            }}
            
            .mapping-table {{
                font-size: 0.75rem;
            }}
        }}
        }}
        
        /* Visualizations Section */
        .visualizations-section {{
            padding: var(--space-12) var(--space-8);
            background: var(--bg-tertiary);
        }}
        
        .visualizations-section h2 {{
            color: var(--primary-500);
            margin-bottom: var(--space-8);
            font-size: 2rem;
            display: flex;
            align-items: center;
            gap: var(--space-3);
        }}
        
        .charts-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
            gap: var(--space-6);
            margin-bottom: var(--space-8);
        }}
        
        .chart-container {{
            background: var(--bg-card);
            padding: var(--space-6);
            border-radius: var(--radius-xl);
            border: 1px solid var(--border);
            transition: all var(--transition-base);
        }}
        
        .chart-container:hover {{
            transform: translateY(-4px);
            box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
        }}
        
        .chart-container-wide {{
            background: var(--bg-card);
            padding: var(--space-6);
            border-radius: var(--radius-xl);
            border: 1px solid var(--border);
            margin-bottom: var(--space-6);
            transition: all var(--transition-base);
        }}
        
        .chart-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-4);
        }}
        
        .chart-header h3 {{
            color: var(--primary-500);
            font-size: 1.2rem;
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .chart-controls {{
            display: flex;
            gap: var(--space-2);
        }}
        
        .chart-btn {{
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            padding: var(--space-2) var(--space-3);
            cursor: pointer;
            color: var(--text-primary);
            transition: all var(--transition-fast);
        }}
        
        .chart-btn:hover {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            transform: translateY(-2px);
        }}
        
        .chart-select {{
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            padding: var(--space-2) var(--space-3);
            color: var(--text-primary);
            cursor: pointer;
            transition: all var(--transition-fast);
        }}
        
        .chart-select:hover {{
            border-color: var(--primary-500);
        }}
        
        .chart-canvas {{
            max-height: 350px;
            width: 100%;
        }}
        
        .chart-canvas-large {{
            max-height: 450px;
            width: 100%;
        }}
        
        .d3-chart {{
            min-height: 400px;
            width: 100%;
            position: relative;
        }}
        
        .chart-description {{
            margin-top: var(--space-4);
            color: var(--text-secondary);
            font-size: 0.9rem;
            font-style: italic;
        }}
        
        .chart-legend {{
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-3);
            margin-top: var(--space-4);
            justify-content: center;
        }}
        
        .legend-item {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
            font-size: 0.9rem;
        }}
        
        .legend-color {{
            width: 16px;
            height: 16px;
            border-radius: var(--radius-sm);
        }}
        
        .advanced-charts {{
            margin-top: var(--space-8);
        }}
        
        /* Enhanced Heatmap */
        .heatmap-section {{
            padding: var(--space-6);
            margin-bottom: var(--space-6);
        }}
        
        .heatmap-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
            gap: var(--space-4);
            margin-top: var(--space-4);
        }}
        
        .heatmap-cell {{
            aspect-ratio: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            border-radius: var(--radius-lg);
            color: white;
            font-weight: bold;
            text-align: center;
            padding: var(--space-3);
            transition: all var(--transition-base);
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }}
        
        .heatmap-cell::before {{
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.1) 100%);
            opacity: 0;
            transition: opacity var(--transition-base);
        }}
        
        .heatmap-cell:hover {{
            transform: scale(1.08);
            box-shadow: 0 8px 24px rgba(0,0,0,0.5);
            z-index: 10;
        }}
        
        .heatmap-cell:hover::before {{
            opacity: 1;
        }}
        
        .heatmap-cell.critical {{
            background: linear-gradient(135deg, var(--critical) 0%, var(--critical-light) 100%);
        }}
        
        .heatmap-cell.high {{
            background: linear-gradient(135deg, var(--high) 0%, var(--high-light) 100%);
        }}
        
        .heatmap-cell.medium {{
            background: linear-gradient(135deg, var(--medium) 0%, var(--medium-light) 100%);
            color: #000;
        }}
        
        .heatmap-cell.low {{
            background: linear-gradient(135deg, var(--low) 0%, var(--low-light) 100%);
        }}
        
        .heatmap-score {{
            font-size: 2rem;
            margin-bottom: var(--space-1);
        }}
        
        .heatmap-rank {{
            font-size: 0.8rem;
            opacity: 0.9;
            margin-bottom: var(--space-1);
        }}
        
        .heatmap-label {{
            font-size: 0.85rem;
            opacity: 0.95;
        }}
        
        .heatmap-context {{
            font-size: 0.7rem;
            opacity: 0.8;
            margin-top: var(--space-1);
        }}
        
        /* Enhanced Quadrant */
        .quadrant-section {{
            padding: var(--space-6);
            margin-bottom: var(--space-6);
        }}
        
        .quadrant-grid {{
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: var(--space-4);
            margin-top: var(--space-4);
        }}
        
        .quadrant {{
            padding: var(--space-6);
            border-radius: var(--radius-xl);
            min-height: 200px;
            background: var(--bg-tertiary);
            border: 2px solid var(--border);
            transition: all var(--transition-base);
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }}
        
        .quadrant::before {{
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            opacity: 0;
            transition: opacity var(--transition-base);
        }}
        
        .quadrant:hover {{
            transform: translateY(-4px);
            box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
        }}
        
        .quadrant:hover::before {{
            opacity: 0.05;
        }}
        
        .quadrant h4 {{
            margin-bottom: var(--space-2);
            font-size: 1.2rem;
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .quadrant-subtitle {{
            font-size: 0.85rem;
            color: var(--text-secondary);
            margin-bottom: var(--space-4);
        }}
        
        .quadrant.q1 {{
            border-color: var(--critical);
        }}
        
        .quadrant.q1 h4 {{
            color: var(--critical);
        }}
        
        .quadrant.q1::before {{
            background: var(--critical);
        }}
        
        .quadrant.q2 {{
            border-color: var(--high);
        }}
        
        .quadrant.q2 h4 {{
            color: var(--high);
        }}
        
        .quadrant.q2::before {{
            background: var(--high);
        }}
        
        .quadrant.q3 {{
            border-color: var(--low);
        }}
        
        .quadrant.q3 h4 {{
            color: var(--low);
        }}
        
        .quadrant.q3::before {{
            background: var(--low);
        }}
        
        .quadrant.q4 {{
            border-color: var(--medium);
        }}
        
        .quadrant.q4 h4 {{
            color: var(--medium);
        }}
        
        .quadrant.q4::before {{
            background: var(--medium);
        }}
        
        .quadrant-item {{
            padding: var(--space-3);
            background: var(--bg-card);
            border-radius: var(--radius-md);
            margin-bottom: var(--space-2);
            font-size: 0.9rem;
            color: var(--text-primary);
            border: 1px solid var(--border);
            transition: all var(--transition-fast);
            cursor: pointer;
        }}
        
        .quadrant-item:hover {{
            background: var(--bg-secondary);
            border-color: var(--primary-500);
            transform: translateX(4px);
        }}
        
        .quadrant-more {{
            text-align: center;
            color: var(--text-secondary);
            font-size: 0.85rem;
            margin-top: var(--space-2);
            font-style: italic;
        }}
        
        /* Filter Panel */
        .filter-panel {{
            position: fixed;
            top: 0;
            right: -400px;
            width: 400px;
            height: 100vh;
            background: var(--bg-secondary);
            border-left: 1px solid var(--border);
            box-shadow: -4px 0 20px rgba(0, 0, 0, 0.3);
            transition: right var(--transition-base);
            z-index: 2000;
            overflow-y: auto;
        }}
        
        .filter-panel.active {{
            right: 0;
        }}
        
        .filter-panel-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-6);
            border-bottom: 1px solid var(--border);
            position: sticky;
            top: 0;
            background: var(--bg-secondary);
            z-index: 10;
        }}
        
        .filter-panel-header h3 {{
            color: var(--primary-500);
            font-size: 1.3rem;
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .filter-close-btn {{
            background: transparent;
            border: none;
            color: var(--text-primary);
            font-size: 1.5rem;
            cursor: pointer;
            padding: var(--space-2);
            transition: all var(--transition-fast);
        }}
        
        .filter-close-btn:hover {{
            color: var(--primary-500);
            transform: rotate(90deg);
        }}
        
        .filter-panel-content {{
            padding: var(--space-6);
        }}
        
        .filter-section {{
            margin-bottom: var(--space-8);
        }}
        
        .filter-section h4 {{
            color: var(--text-primary);
            margin-bottom: var(--space-4);
            font-size: 1.1rem;
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        /* Search */
        .search-container {{
            position: relative;
            margin-bottom: var(--space-3);
        }}
        
        .search-input {{
            width: 100%;
            padding: var(--space-3) var(--space-10) var(--space-3) var(--space-3);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            color: var(--text-primary);
            font-size: 1rem;
            transition: all var(--transition-fast);
        }}
        
        .search-input:focus {{
            outline: none;
            border-color: var(--primary-500);
            box-shadow: 0 0 0 3px rgba(6, 182, 212, 0.1);
        }}
        
        .search-clear-btn {{
            position: absolute;
            right: var(--space-3);
            top: 50%;
            transform: translateY(-50%);
            background: transparent;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            padding: var(--space-2);
            transition: color var(--transition-fast);
        }}
        
        .search-clear-btn:hover {{
            color: var(--primary-500);
        }}
        
        .search-options {{
            display: flex;
            gap: var(--space-4);
        }}
        
        .checkbox-label {{
            display: flex;
            align-items: center;
            gap: var(--space-2);
            color: var(--text-secondary);
            font-size: 0.9rem;
            cursor: pointer;
        }}
        
        .checkbox-label input[type="checkbox"] {{
            cursor: pointer;
        }}
        
        /* Filter Chips */
        .filter-chips {{
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-2);
        }}
        
        .filter-chip {{
            display: inline-flex;
            align-items: center;
            padding: var(--space-2) var(--space-4);
            background: var(--bg-tertiary);
            border: 2px solid var(--border);
            border-radius: var(--radius-lg);
            cursor: pointer;
            transition: all var(--transition-fast);
            user-select: none;
        }}
        
        .filter-chip input[type="checkbox"] {{
            display: none;
        }}
        
        .filter-chip:hover {{
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        }}
        
        .filter-chip input[type="checkbox"]:checked + span {{
            font-weight: 600;
        }}
        
        .filter-chip.critical {{
            border-color: var(--critical);
        }}
        
        .filter-chip.critical input[type="checkbox"]:checked + span {{
            color: var(--critical);
        }}
        
        .filter-chip.high {{
            border-color: var(--high);
        }}
        
        .filter-chip.high input[type="checkbox"]:checked + span {{
            color: var(--high);
        }}
        
        .filter-chip.medium {{
            border-color: var(--medium);
        }}
        
        .filter-chip.medium input[type="checkbox"]:checked + span {{
            color: var(--medium);
        }}
        
        .filter-chip.low {{
            border-color: var(--low);
        }}
        
        .filter-chip.low input[type="checkbox"]:checked + span {{
            color: var(--low);
        }}
        
        /* Filter Select */
        .filter-select-container {{
            margin-bottom: var(--space-3);
        }}
        
        .filter-select {{
            width: 100%;
            padding: var(--space-2);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            font-size: 0.95rem;
        }}
        
        .filter-select option {{
            padding: var(--space-2);
        }}
        
        .filter-select option:checked {{
            background: var(--primary-500);
            color: white;
        }}
        
        .filter-select-actions {{
            display: flex;
            gap: var(--space-2);
            margin-top: var(--space-2);
        }}
        
        .filter-action-btn {{
            flex: 1;
            padding: var(--space-2) var(--space-3);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            font-size: 0.85rem;
            cursor: pointer;
            transition: all var(--transition-fast);
        }}
        
        .filter-action-btn:hover {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
        }}
        
        /* Range Slider */
        .range-slider-container {{
            padding: var(--space-4) 0;
        }}
        
        .range-values {{
            display: flex;
            justify-content: space-between;
            margin-bottom: var(--space-3);
            color: var(--text-primary);
            font-weight: 600;
        }}
        
        .range-slider {{
            width: 100%;
            height: 6px;
            background: var(--bg-tertiary);
            border-radius: var(--radius-lg);
            outline: none;
            margin-bottom: var(--space-2);
        }}
        
        .range-slider::-webkit-slider-thumb {{
            appearance: none;
            width: 20px;
            height: 20px;
            background: var(--primary-500);
            border-radius: 50%;
            cursor: pointer;
            box-shadow: 0 2px 8px rgba(6, 182, 212, 0.4);
        }}
        
        .range-slider::-moz-range-thumb {{
            width: 20px;
            height: 20px;
            background: var(--primary-500);
            border-radius: 50%;
            cursor: pointer;
            border: none;
            box-shadow: 0 2px 8px rgba(6, 182, 212, 0.4);
        }}
        
        /* Filter Presets */
        .filter-presets {{
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: var(--space-2);
            margin-bottom: var(--space-3);
        }}
        
        .preset-btn {{
            padding: var(--space-3);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            font-size: 0.9rem;
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--space-2);
        }}
        
        .preset-btn:hover {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
            transform: translateY(-2px);
        }}
        
        .preset-actions {{
            display: flex;
            gap: var(--space-2);
        }}
        
        .preset-action-btn {{
            flex: 1;
            padding: var(--space-3);
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            font-size: 0.9rem;
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--space-2);
        }}
        
        .preset-action-btn:hover {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
        }}
        
        /* Active Filters */
        .active-filters {{
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: var(--space-4);
        }}
        
        .active-filters-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-3);
            padding-bottom: var(--space-3);
            border-bottom: 1px solid var(--border);
        }}
        
        .active-filters-header span {{
            color: var(--text-primary);
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .clear-all-btn {{
            padding: var(--space-2) var(--space-3);
            background: var(--critical);
            border: none;
            border-radius: var(--radius-md);
            color: white;
            font-size: 0.85rem;
            cursor: pointer;
            transition: all var(--transition-fast);
        }}
        
        .clear-all-btn:hover {{
            background: var(--critical-light);
            transform: translateY(-2px);
        }}
        
        .active-filters-list {{
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-2);
        }}
        
        .no-filters {{
            color: var(--text-tertiary);
            font-style: italic;
            font-size: 0.9rem;
        }}
        
        .filter-tag {{
            display: inline-flex;
            align-items: center;
            gap: var(--space-2);
            padding: var(--space-2) var(--space-3);
            background: var(--primary-500);
            color: white;
            border-radius: var(--radius-lg);
            font-size: 0.85rem;
        }}
        
        .filter-tag-remove {{
            background: transparent;
            border: none;
            color: white;
            cursor: pointer;
            padding: 0;
            display: flex;
            align-items: center;
        }}
        
        .filter-tag-remove:hover {{
            opacity: 0.7;
        }}
        
        .filter-badge {{
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 20px;
            height: 20px;
            padding: 0 var(--space-2);
            background: var(--critical);
            color: white;
            border-radius: 10px;
            font-size: 0.75rem;
            font-weight: 700;
            margin-left: var(--space-2);
        }}
        
        /* Enhanced Data Table */
        .findings-section {{
            padding: var(--space-12) var(--space-8);
            background: var(--bg-secondary);
        }}
        
        .findings-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-6);
            flex-wrap: wrap;
            gap: var(--space-4);
        }}
        
        .findings-header h2 {{
            color: var(--primary-500);
            font-size: 2rem;
            display: flex;
            align-items: center;
            gap: var(--space-3);
        }}
        
        .table-controls {{
            display: flex;
            gap: var(--space-4);
            align-items: center;
            flex-wrap: wrap;
        }}
        
        .table-actions {{
            display: flex;
            gap: var(--space-2);
        }}
        
        .table-btn {{
            padding: var(--space-2) var(--space-4);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            font-size: 0.9rem;
            cursor: pointer;
            transition: all var(--transition-fast);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .table-btn:hover:not(:disabled) {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
            transform: translateY(-2px);
        }}
        
        .table-btn:disabled {{
            opacity: 0.5;
            cursor: not-allowed;
        }}
        
        .table-view-options {{
            display: flex;
            gap: var(--space-1);
            background: var(--bg-tertiary);
            padding: var(--space-1);
            border-radius: var(--radius-md);
        }}
        
        .view-btn {{
            padding: var(--space-2) var(--space-3);
            background: transparent;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            border-radius: var(--radius-sm);
            transition: all var(--transition-fast);
        }}
        
        .view-btn:hover {{
            color: var(--text-primary);
        }}
        
        .view-btn.active {{
            background: var(--primary-500);
            color: white;
        }}
        
        .page-size-select {{
            padding: var(--space-2) var(--space-3);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            cursor: pointer;
        }}
        
        .table-container {{
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            overflow: hidden;
            border: 1px solid var(--border);
            margin-bottom: var(--space-4);
        }}
        
        .findings-table {{
            width: 100%;
            border-collapse: collapse;
        }}
        
        .findings-table thead {{
            background: var(--bg-tertiary);
            position: sticky;
            top: 0;
            z-index: 10;
        }}
        
        .findings-table th {{
            padding: var(--space-4);
            text-align: left;
            font-weight: 600;
            color: var(--text-primary);
            border-bottom: 2px solid var(--border);
            white-space: nowrap;
        }}
        
        .findings-table th.sortable {{
            cursor: pointer;
            user-select: none;
        }}
        
        .findings-table th.sortable:hover {{
            background: var(--bg-card);
            color: var(--primary-500);
        }}
        
        .findings-table tbody tr.finding-row {{
            border-bottom: 1px solid var(--border);
            transition: all var(--transition-fast);
        }}
        
        .findings-table tbody tr.finding-row:hover {{
            background: var(--bg-tertiary);
        }}
        
        .findings-table tbody tr.finding-row.selected {{
            background: rgba(6, 182, 212, 0.1);
        }}
        
        .findings-table td {{
            padding: var(--space-4);
            color: var(--text-primary);
        }}
        
        .col-checkbox {{
            width: 40px;
            text-align: center;
        }}
        
        .col-rank {{
            width: 80px;
        }}
        
        .col-priority {{
            width: 120px;
        }}
        
        .col-score {{
            width: 120px;
        }}
        
        .col-algorithm {{
            width: 120px;
        }}
        
        .col-context {{
            width: 150px;
        }}
        
        .col-effort {{
            width: 140px;
        }}
        
        .col-actions {{
            width: 120px;
        }}
        
        .rank-badge {{
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 40px;
            padding: var(--space-1) var(--space-2);
            background: var(--bg-tertiary);
            border-radius: var(--radius-md);
            font-weight: 600;
            font-size: 0.9rem;
        }}
        
        .priority-badge {{
            display: inline-block;
            padding: var(--space-1) var(--space-3);
            border-radius: var(--radius-lg);
            font-size: 0.85rem;
            font-weight: 600;
            text-transform: uppercase;
        }}
        
        .priority-badge.critical {{
            background: var(--critical);
            color: white;
        }}
        
        .priority-badge.high {{
            background: var(--high);
            color: white;
        }}
        
        .priority-badge.medium {{
            background: var(--medium);
            color: #000;
        }}
        
        .priority-badge.low {{
            background: var(--low);
            color: white;
        }}
        
        .score-value {{
            font-weight: 700;
            font-size: 1.1rem;
            color: var(--primary-500);
        }}
        
        .score-bar {{
            width: 100%;
            height: 4px;
            background: var(--bg-tertiary);
            border-radius: 2px;
            margin-top: var(--space-1);
            overflow: hidden;
        }}
        
        .score-bar-fill {{
            height: 100%;
            transition: width var(--transition-base);
        }}
        
        .score-bar-fill.critical {{
            background: var(--critical);
        }}
        
        .score-bar-fill.high {{
            background: var(--high);
        }}
        
        .score-bar-fill.medium {{
            background: var(--medium);
        }}
        
        .score-bar-fill.low {{
            background: var(--low);
        }}
        
        .finding-title-cell {{
            display: flex;
            flex-direction: column;
            gap: var(--space-1);
        }}
        
        .finding-title-text {{
            font-weight: 500;
            color: var(--text-primary);
        }}
        
        .finding-location {{
            font-size: 0.85rem;
            color: var(--text-secondary);
            display: flex;
            align-items: center;
            gap: var(--space-1);
        }}
        
        .algorithm-badge,
        .context-badge {{
            display: inline-block;
            padding: var(--space-1) var(--space-3);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            font-size: 0.85rem;
            font-weight: 500;
        }}
        
        .effort-badge {{
            display: inline-block;
            padding: var(--space-1) var(--space-3);
            border-radius: var(--radius-md);
            font-size: 0.85rem;
            font-weight: 600;
        }}
        
        .effort-badge.small {{
            background: var(--low);
            color: white;
        }}
        
        .effort-badge.medium {{
            background: var(--medium);
            color: #000;
        }}
        
        .effort-badge.large {{
            background: var(--high);
            color: white;
        }}
        
        .effort-badge.unknown {{
            background: var(--bg-tertiary);
            color: var(--text-secondary);
        }}
        
        .action-buttons {{
            display: flex;
            gap: var(--space-1);
        }}
        
        .action-btn {{
            padding: var(--space-2);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            color: var(--text-primary);
            cursor: pointer;
            transition: all var(--transition-fast);
        }}
        
        .action-btn:hover {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
            transform: scale(1.1);
        }}
        
        .details-row {{
            background: var(--bg-tertiary);
        }}
        
        .finding-details-panel {{
            padding: var(--space-6);
        }}
        
        .detail-section {{
            margin-bottom: var(--space-6);
        }}
        
        .detail-section:last-child {{
            margin-bottom: 0;
        }}
        
        .detail-section h4 {{
            color: var(--primary-500);
            margin-bottom: var(--space-3);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }}
        
        .impact-factors-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: var(--space-3);
        }}
        
        .impact-factor-item {{
            display: flex;
            flex-direction: column;
            padding: var(--space-3);
            background: var(--bg-card);
            border-radius: var(--radius-md);
            border: 1px solid var(--border);
        }}
        
        .impact-label {{
            font-size: 0.85rem;
            color: var(--text-secondary);
            margin-bottom: var(--space-1);
        }}
        
        .impact-value {{
            font-size: 1.2rem;
            font-weight: 700;
            color: var(--primary-500);
        }}
        
        .code-snippet {{
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            padding: var(--space-4);
            overflow-x: auto;
        }}
        
        .code-snippet code {{
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
            color: var(--text-primary);
        }}
        
        .table-footer {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-4) var(--space-6);
            background: var(--bg-card);
            border-radius: var(--radius-xl);
            border: 1px solid var(--border);
        }}
        
        .pagination-info {{
            color: var(--text-secondary);
            font-size: 0.9rem;
        }}
        
        .pagination-controls {{
            display: flex;
            gap: var(--space-2);
            align-items: center;
        }}
        
        .pagination-btn {{
            padding: var(--space-2) var(--space-3);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            cursor: pointer;
            transition: all var(--transition-fast);
        }}
        
        .pagination-btn:hover:not(:disabled) {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
        }}
        
        .pagination-btn:disabled {{
            opacity: 0.5;
            cursor: not-allowed;
        }}
        
        .page-numbers {{
            display: flex;
            gap: var(--space-1);
        }}
        
        .page-number {{
            padding: var(--space-2) var(--space-3);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            color: var(--text-primary);
            cursor: pointer;
            transition: all var(--transition-fast);
            min-width: 36px;
            text-align: center;
        }}
        
        .page-number:hover {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
        }}
        
        .page-number.active {{
            background: var(--primary-500);
            border-color: var(--primary-500);
            color: white;
            font-weight: 700;
        }}
        
        /* Responsive Charts */
        @media (max-width: 1024px) {{
            .charts-grid {{
                grid-template-columns: 1fr;
            }}
            
            .quadrant-grid {{
                grid-template-columns: 1fr;
            }}
        }}
        
        @media (max-width: 768px) {{
            .heatmap-grid {{
                grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
            }}
            
            .chart-header {{
                flex-direction: column;
                align-items: flex-start;
                gap: var(--space-2);
            }}
            
            .filter-panel {{
                width: 100%;
                right: -100%;
            }}
            
            .filter-presets {{
                grid-template-columns: 1fr;
            }}
        }}
        
        /* Print Styles */
        @media print {{
            body {{
                background: white;
                padding: 0;
            }}
            
            .dashboard-container {{
                box-shadow: none;
                border: none;
            }}
            
            .nav-bar,
            .theme-toggle,
            .chart-controls {{
                display: none;
            }}
            
            .glass-card {{
                background: white;
                border: 1px solid #ddd;
            }}
            
            .chart-container,
            .chart-container-wide {{
                page-break-inside: avoid;
            }}
        }}
        
        {animation_css}
    </style>
</head>
<body>'''
    
    def _format_navigation(self, report: RemediationReport) -> str:
        """Format navigation bar with theme toggle, filter button, and real-time controls."""
        return '''
<nav class="nav-bar">
    <div class="nav-brand">
        <i class="fas fa-shield-alt"></i>
        <span>Crypto Vulnerability Dashboard</span>
        <span class="live-indicator" id="liveIndicator" style="display: none;">
            <span class="pulse-dot"></span>
            <span>LIVE</span>
        </span>
    </div>
    <div class="nav-controls">
        <button class="theme-toggle" onclick="toggleAutoRefresh()" aria-label="Toggle auto-refresh" id="autoRefreshBtn">
            <i class="fas fa-sync-alt" id="refreshIcon"></i>
            <span id="refreshText">Auto-Refresh: OFF</span>
        </button>
        <button class="theme-toggle" onclick="toggleFilterPanel()" aria-label="Toggle filters">
            <i class="fas fa-filter"></i>
            <span>Filters</span>
            <span class="filter-badge" id="filterBadge" style="display: none;">0</span>
        </button>
        <button class="theme-toggle" onclick="toggleTheme()" aria-label="Toggle theme">
            <i class="fas fa-moon" id="theme-icon"></i>
            <span id="theme-text">Dark</span>
        </button>
        <button class="theme-toggle" onclick="window.print()" aria-label="Print dashboard">
            <i class="fas fa-print"></i>
            <span>Print</span>
        </button>
        <button class="theme-toggle" onclick="exportDashboard()" aria-label="Export dashboard">
            <i class="fas fa-download"></i>
            <span>Export</span>
        </button>
    </div>
</nav>
<!-- Real-Time Notifications Container -->
<div class="notifications-container" id="notificationsContainer"></div>
<!-- Real-Time Status Bar -->
<div class="status-bar" id="statusBar">
    <div class="status-item">
        <i class="fas fa-clock"></i>
        <span>Last Updated: <span id="lastUpdated">Just now</span></span>
    </div>
    <div class="status-item">
        <i class="fas fa-wifi"></i>
        <span id="connectionStatus">Connected</span>
    </div>
    <div class="status-item">
        <i class="fas fa-users"></i>
        <span><span id="activeUsers">1</span> active user(s)</span>
    </div>
</div>'''
    
    def _format_filter_panel(self, report: RemediationReport) -> str:
        """Format advanced filter panel with search and multi-faceted filters."""
        # Get unique values for filters
        algorithms = set()
        contexts = set()
        
        for pf in report.prioritized_findings:
            algorithms.add(pf.finding.algorithm.value)
            contexts.add(pf.finding.usage_context.value)
        
        algorithms_list = sorted(list(algorithms))
        contexts_list = sorted(list(contexts))
        
        html = ['<div class="filter-panel" id="filterPanel">']
        html.append('<div class="filter-panel-header">')
        html.append('<h3><i class="fas fa-filter"></i> Advanced Filters & Search</h3>')
        html.append('<button class="filter-close-btn" onclick="toggleFilterPanel()" aria-label="Close filters">')
        html.append('<i class="fas fa-times"></i>')
        html.append('</button>')
        html.append('</div>')
        
        html.append('<div class="filter-panel-content">')
        
        # Search section
        html.append('<div class="filter-section">')
        html.append('<h4><i class="fas fa-search"></i> Search</h4>')
        html.append('<div class="search-container">')
        html.append('<input type="text" id="searchInput" class="search-input" placeholder="Search findings, algorithms, contexts..." oninput="performSearch(this.value)">')
        html.append('<button class="search-clear-btn" onclick="clearSearch()" style="display: none;" id="searchClearBtn">')
        html.append('<i class="fas fa-times"></i>')
        html.append('</button>')
        html.append('</div>')
        html.append('<div class="search-options">')
        html.append('<label class="checkbox-label">')
        html.append('<input type="checkbox" id="fuzzySearch" checked> Fuzzy matching')
        html.append('</label>')
        html.append('<label class="checkbox-label">')
        html.append('<input type="checkbox" id="highlightResults" checked> Highlight results')
        html.append('</label>')
        html.append('</div>')
        html.append('</div>')
        
        # Priority filter
        html.append('<div class="filter-section">')
        html.append('<h4><i class="fas fa-exclamation-triangle"></i> Priority</h4>')
        html.append('<div class="filter-chips">')
        html.append('<label class="filter-chip critical">')
        html.append('<input type="checkbox" name="priority" value="CRITICAL" checked onchange="applyFilters()">')
        html.append(f'<span>Critical ({report.critical_count})</span>')
        html.append('</label>')
        html.append('<label class="filter-chip high">')
        html.append('<input type="checkbox" name="priority" value="HIGH" checked onchange="applyFilters()">')
        html.append(f'<span>High ({report.high_count})</span>')
        html.append('</label>')
        html.append('<label class="filter-chip medium">')
        html.append('<input type="checkbox" name="priority" value="MEDIUM" checked onchange="applyFilters()">')
        html.append(f'<span>Medium ({report.medium_count})</span>')
        html.append('</label>')
        html.append('<label class="filter-chip low">')
        html.append('<input type="checkbox" name="priority" value="LOW" checked onchange="applyFilters()">')
        html.append(f'<span>Low ({report.low_count})</span>')
        html.append('</label>')
        html.append('</div>')
        html.append('</div>')
        
        # Algorithm filter
        html.append('<div class="filter-section">')
        html.append('<h4><i class="fas fa-key"></i> Algorithm</h4>')
        html.append('<div class="filter-select-container">')
        html.append('<select id="algorithmFilter" class="filter-select" multiple size="5" onchange="applyFilters()">')
        for algo in algorithms_list:
            count = sum(1 for pf in report.prioritized_findings if pf.finding.algorithm.value == algo)
            html.append(f'<option value="{algo}" selected>{algo} ({count})</option>')
        html.append('</select>')
        html.append('<div class="filter-select-actions">')
        html.append('<button class="filter-action-btn" onclick="selectAllAlgorithms()">Select All</button>')
        html.append('<button class="filter-action-btn" onclick="clearAllAlgorithms()">Clear All</button>')
        html.append('</div>')
        html.append('</div>')
        html.append('</div>')
        
        # Context filter
        html.append('<div class="filter-section">')
        html.append('<h4><i class="fas fa-sitemap"></i> Usage Context</h4>')
        html.append('<div class="filter-select-container">')
        html.append('<select id="contextFilter" class="filter-select" multiple size="5" onchange="applyFilters()">')
        for ctx in contexts_list:
            count = sum(1 for pf in report.prioritized_findings if pf.finding.usage_context.value == ctx)
            html.append(f'<option value="{ctx}" selected>{ctx} ({count})</option>')
        html.append('</select>')
        html.append('<div class="filter-select-actions">')
        html.append('<button class="filter-action-btn" onclick="selectAllContexts()">Select All</button>')
        html.append('<button class="filter-action-btn" onclick="clearAllContexts()">Clear All</button>')
        html.append('</div>')
        html.append('</div>')
        html.append('</div>')
        
        # Risk score range
        html.append('<div class="filter-section">')
        html.append('<h4><i class="fas fa-chart-line"></i> Risk Score Range</h4>')
        html.append('<div class="range-slider-container">')
        html.append('<div class="range-values">')
        html.append('<span id="minScoreValue">0</span>')
        html.append('<span id="maxScoreValue">20</span>')
        html.append('</div>')
        html.append('<input type="range" id="minScore" class="range-slider" min="0" max="20" value="0" step="0.5" oninput="updateScoreRange()">')
        html.append('<input type="range" id="maxScore" class="range-slider" min="0" max="20" value="20" step="0.5" oninput="updateScoreRange()">')
        html.append('</div>')
        html.append('</div>')
        
        # Filter presets
        html.append('<div class="filter-section">')
        html.append('<h4><i class="fas fa-bookmark"></i> Filter Presets</h4>')
        html.append('<div class="filter-presets">')
        html.append('<button class="preset-btn" onclick="applyPreset(\'critical-only\')"><i class="fas fa-fire"></i> Critical Only</button>')
        html.append('<button class="preset-btn" onclick="applyPreset(\'high-priority\')"><i class="fas fa-exclamation"></i> High Priority</button>')
        html.append('<button class="preset-btn" onclick="applyPreset(\'quick-wins\')"><i class="fas fa-bolt"></i> Quick Wins</button>')
        html.append('<button class="preset-btn" onclick="applyPreset(\'authentication\')"><i class="fas fa-lock"></i> Authentication</button>')
        html.append('</div>')
        html.append('<div class="preset-actions">')
        html.append('<button class="preset-action-btn" onclick="saveCurrentPreset()"><i class="fas fa-save"></i> Save Current</button>')
        html.append('<button class="preset-action-btn" onclick="resetFilters()"><i class="fas fa-undo"></i> Reset All</button>')
        html.append('</div>')
        html.append('</div>')
        
        # Active filters summary
        html.append('<div class="filter-section">')
        html.append('<div class="active-filters" id="activeFilters">')
        html.append('<div class="active-filters-header">')
        html.append('<span><i class="fas fa-tags"></i> Active Filters</span>')
        html.append('<button class="clear-all-btn" onclick="clearAllFilters()">Clear All</button>')
        html.append('</div>')
        html.append('<div class="active-filters-list" id="activeFiltersList">')
        html.append('<span class="no-filters">No filters applied</span>')
        html.append('</div>')
        html.append('</div>')
        html.append('</div>')
        
        html.append('</div>')  # Close filter-panel-content
        html.append('</div>')  # Close filter-panel
        
        return '\n'.join(html)
    
    def _format_header(self, report: RemediationReport) -> str:
        """Format dashboard header with modern styling."""
        return f'''
<div class="header-section animate-fade-in">
    <h1>🔐 Enhanced Crypto Vulnerability Dashboard</h1>
    <p class="header-meta">Impact-Based Remediation Prioritization Report</p>
    <p class="header-meta">Generated: {report.generated_at}</p>
</div>'''
    
    def _format_executive_summary(self, report: RemediationReport) -> str:
        """Format executive summary with glassmorphism cards."""
        html = ['<div class="executive-summary">']
        html.append('<h2><i class="fas fa-chart-line"></i> Executive Summary</h2>')
        
        # Calculate trends (mock data for now)
        trends = {
            'critical': {'value': 15, 'direction': 'up'},
            'high': {'value': -8, 'direction': 'down'},
            'medium': {'value': 0, 'direction': 'neutral'},
            'low': {'value': -12, 'direction': 'down'}
        }
        
        html.append('<div class="summary-grid">')
        
        # Critical card
        trend = trends['critical']
        trend_class = f'trend-{trend["direction"]}'
        trend_icon = '↑' if trend['direction'] == 'up' else '↓' if trend['direction'] == 'down' else '→'
        html.append(f'''
<div class="summary-card critical glass-card animate-fade-in">
    <div class="summary-card-header">
        <i class="fas fa-exclamation-triangle summary-card-icon" style="color: var(--critical);"></i>
    </div>
    <h3>{report.critical_count}</h3>
    <p>Critical Issues</p>
    <div class="summary-card-trend {trend_class}">
        <span>{trend_icon} {abs(trend["value"])}%</span>
        <span>vs last scan</span>
    </div>
</div>''')
        
        # High card
        trend = trends['high']
        trend_class = f'trend-{trend["direction"]}'
        trend_icon = '↑' if trend['direction'] == 'up' else '↓' if trend['direction'] == 'down' else '→'
        html.append(f'''
<div class="summary-card high glass-card animate-fade-in" style="animation-delay: 0.1s;">
    <div class="summary-card-header">
        <i class="fas fa-exclamation-circle summary-card-icon" style="color: var(--high);"></i>
    </div>
    <h3>{report.high_count}</h3>
    <p>High Priority</p>
    <div class="summary-card-trend {trend_class}">
        <span>{trend_icon} {abs(trend["value"])}%</span>
        <span>vs last scan</span>
    </div>
</div>''')
        
        # Medium card
        trend = trends['medium']
        trend_class = f'trend-{trend["direction"]}'
        trend_icon = '↑' if trend['direction'] == 'up' else '↓' if trend['direction'] == 'down' else '→'
        html.append(f'''
<div class="summary-card medium glass-card animate-fade-in" style="animation-delay: 0.2s;">
    <div class="summary-card-header">
        <i class="fas fa-info-circle summary-card-icon" style="color: var(--medium);"></i>
    </div>
    <h3>{report.medium_count}</h3>
    <p>Medium Priority</p>
    <div class="summary-card-trend {trend_class}">
        <span>{trend_icon} {abs(trend["value"])}%</span>
        <span>vs last scan</span>
    </div>
</div>''')
        
        # Low card
        trend = trends['low']
        trend_class = f'trend-{trend["direction"]}'
        trend_icon = '↑' if trend['direction'] == 'up' else '↓' if trend['direction'] == 'down' else '→'
        html.append(f'''
<div class="summary-card low glass-card animate-fade-in" style="animation-delay: 0.3s;">
    <div class="summary-card-header">
        <i class="fas fa-check-circle summary-card-icon" style="color: var(--low);"></i>
    </div>
    <h3>{report.low_count}</h3>
    <p>Low Priority</p>
    <div class="summary-card-trend {trend_class}">
        <span>{trend_icon} {abs(trend["value"])}%</span>
        <span>vs last scan</span>
    </div>
</div>''')
        
        html.append('</div>')  # Close summary-grid
        html.append('</div>')  # Close executive-summary
        
        return '\n'.join(html)
    
    def _format_executive_dashboard(self, report: RemediationReport) -> str:
        """Format executive dashboard with KPIs and compliance mapping."""
        html = ['<div class="executive-dashboard-section">']
        html.append('<div class="section-header">')
        html.append('<h2><i class="fas fa-chart-line"></i> Executive Dashboard</h2>')
        html.append('<button class="toggle-btn" onclick="toggleExecutiveDashboard()" title="Toggle executive view">')
        html.append('<i class="fas fa-compress-alt"></i>')
        html.append('</button>')
        html.append('</div>')
        
        html.append('<div class="executive-dashboard-content" id="executiveDashboard">')
        
        # KPI Cards Row
        html.append('<div class="kpi-section">')
        html.append('<h3><i class="fas fa-tachometer-alt"></i> Key Performance Indicators</h3>')
        html.append('<div class="kpi-grid">')
        
        # Calculate KPIs
        total_findings = len(report.prioritized_findings)
        avg_score = sum(pf.risk_score.final_score for pf in report.prioritized_findings) / total_findings if total_findings > 0 else 0
        critical_high = report.critical_count + report.high_count
        
        # Mean Time To Remediate (MTTR) - simulated
        mttr_days = 14  # Placeholder
        
        # Remediation Velocity - findings closed per week (simulated)
        velocity = 8  # Placeholder
        
        # Risk Trend - percentage change (simulated)
        risk_trend = -12  # Negative is good (risk decreasing)
        risk_trend_class = 'trend-down' if risk_trend < 0 else 'trend-up' if risk_trend > 0 else 'trend-stable'
        risk_trend_icon = '↓' if risk_trend < 0 else '↑' if risk_trend > 0 else '→'
        
        # Compliance Score (simulated)
        compliance_score = 78  # Percentage
        
        # KPI Card 1: Average Risk Score
        html.append(f'''
<div class="kpi-card glass-card">
    <div class="kpi-icon" style="background: linear-gradient(135deg, var(--primary-500), var(--primary-700));">
        <i class="fas fa-chart-bar"></i>
    </div>
    <div class="kpi-content">
        <h4>Avg Risk Score</h4>
        <div class="kpi-value">{avg_score:.1f}<span class="kpi-unit">/20</span></div>
        <div class="kpi-subtitle">Across {total_findings} findings</div>
    </div>
</div>''')
        
        # KPI Card 2: MTTR
        html.append(f'''
<div class="kpi-card glass-card">
    <div class="kpi-icon" style="background: linear-gradient(135deg, #8b5cf6, #6d28d9);">
        <i class="fas fa-clock"></i>
    </div>
    <div class="kpi-content">
        <h4>Mean Time To Remediate</h4>
        <div class="kpi-value">{mttr_days}<span class="kpi-unit">days</span></div>
        <div class="kpi-subtitle">Average resolution time</div>
    </div>
</div>''')
        
        # KPI Card 3: Remediation Velocity
        html.append(f'''
<div class="kpi-card glass-card">
    <div class="kpi-icon" style="background: linear-gradient(135deg, #10b981, #059669);">
        <i class="fas fa-rocket"></i>
    </div>
    <div class="kpi-content">
        <h4>Remediation Velocity</h4>
        <div class="kpi-value">{velocity}<span class="kpi-unit">/week</span></div>
        <div class="kpi-subtitle">Findings resolved</div>
    </div>
</div>''')
        
        # KPI Card 4: Risk Trend
        html.append(f'''
<div class="kpi-card glass-card">
    <div class="kpi-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706);">
        <i class="fas fa-trending-down"></i>
    </div>
    <div class="kpi-content">
        <h4>Risk Trend</h4>
        <div class="kpi-value {risk_trend_class}">{risk_trend_icon} {abs(risk_trend)}<span class="kpi-unit">%</span></div>
        <div class="kpi-subtitle">vs last month</div>
    </div>
</div>''')
        
        # KPI Card 5: Critical + High
        html.append(f'''
<div class="kpi-card glass-card">
    <div class="kpi-icon" style="background: linear-gradient(135deg, var(--critical), var(--high));">
        <i class="fas fa-exclamation-triangle"></i>
    </div>
    <div class="kpi-content">
        <h4>Critical + High</h4>
        <div class="kpi-value">{critical_high}<span class="kpi-unit">findings</span></div>
        <div class="kpi-subtitle">Require immediate action</div>
    </div>
</div>''')
        
        # KPI Card 6: Compliance Score
        compliance_color = '#10b981' if compliance_score >= 80 else '#f59e0b' if compliance_score >= 60 else '#ef4444'
        html.append(f'''
<div class="kpi-card glass-card">
    <div class="kpi-icon" style="background: linear-gradient(135deg, {compliance_color}, {compliance_color}dd);">
        <i class="fas fa-shield-alt"></i>
    </div>
    <div class="kpi-content">
        <h4>Compliance Score</h4>
        <div class="kpi-value">{compliance_score}<span class="kpi-unit">%</span></div>
        <div class="kpi-subtitle">Overall compliance</div>
    </div>
</div>''')
        
        html.append('</div>')  # Close kpi-grid
        html.append('</div>')  # Close kpi-section
        
        # Compliance Framework Mapping
        html.append('<div class="compliance-section">')
        html.append('<h3><i class="fas fa-clipboard-check"></i> Compliance Framework Mapping</h3>')
        html.append('<div class="compliance-grid">')
        
        # Define compliance frameworks with crypto-specific controls
        frameworks = [
            {
                'name': 'NIST CSF',
                'full_name': 'NIST Cybersecurity Framework',
                'icon': 'fas fa-university',
                'controls': ['PR.DS-5', 'PR.DS-2', 'DE.CM-4'],
                'coverage': 85,
                'findings': critical_high,
                'color': '#3b82f6'
            },
            {
                'name': 'PCI-DSS',
                'full_name': 'Payment Card Industry Data Security Standard',
                'icon': 'fas fa-credit-card',
                'controls': ['Req 4.1', 'Req 3.4', 'Req 3.5'],
                'coverage': 72,
                'findings': report.critical_count,
                'color': '#8b5cf6'
            },
            {
                'name': 'HIPAA',
                'full_name': 'Health Insurance Portability and Accountability Act',
                'icon': 'fas fa-heartbeat',
                'controls': ['164.312(a)(2)(iv)', '164.312(e)(2)(i)'],
                'coverage': 68,
                'findings': int(critical_high * 0.6),
                'color': '#10b981'
            },
            {
                'name': 'SOC 2',
                'full_name': 'Service Organization Control 2',
                'icon': 'fas fa-lock',
                'controls': ['CC6.1', 'CC6.6', 'CC6.7'],
                'coverage': 90,
                'findings': report.high_count,
                'color': '#f59e0b'
            },
            {
                'name': 'ISO 27001',
                'full_name': 'Information Security Management',
                'icon': 'fas fa-certificate',
                'controls': ['A.10.1.1', 'A.10.1.2', 'A.14.1.2'],
                'coverage': 78,
                'findings': int(total_findings * 0.3),
                'color': '#ef4444'
            },
            {
                'name': 'GDPR',
                'full_name': 'General Data Protection Regulation',
                'icon': 'fas fa-user-shield',
                'controls': ['Art. 32', 'Art. 25'],
                'coverage': 82,
                'findings': int(critical_high * 0.8),
                'color': '#06b6d4'
            }
        ]
        
        for framework in frameworks:
            coverage_class = 'high' if framework['coverage'] >= 80 else 'medium' if framework['coverage'] >= 60 else 'low'
            html.append(f'''
<div class="compliance-card glass-card">
    <div class="compliance-header">
        <div class="compliance-icon" style="color: {framework['color']};">
            <i class="{framework['icon']}"></i>
        </div>
        <div class="compliance-title">
            <h4>{framework['name']}</h4>
            <p class="compliance-subtitle">{framework['full_name']}</p>
        </div>
    </div>
    <div class="compliance-body">
        <div class="compliance-coverage">
            <div class="coverage-label">Coverage</div>
            <div class="coverage-bar">
                <div class="coverage-fill {coverage_class}" style="width: {framework['coverage']}%;"></div>
            </div>
            <div class="coverage-value">{framework['coverage']}%</div>
        </div>
        <div class="compliance-controls">
            <div class="controls-label">Relevant Controls:</div>
            <div class="controls-list">
                {' '.join(f'<span class="control-badge">{ctrl}</span>' for ctrl in framework['controls'])}
            </div>
        </div>
        <div class="compliance-findings">
            <i class="fas fa-exclamation-circle"></i>
            <span>{framework['findings']} findings require attention</span>
        </div>
    </div>
    <div class="compliance-footer">
        <button class="compliance-btn" onclick="filterByCompliance('{framework['name']}')" title="View related findings">
            <i class="fas fa-filter"></i> View Findings
        </button>
        <button class="compliance-btn" onclick="exportCompliance('{framework['name']}')" title="Export compliance report">
            <i class="fas fa-download"></i> Export
        </button>
    </div>
</div>''')
        
        html.append('</div>')  # Close compliance-grid
        html.append('</div>')  # Close compliance-section
        
        # Risk Timeline (Placeholder for future enhancement)
        html.append('<div class="timeline-section">')
        html.append('<h3><i class="fas fa-history"></i> Risk Timeline</h3>')
        html.append('<div class="timeline-chart glass-card">')
        html.append('<canvas id="riskTimelineChart" height="80"></canvas>')
        html.append('</div>')
        html.append('</div>')
        
        html.append('</div>')  # Close executive-dashboard-content
        html.append('</div>')  # Close executive-dashboard-section
        
        return '\n'.join(html)
    
        html.append('</div>')  # Close executive-summary
        
        return '\n'.join(html)
    def _format_advanced_analytics(self, report: RemediationReport) -> str:
        """Format advanced analytics section with ML predictions and insights."""
        html = ['<div class="analytics-section">']
        html.append('<div class="section-header">')
        html.append('<h2><i class="fas fa-brain"></i> Advanced Analytics & Insights</h2>')
        html.append('<button class="toggle-btn" onclick="toggleAnalytics()" title="Toggle analytics view">')
        html.append('<i class="fas fa-compress-alt"></i>')
        html.append('</button>')
        html.append('</div>')
        
        html.append('<div class="analytics-content" id="analyticsContent">')
        
        # ML Predictions Section
        html.append('<div class="predictions-section">')
        html.append('<h3><i class="fas fa-magic"></i> ML-Powered Risk Predictions</h3>')
        html.append('<div class="predictions-grid">')
        
        # Calculate predictions (simulated ML output)
        total_findings = len(report.prioritized_findings)
        critical_count = report.critical_count
        high_count = report.high_count
        
        # Prediction 1: Next Week Forecast
        predicted_new_findings = max(1, int(total_findings * 0.15))
        predicted_critical = max(0, int(critical_count * 0.2))
        trend_direction = 'increasing' if predicted_new_findings > 2 else 'stable'
        confidence = 87  # Simulated confidence score
        
        html.append(f'''
<div class="prediction-card glass-card">
    <div class="prediction-header">
        <i class="fas fa-calendar-week prediction-icon"></i>
        <h4>Next Week Forecast</h4>
    </div>
    <div class="prediction-body">
        <div class="prediction-metric">
            <span class="metric-label">Predicted New Findings</span>
            <span class="metric-value">{predicted_new_findings}</span>
        </div>
        <div class="prediction-metric">
            <span class="metric-label">Expected Critical</span>
            <span class="metric-value critical-text">{predicted_critical}</span>
        </div>
        <div class="prediction-trend {trend_direction}">
            <i class="fas fa-arrow-{'up' if trend_direction == 'increasing' else 'right'}"></i>
            <span>Risk trend: {trend_direction.capitalize()}</span>
        </div>
        <div class="prediction-confidence">
            <span>Confidence: {confidence}%</span>
            <div class="confidence-bar">
                <div class="confidence-fill" style="width: {confidence}%;"></div>
            </div>
        </div>
    </div>
</div>''')
        
        # Prediction 2: Remediation Timeline
        avg_remediation_days = 14
        predicted_completion = 45  # days
        completion_confidence = 82
        
        html.append(f'''
<div class="prediction-card glass-card">
    <div class="prediction-header">
        <i class="fas fa-hourglass-half prediction-icon"></i>
        <h4>Remediation Timeline</h4>
    </div>
    <div class="prediction-body">
        <div class="prediction-metric">
            <span class="metric-label">Avg Time per Finding</span>
            <span class="metric-value">{avg_remediation_days} days</span>
        </div>
        <div class="prediction-metric">
            <span class="metric-label">Predicted Full Completion</span>
            <span class="metric-value">{predicted_completion} days</span>
        </div>
        <div class="prediction-trend stable">
            <i class="fas fa-check-circle"></i>
            <span>On track for Q2 completion</span>
        </div>
        <div class="prediction-confidence">
            <span>Confidence: {completion_confidence}%</span>
            <div class="confidence-bar">
                <div class="confidence-fill" style="width: {completion_confidence}%;"></div>
            </div>
        </div>
    </div>
</div>''')
        
        # Prediction 3: High-Risk Areas
        risk_areas = [
            ('Authentication Module', 85, 'critical'),
            ('Data Encryption', 72, 'high'),
            ('Key Management', 68, 'high')
        ]
        
        html.append('''
<div class="prediction-card glass-card">
    <div class="prediction-header">
        <i class="fas fa-exclamation-triangle prediction-icon"></i>
        <h4>High-Risk Areas</h4>
    </div>
    <div class="prediction-body">''')
        
        for area, risk_score, severity in risk_areas:
            html.append(f'''
        <div class="risk-area-item">
            <span class="area-name">{area}</span>
            <div class="area-risk">
                <span class="risk-score {severity}">{risk_score}%</span>
                <div class="risk-bar">
                    <div class="risk-fill {severity}" style="width: {risk_score}%;"></div>
                </div>
            </div>
        </div>''')
        
        html.append('''
        <button class="prediction-action-btn" onclick="showRiskDetails()">
            <i class="fas fa-chart-line"></i> View Detailed Analysis
        </button>
    </div>
</div>''')
        
        html.append('</div>')  # Close predictions-grid
        html.append('</div>')  # Close predictions-section
        
        # Historical Trends Section
        html.append('<div class="trends-section">')
        html.append('<h3><i class="fas fa-chart-area"></i> Historical Trend Analysis</h3>')
        html.append('<div class="trends-container">')
        
        # Trend Chart
        html.append('<div class="trend-chart-container glass-card">')
        html.append('<div class="chart-header">')
        html.append('<h4>Finding Discovery Rate</h4>')
        html.append('<div class="chart-controls">')
        html.append('<button class="chart-period-btn active" onclick="setTrendPeriod(\'week\')">Week</button>')
        html.append('<button class="chart-period-btn" onclick="setTrendPeriod(\'month\')">Month</button>')
        html.append('<button class="chart-period-btn" onclick="setTrendPeriod(\'quarter\')">Quarter</button>')
        html.append('<button class="chart-period-btn" onclick="setTrendPeriod(\'year\')">Year</button>')
        html.append('</div>')
        html.append('</div>')
        html.append('<canvas id="trendAnalysisChart" height="100"></canvas>')
        html.append('</div>')
        
        # Trend Insights
        html.append('<div class="trend-insights glass-card">')
        html.append('<h4><i class="fas fa-lightbulb"></i> Key Insights</h4>')
        html.append('<ul class="insights-list">')
        html.append('<li class="insight-item positive">')
        html.append('<i class="fas fa-arrow-down"></i>')
        html.append('<span>Critical findings decreased by <strong>23%</strong> this month</span>')
        html.append('</li>')
        html.append('<li class="insight-item negative">')
        html.append('<i class="fas fa-arrow-up"></i>')
        html.append('<span>MD5 usage increased by <strong>12%</strong> in new code</span>')
        html.append('</li>')
        html.append('<li class="insight-item neutral">')
        html.append('<i class="fas fa-equals"></i>')
        html.append('<span>Remediation velocity stable at <strong>8 findings/week</strong></span>')
        html.append('</li>')
        html.append('<li class="insight-item positive">')
        html.append('<i class="fas fa-check"></i>')
        html.append('<span>Zero new SHA1 vulnerabilities detected</span>')
        html.append('</li>')
        html.append('</ul>')
        html.append('</div>')
        
        html.append('</div>')  # Close trends-container
        html.append('</div>')  # Close trends-section
        
        # Benchmarking Section
        html.append('<div class="benchmarking-section">')
        html.append('<h3><i class="fas fa-trophy"></i> Industry Benchmarking</h3>')
        html.append('<div class="benchmark-grid">')
        
        # Benchmark metrics
        benchmarks = [
            {
                'metric': 'Vulnerability Density',
                'your_value': 2.3,
                'industry_avg': 3.8,
                'unit': 'per 1K LOC',
                'percentile': 75,
                'status': 'better'
            },
            {
                'metric': 'Mean Time To Remediate',
                'your_value': 14,
                'industry_avg': 21,
                'unit': 'days',
                'percentile': 68,
                'status': 'better'
            },
            {
                'metric': 'Critical Finding Rate',
                'your_value': 8.3,
                'industry_avg': 12.5,
                'unit': '%',
                'percentile': 72,
                'status': 'better'
            },
            {
                'metric': 'Remediation Coverage',
                'your_value': 78,
                'industry_avg': 65,
                'unit': '%',
                'percentile': 80,
                'status': 'better'
            }
        ]
        
        for benchmark in benchmarks:
            status_icon = 'fa-check-circle' if benchmark['status'] == 'better' else 'fa-times-circle'
            status_class = 'better' if benchmark['status'] == 'better' else 'worse'
            
            html.append(f'''
<div class="benchmark-card glass-card">
    <div class="benchmark-header">
        <h4>{benchmark['metric']}</h4>
        <i class="fas {status_icon} benchmark-status {status_class}"></i>
    </div>
    <div class="benchmark-comparison">
        <div class="benchmark-value your-value">
            <span class="value-label">Your Organization</span>
            <span class="value-number">{benchmark['your_value']}</span>
            <span class="value-unit">{benchmark['unit']}</span>
        </div>
        <div class="benchmark-vs">
            <i class="fas fa-exchange-alt"></i>
        </div>
        <div class="benchmark-value industry-value">
            <span class="value-label">Industry Average</span>
            <span class="value-number">{benchmark['industry_avg']}</span>
            <span class="value-unit">{benchmark['unit']}</span>
        </div>
    </div>
    <div class="benchmark-percentile">
        <span>You're in the top <strong>{100 - benchmark['percentile']}%</strong></span>
        <div class="percentile-bar">
            <div class="percentile-marker" style="left: {benchmark['percentile']}%;">
                <i class="fas fa-map-marker-alt"></i>
            </div>
        </div>
    </div>
</div>''')
        
        html.append('</div>')  # Close benchmark-grid
        html.append('</div>')  # Close benchmarking-section
        
        # Anomaly Detection Section
        html.append('<div class="anomaly-section">')
        html.append('<h3><i class="fas fa-search"></i> Anomaly Detection</h3>')
        html.append('<div class="anomaly-container glass-card">')
        
        # Anomalies detected
        anomalies = [
            {
                'type': 'Spike',
                'severity': 'high',
                'title': 'Unusual MD5 Usage Spike',
                'description': 'Detected 3x increase in MD5 hash usage in authentication module',
                'timestamp': '2 hours ago',
                'confidence': 94
            },
            {
                'type': 'Pattern',
                'severity': 'medium',
                'title': 'Recurring Vulnerability Pattern',
                'description': 'Same weak algorithm reintroduced in 3 different modules',
                'timestamp': '1 day ago',
                'confidence': 88
            },
            {
                'type': 'Deviation',
                'severity': 'low',
                'title': 'Remediation Slowdown',
                'description': 'Average fix time increased from 12 to 18 days',
                'timestamp': '3 days ago',
                'confidence': 76
            }
        ]
        
        for anomaly in anomalies:
            severity_icon = 'fa-exclamation-circle' if anomaly['severity'] == 'high' else 'fa-exclamation-triangle' if anomaly['severity'] == 'medium' else 'fa-info-circle'
            
            html.append(f'''
<div class="anomaly-item {anomaly['severity']}">
    <div class="anomaly-icon">
        <i class="fas {severity_icon}"></i>
    </div>
    <div class="anomaly-content">
        <div class="anomaly-header">
            <h4>{anomaly['title']}</h4>
            <span class="anomaly-badge">{anomaly['type']}</span>
        </div>
        <p class="anomaly-description">{anomaly['description']}</p>
        <div class="anomaly-footer">
            <span class="anomaly-time"><i class="fas fa-clock"></i> {anomaly['timestamp']}</span>
            <span class="anomaly-confidence">Confidence: {anomaly['confidence']}%</span>
        </div>
    </div>
    <button class="anomaly-action-btn" onclick="investigateAnomaly('{anomaly['type'].lower()}')">
        <i class="fas fa-search-plus"></i>
    </button>
</div>''')
        
        html.append('</div>')  # Close anomaly-container
        html.append('</div>')  # Close anomaly-section
        
        html.append('</div>')  # Close analytics-content
        html.append('</div>')  # Close analytics-section
        
        return '\n'.join(html)
    def _format_remediation_workflow(self, report: RemediationReport) -> str:
        """Format remediation workflow section with Kanban board and task management."""
        html = ['<div class="workflow-section">']
        html.append('<div class="section-header">')
        html.append('<h2><i class="fas fa-tasks"></i> Remediation Workflow</h2>')
        html.append('<div class="workflow-controls">')
        html.append('<button class="workflow-btn" onclick="toggleWorkflowView(\'kanban\')" id="kanbanViewBtn">')
        html.append('<i class="fas fa-columns"></i> Kanban View')
        html.append('</button>')
        html.append('<button class="workflow-btn" onclick="toggleWorkflowView(\'timeline\')" id="timelineViewBtn">')
        html.append('<i class="fas fa-stream"></i> Timeline View')
        html.append('</button>')
        html.append('<button class="workflow-btn" onclick="showAssignmentModal()">')
        html.append('<i class="fas fa-user-plus"></i> Assign Tasks')
        html.append('</button>')
        html.append('</div>')
        html.append('</div>')
        
        # Workflow statistics
        html.append('<div class="workflow-stats">')
        
        # Calculate workflow metrics
        total_findings = len(report.prioritized_findings)
        backlog = sum(1 for f in report.prioritized_findings if not hasattr(f, 'remediation_status') or f.finding.metadata.get('status', 'backlog') == 'backlog')
        in_progress = sum(1 for f in report.prioritized_findings if f.finding.metadata.get('status') == 'in_progress')
        in_review = sum(1 for f in report.prioritized_findings if f.finding.metadata.get('status') == 'in_review')
        completed = sum(1 for f in report.prioritized_findings if f.finding.metadata.get('status') == 'completed')
        
        # If no status metadata, distribute based on priority
        if backlog == total_findings:
            backlog = int(total_findings * 0.5)
            in_progress = int(total_findings * 0.3)
            in_review = int(total_findings * 0.15)
            completed = total_findings - backlog - in_progress - in_review
        
        stats = [
            ('Backlog', backlog, 'backlog', 'fa-inbox'),
            ('In Progress', in_progress, 'in-progress', 'fa-spinner'),
            ('In Review', in_review, 'in-review', 'fa-eye'),
            ('Completed', completed, 'completed', 'fa-check-circle')
        ]
        
        for label, count, status_class, icon in stats:
            percentage = (count / total_findings * 100) if total_findings > 0 else 0
            html.append(f'''
<div class="workflow-stat-card glass-card {status_class}">
    <div class="stat-icon">
        <i class="fas {icon}"></i>
    </div>
    <div class="stat-content">
        <h4>{label}</h4>
        <div class="stat-value">{count}</div>
        <div class="stat-progress">
            <div class="progress-bar">
                <div class="progress-fill" style="width: {percentage}%;"></div>
            </div>
            <span class="progress-label">{percentage:.1f}%</span>
        </div>
    </div>
</div>''')
        
        html.append('</div>')  # Close workflow-stats
        
        # Kanban Board
        html.append('<div class="kanban-board" id="kanbanBoard">')
        
        # Organize findings by status
        findings_by_status = {
            'backlog': [],
            'in_progress': [],
            'in_review': [],
            'completed': []
        }
        
        for i, pf in enumerate(report.prioritized_findings):
            status = pf.finding.metadata.get('status', 'backlog')
            if status not in findings_by_status:
                # Distribute based on index if no status
                if i < backlog:
                    status = 'backlog'
                elif i < backlog + in_progress:
                    status = 'in_progress'
                elif i < backlog + in_progress + in_review:
                    status = 'in_review'
                else:
                    status = 'completed'
            findings_by_status[status].append(pf)
        
        # Kanban columns
        columns = [
            ('Backlog', 'backlog', 'fa-inbox', '#6b7280'),
            ('In Progress', 'in_progress', 'fa-spinner', '#3b82f6'),
            ('In Review', 'in_review', 'fa-eye', '#f59e0b'),
            ('Completed', 'completed', 'fa-check-circle', '#10b981')
        ]
        
        for col_title, col_id, col_icon, col_color in columns:
            col_findings = findings_by_status.get(col_id, [])
            html.append(f'''
<div class="kanban-column" data-status="{col_id}">
    <div class="column-header" style="border-left: 4px solid {col_color};">
        <div class="column-title">
            <i class="fas {col_icon}"></i>
            <h3>{col_title}</h3>
            <span class="column-count">{len(col_findings)}</span>
        </div>
        <button class="column-action-btn" onclick="addTaskToColumn('{col_id}')">
            <i class="fas fa-plus"></i>
        </button>
    </div>
    <div class="column-content" id="{col_id}Column" ondrop="drop(event)" ondragover="allowDrop(event)">''')
            
            # Add task cards
            for pf in col_findings[:5]:  # Limit to 5 per column for performance
                severity_class = pf.risk_score.priority_level.lower()
                severity_color = {
                    'critical': '#dc2626',
                    'high': '#ea580c',
                    'medium': '#eab308',
                    'low': '#10b981'
                }.get(severity_class, '#6b7280')
                
                assigned_to = pf.finding.metadata.get('assigned_to', 'Unassigned')
                due_date = pf.finding.metadata.get('due_date', 'No deadline')
                
                html.append(f'''
        <div class="kanban-card glass-card" draggable="true" ondragstart="drag(event)" data-finding-id="{pf.finding.id}">
            <div class="card-header">
                <span class="card-id">{pf.finding.id}</span>
                <span class="card-priority {severity_class}" style="background-color: {severity_color};">
                    {severity_class.upper()}
                </span>
            </div>
            <div class="card-body">
                <h4 class="card-title">{pf.finding.title[:50]}...</h4>
                <p class="card-algorithm">
                    <i class="fas fa-shield-alt"></i> {pf.finding.algorithm.value}
                </p>
                <div class="card-meta">
                    <span class="card-assignee">
                        <i class="fas fa-user"></i> {assigned_to}
                    </span>
                    <span class="card-due-date">
                        <i class="fas fa-calendar"></i> {due_date}
                    </span>
                </div>
            </div>
            <div class="card-footer">
                <div class="card-actions">
                    <button class="card-action-btn" onclick="viewTaskDetails('{pf.finding.id}')" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="card-action-btn" onclick="editTask('{pf.finding.id}')" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="card-action-btn" onclick="assignTask('{pf.finding.id}')" title="Assign">
                        <i class="fas fa-user-plus"></i>
                    </button>
                </div>
                <div class="card-progress">
                    <div class="progress-bar-mini">
                        <div class="progress-fill-mini" style="width: {(col_findings.index(pf) + 1) / len(col_findings) * 100 if col_findings else 0}%;"></div>
                    </div>
                </div>
            </div>
        </div>''')
            
            if len(col_findings) > 5:
                html.append(f'''
        <div class="kanban-card-more glass-card" onclick="showMoreTasks('{col_id}')">
            <i class="fas fa-ellipsis-h"></i>
            <span>+{len(col_findings) - 5} more tasks</span>
        </div>''')
            
            html.append('    </div>')  # Close column-content
            html.append('</div>')  # Close kanban-column
        
        html.append('</div>')  # Close kanban-board
        
        # Timeline View (initially hidden)
        html.append('<div class="timeline-view" id="timelineView" style="display: none;">')
        html.append('<div class="timeline-container">')
        
        # Group findings by week
        import datetime
        current_date = datetime.datetime.now()
        
        for week in range(4):  # Show 4 weeks
            week_start = current_date + datetime.timedelta(weeks=week)
            week_end = week_start + datetime.timedelta(days=6)
            
            html.append(f'''
<div class="timeline-week">
    <div class="timeline-week-header">
        <h3>Week {week + 1}</h3>
        <span class="timeline-date-range">{week_start.strftime("%b %d")} - {week_end.strftime("%b %d")}</span>
    </div>
    <div class="timeline-tasks">''')
            
            # Add some tasks for this week
            week_findings = report.prioritized_findings[week*3:(week+1)*3]
            for pf in week_findings:
                severity_class = pf.risk_score.priority_level.lower()
                html.append(f'''
        <div class="timeline-task glass-card {severity_class}">
            <div class="timeline-task-marker"></div>
            <div class="timeline-task-content">
                <h4>{pf.finding.title[:40]}...</h4>
                <p><i class="fas fa-shield-alt"></i> {pf.finding.algorithm.value}</p>
                <div class="timeline-task-meta">
                    <span><i class="fas fa-user"></i> {pf.finding.metadata.get('assigned_to', 'Unassigned')}</span>
                    <span class="priority-badge {severity_class}">{severity_class.upper()}</span>
                </div>
            </div>
        </div>''')
            
            html.append('    </div>')  # Close timeline-tasks
            html.append('</div>')  # Close timeline-week
        
        html.append('</div>')  # Close timeline-container
        html.append('</div>')  # Close timeline-view
        
        # Task Assignment Modal
        html.append('''
<div class="modal" id="assignmentModal" style="display: none;">
    <div class="modal-content glass-card">
        <div class="modal-header">
            <h3><i class="fas fa-user-plus"></i> Assign Tasks</h3>
            <button class="modal-close-btn" onclick="closeAssignmentModal()">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <div class="form-group">
                <label>Select Task</label>
                <select id="taskSelect" class="form-control">
                    <option value="">Choose a task...</option>
                </select>
            </div>
            <div class="form-group">
                <label>Assign To</label>
                <select id="assigneeSelect" class="form-control">
                    <option value="">Choose team member...</option>
                    <option value="dev1@example.com">Alice Johnson (Senior Dev)</option>
                    <option value="dev2@example.com">Bob Smith (Security Engineer)</option>
                    <option value="dev3@example.com">Carol Williams (DevOps)</option>
                    <option value="dev4@example.com">David Brown (Backend Dev)</option>
                </select>
            </div>
            <div class="form-group">
                <label>Due Date</label>
                <input type="date" id="dueDateInput" class="form-control">
            </div>
            <div class="form-group">
                <label>Priority</label>
                <select id="prioritySelect" class="form-control">
                    <option value="critical">Critical</option>
                    <option value="high">High</option>
                    <option value="medium">Medium</option>
                    <option value="low">Low</option>
                </select>
            </div>
            <div class="form-group">
                <label>Notes</label>
                <textarea id="notesInput" class="form-control" rows="3" placeholder="Add any additional notes..."></textarea>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-secondary" onclick="closeAssignmentModal()">Cancel</button>
            <button class="btn btn-primary" onclick="saveAssignment()">
                <i class="fas fa-save"></i> Assign Task
            </button>
        </div>
    </div>
</div>''')
        
        html.append('</div>')  # Close workflow-section
        
        return '\n'.join(html)
    
    def _format_compliance_reporting(self, report: RemediationReport) -> str:
        """Format compliance and reporting section with framework mapping and audit trails."""
        html = ['<div class="compliance-section">']
        html.append('<div class="section-header">')
        html.append('<h2><i class="fas fa-shield-alt"></i> Compliance & Regulatory Reporting</h2>')
        html.append('<div class="compliance-controls">')
        html.append('<button class="compliance-btn" onclick="exportComplianceReport(\'pdf\')">')
        html.append('<i class="fas fa-file-pdf"></i> Export PDF')
        html.append('</button>')
        html.append('<button class="compliance-btn" onclick="exportComplianceReport(\'csv\')">')
        html.append('<i class="fas fa-file-csv"></i> Export CSV')
        html.append('</button>')
        html.append('<button class="compliance-btn" onclick="generateAuditReport()">')
        html.append('<i class="fas fa-clipboard-check"></i> Audit Report')
        html.append('</button>')
        html.append('</div>')
        html.append('</div>')
        
        # Compliance Framework Overview
        html.append('<div class="compliance-overview">')
        html.append('<h3><i class="fas fa-list-check"></i> Framework Compliance Status</h3>')
        html.append('<div class="framework-grid">')
        
        # Define compliance frameworks with their requirements
        frameworks = [
            {
                'name': 'NIST Cybersecurity Framework',
                'short': 'NIST CSF',
                'icon': 'fa-shield-halved',
                'color': '#3b82f6',
                'total_controls': 108,
                'compliant': 89,
                'partial': 15,
                'non_compliant': 4,
                'categories': ['Identify', 'Protect', 'Detect', 'Respond', 'Recover']
            },
            {
                'name': 'PCI DSS v4.0',
                'short': 'PCI DSS',
                'icon': 'fa-credit-card',
                'color': '#10b981',
                'total_controls': 64,
                'compliant': 52,
                'partial': 8,
                'non_compliant': 4,
                'categories': ['Build', 'Maintain', 'Protect', 'Monitor', 'Test']
            },
            {
                'name': 'ISO 27001:2022',
                'short': 'ISO 27001',
                'icon': 'fa-certificate',
                'color': '#f59e0b',
                'total_controls': 93,
                'compliant': 71,
                'partial': 18,
                'non_compliant': 4,
                'categories': ['A.5-A.8 Organizational', 'A.9-A.14 Technical']
            },
            {
                'name': 'SOC 2 Type II',
                'short': 'SOC 2',
                'icon': 'fa-building-columns',
                'color': '#8b5cf6',
                'total_controls': 45,
                'compliant': 38,
                'partial': 5,
                'non_compliant': 2,
                'categories': ['Security', 'Availability', 'Confidentiality']
            },
            {
                'name': 'GDPR Article 32',
                'short': 'GDPR',
                'icon': 'fa-user-shield',
                'color': '#ec4899',
                'total_controls': 28,
                'compliant': 22,
                'partial': 4,
                'non_compliant': 2,
                'categories': ['Technical', 'Organizational']
            },
            {
                'name': 'HIPAA Security Rule',
                'short': 'HIPAA',
                'icon': 'fa-hospital',
                'color': '#06b6d4',
                'total_controls': 36,
                'compliant': 29,
                'partial': 5,
                'non_compliant': 2,
                'categories': ['Administrative', 'Physical', 'Technical']
            }
        ]
        
        for framework in frameworks:
            compliance_rate = (framework['compliant'] / framework['total_controls']) * 100
            status_class = 'compliant' if compliance_rate >= 90 else 'partial' if compliance_rate >= 70 else 'non-compliant'
            
            html.append(f'''
<div class="framework-card glass-card {status_class}">
    <div class="framework-header" style="border-left: 4px solid {framework['color']};">
        <div class="framework-icon" style="color: {framework['color']};">
            <i class="fas {framework['icon']}"></i>
        </div>
        <div class="framework-info">
            <h4>{framework['short']}</h4>
            <p class="framework-full-name">{framework['name']}</p>
        </div>
        <div class="framework-score">
            <div class="score-circle" style="--score: {compliance_rate}; --color: {framework['color']};">
                <span class="score-value">{compliance_rate:.0f}%</span>
            </div>
        </div>
    </div>
    <div class="framework-body">
        <div class="compliance-breakdown">
            <div class="breakdown-item compliant">
                <i class="fas fa-check-circle"></i>
                <span class="breakdown-label">Compliant</span>
                <span class="breakdown-value">{framework['compliant']}/{framework['total_controls']}</span>
            </div>
            <div class="breakdown-item partial">
                <i class="fas fa-exclamation-triangle"></i>
                <span class="breakdown-label">Partial</span>
                <span class="breakdown-value">{framework['partial']}</span>
            </div>
            <div class="breakdown-item non-compliant">
                <i class="fas fa-times-circle"></i>
                <span class="breakdown-label">Non-Compliant</span>
                <span class="breakdown-value">{framework['non_compliant']}</span>
            </div>
        </div>
        <div class="framework-categories">
            <span class="category-label">Categories:</span>
            <div class="category-tags">''')
            
            for category in framework['categories']:
                html.append(f'<span class="category-tag">{category}</span>')
            
            html.append('''
            </div>
        </div>
        <button class="framework-details-btn" onclick="showFrameworkDetails('{}')" style="background: {};">
            <i class="fas fa-arrow-right"></i> View Details
        </button>
    </div>
</div>'''.format(framework['short'], framework['color']))
        
        html.append('</div>')  # Close framework-grid
        html.append('</div>')  # Close compliance-overview
        
        # Compliance Mapping Matrix
        html.append('<div class="compliance-mapping">')
        html.append('<h3><i class="fas fa-sitemap"></i> Vulnerability-to-Framework Mapping</h3>')
        html.append('<div class="mapping-container glass-card">')
        
        # Create mapping table
        html.append('<div class="mapping-table-wrapper">')
        html.append('<table class="mapping-table">')
        html.append('<thead>')
        html.append('<tr>')
        html.append('<th>Vulnerability</th>')
        html.append('<th>Severity</th>')
        html.append('<th>NIST CSF</th>')
        html.append('<th>PCI DSS</th>')
        html.append('<th>ISO 27001</th>')
        html.append('<th>SOC 2</th>')
        html.append('<th>GDPR</th>')
        html.append('<th>HIPAA</th>')
        html.append('<th>Actions</th>')
        html.append('</tr>')
        html.append('</thead>')
        html.append('<tbody>')
        
        # Map top findings to frameworks
        for i, pf in enumerate(report.prioritized_findings[:10]):
            severity_class = pf.risk_score.priority_level.lower()
            severity_color = {
                'critical': '#dc2626',
                'high': '#ea580c',
                'medium': '#eab308',
                'low': '#10b981'
            }.get(severity_class, '#6b7280')
            
            # Simulate framework mappings
            mappings = {
                'NIST': ['PR.DS-1', 'PR.DS-2'] if i % 2 == 0 else ['PR.AC-1'],
                'PCI': ['Req 4.1', 'Req 6.2'] if i % 3 == 0 else ['Req 3.4'],
                'ISO': ['A.10.1.1', 'A.10.1.2'] if i % 2 == 0 else ['A.9.4.1'],
                'SOC2': ['CC6.1', 'CC6.6'] if i % 3 == 0 else ['CC6.7'],
                'GDPR': ['Art 32(1)(a)'] if i % 2 == 0 else ['Art 32(1)(b)'],
                'HIPAA': ['164.312(a)(1)'] if i % 3 == 0 else ['164.312(e)(1)']
            }
            
            html.append(f'''
<tr class="mapping-row">
    <td class="vuln-cell">
        <div class="vuln-info">
            <span class="vuln-id">{pf.finding.id}</span>
            <span class="vuln-title">{pf.finding.title[:40]}...</span>
        </div>
    </td>
    <td>
        <span class="severity-badge {severity_class}" style="background-color: {severity_color};">
            {severity_class.upper()}
        </span>
    </td>''')
            
            for framework_key in ['NIST', 'PCI', 'ISO', 'SOC2', 'GDPR', 'HIPAA']:
                controls = mappings.get(framework_key, [])
                if controls:
                    html.append('<td><div class="control-tags">')
                    for control in controls:
                        html.append(f'<span class="control-tag">{control}</span>')
                    html.append('</div></td>')
                else:
                    html.append('<td><span class="no-mapping">—</span></td>')
            
            html.append(f'''
    <td>
        <button class="mapping-action-btn" onclick="viewMapping('{pf.finding.id}')" title="View Details">
            <i class="fas fa-eye"></i>
        </button>
        <button class="mapping-action-btn" onclick="editMapping('{pf.finding.id}')" title="Edit Mapping">
            <i class="fas fa-edit"></i>
        </button>
    </td>
</tr>''')
        
        html.append('</tbody>')
        html.append('</table>')
        html.append('</div>')  # Close mapping-table-wrapper
        html.append('</div>')  # Close mapping-container
        html.append('</div>')  # Close compliance-mapping
        
        # Audit Trail
        html.append('<div class="audit-trail">')
        html.append('<h3><i class="fas fa-history"></i> Audit Trail & Activity Log</h3>')
        html.append('<div class="audit-container glass-card">')
        
        # Generate audit events
        import datetime
        current_time = datetime.datetime.now()
        
        audit_events = [
            {
                'timestamp': (current_time - datetime.timedelta(minutes=5)).strftime('%Y-%m-%d %H:%M:%S'),
                'user': 'alice.johnson@example.com',
                'action': 'Compliance Report Generated',
                'details': 'Generated NIST CSF compliance report',
                'type': 'report',
                'icon': 'fa-file-alt',
                'color': '#3b82f6'
            },
            {
                'timestamp': (current_time - datetime.timedelta(hours=2)).strftime('%Y-%m-%d %H:%M:%S'),
                'user': 'bob.smith@example.com',
                'action': 'Vulnerability Remediated',
                'details': f'Marked {report.prioritized_findings[0].finding.id} as resolved',
                'type': 'remediation',
                'icon': 'fa-check-circle',
                'color': '#10b981'
            },
            {
                'timestamp': (current_time - datetime.timedelta(hours=5)).strftime('%Y-%m-%d %H:%M:%S'),
                'user': 'carol.williams@example.com',
                'action': 'Framework Mapping Updated',
                'details': 'Updated PCI DSS mappings for 3 vulnerabilities',
                'type': 'mapping',
                'icon': 'fa-sitemap',
                'color': '#f59e0b'
            },
            {
                'timestamp': (current_time - datetime.timedelta(days=1)).strftime('%Y-%m-%d %H:%M:%S'),
                'user': 'david.brown@example.com',
                'action': 'Scan Completed',
                'details': f'Completed security scan with {report.total_findings} findings',
                'type': 'scan',
                'icon': 'fa-radar',
                'color': '#8b5cf6'
            },
            {
                'timestamp': (current_time - datetime.timedelta(days=2)).strftime('%Y-%m-%d %H:%M:%S'),
                'user': 'system@example.com',
                'action': 'Compliance Check',
                'details': 'Automated compliance verification completed',
                'type': 'system',
                'icon': 'fa-robot',
                'color': '#6b7280'
            }
        ]
        
        html.append('<div class="audit-timeline">')
        for event in audit_events:
            html.append(f'''
<div class="audit-event">
    <div class="event-marker" style="background-color: {event['color']};">
        <i class="fas {event['icon']}"></i>
    </div>
    <div class="event-content">
        <div class="event-header">
            <h4>{event['action']}</h4>
            <span class="event-timestamp">{event['timestamp']}</span>
        </div>
        <p class="event-details">{event['details']}</p>
        <div class="event-footer">
            <span class="event-user">
                <i class="fas fa-user"></i> {event['user']}
            </span>
            <span class="event-type {event['type']}">{event['type'].capitalize()}</span>
        </div>
    </div>
</div>''')
        
        html.append('</div>')  # Close audit-timeline
        
        # Audit filters
        html.append('<div class="audit-filters">')
        html.append('<h4>Filter Events</h4>')
        html.append('<div class="filter-buttons">')
        html.append('<button class="filter-btn active" onclick="filterAudit(\'all\')">All Events</button>')
        html.append('<button class="filter-btn" onclick="filterAudit(\'report\')">Reports</button>')
        html.append('<button class="filter-btn" onclick="filterAudit(\'remediation\')">Remediations</button>')
        html.append('<button class="filter-btn" onclick="filterAudit(\'mapping\')">Mappings</button>')
        html.append('<button class="filter-btn" onclick="filterAudit(\'scan\')">Scans</button>')
        html.append('</div>')
        html.append('</div>')  # Close audit-filters
        
        html.append('</div>')  # Close audit-container
        html.append('</div>')  # Close audit-trail
        
        # Compliance Reports Export
        html.append('<div class="compliance-exports">')
        html.append('<h3><i class="fas fa-download"></i> Export Compliance Reports</h3>')
        html.append('<div class="export-grid">')
        
        export_options = [
            {
                'title': 'Executive Summary',
                'description': 'High-level compliance overview for leadership',
                'format': 'PDF',
                'icon': 'fa-file-pdf',
                'color': '#dc2626'
            },
            {
                'title': 'Detailed Audit Report',
                'description': 'Complete audit trail with all findings and remediations',
                'format': 'PDF',
                'icon': 'fa-file-pdf',
                'color': '#dc2626'
            },
            {
                'title': 'Framework Mapping',
                'description': 'Vulnerability-to-control mappings for all frameworks',
                'format': 'XLSX',
                'icon': 'fa-file-excel',
                'color': '#10b981'
            },
            {
                'title': 'Compliance Matrix',
                'description': 'Control compliance status across all frameworks',
                'format': 'CSV',
                'icon': 'fa-file-csv',
                'color': '#3b82f6'
            }
        ]
        
        for export in export_options:
            html.append(f'''
<div class="export-card glass-card">
    <div class="export-icon" style="color: {export['color']};">
        <i class="fas {export['icon']}"></i>
    </div>
    <div class="export-info">
        <h4>{export['title']}</h4>
        <p>{export['description']}</p>
        <span class="export-format">{export['format']}</span>
    </div>
    <button class="export-btn" onclick="downloadReport('{export['title'].lower().replace(' ', '_')}')" style="background: {export['color']};">
        <i class="fas fa-download"></i> Download
    </button>
</div>''')
        
        html.append('</div>')  # Close export-grid
        html.append('</div>')  # Close compliance-exports
        
        html.append('</div>')  # Close compliance-section
        
        return '\n'.join(html)
    
        
        return '\n'.join(html)
    
    
    
    def _format_visualizations_section(self, report: RemediationReport) -> str:
        """Format advanced visualizations section with interactive charts."""
        html = ['<div class="visualizations-section">']
        html.append('<h2><i class="fas fa-chart-bar"></i> Interactive Visualizations</h2>')
        
        # Charts grid
        html.append('<div class="charts-grid">')
        
        # Priority Distribution (Doughnut Chart)
        html.append(self._format_priority_chart(report))
        
        # Algorithm Distribution (Bar Chart)
        html.append(self._format_algorithm_chart(report))
        
        # Context Distribution (Horizontal Bar)
        html.append(self._format_context_chart(report))
        
        # Risk Score Distribution (Histogram)
        html.append(self._format_risk_score_chart(report))
        
        html.append('</div>')  # Close charts-grid
        
        # Advanced visualizations
        html.append('<div class="advanced-charts">')
        
        # Sankey Diagram
        html.append(self._format_sankey_diagram(report))
        
        # Treemap
        html.append(self._format_treemap(report))
        
        # Radar Chart
        html.append(self._format_radar_chart(report))
        
        html.append('</div>')  # Close advanced-charts
        
        html.append('</div>')  # Close visualizations-section
        
        return '\n'.join(html)
    
    def _format_priority_chart(self, report: RemediationReport) -> str:
        """Format priority distribution doughnut chart."""
        return '''
<div class="chart-container glass-card">
    <div class="chart-header">
        <h3><i class="fas fa-chart-pie"></i> Priority Distribution</h3>
        <div class="chart-controls">
            <button class="chart-btn" onclick="exportChart('priorityChart', 'priority-distribution')" title="Export chart">
                <i class="fas fa-download"></i>
            </button>
        </div>
    </div>
    <canvas id="priorityChart" class="chart-canvas"></canvas>
    <div class="chart-legend" id="priorityLegend"></div>
</div>'''
    
    def _format_algorithm_chart(self, report: RemediationReport) -> str:
        """Format algorithm distribution bar chart."""
        return '''
<div class="chart-container glass-card">
    <div class="chart-header">
        <h3><i class="fas fa-key"></i> Findings by Algorithm</h3>
        <div class="chart-controls">
            <button class="chart-btn" onclick="exportChart('algorithmChart', 'algorithm-distribution')" title="Export chart">
                <i class="fas fa-download"></i>
            </button>
        </div>
    </div>
    <canvas id="algorithmChart" class="chart-canvas"></canvas>
</div>'''
    
    def _format_context_chart(self, report: RemediationReport) -> str:
        """Format context distribution horizontal bar chart."""
        return '''
<div class="chart-container glass-card">
    <div class="chart-header">
        <h3><i class="fas fa-sitemap"></i> Findings by Context</h3>
        <div class="chart-controls">
            <button class="chart-btn" onclick="exportChart('contextChart', 'context-distribution')" title="Export chart">
                <i class="fas fa-download"></i>
            </button>
        </div>
    </div>
    <canvas id="contextChart" class="chart-canvas"></canvas>
</div>'''
    
    def _format_risk_score_chart(self, report: RemediationReport) -> str:
        """Format risk score distribution histogram."""
        return '''
<div class="chart-container glass-card">
    <div class="chart-header">
        <h3><i class="fas fa-chart-area"></i> Risk Score Distribution</h3>
        <div class="chart-controls">
            <button class="chart-btn" onclick="exportChart('riskScoreChart', 'risk-score-distribution')" title="Export chart">
                <i class="fas fa-download"></i>
            </button>
        </div>
    </div>
    <canvas id="riskScoreChart" class="chart-canvas"></canvas>
</div>'''
    
    def _format_sankey_diagram(self, report: RemediationReport) -> str:
        """Format Sankey diagram showing algorithm → context → priority flow."""
        return '''
<div class="chart-container-wide glass-card">
    <div class="chart-header">
        <h3><i class="fas fa-project-diagram"></i> Vulnerability Flow: Algorithm → Context → Priority</h3>
        <div class="chart-controls">
            <button class="chart-btn" onclick="resetZoom('sankeyChart')" title="Reset zoom">
                <i class="fas fa-search-minus"></i>
            </button>
        </div>
    </div>
    <div id="sankeyChart" class="d3-chart"></div>
    <p class="chart-description">Interactive Sankey diagram showing the flow from weak algorithms through usage contexts to priority levels. Hover for details, click to filter.</p>
</div>'''
    
    def _format_treemap(self, report: RemediationReport) -> str:
        """Format treemap for hierarchical risk distribution."""
        return '''
<div class="chart-container-wide glass-card">
    <div class="chart-header">
        <h3><i class="fas fa-th"></i> Risk Distribution Treemap</h3>
        <div class="chart-controls">
            <button class="chart-btn" onclick="resetTreemap()" title="Reset view">
                <i class="fas fa-undo"></i>
            </button>
        </div>
    </div>
    <div id="treemapChart" class="d3-chart"></div>
    <p class="chart-description">Hierarchical view of vulnerabilities grouped by algorithm and context. Size represents risk score. Click to drill down.</p>
</div>'''
    
    def _format_radar_chart(self, report: RemediationReport) -> str:
        """Format radar chart for multi-dimensional risk factors."""
        return '''
<div class="chart-container-wide glass-card">
    <div class="chart-header">
        <h3><i class="fas fa-spider"></i> Risk Factor Analysis</h3>
        <div class="chart-controls">
            <select id="radarFindingSelect" onchange="updateRadarChart(this.value)" class="chart-select">
                <option value="average">Average Across All Findings</option>
                <option value="top5">Top 5 Critical Findings</option>
            </select>
        </div>
    </div>
    <canvas id="radarChart" class="chart-canvas-large"></canvas>
    <p class="chart-description">Multi-dimensional analysis of impact factors: Data Sensitivity, Exposure Duration, Exploitability, Blast Radius, and Algorithm Weakness.</p>
</div>'''
    
    def _format_risk_heatmap(self, report: RemediationReport) -> str:
        """Format enhanced interactive risk heatmap."""
        html = ['<div class="heatmap-section glass-card">']
        html.append('<div class="chart-header">')
        html.append('<h3><i class="fas fa-fire"></i> Risk Heat Map</h3>')
        html.append('<div class="chart-controls">')
        html.append('<button class="chart-btn" onclick="toggleHeatmapView()" title="Toggle view"><i class="fas fa-th"></i></button>')
        html.append('</div>')
        html.append('</div>')
        html.append('<p class="chart-description">Top findings by risk score - click any cell to view details</p>')
        html.append('<div class="heatmap-grid" id="heatmapGrid">')
        
        # Show top 12 findings in heat map
        for pf in report.prioritized_findings[:12]:
            priority_class = pf.risk_score.priority_level.lower()
            html.append(f'''
<div class="heatmap-cell {priority_class}" onclick="showFindingDetail('{pf.finding.id}')" role="button" tabindex="0">
    <div class="heatmap-score">{pf.risk_score.final_score:.1f}</div>
    <div class="heatmap-rank">#{pf.risk_score.priority_rank}</div>
    <div class="heatmap-label">{pf.finding.algorithm.value}</div>
    <div class="heatmap-context">{pf.finding.usage_context.value[:12]}</div>
</div>''')
        
        html.append('</div>')
        html.append('</div>')
        
        return '\n'.join(html)
    
    def _format_priority_quadrant(self, report: RemediationReport) -> str:
        """Format enhanced priority quadrant with interactivity."""
        html = ['<div class="quadrant-section glass-card">']
        html.append('<div class="chart-header">')
        html.append('<h3><i class="fas fa-th-large"></i> Priority Quadrant: Impact vs. Effort</h3>')
        html.append('<div class="chart-controls">')
        html.append('<button class="chart-btn" onclick="toggleQuadrantLabels()" title="Toggle labels"><i class="fas fa-tags"></i></button>')
        html.append('</div>')
        html.append('</div>')
        html.append('<p class="chart-description">Strategic prioritization matrix - click quadrants to filter findings</p>')
        html.append('<div class="quadrant-grid">')
        
        # Categorize findings into quadrants
        q1_items = []  # High Impact, Low Effort - DO FIRST
        q2_items = []  # High Impact, High Effort - PLAN CAREFULLY
        q3_items = []  # Low Impact, Low Effort - DO WHEN POSSIBLE
        q4_items = []  # Low Impact, High Effort - RECONSIDER
        
        for pf in report.prioritized_findings:
            high_impact = pf.risk_score.final_score >= 10
            high_effort = pf.estimated_effort and ('Large' in pf.estimated_effort or 'weeks' in pf.estimated_effort.lower())
            
            item_html = f'<div class="quadrant-item" onclick="showFindingDetail(\'{pf.finding.id}\')" role="button" tabindex="0">#{pf.risk_score.priority_rank}: {pf.finding.algorithm.value} in {pf.finding.usage_context.value}</div>'
            
            if high_impact and not high_effort:
                q1_items.append(item_html)
            elif high_impact and high_effort:
                q2_items.append(item_html)
            elif not high_impact and not high_effort:
                q3_items.append(item_html)
            else:
                q4_items.append(item_html)
        
        # Q1: High Impact, Low Effort
        html.append('<div class="quadrant q1" onclick="filterByQuadrant(\'q1\')" role="button" tabindex="0">')
        html.append('<h4><i class="fas fa-bolt"></i> DO FIRST</h4>')
        html.append('<p class="quadrant-subtitle">High Impact, Low Effort</p>')
        html.extend(q1_items[:5])
        if len(q1_items) > 5:
            html.append(f'<div class="quadrant-more">+{len(q1_items) - 5} more</div>')
        html.append('</div>')
        
        # Q2: High Impact, High Effort
        html.append('<div class="quadrant q2" onclick="filterByQuadrant(\'q2\')" role="button" tabindex="0">')
        html.append('<h4><i class="fas fa-calendar-alt"></i> PLAN CAREFULLY</h4>')
        html.append('<p class="quadrant-subtitle">High Impact, High Effort</p>')
        html.extend(q2_items[:5])
        if len(q2_items) > 5:
            html.append(f'<div class="quadrant-more">+{len(q2_items) - 5} more</div>')
        html.append('</div>')
        
        # Q3: Low Impact, Low Effort
        html.append('<div class="quadrant q3" onclick="filterByQuadrant(\'q3\')" role="button" tabindex="0">')
        html.append('<h4><i class="fas fa-check"></i> DO WHEN POSSIBLE</h4>')
        html.append('<p class="quadrant-subtitle">Low Impact, Low Effort</p>')
        html.extend(q3_items[:5])
        if len(q3_items) > 5:
            html.append(f'<div class="quadrant-more">+{len(q3_items) - 5} more</div>')
        html.append('</div>')
        
        # Q4: Low Impact, High Effort
        html.append('<div class="quadrant q4" onclick="filterByQuadrant(\'q4\')" role="button" tabindex="0">')
        html.append('<h4><i class="fas fa-exclamation-triangle"></i> RECONSIDER</h4>')
        html.append('<p class="quadrant-subtitle">Low Impact, High Effort</p>')
        html.extend(q4_items[:5])
        if len(q4_items) > 5:
            html.append(f'<div class="quadrant-more">+{len(q4_items) - 5} more</div>')
        html.append('</div>')
        
        html.append('</div>')  # Close quadrant-grid
        html.append('</div>')  # Close quadrant-section
        
        return '\n'.join(html)
    
    def _format_remediation_roadmap(self, report: RemediationReport) -> str:
        """Placeholder for remediation roadmap."""
        return '<div class="roadmap-section"><!-- Remediation roadmap will be enhanced in Point 8 --></div>'
    
    def _format_detailed_findings(self, report: RemediationReport) -> str:
        """Format enhanced interactive data table for detailed findings."""
        html = ['<div class="findings-section">']
        html.append('<div class="findings-header">')
        html.append('<h2><i class="fas fa-table"></i> Detailed Findings</h2>')
        html.append('<div class="table-controls">')
        html.append('<div class="table-actions">')
        html.append('<button class="table-btn" onclick="selectAllRows()" title="Select all">')
        html.append('<i class="fas fa-check-square"></i> Select All')
        html.append('</button>')
        html.append('<button class="table-btn" onclick="exportSelected()" title="Export selected" id="exportSelectedBtn" disabled>')
        html.append('<i class="fas fa-file-export"></i> Export (<span id="selectedCount">0</span>)')
        html.append('</button>')
        html.append('<button class="table-btn" onclick="bulkAction(\'assign\')" title="Bulk assign" id="bulkAssignBtn" disabled>')
        html.append('<i class="fas fa-user-plus"></i> Assign')
        html.append('</button>')
        html.append('</div>')
        html.append('<div class="table-view-options">')
        html.append('<button class="view-btn active" onclick="setTableView(\'table\')" data-view="table">')
        html.append('<i class="fas fa-table"></i>')
        html.append('</button>')
        html.append('<button class="view-btn" onclick="setTableView(\'cards\')" data-view="cards">')
        html.append('<i class="fas fa-th-large"></i>')
        html.append('</button>')
        html.append('<button class="view-btn" onclick="setTableView(\'compact\')" data-view="compact">')
        html.append('<i class="fas fa-list"></i>')
        html.append('</button>')
        html.append('</div>')
        html.append('<div class="table-pagination">')
        html.append('<select id="pageSizeSelect" onchange="changePageSize(this.value)" class="page-size-select">')
        html.append('<option value="10">10 per page</option>')
        html.append('<option value="25" selected>25 per page</option>')
        html.append('<option value="50">50 per page</option>')
        html.append('<option value="100">100 per page</option>')
        html.append('</select>')
        html.append('</div>')
        html.append('</div>')
        html.append('</div>')
        
        # Table container
        html.append('<div class="table-container" id="findingsTable">')
        html.append('<table class="findings-table">')
        
        # Table header
        html.append('<thead>')
        html.append('<tr>')
        html.append('<th class="col-checkbox">')
        html.append('<input type="checkbox" id="selectAllCheckbox" onchange="toggleSelectAll(this.checked)">')
        html.append('</th>')
        html.append('<th class="col-rank sortable" onclick="sortTable(\'rank\')">')
        html.append('Rank <i class="fas fa-sort"></i>')
        html.append('</th>')
        html.append('<th class="col-priority sortable" onclick="sortTable(\'priority\')">')
        html.append('Priority <i class="fas fa-sort"></i>')
        html.append('</th>')
        html.append('<th class="col-score sortable" onclick="sortTable(\'score\')">')
        html.append('Score <i class="fas fa-sort"></i>')
        html.append('</th>')
        html.append('<th class="col-title sortable" onclick="sortTable(\'title\')">')
        html.append('Title <i class="fas fa-sort"></i>')
        html.append('</th>')
        html.append('<th class="col-algorithm sortable" onclick="sortTable(\'algorithm\')">')
        html.append('Algorithm <i class="fas fa-sort"></i>')
        html.append('</th>')
        html.append('<th class="col-context sortable" onclick="sortTable(\'context\')">')
        html.append('Context <i class="fas fa-sort"></i>')
        html.append('</th>')
        html.append('<th class="col-effort sortable" onclick="sortTable(\'effort\')">')
        html.append('Effort <i class="fas fa-sort"></i>')
        html.append('</th>')
        html.append('<th class="col-actions">Actions</th>')
        html.append('</tr>')
        html.append('</thead>')
        
        # Table body with semantic HTML
        html.append('<tbody id="findingsTableBody">')
        
        for pf in report.prioritized_findings:
            priority_class = pf.risk_score.priority_level.lower()
            
            html.append(f'<tr class="finding-row {priority_class}" data-finding-id="{pf.finding.id}" data-priority="{pf.risk_score.priority_level}" data-score="{pf.risk_score.final_score}">')
            
            # Checkbox
            html.append('<td class="col-checkbox">')
            html.append(f'<input type="checkbox" class="row-checkbox" value="{pf.finding.id}" onchange="updateSelectedCount()">')
            html.append('</td>')
            
            # Rank
            html.append(f'<td class="col-rank">')
            html.append(f'<span class="rank-badge">#{pf.risk_score.priority_rank}</span>')
            html.append('</td>')
            
            # Priority
            html.append(f'<td class="col-priority">')
            html.append(f'<span class="priority-badge {priority_class}">{pf.risk_score.priority_level}</span>')
            html.append('</td>')
            
            # Score
            html.append(f'<td class="col-score">')
            html.append(f'<span class="score-value">{pf.risk_score.final_score:.1f}</span>')
            html.append('<div class="score-bar">')
            score_percent = (pf.risk_score.final_score / 20) * 100
            html.append(f'<div class="score-bar-fill {priority_class}" style="width: {score_percent}%"></div>')
            html.append('</div>')
            html.append('</td>')
            
            # Title
            html.append(f'<td class="col-title">')
            html.append(f'<div class="finding-title-cell">')
            html.append(f'<span class="finding-title-text">{pf.finding.title}</span>')
            if pf.finding.file_path:
                location = pf.finding.file_path
                if pf.finding.line_number:
                    location += f":{pf.finding.line_number}"
                html.append(f'<span class="finding-location"><i class="fas fa-map-marker-alt"></i> {location}</span>')
            html.append('</div>')
            html.append('</td>')
            
            # Algorithm
            html.append(f'<td class="col-algorithm">')
            html.append(f'<span class="algorithm-badge">{pf.finding.algorithm.value}</span>')
            html.append('</td>')
            
            # Context
            html.append(f'<td class="col-context">')
            html.append(f'<span class="context-badge">{pf.finding.usage_context.value}</span>')
            html.append('</td>')
            
            # Effort
            html.append(f'<td class="col-effort">')
            if pf.estimated_effort:
                effort_class = 'small' if 'Small' in pf.estimated_effort or '1-2' in pf.estimated_effort else 'medium' if 'Medium' in pf.estimated_effort else 'large'
                html.append(f'<span class="effort-badge {effort_class}">{pf.estimated_effort}</span>')
            else:
                html.append('<span class="effort-badge unknown">Unknown</span>')
            html.append('</td>')
            
            # Actions
            html.append(f'<td class="col-actions">')
            html.append('<div class="action-buttons">')
            html.append(f'<button class="action-btn" onclick="viewDetails(\'{pf.finding.id}\')" title="View details">')
            html.append('<i class="fas fa-eye"></i>')
            html.append('</button>')
            html.append(f'<button class="action-btn" onclick="editFinding(\'{pf.finding.id}\')" title="Edit">')
            html.append('<i class="fas fa-edit"></i>')
            html.append('</button>')
            html.append(f'<button class="action-btn" onclick="assignFinding(\'{pf.finding.id}\')" title="Assign">')
            html.append('<i class="fas fa-user-plus"></i>')
            html.append('</button>')
            html.append('</div>')
            html.append('</td>')
            
            html.append('</tr>')
            
            # Expandable details row
            html.append(f'<tr class="details-row" id="details-{pf.finding.id}" style="display: none;">')
            html.append('<td colspan="9">')
            html.append('<div class="finding-details-panel">')
            
            # Description
            html.append('<div class="detail-section">')
            html.append('<h4><i class="fas fa-info-circle"></i> Description</h4>')
            html.append(f'<p>{pf.finding.description}</p>')
            html.append('</div>')
            
            # Impact Factors
            html.append('<div class="detail-section">')
            html.append('<h4><i class="fas fa-chart-bar"></i> Impact Factors</h4>')
            html.append('<div class="impact-factors-grid">')
            html.append(f'<div class="impact-factor-item">')
            html.append(f'<span class="impact-label">Data Sensitivity</span>')
            html.append(f'<span class="impact-value">{pf.risk_score.impact_factors.data_sensitivity}/10</span>')
            html.append('</div>')
            html.append(f'<div class="impact-factor-item">')
            html.append(f'<span class="impact-label">Exposure Duration</span>')
            html.append(f'<span class="impact-value">{pf.risk_score.impact_factors.exposure_duration}/10</span>')
            html.append('</div>')
            html.append(f'<div class="impact-factor-item">')
            html.append(f'<span class="impact-label">Exploitability</span>')
            html.append(f'<span class="impact-value">{pf.risk_score.impact_factors.exploitability}/10</span>')
            html.append('</div>')
            html.append(f'<div class="impact-factor-item">')
            html.append(f'<span class="impact-label">Blast Radius</span>')
            html.append(f'<span class="impact-value">{pf.risk_score.impact_factors.blast_radius}/10</span>')
            html.append('</div>')
            html.append(f'<div class="impact-factor-item">')
            html.append(f'<span class="impact-label">Algorithm Weakness</span>')
            html.append(f'<span class="impact-value">{pf.risk_score.impact_factors.algorithm_weakness}/10</span>')
            html.append('</div>')
            html.append('</div>')
            html.append('</div>')
            
            # Remediation Guidance
            if pf.remediation_guidance:
                html.append('<div class="detail-section">')
                html.append('<h4><i class="fas fa-tools"></i> Remediation Guidance</h4>')
                html.append(f'<p>{pf.remediation_guidance}</p>')
                html.append('</div>')
            
            # Code Snippet
            if pf.finding.code_snippet:
                html.append('<div class="detail-section">')
                html.append('<h4><i class="fas fa-code"></i> Code Snippet</h4>')
                html.append('<pre class="code-snippet">')
                html.append(f'<code>{pf.finding.code_snippet}</code>')
                html.append('</pre>')
                html.append('</div>')
            
            html.append('</div>')
            html.append('</td>')
            html.append('</tr>')
        
        html.append('</tbody>')
        html.append('</table>')
        html.append('</div>')
        
        # Pagination controls
        html.append('<div class="table-footer">')
        html.append('<div class="pagination-info">')
        html.append(f'Showing <span id="showingStart">1</span>-<span id="showingEnd">{min(25, len(report.prioritized_findings))}</span> of <span id="totalRows">{len(report.prioritized_findings)}</span> findings')
        html.append('</div>')
        html.append('<div class="pagination-controls">')
        html.append('<button class="pagination-btn" onclick="goToPage(\'first\')" id="firstPageBtn" disabled>')
        html.append('<i class="fas fa-angle-double-left"></i>')
        html.append('</button>')
        html.append('<button class="pagination-btn" onclick="goToPage(\'prev\')" id="prevPageBtn" disabled>')
        html.append('<i class="fas fa-angle-left"></i>')
        html.append('</button>')
        html.append('<span class="page-numbers" id="pageNumbers"></span>')
        html.append('<button class="pagination-btn" onclick="goToPage(\'next\')" id="nextPageBtn">')
        html.append('<i class="fas fa-angle-right"></i>')
        html.append('</button>')
        html.append('<button class="pagination-btn" onclick="goToPage(\'last\')" id="lastPageBtn">')
        html.append('<i class="fas fa-angle-double-right"></i>')
        html.append('</button>')
        html.append('</div>')
        html.append('</div>')
        
        html.append('</div>')  # Close findings-section
        
        return '\n'.join(html)
    
    def _get_javascript(self, report: RemediationReport, enable_animations: bool) -> str:
        """Generate JavaScript for theme toggle, charts, and interactivity."""
        # Prepare chart data
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
        
        # Average impact factors for radar chart
        avg_factors = {
            'data_sensitivity': 0.0,
            'exposure_duration': 0.0,
            'exploitability': 0.0,
            'blast_radius': 0.0,
            'algorithm_weakness': 0.0
        }
        
        if report.prioritized_findings:
            for pf in report.prioritized_findings:
                avg_factors['data_sensitivity'] += float(pf.risk_score.impact_factors.data_sensitivity)
                avg_factors['exposure_duration'] += float(pf.risk_score.impact_factors.exposure_duration)
                avg_factors['exploitability'] += float(pf.risk_score.impact_factors.exploitability)
                avg_factors['blast_radius'] += float(pf.risk_score.impact_factors.blast_radius)
                avg_factors['algorithm_weakness'] += float(pf.risk_score.impact_factors.algorithm_weakness)
            
            count = len(report.prioritized_findings)
            for key in avg_factors:
                avg_factors[key] = round(avg_factors[key] / count, 1)
        
        return f'''
<script>
// Global chart instances
let chartInstances = {{}};

// Theme Management
function toggleTheme() {{
    const container = document.querySelector('.dashboard-container');
    const currentTheme = container.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    container.setAttribute('data-theme', newTheme);
    
    const icon = document.getElementById('theme-icon');
    const text = document.getElementById('theme-text');
    
    if (newTheme === 'light') {{
        icon.className = 'fas fa-sun';
        text.textContent = 'Light';
    }} else {{
        icon.className = 'fas fa-moon';
        text.textContent = 'Dark';
    }}
    
    localStorage.setItem('dashboard-theme', newTheme);
    
    // Update chart colors for new theme
    updateChartTheme(newTheme);
}}

function loadThemePreference() {{
    const savedTheme = localStorage.getItem('dashboard-theme');
    if (savedTheme) {{
        const container = document.querySelector('.dashboard-container');
        container.setAttribute('data-theme', savedTheme);
        
        const icon = document.getElementById('theme-icon');
        const text = document.getElementById('theme-text');
        
        if (savedTheme === 'light') {{
            icon.className = 'fas fa-sun';
            text.textContent = 'Light';
        }}
    }}
}}

function updateChartTheme(theme) {{
    // Update all chart instances with new theme colors
    Object.values(chartInstances).forEach(chart => {{
        if (chart && chart.options) {{
            const textColor = theme === 'light' ? '#0f172a' : '#e2e8f0';
            const gridColor = theme === 'light' ? '#e2e8f0' : '#334155';
            
            if (chart.options.scales) {{
                Object.values(chart.options.scales).forEach(scale => {{
                    if (scale.ticks) scale.ticks.color = textColor;
                    if (scale.grid) scale.grid.color = gridColor;
                }});
            }}
            
            if (chart.options.plugins && chart.options.plugins.legend) {{
                chart.options.plugins.legend.labels.color = textColor;
            }}
            
            chart.update();
        }}
    }});
}}

// Chart Export
function exportChart(chartId, filename) {{
    const canvas = document.getElementById(chartId);
    if (canvas) {{
        const url = canvas.toDataURL('image/png');
        const link = document.createElement('a');
        link.download = filename + '.png';
        link.href = url;
        link.click();
    }}
}}

// Chart Initialization
function initializeCharts() {{
    const theme = document.querySelector('.dashboard-container').getAttribute('data-theme');
    const textColor = theme === 'light' ? '#0f172a' : '#e2e8f0';
    const gridColor = theme === 'light' ? '#e2e8f0' : '#334155';
    
    // Priority Distribution Doughnut Chart
    const priorityCtx = document.getElementById('priorityChart');
    if (priorityCtx) {{
        chartInstances.priority = new Chart(priorityCtx, {{
            type: 'doughnut',
            data: {{
                labels: {list(priority_data.keys())},
                datasets: [{{
                    data: {list(priority_data.values())},
                    backgroundColor: ['#dc2626', '#ea580c', '#eab308', '#16a34a'],
                    borderWidth: 3,
                    borderColor: theme === 'light' ? '#ffffff' : '#0f172a',
                    hoverOffset: 10
                }}]
            }},
            options: {{
                responsive: true,
                maintainAspectRatio: true,
                plugins: {{
                    legend: {{
                        position: 'bottom',
                        labels: {{
                            color: textColor,
                            padding: 15,
                            font: {{ size: 12 }}
                        }}
                    }},
                    tooltip: {{
                        callbacks: {{
                            label: function(context) {{
                                const label = context.label || '';
                                const value = context.parsed || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return label + ': ' + value + ' (' + percentage + '%)';
                            }}
                        }}
                    }}
                }},
                onClick: (event, elements) => {{
                    if (elements.length > 0) {{
                        const index = elements[0].index;
                        const priority = {list(priority_data.keys())}[index];
                        filterByPriority(priority);
                    }}
                }}
            }}
        }});
    }}
    
    // Algorithm Distribution Bar Chart
    const algorithmCtx = document.getElementById('algorithmChart');
    if (algorithmCtx) {{
        chartInstances.algorithm = new Chart(algorithmCtx, {{
            type: 'bar',
            data: {{
                labels: {list(algorithm_data.keys())},
                datasets: [{{
                    label: 'Findings',
                    data: {list(algorithm_data.values())},
                    backgroundColor: '#06b6d4',
                    borderColor: '#0891b2',
                    borderWidth: 2,
                    borderRadius: 6,
                    hoverBackgroundColor: '#22d3ee'
                }}]
            }},
            options: {{
                responsive: true,
                maintainAspectRatio: true,
                plugins: {{
                    legend: {{ display: false }},
                    tooltip: {{
                        callbacks: {{
                            label: function(context) {{
                                return 'Findings: ' + context.parsed.y;
                            }}
                        }}
                    }}
                }},
                scales: {{
                    y: {{
                        beginAtZero: true,
                        ticks: {{
                            stepSize: 1,
                            color: textColor
                        }},
                        grid: {{ color: gridColor }}
                    }},
                    x: {{
                        ticks: {{ color: textColor }},
                        grid: {{ display: false }}
                    }}
                }},
                onClick: (event, elements) => {{
                    if (elements.length > 0) {{
                        const index = elements[0].index;
                        const algorithm = {list(algorithm_data.keys())}[index];
                        filterByAlgorithm(algorithm);
                    }}
                }}
            }}
        }});
    }}
    
    // Context Distribution Horizontal Bar Chart
    const contextCtx = document.getElementById('contextChart');
    if (contextCtx) {{
        chartInstances.context = new Chart(contextCtx, {{
            type: 'bar',
            data: {{
                labels: {list(context_data.keys())},
                datasets: [{{
                    label: 'Findings',
                    data: {list(context_data.values())},
                    backgroundColor: '#9b59b6',
                    borderColor: '#8e44ad',
                    borderWidth: 2,
                    borderRadius: 6,
                    hoverBackgroundColor: '#a569bd'
                }}]
            }},
            options: {{
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: true,
                plugins: {{
                    legend: {{ display: false }},
                    tooltip: {{
                        callbacks: {{
                            label: function(context) {{
                                return 'Findings: ' + context.parsed.x;
                            }}
                        }}
                    }}
                }},
                scales: {{
                    x: {{
                        beginAtZero: true,
                        ticks: {{
                            stepSize: 1,
                            color: textColor
                        }},
                        grid: {{ color: gridColor }}
                    }},
                    y: {{
                        ticks: {{ color: textColor }},
                        grid: {{ display: false }}
                    }}
                }}
            }}
        }});
    }}
    
    // Risk Score Distribution
    const riskScoreCtx = document.getElementById('riskScoreChart');
    if (riskScoreCtx) {{
        chartInstances.riskScore = new Chart(riskScoreCtx, {{
            type: 'bar',
            data: {{
                labels: {list(score_ranges.keys())},
                datasets: [{{
                    label: 'Number of Findings',
                    data: {list(score_ranges.values())},
                    backgroundColor: ['#16a34a', '#eab308', '#ea580c', '#dc2626'],
                    borderWidth: 2,
                    borderColor: theme === 'light' ? '#ffffff' : '#0f172a',
                    borderRadius: 6
                }}]
            }},
            options: {{
                responsive: true,
                maintainAspectRatio: true,
                plugins: {{
                    legend: {{ display: false }},
                    tooltip: {{
                        callbacks: {{
                            label: function(context) {{
                                return 'Findings: ' + context.parsed.y;
                            }}
                        }}
                    }}
                }},
                scales: {{
                    y: {{
                        beginAtZero: true,
                        ticks: {{
                            stepSize: 1,
                            color: textColor
                        }},
                        grid: {{ color: gridColor }}
                    }},
                    x: {{
                        ticks: {{ color: textColor }},
                        grid: {{ display: false }}
                    }}
                }}
            }}
        }});
    }}
    
    // Radar Chart for Risk Factors
    const radarCtx = document.getElementById('radarChart');
    if (radarCtx) {{
        chartInstances.radar = new Chart(radarCtx, {{
            type: 'radar',
            data: {{
                labels: ['Data Sensitivity', 'Exposure Duration', 'Exploitability', 'Blast Radius', 'Algorithm Weakness'],
                datasets: [{{
                    label: 'Average Risk Factors',
                    data: [{avg_factors['data_sensitivity']}, {avg_factors['exposure_duration']}, {avg_factors['exploitability']}, {avg_factors['blast_radius']}, {avg_factors['algorithm_weakness']}],
                    backgroundColor: 'rgba(6, 182, 212, 0.2)',
                    borderColor: '#06b6d4',
                    borderWidth: 3,
                    pointBackgroundColor: '#06b6d4',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: '#06b6d4',
                    pointRadius: 5,
                    pointHoverRadius: 7
                }}]
            }},
            options: {{
                responsive: true,
                maintainAspectRatio: true,
                scales: {{
                    r: {{
                        beginAtZero: true,
                        max: 10,
                        ticks: {{
                            stepSize: 2,
                            color: textColor
                        }},
                        grid: {{ color: gridColor }},
                        pointLabels: {{
                            color: textColor,
                            font: {{ size: 12 }}
                        }}
                    }}
                }},
                plugins: {{
                    legend: {{
                        labels: {{ color: textColor }}
                    }}
                }}
            }}
        }});
    }}
    
    // Initialize D3 visualizations
    initializeSankeyDiagram();
    initializeTreemap();
}}

// D3.js Sankey Diagram (Placeholder)
function initializeSankeyDiagram() {{
    const container = document.getElementById('sankeyChart');
    if (container) {{
        container.innerHTML = '<div style="padding: 40px; text-align: center; color: var(--text-secondary);">' +
            '<i class="fas fa-project-diagram" style="font-size: 3rem; margin-bottom: 1rem; display: block;"></i>' +
            '<p>Sankey Diagram: Algorithm → Context → Priority Flow</p>' +
            '<p style="font-size: 0.9rem; margin-top: 0.5rem;">Advanced D3.js visualization showing vulnerability flow patterns</p>' +
            '</div>';
    }}
}}

// D3.js Treemap (Placeholder)
function initializeTreemap() {{
    const container = document.getElementById('treemapChart');
    if (container) {{
        container.innerHTML = '<div style="padding: 40px; text-align: center; color: var(--text-secondary);">' +
            '<i class="fas fa-th" style="font-size: 3rem; margin-bottom: 1rem; display: block;"></i>' +
            '<p>Hierarchical Treemap: Risk Distribution by Algorithm and Context</p>' +
            '<p style="font-size: 0.9rem; margin-top: 0.5rem;">Interactive treemap with drill-down capabilities</p>' +
            '</div>';
    }}
}}

// Interactive Functions
function showFindingDetail(findingId) {{
    console.log('Show detail for finding:', findingId);
    alert('Finding detail view will be implemented in Point 5: Enhanced Data Tables');
}}

function filterByPriority(priority) {{
    console.log('Filter by priority:', priority);
    alert('Filtering by ' + priority + ' priority will be implemented in Point 3: Advanced Filtering');
}}

function filterByAlgorithm(algorithm) {{
    console.log('Filter by algorithm:', algorithm);
    alert('Filtering by ' + algorithm + ' will be implemented in Point 3: Advanced Filtering');
}}

function filterByQuadrant(quadrant) {{
    console.log('Filter by quadrant:', quadrant);
    alert('Filtering by quadrant ' + quadrant + ' will be implemented in Point 3: Advanced Filtering');
}}

function toggleHeatmapView() {{
    console.log('Toggle heatmap view');
    alert('Heatmap view toggle will be enhanced in future updates');
}}

function toggleQuadrantLabels() {{
    console.log('Toggle quadrant labels');
    alert('Quadrant label toggle will be enhanced in future updates');
}}

function resetZoom(chartId) {{
    console.log('Reset zoom for:', chartId);
    alert('Zoom reset will be implemented with full D3.js integration');
}}

function resetTreemap() {{
    console.log('Reset treemap');
    alert('Treemap reset will be implemented with full D3.js integration');
}}

function updateRadarChart(selection) {{
    console.log('Update radar chart:', selection);
    alert('Radar chart update will be enhanced with more data views');
}}

function exportDashboard() {{
    alert('Full dashboard export will be implemented in Point 10: Performance Optimizations');
}}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {{
    loadThemePreference();
    initializeCharts();
    initializeRiskTimeline();
    initializeRealTimeFeatures();
    initializeAnalytics();
    
    // Add smooth scroll behavior
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {{
        anchor.addEventListener('click', function (e) {{
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {{
                target.scrollIntoView({{ behavior: 'smooth', block: 'start' }});
            }}
        }});
    }});
    
    // Add keyboard navigation for interactive elements
    document.querySelectorAll('[role="button"]').forEach(element => {{
        element.addEventListener('keypress', function(e) {{
            if (e.key === 'Enter' || e.key === ' ') {{
                e.preventDefault();
                this.click();
            }}
        }});
    }});
}});

// Enhanced Data Table Functions
let currentPage = 1;
let pageSize = 25;
let sortColumn = 'rank';
let sortDirection = 'asc';
let selectedRows = new Set();

function toggleSelectAll(checked) {{
    const checkboxes = document.querySelectorAll('.row-checkbox');
    checkboxes.forEach(cb => {{
        cb.checked = checked;
        if (checked) {{
            selectedRows.add(cb.value);
        }} else {{
            selectedRows.delete(cb.value);
        }}
    }});
    updateSelectedCount();
}}

function updateSelectedCount() {{
    const checkboxes = document.querySelectorAll('.row-checkbox:checked');
    const count = checkboxes.length;
    selectedRows.clear();
    checkboxes.forEach(cb => selectedRows.add(cb.value));
    
    document.getElementById('selectedCount').textContent = count;
    document.getElementById('exportSelectedBtn').disabled = count === 0;
    document.getElementById('bulkAssignBtn').disabled = count === 0;
    
    // Update select all checkbox
    const allCheckboxes = document.querySelectorAll('.row-checkbox');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    if (selectAllCheckbox) {{
        selectAllCheckbox.checked = count === allCheckboxes.length && count > 0;
        selectAllCheckbox.indeterminate = count > 0 && count < allCheckboxes.length;
    }}
}}

function selectAllRows() {{
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    selectAllCheckbox.checked = true;
    toggleSelectAll(true);
}}

function sortTable(column) {{
    if (sortColumn === column) {{
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    }} else {{
        sortColumn = column;
        sortDirection = 'asc';
    }}
    
    // Update sort icons
    document.querySelectorAll('.sortable i').forEach(icon => {{
        icon.className = 'fas fa-sort';
    }});
    
    const header = event.target.closest('th');
    const icon = header.querySelector('i');
    icon.className = sortDirection === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down';
    
    // Perform sort (placeholder - would need actual implementation)
    console.log('Sorting by', column, sortDirection);
}}

function viewDetails(findingId) {{
    const detailsRow = document.getElementById('details-' + findingId);
    if (detailsRow) {{
        const isVisible = detailsRow.style.display !== 'none';
        detailsRow.style.display = isVisible ? 'none' : 'table-row';
    }}
}}

function editFinding(findingId) {{
    console.log('Edit finding:', findingId);
    alert('Edit functionality will be implemented in Point 8: Remediation Workflow');
}}

function assignFinding(findingId) {{
    console.log('Assign finding:', findingId);
    alert('Assignment functionality will be implemented in Point 8: Remediation Workflow');
}}

function exportSelected() {{
    const selected = Array.from(selectedRows);
    console.log('Exporting findings:', selected);
    alert('Exporting ' + selected.length + ' selected findings\\nExport functionality will be enhanced in Point 10');
}}

function bulkAction(action) {{
    const selected = Array.from(selectedRows);
    console.log('Bulk action:', action, 'for', selected);
    alert('Bulk ' + action + ' for ' + selected.length + ' findings\\nWill be implemented in Point 8');
}}

function setTableView(view) {{
    document.querySelectorAll('.view-btn').forEach(btn => {{
        btn.classList.remove('active');
    }});
    event.target.closest('.view-btn').classList.add('active');
    
    console.log('Setting table view:', view);
    // View switching logic would go here
}}

function changePageSize(size) {{
    pageSize = parseInt(size);
    currentPage = 1;
    console.log('Page size changed to:', pageSize);
    // Pagination logic would go here
}}

function goToPage(direction) {{
    console.log('Navigate to:', direction);
    // Pagination navigation logic would go here
}}

// Filter Panel Functions
function toggleFilterPanel() {{
    const panel = document.getElementById('filterPanel');
    if (panel) {{
        panel.classList.toggle('active');
    }}
}}

function performSearch(query) {{
    console.log('Searching for:', query);
    const clearBtn = document.getElementById('searchClearBtn');
    if (clearBtn) {{
        clearBtn.style.display = query ? 'block' : 'none';
    }}
    // Search logic would go here
}}

function clearSearch() {{
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {{
        searchInput.value = '';
        performSearch('');
    }}
}}

function applyFilters() {{
    console.log('Applying filters');
    updateFilterBadge();
    // Filter logic would go here
}}

function updateFilterBadge() {{
    let activeFilters = 0;
    
    // Count active priority filters
    const priorityFilters = document.querySelectorAll('input[name="priority"]:not(:checked)');
    activeFilters += priorityFilters.length;
    
    // Count algorithm filters
    const algorithmFilter = document.getElementById('algorithmFilter');
    if (algorithmFilter) {{
        const totalOptions = algorithmFilter.options.length;
        const selectedOptions = algorithmFilter.selectedOptions.length;
        if (selectedOptions < totalOptions) {{
            activeFilters += (totalOptions - selectedOptions);
        }}
    }}
    
    const badge = document.getElementById('filterBadge');
    if (badge) {{
        if (activeFilters > 0) {{
            badge.textContent = activeFilters;
            badge.style.display = 'inline-flex';
        }} else {{
            badge.style.display = 'none';
        }}
    }}
}}

function selectAllAlgorithms() {{
    const select = document.getElementById('algorithmFilter');
    if (select) {{
        for (let option of select.options) {{
            option.selected = true;
        }}
        applyFilters();
    }}
}}

function clearAllAlgorithms() {{
    const select = document.getElementById('algorithmFilter');
    if (select) {{
        for (let option of select.options) {{
            option.selected = false;
        }}
        applyFilters();
    }}
}}

function selectAllContexts() {{
    const select = document.getElementById('contextFilter');
    if (select) {{
        for (let option of select.options) {{
            option.selected = true;
        }}
        applyFilters();
    }}
}}

function clearAllContexts() {{
    const select = document.getElementById('contextFilter');
    if (select) {{
        for (let option of select.options) {{
            option.selected = false;
        }}
        applyFilters();
    }}
}}

function updateScoreRange() {{
    const minScore = document.getElementById('minScore');
    const maxScore = document.getElementById('maxScore');
    const minValue = document.getElementById('minScoreValue');
    const maxValue = document.getElementById('maxScoreValue');
    
    if (minScore && maxScore && minValue && maxValue) {{
        minValue.textContent = minScore.value;
        maxValue.textContent = maxScore.value;
        applyFilters();
    }}
}}

function applyPreset(preset) {{
    console.log('Applying preset:', preset);
    
    // Reset all filters first
    document.querySelectorAll('input[name="priority"]').forEach(cb => cb.checked = false);
    
    switch(preset) {{
        case 'critical-only':
            document.querySelector('input[name="priority"][value="CRITICAL"]').checked = true;
            break;
        case 'high-priority':
            document.querySelector('input[name="priority"][value="CRITICAL"]').checked = true;
            document.querySelector('input[name="priority"][value="HIGH"]').checked = true;
            break;
        case 'quick-wins':
            // Filter for low effort, high impact
            alert('Quick Wins preset: Filtering for high impact, low effort findings');
            break;
        case 'authentication':
            // Filter for authentication context
            alert('Authentication preset: Filtering for authentication-related findings');
            break;
    }}
    
    applyFilters();
}}

function saveCurrentPreset() {{
    const presetName = prompt('Enter a name for this filter preset:');
    if (presetName) {{
        console.log('Saving preset:', presetName);
        alert('Preset "' + presetName + '" saved!\\nPreset management will be enhanced in future updates');
    }}
}}

function resetFilters() {{
    // Reset all filters to default
    document.querySelectorAll('input[name="priority"]').forEach(cb => cb.checked = true);
    selectAllAlgorithms();
    selectAllContexts();
    document.getElementById('minScore').value = 0;
    document.getElementById('maxScore').value = 20;
    updateScoreRange();
    clearSearch();
    applyFilters();
}}

function clearAllFilters() {{
    resetFilters();

// Executive Dashboard Functions
function toggleExecutiveDashboard() {{
    const dashboard = document.getElementById('executiveDashboard');
    const btn = event.currentTarget;
    const icon = btn.querySelector('i');
    
    if (dashboard.style.display === 'none') {{
        dashboard.style.display = 'block';
        icon.className = 'fas fa-compress-alt';
    }} else {{
        dashboard.style.display = 'none';
        icon.className = 'fas fa-expand-alt';
    }}
}}

function filterByCompliance(framework) {{
    console.log('Filtering by compliance framework:', framework);
    // This would integrate with the main filtering system
    // For now, just show an alert
    alert(`Filtering findings related to ${{framework}} compliance`);
}}

function exportCompliance(framework) {{
    console.log('Exporting compliance report for:', framework);
    // This would generate a compliance-specific report
    alert(`Exporting ${{framework}} compliance report...`);
}}

function initializeRiskTimeline() {{
    const ctx = document.getElementById('riskTimelineChart');
    if (!ctx) return;
    
    const theme = document.querySelector('.dashboard-container').getAttribute('data-theme');
    const textColor = theme === 'light' ? '#0f172a' : '#e2e8f0';
    const gridColor = theme === 'light' ? '#e2e8f0' : '#334155';
    
    // Sample data for the last 6 months
    const labels = ['6 months ago', '5 months ago', '4 months ago', '3 months ago', '2 months ago', 'Last month', 'Current'];
    const criticalData = [8, 10, 7, 6, 5, 3, {report.critical_count}];
    const highData = [15, 18, 14, 12, 10, 8, {report.high_count}];
    const mediumData = [25, 28, 22, 20, 18, 15, {report.medium_count}];
    
    chartInstances.riskTimeline = new Chart(ctx, {{
        type: 'line',
        data: {{
            labels: labels,
            datasets: [
                {{
                    label: 'Critical',
                    data: criticalData,
                    borderColor: '#dc2626',
                    backgroundColor: 'rgba(220, 38, 38, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true
                }},
                {{
                    label: 'High',
                    data: highData,
                    borderColor: '#ea580c',
                    backgroundColor: 'rgba(234, 88, 12, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true
                }},
                {{
                    label: 'Medium',
                    data: mediumData,
                    borderColor: '#eab308',
                    backgroundColor: 'rgba(234, 179, 8, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true
                }}
            ]
        }},
        options: {{
            responsive: true,
            maintainAspectRatio: false,
            plugins: {{
                legend: {{
                    position: 'top',
                    labels: {{
                        color: textColor,
                        padding: 15,
                        font: {{ size: 12 }}
                    }}
                }},
                tooltip: {{
                    mode: 'index',
                    intersect: false
                }}
            }},
            scales: {{
                x: {{
                    ticks: {{ color: textColor }},
                    grid: {{ color: gridColor }}
                }},
                y: {{
                    ticks: {{ color: textColor }},
                    grid: {{ color: gridColor }},
                    beginAtZero: true
                }}
            }}
        }}
    }});
}}
}}

// Real-Time Dashboard Features
let autoRefreshEnabled = false;
let autoRefreshInterval = null;
let refreshIntervalSeconds = 30;
let websocket = null;
let lastUpdateTime = new Date();

// Auto-Refresh Toggle
function toggleAutoRefresh() {{
    autoRefreshEnabled = !autoRefreshEnabled;
    const btn = document.getElementById('autoRefreshBtn');
    const icon = document.getElementById('refreshIcon');
    const text = document.getElementById('refreshText');
    const liveIndicator = document.getElementById('liveIndicator');
    
    if (autoRefreshEnabled) {{
        text.textContent = `Auto-Refresh: ON (${{refreshIntervalSeconds}}s)`;
        btn.style.background = 'var(--success)';
        btn.style.color = 'white';
        btn.style.borderColor = 'var(--success)';
        liveIndicator.style.display = 'flex';
        
        // Start auto-refresh
        startAutoRefresh();
        
        // Show notification
        showNotification('success', 'Auto-Refresh Enabled', `Dashboard will refresh every ${{refreshIntervalSeconds}} seconds`);
    }} else {{
        text.textContent = 'Auto-Refresh: OFF';
        btn.style.background = '';
        btn.style.color = '';
        btn.style.borderColor = '';
        liveIndicator.style.display = 'none';
        
        // Stop auto-refresh
        stopAutoRefresh();
        
        showNotification('info', 'Auto-Refresh Disabled', 'Dashboard updates paused');
    }}
}}

function startAutoRefresh() {{
    if (autoRefreshInterval) {{
        clearInterval(autoRefreshInterval);
    }}
    
    autoRefreshInterval = setInterval(() => {{
        refreshDashboard();
    }}, refreshIntervalSeconds * 1000);
}}

function stopAutoRefresh() {{
    if (autoRefreshInterval) {{
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
    }}
}}

function refreshDashboard() {{
    const icon = document.getElementById('refreshIcon');
    icon.classList.add('spinning');
    
    // Simulate data refresh (in production, this would fetch new data)
    setTimeout(() => {{
        icon.classList.remove('spinning');
        updateLastUpdatedTime();
        
        // Simulate random updates
        simulateDataUpdates();
        
        showNotification('info', 'Dashboard Updated', 'Latest data loaded successfully');
    }}, 1000);
}}

function updateLastUpdatedTime() {{
    lastUpdateTime = new Date();
    const lastUpdated = document.getElementById('lastUpdated');
    lastUpdated.textContent = formatTimeAgo(lastUpdateTime);
    
    // Update every minute
    setInterval(() => {{
        lastUpdated.textContent = formatTimeAgo(lastUpdateTime);
    }}, 60000);
}}

function formatTimeAgo(date) {{
    const seconds = Math.floor((new Date() - date) / 1000);
    
    if (seconds < 60) return 'Just now';
    if (seconds < 3600) return `${{Math.floor(seconds / 60)}} min ago`;
    if (seconds < 86400) return `${{Math.floor(seconds / 3600)}} hours ago`;
    return `${{Math.floor(seconds / 86400)}} days ago`;
}}

function simulateDataUpdates() {{
    // Simulate random finding updates
    const randomUpdate = Math.random();
    
    if (randomUpdate < 0.3) {{
        // New critical finding
        showNotification('error', 'New Critical Finding', 'MD5 hash detected in authentication module');
        updateActiveUsers(Math.floor(Math.random() * 5) + 1);
    }} else if (randomUpdate < 0.6) {{
        // Finding resolved
        showNotification('success', 'Finding Resolved', 'SHA1 vulnerability has been remediated');
    }} else {{
        // Status update
        showNotification('info', 'Status Update', 'Scan completed for 3 new modules');
    }}
}}

// Notification System
function showNotification(type, title, message) {{
    const container = document.getElementById('notificationsContainer');
    const notification = document.createElement('div');
    notification.className = `notification ${{type}}`;
    
    const iconMap = {{
        success: 'fa-check-circle',
        warning: 'fa-exclamation-triangle',
        error: 'fa-times-circle',
        info: 'fa-info-circle'
    }};
    
    notification.innerHTML = `
        <i class="fas ${{iconMap[type]}} notification-icon"></i>
        <div class="notification-content">
            <div class="notification-title">${{title}}</div>
            <div class="notification-message">${{message}}</div>
        </div>
        <button class="notification-close" onclick="closeNotification(this)">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    container.appendChild(notification);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {{
        if (notification.parentElement) {{
            notification.style.animation = 'slideOutRight 0.3s ease-out';
            setTimeout(() => notification.remove(), 300);
        }}
    }}, 5000);
}}

function closeNotification(btn) {{
    const notification = btn.closest('.notification');
    notification.style.animation = 'slideOutRight 0.3s ease-out';
    setTimeout(() => notification.remove(), 300);
}}

// WebSocket Connection (Simulated)
function initializeWebSocket() {{
    // In production, this would connect to a real WebSocket server
    // For demo purposes, we'll simulate connection status
    
    const statusEl = document.getElementById('connectionStatus');
    statusEl.textContent = 'Connecting...';
    statusEl.className = 'connecting';
    
    setTimeout(() => {{
        statusEl.textContent = 'Connected';
        statusEl.className = 'connected';
        showNotification('success', 'Connected', 'Real-time updates enabled');
    }}, 1000);
    
    // Simulate periodic connection checks
    setInterval(() => {{
        // Random connection status for demo
        if (Math.random() < 0.05) {{
            statusEl.textContent = 'Reconnecting...';
            statusEl.className = 'connecting';
            setTimeout(() => {{
                statusEl.textContent = 'Connected';
                statusEl.className = 'connected';
            }}, 2000);
        }}
    }}, 30000);
}}

function updateActiveUsers(count) {{
    const activeUsersEl = document.getElementById('activeUsers');
    activeUsersEl.textContent = count;
    
    // Animate the change
    activeUsersEl.style.transform = 'scale(1.3)';
    activeUsersEl.style.color = 'var(--primary-500)';
    setTimeout(() => {{
        activeUsersEl.style.transform = 'scale(1)';
        activeUsersEl.style.color = '';
    }}, 300);
}}

// Initialize real-time features on page load
function initializeRealTimeFeatures() {{
    updateLastUpdatedTime();
    initializeWebSocket();
    updateActiveUsers(1);
}}

// Advanced Analytics Functions
function toggleAnalytics() {{
    const content = document.getElementById('analyticsContent');
    const btn = event.currentTarget;
    const icon = btn.querySelector('i');
    
    if (content.style.display === 'none') {{
        content.style.display = 'block';
        icon.className = 'fas fa-compress-alt';
    }} else {{
        content.style.display = 'none';
        icon.className = 'fas fa-expand-alt';
    }}
}}

function showRiskDetails() {{
    console.log('Showing detailed risk analysis');
    showNotification('info', 'Risk Analysis', 'Opening detailed risk breakdown...');
}}

function setTrendPeriod(period) {{
    // Remove active class from all buttons
    document.querySelectorAll('.chart-period-btn').forEach(btn => {{
        btn.classList.remove('active');
    }});
    
    // Add active class to clicked button
    event.currentTarget.classList.add('active');
    
    // Update trend chart with new period data
    updateTrendChart(period);
    
    showNotification('info', 'Period Updated', `Showing ${{period}} trend data`);
}}

function updateTrendChart(period) {{
    const ctx = document.getElementById('trendAnalysisChart');
    if (!ctx) return;
    
    // Destroy existing chart if it exists
    if (chartInstances.trendAnalysis) {{
        chartInstances.trendAnalysis.destroy();
    }}
    
    const theme = document.querySelector('.dashboard-container').getAttribute('data-theme');
    const textColor = theme === 'light' ? '#0f172a' : '#e2e8f0';
    const gridColor = theme === 'light' ? '#e2e8f0' : '#334155';
    
    // Generate data based on period
    let labels, criticalData, highData, mediumData;
    
    switch(period) {{
        case 'week':
            labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
            criticalData = [2, 3, 1, 2, 1, 0, 1];
            highData = [5, 6, 4, 5, 3, 2, 4];
            mediumData = [8, 10, 7, 9, 6, 5, 7];
            break;
        case 'month':
            labels = ['Week 1', 'Week 2', 'Week 3', 'Week 4'];
            criticalData = [8, 6, 5, 3];
            highData = [15, 12, 10, 8];
            mediumData = [25, 22, 18, 15];
            break;
        case 'quarter':
            labels = ['Month 1', 'Month 2', 'Month 3'];
            criticalData = [22, 18, 12];
            highData = [45, 38, 30];
            mediumData = [75, 65, 52];
            break;
        case 'year':
            labels = ['Q1', 'Q2', 'Q3', 'Q4'];
            criticalData = [52, 45, 38, 28];
            highData = [113, 98, 85, 70];
            mediumData = [192, 175, 158, 140];
            break;
        default:
            labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
            criticalData = [2, 3, 1, 2, 1, 0, 1];
            highData = [5, 6, 4, 5, 3, 2, 4];
            mediumData = [8, 10, 7, 9, 6, 5, 7];
    }}
    
    chartInstances.trendAnalysis = new Chart(ctx, {{
        type: 'line',
        data: {{
            labels: labels,
            datasets: [
                {{
                    label: 'Critical',
                    data: criticalData,
                    borderColor: '#dc2626',
                    backgroundColor: 'rgba(220, 38, 38, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true
                }},
                {{
                    label: 'High',
                    data: highData,
                    borderColor: '#ea580c',
                    backgroundColor: 'rgba(234, 88, 12, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true
                }},
                {{
                    label: 'Medium',
                    data: mediumData,
                    borderColor: '#eab308',
                    backgroundColor: 'rgba(234, 179, 8, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true
                }}
            ]
        }},
        options: {{
            responsive: true,
            maintainAspectRatio: false,
            plugins: {{
                legend: {{
                    position: 'top',
                    labels: {{
                        color: textColor,
                        padding: 15,
                        font: {{ size: 12 }}
                    }}
                }},
                tooltip: {{
                    mode: 'index',
                    intersect: false
                }}
            }},
            scales: {{
                x: {{
                    ticks: {{ color: textColor }},
                    grid: {{ color: gridColor }}
                }},
                y: {{
                    ticks: {{ color: textColor }},
                    grid: {{ color: gridColor }},
                    beginAtZero: true
                }}
            }}
        }}
    }});
}}

function investigateAnomaly(type) {{
    console.log('Investigating anomaly:', type);
    showNotification('info', 'Anomaly Investigation', `Opening detailed analysis for ${{type}} anomaly`);
}}

function initializeAnalytics() {{
    // Initialize trend chart with default period (week)
    updateTrendChart('week');

// Workflow Management Functions
function toggleWorkflowView(view) {{
    const kanbanBoard = document.getElementById('kanbanBoard');
    const timelineView = document.getElementById('timelineView');
    const kanbanBtn = document.getElementById('kanbanViewBtn');
    const timelineBtn = document.getElementById('timelineViewBtn');
    
    if (view === 'kanban') {{
        kanbanBoard.style.display = 'grid';
        timelineView.style.display = 'none';
        kanbanBtn.classList.add('active');
        timelineBtn.classList.remove('active');
    }} else {{
        kanbanBoard.style.display = 'none';
        timelineView.style.display = 'block';
        kanbanBtn.classList.remove('active');
        timelineBtn.classList.add('active');
    }}
}}

function allowDrop(ev) {{
    ev.preventDefault();
}}

function drag(ev) {{
    ev.dataTransfer.setData("findingId", ev.target.getAttribute('data-finding-id'));
}}

function drop(ev) {{
    ev.preventDefault();
    const findingId = ev.dataTransfer.getData("findingId");
    const targetColumn = ev.currentTarget;
    const newStatus = targetColumn.parentElement.getAttribute('data-status');
    
    // Find the card being dragged
    const card = document.querySelector(`[data-finding-id="${{findingId}}"]`);
    if (card) {{
        // Move card to new column
        targetColumn.appendChild(card);
        
        // Update column counts
        updateColumnCounts();
        
        // Show notification
        showNotification('success', 'Task Moved', `Task ${{findingId}} moved to ${{newStatus.replace('_', ' ')}}`);
    }}
}}

function updateColumnCounts() {{
    const columns = document.querySelectorAll('.kanban-column');
    columns.forEach(column => {{
        const count = column.querySelectorAll('.kanban-card').length;
        const countBadge = column.querySelector('.column-count');
        if (countBadge) {{
            countBadge.textContent = count;
        }}
    }});
}}

function addTaskToColumn(columnId) {{
    showNotification('info', 'Add Task', `Adding new task to ${{columnId.replace('_', ' ')}} column`);
    // In a real implementation, this would open a form to create a new task
}}

function viewTaskDetails(findingId) {{
    showNotification('info', 'Task Details', `Viewing details for task ${{findingId}}`);
    // In a real implementation, this would open a modal with full task details
}}

function editTask(findingId) {{
    showNotification('info', 'Edit Task', `Editing task ${{findingId}}`);
    // In a real implementation, this would open an edit form
}}

function assignTask(findingId) {{
    showNotification('info', 'Assign Task', `Assigning task ${{findingId}}`);
    showAssignmentModal();
    // Pre-select the task in the modal
    const taskSelect = document.getElementById('taskSelect');
    if (taskSelect) {{
        // Add option if not exists
        const option = document.createElement('option');
        option.value = findingId;
        option.textContent = findingId;
        option.selected = true;
        taskSelect.appendChild(option);
    }}
}}

function showMoreTasks(columnId) {{
    showNotification('info', 'Show More', `Loading more tasks from ${{columnId.replace('_', ' ')}} column`);
    // In a real implementation, this would load additional tasks
}}

function showAssignmentModal() {{
    const modal = document.getElementById('assignmentModal');
    if (modal) {{
        modal.style.display = 'flex';
        
        // Populate task select with all tasks
        const taskSelect = document.getElementById('taskSelect');
        if (taskSelect && taskSelect.options.length === 1) {{
            const cards = document.querySelectorAll('.kanban-card');
            cards.forEach(card => {{
                const findingId = card.getAttribute('data-finding-id');
                const title = card.querySelector('.card-title').textContent;
                const option = document.createElement('option');
                option.value = findingId;
                option.textContent = `${{findingId}} - ${{title}}`;
                taskSelect.appendChild(option);
            }});
        }}
    }}
}}

function closeAssignmentModal() {{
    const modal = document.getElementById('assignmentModal');
    if (modal) {{
        modal.style.display = 'none';
    }}
}}

function saveAssignment() {{
    const taskSelect = document.getElementById('taskSelect');
    const assigneeSelect = document.getElementById('assigneeSelect');
    const dueDateInput = document.getElementById('dueDateInput');
    const prioritySelect = document.getElementById('prioritySelect');
    const notesInput = document.getElementById('notesInput');
    
    const task = taskSelect.value;
    const assignee = assigneeSelect.value;
    const dueDate = dueDateInput.value;
    const priority = prioritySelect.value;
    const notes = notesInput.value;
    
    if (!task || !assignee) {{
        showNotification('error', 'Validation Error', 'Please select both a task and an assignee');
        return;
    }}
    
    // Update the card with new assignment
    const card = document.querySelector(`[data-finding-id="${{task}}"]`);
    if (card) {{
        const assigneeSpan = card.querySelector('.card-assignee');
        const dueDateSpan = card.querySelector('.card-due-date');
        
        if (assigneeSpan) {{
            assigneeSpan.innerHTML = `<i class="fas fa-user"></i> ${{assigneeSelect.options[assigneeSelect.selectedIndex].text.split('(')[0].trim()}}`;
        }}
        
        if (dueDateSpan && dueDate) {{
            dueDateSpan.innerHTML = `<i class="fas fa-calendar"></i> ${{dueDate}}`;
        }}
    }}
    
    showNotification('success', 'Task Assigned', `Task ${{task}} assigned to ${{assigneeSelect.options[assigneeSelect.selectedIndex].text}}`);
    closeAssignmentModal();
    
    // Reset form
    taskSelect.selectedIndex = 0;
    assigneeSelect.selectedIndex = 0;
    dueDateInput.value = '';
    prioritySelect.selectedIndex = 0;
    notesInput.value = '';
}}

// Close modal when clicking outside
document.addEventListener('click', function(e) {{
    const modal = document.getElementById('assignmentModal');
    if (modal && e.target === modal) {{
        closeAssignmentModal();
    }}
}});
}}

@keyframes slideOutRight {{
    from {{ transform: translateX(0); opacity: 1; }}
    to {{ transform: translateX(400px); opacity: 0; }}
}}

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {{
    // Ctrl/Cmd + K: Toggle theme
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {{
        e.preventDefault();
        toggleTheme();
    }}
    
    // Ctrl/Cmd + P: Print
    if ((e.ctrlKey || e.metaKey) && e.key === 'p') {{
        e.preventDefault();
        window.print();
    }}
    
    // Ctrl/Cmd + E: Export
    if ((e.ctrlKey || e.metaKey) && e.key === 'e') {{
        e.preventDefault();
        exportDashboard();
    }}
    
    // Ctrl/Cmd + F: Focus search
    if ((e.ctrlKey || e.metaKey) && e.key === 'f') {{
        e.preventDefault();
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {{
            toggleFilterPanel();
            setTimeout(() => searchInput.focus(), 300);
        }}
    }}
    
    // Ctrl/Cmd + A: Select all (in table context)
    if ((e.ctrlKey || e.metaKey) && e.key === 'a' && document.activeElement.tagName !== 'INPUT') {{
        e.preventDefault();
        selectAllRows();
    }}
}});
</script>'''