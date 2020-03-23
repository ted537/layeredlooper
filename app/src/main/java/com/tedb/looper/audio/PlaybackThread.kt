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
        audioTrack!!.setLoopPoints(0, buffer!!.size, -1)
        while (loop) audioTrack!!.write(buffer!!, 0, buffer!!.size)
    }

    fun release() {
        thread(start=true) {
            loop=false
            this.join()
            audioTrack?.release()
        }
    }
}