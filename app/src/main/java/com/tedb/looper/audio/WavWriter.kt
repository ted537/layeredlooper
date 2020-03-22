package com.tedb.looper.audio

import java.io.File
import java.io.OutputStream
import java.util.stream.Stream

fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

fun leWriteInt(stream: OutputStream,num:Int) {
    stream.write(num shr 24)
    stream.write(num shr 16)
    stream.write(num shr 8)
    stream.write(num)
}

fun leWriteShort(stream: OutputStream,num:Int) {
    stream.write(num shr 8)
    stream.write(num)
}

fun saveWavFile(file: File, buffer:ShortArray) {
    val subChunk1Size = 16
    val subChunk2Size = buffer.size*2
    val chunkSize = 4 + (8+subChunk1Size) + (8+subChunk2Size)
    val stream = file.outputStream().buffered()
    // write RIFF
    stream.write(
        byteArrayOfInts(0x52,0x49,0x46,0x46)
    )
    // write chunk size
    leWriteInt(stream,chunkSize)
    // write WAVE
    stream.write(
        byteArrayOfInts(0x57,0x41,0x56,0x45)
    )
    // write fmt
    stream.write(
        byteArrayOfInts(0x66,0x6d,0x74,0x20)
    )
    // write subchunk1 size
    leWriteInt(stream,subChunk1Size)
    // write audio format (PCM)
    leWriteShort(stream,1)
    // write num channels
    leWriteShort(stream,1)
    // write sample rate
    leWriteInt(stream, SAMPLE_RATE)
    // write byte rate
    leWriteInt(stream, SAMPLE_RATE*2)
    // bytes per frame
    leWriteShort(stream,2)
    // write data
    stream.write(
        byteArrayOfInts(0x64,0x61,0x74,0x61)
    )
    // write subchunk2 size
    leWriteInt(stream,subChunk2Size)
    for (sample in buffer) {
        leWriteShort(stream,sample.toInt())
    }
    stream.flush()
    stream.close()
}