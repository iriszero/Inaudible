package com.example.inaudible

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.R.attr.start
import android.media.MediaRecorder
import android.media.AudioRecord
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.AudioFormat
import android.util.Log
import android.widget.TextView
import java.lang.Math.abs
import java.lang.Math.random
import java.sql.Timestamp
import kotlin.random.Random



class MainActivity : AppCompatActivity() {
    private var recorder : AudioRecord? = null
    private val RECORDER_SAMPLERATE = 44100
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private var recordingThread: Thread? = null
    private var BufferSize = 1024
    private val FFT_N = 512
    private val fsmFreq : IntArray = intArrayOf(19000, 20000, 21000, 17000) // 17000 for silence
    private var prvCode = 0 // 17000
    private var lastTimestamp : Long = 0
    private val connectedAudio = false
    private val TIMEOUT_MILLIS = 1000
    private var displayText : String = ""

    private fun updateAudioData() {

        var bits : MutableList<Boolean> = ArrayList()
        while(true) {
            var sData = ShortArray(BufferSize)
            var code = 3

            if (connectedAudio) {
                recorder!!.read(sData, 0, BufferSize);
                var freqData : DoubleArray = FFT(n=FFT_N).getFreqSpectrumFromShort(sData)
                val idx = freqData.indexOf(freqData.max()!!)

                val freq : Int = idx * RECORDER_SAMPLERATE / FFT_N
                code = fsmFreq.indices.minBy{ abs(fsmFreq[it] - idx) }!!
            }
            else {
                code = (0..2).random()
            }

            if (code == 3) {
                val currentTimestamp = System.currentTimeMillis()
                if (prvCode == 3) {
                    if (currentTimestamp - lastTimestamp >= TIMEOUT_MILLIS) {
                        displayText = ""
                    }
                } else {
                    lastTimestamp = currentTimestamp
                }
            } else if (0 <= code && code <= 2) {
                if (code == prvCode) continue;

                var currentBit : Boolean
                if ((code+1)%3 == prvCode) {
                    currentBit = false
                } else {
                    currentBit = true
                }
                bits.add(currentBit)
            }


            if (bits.size >= 8) {
                var sum : Int = 0
                var p : Int = 1
                for (i in 0 until 7) {
                    if (bits[i]) {
                        sum += p
                    }
                    p *= 2
                }

                val c : Char = sum.toChar()

                if (( ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c== ' '))
                    displayText += c.toString()
                bits.clear()
            }

            prvCode = code
            runOnUiThread {
                val text1 : TextView = findViewById(R.id.text1)
                text1.setText(displayText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (connectedAudio) {
            this.recorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING, BufferSize)
            this.recorder!!.startRecording()
        }

        recordingThread = Thread(Runnable {
            updateAudioData()
        }, "AudioRecorder Thread")
        recordingThread!!.start()


    }
}
