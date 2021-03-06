package com.tedb.looper.audio

import android.util.Log

// assumes both recordings are same length
fun mixRecordings(currentRecording: AudioRecording?, newRecording: AudioRecording) : AudioRecording {
    if (currentRecording==null) {
        val clipped = AudioRecording(0,newRecording.frameCount)
        clipped.frameCount = newRecording.frameCount
        for (i in 0 until newRecording.frameCount) {
            clipped.buffer[i] = newRecording.buffer[i]
        }
        return clipped
    }
    val mixRecording = AudioRecording(
        currentRecording.offset,
        currentRecording.frameCount
    )
    mixRecording.frameCount = currentRecording.frameCount
    mixRecording.weight = currentRecording.weight + 1
    // mixing stage
    var maxSample = 1
    Log.d("record","current=${currentRecording.frameCount} new=${newRecording.frameCount}")
    for (i in 0 until currentRecording.frameCount) {
        val currentSample = currentRecording.buffer[i]
        //val newIndex = (i-newRecording.offset+newRecording.frameCount)%newRecording.frameCount
        val newIndex = i
        val newSample = newRecording.buffer[newIndex]
        val totalWeight = currentRecording.weight + newRecording.weight
        val mixSample = (currentSample+newSample)/2
        if (mixSample>maxSample) maxSample = mixSample
        mixRecording.buffer[i] = mixSample.toShort()
    }
    // normalizing stage
    val multFactor = Short.MAX_VALUE/maxSample
    // only scale down
    if (multFactor<1) {
        for (i in 0 until mixRecording.frameCount) {
            mixRecording.buffer[i] = (mixRecording.buffer[i] * multFactor).toShort()
        }
    }
    return mixRecording
}