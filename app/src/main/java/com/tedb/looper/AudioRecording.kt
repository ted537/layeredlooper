package com.tedb.looper

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

class AudioRecording {
    val startTime : Int
    var endTime : Int = -1
    val buffer : ShortArray

    constructor(startTime : Int, bufferSize : Int) {
        this.startTime = startTime
        buffer = ShortArray(bufferSize) {0}
    }

    fun startPlayback() {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(EncodingType)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            )
            .setBufferSizeInBytes(endTime*2)
            .build()

        audioTrack.setLoopPoints(startTime,endTime,-1)

        audioTrack.write(buffer,0,endTime)
        audioTrack.play()

    }
}