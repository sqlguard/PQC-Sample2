"""
SonarQube integration plugin.
Fetches security hotspots and vulnerabilities from SonarQube.
"""

import re
import time
from typing import List, Optional, Dict, Any
from .base_plugin import BasePlugin
from ..models import CryptoFinding, CryptoAlgorithm, Severity, UsageContext

try:
    import requests
    REQUESTS_AVAILABLE = True
except ImportError:
    REQUESTS_AVAILABLE = False


# SonarQube rule key to crypto algorithm mapping
SONARQUBE_RULE_PATTERNS = {
    CryptoAlgorithm.MD5: [
        r'md5', r'message.?digest.?5', r'insecure.*hash.*md5',
        r'weak.*hash.*md5', r'crypto.*md5'
    ],
    CryptoAlgorithm.SHA1: [
        r'sha.?1', r'sha-1', r'insecure.*hash.*sha1',
        r'weak.*hash.*sha1', r'crypto.*sha1'
    ],
    CryptoAlgorithm.DES: [
        r'\bdes\b', r'data.?encryption.?standard',
        r'insecure.*cipher.*des', r'weak.*des'
    ],
    CryptoAlgorithm.TRIPLE_DES: [
        r'3des', r'triple.?des', r'tdea',
        r'insecure.*3des'
    ],
    CryptoAlgorithm.RC4: [
        r'rc4', r'arcfour', r'arc4',
        r'insecure.*rc4', r'weak.*cipher.*rc4'
    ],
    CryptoAlgorithm.RSA_1024: [
        r'rsa.*1024', r'1024.*bit.*rsa',
        r'weak.*rsa.*key', r'insufficient.*key.*size'
    ],
}

# Crypto-related keywords
CRYPTO_KEYWORDS = [
    'crypto', 'encryption', 'cipher', 'hash', 'digest', 'ssl', 'tls',
    'certificate', 'key', 'rsa', 'aes', 'des', 'md5', 'sha', 'rc4',
    'ecdsa', 'signature', 'signing', 'random', 'secure'
]


class SonarQubePlugin(BasePlugin):
    """
    SonarQube integration plugin.
    
    Features:
    - Fetch security hotspots from SonarQube
    - Fetch vulnerabilities and code smells
    - Map rule keys to crypto algorithms
    - Enrich with code quality metrics
    - Support for multiple SonarQube versions
    
    Configuration:
        url: SonarQube server URL (required)
        token: Authentication token (required)
        project_key: Project key to scan (optional)
        branch: Branch name (optional, default: main)
        organization: Organization key (optional, for SonarCloud)
        include_hotspots: Include security hotspots (default: True)
        include_vulnerabilities: Include vulnerabilities (default: True)
        include_code_smells: Include code smells (default: False)
        severity_threshold: Minimum severity (default: 'MINOR')
        max_retries: Maximum retry attempts (default: 3)
        retry_delay: Delay between retries in seconds (default: 2)
    """
    
    def __init__(self, config: Dict[str, Any]):
        """Initialize SonarQube plugin with configuration."""
        super().__init__(config)
        self.url = config.get('url', '').rstrip('/')
        self.token = config.get('token')
        self.project_key = config.get('project_key')
        self.branch = config.get('branch', 'main')
        self.organization = config.get('organization')
        self.include_hotspots = config.get('include_hotspots', True)
        self.include_vulnerabilities = config.get('include_vulnerabilities', True)
        self.include_code_smells = config.get('include_code_smells', False)
        self.severity_threshold = config.get('severity_threshold', 'MINOR')
        self.max_retries = config.get('max_retries', 3)
        self.retry_delay = config.get('retry_delay', 2)
    
    def authenticate(self) -> bool:
        """
        Authenticate with SonarQube API.
        
        Returns:
            True if authentication successful
        """
        if not self.url or not self.token:
            print("SonarQube URL and token are required")
            return False
        
        if not REQUESTS_AVAILABLE:
            raise ImportError("requests library not installed. Run: pip install requests")
        
        try:
            # Test authentication by fetching server version
            response = requests.get(
                f'{self.url}/api/server/version',
                auth=(self.token, ''),
                timeout=10
            )
            
            if response.status_code == 200:
                version = response.text.strip()
                self.authenticated = True
                print(f"SonarQube authentication successful. Server version: {version}")
                return True
            else:
                print(f"SonarQube authentication failed: {response.status_code}")
                return False
        
        except Exception as e:
            print(f"SonarQube authentication error: {e}")
            return False
    
    def fetch_findings(
        self,
        repository: Optional[str] = None,
        project: Optional[str] = None,
        **kwargs
    ) -> List[CryptoFinding]:
        """
        Fetch crypto-related findings from SonarQube.
        
        Args:
            repository: Repository name (not used for SonarQube)
            project: Project key (overrides config)
            **kwargs: Additional parameters
                - branch: Branch name
                - organization: Organization key
            
        Returns:
            List of CryptoFinding objects
        """
        if not self.authenticated:
            if not self.authenticate():
                return []
        
        findings = []
        project_key = project or self.project_key
        
        if not project_key:
            print("No project key specified")
            return []
        
        branch = kwargs.get('branch', self.branch)
        organization = kwargs.get('organization', self.organization)
        
        try:
            # Fetch security hotspots
            if self.include_hotspots:
                hotspot_findings = self._fetch_security_hotspots(
                    project_key, branch, organization
                )
                findings.extend(hotspot_findings)
                print(f"Found {len(hotspot_findings)} security hotspots")
            
            # Fetch vulnerabilities
            if self.include_vulnerabilities:
                vuln_findings = self._fetch_issues(
                    project_key, 'VULNERABILITY', branch, organization
                )
                findings.extend(vuln_findings)
                print(f"Found {len(vuln_findings)} vulnerabilities")
            
            # Fetch code smells (optional)
            if self.include_code_smells:
                smell_findings = self._fetch_issues(
                    project_key, 'CODE_SMELL', branch, organization
                )
                findings.extend(smell_findings)
                print(f"Found {len(smell_findings)} code smells")
        
        except Exception as e:
            print(f"Error fetching findings: {e}")
        
        # Filter for crypto-related findings
        crypto_findings = [f for f in findings if self._is_crypto_related_finding(f)]
        print(f"Filtered to {len(crypto_findings)} crypto-related findings")
        
        return crypto_findings
    
    def _fetch_security_hotspots(
        self,
        project_key: str,
        branch: str,
        organization: Optional[str]
    ) -> List[CryptoFinding]:
        """Fetch security hotspots from SonarQube."""
        findings = []
        
        try:
            params = {
                'projectKey': project_key,
                'status': 'TO_REVIEW',
                'ps': 500  # Page size
            }
            
            if branch:
                params['branch'] = branch
            if organization:
                params['organization'] = organization
            
            response = requests.get(
                f'{self.url}/api/hotspots/search',
                auth=(self.token, ''),
                params=params,
                timeout=30
            )
            
            if response.status_code == 200:
                data = response.json()
                hotspots = data.get('hotspots', [])
                
                for hotspot in hotspots:
                    finding = self._create_finding_from_hotspot(hotspot, project_key)
                    if finding:
                        findings.append(finding)
            else:
                print(f"Error fetching hotspots: {response.status_code}")
        
        except Exception as e:
            print(f"Error in hotspots request: {e}")
        
        return findings
    
    def _fetch_issues(
        self,
        project_key: str,
        issue_type: str,
        branch: str,
        organization: Optional[str]
    ) -> List[CryptoFinding]:
        """Fetch issues (vulnerabilities or code smells) from SonarQube."""
        findings = []
        
        try:
            params = {
                'componentKeys': project_key,
                'types': issue_type,
                'resolved': 'false',
                'ps': 500  # Page size
            }
            
            if branch:
                params['branch'] = branch
            if organization:
                params['organization'] = organization
            
            response = requests.get(
                f'{self.url}/api/issues/search',
                auth=(self.token, ''),
                params=params,
                timeout=30
            )
            
            if response.status_code == 200:
                data = response.json()
                issues = data.get('issues', [])
                
                for issue in issues:
                    finding = self._create_finding_from_issue(issue, project_key)
                    if finding:
                        findings.append(finding)
            else:
                print(f"Error fetching issues: {response.status_code}")
        
        except Exception as e:
            print(f"Error in issues request: {e}")
        
        return findings
    
    def _create_finding_from_hotspot(
        self,
        hotspot: Dict[str, Any],
        project_key: str
    ) -> Optional[CryptoFinding]:
        """Create CryptoFinding from security hotspot."""
        try:
            rule_key = hotspot.get('ruleKey', 'unknown')
            message = hotspot.get('message', 'Security hotspot')
            
            # Map rule to algorithm
            algorithm = self._map_rule_to_algorithm(rule_key, message)
            
            # Map vulnerability probability to severity
            prob_map = {
                'HIGH': Severity.HIGH,
                'MEDIUM': Severity.MEDIUM,
                'LOW': Severity.LOW
            }
            severity = prob_map.get(
                hotspot.get('vulnerabilityProbability', 'MEDIUM'),
                Severity.MEDIUM
            )
            
            # Infer usage context
            usage_context = self._infer_usage_context(rule_key, message)
            
            # Get location info
            component = hotspot.get('component', '')
            file_path = component.split(':')[-1] if ':' in component else component
            
            finding = CryptoFinding(
                id=f"SONARQUBE-HOTSPOT-{hotspot.get('key', 'unknown')}",
                title=message,
                description=f"Security hotspot: {message}",
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=file_path,
                line_number=hotspot.get('line')
            )
            
            # Add metadata
            finding.metadata.update({
                'source': 'sonarqube_hotspot',
                'project_key': project_key,
                'hotspot_key': hotspot.get('key'),
                'rule_key': rule_key,
                'status': hotspot.get('status'),
                'vulnerability_probability': hotspot.get('vulnerabilityProbability'),
                'security_category': hotspot.get('securityCategory'),
                'creation_date': hotspot.get('creationDate'),
                'update_date': hotspot.get('updateDate'),
                'author': hotspot.get('author')
            })
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from hotspot: {e}")
            return None
    
    def _create_finding_from_issue(
        self,
        issue: Dict[str, Any],
        project_key: str
    ) -> Optional[CryptoFinding]:
        """Create CryptoFinding from SonarQube issue."""
        try:
            rule_key = issue.get('rule', 'unknown')
            message = issue.get('message', 'Issue detected')
            
            # Map rule to algorithm
            algorithm = self._map_rule_to_algorithm(rule_key, message)
            
            # Map severity
            severity_map = {
                'BLOCKER': Severity.CRITICAL,
                'CRITICAL': Severity.CRITICAL,
                'MAJOR': Severity.HIGH,
                'MINOR': Severity.MEDIUM,
                'INFO': Severity.LOW
            }
            severity = severity_map.get(issue.get('severity', 'MAJOR'), Severity.MEDIUM)
            
            # Infer usage context
            usage_context = self._infer_usage_context(rule_key, message)
            
            # Get location info
            component = issue.get('component', '')
            file_path = component.split(':')[-1] if ':' in component else component
            
            # Get text range for code snippet
            text_range = issue.get('textRange', {})
            start_line = text_range.get('startLine')
            end_line = text_range.get('endLine')
            
            finding = CryptoFinding(
                id=f"SONARQUBE-ISSUE-{issue.get('key', 'unknown')}",
                title=message,
                description=f"{issue.get('type', 'Issue')}: {message}",
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=file_path,
                line_number=start_line
            )
            
            # Add metadata
            finding.metadata.update({
                'source': 'sonarqube_issue',
                'project_key': project_key,
                'issue_key': issue.get('key'),
                'rule_key': rule_key,
                'type': issue.get('type'),
                'status': issue.get('status'),
                'resolution': issue.get('resolution'),
                'effort': issue.get('effort'),
                'debt': issue.get('debt'),
                'creation_date': issue.get('creationDate'),
                'update_date': issue.get('updateDate'),
                'author': issue.get('author'),
                'assignee': issue.get('assignee'),
                'tags': issue.get('tags', [])
            })
            
            # Add CWE if available
            if issue.get('tags'):
                cwe_tags = [tag for tag in issue['tags'] if tag.startswith('cwe')]
                finding.cwe_ids.extend(cwe_tags)
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from issue: {e}")
            return None
    
    def _map_rule_to_algorithm(self, rule_key: str, message: str) -> CryptoAlgorithm:
        """Map SonarQube rule key to crypto algorithm."""
        text = f"{rule_key} {message}".lower()
        
        # Check each algorithm's patterns
        for algorithm, patterns in SONARQUBE_RULE_PATTERNS.items():
            for pattern in patterns:
                if re.search(pattern, text, re.IGNORECASE):
                    return algorithm
        
        return CryptoAlgorithm.UNKNOWN
    
    def _infer_usage_context(self, rule_key: str, message: str) -> UsageContext:
        """Infer usage context from rule key and message."""
        text = f"{rule_key} {message}".lower()
        
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
    
    def _is_crypto_related_finding(self, finding: CryptoFinding) -> bool:
        """Check if finding is crypto-related."""
        # Already filtered if algorithm is not UNKNOWN
        if finding.algorithm != CryptoAlgorithm.UNKNOWN:
            return True
        
        # Check rule key and title for crypto keywords
        rule_key = finding.metadata.get('rule_key', '').lower()
        title = finding.title.lower()
        
        return any(keyword in rule_key or keyword in title for keyword in CRYPTO_KEYWORDS)
    
    def enrich_context(
        self,
        finding: CryptoFinding,
        repository: Optional[str] = None
    ) -> CryptoFinding:
        """
        Enrich finding with SonarQube quality metrics.
        
        Args:
            finding: CryptoFinding to enrich
            repository: Repository name (not used)
            
        Returns:
            Enriched CryptoFinding
        """
        project_key = finding.metadata.get('project_key')
        if not project_key or not self.authenticated:
            return finding
        
        try:
            # Fetch project measures
            params = {
                'component': project_key,
                'metricKeys': ','.join([
                    'bugs', 'vulnerabilities', 'code_smells',
                    'security_hotspots', 'coverage', 'duplicated_lines_density',
                    'ncloc', 'sqale_index', 'reliability_rating',
                    'security_rating', 'sqale_rating'
                ])
            }
            
            response = requests.get(
                f'{self.url}/api/measures/component',
                auth=(self.token, ''),
                params=params,
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                measures = data.get('component', {}).get('measures', [])
                
                # Add quality metrics to metadata
                for measure in measures:
                    metric_key = measure.get('metric')
                    value = measure.get('value')
                    finding.metadata[f'quality_{metric_key}'] = value
                
                # Fetch rule details
                rule_key = finding.metadata.get('rule_key')
                if rule_key:
                    rule_response = requests.get(
                        f'{self.url}/api/rules/show',
                        auth=(self.token, ''),
                        params={'key': rule_key},
                        timeout=10
                    )
                    
                    if rule_response.status_code == 200:
                        rule_data = rule_response.json().get('rule', {})
                        finding.metadata['rule_name'] = rule_data.get('name')
                        finding.metadata['rule_description'] = rule_data.get('htmlDesc')
                        finding.metadata['rule_type'] = rule_data.get('type')
                        finding.metadata['rule_severity'] = rule_data.get('severity')
                        
                        # Add references
                        if rule_data.get('htmlNote'):
                            finding.description += f"\n\n{rule_data['htmlNote']}"
        
        except Exception as e:
            print(f"Error enriching with quality metrics: {e}")
        
        return finding