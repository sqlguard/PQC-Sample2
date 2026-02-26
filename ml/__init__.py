"""
Machine Learning module for adaptive context detection and priority learning.
"""

from .feedback_collector import FeedbackCollector
from .context_classifier import MLContextClassifier
from .training_pipeline import TrainingPipeline

__all__ = [
    'FeedbackCollector',
    'MLContextClassifier',
    'TrainingPipeline'
]