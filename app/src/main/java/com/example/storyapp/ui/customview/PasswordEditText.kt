package com.example.storyapp.ui.customview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import com.example.storyapp.R

class PasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        doOnTextChanged { text, _, _, _ ->
            error = if (!text.isNullOrEmpty() && text.length < MIN_LENGTH) {
                context.getString(R.string.error_password_length)
            } else {
                null
            }
        }
    }

    fun isValid(): Boolean = (text?.length ?: 0) >= MIN_LENGTH

    companion object {
        private const val MIN_LENGTH = 8
    }
}
