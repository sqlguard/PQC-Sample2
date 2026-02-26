"""
Main entry point for the Crypto Remediation Prioritizer CLI.
"""
import sys
import click
import fnmatch
from pathlib import Path
from typing import Optional, List

from .models import CryptoFinding
from .parsers.sarif_parser import SARIFParser
from .parsers.json_parser import JSONParser
from .scoring.risk_scorer import RiskScorer
from .reporting.report_generator import ReportGenerator
from .config import ConfigLoader, Config, validate_and_normalize_config

def apply_filters(
    findings: List[CryptoFinding],
    severity_filter: Optional[List[str]] = None,
    algorithm_filter: Optional[List[str]] = None,
    context_filter: Optional[List[str]] = None,
    file_patterns: Optional[List[str]] = None,
    exclude_patterns: Optional[List[str]] = None,
    config: Optional[Config] = None,
    verbose: bool = False
) -> List[CryptoFinding]:
    """
    Apply filters to findings based on CLI options and config.
    
    Args:
        findings: List of findings to filter
        severity_filter: Filter by severity levels
        algorithm_filter: Filter by algorithms
        context_filter: Filter by usage contexts
        file_patterns: Include only files matching patterns
        exclude_patterns: Exclude files matching patterns
        config: Configuration object (for additional filters)
        verbose: Whether to print verbose output
        
    Returns:
        Filtered list of findings
    """
    filtered = findings
    initial_count = len(filtered)
    
    # Apply severity filter
    if severity_filter:
        filtered = [f for f in filtered if f.severity.value.lower() in [s.lower() for s in severity_filter]]
        if verbose:
            click.echo(f"  Severity filter: {len(filtered)}/{initial_count} findings")
    elif config and config.filters.severity_levels:
        filtered = [f for f in filtered if f.severity.value.lower() in [s.lower() for s in config.filters.severity_levels]]
        if verbose:
            click.echo(f"  Severity filter (from config): {len(filtered)}/{initial_count} findings")
    
    # Apply algorithm filter
    if algorithm_filter:
        filtered = [f for f in filtered if f.algorithm.value in algorithm_filter]
        if verbose:
            click.echo(f"  Algorithm filter: {len(filtered)} findings")
    elif config and config.filters.algorithms:
        filtered = [f for f in filtered if f.algorithm.value in config.filters.algorithms]
        if verbose:
            click.echo(f"  Algorithm filter (from config): {len(filtered)} findings")
    
    # Apply context filter
    if context_filter:
        filtered = [f for f in filtered if f.usage_context.value in context_filter]
        if verbose:
            click.echo(f"  Context filter: {len(filtered)} findings")
    elif config and config.filters.contexts:
        filtered = [f for f in filtered if f.usage_context.value in config.filters.contexts]
        if verbose:
            click.echo(f"  Context filter (from config): {len(filtered)} findings")
    
    # Apply file pattern filters
    patterns_to_include = file_patterns or (config.filters.file_patterns if config else [])
    patterns_to_exclude = exclude_patterns or (config.filters.exclude_file_patterns if config else [])
    
    if patterns_to_include:
        filtered = [
            f for f in filtered
            if f.file_path and any(fnmatch.fnmatch(f.file_path, pattern) for pattern in patterns_to_include)
        ]
        if verbose:
            click.echo(f"  File pattern filter (include): {len(filtered)} findings")
    
    if patterns_to_exclude:
        filtered = [
            f for f in filtered
            if not (f.file_path and any(fnmatch.fnmatch(f.file_path, pattern) for pattern in patterns_to_exclude))
        ]
        if verbose:
            click.echo(f"  File pattern filter (exclude): {len(filtered)} findings")
    
    return filtered

from .config import ConfigLoader, Config, validate_and_normalize_config
from .models import CryptoFinding


@click.command()
@click.argument('input_file', type=click.Path(exists=True))
@click.option(
    '--format',
    '-f',
    type=click.Choice(['sarif', 'json', 'auto'], case_sensitive=False),
    default='auto',
    help='Input file format (default: auto-detect)'
)
@click.option(
    '--output',
    '-o',
    type=click.Path(),
    default='report',
    help='Output file path (without extension, default: report)'
)
@click.option(
    '--output-format',
    '-t',
    type=click.Choice(['json', 'markdown', 'html', 'dashboard', 'cicd', 'github', 'jenkins', 'all'], case_sensitive=False),
    multiple=True,
    default=['all'],
    help='Output format(s) (default: all)'
)
@click.option(
    '--fail-on-critical',
    is_flag=True,
    default=False,
    help='Exit with error code if critical issues found (for CI/CD)'
)
@click.option(
    '--min-score',
    type=float,
    default=0.0,
    help='Minimum risk score to include in report (default: 0.0)'
)
@click.option(
    '--max-findings',
    type=int,
    help='Maximum number of findings to include in report'
)
@click.option(
    '--no-guidance',
    is_flag=True,
    help='Exclude remediation guidance from report'
)
@click.option(
    '--custom-weights',
    type=str,
    help='Custom scoring weights as JSON string (e.g., \'{"data_sensitivity": 0.3}\')'
)
@click.option(
    '--config',
    '-c',
    type=click.Path(exists=True),
    help='Path to configuration file (JSON or YAML)'
)
@click.option(
    '--severity',
    '-s',
    type=click.Choice(['critical', 'high', 'medium', 'low'], case_sensitive=False),
    multiple=True,
    help='Filter by severity level(s)'
)
@click.option(
    '--algorithm',
    '-a',
    type=str,
    multiple=True,
    help='Filter by algorithm(s) (e.g., MD5, SHA1)'
)
@click.option(
    '--context',
    type=str,
    multiple=True,
    help='Filter by usage context(s) (e.g., authentication, key_storage)'
)
@click.option(
    '--file-pattern',
    type=str,
    multiple=True,
    help='Include only files matching pattern(s) (glob syntax)'
)
@click.option(
    '--exclude-pattern',
    type=str,
    multiple=True,
    help='Exclude files matching pattern(s) (glob syntax)'
)
@click.option(
    '--verbose',
    '-v',
    is_flag=True,
    help='Verbose output'
)
def main(
    input_file: str,
    format: str,
    output: str,
    output_format: tuple,
    min_score: float,
    max_findings: Optional[int],
    no_guidance: bool,
    custom_weights: Optional[str],
    config: Optional[str],
    severity: tuple,
    algorithm: tuple,
    context: tuple,
    file_pattern: tuple,
    exclude_pattern: tuple,
    fail_on_critical: bool,
    verbose: bool
):
    """
    Crypto Remediation Prioritizer - Impact-based vulnerability prioritization tool.
    
    Analyzes cryptographic vulnerability findings and prioritizes them based on
    risk factors including data sensitivity, exposure duration, exploitability,
    blast radius, and algorithm weakness.
    
    INPUT_FILE: Path to vulnerability scan results (SARIF or JSON format)
    """
    try:
        # Load configuration if provided
        app_config = None
        if config:
            if verbose:
                click.echo(f"📋 Loading configuration from: {config}")
            try:
                app_config = ConfigLoader.load_from_file(config)
                app_config, warnings = validate_and_normalize_config(app_config)
                
                if warnings and verbose:
                    for warning in warnings:
                        click.echo(f"⚠️  {warning}")
                
                if verbose:
                    click.echo(f"✓ Configuration loaded: {app_config.organization}")
            except Exception as e:
                click.echo(f"❌ Error loading configuration: {e}", err=True)
                sys.exit(1)
        
        if verbose:
            click.echo("🔐 Crypto Remediation Prioritizer")
            click.echo("=" * 50)
            click.echo(f"Input file: {input_file}")
            click.echo(f"Format: {format}")
            if app_config:
                click.echo(f"Configuration: {app_config.organization}")
        
        # Parse input file
        if verbose:
            click.echo("\n📄 Parsing input file...")
        
        findings = parse_input_file(input_file, format, verbose)
        
        if not findings:
            click.echo("⚠️  No crypto-related findings detected in input file.", err=True)
            sys.exit(1)
        
        if verbose:
            click.echo(f"✓ Found {len(findings)} crypto-related findings")
        
        # Apply filters
        findings = apply_filters(
            findings,
            severity_filter=list(severity) if severity else None,
            algorithm_filter=list(algorithm) if algorithm else None,
            context_filter=list(context) if context else None,
            file_patterns=list(file_pattern) if file_pattern else None,
            exclude_patterns=list(exclude_pattern) if exclude_pattern else None,
            config=app_config,
            verbose=verbose
        )
        
        if not findings:
            click.echo("⚠️  No findings match the specified filters.", err=True)
            sys.exit(1)
        
        if verbose:
            click.echo(f"✓ {len(findings)} findings after filtering")
        
        # Initialize scorer with custom weights
        scorer_kwargs = {}
        
        # Priority: CLI custom weights > config weights > defaults
        if custom_weights:
            import json
            try:
                scorer_kwargs['custom_weights'] = json.loads(custom_weights)
                if verbose:
                    click.echo(f"✓ Using custom weights from CLI: {scorer_kwargs['custom_weights']}")
            except json.JSONDecodeError as e:
                click.echo(f"❌ Invalid JSON in custom weights: {e}", err=True)
                sys.exit(1)
        elif app_config:
            scorer_kwargs['custom_weights'] = {
                'data_sensitivity': app_config.scoring_weights.data_sensitivity,
                'exposure_duration': app_config.scoring_weights.exposure_duration,
                'exploitability': app_config.scoring_weights.exploitability,
                'blast_radius': app_config.scoring_weights.blast_radius,
                'algorithm_weakness': app_config.scoring_weights.algorithm_weakness
            }
            if verbose:
                click.echo(f"✓ Using weights from config file")
        
        # Score and prioritize findings
        if verbose:
            click.echo("\n📊 Calculating risk scores...")
        
        scorer = RiskScorer(**scorer_kwargs)
        prioritized_findings = scorer.prioritize_findings(
            findings,
            include_guidance=not no_guidance
        )
        
        # Apply score and count filters
        # Priority: CLI options > config options
        effective_min_score = min_score if min_score > 0 else (app_config.filters.min_score if app_config else 0.0)
        effective_max_findings = max_findings if max_findings else (app_config.filters.max_findings if app_config else None)
        
        if effective_min_score > 0:
            prioritized_findings = [
                pf for pf in prioritized_findings
                if pf.risk_score.final_score >= effective_min_score
            ]
            if verbose:
                click.echo(f"✓ Filtered to {len(prioritized_findings)} findings with score >= {effective_min_score}")
        
        if effective_max_findings and effective_max_findings > 0:
            prioritized_findings = prioritized_findings[:effective_max_findings]
            if verbose:
                click.echo(f"✓ Limited to top {effective_max_findings} findings")
        
        if not prioritized_findings:
            click.echo("⚠️  No findings match the specified criteria.", err=True)
            sys.exit(1)
        
        # Generate statistics
        statistics = scorer.get_statistics(prioritized_findings)
        
        # Generate report
        if verbose:
            click.echo("\n📝 Generating report...")
        
        report_generator = ReportGenerator()
        report = report_generator.generate_report(prioritized_findings, statistics)
        
        # Print summary to console
        report_generator.print_summary(report)
        
        # Determine output formats and path
        # Priority: CLI options > config options
        formats = list(output_format)
        if 'all' in formats:
            formats = app_config.output.formats if app_config else ['json', 'markdown', 'html', 'dashboard']
        
        effective_output = output if output != 'report' else (app_config.output.output_path if app_config else 'report')
        
        report_generator.save_all_formats(report, effective_output, formats)
        
        if verbose:
            click.echo("\n✅ Report generation complete!")
        
        # Exit with error if critical issues found
        # Priority: CLI flag > config setting
        should_fail = fail_on_critical or (app_config and app_config.quality_gates.fail_on_critical)
        
        if should_fail and report.critical_count > 0:
            click.echo(f"\n❌ Build failed: {report.critical_count} critical issue(s) found", err=True)
            sys.exit(1)
        
    except FileNotFoundError as e:
        click.echo(f"❌ File not found: {e}", err=True)
        sys.exit(1)
    except ValueError as e:
        click.echo(f"❌ Invalid input: {e}", err=True)
        sys.exit(1)
    except Exception as e:
        click.echo(f"❌ Unexpected error: {e}", err=True)
        if verbose:
            import traceback
            traceback.print_exc()
        sys.exit(1)


def parse_input_file(file_path: str, format: str, verbose: bool):
    """
    Parse input file based on format.
    
    Args:
        file_path: Path to input file
        format: Format type ('sarif', 'json', or 'auto')
        verbose: Whether to print verbose output
        
    Returns:
        List of CryptoFinding objects
    """
    path = Path(file_path)
    
    # Auto-detect format if needed
    if format == 'auto':
        format = detect_format(path, verbose)
    
    # Parse based on format
    if format == 'sarif':
        parser = SARIFParser(str(path))
    elif format == 'json':
        parser = JSONParser(str(path))
    else:
        raise ValueError(f"Unsupported format: {format}")
    
    findings = parser.parse()
    return findings


def detect_format(file_path: Path, verbose: bool) -> str:
    """
    Auto-detect file format.
    
    Args:
        file_path: Path to file
        verbose: Whether to print verbose output
        
    Returns:
        Detected format ('sarif' or 'json')
    """
    import json
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Check for SARIF indicators
        if 'version' in data and 'runs' in data:
            if verbose:
                click.echo("✓ Detected SARIF format")
            return 'sarif'
        
        # Default to generic JSON
        if verbose:
            click.echo("✓ Detected generic JSON format")
        return 'json'
        
    except json.JSONDecodeError:
        raise ValueError("Input file is not valid JSON")


@click.command()
@click.argument('directory', type=click.Path(exists=True, file_okay=False))
@click.option(
    '--output',
    '-o',
    type=click.Path(),
    default='batch_report',
    help='Output file path prefix (default: batch_report)'
)
@click.option(
    '--pattern',
    '-p',
    default='*.json',
    help='File pattern to match (default: *.json)'
)
@click.option(
    '--verbose',
    '-v',
    is_flag=True,
    help='Verbose output'
)
def batch(directory: str, output: str, pattern: str, verbose: bool):
    """
    Process multiple scan result files in a directory.
    
    DIRECTORY: Path to directory containing scan results
    """
    from glob import glob
    
    dir_path = Path(directory)
    files = list(dir_path.glob(pattern))
    
    if not files:
        click.echo(f"⚠️  No files matching pattern '{pattern}' found in {directory}", err=True)
        sys.exit(1)
    
    click.echo(f"📁 Found {len(files)} files to process")
    
    all_findings = []
    
    for file_path in files:
        if verbose:
            click.echo(f"\nProcessing: {file_path.name}")
        
        try:
            findings = parse_input_file(str(file_path), 'auto', verbose)
            all_findings.extend(findings)
            if verbose:
                click.echo(f"  ✓ {len(findings)} findings")
        except Exception as e:
            click.echo(f"  ⚠️  Error: {e}", err=True)
            continue
    
    if not all_findings:
        click.echo("⚠️  No crypto-related findings detected in any files.", err=True)
        sys.exit(1)
    
    click.echo(f"\n📊 Total findings: {len(all_findings)}")
    
    # Score and generate report
    scorer = RiskScorer()
    prioritized_findings = scorer.prioritize_findings(all_findings)
    statistics = scorer.get_statistics(prioritized_findings)
    
    report_generator = ReportGenerator()
    report = report_generator.generate_report(prioritized_findings, statistics)
    
    report_generator.print_summary(report)
    report_generator.save_all_formats(report, output, ['json', 'markdown', 'html', 'dashboard'])
    
    click.echo("\n✅ Batch processing complete!")


@click.group()
def cli():
    """Crypto Remediation Prioritizer - CLI tool for impact-based vulnerability prioritization."""
    pass


cli.add_command(main, name='analyze')
cli.add_command(batch, name='batch')


if __name__ == '__main__':
    cli()