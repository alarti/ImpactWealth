package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class BreathingMode(val displayName: String, val description: String, val inhaleSec: Int, val holdInSec: Int, val exhaleSec: Int, val holdOutSec: Int) {
    SIMPLE("Calma Simple", "Ritmo equilibrado para soltar el estrés de inmediato.", 4, 0, 4, 0),
    BOX("Caja Zen", "Máxima concentración. Utilizado para aquietar el parloteo mental.", 4, 4, 4, 4),
    SLEEP("Sueño Profundo", "Patrón 4-7-8 para calmar la ansiedad nocturna y dormir feliz.", 4, 7, 8, 0),
    COHERENCE("Sincronía Zen", "Coherencia cardíaca ideal para equilibrar tu sistema nervioso.", 5, 0, 5, 0)
}

enum class BreathingPhase(val label: String) {
    INHALE("Inspira profundo..."),
    HOLD_IN("Sostén el aire..."),
    EXHALE("Expulsa suavemente..."),
    HOLD_OUT("Quédate en silencio...")
}

class OasisViewModel(application: Application) : AndroidViewModel(application) {
    private val db = OasisDatabase.getDatabase(application)
    private val repository = OasisRepository(db.oasisDao())

    // Tone Generator for closed-eye audio feedback cues (M3 design best practice)
    private var toneGenerator: ToneGenerator? = null
    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {}
    }

    // --- State: Database Collections ---
    val savedMantras: StateFlow<List<MantraEntity>> = repository.allMantras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedScenarios: StateFlow<List<OasisScenarioEntity>> = repository.allScenarios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSessions: StateFlow<List<SessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State: Stats & Streak Calculations ---
    val currentStreak: StateFlow<Int> = savedSessions.map { sessions ->
        if (sessions.isEmpty()) return@map 0
        val utcDates = sessions.map { 
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
            calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)
        }.distinct().sortedDescending()
        
        var streak = 0
        val todayCalendar = java.util.Calendar.getInstance()
        val todayCode = todayCalendar.get(java.util.Calendar.YEAR) * 1000 + todayCalendar.get(java.util.Calendar.DAY_OF_YEAR)
        
        val yesterdayCalendar = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        val yesterdayCode = yesterdayCalendar.get(java.util.Calendar.YEAR) * 1000 + yesterdayCalendar.get(java.util.Calendar.DAY_OF_YEAR)
        
        if (utcDates.firstOrNull() == todayCode || utcDates.firstOrNull() == yesterdayCode) {
            var currentCheck = utcDates.first()
            streak = 1
            for (i in 1..utcDates.lastIndex) {
                val prevCode = utcDates[i]
                val checkCalendar = java.util.Calendar.getInstance().apply {
                    val year = currentCheck / 1000
                    val day = currentCheck % 1000
                    set(java.util.Calendar.YEAR, year)
                    set(java.util.Calendar.DAY_OF_YEAR, day)
                    add(java.util.Calendar.DAY_OF_YEAR, -1)
                }
                val expectedPrevCode = checkCalendar.get(java.util.Calendar.YEAR) * 1000 + checkCalendar.get(java.util.Calendar.DAY_OF_YEAR)
                if (prevCode == expectedPrevCode) {
                    streak++
                    currentCheck = prevCode
                } else {
                    break
                }
            }
        }
        streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBreathingMinutes: StateFlow<Int> = savedSessions.map { sessions ->
        sessions.sumOf { it.durationSeconds } / 60
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSessionsCompleted: StateFlow<Int> = savedSessions.map { sessions ->
        sessions.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- State: Interactive Breathe Loop ---
    private val _isBreatheRunning = MutableStateFlow(false)
    val isBreatheRunning: StateFlow<Boolean> = _isBreatheRunning.asStateFlow()

    private val _selectedMode = MutableStateFlow(BreathingMode.SIMPLE)
    val selectedMode: StateFlow<BreathingMode> = _selectedMode.asStateFlow()

    private val _currentPhase = MutableStateFlow(BreathingPhase.INHALE)
    val currentPhase: StateFlow<BreathingPhase> = _currentPhase.asStateFlow()

    private val _phaseSecondsRemaining = MutableStateFlow(4)
    val phaseSecondsRemaining: StateFlow<Int> = _phaseSecondsRemaining.asStateFlow()

    private val _sessionProgressSec = MutableStateFlow(0)
    val sessionProgressSec: StateFlow<Int> = _sessionProgressSec.asStateFlow()

    private val _targetSessionMinutes = MutableStateFlow(1) // Default practice session 1 min
    val targetSessionMinutes: StateFlow<Int> = _targetSessionMinutes.asStateFlow()

    private val _isAudioAlertsEnabled = MutableStateFlow(true)
    val isAudioAlertsEnabled: StateFlow<Boolean> = _isAudioAlertsEnabled.asStateFlow()

    private var breatheJob: Job? = null

    fun selectBreathingMode(mode: BreathingMode) {
        if (!_isBreatheRunning.value) {
            _selectedMode.value = mode
            resetPhaseForMode(mode)
        }
    }

    fun setTargetSessionMinutes(mins: Int) {
        if (!_isBreatheRunning.value) {
            _targetSessionMinutes.value = mins
        }
    }

    fun toggleAudioAlerts() {
        _isAudioAlertsEnabled.value = !_isAudioAlertsEnabled.value
    }

    private fun resetPhaseForMode(mode: BreathingMode) {
        _currentPhase.value = BreathingPhase.INHALE
        _phaseSecondsRemaining.value = mode.inhaleSec
        _sessionProgressSec.value = 0
    }

    fun startBreathingSession() {
        breatheJob?.cancel()
        _isBreatheRunning.value = true
        resetPhaseForMode(_selectedMode.value)
        playPhaseBeep()

        breatheJob = viewModelScope.launch {
            while (isActive && _isBreatheRunning.value) {
                delay(1000)
                _sessionProgressSec.value += 1
                
                // Check if session has finished
                val targetSec = _targetSessionMinutes.value * 60
                if (_sessionProgressSec.value >= targetSec) {
                    saveCompletedPracticeSession()
                    stopBreathingSession(completedSuccessfully = true)
                    break
                }

                // Decrement phase time
                val rem = _phaseSecondsRemaining.value - 1
                if (rem > 0) {
                    _phaseSecondsRemaining.value = rem
                } else {
                    // Transition to next phase
                    transitionToNextPhase()
                }
            }
        }
    }

    fun stopBreathingSession(completedSuccessfully: Boolean = false) {
        _isBreatheRunning.value = false
        breatheJob?.cancel()
        breatheJob = null
        resetPhaseForMode(_selectedMode.value)
        if (completedSuccessfully) {
            playCompletionBeep()
        }
    }

    private fun transitionToNextPhase() {
        val mode = _selectedMode.value
        val next = when (_currentPhase.value) {
            BreathingPhase.INHALE -> {
                if (mode.holdInSec > 0) BreathingPhase.HOLD_IN 
                else if (mode.exhaleSec > 0) BreathingPhase.EXHALE 
                else if (mode.holdOutSec > 0) BreathingPhase.HOLD_OUT 
                else BreathingPhase.INHALE
            }
            BreathingPhase.HOLD_IN -> {
                if (mode.exhaleSec > 0) BreathingPhase.EXHALE 
                else if (mode.holdOutSec > 0) BreathingPhase.HOLD_OUT 
                else BreathingPhase.INHALE
            }
            BreathingPhase.EXHALE -> {
                if (mode.holdOutSec > 0) BreathingPhase.HOLD_OUT 
                else BreathingPhase.INHALE
            }
            BreathingPhase.HOLD_OUT -> {
                BreathingPhase.INHALE
            }
        }

        _currentPhase.value = next
        _phaseSecondsRemaining.value = when (next) {
            BreathingPhase.INHALE -> mode.inhaleSec
            BreathingPhase.HOLD_IN -> mode.holdInSec
            BreathingPhase.EXHALE -> mode.exhaleSec
            BreathingPhase.HOLD_OUT -> mode.holdOutSec
        }
        playPhaseBeep()
    }

    private fun playPhaseBeep() {
        if (_isAudioAlertsEnabled.value) {
            viewModelScope.launch {
                try {
                    val tone = when (_currentPhase.value) {
                        BreathingPhase.INHALE -> ToneGenerator.TONE_PROP_BEEP
                        BreathingPhase.HOLD_IN -> ToneGenerator.TONE_CDMA_PIP
                        BreathingPhase.EXHALE -> ToneGenerator.TONE_PROP_BEEP2
                        BreathingPhase.HOLD_OUT -> ToneGenerator.TONE_CDMA_PIP
                    }
                    toneGenerator?.startTone(tone, 80)
                } catch (_: Exception) {}
            }
        }
    }

    private fun playCompletionBeep() {
        if (_isAudioAlertsEnabled.value) {
            viewModelScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                } catch (_: Exception) {}
            }
        }
    }

    private fun saveCompletedPracticeSession() {
        viewModelScope.launch {
            repository.insertSession(
                SessionEntity(
                    modeName = _selectedMode.value.displayName,
                    durationSeconds = _targetSessionMinutes.value * 60
                )
            )
        }
    }

    // --- Actions: Mantras Database CRUD ---
    fun saveMantra(text: String, category: String = "Personal") {
        viewModelScope.launch {
            repository.insertMantra(MantraEntity(text = text, category = category))
        }
    }

    fun removeMantra(mantra: MantraEntity) {
        viewModelScope.launch {
            repository.deleteMantra(mantra)
        }
    }

    // --- Actions: Scenarios Database CRUD ---
    fun saveScenario(title: String, metaphor: String, prompt: String, color1Hex: String, color2Hex: String) {
        viewModelScope.launch {
            repository.insertScenario(
                OasisScenarioEntity(
                    title = title,
                    metaphor = metaphor,
                    prompt = prompt,
                    color1Hex = color1Hex,
                    color2Hex = color2Hex,
                    isCustom = true
                )
            )
        }
    }

    fun removeScenario(scenario: OasisScenarioEntity) {
        viewModelScope.launch {
            repository.deleteScenario(scenario)
        }
    }

    fun clearAllStats() {
        viewModelScope.launch {
            repository.clearSessions()
        }
    }

    override fun onCleared() {
        super.onCleared()
        breatheJob?.cancel()
        toneGenerator?.release()
    }
}
