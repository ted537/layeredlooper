package com.tedb.looper

import android.media.AudioFormat

const val SAMPLE_RATE = 44100
const val MAX_SECONDS = 100
// the times two is because 16 bit is 2 bytes per element
const val BUFFER_BYTES = SAMPLE_RATE*2* MAX_SECONDS

const val EncodingType = AudioFormat.ENCODING_PCM_16BIT