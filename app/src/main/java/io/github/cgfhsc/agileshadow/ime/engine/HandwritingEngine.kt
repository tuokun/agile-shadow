package io.github.cgfhsc.agileshadow.ime.engine

import android.content.Context
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.google.mlkit.vision.digitalink.common.RecognitionResult
import kotlinx.coroutines.tasks.await

class HandwritingEngine private constructor() {
    var isInitialized = false
        private set

    private val TAG = "HandwritingEngine"
    private var recognizer: DigitalInkRecognizer? = null
    private var inkBuilder: Ink.Builder = Ink.builder()
    private var strokeBuilder: Ink.Stroke.Builder? = null

    companion object {
        private var instance: HandwritingEngine? = null

        fun getInstance(): HandwritingEngine =
            instance ?: HandwritingEngine().also { instance = it }
    }

    suspend fun init(context: Context): Boolean {
        if (isInitialized) return true

        return try {
            val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("zh-Hans-CN")
            if (modelIdentifier == null) {
                Log.e(TAG, "Failed to get Chinese model identifier")
                return false
            }

            val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()

            val remoteModelManager = RemoteModelManager.getInstance()
            val isDownloaded = remoteModelManager.isModelDownloaded(model).await()
            if (!isDownloaded) {
                Log.d(TAG, "Downloading Chinese handwriting model...")
                remoteModelManager.download(model, DownloadConditions.Builder().build()).await()
                Log.d(TAG, "Model downloaded")
            }

            recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build()
            )
            isInitialized = true
            Log.d(TAG, "ML Kit recognizer initialized")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init handwriting engine", e)
            false
        }
    }

    fun addPoint(x: Int, y: Int) {
        if (strokeBuilder == null) {
            strokeBuilder = Ink.Stroke.builder()
        }
        strokeBuilder?.addPoint(Ink.Point.create(x.toFloat(), y.toFloat(), System.currentTimeMillis()))
    }

    fun finishStroke() {
        strokeBuilder?.build()?.let { stroke ->
            inkBuilder.addStroke(stroke)
        }
        strokeBuilder = null
    }

    suspend fun recognize(): List<String> {
        if (!isInitialized || recognizer == null) {
            Log.w(TAG, "Recognizer not initialized")
            return emptyList()
        }

        val ink = inkBuilder.build()
        if (ink.getStrokes().isEmpty()) return emptyList()

        return try {
            val result: RecognitionResult = recognizer!!.recognize(ink).await()
            result.getCandidates().mapNotNull { it.getText() }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Recognition failed", e)
            emptyList()
        }
    }

    fun clear() {
        inkBuilder = Ink.builder()
        strokeBuilder = null
    }

    fun release() {
        recognizer?.close()
        recognizer = null
        isInitialized = false
    }
}
