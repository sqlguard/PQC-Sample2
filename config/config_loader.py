"""
Configuration loader for custom risk scoring weights and settings.
Supports JSON and YAML formats with validation.
"""
import json
from pathlib import Path
from typing import Dict, Any, Optional
from pydantic import BaseModel, Field, field_validator


class ScoringWeights(BaseModel):
    """Risk scoring factor weights."""
    data_sensitivity: float = Field(default=0.25, ge=0.0, le=1.0)
    exposure_duration: float = Field(default=0.20, ge=0.0, le=1.0)
    exploitability: float = Field(default=0.20, ge=0.0, le=1.0)
    blast_radius: float = Field(default=0.20, ge=0.0, le=1.0)
    algorithm_weakness: float = Field(default=0.15, ge=0.0, le=1.0)
    
    @field_validator('data_sensitivity', 'exposure_duration', 'exploitability', 'blast_radius', 'algorithm_weakness')
    @classmethod
    def validate_weight(cls, v: float) -> float:
        """Validate weight is between 0 and 1."""
        if not 0.0 <= v <= 1.0:
            raise ValueError("Weight must be between 0.0 and 1.0")
        return v
    
    def validate_sum(self) -> bool:
        """Check if weights sum to approximately 1.0."""
        total = (self.data_sensitivity + self.exposure_duration + 
                self.exploitability + self.blast_radius + self.algorithm_weakness)
        return abs(total - 1.0) < 0.01
    
    def normalize(self) -> 'ScoringWeights':
        """Normalize weights to sum to 1.0."""
        total = (self.data_sensitivity + self.exposure_duration + 
                self.exploitability + self.blast_radius + self.algorithm_weakness)
        
        if total == 0:
            # If all weights are 0, use equal weights
            return ScoringWeights(
                data_sensitivity=0.2,
                exposure_duration=0.2,
                exploitability=0.2,
                blast_radius=0.2,
                algorithm_weakness=0.2
            )
        
        return ScoringWeights(
            data_sensitivity=self.data_sensitivity / total,
            exposure_duration=self.exposure_duration / total,
            exploitability=self.exploitability / total,
            blast_radius=self.blast_radius / total,
            algorithm_weakness=self.algorithm_weakness / total
        )


class ContextMultipliers(BaseModel):
    """Context-based risk multipliers."""
    authentication: float = Field(default=2.0, ge=0.5, le=3.0)
    key_storage: float = Field(default=1.8, ge=0.5, le=3.0)
    data_encryption: float = Field(default=1.5, ge=0.5, le=3.0)
    signing: float = Field(default=1.4, ge=0.5, le=3.0)
    hashing: float = Field(default=1.2, ge=0.5, le=3.0)
    internal: float = Field(default=1.0, ge=0.5, le=3.0)
    unknown: float = Field(default=1.0, ge=0.5, le=3.0)


class QualityGateThresholds(BaseModel):
    """Quality gate thresholds for CI/CD."""
    max_critical: int = Field(default=0, ge=0)
    max_high: int = Field(default=5, ge=0)
    max_average_score: float = Field(default=10.0, ge=0.0, le=20.0)
    fail_on_critical: bool = Field(default=True)


class FilterOptions(BaseModel):
    """Filtering options for findings."""
    min_score: float = Field(default=0.0, ge=0.0, le=20.0)
    max_findings: Optional[int] = Field(default=None, ge=1)
    severity_levels: list[str] = Field(default_factory=lambda: ["critical", "high", "medium", "low"])
    algorithms: list[str] = Field(default_factory=list)
    contexts: list[str] = Field(default_factory=list)
    file_patterns: list[str] = Field(default_factory=list)
    exclude_file_patterns: list[str] = Field(default_factory=list)


class OutputOptions(BaseModel):
    """Output format and options."""
    formats: list[str] = Field(default_factory=lambda: ["json", "markdown", "html", "dashboard"])
    output_path: str = Field(default="report")
    include_guidance: bool = Field(default=True)
    verbose: bool = Field(default=False)


class Config(BaseModel):
    """Complete configuration for crypto remediation prioritizer."""
    version: str = Field(default="1.0")
    organization: str = Field(default="Default Organization")
    description: Optional[str] = Field(default=None)
    
    scoring_weights: ScoringWeights = Field(default_factory=ScoringWeights)
    context_multipliers: ContextMultipliers = Field(default_factory=ContextMultipliers)
    quality_gates: QualityGateThresholds = Field(default_factory=QualityGateThresholds)
    filters: FilterOptions = Field(default_factory=FilterOptions)
    output: OutputOptions = Field(default_factory=OutputOptions)
    
    def validate_config(self) -> tuple[bool, list[str]]:
        """
        Validate the entire configuration.
        
        Returns:
            Tuple of (is_valid, list of error messages)
        """
        errors = []
        
        # Validate scoring weights sum
        if not self.scoring_weights.validate_sum():
            total = (self.scoring_weights.data_sensitivity + 
                    self.scoring_weights.exposure_duration +
                    self.scoring_weights.exploitability + 
                    self.scoring_weights.blast_radius +
                    self.scoring_weights.algorithm_weakness)
            errors.append(f"Scoring weights sum to {total:.3f}, should be 1.0")
        
        # Validate severity levels
        valid_severities = {"critical", "high", "medium", "low", "info"}
        for severity in self.filters.severity_levels:
            if severity.lower() not in valid_severities:
                errors.append(f"Invalid severity level: {severity}")
        
        # Validate output formats
        valid_formats = {"json", "markdown", "html", "dashboard", "cicd", "github", "jenkins", "all"}
        for fmt in self.output.formats:
            if fmt.lower() not in valid_formats:
                errors.append(f"Invalid output format: {fmt}")
        
        return len(errors) == 0, errors


class ConfigLoader:
    """Loads and validates configuration from files."""
    
    @staticmethod
    def load_from_file(file_path: str) -> Config:
        """
        Load configuration from JSON or YAML file.
        
        Args:
            file_path: Path to configuration file
            
        Returns:
            Config object
            
        Raises:
            FileNotFoundError: If file doesn't exist
            ValueError: If file format is invalid
        """
        path = Path(file_path)
        
        if not path.exists():
            raise FileNotFoundError(f"Configuration file not found: {file_path}")
        
        # Determine format from extension
        if path.suffix.lower() in ['.json']:
            return ConfigLoader._load_json(path)
        elif path.suffix.lower() in ['.yaml', '.yml']:
            return ConfigLoader._load_yaml(path)
        else:
            raise ValueError(f"Unsupported configuration file format: {path.suffix}")
    
    @staticmethod
    def _load_json(path: Path) -> Config:
        """Load configuration from JSON file."""
        try:
            with open(path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            return Config(**data)
        except json.JSONDecodeError as e:
            raise ValueError(f"Invalid JSON in configuration file: {e}")
        except Exception as e:
            raise ValueError(f"Error loading configuration: {e}")
    
    @staticmethod
    def _load_yaml(path: Path) -> Config:
        """Load configuration from YAML file."""
        try:
            import yaml
            with open(path, 'r', encoding='utf-8') as f:
                data = yaml.safe_load(f)
            return Config(**data)
        except ImportError:
            raise ValueError("PyYAML not installed. Install with: pip install pyyaml")
        except yaml.YAMLError as e:
            raise ValueError(f"Invalid YAML in configuration file: {e}")
        except Exception as e:
            raise ValueError(f"Error loading configuration: {e}")
    
    @staticmethod
    def load_from_dict(data: Dict[str, Any]) -> Config:
        """
        Load configuration from dictionary.
        
        Args:
            data: Configuration dictionary
            
        Returns:
            Config object
        """
        return Config(**data)
    
    @staticmethod
    def get_default_config() -> Config:
        """
        Get default configuration.
        
        Returns:
            Config object with default values
        """
        return Config()
    
    @staticmethod
    def save_to_file(config: Config, file_path: str, format: str = 'json') -> None:
        """
        Save configuration to file.
        
        Args:
            config: Config object to save
            file_path: Path to save configuration
            format: File format ('json' or 'yaml')
        """
        path = Path(file_path)
        path.parent.mkdir(parents=True, exist_ok=True)
        
        if format.lower() == 'json':
            with open(path, 'w', encoding='utf-8') as f:
                json.dump(config.model_dump(), f, indent=2)
        elif format.lower() in ['yaml', 'yml']:
            try:
                import yaml
                with open(path, 'w', encoding='utf-8') as f:
                    yaml.dump(config.model_dump(), f, default_flow_style=False)
            except ImportError:
                raise ValueError("PyYAML not installed. Install with: pip install pyyaml")
        else:
            raise ValueError(f"Unsupported format: {format}")
    
    @staticmethod
    def create_example_configs() -> Dict[str, Config]:
        """
        Create example configurations for different scenarios.
        
        Returns:
            Dictionary of example configs
        """
        examples = {}
        
        # Default config
        examples['default'] = Config()
        
        # High security organization
        examples['high_security'] = Config(
            organization="High Security Organization",
            description="Strict security requirements with zero tolerance for critical issues",
            scoring_weights=ScoringWeights(
                data_sensitivity=0.30,
                exposure_duration=0.25,
                exploitability=0.20,
                blast_radius=0.15,
                algorithm_weakness=0.10
            ),
            context_multipliers=ContextMultipliers(
                authentication=2.5,
                key_storage=2.2,
                data_encryption=1.8,
                signing=1.6,
                hashing=1.3
            ),
            quality_gates=QualityGateThresholds(
                max_critical=0,
                max_high=2,
                max_average_score=8.0,
                fail_on_critical=True
            )
        )
        
        # Development environment
        examples['development'] = Config(
            organization="Development Environment",
            description="More lenient settings for development phase",
            scoring_weights=ScoringWeights(
                data_sensitivity=0.20,
                exposure_duration=0.15,
                exploitability=0.25,
                blast_radius=0.25,
                algorithm_weakness=0.15
            ),
            quality_gates=QualityGateThresholds(
                max_critical=3,
                max_high=10,
                max_average_score=12.0,
                fail_on_critical=False
            ),
            filters=FilterOptions(
                min_score=5.0,
                severity_levels=["critical", "high"]
            )
        )
        
        # Compliance focused
        examples['compliance'] = Config(
            organization="Compliance Focused Organization",
            description="Emphasis on algorithm weakness and data sensitivity for compliance",
            scoring_weights=ScoringWeights(
                data_sensitivity=0.35,
                exposure_duration=0.15,
                exploitability=0.15,
                blast_radius=0.15,
                algorithm_weakness=0.20
            ),
            context_multipliers=ContextMultipliers(
                authentication=2.0,
                key_storage=2.0,
                data_encryption=1.8,
                signing=1.8,
                hashing=1.5
            )
        )
        
        # Fast remediation
        examples['fast_remediation'] = Config(
            organization="Fast Remediation Focus",
            description="Prioritize quick wins and exploitability",
            scoring_weights=ScoringWeights(
                data_sensitivity=0.15,
                exposure_duration=0.15,
                exploitability=0.35,
                blast_radius=0.20,
                algorithm_weakness=0.15
            ),
            filters=FilterOptions(
                min_score=10.0,
                max_findings=20
            ),
            output=OutputOptions(
                formats=["dashboard", "cicd"],
                include_guidance=True
            )
        )
        
        return examples


def validate_and_normalize_config(config: Config) -> tuple[Config, list[str]]:
    """
    Validate and normalize configuration.
    
    Args:
        config: Config object to validate
        
    Returns:
        Tuple of (normalized config, list of warnings)
    """
    warnings = []
    
    # Validate configuration
    is_valid, errors = config.validate_config()
    
    if not is_valid:
        raise ValueError(f"Invalid configuration: {', '.join(errors)}")
    
    # Normalize scoring weights if needed
    if not config.scoring_weights.validate_sum():
        warnings.append("Scoring weights don't sum to 1.0, normalizing...")
        config.scoring_weights = config.scoring_weights.normalize()
    
    return config, warnings