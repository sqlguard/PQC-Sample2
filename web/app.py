"""
FastAPI web application for interactive crypto vulnerability prioritization.
Provides drag-and-drop file upload, risk factor adjustment, and visual remediation planning.
"""

import json as json_module
import tempfile
from pathlib import Path
from typing import Dict, List, Optional, Any
from datetime import datetime

from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from ..parsers.json_parser import JSONParser
from ..parsers.sarif_parser import SARIFParser
from ..scoring.risk_scorer import RiskScorer
from ..analysis.context_analyzer import ContextAnalyzer
from ..models import PrioritizedFinding


# Initialize FastAPI app
app = FastAPI(
    title="Crypto Remediation Prioritizer",
    description="Interactive web UI for crypto vulnerability prioritization",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Setup templates and static files
templates_dir = Path(__file__).parent / "templates"
static_dir = Path(__file__).parent / "static"
templates_dir.mkdir(exist_ok=True)
static_dir.mkdir(exist_ok=True)

templates = Jinja2Templates(directory=str(templates_dir))
app.mount("/static", StaticFiles(directory=str(static_dir)), name="static")

# Global state (in production, use a database)
current_findings: List[PrioritizedFinding] = []
current_weights: Dict[str, float] = {
    'data_sensitivity': 0.2,
    'exposure_duration': 0.2,
    'exploitability': 0.2,
    'blast_radius': 0.2,
    'algorithm_weakness': 0.2
}


# Pydantic models for API
class WeightsUpdate(BaseModel):
    """Model for updating risk factor weights."""
    data_sensitivity: float
    exposure_duration: float
    exploitability: float
    blast_radius: float
    algorithm_weakness: float


class PriorityUpdate(BaseModel):
    """Model for updating finding priorities."""
    finding_ids: List[str]


class FindingResponse(BaseModel):
    """Model for finding response."""
    id: str
    title: str
    description: str
    algorithm: str
    usage_context: str
    risk_score: float
    priority: str
    remediation_guidance: Optional[str]
    estimated_effort: Optional[str]


@app.get("/", response_class=HTMLResponse)
async def root():
    """Serve the main web UI."""
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <title>Crypto Remediation Prioritizer</title>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                background: #020617;
                min-height: 100vh;
                padding: 20px;
            }
            .container {
                max-width: 1400px;
                margin: 0 auto;
                background: #0f172a;
                border-radius: 20px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.5), 0 0 100px rgba(6, 182, 212, 0.1);
                overflow: hidden;
                border: 1px solid #1e293b;
            }
            .header {
                background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
                color: #06b6d4;
                padding: 30px;
                text-align: center;
                border-bottom: 2px solid #06b6d4;
                position: relative;
            }
            .header::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 2px;
                background: linear-gradient(90deg, transparent, #06b6d4, transparent);
            }
            .header h1 {
                font-size: 2.5em;
                margin-bottom: 10px;
                text-shadow: 0 0 20px rgba(6, 182, 212, 0.5);
            }
            .header p {
                font-size: 1.1em;
                opacity: 0.9;
                color: #94a3b8;
            }
            .content {
                padding: 30px;
            }
            .upload-section {
                background: #1e293b;
                border: 3px dashed #06b6d4;
                border-radius: 15px;
                padding: 40px;
                text-align: center;
                margin-bottom: 30px;
                transition: all 0.3s;
                cursor: pointer;
            }
            .upload-section:hover {
                background: #334155;
                border-color: #22d3ee;
                box-shadow: 0 0 30px rgba(6, 182, 212, 0.2);
            }
            .upload-section.dragover {
                background: #1e3a5f;
                border-color: #22d3ee;
                transform: scale(1.02);
                box-shadow: 0 0 40px rgba(6, 182, 212, 0.3);
            }
            .upload-icon {
                font-size: 4em;
                margin-bottom: 20px;
                color: #06b6d4;
            }
            .upload-section h2 {
                color: #06b6d4;
                margin-bottom: 10px;
                text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
            }
            .upload-section p {
                color: #94a3b8;
                margin-bottom: 20px;
            }
            .file-input {
                display: none;
            }
            .btn {
                background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
                color: #020617;
                border: none;
                padding: 12px 30px;
                border-radius: 25px;
                font-size: 1em;
                cursor: pointer;
                transition: all 0.2s;
                font-weight: 600;
                box-shadow: 0 0 20px rgba(6, 182, 212, 0.3);
            }
            .btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 5px 25px rgba(6, 182, 212, 0.5);
                background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%);
            }
            .weights-section {
                background: #1e293b;
                border-radius: 15px;
                padding: 25px;
                margin-bottom: 30px;
                display: none;
                border: 1px solid #334155;
            }
            .weights-section h3 {
                color: #06b6d4;
                margin-bottom: 20px;
                text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
            }
            .weight-control {
                margin-bottom: 20px;
            }
            .weight-control label {
                display: block;
                margin-bottom: 8px;
                color: #94a3b8;
                font-weight: 500;
            }
            .weight-slider {
                width: 100%;
                height: 8px;
                border-radius: 5px;
                background: #334155;
                outline: none;
                -webkit-appearance: none;
            }
            .weight-slider::-webkit-slider-thumb {
                -webkit-appearance: none;
                appearance: none;
                width: 20px;
                height: 20px;
                border-radius: 50%;
                background: #06b6d4;
                cursor: pointer;
                box-shadow: 0 0 10px rgba(6, 182, 212, 0.5);
            }
            .weight-slider::-moz-range-thumb {
                width: 20px;
                height: 20px;
                border-radius: 50%;
                background: #06b6d4;
                cursor: pointer;
                border: none;
                box-shadow: 0 0 10px rgba(6, 182, 212, 0.5);
            }
            .weight-value {
                display: inline-block;
                min-width: 50px;
                text-align: right;
                color: #06b6d4;
                font-weight: bold;
            }
            .findings-section {
                display: none;
            }
            .findings-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
            }
            .findings-header h3 {
                color: #06b6d4;
                text-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
            }
            .stats {
                display: flex;
                gap: 20px;
            }
            .stat {
                text-align: center;
            }
            .stat-value {
                font-size: 2em;
                font-weight: bold;
                color: #06b6d4;
                text-shadow: 0 0 10px rgba(6, 182, 212, 0.5);
            }
            .stat-label {
                font-size: 0.9em;
                color: #94a3b8;
            }
            .findings-list {
                display: flex;
                flex-direction: column;
                gap: 15px;
            }
            .finding-card {
                background: #1e293b;
                border: 2px solid #334155;
                border-radius: 10px;
                padding: 20px;
                cursor: move;
                transition: all 0.3s;
            }
            .finding-card:hover {
                box-shadow: 0 5px 25px rgba(6, 182, 212, 0.2);
                transform: translateY(-2px);
                border-color: #06b6d4;
            }
            .finding-card.dragging {
                opacity: 0.5;
            }
            .finding-header {
                display: flex;
                justify-content: space-between;
                align-items: start;
                margin-bottom: 15px;
            }
            .finding-title {
                flex: 1;
                font-size: 1.2em;
                font-weight: 600;
                color: #e2e8f0;
            }
            .priority-badge {
                padding: 5px 15px;
                border-radius: 20px;
                font-size: 0.85em;
                font-weight: bold;
                text-transform: uppercase;
                box-shadow: 0 0 10px currentColor;
            }
            .priority-CRITICAL {
                background: #dc2626;
                color: white;
            }
            .priority-HIGH {
                background: #ea580c;
                color: white;
            }
            .priority-MEDIUM {
                background: #eab308;
                color: #000;
            }
            .priority-LOW {
                background: #16a34a;
                color: white;
            }
            .finding-details {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 15px;
                margin-bottom: 15px;
            }
            .detail-item {
                display: flex;
                flex-direction: column;
            }
            .detail-label {
                font-size: 0.85em;
                color: #94a3b8;
                margin-bottom: 5px;
            }
            .detail-value {
                font-weight: 500;
                color: #cbd5e1;
            }
            .risk-score {
                font-size: 1.5em;
                color: #06b6d4;
                font-weight: bold;
                text-shadow: 0 0 10px rgba(6, 182, 212, 0.5);
            }
            .finding-guidance {
                background: #0f172a;
                border-left: 4px solid #06b6d4;
                padding: 15px;
                border-radius: 5px;
                margin-top: 15px;
                box-shadow: 0 0 20px rgba(6, 182, 212, 0.1);
            }
            .guidance-title {
                font-weight: 600;
                color: #06b6d4;
                margin-bottom: 8px;
            }
            .guidance-text {
                color: #94a3b8;
                line-height: 1.6;
            }
            .loading {
                text-align: center;
                padding: 40px;
                color: #06b6d4;
            }
            .spinner {
                border: 4px solid #334155;
                border-top: 4px solid #06b6d4;
                border-radius: 50%;
                width: 40px;
                height: 40px;
                animation: spin 1s linear infinite;
                margin: 0 auto 20px;
                box-shadow: 0 0 20px rgba(6, 182, 212, 0.3);
            }
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
            .error {
                background: #7f1d1d;
                color: #fca5a5;
                padding: 15px;
                border-radius: 10px;
                margin-bottom: 20px;
                border: 1px solid #dc2626;
                box-shadow: 0 0 20px rgba(220, 38, 38, 0.3);
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>🔐 Crypto Remediation Prioritizer</h1>
                <p>Interactive Risk-Based Vulnerability Prioritization</p>
            </div>
            
            <div class="content">
                <!-- Upload Section -->
                <div class="upload-section" id="uploadSection">
                    <div class="upload-icon">📁</div>
                    <h2>Upload Vulnerability Report</h2>
                    <p>Drag and drop your JSON or SARIF file here, or click to browse</p>
                    <input type="file" id="fileInput" class="file-input" accept=".json,.sarif">
                    <button class="btn" onclick="document.getElementById('fileInput').click()">
                        Choose File
                    </button>
                </div>
                
                <!-- Weights Section -->
                <div class="weights-section" id="weightsSection">
                    <h3>⚖️ Adjust Risk Factor Weights</h3>
                    <div class="weight-control">
                        <label>
                            Data Sensitivity
                            <span class="weight-value" id="weightDataSensitivity">20%</span>
                        </label>
                        <input type="range" class="weight-slider" id="sliderDataSensitivity" 
                               min="0" max="100" value="20" step="5">
                    </div>
                    <div class="weight-control">
                        <label>
                            Exposure Duration
                            <span class="weight-value" id="weightExposureDuration">20%</span>
                        </label>
                        <input type="range" class="weight-slider" id="sliderExposureDuration" 
                               min="0" max="100" value="20" step="5">
                    </div>
                    <div class="weight-control">
                        <label>
                            Exploitability
                            <span class="weight-value" id="weightExploitability">20%</span>
                        </label>
                        <input type="range" class="weight-slider" id="sliderExploitability" 
                               min="0" max="100" value="20" step="5">
                    </div>
                    <div class="weight-control">
                        <label>
                            Blast Radius
                            <span class="weight-value" id="weightBlastRadius">20%</span>
                        </label>
                        <input type="range" class="weight-slider" id="sliderBlastRadius" 
                               min="0" max="100" value="20" step="5">
                    </div>
                    <div class="weight-control">
                        <label>
                            Algorithm Weakness
                            <span class="weight-value" id="weightAlgorithmWeakness">20%</span>
                        </label>
                        <input type="range" class="weight-slider" id="sliderAlgorithmWeakness" 
                               min="0" max="100" value="20" step="5">
                    </div>
                    <button class="btn" onclick="applyWeights()">Apply Weights</button>
                </div>
                
                <!-- Findings Section -->
                <div class="findings-section" id="findingsSection">
                    <div class="findings-header">
                        <h3>📊 Prioritized Findings</h3>
                        <div class="stats">
                            <div class="stat">
                                <div class="stat-value" id="statCritical">0</div>
                                <div class="stat-label">Critical</div>
                            </div>
                            <div class="stat">
                                <div class="stat-value" id="statHigh">0</div>
                                <div class="stat-label">High</div>
                            </div>
                            <div class="stat">
                                <div class="stat-value" id="statMedium">0</div>
                                <div class="stat-label">Medium</div>
                            </div>
                            <div class="stat">
                                <div class="stat-value" id="statLow">0</div>
                                <div class="stat-label">Low</div>
                            </div>
                        </div>
                    </div>
                    <p style="color: #6c757d; margin-bottom: 20px;">
                        💡 Drag and drop findings to reorder priorities
                    </p>
                    <div class="findings-list" id="findingsList"></div>
                </div>
            </div>
        </div>
        
        <script>
            let findings = [];
            let draggedElement = null;
            
            // File upload handling
            const uploadSection = document.getElementById('uploadSection');
            const fileInput = document.getElementById('fileInput');
            
            uploadSection.addEventListener('click', (e) => {
                // Only trigger if clicking the section itself, not a button
                if (e.target === uploadSection || e.target.closest('.upload-section') === uploadSection) {
                    if (!e.target.classList.contains('btn')) {
                        fileInput.click();
                    }
                }
            });
            
            uploadSection.addEventListener('dragover', (e) => {
                e.preventDefault();
                uploadSection.classList.add('dragover');
            });
            
            uploadSection.addEventListener('dragleave', () => {
                uploadSection.classList.remove('dragover');
            });
            
            uploadSection.addEventListener('drop', (e) => {
                e.preventDefault();
                uploadSection.classList.remove('dragover');
                const files = e.dataTransfer.files;
                if (files && files.length > 0) {
                    uploadFile(files[0]);
                }
            });
            
            fileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) uploadFile(file);
                // Reset the input so the same file can be uploaded again if needed
                e.target.value = '';
            });
            
            async function uploadFile(file) {
                // Validate file
                if (!file) {
                    alert('No file selected');
                    return;
                }
                
                // Ensure file has a name
                if (!file.name) {
                    alert('Invalid file: no filename');
                    return;
                }
                
                const formData = new FormData();
                // Explicitly set filename to ensure it's sent properly
                formData.append('file', file, file.name);
                
                uploadSection.innerHTML = '<div class="loading"><div class="spinner"></div><p>Analyzing vulnerabilities...</p></div>';
                
                try {
                    const response = await fetch('/api/upload', {
                        method: 'POST',
                        body: formData
                    });
                    
                    if (!response.ok) {
                        // Try to get error message from response
                        let errorMessage = 'Upload failed';
                        try {
                            const errorData = await response.json();
                            if (errorData.detail) {
                                errorMessage = errorData.detail;
                            }
                        } catch (e) {
                            // If can't parse error, use default message
                        }
                        throw new Error(errorMessage);
                    }
                    
                    const data = await response.json();
                    findings = data.findings;
                    
                    document.getElementById('weightsSection').style.display = 'block';
                    document.getElementById('findingsSection').style.display = 'block';
                    uploadSection.style.display = 'none';
                    
                    renderFindings();
                    updateStats();
                } catch (error) {
                    uploadSection.innerHTML = `
                        <div class="error">
                            <strong>Error:</strong> ${error.message}
                        </div>
                        <div class="upload-icon">📁</div>
                        <h2>Upload Vulnerability Report</h2>
                        <p>Drag and drop your JSON or SARIF file here, or click to browse</p>
                        <button class="btn" onclick="document.getElementById('fileInput').click()">
                            Choose File
                        </button>
                    `;
                }
            }
            
            // Weight sliders
            const sliders = ['DataSensitivity', 'ExposureDuration', 'Exploitability', 'BlastRadius', 'AlgorithmWeakness'];
            let isNormalizing = false; // Flag to prevent recursive normalization
            
            sliders.forEach(name => {
                const slider = document.getElementById(`slider${name}`);
                const display = document.getElementById(`weight${name}`);
                slider.addEventListener('input', () => {
                    display.textContent = slider.value + '%';
                    if (!isNormalizing) {
                        normalizeWeights(name);
                    }
                });
            });
            
            function normalizeWeights(changedSlider) {
                isNormalizing = true; // Set flag to prevent recursive calls
                
                const total = sliders.reduce((sum, name) => {
                    return sum + parseInt(document.getElementById(`slider${name}`).value);
                }, 0);
                
                if (total !== 100) {
                    const diff = 100 - total;
                    const others = sliders.filter(n => n !== changedSlider);
                    const perOther = Math.floor(diff / others.length);
                    
                    others.forEach((name, i) => {
                        const slider = document.getElementById(`slider${name}`);
                        let newValue = parseInt(slider.value) + perOther;
                        if (i === 0) newValue += diff % others.length;
                        newValue = Math.max(0, Math.min(100, newValue));
                        slider.value = newValue;
                        document.getElementById(`weight${name}`).textContent = newValue + '%';
                    });
                }
                
                isNormalizing = false; // Reset flag after normalization complete
            }
            
            async function applyWeights() {
                // Get raw slider values
                const sliderValues = {
                    data_sensitivity: parseInt(document.getElementById('sliderDataSensitivity').value),
                    exposure_duration: parseInt(document.getElementById('sliderExposureDuration').value),
                    exploitability: parseInt(document.getElementById('sliderExploitability').value),
                    blast_radius: parseInt(document.getElementById('sliderBlastRadius').value),
                    algorithm_weakness: parseInt(document.getElementById('sliderAlgorithmWeakness').value)
                };
                
                // Calculate total to ensure it's exactly 100
                const total = Object.values(sliderValues).reduce((sum, val) => sum + val, 0);
                
                // Normalize to ensure weights sum to exactly 1.0
                const weights = {
                    data_sensitivity: sliderValues.data_sensitivity / total,
                    exposure_duration: sliderValues.exposure_duration / total,
                    exploitability: sliderValues.exploitability / total,
                    blast_radius: sliderValues.blast_radius / total,
                    algorithm_weakness: sliderValues.algorithm_weakness / total
                };
                
                // Verify sum is 1.0 (with floating point tolerance)
                const weightSum = Object.values(weights).reduce((sum, val) => sum + val, 0);
                if (Math.abs(weightSum - 1.0) > 0.001) {
                    alert(`Error: Weights sum to ${weightSum.toFixed(4)}, expected 1.0. Please adjust sliders.`);
                    return;
                }
                
                try {
                    const response = await fetch('/api/weights', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(weights)
                    });
                    
                    if (!response.ok) throw new Error('Failed to update weights');
                    
                    const data = await response.json();
                    findings = data.findings;
                    renderFindings();
                    updateStats();
                } catch (error) {
                    alert('Error updating weights: ' + error.message);
                }
            }
            
            function renderFindings() {
                const list = document.getElementById('findingsList');
                list.innerHTML = findings.map((f, index) => `
                    <div class="finding-card" draggable="true" data-index="${index}">
                        <div class="finding-header">
                            <div class="finding-title">${f.title}</div>
                            <span class="priority-badge priority-${f.priority}">${f.priority}</span>
                        </div>
                        <div class="finding-details">
                            <div class="detail-item">
                                <span class="detail-label">Risk Score</span>
                                <span class="risk-score">${f.risk_score.toFixed(2)}/20</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Algorithm</span>
                                <span class="detail-value">${f.algorithm}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Usage Context</span>
                                <span class="detail-value">${f.usage_context}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Estimated Effort</span>
                                <span class="detail-value">${f.estimated_effort || 'N/A'}</span>
                            </div>
                        </div>
                        ${f.remediation_guidance ? `
                        <div class="finding-guidance">
                            <div class="guidance-title">🔧 Remediation Guidance</div>
                            <div class="guidance-text">${f.remediation_guidance}</div>
                        </div>
                        ` : ''}
                    </div>
                `).join('');
                
                // Add drag and drop handlers
                document.querySelectorAll('.finding-card').forEach(card => {
                    card.addEventListener('dragstart', handleDragStart);
                    card.addEventListener('dragover', handleDragOver);
                    card.addEventListener('drop', handleDrop);
                    card.addEventListener('dragend', handleDragEnd);
                });
            }
            
            function handleDragStart(e) {
                draggedElement = this;
                this.classList.add('dragging');
                e.dataTransfer.effectAllowed = 'move';
            }
            
            function handleDragOver(e) {
                e.preventDefault();
                e.dataTransfer.dropEffect = 'move';
                
                const afterElement = getDragAfterElement(this.parentElement, e.clientY);
                if (afterElement == null) {
                    this.parentElement.appendChild(draggedElement);
                } else {
                    this.parentElement.insertBefore(draggedElement, afterElement);
                }
            }
            
            function handleDrop(e) {
                e.stopPropagation();
                return false;
            }
            
            function handleDragEnd(e) {
                this.classList.remove('dragging');
                
                // Update order
                const cards = Array.from(document.querySelectorAll('.finding-card'));
                const newOrder = cards.map(card => findings[parseInt(card.dataset.index)].id);
                updatePriorities(newOrder);
            }
            
            function getDragAfterElement(container, y) {
                const draggableElements = [...container.querySelectorAll('.finding-card:not(.dragging)')];
                
                return draggableElements.reduce((closest, child) => {
                    const box = child.getBoundingClientRect();
                    const offset = y - box.top - box.height / 2;
                    
                    if (offset < 0 && offset > closest.offset) {
                        return { offset: offset, element: child };
                    } else {
                        return closest;
                    }
                }, { offset: Number.NEGATIVE_INFINITY }).element;
            }
            
            async function updatePriorities(newOrder) {
                try {
                    const response = await fetch('/api/priorities', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ finding_ids: newOrder })
                    });
                    
                    if (!response.ok) throw new Error('Failed to update priorities');
                    
                    const data = await response.json();
                    findings = data.findings;
                    renderFindings();
                } catch (error) {
                    console.error('Error updating priorities:', error);
                }
            }
            
            function updateStats() {
                const stats = findings.reduce((acc, f) => {
                    acc[f.priority] = (acc[f.priority] || 0) + 1;
                    return acc;
                }, {});
                
                document.getElementById('statCritical').textContent = stats.CRITICAL || 0;
                document.getElementById('statHigh').textContent = stats.HIGH || 0;
                document.getElementById('statMedium').textContent = stats.MEDIUM || 0;
                document.getElementById('statLow').textContent = stats.LOW || 0;
            }
        </script>
    </body>
    </html>
    """


@app.post("/api/upload")
async def upload_file(file: UploadFile = File(...)):
    """
    Upload and analyze a vulnerability report file.
    
    Supports JSON and SARIF formats.
    """
    global current_findings
    
    try:
        # Validate filename
        if not file.filename:
            raise HTTPException(
                status_code=400,
                detail="No filename provided. Please select a valid file."
            )
        
        # Validate file extension
        suffix = Path(file.filename).suffix.lower()
        if suffix not in ['.json', '.sarif']:
            raise HTTPException(
                status_code=400,
                detail=f"Unsupported file format '{suffix}'. Please upload a JSON or SARIF file."
            )
        
        # Reset file pointer to beginning (in case it was read before)
        await file.seek(0)
        
        # Read and validate file content
        content = await file.read()
        
        # Check if file is empty
        if not content or len(content) == 0:
            raise HTTPException(
                status_code=400,
                detail="File is empty. Please upload a file with vulnerability data."
            )
        
        # Check if content is valid JSON
        try:
            json_data = json_module.loads(content)
        except json_module.JSONDecodeError as e:
            raise HTTPException(
                status_code=400,
                detail=f"Invalid JSON format: {str(e)}. Please ensure the file contains valid JSON."
            )
        
        # Save to temporary file
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix, mode='wb') as tmp:
            tmp.write(content)
            tmp_path = tmp.name
        
        # Parse file
        findings = []
        parse_error = None
        json_error = None
        sarif_error = None
        
        # Try JSON first
        try:
            parser = JSONParser(tmp_path)
            findings = parser.parse()
        except Exception as e:
            json_error = str(e)
            # Try SARIF
            try:
                parser = SARIFParser(tmp_path)
                findings = parser.parse()
            except Exception as e2:
                sarif_error = str(e2)
        
        if not findings:
            # Clean up
            Path(tmp_path).unlink()
            
            # Provide helpful error message
            if json_error and sarif_error:
                if "expected findings array" in json_error.lower():
                    detail = (
                        "File format not recognized. Expected JSON with 'findings' array or "
                        "'prioritized_findings' array, or SARIF 2.1.0 format. "
                        f"Please check the file structure."
                    )
                elif "no crypto" in json_error.lower() or "no crypto" in sarif_error.lower():
                    detail = (
                        "No crypto-related findings detected in the file. "
                        "Please ensure the file contains cryptographic vulnerability data."
                    )
                else:
                    detail = f"Unable to parse file. JSON error: {json_error}. SARIF error: {sarif_error}"
            else:
                detail = "No findings found in the uploaded file."
            
            raise HTTPException(status_code=400, detail=detail)
        
        # Analyze and score
        analyzer = ContextAnalyzer()
        scorer = RiskScorer(custom_weights=current_weights)
        
        # Analyze context for each finding
        for finding in findings:
            context = analyzer.analyze(finding)
            if not finding.data_sensitivity:
                finding.data_sensitivity = context['data_sensitivity']
            if not finding.exposure_duration:
                finding.exposure_duration = context['exposure_duration']
            if not finding.is_external_facing:
                finding.is_external_facing = context['is_external_facing']
        
        # Score and prioritize
        current_findings = scorer.prioritize_findings(findings)
        
        # Clean up
        Path(tmp_path).unlink()
        
        # Return findings
        return JSONResponse({
            "success": True,
            "findings": [
                {
                    "id": pf.finding.id,
                    "title": pf.finding.title,
                    "description": pf.finding.description,
                    "algorithm": pf.finding.algorithm.value,
                    "usage_context": pf.finding.usage_context.value,
                    "risk_score": pf.risk_score.final_score,
                    "priority": pf.risk_score.priority_level,
                    "remediation_guidance": pf.remediation_guidance,
                    "estimated_effort": pf.estimated_effort
                }
                for pf in current_findings
            ]
        })
        
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/weights")
async def update_weights(weights: WeightsUpdate):
    """
    Update risk factor weights and re-score findings.
    """
    global current_findings, current_weights
    
    try:
        # Validate weights sum to 1.0
        total = (weights.data_sensitivity + weights.exposure_duration + 
                weights.exploitability + weights.blast_radius + 
                weights.algorithm_weakness)
        
        if abs(total - 1.0) > 0.01:
            raise HTTPException(status_code=400, detail="Weights must sum to 1.0")
        
        # Update weights
        current_weights = weights.dict()
        
        # Re-score with new weights
        scorer = RiskScorer(custom_weights=current_weights)
        findings = [pf.finding for pf in current_findings]
        current_findings = scorer.prioritize_findings(findings)
        
        return JSONResponse({
            "success": True,
            "findings": [
                {
                    "id": pf.finding.id,
                    "title": pf.finding.title,
                    "description": pf.finding.description,
                    "algorithm": pf.finding.algorithm.value,
                    "usage_context": pf.finding.usage_context.value,
                    "risk_score": pf.risk_score.final_score,
                    "priority": pf.risk_score.priority_level,
                    "remediation_guidance": pf.remediation_guidance,
                    "estimated_effort": pf.estimated_effort
                }
                for pf in current_findings
            ]
        })
        
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/priorities")
async def update_priorities(update: PriorityUpdate):
    """
    Update finding priorities based on manual reordering.
    """
    global current_findings
    
    try:
        # Reorder findings based on provided IDs
        id_to_finding = {pf.finding.id: pf for pf in current_findings}
        current_findings = [id_to_finding[fid] for fid in update.finding_ids if fid in id_to_finding]
        
        return JSONResponse({
            "success": True,
            "findings": [
                {
                    "id": pf.finding.id,
                    "title": pf.finding.title,
                    "description": pf.finding.description,
                    "algorithm": pf.finding.algorithm.value,
                    "usage_context": pf.finding.usage_context.value,
                    "risk_score": pf.risk_score.final_score,
                    "priority": pf.risk_score.priority_level,
                    "remediation_guidance": pf.remediation_guidance,
                    "estimated_effort": pf.estimated_effort
                }
                for pf in current_findings
            ]
        })
        
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/api/stats")
async def get_stats():
    """Get statistics about current findings."""
    stats = {
        "total": len(current_findings),
        "critical": sum(1 for pf in current_findings if pf.risk_score.priority_level == "CRITICAL"),
        "high": sum(1 for pf in current_findings if pf.risk_score.priority_level == "HIGH"),
        "medium": sum(1 for pf in current_findings if pf.risk_score.priority_level == "MEDIUM"),
        "low": sum(1 for pf in current_findings if pf.risk_score.priority_level == "LOW"),
        "average_score": sum(pf.risk_score.final_score for pf in current_findings) / len(current_findings) if current_findings else 0
    }
    
    return JSONResponse(stats)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)