"""
Generic JSON parser for custom vulnerability scan formats.
Supports flexible JSON structures with configurable field mappings.
"""
from typing import List, Dict, Any, Optional
from ..models import CryptoFinding, Severity, CryptoAlgorithm, UsageContext
from .base_parser import BaseParser


class JSONParser(BaseParser):
    """Parser for generic JSON format vulnerability scan results."""
    
    def __init__(self, file_path: str, field_mapping: Optional[Dict[str, str]] = None):
        """
        Initialize JSON parser with optional field mapping.
        
        Args:
            file_path: Path to the JSON file
            field_mapping: Optional mapping of standard fields to custom field names
        """
        super().__init__(file_path)
        
        # Default field mapping
        self.field_mapping = {
            'findings': 'findings',  # Array of findings
            'id': 'id',
            'title': 'title',
            'description': 'description',
            'severity': 'severity',
            'file_path': 'file',
            'line_number': 'line',
            'code_snippet': 'snippet',
            'algorithm': 'algorithm',
            'usage_context': 'context',
            'cwe': 'cwe',
            'references': 'references'
        }
        
        # Override with custom mapping if provided
        if field_mapping:
            self.field_mapping.update(field_mapping)
    
    def validate_format(self) -> bool:
        """
        Validate JSON format structure.
        
        Returns:
            True if valid JSON with findings array
        """
        if not self.raw_data:
            self.load_file()
        
        # Check if findings array exists
        findings_key = self.field_mapping['findings']
        
        # Support both direct array and nested object
        if isinstance(self.raw_data, list):
            return True
        elif isinstance(self.raw_data, dict):
            # Support standard format: {"findings": [...]}
            if findings_key in self.raw_data and isinstance(self.raw_data[findings_key], list):
                return True
            # Support output format: {"prioritized_findings": [...]}
            if 'prioritized_findings' in self.raw_data and isinstance(self.raw_data['prioritized_findings'], list):
                return True
        
        return False
    
    def parse(self) -> List[CryptoFinding]:
        """
        Parse JSON results and extract crypto findings.
        
        Returns:
            List of CryptoFinding objects
        """
        if not self.raw_data:
            self.load_file()
        
        if not self.validate_format():
            raise ValueError("Invalid JSON format: expected findings array")
        
        findings = []
        
        # Get findings array
        findings_key = self.field_mapping['findings']
        if isinstance(self.raw_data, list):
            raw_findings = self.raw_data
        elif 'prioritized_findings' in self.raw_data:
            # Handle output format: extract the nested finding objects
            prioritized = self.raw_data['prioritized_findings']
            raw_findings = []
            for pf in prioritized:
                if isinstance(pf, dict) and 'finding' in pf:
                    raw_findings.append(pf['finding'])
        else:
            raw_findings = self.raw_data.get(findings_key, [])
        
        for idx, finding_data in enumerate(raw_findings):
            # Skip if not a dict
            if not isinstance(finding_data, dict):
                continue
            
            # Only process crypto-related findings
            if not self.is_crypto_related(finding_data):
                continue
            
            finding = self._parse_finding(finding_data, idx)
            if finding:
                findings.append(finding)
        
        return findings
    
    def _parse_finding(self, finding_data: Dict[str, Any], idx: int) -> Optional[CryptoFinding]:
        """
        Parse a single finding into a CryptoFinding.
        
        Args:
            finding_data: Raw finding data
            idx: Finding index
            
        Returns:
            CryptoFinding object or None
        """
        try:
            # Extract fields using mapping
            finding_id = self._get_field(finding_data, 'id', f'json-{idx}')
            title = self._get_field(finding_data, 'title', 'Untitled Finding')
            description = self._get_field(finding_data, 'description', '')
            
            # Map severity
            severity_str = self._get_field(finding_data, 'severity', 'medium')
            severity = self._map_severity(severity_str)
            
            # Extract location
            file_path = self._get_field(finding_data, 'file_path')
            line_number = self._get_field(finding_data, 'line_number')
            code_snippet = self._get_field(finding_data, 'code_snippet')
            
            # Detect or extract algorithm
            algorithm_str = self._get_field(finding_data, 'algorithm')
            if algorithm_str:
                algorithm = self._parse_algorithm(algorithm_str)
            else:
                algorithm = self._detect_algorithm(title + ' ' + description)
            
            # Detect or extract usage context
            context_str = self._get_field(finding_data, 'usage_context')
            if context_str:
                usage_context = self._parse_usage_context(context_str)
            else:
                usage_context = self._detect_usage_context(title + ' ' + description)
            
            # Extract CWE IDs
            cwe_ids = self.extract_cwe_ids(finding_data)
            
            # Extract references
            references = self._get_field(finding_data, 'references', [])
            if not isinstance(references, list):
                references = [str(references)]
            
            finding = CryptoFinding(
                id=str(finding_id),
                title=title,
                description=description,
                severity=severity,
                algorithm=algorithm,
                usage_context=usage_context,
                file_path=file_path,
                line_number=int(line_number) if line_number else None,
                code_snippet=code_snippet,
                cwe_ids=cwe_ids,
                references=references,
                metadata=finding_data
            )
            
            return finding
            
        except Exception as e:
            print(f"Warning: Failed to parse JSON finding {idx}: {e}")
            return None
    
    def _get_field(self, data: Dict[str, Any], field_key: str, default: Any = None) -> Any:
        """
        Get field value using field mapping.
        
        Args:
            data: Finding data dictionary
            field_key: Standard field key
            default: Default value if not found
            
        Returns:
            Field value or default
        """
        mapped_key = self.field_mapping.get(field_key, field_key)
        
        # Try direct access
        if mapped_key in data:
            return data[mapped_key]
        
        # Try nested access (e.g., "location.file")
        if '.' in mapped_key:
            keys = mapped_key.split('.')
            value = data
            for key in keys:
                if isinstance(value, dict) and key in value:
                    value = value[key]
                else:
                    return default
            return value
        
        return default
    
    def _map_severity(self, severity_str: str) -> Severity:
        """Map severity string to Severity enum."""
        severity_lower = str(severity_str).lower()
        
        if severity_lower in ['critical', 'crit']:
            return Severity.CRITICAL
        elif severity_lower in ['high', 'error']:
            return Severity.HIGH
        elif severity_lower in ['medium', 'med', 'warning', 'warn']:
            return Severity.MEDIUM
        elif severity_lower in ['low']:
            return Severity.LOW
        else:
            return Severity.INFO
    
    def _parse_algorithm(self, algorithm_str: str) -> CryptoAlgorithm:
        """Parse algorithm string to CryptoAlgorithm enum."""
        algo_lower = algorithm_str.lower().replace('-', '').replace('_', '')
        
        mapping = {
            'md5': CryptoAlgorithm.MD5,
            'sha1': CryptoAlgorithm.SHA1,
            'des': CryptoAlgorithm.DES,
            '3des': CryptoAlgorithm.TRIPLE_DES,
            'tripledes': CryptoAlgorithm.TRIPLE_DES,
            'rc4': CryptoAlgorithm.RC4,
            'rsa1024': CryptoAlgorithm.RSA_1024,
            'rsa2048': CryptoAlgorithm.RSA_2048,
            'aes128': CryptoAlgorithm.AES_128,
            'aes256': CryptoAlgorithm.AES_256,
            'ecdsap256': CryptoAlgorithm.ECDSA_P256,
        }
        
        return mapping.get(algo_lower, CryptoAlgorithm.UNKNOWN)
    
    def _parse_usage_context(self, context_str: str) -> UsageContext:
        """Parse usage context string to UsageContext enum."""
        context_lower = context_str.lower().replace('-', '_')
        
        try:
            return UsageContext(context_lower)
        except ValueError:
            return UsageContext.UNKNOWN
    
    def _detect_algorithm(self, text: str) -> CryptoAlgorithm:
        """Detect algorithm from text."""
        text_lower = text.lower()
        
        if 'md5' in text_lower:
            return CryptoAlgorithm.MD5
        elif 'sha-1' in text_lower or 'sha1' in text_lower:
            return CryptoAlgorithm.SHA1
        elif 'des' in text_lower and '3des' not in text_lower:
            return CryptoAlgorithm.DES
        elif '3des' in text_lower or 'triple-des' in text_lower:
            return CryptoAlgorithm.TRIPLE_DES
        elif 'rc4' in text_lower:
            return CryptoAlgorithm.RC4
        elif 'rsa' in text_lower:
            if '1024' in text_lower:
                return CryptoAlgorithm.RSA_1024
            return CryptoAlgorithm.RSA_2048
        elif 'aes' in text_lower:
            if '128' in text_lower:
                return CryptoAlgorithm.AES_128
            return CryptoAlgorithm.AES_256
        
        return CryptoAlgorithm.UNKNOWN
    
    def _detect_usage_context(self, text: str) -> UsageContext:
        """Detect usage context from text."""
        text_lower = text.lower()
        
        if any(word in text_lower for word in ['auth', 'login', 'credential']):
            return UsageContext.AUTHENTICATION
        elif 'key storage' in text_lower or 'keystore' in text_lower:
            return UsageContext.KEY_STORAGE
        elif any(word in text_lower for word in ['encrypt', 'decrypt']):
            return UsageContext.DATA_ENCRYPTION
        elif any(word in text_lower for word in ['sign', 'signature']):
            return UsageContext.SIGNING
        elif 'hash' in text_lower:
            return UsageContext.HASHING
        
        return UsageContext.UNKNOWN