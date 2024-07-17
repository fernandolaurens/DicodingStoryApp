package com.laurens.storyappdicoding.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.CheckBox
import com.laurens.storyappdicoding.data.pref.Result
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.laurens.storyappdicoding.R
import com.laurens.storyappdicoding.databinding.ActivitySignupBinding
import com.laurens.storyappdicoding.view.ModelFacotry.ViewModelFactory
import com.laurens.storyappdicoding.view.auth.AuthViewModel

class SignupActivity : AppCompatActivity() {
    private lateinit var signUpBinding: ActivitySignupBinding
    private lateinit var dialogTitle: String
    private lateinit var dialogMessage: String
    private lateinit var positiveButtonText: String
    private lateinit var showPasswordCheckBox: CheckBox



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding  = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(signUpBinding.root)
        setupCustomView()
        setupCustomAction()
        playAnimation()


        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox)
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                signUpBinding.passwordEditTextLayout.transformationMethod = null
            } else {
                // Hide password
                signUpBinding.passwordEditTextLayout.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        signUpBinding.nameEditTextLayout.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setCustomButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        signUpBinding.passwordEditTextLayout.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setCustomButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        signUpBinding.emailEditTextLayout.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setCustomButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun setupCustomAction() {

        val factory = ViewModelFactory.getInstance(this)
        val viewModel: AuthViewModel by viewModels { factory }
        val context = this@SignupActivity

        with(signUpBinding) {
            signupButton.setOnClickListener {
                val name = nameEditTextLayout.text.toString()
                val email = emailEditTextLayout.text.toString()
                val password = passwordEditTextLayout.text.toString()
                viewModel.signup(name, email, password).observe(this@SignupActivity) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showCustomLoading(true)
                            }

                            is Result.Success -> {
                                showCustomLoading(false)
                                if (!result.data.error) {
                                    dialogTitle = getString(R.string.yeah)
                                    dialogMessage = getString(R.string.msg_signup_s)
                                    positiveButtonText = getString(R.string.positive_btn)
                                    showCustomAlertDialog(context, dialogTitle, dialogMessage, positiveButtonText) {
                                        val intent = Intent(context, Log::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    dialogTitle = getString(R.string.no)
                                    dialogMessage = getString(R.string.msg_signup_e)
                                    positiveButtonText = getString(R.string.positive_btn_e)
                                    showCustomAlertDialog(context, dialogTitle, dialogMessage, positiveButtonText) {}
                                }
                            }

                            is Result.Error -> {
                                showCustomLoading(false)
                                dialogTitle = getString(R.string.no)
                                dialogMessage = getString(R.string.msg_signup_e)
                                positiveButtonText = getString(R.string.positive_btn_e)
                                showCustomAlertDialog(context, dialogTitle, dialogMessage, positiveButtonText) {}
                            }

                        }
                    }
                }
            }
        }
    }


    private fun showCustomAlertDialog(
        context: Context,
        dialogTitle: String,
        dialogMessage: String,
        positiveButtonText: String = "OK",
        positiveButtonAction: () -> Unit
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.apply {
            setTitle(dialogTitle)
            setMessage(dialogMessage)
            setPositiveButton(positiveButtonText) { dialog, _ ->
                positiveButtonAction.invoke()
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun showCustomLoading(isLoading: Boolean) {
        if (isLoading) {
            signUpBinding.progressBar.visibility = View.VISIBLE

        } else {
            signUpBinding.progressBar.visibility = View.GONE
        }
    }

    private fun setCustomButtonEnable() {
        val isNameValid =
            signUpBinding.nameEditTextLayout.text != null && signUpBinding.nameEditTextLayout.text.toString().isNotEmpty()
        val isEmailValid =
            signUpBinding.emailEditTextLayout.text != null && signUpBinding.emailEditTextLayout.text.toString().isNotEmpty()
        val isPasswordValid =
            signUpBinding.passwordEditTextLayout.text != null && signUpBinding.passwordEditTextLayout.text.toString()
                .isNotEmpty()
        signUpBinding.signupButton.isEnabled = isNameValid && isEmailValid && isPasswordValid
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(signUpBinding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(signUpBinding.titleTextView, View.ALPHA, 1f).setDuration(50)
        val nameTextView = ObjectAnimator.ofFloat(signUpBinding.nameTextView, View.ALPHA, 1f).setDuration(50)
        val nameEditTextLayout = ObjectAnimator.ofFloat(signUpBinding.nameEditTextLayout, View.ALPHA, 1f).setDuration(50)
        val emailTextView = ObjectAnimator.ofFloat(signUpBinding.emailTextView, View.ALPHA, 1f).setDuration(50)
        val emailEditTextLayout = ObjectAnimator.ofFloat(signUpBinding.emailEditTextLayout, View.ALPHA, 1f).setDuration(50)
        val passwordTextView = ObjectAnimator.ofFloat(signUpBinding.passwordTextView, View.ALPHA, 1f).setDuration(50)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(signUpBinding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(50)
        val signup = ObjectAnimator.ofFloat(signUpBinding.signupButton, View.ALPHA, 1f).setDuration(50)


        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                signup
            )
            startDelay = 50
        }.start()
    }


    private fun setupCustomView() {
        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}