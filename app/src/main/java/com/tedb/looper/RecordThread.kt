package com.tedb.looper

import android.media.AudioRecord
import android.util.Log

const val RECORD_SHORT_SIZE = 400

class RecordThread : Thread() {
    var recording : AudioRecording? = null
    var recorder : AudioRecord? = null
    var isRecording : Boolean = false

    override fun run() {
        isRecording=true
        recorder!!.startRecording()
        var offset=0
        // while should be recording
        // and buffer can hold more
        while (isRecording && offset+ RECORD_SHORT_SIZE < recording!!.buffer.size) {
            // write shorts
            recorder!!.read(
                recording!!.buffer,
                offset, RECORD_SHORT_SIZE,
                AudioRecord.READ_BLOCKING
            )
            offset += RECORD_SHORT_SIZE
        }
        recording!!.endTime = offset
        recorder!!.stop()
        Log.d("record","Finished recording, wrote $offset values");
    }
}