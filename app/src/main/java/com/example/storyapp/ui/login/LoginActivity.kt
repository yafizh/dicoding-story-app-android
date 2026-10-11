package com.example.storyapp.ui.login

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.storyapp.R
import com.example.storyapp.data.ResultState
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.main.MainActivity
import com.example.storyapp.ui.register.RegisterActivity
import com.example.storyapp.utils.applySystemBarsPadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private var imageAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.btnLogin.setOnClickListener { login() }
        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, getString(R.string.login_success, result.data), Toast.LENGTH_SHORT).show()
                    navigateToMain()
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

    private fun login() {
        if (!binding.edLoginEmail.isValid() || !binding.edLoginPassword.isValid()) {
            Toast.makeText(this, R.string.error_invalid_form, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.login(
            email = binding.edLoginEmail.text.toString().trim(),
            password = binding.edLoginPassword.text.toString()
        )
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
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
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun playAnimation() {
        imageAnimator = ObjectAnimator.ofFloat(binding.ivLoginImage, View.TRANSLATION_X, -30f, 30f).apply {
            duration = IMAGE_ANIMATION_DURATION
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }

        val fadeInAnimators = listOf(
            binding.tvLoginTitle,
            binding.tvLoginMessage,
            binding.edLoginEmail,
            binding.edLoginPassword,
            binding.btnLogin,
            binding.btnToRegister
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
