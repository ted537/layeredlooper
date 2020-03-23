package com.tedb.looper.audio

import android.media.AudioTrack
import kotlin.concurrent.thread

class PlaybackThread : Thread() {
    var audioTrack : AudioTrack? = null
    var buffer : ShortArray? = null
    var loop : Boolean = true

    override fun run() {
        super.run()
        audioTrack!!.play()
        // duplicate buffer at end
        val bigbuffer = shortArrayOf(*buffer!!,*buffer!!)
        var offset = 0
        while (loop) {
            val audioBuffSize =  audioTrack!!.bufferSizeInFrames
            audioTrack!!.write(bigbuffer, offset, audioBuffSize)
            offset += audioBuffSize
            offset %= buffer!!.size
        }
    }

    fun release() {
        thread(start=true) {
            loop=false
            this.join()
            audioTrack?.release()
        }
    }
}