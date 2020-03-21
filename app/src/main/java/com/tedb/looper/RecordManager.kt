package com.tedb.looper

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlin.concurrent.thread

class RecordManager {
    private var time : Int = 0
    private var isRecording : Boolean = false

    private val recordCallback : (isRecording:Boolean) -> Unit

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
    private var recordings : List<AudioRecording> = emptyList()
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
        recordThread!!.isRecording=false
        recordThread!!.join()
        recordThread!!.recording!!.startPlayback()
    }

    fun clearRecordings() {
        Log.d("looper","clearing recordings")
        recordings = emptyList()
    }
}