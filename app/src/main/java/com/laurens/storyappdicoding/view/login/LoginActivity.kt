package com.laurens.storyappdicoding.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.laurens.storyappdicoding.R
import com.laurens.storyappdicoding.databinding.ActivityLoginBinding
import com.laurens.storyappdicoding.data.pref.Result
import com.laurens.storyappdicoding.view.ModelFacotry.ViewModelFactory
import com.laurens.storyappdicoding.view.auth.AuthViewModel
import com.laurens.storyappdicoding.view.main.MainActivity
import com.laurens.storyappdicoding.view.main.MainActivity.Companion.EXTRA_TOKEN


class LoginActivity : AppCompatActivity() {
    private lateinit var uniqueBinding: ActivityLoginBinding
    private lateinit var uniqueTitle: String
    private lateinit var uniqueMessage: String
    private lateinit var uniquePositiveButtonTitle: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uniqueBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(uniqueBinding.root)
        setupUniqueView()
        setupUniqueAction()
        playAnimation()

        uniqueBinding.passwordEditTextLayout.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setUniqueButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        uniqueBinding.emailEditTextLayout.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setUniqueButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {

            }

        })

        uniqueBinding.showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                uniqueBinding.passwordEditTextLayout.transformationMethod = SingleLineTransformationMethod.getInstance()
            } else {
                uniqueBinding.passwordEditTextLayout.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }

    private fun setUniqueButtonEnable() {
        val isUniqueEmailValid =
            uniqueBinding.emailEditTextLayout.text != null && uniqueBinding.emailEditTextLayout.text.toString()
                .isNotEmpty()
        val isUniquePasswordValid =
            uniqueBinding.passwordEditTextLayout.text != null && uniqueBinding.passwordEditTextLayout.text.toString()
                .isNotEmpty()
        uniqueBinding.loginButton.isEnabled = isUniqueEmailValid && isUniquePasswordValid
    }

    private fun setupUniqueView() {
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

    private fun setupUniqueAction() {
        val factory = ViewModelFactory.getInstance(this)
        val viewModel: AuthViewModel by viewModels { factory }
        val context = this@LoginActivity

        with(uniqueBinding) {
            loginButton.setOnClickListener {
                val email = emailEditTextLayout.text.toString()
                val password = passwordEditTextLayout.text.toString()
                viewModel.login(email, password).observe(this@LoginActivity) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                            }

                            is Result.Success -> {
                                showLoading(false)
                                val token = result.data.loginResult.token
                                viewModel.saveSession(token)
                                if (!result.data.error) {
                                    uniqueTitle = getString(R.string.yeah)
                                    uniqueMessage = getString(R.string.msg_login_s)
                                    uniquePositiveButtonTitle = getString(R.string.positive_btn)
                                    showCustomAlertDialog(
                                        context,
                                        uniqueTitle,
                                        uniqueMessage,
                                        uniquePositiveButtonTitle
                                    ) {
                                        val intent = Intent(context, MainActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        viewModel.saveSession(token)
                                        intent.putExtra(EXTRA_TOKEN, token)
                                        startActivity(intent)
                                        finish()
                                    }

                                } else {
                                    uniqueTitle = getString(R.string.no)
                                    uniqueMessage = getString(R.string.msg_login_e)
                                    uniquePositiveButtonTitle = getString(R.string.positive_btn_e)
                                    showCustomAlertDialog(
                                        context,
                                        uniqueTitle,
                                        uniqueMessage,
                                        uniquePositiveButtonTitle
                                    ) {}

                                }
                            }

                            is Result.Error -> {
                                showLoading(false)
                                title = getString(R.string.no)
                                uniqueMessage = getString(R.string.msg_login_e)
                                uniquePositiveButtonTitle = getString(R.string.positive_btn_e)
                                showCustomAlertDialog(
                                    context,
                                    uniqueTitle,
                                    uniqueMessage,
                                    uniquePositiveButtonTitle) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            uniqueBinding.progressBar.visibility = View.VISIBLE

        } else {
            uniqueBinding.progressBar.visibility = View.GONE
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(uniqueBinding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(uniqueBinding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(uniqueBinding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(uniqueBinding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(uniqueBinding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(uniqueBinding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(uniqueBinding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(uniqueBinding.loginButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                login
            )
            startDelay = 2
        }.start()
    }


    private fun showCustomAlertDialog(
        context: Context,
        uniqueTitle: String,
        uniqueMessage: String,
        uniquePositiveButtonTitle: String = "OK",
        positiveButtonAction: () -> Unit
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.apply {
            setTitle(uniqueTitle)
            setMessage(uniqueMessage)
            setPositiveButton(uniquePositiveButtonTitle) { dialog, _ ->
                positiveButtonAction.invoke()
                dialog.dismiss()
            }
            create()
            show()
        }
    }

}