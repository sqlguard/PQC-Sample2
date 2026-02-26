"""
Training pipeline for ML-based context detection.
Orchestrates data collection, model training, and evaluation.
"""

import json
from pathlib import Path
from typing import Dict, Any, Optional
from datetime import datetime

from .feedback_collector import FeedbackCollector
from .context_classifier import MLContextClassifier


class TrainingPipeline:
    """
    Orchestrates the ML training pipeline.
    
    Workflow:
    1. Collect feedback from various sources
    2. Validate data quality
    3. Train ML models
    4. Evaluate performance
    5. Deploy if performance meets threshold
    6. Generate training report
    """
    
    def __init__(
        self,
        feedback_db_path: str = "data/feedback.db",
        model_dir: str = "models",
        min_accuracy: float = 0.7
    ):
        """
        Initialize the training pipeline.
        
        Args:
            feedback_db_path: Path to feedback database
            model_dir: Directory for storing models
            min_accuracy: Minimum accuracy threshold for deployment
        """
        self.feedback_collector = FeedbackCollector(feedback_db_path)
        self.classifier = MLContextClassifier(model_dir)
        self.min_accuracy = min_accuracy
        self.model_dir = Path(model_dir)
        self.model_dir.mkdir(parents=True, exist_ok=True)
    
    def run_training(
        self,
        min_samples: int = 50,
        test_size: float = 0.2,
        auto_deploy: bool = True
    ) -> Dict[str, Any]:
        """
        Run the complete training pipeline.
        
        Args:
            min_samples: Minimum samples required for training
            test_size: Fraction of data for testing
            auto_deploy: Automatically deploy if performance is good
            
        Returns:
            Training results and metrics
        """
        print("=" * 70)
        print("ML TRAINING PIPELINE")
        print("=" * 70)
        
        # Step 1: Check feedback statistics
        print("\n[1/5] Checking feedback data...")
        stats = self.feedback_collector.get_statistics()
        print(f"  Total feedback records: {stats['total_feedback']}")
        print(f"  Feedback by type:")
        for feedback_type, count in stats['feedback_by_type'].items():
            print(f"    - {feedback_type}: {count}")
        
        if stats['total_feedback'] < min_samples:
            return {
                'success': False,
                'error': f'Insufficient feedback: {stats["total_feedback"]} < {min_samples}',
                'statistics': stats
            }
        
        # Step 2: Train models
        print(f"\n[2/5] Training ML models...")
        print(f"  Minimum samples: {min_samples}")
        print(f"  Test size: {test_size * 100}%")
        
        training_results = self.classifier.train(
            self.feedback_collector,
            min_samples=min_samples,
            test_size=test_size
        )
        
        if not training_results['success']:
            return training_results
        
        print(f"  Training samples: {training_results['training_samples']}")
        print(f"  Test samples: {training_results['test_samples']}")
        
        # Step 3: Evaluate performance
        print(f"\n[3/5] Evaluating model performance...")
        evaluation = self._evaluate_models(training_results['metrics'])
        print(f"  Overall accuracy: {evaluation['overall_accuracy']:.2%}")
        print(f"  Meets threshold ({self.min_accuracy:.2%}): {evaluation['meets_threshold']}")
        
        # Step 4: Deployment decision
        print(f"\n[4/5] Making deployment decision...")
        should_deploy = evaluation['meets_threshold'] and auto_deploy
        
        if should_deploy:
            print("  ✓ Models meet quality threshold - deploying")
            deployment_status = "deployed"
        else:
            print("  ✗ Models do not meet threshold - not deploying")
            deployment_status = "not_deployed"
        
        # Step 5: Generate report
        print(f"\n[5/5] Generating training report...")
        report = self._generate_report(
            stats,
            training_results,
            evaluation,
            deployment_status
        )
        
        report_path = self.model_dir / f"training_report_{datetime.utcnow().strftime('%Y%m%d_%H%M%S')}.json"
        with open(report_path, 'w') as f:
            json.dump(report, f, indent=2)
        print(f"  Report saved to: {report_path}")
        
        print("\n" + "=" * 70)
        print("TRAINING COMPLETE")
        print("=" * 70)
        
        return {
            'success': True,
            'deployment_status': deployment_status,
            'statistics': stats,
            'training_results': training_results,
            'evaluation': evaluation,
            'report_path': str(report_path)
        }
    
    def _evaluate_models(self, metrics: Dict[str, Any]) -> Dict[str, Any]:
        """Evaluate model performance."""
        accuracies = []
        
        for model_name, model_metrics in metrics.items():
            if 'accuracy' in model_metrics:
                accuracies.append(model_metrics['accuracy'])
        
        overall_accuracy = sum(accuracies) / len(accuracies) if accuracies else 0.0
        meets_threshold = overall_accuracy >= self.min_accuracy
        
        return {
            'overall_accuracy': overall_accuracy,
            'individual_accuracies': {
                name: metrics[name]['accuracy']
                for name in metrics
                if 'accuracy' in metrics[name]
            },
            'meets_threshold': meets_threshold,
            'threshold': self.min_accuracy
        }
    
    def _generate_report(
        self,
        stats: Dict[str, Any],
        training_results: Dict[str, Any],
        evaluation: Dict[str, Any],
        deployment_status: str
    ) -> Dict[str, Any]:
        """Generate comprehensive training report."""
        return {
            'timestamp': datetime.utcnow().isoformat(),
            'model_version': training_results['model_version'],
            'deployment_status': deployment_status,
            'data_statistics': stats,
            'training': {
                'samples': training_results['training_samples'],
                'test_samples': training_results['test_samples'],
                'trained_at': training_results['trained_at']
            },
            'evaluation': evaluation,
            'metrics': training_results['metrics'],
            'recommendations': self._generate_recommendations(evaluation, stats)
        }
    
    def _generate_recommendations(
        self,
        evaluation: Dict[str, Any],
        stats: Dict[str, Any]
    ) -> list[str]:
        """Generate recommendations based on training results."""
        recommendations = []
        
        if not evaluation['meets_threshold']:
            recommendations.append(
                f"Model accuracy ({evaluation['overall_accuracy']:.2%}) is below threshold "
                f"({evaluation['threshold']:.2%}). Collect more feedback data."
            )
        
        if stats['total_feedback'] < 100:
            recommendations.append(
                f"Only {stats['total_feedback']} feedback records available. "
                "Aim for 100+ samples for better model performance."
            )
        
        exploitation_stats = stats.get('exploitation_stats', {})
        if exploitation_stats.get('total_attempts', 0) < 10:
            recommendations.append(
                "Limited exploitation attempt data. Track actual security incidents "
                "to improve priority accuracy."
            )
        
        if evaluation['meets_threshold']:
            recommendations.append(
                "Models meet quality threshold. Continue collecting feedback to "
                "improve accuracy over time."
            )
        
        return recommendations
    
    def simulate_feedback(self, num_samples: int = 100) -> Dict[str, Any]:
        """
        Generate simulated feedback for testing the ML pipeline.
        
        Args:
            num_samples: Number of simulated samples to generate
            
        Returns:
            Statistics about generated feedback
        """
        from ..models import (
            CryptoFinding, CryptoAlgorithm, UsageContext,
            DataSensitivity, ExposureDuration, Severity,
            RiskScore, ImpactFactors, PrioritizedFinding
        )
        import random
        
        print(f"Generating {num_samples} simulated feedback samples...")
        
        algorithms = [
            CryptoAlgorithm.MD5, CryptoAlgorithm.SHA1, CryptoAlgorithm.DES,
            CryptoAlgorithm.RC4, CryptoAlgorithm.RSA_1024
        ]
        contexts = [
            UsageContext.AUTHENTICATION, UsageContext.KEY_STORAGE,
            UsageContext.DATA_ENCRYPTION, UsageContext.SIGNING, UsageContext.HASHING
        ]
        sensitivities = [
            DataSensitivity.PUBLIC, DataSensitivity.INTERNAL,
            DataSensitivity.CONFIDENTIAL, DataSensitivity.RESTRICTED, DataSensitivity.CRITICAL
        ]
        durations = [
            ExposureDuration.EPHEMERAL, ExposureDuration.SHORT_TERM,
            ExposureDuration.MEDIUM_TERM, ExposureDuration.LONG_TERM, ExposureDuration.PERSISTENT
        ]
        priorities = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
        
        for i in range(num_samples):
            # Create random finding
            algorithm = random.choice(algorithms)
            context = random.choice(contexts)
            sensitivity = random.choice(sensitivities)
            duration = random.choice(durations)
            is_external = random.choice([True, False])
            
            finding = CryptoFinding(
                id=f"SIM-{i:04d}",
                title=f"Simulated finding {i}",
                description=f"Test finding using {algorithm.value} for {context.value}",
                severity=Severity.HIGH,
                algorithm=algorithm,
                usage_context=context,
                data_sensitivity=sensitivity,
                exposure_duration=duration,
                is_external_facing=is_external,
                file_path=f"src/test/file_{i}.py"
            )
            
            # Create risk score
            base_score = random.uniform(5, 10)
            multiplier = 1.5 if context == UsageContext.AUTHENTICATION else 1.0
            final_score = base_score * multiplier
            
            risk_score = RiskScore(
                finding_id=finding.id,
                base_score=base_score,
                context_multiplier=multiplier,
                final_score=min(final_score, 20.0),
                impact_factors=ImpactFactors(
                    data_sensitivity=random.randint(5, 10),
                    exposure_duration=random.randint(5, 10),
                    exploitability=random.randint(5, 10),
                    blast_radius=random.randint(5, 10),
                    algorithm_weakness=random.randint(5, 10)
                )
            )
            
            prioritized = PrioritizedFinding(
                finding=finding,
                risk_score=risk_score
            )
            
            # Record different types of feedback
            feedback_type = random.choice(['remediation', 'priority', 'context'])
            
            if feedback_type == 'remediation':
                self.feedback_collector.record_remediation_decision(
                    prioritized,
                    actual_priority=random.choice(priorities),
                    remediation_time_days=random.randint(1, 30),
                    notes=f"Simulated remediation for {finding.id}"
                )
            elif feedback_type == 'priority':
                self.feedback_collector.record_priority_feedback(
                    prioritized,
                    suggested_priority=random.choice(priorities),
                    reason=f"Simulated priority feedback for {finding.id}"
                )
            else:
                self.feedback_collector.record_context_correction(
                    prioritized,
                    corrected_context={
                        'usage_context': random.choice(contexts).value,
                        'data_sensitivity': random.choice(sensitivities).value
                    },
                    notes=f"Simulated context correction for {finding.id}"
                )
        
        stats = self.feedback_collector.get_statistics()
        print(f"Generated {stats['total_feedback']} feedback records")
        
        return stats