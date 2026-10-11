package com.example.storyapp.ui.register

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.storyapp.R
import com.example.storyapp.data.ResultState
import com.example.storyapp.databinding.ActivityRegisterBinding
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.utils.applySystemBarsPadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private var imageAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.btnRegister.setOnClickListener { register() }
        binding.btnToLogin.setOnClickListener { finish() }
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    showLoading(false)
                    showSuccessDialog()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    showErrorDialog(result.message ?: getString(R.string.error_connection))
                }
            }
        }
        playAnimation()
    }

    override fun onDestroy() {
        imageAnimator?.cancel()
        super.onDestroy()
    }

    private fun register() {
        val name = binding.edRegisterName.text.toString().trim()
        if (name.isEmpty()) {
            binding.edRegisterName.error = getString(R.string.error_empty_name)
            return
        }
        if (!binding.edRegisterEmail.isValid() || !binding.edRegisterPassword.isValid()) {
            Toast.makeText(this, R.string.error_invalid_form, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.register(
            name = name,
            email = binding.edRegisterEmail.text.toString().trim(),
            password = binding.edRegisterPassword.text.toString()
        )
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.register_success_title)
            .setMessage(R.string.register_success_message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .show()
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_failed_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnRegister.isEnabled = !isLoading
    }

    private fun playAnimation() {
        imageAnimator = ObjectAnimator.ofFloat(binding.ivRegisterImage, View.TRANSLATION_X, -30f, 30f).apply {
            duration = IMAGE_ANIMATION_DURATION
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }

        val fadeInAnimators = listOf(
            binding.tvRegisterTitle,
            binding.tvRegisterMessage,
            binding.edRegisterName,
            binding.edRegisterEmail,
            binding.edRegisterPassword,
            binding.btnRegister,
            binding.btnToLogin
        ).map<View, Animator> { view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 1f).setDuration(FADE_DURATION)
        }
        AnimatorSet().apply {
            playSequentially(fadeInAnimators)
            start()
        }
    }

    companion object {
        private const val IMAGE_ANIMATION_DURATION = 3000L
        private const val FADE_DURATION = 300L
    }
}
