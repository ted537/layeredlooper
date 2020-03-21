package com.tedb.looper.audio

// assumes both recordings are same length
fun mixRecordings(currentRecording: AudioRecording?, newRecording: AudioRecording) : AudioRecording {
    if (currentRecording==null) return  newRecording
    val mixRecording = AudioRecording(
        currentRecording.offset,
        currentRecording.buffer.size
    )
    mixRecording.frameCount = currentRecording.frameCount
    mixRecording.weight = currentRecording.weight + 1
    for (i in 0 until currentRecording.frameCount) {
        val currentSample = currentRecording.buffer[i]
        //val newIndex = (i-newRecording.offset+newRecording.frameCount)%newRecording.frameCount
        val newIndex = i
        val newSample = newRecording.buffer[newIndex]
        val totalWeight = currentRecording.weight + newRecording.weight
        val mixSample =
            (currentSample*currentRecording.weight+newSample*newRecording.weight)/totalWeight
        mixRecording.buffer[i] = mixSample.toShort()
    }
    return mixRecording
}