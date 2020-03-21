package com.tedb.looper

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
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
        if (recordThread!=null) {
            Log.d("loop","ALREADY RECORDING")
            return
        }

        // restart from beginning
        audioTrack?.pause()
        audioTrack?.playbackHeadPosition= 0
        audioTrack?.play()

        recordCallback(true)
        recordThread = RecordThread()
        recordThread!!.callback = {
            stopRecording()
        }

        var time = 0
        var maxFrames = BUFFER_BYTES
        if (audioTrack!=null) {
            time = audioTrack!!.playbackHeadPosition % currentRecording!!.frameCount
            maxFrames = audioTrack!!.bufferSizeInFrames*2
        }

        Log.d("loop","created new recording with offset $time and size $maxFrames")

        recordThread!!.recording = AudioRecording(time, maxFrames)
        recordThread!!.recorder = recorder
        recordThread!!.start()
    }

    fun onStopRecordingButton() {
        if (recordThread==null) return
        recordCallback(false)
        // remove the callback so recording doesn't attempt to save twice
        recordThread!!.callback = null
        // set flag to tell thread to stop
        recordThread!!.isRecording=false
        // wait for last sample to finish
        // THIS IS A BUG CANT JOIN FROM SAME SPOT BUDDY
        recordThread!!.join()
        stopRecording()
    }

    private fun stopRecording() {
        // re mix the loop with the new recording
        currentRecording = mixRecordings(currentRecording,recordThread!!.recording!!)
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = currentRecording!!.createLoopingAudioTrack()
        audioTrack!!.play()

        recordCallback(false)
        recordThread = null
        Log.d("loop","Finished saving recording")
    }

    fun clearRecordings() {
        Log.d("looper","clearing recordings")
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        currentRecording = null
    }
}