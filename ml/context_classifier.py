"""
ML-based context classifier that learns from historical feedback.
Uses scikit-learn for classification tasks.
"""

import json
import pickle
from pathlib import Path
from typing import Dict, List, Optional, Any, Tuple
from datetime import datetime

import numpy as np
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.metrics import classification_report, accuracy_score, precision_recall_fscore_support

from ..models import CryptoFinding, UsageContext, DataSensitivity, ExposureDuration
from .feedback_collector import FeedbackCollector, FeedbackType


class MLContextClassifier:
    """
    Machine learning classifier for context detection.
    
    Learns from:
    - Historical remediation decisions
    - Exploitation attempts
    - Team feedback on priority accuracy
    - Expert context corrections
    """
    
    def __init__(self, model_dir: str = "models"):
        """
        Initialize the ML context classifier.
        
        Args:
            model_dir: Directory to store trained models
        """
        self.model_dir = Path(model_dir)
        self.model_dir.mkdir(parents=True, exist_ok=True)
        
        # Classifiers for different aspects
        self.usage_context_classifier: Optional[RandomForestClassifier] = None
        self.data_sensitivity_classifier: Optional[RandomForestClassifier] = None
        self.exposure_duration_classifier: Optional[RandomForestClassifier] = None
        self.priority_classifier: Optional[GradientBoostingClassifier] = None
        
        # Text vectorizer for descriptions
        self.text_vectorizer = TfidfVectorizer(
            max_features=100,
            stop_words='english',
            ngram_range=(1, 2)
        )
        
        # Label encoders
        self.usage_context_encoder = LabelEncoder()
        self.data_sensitivity_encoder = LabelEncoder()
        self.exposure_duration_encoder = LabelEncoder()
        self.priority_encoder = LabelEncoder()
        
        # Model metadata
        self.model_version = "1.0.0"
        self.last_trained: Optional[str] = None
        self.training_samples: int = 0
        
        # Load existing models if available
        self._load_models()
    
    def train(
        self,
        feedback_collector: FeedbackCollector,
        min_samples: int = 50,
        test_size: float = 0.2
    ) -> Dict[str, Any]:
        """
        Train the ML models using collected feedback.
        
        Args:
            feedback_collector: FeedbackCollector instance with training data
            min_samples: Minimum samples required for training
            test_size: Fraction of data to use for testing
            
        Returns:
            Training metrics and results
        """
        # Get training data
        training_data = feedback_collector.get_training_data(
            feedback_types=[
                FeedbackType.REMEDIATION_DECISION,
                FeedbackType.PRIORITY_ACCURACY,
                FeedbackType.CONTEXT_CORRECTION
            ],
            min_samples=min_samples
        )
        
        if len(training_data) < min_samples:
            return {
                'success': False,
                'error': f'Insufficient training data: {len(training_data)} < {min_samples}',
                'samples_collected': len(training_data)
            }
        
        self.training_samples = len(training_data)
        
        # Prepare features and labels
        X, y_usage, y_sensitivity, y_duration, y_priority = self._prepare_training_data(training_data)
        
        # Split data
        X_train, X_test, y_usage_train, y_usage_test = train_test_split(
            X, y_usage, test_size=test_size, random_state=42
        )
        _, _, y_sens_train, y_sens_test = train_test_split(
            X, y_sensitivity, test_size=test_size, random_state=42
        )
        _, _, y_dur_train, y_dur_test = train_test_split(
            X, y_duration, test_size=test_size, random_state=42
        )
        _, _, y_pri_train, y_pri_test = train_test_split(
            X, y_priority, test_size=test_size, random_state=42
        )
        
        metrics = {}
        
        # Train usage context classifier
        if len(set(y_usage)) > 1:
            self.usage_context_classifier = RandomForestClassifier(
                n_estimators=100,
                max_depth=10,
                random_state=42
            )
            self.usage_context_classifier.fit(X_train, y_usage_train)
            y_usage_pred = self.usage_context_classifier.predict(X_test)
            metrics['usage_context'] = {
                'accuracy': accuracy_score(y_usage_test, y_usage_pred),
                'report': classification_report(y_usage_test, y_usage_pred, output_dict=True)
            }
        
        # Train data sensitivity classifier
        if len(set(y_sensitivity)) > 1:
            self.data_sensitivity_classifier = RandomForestClassifier(
                n_estimators=100,
                max_depth=10,
                random_state=42
            )
            self.data_sensitivity_classifier.fit(X_train, y_sens_train)
            y_sens_pred = self.data_sensitivity_classifier.predict(X_test)
            metrics['data_sensitivity'] = {
                'accuracy': accuracy_score(y_sens_test, y_sens_pred),
                'report': classification_report(y_sens_test, y_sens_pred, output_dict=True)
            }
        
        # Train exposure duration classifier
        if len(set(y_duration)) > 1:
            self.exposure_duration_classifier = RandomForestClassifier(
                n_estimators=100,
                max_depth=10,
                random_state=42
            )
            self.exposure_duration_classifier.fit(X_train, y_dur_train)
            y_dur_pred = self.exposure_duration_classifier.predict(X_test)
            metrics['exposure_duration'] = {
                'accuracy': accuracy_score(y_dur_test, y_dur_pred),
                'report': classification_report(y_dur_test, y_dur_pred, output_dict=True)
            }
        
        # Train priority classifier
        if len(set(y_priority)) > 1:
            self.priority_classifier = GradientBoostingClassifier(
                n_estimators=100,
                max_depth=5,
                random_state=42
            )
            self.priority_classifier.fit(X_train, y_pri_train)
            y_pri_pred = self.priority_classifier.predict(X_test)
            metrics['priority'] = {
                'accuracy': accuracy_score(y_pri_test, y_pri_pred),
                'report': classification_report(y_pri_test, y_pri_pred, output_dict=True)
            }
        
        # Update metadata
        self.last_trained = datetime.utcnow().isoformat()
        
        # Save models
        self._save_models()
        
        return {
            'success': True,
            'training_samples': self.training_samples,
            'test_samples': len(X_test),
            'metrics': metrics,
            'model_version': self.model_version,
            'trained_at': self.last_trained
        }
    
    def predict_context(self, finding: CryptoFinding) -> Dict[str, Any]:
        """
        Predict context for a finding using trained ML models.
        
        Args:
            finding: CryptoFinding to classify
            
        Returns:
            Predicted context with confidence scores
        """
        if not self._models_trained():
            return {
                'ml_available': False,
                'reason': 'Models not trained yet'
            }
        
        # Extract features
        features = self._extract_features(finding)
        X = np.array([features])
        
        predictions = {
            'ml_available': True,
            'model_version': self.model_version,
            'last_trained': self.last_trained
        }
        
        # Predict usage context
        if self.usage_context_classifier:
            usage_pred = self.usage_context_classifier.predict(X)[0]
            usage_proba = self.usage_context_classifier.predict_proba(X)[0]
            predictions['usage_context'] = {
                'prediction': self.usage_context_encoder.inverse_transform([usage_pred])[0],
                'confidence': float(max(usage_proba))
            }
        
        # Predict data sensitivity
        if self.data_sensitivity_classifier:
            sens_pred = self.data_sensitivity_classifier.predict(X)[0]
            sens_proba = self.data_sensitivity_classifier.predict_proba(X)[0]
            predictions['data_sensitivity'] = {
                'prediction': self.data_sensitivity_encoder.inverse_transform([sens_pred])[0],
                'confidence': float(max(sens_proba))
            }
        
        # Predict exposure duration
        if self.exposure_duration_classifier:
            dur_pred = self.exposure_duration_classifier.predict(X)[0]
            dur_proba = self.exposure_duration_classifier.predict_proba(X)[0]
            predictions['exposure_duration'] = {
                'prediction': self.exposure_duration_encoder.inverse_transform([dur_pred])[0],
                'confidence': float(max(dur_proba))
            }
        
        # Predict priority
        if self.priority_classifier:
            pri_pred = self.priority_classifier.predict(X)[0]
            pri_proba = self.priority_classifier.predict_proba(X)[0]
            predictions['priority'] = {
                'prediction': self.priority_encoder.inverse_transform([pri_pred])[0],
                'confidence': float(max(pri_proba))
            }
        
        return predictions
    
    def _prepare_training_data(
        self,
        training_data: List[Dict[str, Any]]
    ) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
        """Prepare features and labels from training data."""
        features_list = []
        usage_labels = []
        sensitivity_labels = []
        duration_labels = []
        priority_labels = []
        
        # First pass: collect all text for vectorizer
        texts = [
            f"{sample['features']['title']} {sample['features']['description']}"
            for sample in training_data
        ]
        self.text_vectorizer.fit(texts)
        
        # Collect all unique labels for encoders
        all_usage = [s['features']['usage_context'] for s in training_data if s['features']['usage_context']]
        all_sensitivity = [s['features']['data_sensitivity'] for s in training_data if s['features']['data_sensitivity']]
        all_duration = [s['features']['exposure_duration'] for s in training_data if s['features']['exposure_duration']]
        all_priority = []
        for s in training_data:
            if s['labels'].get('actual_priority'):
                all_priority.append(s['labels']['actual_priority'])
            elif s['labels'].get('suggested_priority'):
                all_priority.append(s['labels']['suggested_priority'])
        
        self.usage_context_encoder.fit(all_usage)
        self.data_sensitivity_encoder.fit(all_sensitivity)
        self.exposure_duration_encoder.fit(all_duration)
        self.priority_encoder.fit(all_priority)
        
        # Second pass: extract features and encode labels
        for sample in training_data:
            # Skip samples without required labels
            if not sample['features']['usage_context']:
                continue
            if not sample['features']['data_sensitivity']:
                continue
            if not sample['features']['exposure_duration']:
                continue
            priority = sample['labels'].get('actual_priority') or sample['labels'].get('suggested_priority')
            if not priority:
                continue
            
            # Create pseudo-finding for feature extraction
            from ..models import Severity
            finding = CryptoFinding(
                id=sample['finding_id'],
                title=sample['features']['title'],
                description=sample['features']['description'],
                severity=Severity.HIGH,
                algorithm=sample['features']['algorithm'],
                usage_context=sample['features']['usage_context'],
                data_sensitivity=sample['features']['data_sensitivity'],
                exposure_duration=sample['features']['exposure_duration'],
                is_external_facing=sample['features']['is_external_facing'],
                file_path=sample['features']['file_path']
            )
            
            features = self._extract_features(finding)
            features_list.append(features)
            
            # Encode labels (all samples now have all labels)
            usage_labels.append(
                self.usage_context_encoder.transform([sample['features']['usage_context']])[0]
            )
            
            sensitivity_labels.append(
                self.data_sensitivity_encoder.transform([sample['features']['data_sensitivity']])[0]
            )
            
            duration_labels.append(
                self.exposure_duration_encoder.transform([sample['features']['exposure_duration']])[0]
            )
            
            priority_labels.append(
                self.priority_encoder.transform([priority])[0]
            )
        
        return (
            np.array(features_list),
            np.array(usage_labels),
            np.array(sensitivity_labels),
            np.array(duration_labels),
            np.array(priority_labels)
        )
    
    def _extract_features(self, finding: CryptoFinding) -> List[float]:
        """Extract numerical features from a finding."""
        features = []
        
        # Text features (TF-IDF)
        text = f"{finding.title} {finding.description}"
        text_features = self.text_vectorizer.transform([text]).toarray()[0]
        features.extend(text_features)
        
        # Algorithm features (one-hot)
        algorithm_map = {
            'MD5': [1, 0, 0, 0, 0, 0],
            'SHA1': [0, 1, 0, 0, 0, 0],
            'DES': [0, 0, 1, 0, 0, 0],
            '3DES': [0, 0, 0, 1, 0, 0],
            'RC4': [0, 0, 0, 0, 1, 0],
            'RSA-1024': [0, 0, 0, 0, 0, 1]
        }
        features.extend(algorithm_map.get(finding.algorithm.value, [0, 0, 0, 0, 0, 0]))
        
        # Boolean features
        features.append(1.0 if finding.is_external_facing else 0.0)
        
        # File path features
        if finding.file_path:
            features.append(1.0 if 'auth' in finding.file_path.lower() else 0.0)
            features.append(1.0 if 'api' in finding.file_path.lower() else 0.0)
            features.append(1.0 if 'internal' in finding.file_path.lower() else 0.0)
            features.append(1.0 if 'public' in finding.file_path.lower() else 0.0)
        else:
            features.extend([0.0, 0.0, 0.0, 0.0])
        
        return features
    
    def _models_trained(self) -> bool:
        """Check if models are trained."""
        return any([
            self.usage_context_classifier is not None,
            self.data_sensitivity_classifier is not None,
            self.exposure_duration_classifier is not None,
            self.priority_classifier is not None
        ])
    
    def _save_models(self):
        """Save trained models to disk."""
        model_data = {
            'usage_context_classifier': self.usage_context_classifier,
            'data_sensitivity_classifier': self.data_sensitivity_classifier,
            'exposure_duration_classifier': self.exposure_duration_classifier,
            'priority_classifier': self.priority_classifier,
            'text_vectorizer': self.text_vectorizer,
            'usage_context_encoder': self.usage_context_encoder,
            'data_sensitivity_encoder': self.data_sensitivity_encoder,
            'exposure_duration_encoder': self.exposure_duration_encoder,
            'priority_encoder': self.priority_encoder,
            'model_version': self.model_version,
            'last_trained': self.last_trained,
            'training_samples': self.training_samples
        }
        
        model_path = self.model_dir / 'context_classifier.pkl'
        with open(model_path, 'wb') as f:
            pickle.dump(model_data, f)
    
    def _load_models(self):
        """Load trained models from disk."""
        model_path = self.model_dir / 'context_classifier.pkl'
        if not model_path.exists():
            return
        
        try:
            with open(model_path, 'rb') as f:
                model_data = pickle.load(f)
            
            self.usage_context_classifier = model_data.get('usage_context_classifier')
            self.data_sensitivity_classifier = model_data.get('data_sensitivity_classifier')
            self.exposure_duration_classifier = model_data.get('exposure_duration_classifier')
            self.priority_classifier = model_data.get('priority_classifier')
            self.text_vectorizer = model_data.get('text_vectorizer')
            self.usage_context_encoder = model_data.get('usage_context_encoder')
            self.data_sensitivity_encoder = model_data.get('data_sensitivity_encoder')
            self.exposure_duration_encoder = model_data.get('exposure_duration_encoder')
            self.priority_encoder = model_data.get('priority_encoder')
            self.model_version = model_data.get('model_version', '1.0.0')
            self.last_trained = model_data.get('last_trained')
            self.training_samples = model_data.get('training_samples', 0)
        except Exception as e:
            print(f"Warning: Could not load models: {e}")