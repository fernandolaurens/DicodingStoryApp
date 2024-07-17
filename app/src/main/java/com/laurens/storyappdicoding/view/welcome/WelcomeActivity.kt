package com.laurens.storyappdicoding.view.welcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.laurens.storyappdicoding.databinding.ActivityWelcomeBinding
import com.laurens.storyappdicoding.view.ModelFacotry.ViewModelFactory
import com.laurens.storyappdicoding.view.login.LoginActivity
import com.laurens.storyappdicoding.view.main.MainActivity
import com.laurens.storyappdicoding.view.signup.SignupActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityWelcomeBinding
    private val welcomeViewModel by viewModels<WelcomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWelcomeBinding.inflate(layoutInflater)
        welcomeViewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        setContentView(viewBinding.root)
        configureUI()
        setOnClickListeners()
        runWelcomeAnimations()

    }

    private fun configureUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setOnClickListeners() {
        viewBinding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        viewBinding.signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun runWelcomeAnimations() {
        // Animasi translasi dan rotasi pada gambar
        ObjectAnimator.ofFloat(viewBinding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        ObjectAnimator.ofFloat(viewBinding.imageView, View.ROTATION, 0f, 360f).apply {
            duration = 5000
            repeatCount = ObjectAnimator.INFINITE
        }.start()

        // Animasi untuk elemen UI lainnya
        with(viewBinding) {
            val login = ObjectAnimator.ofFloat(loginButton, View.ALPHA, 0f, 1f).setDuration(1000)
            val signup = ObjectAnimator.ofFloat(signupButton, View.ALPHA, 0f, 1f).setDuration(1000)
            val title = ObjectAnimator.ofFloat(titleTextView, View.ALPHA, 0f, 1f).setDuration(1000)
            val desc = ObjectAnimator.ofFloat(descTextView, View.ALPHA, 0f, 1f).setDuration(1000)

            val together = AnimatorSet().apply {
                playTogether(login, signup)
            }

            AnimatorSet().apply {
                playSequentially(title, desc, together)
                startDelay = 2000 // Tunda animasi selama 2 detik
                start()
            }
        }
    }
}