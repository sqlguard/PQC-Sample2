"""
Snyk integration plugin.
Fetches vulnerability scan results from Snyk API or parses CLI output.
"""

import re
import json
import time
import subprocess
from typing import List, Optional, Dict, Any
from pathlib import Path
from .base_plugin import BasePlugin
from ..models import CryptoFinding, CryptoAlgorithm, Severity, UsageContext

try:
    import requests
    REQUESTS_AVAILABLE = True
except ImportError:
    REQUESTS_AVAILABLE = False


# Snyk vulnerability ID to crypto algorithm mapping
SNYK_VULN_PATTERNS = {
    CryptoAlgorithm.MD5: [
        r'md5', r'message.?digest.?5', r'snyk.*md5', r'weak.*md5',
        r'insecure.*md5', r'broken.*md5'
    ],
    CryptoAlgorithm.SHA1: [
        r'sha.?1', r'sha-1', r'snyk.*sha1', r'weak.*sha1',
        r'insecure.*sha1', r'broken.*sha1'
    ],
    CryptoAlgorithm.DES: [
        r'\bdes\b', r'data.?encryption.?standard', r'snyk.*\bdes\b',
        r'weak.*des', r'insecure.*des'
    ],
    CryptoAlgorithm.TRIPLE_DES: [
        r'3des', r'triple.?des', r'tdea', r'snyk.*3des',
        r'weak.*3des'
    ],
    CryptoAlgorithm.RC4: [
        r'rc4', r'arcfour', r'arc4', r'snyk.*rc4',
        r'weak.*rc4', r'insecure.*rc4'
    ],
    CryptoAlgorithm.RSA_1024: [
        r'rsa.*1024', r'1024.*bit.*rsa', r'weak.*rsa.*key',
        r'insufficient.*rsa.*key'
    ],
    CryptoAlgorithm.RSA_2048: [
        r'rsa.*2048', r'2048.*bit.*rsa'
    ],
}

# Crypto-related keywords for filtering
CRYPTO_KEYWORDS = [
    'crypto', 'encryption', 'cipher', 'hash', 'digest', 'ssl', 'tls',
    'certificate', 'key', 'rsa', 'aes', 'des', 'md5', 'sha', 'rc4',
    'ecdsa', 'signature', 'signing', 'random', 'prng', 'secure'
]


class SnykPlugin(BasePlugin):
    """
    Snyk integration plugin.
    
    Features:
    - Fetch vulnerabilities from Snyk REST API
    - Parse Snyk CLI JSON output
    - Run Snyk scans locally
    - Map vulnerability IDs to crypto algorithms
    - Enrich with package and vulnerability metadata
    - Support multiple project types (npm, pip, maven, etc.)
    
    Configuration:
        api_token: Snyk API token (required for API mode)
        org_id: Snyk organization ID (optional, auto-detected)
        project_id: Specific project ID to scan (optional)
        scan_path: Local path to scan with CLI (optional)
        project_type: Project type (npm, pip, maven, etc.) (optional)
        cli_path: Path to snyk binary (optional, default: 'snyk')
        timeout: Scan timeout in seconds (optional, default: 300)
        severity_threshold: Minimum severity to report (optional, default: 'low')
        max_retries: Maximum retry attempts (optional, default: 3)
        retry_delay: Delay between retries in seconds (optional, default: 2)
    """
    
    def __init__(self, config: Dict[str, Any]):
        """Initialize Snyk plugin with configuration."""
        super().__init__(config)
        self.api_token = config.get('api_token')
        self.org_id = config.get('org_id')
        self.project_id = config.get('project_id')
        self.scan_path = config.get('scan_path')
        self.project_type = config.get('project_type')
        self.cli_path = config.get('cli_path', 'snyk')
        self.timeout = config.get('timeout', 300)
        self.severity_threshold = config.get('severity_threshold', 'low')
        self.max_retries = config.get('max_retries', 3)
        self.retry_delay = config.get('retry_delay', 2)
        self.base_url = 'https://api.snyk.io/v1'
    
    def authenticate(self) -> bool:
        """
        Authenticate with Snyk API.
        
        Returns:
            True if authentication successful or CLI-only mode
        """
        # If no API token, assume CLI-only mode
        if not self.api_token:
            print("No Snyk API token provided, using CLI-only mode")
            self.authenticated = True
            return True
        
        if not REQUESTS_AVAILABLE:
            raise ImportError("requests library not installed. Run: pip install requests")
        
        try:
            # Test API token by fetching user info
            headers = {
                'Authorization': f'token {self.api_token}',
                'Content-Type': 'application/json'
            }
            
            response = requests.get(
                f'{self.base_url}/user/me',
                headers=headers,
                timeout=10
            )
            
            if response.status_code == 200:
                self.authenticated = True
                user_data = response.json()
                print(f"Snyk authentication successful for user: {user_data.get('username', 'unknown')}")
                
                # Auto-detect org if not provided
                if not self.org_id:
                    orgs_response = requests.get(
                        f'{self.base_url}/orgs',
                        headers=headers,
                        timeout=10
                    )
                    if orgs_response.status_code == 200:
                        orgs = orgs_response.json().get('orgs', [])
                        if orgs:
                            self.org_id = orgs[0]['id']
                            print(f"Auto-detected organization: {orgs[0]['name']}")
                
                return True
            else:
                print(f"Snyk authentication failed: {response.status_code} - {response.text}")
                return False
        
        except Exception as e:
            print(f"Snyk authentication error: {e}")
            return False
    
    def fetch_findings(
        self,
        repository: Optional[str] = None,
        project: Optional[str] = None,
        **kwargs
    ) -> List[CryptoFinding]:
        """
        Fetch crypto-related findings from Snyk.
        
        Args:
            repository: Repository name (for API mode)
            project: Project ID or name (for API mode)
            **kwargs: Additional parameters
                - scan_path: Override scan path for CLI
                - project_type: Override project type for CLI
                - from_file: Path to Snyk JSON output file
            
        Returns:
            List of CryptoFinding objects
        """
        findings = []
        
        # Check if loading from file
        from_file = kwargs.get('from_file')
        if from_file:
            return self._parse_snyk_output_file(from_file)
        
        # Try API first if authenticated
        if self.authenticated and self.api_token:
            try:
                api_findings = self._fetch_from_api(project or self.project_id)
                findings.extend(api_findings)
                print(f"Fetched {len(api_findings)} findings from Snyk API")
            except Exception as e:
                print(f"Error fetching from Snyk API: {e}")
        
        # Try CLI scan if scan_path provided
        scan_path = kwargs.get('scan_path', self.scan_path)
        if scan_path:
            try:
                cli_findings = self._run_cli_scan(
                    scan_path,
                    kwargs.get('project_type', self.project_type)
                )
                findings.extend(cli_findings)
                print(f"Found {len(cli_findings)} findings from CLI scan")
            except Exception as e:
                print(f"Error running CLI scan: {e}")
        
        # Filter for crypto-related findings
        crypto_findings = [f for f in findings if self._is_crypto_related_finding(f)]
        print(f"Filtered to {len(crypto_findings)} crypto-related findings")
        
        return crypto_findings
    
    def _fetch_from_api(self, project_id: Optional[str] = None) -> List[CryptoFinding]:
        """Fetch findings from Snyk REST API."""
        if not REQUESTS_AVAILABLE:
            raise ImportError("requests library required for API mode")
        
        findings = []
        headers = {
            'Authorization': f'token {self.api_token}',
            'Content-Type': 'application/json'
        }
        
        if not self.org_id:
            print("No organization ID available")
            return findings
        
        try:
            # If specific project ID provided, fetch that project
            if project_id:
                project_findings = self._fetch_project_issues(project_id, headers)
                findings.extend(project_findings)
            else:
                # Fetch all projects in organization
                projects_response = requests.get(
                    f'{self.base_url}/org/{self.org_id}/projects',
                    headers=headers,
                    timeout=30
                )
                
                if projects_response.status_code == 200:
                    projects = projects_response.json().get('projects', [])
                    print(f"Found {len(projects)} projects in organization")
                    
                    for project in projects[:10]:  # Limit to first 10 projects
                        project_findings = self._fetch_project_issues(project['id'], headers)
                        findings.extend(project_findings)
                else:
                    print(f"Error fetching projects: {projects_response.status_code}")
        
        except Exception as e:
            print(f"Error in API request: {e}")
        
        return findings
    
    def _fetch_project_issues(self, project_id: str, headers: Dict[str, str]) -> List[CryptoFinding]:
        """Fetch issues for a specific project."""
        findings = []
        
        try:
            response = requests.post(
                f'{self.base_url}/org/{self.org_id}/project/{project_id}/issues',
                headers=headers,
                json={'filters': {'severities': ['high', 'medium', 'low'], 'types': ['vuln']}},
                timeout=30
            )
            
            if response.status_code == 200:
                data = response.json()
                issues = data.get('issues', {}).get('vulnerabilities', [])
                
                for issue in issues:
                    finding = self._create_finding_from_api_issue(issue, project_id)
                    if finding:
                        findings.append(finding)
            else:
                print(f"Error fetching issues for project {project_id}: {response.status_code}")
        
        except Exception as e:
            print(f"Error fetching project issues: {e}")
        
        return findings
    
    def _run_cli_scan(self, scan_path: str, project_type: Optional[str] = None) -> List[CryptoFinding]:
        """Run Snyk CLI scan and parse results."""
        findings = []
        
        try:
            # Build snyk command
            cmd = [self.cli_path, 'test', '--json']
            
            if project_type:
                cmd.extend(['--' + project_type])
            
            cmd.append(scan_path)
            
            print(f"Running Snyk scan: {' '.join(cmd)}")
            
            # Run snyk
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=self.timeout
            )
            
            # Parse JSON output (Snyk returns JSON even on error)
            if result.stdout:
                try:
                    output_data = json.loads(result.stdout)
                    findings = self._parse_snyk_output(output_data)
                except json.JSONDecodeError:
                    # Try parsing stderr if stdout failed
                    if result.stderr:
                        try:
                            output_data = json.loads(result.stderr)
                            findings = self._parse_snyk_output(output_data)
                        except:
                            pass
            
            if result.returncode != 0 and not findings:
                print(f"Snyk scan completed with warnings (exit code: {result.returncode})")
        
        except subprocess.TimeoutExpired:
            print(f"Snyk scan timed out after {self.timeout} seconds")
        except FileNotFoundError:
            print(f"Snyk CLI not found at: {self.cli_path}")
            print("Install with: npm install -g snyk")
        except Exception as e:
            print(f"Error running Snyk scan: {e}")
        
        return findings
    
    def _parse_snyk_output_file(self, file_path: str) -> List[CryptoFinding]:
        """Parse Snyk JSON output from file."""
        try:
            with open(file_path, 'r') as f:
                data = json.load(f)
            return self._parse_snyk_output(data)
        except Exception as e:
            print(f"Error parsing Snyk output file: {e}")
            return []
    
    def _parse_snyk_output(self, data: Dict[str, Any]) -> List[CryptoFinding]:
        """Parse Snyk JSON output format."""
        findings = []
        
        # Handle both CLI and API formats
        vulnerabilities = data.get('vulnerabilities', [])
        
        for vuln in vulnerabilities:
            finding = self._create_finding_from_cli_vuln(vuln)
            if finding:
                findings.append(finding)
        
        return findings
    
    def _create_finding_from_api_issue(self, issue: Dict[str, Any], project_id: str) -> Optional[CryptoFinding]:
        """Create CryptoFinding from Snyk API issue."""
        try:
            vuln_id = issue.get('id', 'unknown')
            title = issue.get('title', 'Unknown vulnerability')
            
            # Map to algorithm
            algorithm = self._map_vuln_to_algorithm(
                vuln_id,
                title,
                issue.get('description', '')
            )
            
            # Map severity
            severity_map = {
                'critical': Severity.CRITICAL,
                'high': Severity.HIGH,
                'medium': Severity.MEDIUM,
                'low': Severity.LOW
            }
            severity = severity_map.get(issue.get('severity', 'medium').lower(), Severity.MEDIUM)
            
            # Infer usage context
            usage_context = self._infer_usage_context(title, issue.get('description', ''))
            
            # Get package info
            package_name = issue.get('packageName', 'unknown')
            version = issue.get('version', 'unknown')
            
            finding = CryptoFinding(
                id=f"SNYK-API-{vuln_id}",
                title=f"{title} in {package_name}",
                description=issue.get('description', ''),
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context
            )
            
            # Add metadata
            finding.metadata.update({
                'source': 'snyk_api',
                'vuln_id': vuln_id,
                'project_id': project_id,
                'package_name': package_name,
                'package_version': version,
                'is_upgradable': issue.get('isUpgradable', False),
                'is_patchable': issue.get('isPatchable', False),
                'cvss_score': issue.get('cvssScore'),
                'exploit_maturity': issue.get('exploitMaturity'),
                'publication_time': issue.get('publicationTime'),
                'disclosure_time': issue.get('disclosureTime')
            })
            
            # Add CVE IDs
            if issue.get('identifiers', {}).get('CVE'):
                finding.cwe_ids.extend(issue['identifiers']['CVE'])
            
            # Add CWE IDs
            if issue.get('identifiers', {}).get('CWE'):
                finding.cwe_ids.extend([f"CWE-{cwe}" for cwe in issue['identifiers']['CWE']])
            
            # Add references
            if issue.get('references'):
                finding.references.extend([ref.get('url') for ref in issue['references'] if ref.get('url')])
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from API issue: {e}")
            return None
    
    def _create_finding_from_cli_vuln(self, vuln: Dict[str, Any]) -> Optional[CryptoFinding]:
        """Create CryptoFinding from Snyk CLI vulnerability."""
        try:
            vuln_id = vuln.get('id', 'unknown')
            title = vuln.get('title', 'Unknown vulnerability')
            
            # Map to algorithm
            algorithm = self._map_vuln_to_algorithm(
                vuln_id,
                title,
                vuln.get('description', '')
            )
            
            # Map severity
            severity_map = {
                'critical': Severity.CRITICAL,
                'high': Severity.HIGH,
                'medium': Severity.MEDIUM,
                'low': Severity.LOW
            }
            severity = severity_map.get(vuln.get('severity', 'medium').lower(), Severity.MEDIUM)
            
            # Infer usage context
            usage_context = self._infer_usage_context(title, vuln.get('description', ''))
            
            # Get package info
            package_name = vuln.get('packageName', 'unknown')
            version = vuln.get('version', 'unknown')
            
            # Get file path from 'from' field
            from_path = vuln.get('from', [])
            file_path = from_path[0] if from_path else None
            
            finding = CryptoFinding(
                id=f"SNYK-CLI-{vuln_id}",
                title=f"{title} in {package_name}",
                description=vuln.get('description', ''),
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=file_path
            )
            
            # Add metadata
            finding.metadata.update({
                'source': 'snyk_cli',
                'vuln_id': vuln_id,
                'package_name': package_name,
                'package_version': version,
                'is_upgradable': vuln.get('isUpgradable', False),
                'is_patchable': vuln.get('isPatchable', False),
                'upgrade_path': vuln.get('upgradePath', []),
                'cvss_score': vuln.get('cvssScore'),
                'cvss_vector': vuln.get('CVSSv3'),
                'exploit_maturity': vuln.get('exploitMaturity'),
                'publication_time': vuln.get('publicationTime'),
                'disclosure_time': vuln.get('disclosureTime'),
                'language': vuln.get('language'),
                'package_manager': vuln.get('packageManager')
            })
            
            # Add CVE IDs
            if vuln.get('identifiers', {}).get('CVE'):
                finding.cwe_ids.extend(vuln['identifiers']['CVE'])
            
            # Add CWE IDs
            if vuln.get('identifiers', {}).get('CWE'):
                finding.cwe_ids.extend([f"CWE-{cwe}" for cwe in vuln['identifiers']['CWE']])
            
            # Add references
            if vuln.get('references'):
                finding.references.extend([ref.get('url') for ref in vuln['references'] if ref.get('url')])
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from CLI vulnerability: {e}")
            return None
    
    def _map_vuln_to_algorithm(self, vuln_id: str, title: str, description: str) -> CryptoAlgorithm:
        """Map Snyk vulnerability to crypto algorithm."""
        text = f"{vuln_id} {title} {description}".lower()
        
        # Check each algorithm's patterns
        for algorithm, patterns in SNYK_VULN_PATTERNS.items():
            for pattern in patterns:
                if re.search(pattern, text, re.IGNORECASE):
                    return algorithm
        
        return CryptoAlgorithm.UNKNOWN
    
    def _infer_usage_context(self, title: str, description: str) -> UsageContext:
        """Infer usage context from vulnerability details."""
        text = f"{title} {description}".lower()
        
        if any(word in text for word in ['auth', 'login', 'session', 'token', 'jwt', 'oauth']):
            return UsageContext.AUTHENTICATION
        elif any(word in text for word in ['key', 'secret', 'credential', 'password', 'storage']):
            return UsageContext.KEY_STORAGE
        elif any(word in text for word in ['encrypt', 'decrypt', 'cipher', 'aes', 'des']):
            return UsageContext.DATA_ENCRYPTION
        elif any(word in text for word in ['sign', 'signature', 'verify', 'hmac']):
            return UsageContext.SIGNING
        elif any(word in text for word in ['hash', 'digest', 'checksum', 'md5', 'sha']):
            return UsageContext.HASHING
        
        return UsageContext.UNKNOWN
    
    def _is_crypto_related_finding(self, finding: CryptoFinding) -> bool:
        """Check if finding is crypto-related."""
        # Already filtered if algorithm is not UNKNOWN
        if finding.algorithm != CryptoAlgorithm.UNKNOWN:
            return True
        
        # Check vulnerability ID and title for crypto keywords
        vuln_id = finding.metadata.get('vuln_id', '').lower()
        title = finding.title.lower()
        
        return any(keyword in vuln_id or keyword in title for keyword in CRYPTO_KEYWORDS)
    
    def enrich_context(
        self,
        finding: CryptoFinding,
        repository: Optional[str] = None
    ) -> CryptoFinding:
        """
        Enrich finding with Snyk vulnerability metadata.
        
        Args:
            finding: CryptoFinding to enrich
            repository: Repository name (optional)
            
        Returns:
            Enriched CryptoFinding
        """
        vuln_id = finding.metadata.get('vuln_id')
        if not vuln_id or not self.authenticated or not self.api_token:
            return finding
        
        try:
            # Fetch detailed vulnerability info from Snyk API
            if REQUESTS_AVAILABLE:
                headers = {
                    'Authorization': f'token {self.api_token}',
                    'Content-Type': 'application/json'
                }
                
                response = requests.get(
                    f'{self.base_url}/vuln/{vuln_id}',
                    headers=headers,
                    timeout=10
                )
                
                if response.status_code == 200:
                    vuln_data = response.json()
                    
                    # Add detailed vulnerability metadata
                    finding.metadata['vuln_title'] = vuln_data.get('title')
                    finding.metadata['vuln_credit'] = vuln_data.get('credit', [])
                    finding.metadata['vuln_semver'] = vuln_data.get('semver', {})
                    finding.metadata['vuln_patches'] = vuln_data.get('patches', [])
                    finding.metadata['vuln_functions'] = vuln_data.get('functions', [])
                    finding.metadata['vuln_malicious'] = vuln_data.get('malicious', False)
                    
                    # Add more references
                    if vuln_data.get('references'):
                        finding.references.extend([
                            ref.get('url') for ref in vuln_data['references']
                            if ref.get('url') and ref['url'] not in finding.references
                        ])
        
        except Exception as e:
            print(f"Error enriching with vulnerability metadata: {e}")
        
        return finding