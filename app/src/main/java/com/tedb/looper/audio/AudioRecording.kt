package com.tedb.looper.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.*
import android.util.Log
import com.tedb.looper.audio.EncodingType
import com.tedb.looper.audio.SAMPLE_RATE
import java.util.Collections.max
import kotlin.concurrent.thread

class AudioRecording {
    var offset : Int = 0;
    var frameCount : Int = -1
    val buffer : ShortArray
    var weight : Int = 1

    constructor(offset : Int, bufferSize : Int) {
        buffer = ShortArray(bufferSize) {0}
        this.offset = offset
    }

    fun createLoopingAudioTrack() : PlaybackThread {
        val trackFrameCount = max(listOf(frameCount,1))

        val bufferSize = getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            EncodingType
        )
        val audioTrack = Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setTransferMode(MODE_STREAM)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(EncodingType)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()
        Log.d("loop","built audio track")

        val playbackThread = PlaybackThread()
        playbackThread.audioTrack = audioTrack
        playbackThread.buffer = buffer

        return playbackThread
    }
}