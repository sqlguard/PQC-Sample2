"""
Base plugin interface for security tool integrations.
"""

from abc import ABC, abstractmethod
from typing import List, Dict, Any, Optional
from ..models import CryptoFinding


class BasePlugin(ABC):
    """
    Abstract base class for security tool integration plugins.
    
    All plugins must implement:
    - fetch_findings(): Retrieve findings from the tool
    - enrich_context(): Add repository/project context
    - authenticate(): Handle authentication
    """
    
    def __init__(self, config: Dict[str, Any]):
        """
        Initialize the plugin with configuration.
        
        Args:
            config: Plugin configuration (API keys, URLs, etc.)
        """
        self.config = config
        self.authenticated = False
    
    @abstractmethod
    def authenticate(self) -> bool:
        """
        Authenticate with the security tool.
        
        Returns:
            True if authentication successful
        """
        pass
    
    @abstractmethod
    def fetch_findings(
        self,
        repository: Optional[str] = None,
        project: Optional[str] = None,
        **kwargs
    ) -> List[CryptoFinding]:
        """
        Fetch crypto-related findings from the tool.
        
        Args:
            repository: Repository name/URL
            project: Project identifier
            **kwargs: Additional tool-specific parameters
            
        Returns:
            List of CryptoFinding objects
        """
        pass
    
    @abstractmethod
    def enrich_context(
        self,
        finding: CryptoFinding,
        repository: Optional[str] = None
    ) -> CryptoFinding:
        """
        Enrich finding with repository/project context.
        
        Args:
            finding: CryptoFinding to enrich
            repository: Repository name/URL
            
        Returns:
            Enriched CryptoFinding
        """
        pass
    
    def test_connection(self) -> bool:
        """
        Test connection to the security tool.
        
        Returns:
            True if connection successful
        """
        try:
            return self.authenticate()
        except Exception:
            return False
    
    def get_supported_algorithms(self) -> List[str]:
        """
        Get list of crypto algorithms this plugin can detect.
        
        Returns:
            List of algorithm names
        """
        return [
            'MD5', 'SHA1', 'DES', '3DES', 'RC4',
            'RSA-1024', 'RSA-2048', 'AES-128', 'AES-256'
        ]