package com.deeplyinc.listen.cloud.samples.basic

import android.media.AudioFormat

data class Configuration(
    val bufferSize: Int = 48000,
    val sampleRate: Int = 16000,
    val channelMode: Int = AudioFormat.CHANNEL_IN_MONO,
    val encoding: Int = AudioFormat.ENCODING_PCM_16BIT,
)