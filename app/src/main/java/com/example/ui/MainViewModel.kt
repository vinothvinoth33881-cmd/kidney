package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgreementStatus
import com.example.data.DiagnosticResult
import com.example.data.ExtractedFeatures
import com.example.data.KidneyDiseaseClass
import com.example.data.ModelPrediction
import com.example.data.SamplePresets
import com.example.data.SampleScanPreset
import com.example.data.UserProfile
import com.example.data.db.AppDatabase
import com.example.data.repository.ReportRepository
import com.example.ml.DiagnosisPipelineEngine
import com.example.ml.PipelineProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    HOME,
    DIAGNOSIS,
    RESULT,
    HISTORY,
    MODEL_INFO,
    ABOUT,
    PROFILE,
    AUTH
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReportRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ReportRepository(db.scanReportDao())
    }

    val historyReports: StateFlow<List<DiagnosticResult>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true) // Ready to explore, toggleable in Auth
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Scan selection & Analysis state
    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _selectedScanName = MutableStateFlow("axial_renal_tomogram.dcm")
    val selectedScanName: StateFlow<String> = _selectedScanName.asStateFlow()

    private val _selectedScanType = MutableStateFlow("Contrast CT Scan")
    val selectedScanType: StateFlow<String> = _selectedScanType.asStateFlow()

    private val _selectedPatientId = MutableStateFlow("PAT-2026-N104")
    val selectedPatientId: StateFlow<String> = _selectedPatientId.asStateFlow()

    private val _activePreset = MutableStateFlow<SampleScanPreset?>(null)
    val activePreset: StateFlow<SampleScanPreset?> = _activePreset.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _pipelineProgress = MutableStateFlow<PipelineProgress?>(null)
    val pipelineProgress: StateFlow<PipelineProgress?> = _pipelineProgress.asStateFlow()

    private val _currentResult = MutableStateFlow<DiagnosticResult?>(null)
    val currentResult: StateFlow<DiagnosticResult?> = _currentResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Pre-load the first sample case so user can immediately analyze or preview
        loadDefaultPreset()
    }

    private fun loadDefaultPreset() {
        val preset = SamplePresets.presets.firstOrNull() ?: return
        selectPreset(preset)
    }

    fun navigateTo(screen: AppScreen) {
        _errorMessage.value = null
        _currentScreen.value = screen
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun selectPreset(preset: SampleScanPreset) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val bitmap = BitmapFactory.decodeResource(context.resources, preset.drawableResId)
                withContext(Dispatchers.Main) {
                    _selectedBitmap.value = bitmap
                    _selectedScanName.value = "${preset.id}.dcm"
                    _selectedPatientId.value = preset.patientId
                    _selectedScanType.value = "Axial Renal CT"
                    _activePreset.value = preset
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Failed to load sample image: ${e.localizedMessage}"
                }
            }
        }
    }

    fun selectCustomUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        _selectedBitmap.value = bitmap
                        _selectedScanName.value = "uploaded_scan_${System.currentTimeMillis() % 10000}.png"
                        _selectedPatientId.value = "PAT-EXT-${(1000..9999).random()}"
                        _selectedScanType.value = "User Uploaded Scan (CT/MRI)"
                        _activePreset.value = null
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Could not decode image file format."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error reading selected file: ${e.localizedMessage}"
                }
            }
        }
    }

    fun setScanType(type: String) {
        _selectedScanType.value = type
    }

    fun startAnalysis() {
        val bitmap = _selectedBitmap.value
        if (bitmap == null) {
            _errorMessage.value = "Please select or upload a kidney scan first."
            return
        }

        _isAnalyzing.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val result = DiagnosisPipelineEngine.executePipeline(
                    bitmap = bitmap,
                    scanName = _selectedScanName.value,
                    scanType = _selectedScanType.value,
                    patientRef = _selectedPatientId.value,
                    samplePresetName = _activePreset.value?.title,
                    imageUri = null
                ) { progress ->
                    _pipelineProgress.value = progress
                }

                // Persist automatically to Room DB
                repository.saveReport(result)

                withContext(Dispatchers.Main) {
                    _currentResult.value = result
                    _isAnalyzing.value = false
                    _currentScreen.value = AppScreen.RESULT
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isAnalyzing.value = false
                    _errorMessage.value = "Pipeline processing error: ${e.localizedMessage}"
                }
            }
        }
    }

    fun viewReport(report: DiagnosticResult) {
        _currentResult.value = report
        _currentScreen.value = AppScreen.RESULT
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    fun clearAllReports() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun login(email: String, name: String) {
        _userProfile.value = UserProfile(
            fullName = name.ifBlank { "Dr. Clinical Radiologist" },
            email = email.ifBlank { "radiologist@hospital.org" },
            institution = "Department of Nephrology & Diagnostic Radiology"
        )
        _isLoggedIn.value = true
        _currentScreen.value = AppScreen.HOME
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentScreen.value = AppScreen.AUTH
    }
}
