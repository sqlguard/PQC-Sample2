"""
SARIF (Static Analysis Results Interchange Format) parser.
Supports SARIF v2.1.0 specification.
"""
from typing import List, Dict, Any, Optional
from ..models import CryptoFinding, Severity, CryptoAlgorithm, UsageContext
from .base_parser import BaseParser


class SARIFParser(BaseParser):
    """Parser for SARIF format vulnerability scan results."""
    
    def validate_format(self) -> bool:
        """
        Validate SARIF format structure.
        
        Returns:
            True if valid SARIF format
        """
        if not self.raw_data:
            self.load_file()
        
        # Check for required SARIF fields
        required_fields = ['version', 'runs']
        return all(field in self.raw_data for field in required_fields)
    
    def parse(self) -> List[CryptoFinding]:
        """
        Parse SARIF results and extract crypto findings.
        
        Returns:
            List of CryptoFinding objects
        """
        if not self.raw_data:
            self.load_file()
        
        if not self.validate_format():
            raise ValueError("Invalid SARIF format")
        
        findings = []
        
        # SARIF can have multiple runs
        for run in self.raw_data.get('runs', []):
            results = run.get('results', [])
            
            for idx, result in enumerate(results):
                # Only process crypto-related findings
                if not self.is_crypto_related(result):
                    continue
                
                finding = self._parse_result(result, idx, run)
                if finding:
                    findings.append(finding)
        
        return findings
    
    def _parse_result(self, result: Dict[str, Any], idx: int, run: Dict[str, Any]) -> Optional[CryptoFinding]:
        """
        Parse a single SARIF result into a CryptoFinding.
        
        Args:
            result: SARIF result object
            idx: Result index
            run: Parent run object for context
            
        Returns:
            CryptoFinding object or None
        """
        try:
            # Extract basic information
            rule_id = result.get('ruleId', f'unknown-{idx}')
            message = self._extract_message(result)
            
            # Get rule details from run
            rule_details = self._get_rule_details(rule_id, run)
            
            # Extract location information
            location_info = self._extract_location(result)
            
            # Determine severity
            severity = self._map_severity(result.get('level', 'warning'))
            
            # Extract CWE IDs
            cwe_ids = self.extract_cwe_ids(result)
            if rule_details:
                cwe_ids.extend(self.extract_cwe_ids(rule_details))
            
            # Detect algorithm and usage context
            algorithm = self._detect_algorithm(message, rule_details)
            usage_context = self._detect_usage_context(message, rule_details)
            
            finding = CryptoFinding(
                id=f"sarif-{rule_id}-{idx}",
                title=rule_details.get('name', rule_id) if rule_details else rule_id,
                description=message,
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=location_info.get('file_path'),
                line_number=location_info.get('line_number'),
                code_snippet=location_info.get('code_snippet'),
                cwe_ids=cwe_ids,
                references=self._extract_references(rule_details) if rule_details else [],
                metadata={
                    'rule_id': rule_id,
                    'sarif_level': result.get('level', 'warning')
                }
            )
            
            return finding
            
        except Exception as e:
            print(f"Warning: Failed to parse SARIF result {idx}: {e}")
            return None
    
    def _extract_message(self, result: Dict[str, Any]) -> str:
        """Extract message text from SARIF result."""
        message = result.get('message', {})
        
        if isinstance(message, dict):
            return message.get('text', message.get('markdown', 'No description'))
        return str(message)
    
    def _get_rule_details(self, rule_id: str, run: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """Get rule details from the run's tool component."""
        tool = run.get('tool', {})
        driver = tool.get('driver', {})
        rules = driver.get('rules', [])
        
        for rule in rules:
            if rule.get('id') == rule_id:
                return rule
        
        return None
    
    def _extract_location(self, result: Dict[str, Any]) -> Dict[str, Optional[str]]:
        """Extract location information from SARIF result."""
        locations = result.get('locations', [])
        
        if not locations:
            return {'file_path': None, 'line_number': None, 'code_snippet': None}
        
        location = locations[0]  # Use first location
        physical_location = location.get('physicalLocation', {})
        artifact_location = physical_location.get('artifactLocation', {})
        region = physical_location.get('region', {})
        
        return {
            'file_path': artifact_location.get('uri'),
            'line_number': region.get('startLine'),
            'code_snippet': region.get('snippet', {}).get('text')
        }
    
    def _map_severity(self, sarif_level: str) -> Severity:
        """Map SARIF level to Severity enum."""
        mapping = {
            'error': Severity.HIGH,
            'warning': Severity.MEDIUM,
            'note': Severity.LOW,
            'none': Severity.INFO
        }
        return mapping.get(sarif_level.lower(), Severity.MEDIUM)
    
    def _detect_algorithm(self, message: str, rule_details: Optional[Dict[str, Any]]) -> CryptoAlgorithm:
        """Detect cryptographic algorithm from message and rule details."""
        text = message.lower()
        if rule_details:
            text += ' ' + str(rule_details.get('name', '')).lower()
            text += ' ' + str(rule_details.get('shortDescription', {}).get('text', '')).lower()
        
        # Algorithm detection patterns
        if 'md5' in text:
            return CryptoAlgorithm.MD5
        elif 'sha-1' in text or 'sha1' in text:
            return CryptoAlgorithm.SHA1
        elif 'des' in text and '3des' not in text and 'triple' not in text:
            return CryptoAlgorithm.DES
        elif '3des' in text or 'triple-des' in text or 'triple des' in text:
            return CryptoAlgorithm.TRIPLE_DES
        elif 'rc4' in text:
            return CryptoAlgorithm.RC4
        elif 'rsa' in text:
            if '1024' in text:
                return CryptoAlgorithm.RSA_1024
            elif '2048' in text:
                return CryptoAlgorithm.RSA_2048
        elif 'aes' in text:
            if '128' in text:
                return CryptoAlgorithm.AES_128
            elif '256' in text:
                return CryptoAlgorithm.AES_256
        elif 'ecdsa' in text or 'ecc' in text:
            return CryptoAlgorithm.ECDSA_P256
        
        return CryptoAlgorithm.UNKNOWN
    
    def _detect_usage_context(self, message: str, rule_details: Optional[Dict[str, Any]]) -> UsageContext:
        """Detect usage context from message and rule details."""
        text = message.lower()
        if rule_details:
            text += ' ' + str(rule_details.get('name', '')).lower()
            text += ' ' + str(rule_details.get('shortDescription', {}).get('text', '')).lower()
        
        # Context detection patterns
        if any(word in text for word in ['auth', 'login', 'credential', 'password']):
            return UsageContext.AUTHENTICATION
        elif any(word in text for word in ['key storage', 'key management', 'keystore']):
            return UsageContext.KEY_STORAGE
        elif any(word in text for word in ['encrypt', 'decrypt', 'cipher']):
            return UsageContext.DATA_ENCRYPTION
        elif any(word in text for word in ['sign', 'signature', 'verify']):
            return UsageContext.SIGNING
        elif any(word in text for word in ['hash', 'digest', 'checksum']):
            return UsageContext.HASHING
        elif any(word in text for word in ['internal', 'private', 'backend']):
            return UsageContext.INTERNAL
        
        return UsageContext.UNKNOWN
    
    def _extract_references(self, rule_details: Dict[str, Any]) -> List[str]:
        """Extract reference URLs from rule details."""
        references = []
        
        help_uri = rule_details.get('helpUri')
        if help_uri:
            references.append(help_uri)
        
        help_obj = rule_details.get('help', {})
        if isinstance(help_obj, dict):
            help_text = help_obj.get('text', '')
            # Simple URL extraction (could be improved with regex)
            if 'http' in help_text:
                references.append(help_text)
        
        return references