package com.deeplyinc.listen.cloud.samples.basic

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.deeplyinc.listen.cloud.samples.basic.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var audioRecord: AudioRecord
    private var isRecording = MutableLiveData(false)
    private val config = Configuration(
        bufferSize = 48000,
        sampleRate = 16000,
        channelMode = AudioFormat.CHANNEL_IN_MONO,
        encoding = AudioFormat.ENCODING_PCM_16BIT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        setContentView(binding.root)
        requestRecordingPermission()
    }

    private fun initView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.isRecording = isRecording

        binding.buttonRecord.setOnClickListener {
            isRecording.value = true
            startRecording()
        }
        binding.buttonStop.setOnClickListener {
            isRecording.value = false
        }
    }

    private fun requestRecordingPermission() {
        val permissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d(TAG, "Recording permission is granted")
                }
            }
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            config.sampleRate,
            config.channelMode,
            config.encoding,
            config.bufferSize
        )
        if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
            Log.w(TAG, "Failed to initialize AudioRecord")
            return
        }

        val buffer = ShortArray(config.bufferSize)
        audioRecord.startRecording()
        lifecycleScope.launch(Dispatchers.Default) {
            while (isRecording.value == true) {
                audioRecord.read(buffer, 0, buffer.size)
                doAnythingWith(buffer)
            }
            audioRecord.stop()
        }
    }

    private fun doAnythingWith(buffer: ShortArray) {
        /* Use recorded data here */
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}