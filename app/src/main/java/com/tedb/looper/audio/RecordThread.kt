package com.tedb.looper.audio

import android.media.AudioRecord
import android.util.Log

//const val RECORD_SHORT_SIZE = 40


class RecordThread : Thread() {
    var recording : AudioRecording? = null
    var recorder : AudioRecord? = null
    var isRecording : Boolean = false
    var callback : (()->Unit)? = null

    override fun run() {
        isRecording=true
        recorder!!.startRecording()
        val buffsize = recorder!!.bufferSizeInFrames
        var offset=0
        // while should be recording
        // and buffer can hold more
        // multiplied by 2 for short to byte conversion
        while (isRecording && (offset+buffsize ) < recording!!.buffer.size) {
            // write shorts
            recorder!!.read(
                recording!!.buffer,
                offset, buffsize,
                AudioRecord.READ_BLOCKING
            )
            offset += buffsize
        }

        recording!!.frameCount = offset
        recorder!!.stop()
        callback?.invoke()
    }
}