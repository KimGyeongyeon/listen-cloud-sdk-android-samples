# Audio Recording

To use the Listen sound event detection feature, we need to record audio with `AudioRecord` from an Android device. 


## Permission Request

Before implementing the recording function, you must first implement the function that asks the user for permission to record as follows:

```kotlin
class MainActivity : AppCompatActivity() {

    // for RECORD_AUDIO permission request
    private val requestRecordPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Audio recording permission is granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...

        Listen.getInstance().init("SDK KEY", "MODEL NAME")

        // Request RECORD_AUDIO permission
        requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
    }
}
```


## AudioRecord

### Recording

You can use `AudioRecord` or `MediaRecorder` to implement the recording function on Android. 
For Listen integration, `AudioRecord` is more suitable for real-time processing of raw audio data. 
For more information on `AudioRecord`, visit [Official Document](https://developer.android.com/reference/android/media/AudioRecord).

```kotlin
class MainActivity : AppCompatActivity() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // start recording if permission is granted
            startRecording()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...

        Listen.getInstance().init("SDK KEY", "MODEL NAME")

        requestPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        val sampleRate = Listen.getInstance().getInferenceParams().sampleRate
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            sampleRate // bufferSizeInBytes
        )
    }
}
```

[Sample Apps](https://github.com/deeplyinc/listen-cloud-sdk-android-samples) for examples of actual implementations. 

The recording function implementation is complete. 
Now move on to the Sound Event Analysis document to use the Sound Analysis feature in Listen. 

