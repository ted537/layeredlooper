package com.tedb.looper

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.*
import android.util.Log

class AudioRecording {
    var offset = 0;
    var frameCount : Int = -1
    val buffer : ShortArray

    constructor(offset : Int, bufferSize : Int) {
        buffer = ShortArray(bufferSize) {0}
        this.offset = offset
    }

    fun createLoopingAudioTrack() : AudioTrack {
        val audioTrack = Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setTransferMode(MODE_STATIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(EncodingType)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(frameCount*2)
            .build()
        Log.d("loop","built audio track")

        audioTrack.setLoopPoints(0,frameCount,-1)
        Log.d("loop","set loop points")

        audioTrack.write(buffer,0,frameCount)
        Log.d("loop","wrote to buffer")
        return audioTrack
    }
}