"""
Core data models for crypto vulnerability analysis and risk scoring.
"""
from enum import Enum
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field, field_validator


class CryptoAlgorithm(str, Enum):
    """Common cryptographic algorithms."""
    MD5 = "MD5"
    SHA1 = "SHA1"
    DES = "DES"
    TRIPLE_DES = "3DES"
    RC4 = "RC4"
    RSA_1024 = "RSA-1024"
    RSA_2048 = "RSA-2048"
    AES_128 = "AES-128"
    AES_256 = "AES-256"
    ECDSA_P256 = "ECDSA-P256"
    UNKNOWN = "UNKNOWN"


class UsageContext(str, Enum):
    """Context in which crypto is used."""
    AUTHENTICATION = "authentication"
    KEY_STORAGE = "key_storage"
    DATA_ENCRYPTION = "data_encryption"
    SIGNING = "signing"
    HASHING = "hashing"
    INTERNAL = "internal"
    UNKNOWN = "unknown"


class DataSensitivity(str, Enum):
    """Data sensitivity classification."""
    PUBLIC = "public"
    INTERNAL = "internal"
    CONFIDENTIAL = "confidential"
    RESTRICTED = "restricted"
    CRITICAL = "critical"


class ExposureDuration(str, Enum):
    """How long the data/keys are exposed."""
    EPHEMERAL = "ephemeral"  # < 1 hour
    SHORT_TERM = "short_term"  # 1 hour - 1 day
    MEDIUM_TERM = "medium_term"  # 1 day - 1 month
    LONG_TERM = "long_term"  # 1 month - 1 year
    PERSISTENT = "persistent"  # > 1 year


class Severity(str, Enum):
    """Vulnerability severity levels."""
    CRITICAL = "critical"
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"
    INFO = "info"


class ImpactFactors(BaseModel):
    """Impact factors for risk scoring."""
    data_sensitivity: int = Field(ge=1, le=10, description="Data sensitivity score (1-10)")
    exposure_duration: int = Field(ge=1, le=10, description="Exposure duration score (1-10)")
    exploitability: int = Field(ge=1, le=10, description="Exploitability score (1-10)")
    blast_radius: int = Field(ge=1, le=10, description="Blast radius score (1-10)")
    algorithm_weakness: int = Field(ge=1, le=10, description="Algorithm weakness score (1-10)")
    
    @field_validator('data_sensitivity', 'exposure_duration', 'exploitability', 'blast_radius', 'algorithm_weakness')
    @classmethod
    def validate_score_range(cls, v: int) -> int:
        if not 1 <= v <= 10:
            raise ValueError("Score must be between 1 and 10")
        return v


class CryptoFinding(BaseModel):
    """Represents a single cryptographic vulnerability finding."""
    id: str = Field(description="Unique identifier for the finding")
    title: str = Field(description="Short title of the finding")
    description: str = Field(description="Detailed description")
    severity: Severity = Field(description="Original severity from scanner")
    algorithm: CryptoAlgorithm = Field(default=CryptoAlgorithm.UNKNOWN)
    usage_context: UsageContext = Field(default=UsageContext.UNKNOWN)
    
    # Location information
    file_path: Optional[str] = Field(default=None, description="File where issue was found")
    line_number: Optional[int] = Field(default=None, description="Line number")
    code_snippet: Optional[str] = Field(default=None, description="Relevant code snippet")
    
    # Context information
    data_sensitivity: Optional[DataSensitivity] = Field(default=None)
    exposure_duration: Optional[ExposureDuration] = Field(default=None)
    is_external_facing: bool = Field(default=False, description="Is this externally accessible?")
    
    # Additional metadata
    cwe_ids: List[str] = Field(default_factory=list, description="CWE identifiers")
    references: List[str] = Field(default_factory=list, description="Reference URLs")
    metadata: Dict[str, Any] = Field(default_factory=dict, description="Additional metadata")


class RiskScore(BaseModel):
    """Risk score calculation result."""
    finding_id: str
    base_score: float = Field(ge=0, le=10, description="Base risk score (0-10)")
    context_multiplier: float = Field(ge=0.5, le=2.0, description="Context multiplier")
    final_score: float = Field(ge=0, le=20, description="Final risk score")
    priority_rank: Optional[int] = Field(default=None, description="Priority ranking")
    impact_factors: ImpactFactors
    
    @property
    def priority_level(self) -> str:
        """Get priority level based on final score."""
        if self.final_score >= 15:
            return "CRITICAL"
        elif self.final_score >= 10:
            return "HIGH"
        elif self.final_score >= 5:
            return "MEDIUM"
        else:
            return "LOW"


class PrioritizedFinding(BaseModel):
    """A finding with its risk score and prioritization."""
    finding: CryptoFinding
    risk_score: RiskScore
    remediation_guidance: Optional[str] = Field(default=None)
    estimated_effort: Optional[str] = Field(default=None, description="Estimated remediation effort")


class RemediationReport(BaseModel):
    """Complete remediation prioritization report."""
    total_findings: int
    prioritized_findings: List[PrioritizedFinding]
    summary: Dict[str, Any] = Field(default_factory=dict)
    generated_at: str = Field(description="ISO timestamp of report generation")
    
    @property
    def critical_count(self) -> int:
        return sum(1 for pf in self.prioritized_findings if pf.risk_score.priority_level == "CRITICAL")
    
    @property
    def high_count(self) -> int:
        return sum(1 for pf in self.prioritized_findings if pf.risk_score.priority_level == "HIGH")
    
    @property
    def medium_count(self) -> int:
        return sum(1 for pf in self.prioritized_findings if pf.risk_score.priority_level == "MEDIUM")
    
    @property
    def low_count(self) -> int:
        return sum(1 for pf in self.prioritized_findings if pf.risk_score.priority_level == "LOW")