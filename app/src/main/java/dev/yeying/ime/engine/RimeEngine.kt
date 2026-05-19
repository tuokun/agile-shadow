package dev.yeying.ime.engine

import android.content.Context
import com.yuyan.inputmethod.core.Rime
import java.io.File

class RimeEngine private constructor() {

    companion object {
        val instance: RimeEngine by lazy { RimeEngine() }
    }

    var isInitialized = false
        private set

    fun startup(context: Context, fullCheck: Boolean = false) {
        val sharedDir = copyAssetsToShared(context)
        val userDir = File(context.filesDir, "rime/user").apply { mkdirs() }.absolutePath

        Rime.startupRime(context, sharedDir, userDir, fullCheck)
        Rime.setRimePageSize(5)
        isInitialized = true
    }

    fun shutdown() {
        Rime.exitRime()
        isInitialized = false
    }

    fun processKey(keycode: Int, mask: Int = 0): Boolean =
        Rime.processRimeKey(keycode, mask)

    fun replaceKey(caretPos: Int, length: Int, key: String): Boolean =
        Rime.replaceRimeKey(caretPos, length, key)

    fun clearComposition() = Rime.clearRimeComposition()

    fun getContext() = Rime.getRimeContext()

    fun getCommit() = Rime.getRimeCommit()

    fun getStatus() = Rime.getRimeStatus()

    fun selectCandidate(index: Int): Boolean =
        Rime.selectRimeCandidate(index)

    fun selectSchema(schemaId: String): Boolean =
        Rime.selectRimeSchema(schemaId)

    fun getCurrentSchema(): String =
        Rime.getCurrentRimeSchema()

    fun setOption(option: String, value: Boolean) =
        Rime.setRimeOption(option, value)

    private fun copyAssetsToShared(context: Context): String {
        val sharedDir = File(context.filesDir, "rime/shared")
        val versionFile = File(sharedDir, "rime_version.txt")
        val currentVersion = readAssetVersion(context)

        if (!sharedDir.exists() || readVersion(versionFile) != currentVersion) {
            copyAssetDir(context, "rime", sharedDir)
            versionFile.writeText(currentVersion.toString())
        }
        return sharedDir.absolutePath
    }

    private fun readAssetVersion(context: Context): Int {
        return context.assets.open("rime/rime_version.txt").bufferedReader().use {
            it.readText().trim().toIntOrNull() ?: 1
        }
    }

    private fun readVersion(file: File): Int {
        return if (file.exists()) file.readText().trim().toIntOrNull() ?: 0 else 0
    }

    private fun copyAssetDir(context: Context, assetPath: String, dest: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetPath) ?: return
        dest.mkdirs()
        for (file in files) {
            val src = "$assetPath/$file"
            val dst = File(dest, file)
            if (assetManager.list(src)?.isNotEmpty() == true) {
                copyAssetDir(context, src, dst)
            } else {
                assetManager.open(src).use { input ->
                    dst.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}
