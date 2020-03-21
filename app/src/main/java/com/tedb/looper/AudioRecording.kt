package com.tedb.looper

class AudioRecording {
    val startTime : Int
    val endTime : Int = -1
    val buffer : ShortArray

    constructor(startTime : Int, bufferSize : Int) {
        this.startTime = startTime
        buffer = ShortArray(bufferSize) {0}
    }
}