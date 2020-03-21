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
        var i=0
        // while should be recording
        // and buffer can hold more
        while (isRecording && i* RECORD_SHORT_SIZE+1 < recording!!.buffer.size) {
            // write shorts
            recorder!!.read(
                recording!!.buffer,
                i* RECORD_SHORT_SIZE, RECORD_SHORT_SIZE,
                AudioRecord.READ_BLOCKING
            )
            ++i
        }
        recorder!!.stop()
        Log.d("record","Finished recording, wrote $i times");
    }
}