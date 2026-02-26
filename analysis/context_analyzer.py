"""
Context analyzer for extracting usage patterns and environmental context
from crypto findings to inform risk scoring.
"""
from typing import Dict, Any, Optional
from ..models import (
    CryptoFinding, DataSensitivity, ExposureDuration,
    UsageContext, CryptoAlgorithm
)


class ContextAnalyzer:
    """Analyzes crypto findings to extract contextual information for risk scoring."""
    
    def __init__(self):
        """Initialize the context analyzer."""
        self.sensitivity_keywords = {
            DataSensitivity.CRITICAL: [
                'private key', 'master key', 'root password', 'admin credential',
                'encryption key', 'signing key', 'certificate private'
            ],
            DataSensitivity.RESTRICTED: [
                'password', 'secret', 'token', 'api key', 'credential',
                'authentication', 'authorization', 'session'
            ],
            DataSensitivity.CONFIDENTIAL: [
                'personal', 'pii', 'financial', 'payment', 'credit card',
                'ssn', 'health', 'medical', 'confidential'
            ],
            DataSensitivity.INTERNAL: [
                'internal', 'private', 'protected', 'backend'
            ],
            DataSensitivity.PUBLIC: [
                'public', 'open', 'anonymous', 'guest'
            ]
        }
        
        self.duration_keywords = {
            ExposureDuration.PERSISTENT: [
                'database', 'storage', 'persistent', 'long-term', 'archive',
                'backup', 'stored', 'saved', 'file system'
            ],
            ExposureDuration.LONG_TERM: [
                'session', 'cache', 'temporary file', 'temp storage'
            ],
            ExposureDuration.MEDIUM_TERM: [
                'request', 'response', 'transaction', 'processing'
            ],
            ExposureDuration.SHORT_TERM: [
                'memory', 'buffer', 'in-memory', 'runtime'
            ],
            ExposureDuration.EPHEMERAL: [
                'ephemeral', 'transient', 'temporary', 'volatile', 'one-time'
            ]
        }
    
    def analyze(self, finding: CryptoFinding) -> Dict[str, Any]:
        """
        Analyze a finding to extract contextual information.
        
        Args:
            finding: CryptoFinding to analyze
            
        Returns:
            Dictionary with analyzed context information
        """
        context = {
            'data_sensitivity': self.infer_data_sensitivity(finding),
            'exposure_duration': self.infer_exposure_duration(finding),
            'is_external_facing': self.is_external_facing(finding),
            'has_authentication': self.involves_authentication(finding),
            'affects_multiple_systems': self.affects_multiple_systems(finding),
            'exploitability_factors': self.assess_exploitability(finding)
        }
        
        return context
    
    def infer_data_sensitivity(self, finding: CryptoFinding) -> DataSensitivity:
        """
        Infer data sensitivity level from finding details.
        
        Args:
            finding: CryptoFinding to analyze
            
        Returns:
            DataSensitivity level
        """
        # Use explicit sensitivity if provided
        if finding.data_sensitivity:
            return finding.data_sensitivity
        
        # Combine text for analysis
        text = f"{finding.title} {finding.description}".lower()
        if finding.file_path:
            text += f" {finding.file_path}".lower()
        
        # Check keywords in order of severity (highest first)
        for sensitivity, keywords in self.sensitivity_keywords.items():
            if any(keyword in text for keyword in keywords):
                return sensitivity
        
        # Default based on usage context
        if finding.usage_context == UsageContext.AUTHENTICATION:
            return DataSensitivity.RESTRICTED
        elif finding.usage_context == UsageContext.KEY_STORAGE:
            return DataSensitivity.CRITICAL
        
        return DataSensitivity.INTERNAL
    
    def infer_exposure_duration(self, finding: CryptoFinding) -> ExposureDuration:
        """
        Infer exposure duration from finding details.
        
        Args:
            finding: CryptoFinding to analyze
            
        Returns:
            ExposureDuration level
        """
        # Use explicit duration if provided
        if finding.exposure_duration:
            return finding.exposure_duration
        
        # Combine text for analysis
        text = f"{finding.title} {finding.description}".lower()
        if finding.file_path:
            text += f" {finding.file_path}".lower()
        
        # Check keywords in order (longest duration first)
        for duration, keywords in self.duration_keywords.items():
            if any(keyword in text for keyword in keywords):
                return duration
        
        # Default based on usage context
        if finding.usage_context == UsageContext.KEY_STORAGE:
            return ExposureDuration.PERSISTENT
        elif finding.usage_context == UsageContext.AUTHENTICATION:
            return ExposureDuration.LONG_TERM
        
        return ExposureDuration.MEDIUM_TERM
    
    def is_external_facing(self, finding: CryptoFinding) -> bool:
        """
        Determine if the finding affects external-facing components.
        
        Args:
            finding: CryptoFinding to analyze
            
        Returns:
            True if external-facing
        """
        # Use explicit flag if provided
        if finding.is_external_facing:
            return True
        
        text = f"{finding.title} {finding.description}".lower()
        if finding.file_path:
            text += f" {finding.file_path}".lower()
        
        external_keywords = [
            'api', 'endpoint', 'public', 'external', 'internet',
            'web', 'http', 'rest', 'graphql', 'client', 'frontend'
        ]
        
        internal_keywords = [
            'internal', 'backend', 'private', 'local', 'localhost'
        ]
        
        # Check for internal keywords first (more specific)
        if any(keyword in text for keyword in internal_keywords):
            return False
        
        # Check for external keywords
        if any(keyword in text for keyword in external_keywords):
            return True
        
        # Authentication and signing are often external-facing
        if finding.usage_context in [UsageContext.AUTHENTICATION, UsageContext.SIGNING]:
            return True
        
        return False
    
    def involves_authentication(self, finding: CryptoFinding) -> bool:
        """
        Check if finding involves authentication mechanisms.
        
        Args:
            finding: CryptoFinding to analyze
            
        Returns:
            True if involves authentication
        """
        if finding.usage_context == UsageContext.AUTHENTICATION:
            return True
        
        text = f"{finding.title} {finding.description}".lower()
        
        auth_keywords = [
            'auth', 'login', 'credential', 'password', 'token',
            'session', 'jwt', 'oauth', 'saml', 'sso'
        ]
        
        return any(keyword in text for keyword in auth_keywords)
    
    def affects_multiple_systems(self, finding: CryptoFinding) -> bool:
        """
        Determine if the finding affects multiple systems (blast radius).
        
        Args:
            finding: CryptoFinding to analyze
            
        Returns:
            True if affects multiple systems
        """
        text = f"{finding.title} {finding.description}".lower()
        
        multi_system_keywords = [
            'shared', 'common', 'library', 'framework', 'service',
            'microservice', 'distributed', 'cluster', 'multiple',
            'all', 'global', 'system-wide'
        ]
        
        return any(keyword in text for keyword in multi_system_keywords)
    
    def assess_exploitability(self, finding: CryptoFinding) -> Dict[str, Any]:
        """
        Assess exploitability factors for the finding.
        
        Args:
            finding: CryptoFinding to analyze
            
        Returns:
            Dictionary with exploitability assessment
        """
        factors = {
            'algorithm_weakness': self._assess_algorithm_weakness(finding.algorithm),
            'known_attacks': self._has_known_attacks(finding.algorithm),
            'complexity': self._assess_exploit_complexity(finding),
            'prerequisites': self._assess_prerequisites(finding)
        }
        
        return factors
    
    def _assess_algorithm_weakness(self, algorithm: CryptoAlgorithm) -> str:
        """Assess the weakness level of the algorithm."""
        critical_weak = [CryptoAlgorithm.MD5, CryptoAlgorithm.DES, CryptoAlgorithm.RC4]
        high_weak = [CryptoAlgorithm.SHA1, CryptoAlgorithm.TRIPLE_DES, CryptoAlgorithm.RSA_1024]
        
        if algorithm in critical_weak:
            return "critical"
        elif algorithm in high_weak:
            return "high"
        elif algorithm == CryptoAlgorithm.UNKNOWN:
            return "unknown"
        else:
            return "low"
    
    def _has_known_attacks(self, algorithm: CryptoAlgorithm) -> bool:
        """Check if algorithm has known practical attacks."""
        vulnerable_algorithms = [
            CryptoAlgorithm.MD5,
            CryptoAlgorithm.SHA1,
            CryptoAlgorithm.DES,
            CryptoAlgorithm.RC4,
            CryptoAlgorithm.RSA_1024
        ]
        
        return algorithm in vulnerable_algorithms
    
    def _assess_exploit_complexity(self, finding: CryptoFinding) -> str:
        """Assess the complexity of exploiting the vulnerability."""
        # External-facing issues are easier to exploit
        if finding.is_external_facing:
            return "low"
        
        # Authentication issues are often easier to exploit
        if finding.usage_context == UsageContext.AUTHENTICATION:
            return "low"
        
        # Internal issues require more access
        if finding.usage_context == UsageContext.INTERNAL:
            return "high"
        
        return "medium"
    
    def _assess_prerequisites(self, finding: CryptoFinding) -> str:
        """Assess prerequisites needed to exploit the vulnerability."""
        if finding.is_external_facing:
            return "none"
        
        text = f"{finding.title} {finding.description}".lower()
        
        if any(word in text for word in ['internal', 'private', 'backend']):
            return "internal_access"
        
        if any(word in text for word in ['admin', 'privileged', 'root']):
            return "privileged_access"
        
        return "authenticated_access"