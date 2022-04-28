package com.example.dicodingstoryapp.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ActivityLoginBinding
import com.example.dicodingstoryapp.ui.register.RegisterActivity
import com.example.dicodingstoryapp.ui.story.StoryListActivity
import com.example.dicodingstoryapp.utils.ApiCallbackString
import com.example.dicodingstoryapp.utils.validateEmail

class LoginActivity : AppCompatActivity() {
    private lateinit var loginBinding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        setFullscreen()
        playAnimation()
        showPassword()
        enableButton()
        setupListener()
    }

    private fun setFullscreen() {
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

    private fun showPassword() {
        loginBinding.apply {
            passVisibility.setOnClickListener {
                if (etPassword.transformationMethod.equals(SingleLineTransformationMethod.getInstance())
                ) {
                    passVisibility.setImageResource(R.drawable.ic_visible)
                    etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    passVisibility.setImageResource(R.drawable.ic_visible_off)
                    etPassword.transformationMethod = SingleLineTransformationMethod.getInstance()
                }
                etPassword.setSelection(etPassword.text!!.length)
            }
        }
    }

    private fun enableButton() {
        val editTexts = listOf(loginBinding.etEmail, loginBinding.etPassword)
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val etEmail = loginBinding.etEmail.text.toString().trim()
                    val etPassword = loginBinding.etPassword.text.toString().trim()

                    loginBinding.btnLogin.isEnabled = etEmail.isNotEmpty()
                            && etPassword.isNotEmpty()
                            && !validateEmail(etEmail)
                            && etPassword.length > 5

                    if (loginBinding.btnLogin.isEnabled) {
                        loginBinding.btnLogin.text = getString(R.string.login)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
        }

        setupLogin()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(loginBinding.ivLogo, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val translate =
            ObjectAnimator.ofFloat(loginBinding.civTranslate, View.ALPHA, 1f).setDuration(500)
        ObjectAnimator.ofFloat(loginBinding.civTranslate, View.TRANSLATION_X, -100f, 0f).apply {
            duration = 2000
        }.start()

        val email = ObjectAnimator.ofFloat(loginBinding.etEmail, View.ALPHA, 1f).setDuration(500)
        val password =
            ObjectAnimator.ofFloat(loginBinding.etPassword, View.ALPHA, 1f).setDuration(500)
        val visibility =
            ObjectAnimator.ofFloat(loginBinding.passVisibility, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(loginBinding.btnLogin, View.ALPHA, 1f).setDuration(500)
        val register =
            ObjectAnimator.ofFloat(loginBinding.tvRegister, View.ALPHA, 1f).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(email, password, visibility)
        }

        AnimatorSet().apply {
            playSequentially(translate, together, login, register)
            startDelay = 500
        }.start()
    }

    private fun setupListener() {
        loginBinding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBinding.civTranslate.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
    }

    private fun setupLogin() {
        loginBinding.btnLogin.setOnClickListener {
            showLoading(true)
            val email = loginBinding.etEmail.text.toString()
            val password = loginBinding.etPassword.text.toString()

            loginViewModel.login(email, password, object : ApiCallbackString {
                override fun responseState(success: Boolean, message: String) {
                    if (!success) {
                        AlertDialog.Builder(this@LoginActivity).apply {
                            showLoading(false)
                            setTitle(getString(R.string.failed))
                            setMessage(getString(R.string.failed_login_message))
                            setPositiveButton(getString(R.string.cont), null)
                            create()
                            show()
                        }
                    } else {
                        AlertDialog.Builder(this@LoginActivity).apply {
                            setTitle(getString(R.string.success))
                            setMessage(getString(R.string.success_login_message))
                            setPositiveButton(getString(R.string.cont)) { _, _ ->
                                val intent = Intent(context, StoryListActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    }
                }
            })
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            loginBinding.apply {
                pbLogin.visibility = View.VISIBLE

                etEmail.visibility = View.GONE

                etPassword.visibility = View.GONE

                civTranslate.visibility = View.GONE

                tvRegister.visibility = View.GONE

                btnLogin.visibility = View.GONE
            }
        } else {
            loginBinding.apply {
                pbLogin.visibility = View.GONE

                etEmail.visibility = View.VISIBLE

                etPassword.visibility = View.VISIBLE

                civTranslate.visibility = View.VISIBLE

                tvRegister.visibility = View.VISIBLE

                btnLogin.visibility = View.VISIBLE
            }
        }
    }
}