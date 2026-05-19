package dev.yeying.ime.engine

import android.content.Context
import com.yuyan.inputmethod.core.HandWriting

class HandwritingEngine {
    var isInitialized = false
        private set

    fun init(context: Context): Boolean {
        if (isInitialized) return true
        val result = HandWriting.initWithDirectory(
            context,
            context.getExternalFilesDir("hw").toString()
        )
        isInitialized = result
        return result
    }

    fun recognize(points: IntArray): List<List<String>> {
        HandWriting.inputHWPoints(points)
        val raw = HandWriting.getCandidates()
        return raw?.mapNotNull { it?.filterNotNull() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    fun reset() = HandWriting.reset()
    fun release() = HandWriting.release()
}
