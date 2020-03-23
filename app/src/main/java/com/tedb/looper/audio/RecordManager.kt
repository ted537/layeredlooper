package com.tedb.looper.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.nio.ByteOrder

enum class RecordState {
    NOT_RECORDING,
    RECORDING_FIRST,
    RECORDING_LAYER
}

class RecordManager {
    // external connections
    private val recordCallback : (isRecording:Boolean) -> Unit

    // audio data
    private var currentRecording : AudioRecording? = null
    var playbackThread : PlaybackThread? = null

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

    fun stopPlayback() {
        playbackThread?.loop=false
    }

    fun getRecordState() : RecordState {
        if (recordThread!=null) {
            if (currentRecording==null) return RecordState.RECORDING_FIRST
            else return RecordState.RECORDING_LAYER
        }
        else return RecordState.NOT_RECORDING
    }

    fun startRecording() {
        if (recordThread!=null) {
            Log.d("loop","ALREADY RECORDING")
            return
        }

        playbackThread?.release()
        playbackThread = currentRecording?.createLoopingAudioTrack()
        playbackThread?.start()

        recordThread = RecordThread()
        recordCallback(true)
        recordThread!!.callback = {
            stopRecording()
        }

        var time = 0
        var maxFrames = BUFFER_BYTES
        if (playbackThread!=null) {
            val audioTrack = playbackThread!!.audioTrack
            time = audioTrack!!.playbackHeadPosition % currentRecording!!.frameCount
            maxFrames = playbackThread!!.buffer!!.size
        }

        Log.d("loop","created new recording with offset $time and size $maxFrames")

        recordThread!!.recording =
            AudioRecording(time, maxFrames)
        recordThread!!.recorder = recorder
        recordThread!!.start()
    }

    // call this when coming from UI
    fun onStopRecordingButton() {
        // if not recording do nothing
        if (recordThread==null) return
        // if have at least one layer do nothing
        // this prevents layers from getting out of sync
        if (currentRecording!=null) return

        recordCallback(false)
        syncRecordThread()
        stopRecording()
    }

    private fun syncRecordThread() {
        // remove the callback so recording doesn't attempt to save twice
        recordThread?.callback = null
        // set flag to tell thread to stop
        recordThread?.isRecording=false
        // wait for last sample to finish
        recordThread?.join()
    }

    // call this when coming from thread
    private fun stopRecording() {
        // re mix the loop with the new recording
        currentRecording = mixRecordings(
            currentRecording,
            recordThread!!.recording!!
        )
        playbackThread?.release()
        playbackThread = currentRecording!!.createLoopingAudioTrack()
        playbackThread!!.start()

        recordThread = null
        recordCallback(false)
        Log.d("loop","Finished saving recording")
    }

    fun clearRecordings() {
        Log.d("looper","clearing recordings")
        syncRecordThread()
        recordThread = null
        playbackThread?.release()
        playbackThread=null
        currentRecording = null
        recordCallback(false)
    }

    fun saveToFile(path:String) : File {
        val file = File(path)
        saveWavFile(file,currentRecording!!.buffer)
        return file
    }

}