package com.tedb.looper

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlin.concurrent.thread

class RecordManager {
    // external connections
    private val recordCallback : (isRecording:Boolean) -> Unit

    // simple state
    private var time : Int = 0
    private var isRecording : Boolean = false
    // audio data
    private var currentRecording : AudioRecording? = null
    var audioTrack : AudioTrack? = null

    private val recorder : AudioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,
        EncodingType,
        AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            EncodingType
        )
    )
    private var recordThread : RecordThread? = null

    constructor(recordCallback: (isRecording:Boolean) -> Unit) {
        this.recordCallback = recordCallback
    }

    fun setRecording(value:Boolean) {
        isRecording=value
        recordCallback(value)
    }

    fun startRecording() {
        if (isRecording) return
        setRecording(true)
        recordThread = RecordThread()
        recordThread!!.recording = AudioRecording(time, BUFFER_BYTES)
        recordThread!!.recorder = recorder
        recordThread!!.start()
    }

    fun stopRecording() {
        recordCallback(false)
        // set flag to tell thread to stop
        recordThread!!.isRecording=false
        // wait for last sample to finish
        recordThread!!.join()

        recordThread!!.recording!!.createLoopingAudioTrack().play()

        setRecording(false)
    }

    fun clearRecordings() {
        Log.d("looper","clearing recordings")
        audioTrack?.stop()
    }
}