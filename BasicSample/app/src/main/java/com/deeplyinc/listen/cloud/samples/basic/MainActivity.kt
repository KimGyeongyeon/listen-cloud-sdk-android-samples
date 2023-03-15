package com.deeplyinc.listen.cloud.samples.basic

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.deeplyinc.listen.cloud.samples.basic.databinding.ActivityMainBinding
import com.deeplyinc.listen.cloud.sdk.Listen
import com.deeplyinc.listen.cloud.sdk.ListenEventListener
import com.deeplyinc.listen.cloud.sdk.models.ClassifierOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var audioRecord: AudioRecord
    private var isRecording = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        setContentView(binding.root)

        Listen.getInstance().init("YOUR SDK KEY HERE", "YOUR DEMO NAME HERE")
        Listen.getInstance().setAsyncInferenceListener(object : ListenEventListener {
            override fun onDetected(result: ClassifierOutput) {
                Log.d(TAG, "Asynchronous inference results - listener: $result")
            }
        })
        lifecycleScope.launch(Dispatchers.Default) {
            Listen.getInstance().getResultFlow().collect() {
                Log.d(TAG, "Asynchronous inference results - flow: $it")
            }
        }

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
        val bufferSize = Listen.getInstance().getInferenceParams().minInputSize
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            Listen.getInstance().getInferenceParams().sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
        )
        if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
            Log.w(TAG, "Failed to initialize AudioRecord")
            return
        }

        val buffer = ShortArray(bufferSize)

        audioRecord.startRecording()
        lifecycleScope.launch(Dispatchers.IO) {
            while (isRecording.value == true) {
                audioRecord.read(buffer, 0, buffer.size)

                val result = Listen.getInstance().inference(buffer)
                Log.d(TAG, "Synchronous inference result: $result")

                Listen.getInstance().inferenceAsync(buffer)
            }

            audioRecord.stop()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}