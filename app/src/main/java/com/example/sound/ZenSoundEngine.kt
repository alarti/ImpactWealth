package com.example.sound

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * An advanced fully offline, zero-key, zero-dependency procedural audio engine.
 * Synthesizes a relaxing 432Hz binaural drone (deep theta wave focus) mixed in real-time
 * with random wind chime strikes from the Zen Pentatonic Scale.
 */
object ZenSoundEngine {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Scale tones for Zen Wind Chimes (Pentatonic Scale in A)
    private val PENTATONIC_SCALE = doubleArrayOf(
        440.00, // A4
        493.88, // B4
        554.37, // C#5
        659.25, // E5
        739.99, // F#5
        880.00  // A5
    )

    // Synchronized play state
    @Synchronized
    fun start(
        modeName: String? = null,
        inhaleSec: Int = 0,
        holdInSec: Int = 0,
        exhaleSec: Int = 0,
        holdOutSec: Int = 0
    ) {
        if (isPlaying) return
        isPlaying = true

        val baseFreq = when (modeName) {
            "SIMPLE" -> 432.0   // Universal Healing
            "BOX" -> 417.0      // Wiping out negativity / Focus
            "SLEEP" -> 396.0    // Liberating from fear
            "COHERENCE" -> 528.0 // Transformation / DNA Repair
            else -> 432.0
        }
        val beatFreq = when (modeName) {
            "SIMPLE" -> 8.0     // Alpha (Relaxed)
            "BOX" -> 14.0       // Beta (Focus)
            "SLEEP" -> 3.0      // Delta (Deep Sleep)
            "COHERENCE" -> 6.0  // Theta (Meditative Sync)
            else -> 4.0
        }
        val bpm = when (modeName) {
            "SIMPLE" -> 60.0
            "BOX" -> 70.0
            "SLEEP" -> 50.0
            "COHERENCE" -> 55.0
            else -> 60.0
        }

        job = scope.launch {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val bufferSize = minBufferSize.coerceAtLeast(16384)
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            audioTrack = track
            try {
                track.play()
            } catch (e: Exception) {
                return@launch
            }

            // Phase and sound parameters
            var phaseLeft = 0.0
            var phaseRight = 0.0
            val twoPi = 2.0 * Math.PI

            // Binaural setup guides brain to specific wave
            val leftIncrement = baseFreq / sampleRate
            val rightIncrement = (baseFreq + beatFreq) / sampleRate

            // Heartbeat simulation
            var heartbeatPhase = 0.0
            val heartbeatIncrement = (bpm / 60.0) / sampleRate

            // Breathing cycle envelope
            val cycleLength = inhaleSec + holdInSec + exhaleSec + holdOutSec
            var cyclePhase = 0.0
            val cycleIncrement = if (cycleLength > 0) 1.0 / (sampleRate * cycleLength) else 0.0

            // Wind Chime registers
            var chimeActive = false
            var chimeFreq = 0.0
            var chimePhase = 0.0
            var chimeSampleIdx = 0L
            var nextChimeDelaySamples = (sampleRate * 2.0).toLong() // Next strike in ~2 seconds
            var samplesSinceLastStrike = 0L

            val writeBuffer = ShortArray(2048)

            while (isPlaying && coroutineContext.isActive) {
                for (i in 0 until writeBuffer.size step 2) {
                    
                    // --- Breathing Envelope ---
                    var breathMultiplier = 1.0
                    if (cycleLength > 0) {
                        val pInhale = inhaleSec.toDouble() / cycleLength
                        val pHoldIn = pInhale + holdInSec.toDouble() / cycleLength
                        val pExhale = pHoldIn + exhaleSec.toDouble() / cycleLength
                        
                        breathMultiplier = when {
                            cyclePhase < pInhale -> 0.3 + 0.7 * (if (pInhale > 0.0) cyclePhase / pInhale else 1.0)
                            cyclePhase < pHoldIn -> 1.0
                            cyclePhase < pExhale -> 1.0 - 0.7 * (if (exhaleSec > 0) (cyclePhase - pHoldIn) / (exhaleSec.toDouble() / cycleLength) else 1.0)
                            else -> 0.3
                        }
                        
                        cyclePhase += cycleIncrement
                        if (cyclePhase > 1.0) cyclePhase -= 1.0
                    }

                    // --- Heartbeat Envelope ---
                    heartbeatPhase += heartbeatIncrement
                    if (heartbeatPhase > 1.0) heartbeatPhase -= 1.0
                    
                    // Double pulse simulating real heart (lub-dub)
                    val pulse1 = Math.max(0.0, sin(Math.PI * (heartbeatPhase * 5.0))) 
                    val pulse2 = Math.max(0.0, sin(Math.PI * ((heartbeatPhase - 0.2) * 5.0)))
                    val heartBump = if (heartbeatPhase < 0.2) Math.pow(pulse1, 4.0) else if (heartbeatPhase in 0.2..0.4) Math.pow(pulse2, 4.0) else 0.0
                    val throb = 0.85 + 0.15 * heartBump

                    // 1. Calculate the binaural hum drone (low amplitude, soothing)
                    val droneL = sin(phaseLeft * twoPi) * 1600.0 * throb * breathMultiplier
                    val droneR = sin(phaseRight * twoPi) * 1600.0 * throb * breathMultiplier

                    phaseLeft += leftIncrement
                    if (phaseLeft > 1.0) phaseLeft -= 1.0
                    phaseRight += rightIncrement
                    if (phaseRight > 1.0) phaseRight -= 1.0

                    // 2. Schedule and calculate Wind Chime strike
                    samplesSinceLastStrike++
                    if (samplesSinceLastStrike >= nextChimeDelaySamples) {
                        // Strike a new random chime note
                        chimeActive = true
                        chimeFreq = PENTATONIC_SCALE[Random.nextInt(PENTATONIC_SCALE.size)]
                        chimePhase = 0.0
                        chimeSampleIdx = 0
                        samplesSinceLastStrike = 0
                        // Next chime strike randomly spaced between 3 and 7 seconds
                        nextChimeDelaySamples = (sampleRate * (3.0 + Random.nextDouble() * 4.0)).toLong()
                    }

                    var chimeVal = 0.0
                    if (chimeActive) {
                        // Exponential decay envelope calculation (chime dies out over time)
                        val elapsedSecs = chimeSampleIdx.toDouble() / sampleRate
                        val amplitude = 6000.0 * exp(-elapsedSecs * 1.8) // Decays over ~2 seconds

                        chimeVal = sin(chimePhase * twoPi) * amplitude

                        // Apply frequency rotation
                        chimePhase += chimeFreq / sampleRate
                        if (chimePhase > 1.0) chimePhase -= 1.0

                        chimeSampleIdx++
                        if (amplitude < 1.0) {
                            chimeActive = false
                        }
                    }

                    // 3. Combine levels and write to buffer
                    val finalL = (droneL + chimeVal).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    val finalR = (droneR + chimeVal).coerceIn(-32767.0, 32767.0).toInt().toShort()

                    writeBuffer[i] = finalL
                    if (i + 1 < writeBuffer.size) {
                        writeBuffer[i + 1] = finalR
                    }
                }

                track.write(writeBuffer, 0, writeBuffer.size)
            }

            try {
                track.stop()
                track.release()
            } catch (e: Exception) {
                // ignore
            }
            } catch (e: Exception) {
               // handle
            }
        }
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        job?.cancel()
        job = null
        audioTrack = null
    }

    fun isPlaying(): Boolean = isPlaying
}
