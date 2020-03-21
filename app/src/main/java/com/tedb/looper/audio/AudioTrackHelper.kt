package com.tedb.looper.audio

// assumes both recordings are same length
fun mixRecordings(currentRecording: AudioRecording?, newRecording: AudioRecording) : AudioRecording {
    if (currentRecording==null) return  newRecording
    val mixRecording = AudioRecording(
        currentRecording.offset,
        currentRecording.buffer.size
    )
    mixRecording.frameCount = currentRecording.frameCount
    for (i in 0 until currentRecording.frameCount) {
        val currentSample = currentRecording.buffer[i]
        //val newIndex = (i-newRecording.offset+newRecording.frameCount)%newRecording.frameCount
        val newIndex = i
        val newSample = newRecording.buffer[newIndex]
        val mixSample = currentSample+newSample
        mixRecording.buffer[i] = mixSample.toShort()
    }
    return mixRecording
}