"""
Impact factor calculator for converting contextual information
into numerical scores for risk assessment.
"""
from typing import Dict, Any
from ..models import (
    CryptoFinding, ImpactFactors, DataSensitivity,
    ExposureDuration, UsageContext, CryptoAlgorithm
)


class ImpactFactorCalculator:
    """Calculates numerical impact factors from finding context."""
    
    def __init__(self):
        """Initialize the impact factor calculator with scoring tables."""
        
        # Data sensitivity scoring (1-10)
        self.sensitivity_scores = {
            DataSensitivity.PUBLIC: 1,
            DataSensitivity.INTERNAL: 3,
            DataSensitivity.CONFIDENTIAL: 6,
            DataSensitivity.RESTRICTED: 8,
            DataSensitivity.CRITICAL: 10
        }
        
        # Exposure duration scoring (1-10)
        self.duration_scores = {
            ExposureDuration.EPHEMERAL: 2,
            ExposureDuration.SHORT_TERM: 4,
            ExposureDuration.MEDIUM_TERM: 6,
            ExposureDuration.LONG_TERM: 8,
            ExposureDuration.PERSISTENT: 10
        }
        
        # Algorithm weakness scoring (1-10)
        self.algorithm_scores = {
            CryptoAlgorithm.MD5: 10,
            CryptoAlgorithm.DES: 10,
            CryptoAlgorithm.RC4: 10,
            CryptoAlgorithm.SHA1: 8,
            CryptoAlgorithm.TRIPLE_DES: 7,
            CryptoAlgorithm.RSA_1024: 7,
            CryptoAlgorithm.RSA_2048: 4,
            CryptoAlgorithm.AES_128: 3,
            CryptoAlgorithm.AES_256: 2,
            CryptoAlgorithm.ECDSA_P256: 3,
            CryptoAlgorithm.UNKNOWN: 5
        }
        
        # Usage context multipliers
        self.context_multipliers = {
            UsageContext.AUTHENTICATION: 2.0,
            UsageContext.KEY_STORAGE: 1.8,
            UsageContext.DATA_ENCRYPTION: 1.5,
            UsageContext.SIGNING: 1.4,
            UsageContext.HASHING: 1.2,
            UsageContext.INTERNAL: 0.8,
            UsageContext.UNKNOWN: 1.0
        }
    
    def calculate(self, finding: CryptoFinding, context: Dict[str, Any]) -> ImpactFactors:
        """
        Calculate impact factors for a finding.
        
        Args:
            finding: CryptoFinding to score
            context: Context information from ContextAnalyzer
            
        Returns:
            ImpactFactors with numerical scores
        """
        data_sensitivity = self._calculate_data_sensitivity(
            context.get('data_sensitivity', DataSensitivity.INTERNAL)
        )
        
        exposure_duration = self._calculate_exposure_duration(
            context.get('exposure_duration', ExposureDuration.MEDIUM_TERM)
        )
        
        exploitability = self._calculate_exploitability(
            finding,
            context
        )
        
        blast_radius = self._calculate_blast_radius(
            finding,
            context
        )
        
        algorithm_weakness = self._calculate_algorithm_weakness(
            finding.algorithm
        )
        
        return ImpactFactors(
            data_sensitivity=data_sensitivity,
            exposure_duration=exposure_duration,
            exploitability=exploitability,
            blast_radius=blast_radius,
            algorithm_weakness=algorithm_weakness
        )
    
    def get_context_multiplier(self, usage_context: UsageContext) -> float:
        """
        Get the context multiplier for a usage context.
        
        Args:
            usage_context: Usage context
            
        Returns:
            Multiplier value
        """
        return self.context_multipliers.get(usage_context, 1.0)
    
    def _calculate_data_sensitivity(self, sensitivity: DataSensitivity) -> int:
        """Calculate data sensitivity score."""
        return self.sensitivity_scores.get(sensitivity, 5)
    
    def _calculate_exposure_duration(self, duration: ExposureDuration) -> int:
        """Calculate exposure duration score."""
        return self.duration_scores.get(duration, 5)
    
    def _calculate_exploitability(self, finding: CryptoFinding, context: Dict[str, Any]) -> int:
        """
        Calculate exploitability score based on multiple factors.
        
        Args:
            finding: CryptoFinding
            context: Context information
            
        Returns:
            Exploitability score (1-10)
        """
        base_score = 5
        
        # External-facing increases exploitability
        if context.get('is_external_facing', False):
            base_score += 3
        
        # Known attacks increase exploitability
        exploit_factors = context.get('exploitability_factors', {})
        if exploit_factors.get('known_attacks', False):
            base_score += 2
        
        # Complexity affects exploitability
        complexity = exploit_factors.get('complexity', 'medium')
        if complexity == 'low':
            base_score += 1
        elif complexity == 'high':
            base_score -= 2
        
        # Prerequisites affect exploitability
        prerequisites = exploit_factors.get('prerequisites', 'authenticated_access')
        if prerequisites == 'none':
            base_score += 2
        elif prerequisites == 'privileged_access':
            base_score -= 2
        
        # Clamp to valid range
        return max(1, min(10, base_score))
    
    def _calculate_blast_radius(self, finding: CryptoFinding, context: Dict[str, Any]) -> int:
        """
        Calculate blast radius (impact scope) score.
        
        Args:
            finding: CryptoFinding
            context: Context information
            
        Returns:
            Blast radius score (1-10)
        """
        base_score = 5
        
        # Multiple systems affected
        if context.get('affects_multiple_systems', False):
            base_score += 3
        
        # Authentication affects many users
        if context.get('has_authentication', False):
            base_score += 2
        
        # External-facing has wider impact
        if context.get('is_external_facing', False):
            base_score += 2
        
        # Key storage affects all encrypted data
        if finding.usage_context == UsageContext.KEY_STORAGE:
            base_score += 3
        
        # Critical data sensitivity increases blast radius
        data_sensitivity = context.get('data_sensitivity', DataSensitivity.INTERNAL)
        if data_sensitivity == DataSensitivity.CRITICAL:
            base_score += 2
        elif data_sensitivity == DataSensitivity.RESTRICTED:
            base_score += 1
        
        # Clamp to valid range
        return max(1, min(10, base_score))
    
    def _calculate_algorithm_weakness(self, algorithm: CryptoAlgorithm) -> int:
        """Calculate algorithm weakness score."""
        return self.algorithm_scores.get(algorithm, 5)
    
    def calculate_priority_adjustments(self, finding: CryptoFinding, context: Dict[str, Any]) -> Dict[str, float]:
        """
        Calculate additional priority adjustments based on special conditions.
        
        Args:
            finding: CryptoFinding
            context: Context information
            
        Returns:
            Dictionary of adjustment factors
        """
        adjustments = {}
        
        # Compliance-related findings get priority boost
        if any(cwe in ['CWE-327', 'CWE-326', 'CWE-328'] for cwe in finding.cwe_ids):
            adjustments['compliance'] = 1.2
        
        # Findings with known exploits get priority boost
        exploit_factors = context.get('exploitability_factors', {})
        if exploit_factors.get('known_attacks', False):
            adjustments['known_exploit'] = 1.3
        
        # Authentication-related findings in external systems
        if context.get('has_authentication') and context.get('is_external_facing'):
            adjustments['auth_external'] = 1.4
        
        # Persistent storage of critical data
        if (context.get('exposure_duration') == ExposureDuration.PERSISTENT and
            context.get('data_sensitivity') == DataSensitivity.CRITICAL):
            adjustments['persistent_critical'] = 1.3
        
        return adjustments