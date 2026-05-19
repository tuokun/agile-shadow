package com.yuyan.inputmethod.core

import android.content.Context
import androidx.annotation.Keep

object HandWriting {

    init {
        System.loadLibrary("handwriting")
    }

    @Keep
    external fun getPackageName(): String?

    external fun activeMode(mode: Int): Boolean

    external fun getCandidates(): Array<Array<String?>?>

    external fun initWithDirectory(context: Context, str: String?): Boolean

    external fun inputHWPoints(iArr: IntArray?): Boolean

    external fun release()

    external fun reloadConfig(): Boolean

    external fun reset(): Boolean
}
