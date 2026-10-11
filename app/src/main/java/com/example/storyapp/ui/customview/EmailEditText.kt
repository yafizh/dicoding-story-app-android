package com.example.storyapp.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import com.example.storyapp.R

class EmailEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        doOnTextChanged { text, _, _, _ ->
            error = if (!text.isNullOrEmpty() && !isValidEmail(text)) {
                context.getString(R.string.error_invalid_email)
            } else {
                null
            }
        }
    }

    fun isValid(): Boolean = isValidEmail(text?.toString().orEmpty().trim())

    private fun isValidEmail(email: CharSequence): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
