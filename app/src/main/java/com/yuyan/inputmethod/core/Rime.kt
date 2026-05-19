package com.yuyan.inputmethod.core

import android.content.Context

object Rime {

    init {
        System.loadLibrary("yuyanime")
    }

    @JvmStatic
    external fun startupRime(context: Context, sharedDir: String, userDir: String, fullCheck: Boolean)

    @JvmStatic
    external fun exitRime()

    @JvmStatic
    external fun setRimePageSize(pageSize: Int)

    @JvmStatic
    external fun processRimeKey(keycode: Int, mask: Int): Boolean

    @JvmStatic
    external fun replaceRimeKey(caretPos: Int, length: Int, key: String?): Boolean

    @JvmStatic
    external fun clearRimeComposition()

    @JvmStatic
    external fun getRimeCommit(): RimeCommit?

    @JvmStatic
    external fun getRimeContext(): RimeContext?

    @JvmStatic
    external fun getRimeStatus(): RimeStatus?

    @JvmStatic
    external fun setRimeOption(option: String, value: Boolean)

    @JvmStatic
    external fun getCurrentRimeSchema(): String

    @JvmStatic
    external fun selectRimeSchema(schemaId: String): Boolean

    @JvmStatic
    external fun selectRimeCandidate(index: Int): Boolean

    @JvmStatic
    external fun getRimeKeycodeByName(name: String): Int
}
