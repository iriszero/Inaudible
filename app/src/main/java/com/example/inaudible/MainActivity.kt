package com.example.inaudible

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.R.attr.start
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import java.lang.Math.abs
import java.lang.Math.random
import java.sql.Timestamp
import kotlin.random.Random



class MainActivity : AppCompatActivity() {
    private var recorder: AudioRecord? = null
    private val RECORDER_SAMPLERATE = 44100
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private var recordingThread: Thread? = null
    private var BufferSize = 1024
    private val FFT_N = 512
    private val fsmFreq: IntArray = intArrayOf(19000, 20000, 21000) // 17000 for silence
    private var prvCode = 0
    private var lastCode = 0

    private var lastTimestamp: Long = 0

    private val TIMEOUT_MILLIS = 1000
    private var displayText: String = ""

    private fun getVisualizedString(freqData: DoubleArray): String {
        var s: String = ""
        for (i in 0 until FFT_N / 2 / 4) {
            var sum: Double = 0.0
            for (j in 0 until 3) {
                val now: Int = i * 4 + j
                sum += freqData[now]
            }
            s += "[%d-%d) %f\n".format(i * 4, i * 4 + 4, sum / 4.0)
        }
        return s
    }

    private fun getCode(freqData: DoubleArray): Int {
        var highFreqData = freqData.slice(216..FFT_N / 2).toDoubleArray()


        var idx = highFreqData.indices.maxBy { highFreqData[it] }!! + 216

        val freq: Int = idx * (RECORDER_SAMPLERATE / 2) / (FFT_N / 2)
        val code = fsmFreq.indices.minBy { abs(fsmFreq[it] - freq) }!!
        val ratio = freqData[idx]/ highFreqData.average()



        if (ratio >= 8.0) {
            Log.d("ratio", "%f".format(ratio))
            Log.d("code", code.toString())
            return code
        }
        else {
            // Log.i("code", code.toString())

            return 3
        }

    }

    private fun updateAudioData() {

        var codes : MutableList<Int> = ArrayList()

        while(true) {
            var sData = ShortArray(BufferSize)
            var code : Int


            recorder!!.read(sData, 0, BufferSize)
            var freqData : DoubleArray = FFT(n=FFT_N).getFreqSpectrumFromShort(sData)

            code = getCode(freqData)

            if (code == 3) {
                val currentTimestamp = System.currentTimeMillis()
                if (prvCode == 3) {
                    if (currentTimestamp - lastTimestamp >= TIMEOUT_MILLIS) {
                        displayText = ""
                        codes.clear()


                    }
                } else {
                    lastTimestamp = currentTimestamp
                }
            } else if (0 <= code && code <= 2) {
                if (code == prvCode) continue

                codes.add(code);
            }

            if (codes.size >= 8) {
                var sum : Int = 0
                var p : Int = 1
                for (i in 7 downTo 0) {
                    val t = (if (i==0) lastCode else codes[i-1])
                    if (codes[i] == (t -1 + 3)%3) {
                        //bit 1
                        sum += p
                    }
                    p *= 2
                }

                val c : Char = sum.toChar()
                Log.d("char", c.toString())
                //if (( ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c== ' '))
                displayText += c.toString()

                lastCode = codes[7]
                codes.clear()
                Log.d("clear", "==================")
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

        this.recorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferSize)
        this.recorder!!.startRecording()

        recordingThread = Thread(Runnable {
            updateAudioData()
        }, "AudioRecorder Thread")
        recordingThread!!.start()

    }

}
