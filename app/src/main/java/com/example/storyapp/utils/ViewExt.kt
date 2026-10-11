package com.example.storyapp.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.applySystemBarsPadding(topInsetTarget: View = this) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        val topPadding = if (topInsetTarget == view) bars.top else 0
        view.setPadding(bars.left, topPadding, bars.right, bars.bottom)
        if (topInsetTarget != view) topInsetTarget.updatePadding(top = bars.top)
        WindowInsetsCompat.CONSUMED
    }
}
