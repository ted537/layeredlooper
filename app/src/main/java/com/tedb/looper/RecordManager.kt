package com.tedb.looper

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlin.concurrent.thread

const val SAMPLE_RATE = 8000
const val MAX_SECONDS = 100
// the times two is because 16 bit is 2 bytes per element
const val BUFFER_BYTES = SAMPLE_RATE*2* MAX_SECONDS

class RecordManager {
    private var time : Int = 0
    private var isRecording : Boolean = false

    private val recordCallback : (isRecording:Boolean) -> Unit

    private val recorder : AudioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        BUFFER_BYTES
    )
    private var recordings : List<AudioRecording> = emptyList()
    private var newRecording : AudioRecording? = null
    private var recordThread : RecordThread? = null

    constructor(recordCallback: (isRecording:Boolean) -> Unit) {
        this.recordCallback = recordCallback
    }

    fun startRecording() {
        if (isRecording) return
        isRecording = true;
        recordCallback(true)
        recordThread = RecordThread()
        recordThread!!.recording = AudioRecording(time, BUFFER_BYTES)
        recordThread!!.recorder = recorder
        recordThread!!.start()
    }

    fun stopRecording() {
        recordCallback(false)
        recordThread?.isRecording=false
    }

    fun clearRecordings() {
        Log.d("looper","clearing recordings")
        recordings = emptyList()
    }
}