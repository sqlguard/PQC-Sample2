"""
Semgrep integration plugin.
Fetches static analysis results from Semgrep Cloud API or parses CLI output.
"""

import re
import json
import time
import subprocess
from typing import List, Optional, Dict, Any, Union
from pathlib import Path
from .base_plugin import BasePlugin
from ..models import CryptoFinding, CryptoAlgorithm, Severity, UsageContext

try:
    import requests
    REQUESTS_AVAILABLE = True
except ImportError:
    REQUESTS_AVAILABLE = False


# Semgrep rule ID to crypto algorithm mapping
SEMGREP_RULE_PATTERNS = {
    CryptoAlgorithm.MD5: [
        r'md5', r'message.?digest.?5', r'crypto.*md5', r'hash.*md5',
        r'insecure.*md5', r'weak.*md5'
    ],
    CryptoAlgorithm.SHA1: [
        r'sha.?1', r'sha-1', r'crypto.*sha1', r'hash.*sha1',
        r'insecure.*sha1', r'weak.*sha1'
    ],
    CryptoAlgorithm.DES: [
        r'\bdes\b', r'data.?encryption.?standard', r'crypto.*\bdes\b',
        r'insecure.*des', r'weak.*des'
    ],
    CryptoAlgorithm.TRIPLE_DES: [
        r'3des', r'triple.?des', r'tdea', r'crypto.*3des',
        r'insecure.*3des'
    ],
    CryptoAlgorithm.RC4: [
        r'rc4', r'arcfour', r'arc4', r'crypto.*rc4',
        r'insecure.*rc4', r'weak.*rc4'
    ],
    CryptoAlgorithm.RSA_1024: [
        r'rsa.*1024', r'1024.*bit.*rsa', r'weak.*rsa.*key',
        r'insecure.*rsa.*1024'
    ],
    CryptoAlgorithm.RSA_2048: [
        r'rsa.*2048', r'2048.*bit.*rsa'
    ],
}

# Crypto-related rule patterns
CRYPTO_RULE_KEYWORDS = [
    'crypto', 'encryption', 'cipher', 'hash', 'digest', 'ssl', 'tls',
    'certificate', 'key', 'rsa', 'aes', 'des', 'md5', 'sha', 'rc4',
    'ecdsa', 'signature', 'signing', 'random', 'prng', 'secure'
]


class SemgrepPlugin(BasePlugin):
    """
    Semgrep integration plugin.
    
    Features:
    - Fetch findings from Semgrep Cloud API
    - Parse Semgrep CLI JSON output
    - Run Semgrep scans locally
    - Map rule IDs to crypto algorithms
    - Enrich with rule metadata
    - Filter crypto-related findings
    
    Configuration:
        api_token: Semgrep Cloud API token (optional, for Cloud API)
        deployment_id: Semgrep deployment ID (optional, for Cloud API)
        scan_path: Local path to scan with CLI (optional)
        rules: Semgrep rules to use (optional, default: auto)
        cli_path: Path to semgrep binary (optional, default: 'semgrep')
        timeout: Scan timeout in seconds (optional, default: 300)
        max_retries: Maximum retry attempts (optional, default: 3)
        retry_delay: Delay between retries in seconds (optional, default: 2)
    """
    
    def __init__(self, config: Dict[str, Any]):
        """Initialize Semgrep plugin with configuration."""
        super().__init__(config)
        self.api_token = config.get('api_token')
        self.deployment_id = config.get('deployment_id')
        self.scan_path = config.get('scan_path')
        self.rules = config.get('rules', 'auto')
        self.cli_path = config.get('cli_path', self._find_semgrep_binary())
        self.timeout = config.get('timeout', 300)
        self.max_retries = config.get('max_retries', 3)
        self.retry_delay = config.get('retry_delay', 2)
        self.base_url = 'https://semgrep.dev/api/v1'
    
    def _find_semgrep_binary(self) -> str:
        """Find semgrep binary in common locations."""
        import os
        import shutil
        
        # Try to find in PATH first
        semgrep_path = shutil.which('semgrep')
        if semgrep_path:
            return semgrep_path
        
        # Check common user installation paths
        home = os.path.expanduser('~')
        common_paths = [
            f'{home}/Library/Python/3.9/bin/semgrep',
            f'{home}/Library/Python/3.10/bin/semgrep',
            f'{home}/Library/Python/3.11/bin/semgrep',
            f'{home}/.local/bin/semgrep',
            '/usr/local/bin/semgrep',
        ]
        
        for path in common_paths:
            if os.path.isfile(path) and os.access(path, os.X_OK):
                return path
        
        # Default fallback
        return 'semgrep'
    
    def authenticate(self) -> bool:
        """
        Authenticate with Semgrep Cloud API.
        
        Returns:
            True if authentication successful or not required
        """
        # If no API token, assume CLI-only mode
        if not self.api_token:
            print("No Semgrep API token provided, using CLI-only mode")
            self.authenticated = True
            return True
        
        if not REQUESTS_AVAILABLE:
            raise ImportError("requests library not installed. Run: pip install requests")
        
        try:
            # Test API token by fetching deployments
            headers = {
                'Authorization': f'Bearer {self.api_token}',
                'Content-Type': 'application/json'
            }
            
            response = requests.get(
                f'{self.base_url}/deployments',
                headers=headers,
                timeout=10
            )
            
            if response.status_code == 200:
                self.authenticated = True
                deployments = response.json().get('deployments', [])
                print(f"Semgrep authentication successful. Found {len(deployments)} deployments")
                return True
            else:
                print(f"Semgrep authentication failed: {response.status_code} - {response.text}")
                return False
        
        except Exception as e:
            print(f"Semgrep authentication error: {e}")
            return False
    
    def fetch_findings(
        self,
        repository: Optional[str] = None,
        project: Optional[str] = None,
        **kwargs
    ) -> List[CryptoFinding]:
        """
        Fetch crypto-related findings from Semgrep.
        
        Args:
            repository: Repository name (for Cloud API)
            project: Project name (for Cloud API)
            **kwargs: Additional parameters
                - scan_path: Override scan path for CLI
                - rules: Override rules for CLI
                - from_file: Path to Semgrep JSON output file
            
        Returns:
            List of CryptoFinding objects
        """
        findings = []
        
        # Check if loading from file
        from_file = kwargs.get('from_file')
        if from_file:
            return self._parse_semgrep_output_file(from_file)
        
        # Try Cloud API first if authenticated
        if self.authenticated and self.api_token:
            try:
                cloud_findings = self._fetch_from_cloud_api(repository, project)
                findings.extend(cloud_findings)
                print(f"Fetched {len(cloud_findings)} findings from Semgrep Cloud")
            except Exception as e:
                print(f"Error fetching from Semgrep Cloud: {e}")
        
        # Try CLI scan if scan_path provided
        scan_path = kwargs.get('scan_path', self.scan_path)
        if scan_path:
            try:
                cli_findings = self._run_cli_scan(scan_path, kwargs.get('rules', self.rules))
                findings.extend(cli_findings)
                print(f"Found {len(cli_findings)} findings from CLI scan")
            except Exception as e:
                print(f"Error running CLI scan: {e}")
        
        # Filter for crypto-related findings
        crypto_findings = [f for f in findings if self._is_crypto_related_finding(f)]
        print(f"Filtered to {len(crypto_findings)} crypto-related findings")
        
        return crypto_findings
    
    def _fetch_from_cloud_api(
        self,
        repository: Optional[str] = None,
        project: Optional[str] = None
    ) -> List[CryptoFinding]:
        """Fetch findings from Semgrep Cloud API."""
        if not REQUESTS_AVAILABLE:
            raise ImportError("requests library required for Cloud API")
        
        findings = []
        headers = {
            'Authorization': f'Bearer {self.api_token}',
            'Content-Type': 'application/json'
        }
        
        # Determine deployment ID
        deployment_id = self.deployment_id
        if not deployment_id:
            # Get first deployment
            response = requests.get(
                f'{self.base_url}/deployments',
                headers=headers,
                timeout=10
            )
            if response.status_code == 200:
                deployments = response.json().get('deployments', [])
                if deployments:
                    deployment_id = deployments[0]['id']
        
        if not deployment_id:
            print("No deployment ID found")
            return findings
        
        # Fetch findings for deployment
        try:
            response = requests.get(
                f'{self.base_url}/deployments/{deployment_id}/findings',
                headers=headers,
                params={
                    'repository': repository,
                    'project': project,
                    'status': 'open'
                },
                timeout=30
            )
            
            if response.status_code == 200:
                data = response.json()
                for finding_data in data.get('findings', []):
                    finding = self._create_finding_from_cloud_data(finding_data)
                    if finding:
                        findings.append(finding)
            else:
                print(f"Error fetching findings: {response.status_code}")
        
        except Exception as e:
            print(f"Error in Cloud API request: {e}")
        
        return findings
    
    def _run_cli_scan(self, scan_path: str, rules: str = 'auto') -> List[CryptoFinding]:
        """Run Semgrep CLI scan and parse results."""
        findings = []
        
        try:
            # Build semgrep command
            cmd = [
                self.cli_path,
                '--config', rules,
                '--json',
                '--no-git-ignore',
                scan_path
            ]
            
            print(f"Running Semgrep scan: {' '.join(cmd)}")
            
            # Run semgrep with updated PATH
            import os
            env = os.environ.copy()
            # Add Python bin directory to PATH
            python_bin = str(Path(self.cli_path).parent)
            env['PATH'] = f"{python_bin}:{env.get('PATH', '')}"
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=self.timeout,
                env=env
            )
            
            # Parse JSON output
            if result.stdout:
                output_data = json.loads(result.stdout)
                findings = self._parse_semgrep_output(output_data)
            
            if result.returncode != 0 and result.stderr:
                print(f"Semgrep scan warnings: {result.stderr}")
        
        except subprocess.TimeoutExpired:
            print(f"Semgrep scan timed out after {self.timeout} seconds")
        except FileNotFoundError:
            print(f"Semgrep CLI not found at: {self.cli_path}")
            print("Install with: pip install semgrep")
        except json.JSONDecodeError as e:
            print(f"Error parsing Semgrep output: {e}")
        except Exception as e:
            print(f"Error running Semgrep scan: {e}")
        
        return findings
    
    def _parse_semgrep_output_file(self, file_path: str) -> List[CryptoFinding]:
        """Parse Semgrep JSON output from file."""
        try:
            with open(file_path, 'r') as f:
                data = json.load(f)
            return self._parse_semgrep_output(data)
        except Exception as e:
            print(f"Error parsing Semgrep output file: {e}")
            return []
    
    def _parse_semgrep_output(self, data: Dict[str, Any]) -> List[CryptoFinding]:
        """Parse Semgrep JSON output format."""
        findings = []
        
        results = data.get('results', [])
        for result in results:
            finding = self._create_finding_from_cli_result(result)
            if finding:
                findings.append(finding)
        
        return findings
    
    def _create_finding_from_cloud_data(self, data: Dict[str, Any]) -> Optional[CryptoFinding]:
        """Create CryptoFinding from Semgrep Cloud API data."""
        try:
            rule_id = data.get('rule_id', 'unknown')
            
            # Map rule to algorithm
            algorithm = self._map_rule_to_algorithm(rule_id, data.get('rule_message', ''))
            
            # Map severity
            severity_map = {
                'ERROR': Severity.HIGH,
                'WARNING': Severity.MEDIUM,
                'INFO': Severity.LOW
            }
            severity = severity_map.get(data.get('severity', 'WARNING'), Severity.MEDIUM)
            
            # Infer usage context
            usage_context = self._infer_usage_context(rule_id, data.get('rule_message', ''))
            
            finding = CryptoFinding(
                id=f"SEMGREP-CLOUD-{data.get('id', 'unknown')}",
                title=data.get('rule_message', f"Semgrep finding: {rule_id}"),
                description=data.get('extra', {}).get('message', ''),
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=data.get('path'),
                line_number=data.get('line')
            )
            
            # Add metadata
            finding.metadata.update({
                'source': 'semgrep_cloud',
                'rule_id': rule_id,
                'repository': data.get('repository'),
                'ref': data.get('ref'),
                'commit_sha': data.get('commit_sha'),
                'triage_state': data.get('triage_state'),
                'first_seen': data.get('first_seen_scan_id')
            })
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from Cloud data: {e}")
            return None
    
    def _create_finding_from_cli_result(self, result: Dict[str, Any]) -> Optional[CryptoFinding]:
        """Create CryptoFinding from Semgrep CLI result."""
        try:
            check_id = result.get('check_id', 'unknown')
            
            # Map rule to algorithm
            algorithm = self._map_rule_to_algorithm(
                check_id,
                result.get('extra', {}).get('message', '')
            )
            
            # Map severity
            severity_map = {
                'ERROR': Severity.HIGH,
                'WARNING': Severity.MEDIUM,
                'INFO': Severity.LOW
            }
            severity = severity_map.get(
                result.get('extra', {}).get('severity', 'WARNING'),
                Severity.MEDIUM
            )
            
            # Infer usage context
            usage_context = self._infer_usage_context(
                check_id,
                result.get('extra', {}).get('message', '')
            )
            
            # Extract location info
            start = result.get('start', {})
            end = result.get('end', {})
            
            finding = CryptoFinding(
                id=f"SEMGREP-CLI-{check_id}-{start.get('line', 0)}",
                title=result.get('extra', {}).get('message', f"Semgrep: {check_id}"),
                description=result.get('extra', {}).get('metadata', {}).get('description', ''),
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=result.get('path'),
                line_number=start.get('line'),
                code_snippet=result.get('extra', {}).get('lines', '')
            )
            
            # Add metadata
            metadata = result.get('extra', {}).get('metadata', {})
            finding.metadata.update({
                'source': 'semgrep_cli',
                'rule_id': check_id,
                'category': metadata.get('category'),
                'technology': metadata.get('technology'),
                'owasp': metadata.get('owasp', []),
                'cwe': metadata.get('cwe', []),
                'confidence': metadata.get('confidence'),
                'likelihood': metadata.get('likelihood'),
                'impact': metadata.get('impact'),
                'subcategory': metadata.get('subcategory', [])
            })
            
            # Add CWE IDs
            if metadata.get('cwe'):
                finding.cwe_ids.extend([f"CWE-{cwe}" for cwe in metadata.get('cwe', [])])
            
            # Add references
            if metadata.get('references'):
                finding.references.extend(metadata.get('references', []))
            
            return finding
        
        except Exception as e:
            print(f"Error creating finding from CLI result: {e}")
            return None
    
    def _map_rule_to_algorithm(self, rule_id: str, message: str) -> CryptoAlgorithm:
        """Map Semgrep rule ID to crypto algorithm."""
        text = f"{rule_id} {message}".lower()
        
        # Check each algorithm's patterns
        for algorithm, patterns in SEMGREP_RULE_PATTERNS.items():
            for pattern in patterns:
                if re.search(pattern, text, re.IGNORECASE):
                    return algorithm
        
        return CryptoAlgorithm.UNKNOWN
    
    def _infer_usage_context(self, rule_id: str, message: str) -> UsageContext:
        """Infer usage context from rule ID and message."""
        text = f"{rule_id} {message}".lower()
        
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
        
        # Check rule ID and title for crypto keywords
        rule_id = finding.metadata.get('rule_id', '').lower()
        title = finding.title.lower()
        
        return any(keyword in rule_id or keyword in title for keyword in CRYPTO_RULE_KEYWORDS)
    
    def enrich_context(
        self,
        finding: CryptoFinding,
        repository: Optional[str] = None
    ) -> CryptoFinding:
        """
        Enrich finding with Semgrep rule metadata.
        
        Args:
            finding: CryptoFinding to enrich
            repository: Repository name (optional)
            
        Returns:
            Enriched CryptoFinding
        """
        rule_id = finding.metadata.get('rule_id')
        if not rule_id:
            return finding
        
        try:
            # Fetch rule metadata from Semgrep registry
            if REQUESTS_AVAILABLE:
                response = requests.get(
                    f'https://semgrep.dev/api/registry/rule/{rule_id}',
                    timeout=10
                )
                
                if response.status_code == 200:
                    rule_data = response.json()
                    
                    # Add rule metadata
                    finding.metadata['rule_name'] = rule_data.get('name')
                    finding.metadata['rule_description'] = rule_data.get('description')
                    finding.metadata['rule_severity'] = rule_data.get('severity')
                    finding.metadata['rule_languages'] = rule_data.get('languages', [])
                    finding.metadata['rule_tags'] = rule_data.get('tags', [])
                    
                    # Add references from rule
                    if rule_data.get('references'):
                        finding.references.extend(rule_data.get('references', []))
        
        except Exception as e:
            print(f"Error enriching with rule metadata: {e}")
        
        return finding