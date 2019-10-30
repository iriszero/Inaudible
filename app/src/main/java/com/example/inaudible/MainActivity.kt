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
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioFormat
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T






class MainActivity : AppCompatActivity() {
    private var recorder : AudioRecord? = null
    private val RECORDER_SAMPLERATE = 44100
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private var recordingThread: Thread? = null
    private var BufferSize = 1024 // want to play 2048 (2K) since 2 bytes we use only 1024


    private fun updateAudioData() {
        var sData = ShortArray(BufferSize)
        recorder.read(sData, 0, BufferSize);
        var ret = FFT(n=512).getFreqSpectrumFromShort(sData)

        print(ret.toString())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferSize)

        recorder!!.startRecording()

        recordingThread = Thread(Runnable { updateAudioData() }, "AudioRecorder Thread")
        recordingThread!!.start()



    }
}
