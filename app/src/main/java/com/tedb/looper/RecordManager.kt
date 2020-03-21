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


    fun startRecording() {
        if (recordThread!=null) return
        recordCallback(true)
        recordThread = RecordThread()

        var time = 0
        var maxFrames = BUFFER_BYTES
        if (audioTrack!=null) {
            time = audioTrack!!.playbackHeadPosition!!
            maxFrames = audioTrack!!.bufferSizeInFrames!!*2
        }

        recordThread!!.recording = AudioRecording(time, maxFrames)
        recordThread!!.recorder = recorder
        recordThread!!.start()
    }

    fun stopRecording() {
        recordCallback(false)
        // set flag to tell thread to stop
        recordThread!!.isRecording=false
        // wait for last sample to finish
        recordThread!!.join()

        currentRecording = mixRecordings(currentRecording,recordThread!!.recording!!)
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = currentRecording!!.createLoopingAudioTrack()
        audioTrack!!.play()

        recordCallback(false)
        recordThread = null

    }

    fun clearRecordings() {
        Log.d("looper","clearing recordings")
        audioTrack?.stop()
    }
}