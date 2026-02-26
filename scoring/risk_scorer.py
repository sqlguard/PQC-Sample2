"""
Risk scoring engine for calculating and prioritizing crypto vulnerabilities.
"""
from typing import List, Dict, Any, Optional
from ..models import CryptoFinding, RiskScore, PrioritizedFinding
from ..analysis.context_analyzer import ContextAnalyzer
from ..analysis.impact_factors import ImpactFactorCalculator


class RiskScorer:
    """
    Risk scoring engine that calculates risk scores for crypto findings
    based on impact factors and usage context.
    """
    
    def __init__(self, custom_weights: Optional[Dict[str, float]] = None):
        """
        Initialize the risk scorer.
        
        Args:
            custom_weights: Optional custom weights for scoring factors
        """
        # Default weights for base score calculation
        self.weights = {
            'data_sensitivity': 0.25,
            'exposure_duration': 0.20,
            'exploitability': 0.20,
            'blast_radius': 0.25,
            'algorithm_weakness': 0.10
        }
        
        # Override with custom weights if provided
        if custom_weights:
            self.weights.update(custom_weights)
        
        # Validate weights sum to 1.0
        total_weight = sum(self.weights.values())
        if not 0.99 <= total_weight <= 1.01:  # Allow small floating point errors
            raise ValueError(f"Weights must sum to 1.0, got {total_weight}")
        
        self.context_analyzer = ContextAnalyzer()
        self.impact_calculator = ImpactFactorCalculator()
    
    def score_finding(self, finding: CryptoFinding) -> RiskScore:
        """
        Calculate risk score for a single finding.
        
        Args:
            finding: CryptoFinding to score
            
        Returns:
            RiskScore with calculated scores
        """
        # Analyze context
        context = self.context_analyzer.analyze(finding)
        
        # Calculate impact factors
        impact_factors = self.impact_calculator.calculate(finding, context)
        
        # Calculate base score (0-10)
        base_score = self._calculate_base_score(impact_factors)
        
        # Get context multiplier
        context_multiplier = self.impact_calculator.get_context_multiplier(
            finding.usage_context
        )
        
        # Apply additional adjustments
        adjustments = self.impact_calculator.calculate_priority_adjustments(
            finding, context
        )
        for adjustment_value in adjustments.values():
            context_multiplier *= adjustment_value
        
        # Clamp multiplier to valid range
        context_multiplier = max(0.5, min(2.0, context_multiplier))
        
        # Calculate final score
        final_score = base_score * context_multiplier
        final_score = min(20.0, final_score)  # Cap at 20
        
        return RiskScore(
            finding_id=finding.id,
            base_score=round(base_score, 2),
            context_multiplier=round(context_multiplier, 2),
            final_score=round(final_score, 2),
            impact_factors=impact_factors
        )
    
    def score_findings(self, findings: List[CryptoFinding]) -> List[RiskScore]:
        """
        Calculate risk scores for multiple findings.
        
        Args:
            findings: List of CryptoFindings to score
            
        Returns:
            List of RiskScores
        """
        return [self.score_finding(finding) for finding in findings]
    
    def prioritize_findings(
        self,
        findings: List[CryptoFinding],
        include_guidance: bool = True
    ) -> List[PrioritizedFinding]:
        """
        Score and prioritize findings in order of risk.
        
        Args:
            findings: List of CryptoFindings to prioritize
            include_guidance: Whether to include remediation guidance
            
        Returns:
            List of PrioritizedFindings sorted by priority (highest first)
        """
        prioritized = []
        
        for finding in findings:
            risk_score = self.score_finding(finding)
            
            guidance = None
            effort = None
            if include_guidance:
                guidance = self._generate_remediation_guidance(finding, risk_score)
                effort = self._estimate_effort(finding, risk_score)
            
            prioritized.append(PrioritizedFinding(
                finding=finding,
                risk_score=risk_score,
                remediation_guidance=guidance,
                estimated_effort=effort
            ))
        
        # Sort by final score (descending)
        prioritized.sort(key=lambda x: x.risk_score.final_score, reverse=True)
        
        # Assign priority ranks
        for rank, pf in enumerate(prioritized, start=1):
            pf.risk_score.priority_rank = rank
        
        return prioritized
    
    def _calculate_base_score(self, impact_factors) -> float:
        """
        Calculate base risk score from impact factors.
        
        Args:
            impact_factors: ImpactFactors object
            
        Returns:
            Base score (0-10)
        """
        score = (
            impact_factors.data_sensitivity * self.weights['data_sensitivity'] +
            impact_factors.exposure_duration * self.weights['exposure_duration'] +
            impact_factors.exploitability * self.weights['exploitability'] +
            impact_factors.blast_radius * self.weights['blast_radius'] +
            impact_factors.algorithm_weakness * self.weights['algorithm_weakness']
        )
        
        return score
    
    def _generate_remediation_guidance(
        self,
        finding: CryptoFinding,
        risk_score: RiskScore
    ) -> str:
        """
        Generate remediation guidance based on the finding and risk score.
        
        Args:
            finding: CryptoFinding
            risk_score: Calculated RiskScore
            
        Returns:
            Remediation guidance text
        """
        guidance_parts = []
        
        # Priority level guidance
        priority = risk_score.priority_level
        if priority == "CRITICAL":
            guidance_parts.append("🚨 CRITICAL: Immediate action required.")
        elif priority == "HIGH":
            guidance_parts.append("⚠️ HIGH: Address within current sprint.")
        elif priority == "MEDIUM":
            guidance_parts.append("📋 MEDIUM: Schedule for next sprint.")
        else:
            guidance_parts.append("ℹ️ LOW: Address in regular maintenance cycle.")
        
        # Algorithm-specific guidance
        from ..models import CryptoAlgorithm
        algo_guidance = {
            CryptoAlgorithm.MD5: "Replace MD5 with SHA-256 or SHA-3 for hashing.",
            CryptoAlgorithm.SHA1: "Migrate from SHA-1 to SHA-256 or SHA-3.",
            CryptoAlgorithm.DES: "Replace DES with AES-256.",
            CryptoAlgorithm.TRIPLE_DES: "Migrate from 3DES to AES-256.",
            CryptoAlgorithm.RC4: "Replace RC4 with AES-GCM or ChaCha20-Poly1305.",
            CryptoAlgorithm.RSA_1024: "Upgrade to RSA-2048 or higher, or consider ECDSA.",
        }
        
        if finding.algorithm in algo_guidance:
            guidance_parts.append(algo_guidance[finding.algorithm])
        
        # Context-specific guidance
        from ..models import UsageContext
        if finding.usage_context == UsageContext.AUTHENTICATION:
            guidance_parts.append("Consider implementing multi-factor authentication.")
        elif finding.usage_context == UsageContext.KEY_STORAGE:
            guidance_parts.append("Use hardware security modules (HSM) or key management services.")
        
        # External-facing guidance
        if finding.is_external_facing:
            guidance_parts.append("External-facing: Prioritize due to increased attack surface.")
        
        return " ".join(guidance_parts)
    
    def _estimate_effort(self, finding: CryptoFinding, risk_score: RiskScore) -> str:
        """
        Estimate remediation effort.
        
        Args:
            finding: CryptoFinding
            risk_score: Calculated RiskScore
            
        Returns:
            Effort estimate (Small/Medium/Large)
        """
        # Base effort on algorithm and context
        from ..models import CryptoAlgorithm, UsageContext
        
        # Simple algorithm replacements
        simple_replacements = [
            CryptoAlgorithm.MD5,
            CryptoAlgorithm.SHA1,
        ]
        
        # Complex migrations
        complex_migrations = [
            CryptoAlgorithm.DES,
            CryptoAlgorithm.TRIPLE_DES,
            CryptoAlgorithm.RC4,
        ]
        
        if finding.algorithm in simple_replacements:
            if finding.usage_context == UsageContext.HASHING:
                return "Small (1-2 days)"
            else:
                return "Medium (3-5 days)"
        
        elif finding.algorithm in complex_migrations:
            if finding.usage_context in [UsageContext.KEY_STORAGE, UsageContext.AUTHENTICATION]:
                return "Large (1-2 weeks)"
            else:
                return "Medium (3-5 days)"
        
        # Default based on blast radius
        if risk_score.impact_factors.blast_radius >= 8:
            return "Large (1-2 weeks)"
        elif risk_score.impact_factors.blast_radius >= 5:
            return "Medium (3-5 days)"
        else:
            return "Small (1-2 days)"
    
    def get_statistics(self, prioritized_findings: List[PrioritizedFinding]) -> Dict[str, Any]:
        """
        Generate statistics from prioritized findings.
        
        Args:
            prioritized_findings: List of prioritized findings
            
        Returns:
            Dictionary with statistics
        """
        if not prioritized_findings:
            return {
                'total': 0,
                'by_priority': {},
                'by_algorithm': {},
                'by_context': {},
                'average_score': 0.0
            }
        
        stats = {
            'total': len(prioritized_findings),
            'by_priority': {
                'CRITICAL': 0,
                'HIGH': 0,
                'MEDIUM': 0,
                'LOW': 0
            },
            'by_algorithm': {},
            'by_context': {},
            'average_score': 0.0,
            'highest_score': 0.0,
            'lowest_score': 20.0
        }
        
        total_score = 0.0
        
        for pf in prioritized_findings:
            # Priority counts
            priority = pf.risk_score.priority_level
            stats['by_priority'][priority] += 1
            
            # Algorithm counts
            algo = pf.finding.algorithm.value
            stats['by_algorithm'][algo] = stats['by_algorithm'].get(algo, 0) + 1
            
            # Context counts
            context = pf.finding.usage_context.value
            stats['by_context'][context] = stats['by_context'].get(context, 0) + 1
            
            # Score statistics
            score = pf.risk_score.final_score
            total_score += score
            stats['highest_score'] = max(stats['highest_score'], score)
            stats['lowest_score'] = min(stats['lowest_score'], score)
        
        stats['average_score'] = round(total_score / len(prioritized_findings), 2)
        
        return stats