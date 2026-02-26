"""
Base parser interface for vulnerability scan results.
"""
from abc import ABC, abstractmethod
from typing import List, Dict, Any
from pathlib import Path
import json

from ..models import CryptoFinding


class BaseParser(ABC):
    """Abstract base class for vulnerability scan parsers."""
    
    def __init__(self, file_path: str):
        """
        Initialize parser with file path.
        
        Args:
            file_path: Path to the scan results file
        """
        self.file_path = Path(file_path)
        if not self.file_path.exists():
            raise FileNotFoundError(f"File not found: {file_path}")
        
        self.raw_data: Dict[str, Any] = {}
    
    def load_file(self) -> Dict[str, Any]:
        """
        Load and parse the JSON file.
        
        Returns:
            Parsed JSON data as dictionary
        """
        with open(self.file_path, 'r', encoding='utf-8') as f:
            self.raw_data = json.load(f)
        return self.raw_data
    
    @abstractmethod
    def parse(self) -> List[CryptoFinding]:
        """
        Parse the scan results and extract crypto findings.
        
        Returns:
            List of CryptoFinding objects
        """
        pass
    
    @abstractmethod
    def validate_format(self) -> bool:
        """
        Validate that the file format matches expected structure.
        
        Returns:
            True if format is valid, False otherwise
        """
        pass
    
    def is_crypto_related(self, finding_data: Dict[str, Any]) -> bool:
        """
        Determine if a finding is crypto-related based on keywords.
        
        Args:
            finding_data: Raw finding data
            
        Returns:
            True if crypto-related, False otherwise
        """
        crypto_keywords = [
            'crypto', 'encryption', 'decrypt', 'cipher', 'hash',
            'md5', 'sha1', 'sha-1', 'des', 'rc4', 'rsa', 'aes',
            'ecdsa', 'key', 'certificate', 'ssl', 'tls',
            'signature', 'signing', 'random', 'prng',
            'password', 'secret', 'token', 'authentication'
        ]
        
        # Check in title, description, and message fields
        text_fields = []
        if 'title' in finding_data:
            text_fields.append(str(finding_data['title']).lower())
        if 'description' in finding_data:
            text_fields.append(str(finding_data['description']).lower())
        if 'message' in finding_data:
            text_fields.append(str(finding_data['message']).lower())
        
        combined_text = ' '.join(text_fields)
        
        return any(keyword in combined_text for keyword in crypto_keywords)
    
    def extract_cwe_ids(self, finding_data: Dict[str, Any]) -> List[str]:
        """
        Extract CWE identifiers from finding data.
        
        Args:
            finding_data: Raw finding data
            
        Returns:
            List of CWE IDs
        """
        cwe_ids = []
        
        # Check common CWE field locations
        if 'cwe' in finding_data:
            cwe = finding_data['cwe']
            if isinstance(cwe, list):
                cwe_ids.extend([str(c) for c in cwe])
            else:
                cwe_ids.append(str(cwe))
        
        if 'cweIds' in finding_data:
            cwe_ids.extend([str(c) for c in finding_data['cweIds']])
        
        if 'properties' in finding_data and 'cwe' in finding_data['properties']:
            cwe_ids.append(str(finding_data['properties']['cwe']))
        
        return list(set(cwe_ids))  # Remove duplicates