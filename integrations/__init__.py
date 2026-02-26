"""
Integration plugins for external security tools.
Auto-pull findings and enrich with repository context.
"""

from .base_plugin import BasePlugin
from .github_plugin import GitHubPlugin
from .sonarqube_plugin import SonarQubePlugin
from .semgrep_plugin import SemgrepPlugin
from .snyk_plugin import SnykPlugin

__all__ = [
    'BasePlugin',
    'GitHubPlugin',
    'SonarQubePlugin',
    'SemgrepPlugin',
    'SnykPlugin'
]