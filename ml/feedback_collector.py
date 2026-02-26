"""
Feedback collection system for learning from remediation decisions,
exploitation attempts, and team feedback on priority accuracy.
"""

import json
import sqlite3
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional, Any
from enum import Enum

from ..models import CryptoFinding, PrioritizedFinding


class FeedbackType(str, Enum):
    """Types of feedback that can be collected."""
    REMEDIATION_DECISION = "remediation_decision"
    EXPLOITATION_ATTEMPT = "exploitation_attempt"
    PRIORITY_ACCURACY = "priority_accuracy"
    CONTEXT_CORRECTION = "context_correction"


class FeedbackCollector:
    """
    Collects and stores feedback for ML model training.
    
    Tracks:
    - Remediation decisions (what was fixed first, why)
    - Exploitation attempts (which vulnerabilities were actually exploited)
    - Team feedback on priority accuracy
    - Context corrections from security experts
    """
    
    def __init__(self, db_path: str = "data/feedback.db"):
        """
        Initialize the feedback collector.
        
        Args:
            db_path: Path to SQLite database for storing feedback
        """
        self.db_path = Path(db_path)
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self._init_database()
    
    def _init_database(self):
        """Initialize the SQLite database schema."""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Feedback table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS feedback (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                finding_id TEXT NOT NULL,
                feedback_type TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                original_priority TEXT,
                suggested_priority TEXT,
                actual_priority TEXT,
                remediation_time_days INTEGER,
                was_exploited BOOLEAN,
                exploitation_severity TEXT,
                context_correction TEXT,
                notes TEXT,
                metadata TEXT
            )
        """)
        
        # Finding features table (for ML training)
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS finding_features (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                finding_id TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                algorithm TEXT,
                usage_context TEXT,
                data_sensitivity TEXT,
                exposure_duration TEXT,
                is_external_facing BOOLEAN,
                file_path TEXT,
                description TEXT,
                title TEXT,
                risk_score REAL,
                priority TEXT,
                FOREIGN KEY (finding_id) REFERENCES feedback(finding_id)
            )
        """)
        
        # Model performance metrics
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS model_metrics (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT NOT NULL,
                model_version TEXT,
                accuracy REAL,
                precision_critical REAL,
                precision_high REAL,
                precision_medium REAL,
                recall_critical REAL,
                recall_high REAL,
                recall_medium REAL,
                f1_score REAL,
                training_samples INTEGER,
                notes TEXT
            )
        """)
        
        conn.commit()
        conn.close()
    
    def record_remediation_decision(
        self,
        finding: PrioritizedFinding,
        actual_priority: str,
        remediation_time_days: int,
        notes: Optional[str] = None
    ):
        """
        Record a remediation decision made by the team.
        
        Args:
            finding: The prioritized finding
            actual_priority: The priority the team actually assigned
            remediation_time_days: How many days it took to remediate
            notes: Additional notes about the decision
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute("""
            INSERT INTO feedback (
                finding_id, feedback_type, timestamp,
                original_priority, actual_priority,
                remediation_time_days, notes, metadata
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            finding.finding.id,
            FeedbackType.REMEDIATION_DECISION.value,
            datetime.utcnow().isoformat(),
            finding.risk_score.priority_level,
            actual_priority,
            remediation_time_days,
            notes,
            json.dumps(finding.risk_score.model_dump())
        ))
        
        # Store finding features for training
        self._store_finding_features(cursor, finding)
        
        conn.commit()
        conn.close()
    
    def record_exploitation_attempt(
        self,
        finding_id: str,
        was_successful: bool,
        severity: str,
        notes: Optional[str] = None
    ):
        """
        Record an actual exploitation attempt.
        
        Args:
            finding_id: ID of the finding that was exploited
            was_successful: Whether the exploitation was successful
            severity: Severity of the exploitation impact
            notes: Details about the exploitation
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute("""
            INSERT INTO feedback (
                finding_id, feedback_type, timestamp,
                was_exploited, exploitation_severity, notes
            ) VALUES (?, ?, ?, ?, ?, ?)
        """, (
            finding_id,
            FeedbackType.EXPLOITATION_ATTEMPT.value,
            datetime.utcnow().isoformat(),
            was_successful,
            severity,
            notes
        ))
        
        conn.commit()
        conn.close()
    
    def record_priority_feedback(
        self,
        finding: PrioritizedFinding,
        suggested_priority: str,
        reason: str
    ):
        """
        Record team feedback on priority accuracy.
        
        Args:
            finding: The prioritized finding
            suggested_priority: What priority the team thinks it should be
            reason: Why they think the priority should change
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute("""
            INSERT INTO feedback (
                finding_id, feedback_type, timestamp,
                original_priority, suggested_priority, notes
            ) VALUES (?, ?, ?, ?, ?, ?)
        """, (
            finding.finding.id,
            FeedbackType.PRIORITY_ACCURACY.value,
            datetime.utcnow().isoformat(),
            finding.risk_score.priority_level,
            suggested_priority,
            reason
        ))
        
        # Store finding features
        self._store_finding_features(cursor, finding)
        
        conn.commit()
        conn.close()
    
    def record_context_correction(
        self,
        finding: PrioritizedFinding,
        corrected_context: Dict[str, Any],
        notes: Optional[str] = None
    ):
        """
        Record expert corrections to context detection.
        
        Args:
            finding: The prioritized finding
            corrected_context: Corrected context values
            notes: Explanation of the correction
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute("""
            INSERT INTO feedback (
                finding_id, feedback_type, timestamp,
                context_correction, notes, metadata
            ) VALUES (?, ?, ?, ?, ?, ?)
        """, (
            finding.finding.id,
            FeedbackType.CONTEXT_CORRECTION.value,
            datetime.utcnow().isoformat(),
            json.dumps(corrected_context),
            notes,
            json.dumps({
                'original_context': finding.finding.usage_context.value,
                'original_sensitivity': finding.finding.data_sensitivity.value if finding.finding.data_sensitivity else None,
                'original_duration': finding.finding.exposure_duration.value if finding.finding.exposure_duration else None
            })
        ))
        
        # Store finding features
        self._store_finding_features(cursor, finding)
        
        conn.commit()
        conn.close()
    
    def _store_finding_features(self, cursor, finding: PrioritizedFinding):
        """Store finding features for ML training."""
        cursor.execute("""
            INSERT INTO finding_features (
                finding_id, timestamp, algorithm, usage_context,
                data_sensitivity, exposure_duration, is_external_facing,
                file_path, description, title, risk_score, priority
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            finding.finding.id,
            datetime.utcnow().isoformat(),
            finding.finding.algorithm.value,
            finding.finding.usage_context.value,
            finding.finding.data_sensitivity.value if finding.finding.data_sensitivity else None,
            finding.finding.exposure_duration.value if finding.finding.exposure_duration else None,
            finding.finding.is_external_facing,
            finding.finding.file_path,
            finding.finding.description,
            finding.finding.title,
            finding.risk_score.final_score,
            finding.risk_score.priority_level
        ))
    
    def get_training_data(
        self,
        feedback_types: Optional[List[FeedbackType]] = None,
        min_samples: int = 10
    ) -> List[Dict[str, Any]]:
        """
        Get training data from collected feedback.
        
        Args:
            feedback_types: Types of feedback to include
            min_samples: Minimum number of samples required
            
        Returns:
            List of training samples with features and labels
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Build query
        query = """
            SELECT 
                f.finding_id,
                f.feedback_type,
                f.original_priority,
                f.suggested_priority,
                f.actual_priority,
                f.was_exploited,
                f.exploitation_severity,
                f.context_correction,
                ff.algorithm,
                ff.usage_context,
                ff.data_sensitivity,
                ff.exposure_duration,
                ff.is_external_facing,
                ff.file_path,
                ff.description,
                ff.title,
                ff.risk_score
            FROM feedback f
            JOIN finding_features ff ON f.finding_id = ff.finding_id
        """
        
        if feedback_types:
            placeholders = ','.join('?' * len(feedback_types))
            query += f" WHERE f.feedback_type IN ({placeholders})"
            cursor.execute(query, [ft.value for ft in feedback_types])
        else:
            cursor.execute(query)
        
        rows = cursor.fetchall()
        conn.close()
        
        if len(rows) < min_samples:
            return []
        
        # Convert to training samples
        training_data = []
        for row in rows:
            sample = {
                'finding_id': row[0],
                'feedback_type': row[1],
                'features': {
                    'algorithm': row[8],
                    'usage_context': row[9],
                    'data_sensitivity': row[10],
                    'exposure_duration': row[11],
                    'is_external_facing': bool(row[12]),
                    'file_path': row[13],
                    'description': row[14],
                    'title': row[15],
                    'risk_score': row[16]
                },
                'labels': {
                    'original_priority': row[2],
                    'suggested_priority': row[3],
                    'actual_priority': row[4],
                    'was_exploited': bool(row[5]) if row[5] is not None else None,
                    'exploitation_severity': row[6],
                    'context_correction': json.loads(row[7]) if row[7] else None
                }
            }
            training_data.append(sample)
        
        return training_data
    
    def get_statistics(self) -> Dict[str, Any]:
        """Get statistics about collected feedback."""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Total feedback count
        cursor.execute("SELECT COUNT(*) FROM feedback")
        total_feedback = cursor.fetchone()[0]
        
        # Feedback by type
        cursor.execute("""
            SELECT feedback_type, COUNT(*) 
            FROM feedback 
            GROUP BY feedback_type
        """)
        feedback_by_type = dict(cursor.fetchall())
        
        # Exploitation statistics
        cursor.execute("""
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN was_exploited = 1 THEN 1 ELSE 0 END) as exploited
            FROM feedback
            WHERE feedback_type = ?
        """, (FeedbackType.EXPLOITATION_ATTEMPT.value,))
        exploitation_stats = cursor.fetchone()
        
        # Priority accuracy
        cursor.execute("""
            SELECT 
                original_priority,
                COUNT(*) as count,
                AVG(CASE WHEN original_priority = actual_priority THEN 1.0 ELSE 0.0 END) as accuracy
            FROM feedback
            WHERE actual_priority IS NOT NULL
            GROUP BY original_priority
        """)
        priority_accuracy = cursor.fetchall()
        
        conn.close()
        
        return {
            'total_feedback': total_feedback,
            'feedback_by_type': feedback_by_type,
            'exploitation_stats': {
                'total_attempts': exploitation_stats[0] if exploitation_stats else 0,
                'successful_exploits': exploitation_stats[1] if exploitation_stats else 0
            },
            'priority_accuracy': [
                {
                    'priority': row[0],
                    'count': row[1],
                    'accuracy': row[2]
                }
                for row in priority_accuracy
            ]
        }
    
    def export_feedback(self, output_path: str):
        """Export all feedback to JSON file."""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT 
                f.*,
                ff.algorithm,
                ff.usage_context,
                ff.data_sensitivity,
                ff.exposure_duration,
                ff.is_external_facing,
                ff.file_path,
                ff.description,
                ff.title,
                ff.risk_score,
                ff.priority
            FROM feedback f
            LEFT JOIN finding_features ff ON f.finding_id = ff.finding_id
        """)
        
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]
        
        feedback_data = []
        for row in rows:
            feedback_data.append(dict(zip(columns, row)))
        
        conn.close()
        
        with open(output_path, 'w') as f:
            json.dump(feedback_data, f, indent=2)