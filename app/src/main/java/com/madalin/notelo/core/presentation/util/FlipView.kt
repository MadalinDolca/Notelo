package com.madalin.notelo.core.presentation.util

import android.util.Log
import android.view.View
import android.widget.ViewFlipper

/**
 * Flips this [ViewFlipper] to the given [view].
 */
fun ViewFlipper.flipTo(view: View) {
    try {
        for (i in 0 until this.childCount) {
            if (this.getChildAt(i) == view) {
                this.displayedChild = i
                return
            }
        }
    } catch (e: Exception) {
        this.displayedChild = 0
        Log.e("flipView", "flipView: ${e.message}")
    }
}