"""
GitHub Security Advisories integration plugin.
Fetches security advisories and Dependabot alerts.
"""

import re
import time
from typing import List, Optional, Dict, Any
from datetime import datetime
from .base_plugin import BasePlugin
from ..models import CryptoFinding, CryptoAlgorithm, Severity, UsageContext

try:
    from github import Github, GithubException
    from github.Repository import Repository
    GITHUB_AVAILABLE = True
except ImportError:
    GITHUB_AVAILABLE = False


# CVE to Crypto Algorithm mapping patterns
CVE_CRYPTO_PATTERNS = {
    CryptoAlgorithm.MD5: [
        r'md5', r'message.?digest.?5', r'cve-\d{4}-\d+.*md5'
    ],
    CryptoAlgorithm.SHA1: [
        r'sha.?1', r'sha-1', r'secure.?hash.?algorithm.?1', r'cve-\d{4}-\d+.*sha.?1'
    ],
    CryptoAlgorithm.DES: [
        r'\bdes\b', r'data.?encryption.?standard', r'cve-\d{4}-\d+.*\bdes\b'
    ],
    CryptoAlgorithm.TRIPLE_DES: [
        r'3des', r'triple.?des', r'tdea', r'cve-\d{4}-\d+.*3des'
    ],
    CryptoAlgorithm.RC4: [
        r'rc4', r'arcfour', r'arc4', r'cve-\d{4}-\d+.*rc4'
    ],
    CryptoAlgorithm.RSA_1024: [
        r'rsa.?1024', r'1024.?bit.?rsa', r'cve-\d{4}-\d+.*rsa.*1024'
    ],
    CryptoAlgorithm.RSA_2048: [
        r'rsa.?2048', r'2048.?bit.?rsa', r'cve-\d{4}-\d+.*rsa.*2048'
    ],
}

# Crypto-related keywords for filtering
CRYPTO_KEYWORDS = [
    'crypto', 'encryption', 'cipher', 'hash', 'digest', 'ssl', 'tls',
    'certificate', 'key', 'rsa', 'aes', 'des', 'md5', 'sha', 'rc4',
    'ecdsa', 'signature', 'signing', 'authentication'
]


class GitHubPlugin(BasePlugin):
    """
    GitHub Security Advisories integration.
    
    Features:
    - Fetch security advisories via GraphQL API
    - Pull Dependabot alerts via REST API
    - Enrich with commit context
    - Map CVEs to crypto algorithms
    - Rate limiting and error handling
    
    Configuration:
        token: GitHub personal access token (required)
        repositories: List of repos to scan in owner/repo format (optional)
        include_dependabot: Whether to fetch Dependabot alerts (default: True)
        include_advisories: Whether to fetch security advisories (default: True)
        max_retries: Maximum retry attempts for API calls (default: 3)
        retry_delay: Delay between retries in seconds (default: 2)
    """
    
    def __init__(self, config: Dict[str, Any]):
        """Initialize GitHub plugin with configuration."""
        super().__init__(config)
        self.max_retries = config.get('max_retries', 3)
        self.retry_delay = config.get('retry_delay', 2)
        self.include_dependabot = config.get('include_dependabot', True)
        self.include_advisories = config.get('include_advisories', True)
    
    def authenticate(self) -> bool:
        """Authenticate with GitHub API."""
        if not GITHUB_AVAILABLE:
            raise ImportError("PyGithub not installed. Run: pip install PyGithub")
        
        try:
            token = self.config.get('token')
            if not token:
                print("GitHub token not provided in configuration")
                return False
            
            self.client = Github(token)
            # Test authentication by getting user info
            user = self.client.get_user()
            user.login  # This will raise exception if auth fails
            self.authenticated = True
            print(f"GitHub authentication successful for user: {user.login}")
            return True
        except Exception as e:
            print(f"GitHub authentication failed: {e}")
            self.authenticated = False
            return False
    
    def fetch_findings(
        self,
        repository: Optional[str] = None,
        project: Optional[str] = None,
        **kwargs
    ) -> List[CryptoFinding]:
        """
        Fetch crypto-related findings from GitHub.
        
        Args:
            repository: Repository in owner/repo format
            project: Alias for repository (ignored if repository provided)
            **kwargs: Additional parameters
            
        Returns:
            List of CryptoFinding objects
        """
        if not self.authenticated:
            if not self.authenticate():
                return []
        
        findings = []
        repos = [repository] if repository else self.config.get('repositories', [])
        
        if not repos:
            print("No repositories specified for scanning")
            return []
        
        for repo_name in repos:
            try:
                print(f"Fetching findings from {repo_name}...")
                repo = self._get_repo_with_retry(repo_name)
                
                if self.include_dependabot:
                    dependabot_findings = self._fetch_dependabot_alerts(repo, repo_name)
                    findings.extend(dependabot_findings)
                    print(f"Found {len(dependabot_findings)} Dependabot alerts")
                
                if self.include_advisories:
                    advisory_findings = self._fetch_security_advisories(repo, repo_name)
                    findings.extend(advisory_findings)
                    print(f"Found {len(advisory_findings)} security advisories")
                
            except GithubException as e:
                print(f"GitHub API error for {repo_name}: {e.status} - {e.data.get('message', str(e))}")
            except Exception as e:
                print(f"Error fetching from {repo_name}: {e}")
        
        print(f"Total crypto-related findings: {len(findings)}")
        return findings
    
    def _get_repo_with_retry(self, repo_name: str) -> Repository:
        """Get repository with retry logic."""
        for attempt in range(self.max_retries):
            try:
                return self.client.get_repo(repo_name)
            except GithubException as e:
                if e.status == 403 and 'rate limit' in str(e).lower():
                    if attempt < self.max_retries - 1:
                        print(f"Rate limit hit, waiting {self.retry_delay}s...")
                        time.sleep(self.retry_delay)
                        continue
                raise
        raise Exception(f"Failed to get repository after {self.max_retries} attempts")
    
    def _fetch_dependabot_alerts(self, repo: Repository, repo_name: str) -> List[CryptoFinding]:
        """
        Fetch Dependabot alerts from repository.
        
        Note: Requires 'security_events' scope on the token.
        """
        findings = []
        
        try:
            # Use PyGithub's get_dependabot_alerts if available
            # This requires PyGithub >= 1.59
            if hasattr(repo, 'get_dependabot_alerts'):
                alerts = repo.get_dependabot_alerts(state='open')
                
                for alert in alerts:
                    # Filter for crypto-related vulnerabilities
                    if not self._is_crypto_related(alert.security_advisory.summary,
                                                   alert.security_advisory.description):
                        continue
                    
                    finding = self._create_finding_from_dependabot(alert, repo_name)
                    if finding:
                        findings.append(finding)
            else:
                print(f"Dependabot alerts not available for {repo_name} (requires PyGithub >= 1.59)")
        
        except GithubException as e:
            if e.status == 404:
                print(f"Dependabot alerts not accessible for {repo_name} (may require permissions)")
            else:
                print(f"Error fetching Dependabot alerts: {e}")
        except AttributeError:
            print(f"Dependabot API not available in this PyGithub version")
        except Exception as e:
            print(f"Unexpected error fetching Dependabot alerts: {e}")
        
        return findings
    
    def _fetch_security_advisories(self, repo: Repository, repo_name: str) -> List[CryptoFinding]:
        """
        Fetch security advisories using GraphQL API.
        
        Note: This uses the repository's vulnerability alerts.
        """
        findings = []
        
        try:
            # Get vulnerability alerts (requires appropriate permissions)
            if hasattr(repo, 'get_vulnerability_alert'):
                # This is a placeholder - actual implementation would use GraphQL
                # to fetch security advisories with crypto-related CVEs
                pass
            
            # Alternative: Scan through issues with security labels
            issues = repo.get_issues(state='open', labels=['security'])
            
            for issue in issues:
                if not self._is_crypto_related(issue.title, issue.body or ''):
                    continue
                
                finding = self._create_finding_from_issue(issue, repo_name)
                if finding:
                    findings.append(finding)
        
        except GithubException as e:
            if e.status == 404:
                print(f"Security advisories not accessible for {repo_name}")
            else:
                print(f"Error fetching security advisories: {e}")
        except Exception as e:
            print(f"Unexpected error fetching security advisories: {e}")
        
        return findings
    
    def _is_crypto_related(self, title: str, description: str) -> bool:
        """Check if text contains crypto-related keywords."""
        text = f"{title} {description}".lower()
        return any(keyword in text for keyword in CRYPTO_KEYWORDS)
    
    def _create_finding_from_dependabot(self, alert: Any, repo_name: str) -> Optional[CryptoFinding]:
        """Create CryptoFinding from Dependabot alert."""
        try:
            advisory = alert.security_advisory
            vulnerability = alert.security_vulnerability
            
            # Map CVE to crypto algorithm
            algorithm = self._map_cve_to_algorithm(
                advisory.cve_id or '',
                advisory.summary,
                advisory.description
            )
            
            # Map severity
            severity_map = {
                'critical': Severity.CRITICAL,
                'high': Severity.HIGH,
                'medium': Severity.MEDIUM,
                'low': Severity.LOW,
            }
            severity = severity_map.get(advisory.severity.lower(), Severity.MEDIUM)
            
            # Determine usage context from package ecosystem
            usage_context = self._infer_usage_context(
                vulnerability.package.ecosystem,
                advisory.summary
            )
            
            finding = CryptoFinding(
                id=f"GITHUB-DEPENDABOT-{repo_name.replace('/', '-')}-{alert.number}",
                title=f"{advisory.summary} in {vulnerability.package.name}",
                description=advisory.description,
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=alert.dependency.manifest_path if hasattr(alert.dependency, 'manifest_path') else None
            )
            
            # Add metadata
            finding.metadata.update({
                'source': 'github_dependabot',
                'repository': repo_name,
                'alert_number': alert.number,
                'alert_url': alert.html_url,
                'package_name': vulnerability.package.name,
                'package_ecosystem': vulnerability.package.ecosystem,
                'vulnerable_version': vulnerability.vulnerable_version_range,
                'patched_version': vulnerability.first_patched_version.identifier if vulnerability.first_patched_version else None,
                'cve_id': advisory.cve_id,
                'ghsa_id': advisory.ghsa_id,
                'published_at': advisory.published_at.isoformat() if advisory.published_at else None,
            })
            
            if advisory.cve_id:
                finding.cwe_ids.append(advisory.cve_id)
            if advisory.references:
                finding.references.extend([ref.url for ref in advisory.references])
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from Dependabot alert: {e}")
            return None
    
    def _create_finding_from_issue(self, issue: Any, repo_name: str) -> Optional[CryptoFinding]:
        """Create CryptoFinding from GitHub issue."""
        try:
            # Extract CVE from issue body if present
            cve_pattern = r'CVE-\d{4}-\d+'
            cves = re.findall(cve_pattern, issue.body or '', re.IGNORECASE)
            
            # Map to crypto algorithm
            algorithm = self._map_cve_to_algorithm(
                cves[0] if cves else '',
                issue.title,
                issue.body or ''
            )
            
            finding = CryptoFinding(
                id=f"GITHUB-ISSUE-{repo_name.replace('/', '-')}-{issue.number}",
                title=issue.title,
                description=issue.body or 'No description provided',
                severity=Severity.MEDIUM,  # Default for issues
                algorithm=algorithm,
                usage_context=UsageContext.UNKNOWN
            )
            
            finding.metadata.update({
                'source': 'github_issue',
                'repository': repo_name,
                'issue_number': issue.number,
                'issue_url': issue.html_url,
                'state': issue.state,
                'created_at': issue.created_at.isoformat(),
                'labels': [label.name for label in issue.labels],
            })
            
            if cves:
                finding.cwe_ids.extend(cves)
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from issue: {e}")
            return None
    
    def _map_cve_to_algorithm(self, cve_id: str, title: str, description: str) -> CryptoAlgorithm:
        """Map CVE and text to crypto algorithm."""
        text = f"{cve_id} {title} {description}".lower()
        
        # Check each algorithm's patterns
        for algorithm, patterns in CVE_CRYPTO_PATTERNS.items():
            for pattern in patterns:
                if re.search(pattern, text, re.IGNORECASE):
                    return algorithm
        
        return CryptoAlgorithm.UNKNOWN
    
    def _infer_usage_context(self, ecosystem: str, summary: str) -> UsageContext:
        """Infer usage context from package ecosystem and summary."""
        text = f"{ecosystem} {summary}".lower()
        
        if any(word in text for word in ['auth', 'login', 'session', 'token', 'jwt']):
            return UsageContext.AUTHENTICATION
        elif any(word in text for word in ['key', 'secret', 'credential', 'password']):
            return UsageContext.KEY_STORAGE
        elif any(word in text for word in ['encrypt', 'decrypt', 'cipher']):
            return UsageContext.DATA_ENCRYPTION
        elif any(word in text for word in ['sign', 'signature', 'verify']):
            return UsageContext.SIGNING
        elif any(word in text for word in ['hash', 'digest', 'checksum']):
            return UsageContext.HASHING
        
        return UsageContext.UNKNOWN
    
    def enrich_context(
        self,
        finding: CryptoFinding,
        repository: Optional[str] = None
    ) -> CryptoFinding:
        """
        Enrich finding with GitHub repository context.
        
        Args:
            finding: CryptoFinding to enrich
            repository: Repository name
            
        Returns:
            Enriched CryptoFinding
        """
        if not repository:
            repository = finding.metadata.get('repository')
        
        if not repository or not self.authenticated:
            return finding
        
        try:
            repo = self._get_repo_with_retry(repository)
            
            # Add repository metadata
            finding.metadata['repo_stars'] = repo.stargazers_count
            finding.metadata['repo_forks'] = repo.forks_count
            finding.metadata['repo_watchers'] = repo.watchers_count
            finding.metadata['repo_language'] = repo.language
            finding.metadata['repo_open_issues'] = repo.open_issues_count
            finding.metadata['repo_size'] = repo.size
            finding.metadata['repo_created_at'] = repo.created_at.isoformat()
            finding.metadata['repo_updated_at'] = repo.updated_at.isoformat()
            finding.metadata['repo_default_branch'] = repo.default_branch
            
            # Determine if external facing based on repo visibility
            finding.is_external_facing = not repo.private
            
            # Add commit context if file path available
            if finding.file_path:
                try:
                    commits = repo.get_commits(path=finding.file_path)
                    if commits.totalCount > 0:
                        latest = commits[0]
                        finding.metadata['file_last_modified'] = latest.commit.author.date.isoformat()
                        finding.metadata['file_last_author'] = latest.commit.author.name
                        finding.metadata['file_last_commit_sha'] = latest.sha
                        finding.metadata['file_last_commit_message'] = latest.commit.message
                        
                        # Get file content to extract code snippet
                        try:
                            file_content = repo.get_contents(finding.file_path, ref=latest.sha)
                            if hasattr(file_content, 'decoded_content'):
                                content = file_content.decoded_content.decode('utf-8')
                                lines = content.split('\n')
                                
                                # Extract snippet around line number if available
                                if finding.line_number and 0 < finding.line_number <= len(lines):
                                    start = max(0, finding.line_number - 3)
                                    end = min(len(lines), finding.line_number + 2)
                                    finding.code_snippet = '\n'.join(lines[start:end])
                        except:
                            pass
                except Exception as e:
                    print(f"Error fetching commit history: {e}")
            
            # Add contributor information
            try:
                contributors = repo.get_contributors()
                if contributors.totalCount > 0:
                    finding.metadata['repo_contributors_count'] = contributors.totalCount
                    top_contributors = list(contributors[:5])
                    finding.metadata['repo_top_contributors'] = [c.login for c in top_contributors]
            except:
                pass
            
        except GithubException as e:
            print(f"GitHub API error enriching context: {e.status} - {e.data.get('message', str(e))}")
        except Exception as e:
            print(f"Error enriching context: {e}")
        
        return finding