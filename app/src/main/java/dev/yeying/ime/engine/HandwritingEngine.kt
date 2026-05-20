package dev.yeying.ime.engine

import android.content.Context
import android.util.Log
import com.yuyan.inputmethod.core.HandWriting
import java.io.File
import java.io.FileOutputStream

class HandwritingEngine {
    var isInitialized = false
        private set

    private val strokePoints = mutableListOf<Int>()
    private val TAG = "HandwritingEngine"

    fun init(context: Context): Boolean {
        if (isInitialized) return true
        val hwDir = context.getExternalFilesDir("hw")!!.absolutePath
        copyAssets(context, "hw", hwDir)
        Log.d(TAG, "hwDir=$hwDir, files=${File(hwDir).list()?.joinToString()}")
        val result = HandWriting.initWithDirectory(context, hwDir)
        Log.d(TAG, "initWithDirectory result=$result")
        if (result) {
            HandWriting.reloadConfig()
            HandWriting.activeMode(5)
        }
        isInitialized = result
        return result
    }

    fun addPoint(x: Int, y: Int) {
        strokePoints.add(x)
        strokePoints.add(y)
    }

    fun finishStroke() {
        strokePoints.add(-1)
        strokePoints.add(0)
    }

    fun recognize(): List<String> {
        if (strokePoints.isEmpty()) return emptyList()
        Log.d(TAG, "recognize: pointsCount=${strokePoints.size}")
        HandWriting.reset()
        HandWriting.inputHWPoints(strokePoints.toIntArray())
        val raw = HandWriting.getCandidates()
        Log.d(TAG, "recognize: raw=${raw?.map { it?.toList() }}")
        return raw?.firstOrNull()?.filterNotNull() ?: emptyList()
    }

    fun clear() {
        strokePoints.clear()
        HandWriting.reset()
    }

    fun release() = HandWriting.release()

    private fun copyAssets(context: Context, assetPath: String, destPath: String) {
        val assets = context.assets.list(assetPath) ?: return
        if (assets.isEmpty()) {
            copyFile(context, assetPath, destPath)
        } else {
            val dir = File(destPath)
            if (!dir.exists()) dir.mkdirs()
            for (asset in assets) {
                copyAssets(context, "$assetPath/$asset", "$destPath/$asset")
            }
        }
    }

    private fun copyFile(context: Context, assetPath: String, destPath: String) {
        val file = File(destPath)
        if (file.exists()) return
        file.parentFile?.mkdirs()
        context.assets.open(assetPath).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }
}
