package com.example.dicodingstoryapp.ui.register

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
import com.example.dicodingstoryapp.databinding.ActivityRegisterBinding
import com.example.dicodingstoryapp.ui.login.LoginActivity
import com.example.dicodingstoryapp.utils.ApiCallbackString
import com.example.dicodingstoryapp.utils.validateEmail

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerBinding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)

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
        registerBinding.apply {
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
        val editTexts =
            listOf(registerBinding.etName, registerBinding.etEmail, registerBinding.etPassword)
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val etName = registerBinding.etName.text.toString().trim()
                    val etEmail = registerBinding.etEmail.text.toString().trim()
                    val etPassword = registerBinding.etPassword.text.toString().trim()

                    registerBinding.btnRegister.isEnabled = etName.isNotEmpty()
                            && etEmail.isNotEmpty()
                            && etPassword.isNotEmpty()
                            && !validateEmail(etEmail)
                            && etPassword.length > 5

                    if (registerBinding.btnRegister.isEnabled) {
                        registerBinding.btnRegister.text = getString(R.string.register)
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

        setupRegister()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(registerBinding.ivLogo, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val translate =
            ObjectAnimator.ofFloat(registerBinding.civTranslate, View.ALPHA, 1f).setDuration(500)
        ObjectAnimator.ofFloat(registerBinding.civTranslate, View.TRANSLATION_X, -100f, 0f).apply {
            duration = 2000
        }.start()

        val name = ObjectAnimator.ofFloat(registerBinding.etName, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(registerBinding.etEmail, View.ALPHA, 1f).setDuration(500)
        val password =
            ObjectAnimator.ofFloat(registerBinding.etPassword, View.ALPHA, 1f).setDuration(500)
        val visibility =
            ObjectAnimator.ofFloat(registerBinding.passVisibility, View.ALPHA, 1f).setDuration(500)
        val register =
            ObjectAnimator.ofFloat(registerBinding.btnRegister, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(registerBinding.tvLogin, View.ALPHA, 1f).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(name, email, password, visibility)
        }

        AnimatorSet().apply {
            playSequentially(translate, together, register, login)
            startDelay = 500
        }.start()
    }

    private fun setupListener() {
        registerBinding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerBinding.civTranslate.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
    }

    private fun setupRegister() {
        registerBinding.btnRegister.setOnClickListener {
            showLoading(true)
            val name = registerBinding.etName.text.toString()
            val email = registerBinding.etEmail.text.toString()
            val password = registerBinding.etPassword.text.toString()

            registerViewModel.register(name, email, password, object : ApiCallbackString {
                override fun responseState(success: Boolean, message: String) {
                    if (!success) {
                        AlertDialog.Builder(this@RegisterActivity).apply {
                            showLoading(false)
                            setTitle(getString(R.string.failed))
                            setMessage(getString(R.string.failed_regist_message))
                            setPositiveButton(getString(R.string.cont), null)
                            create()
                            show()
                        }
                    } else {
                        AlertDialog.Builder(this@RegisterActivity).apply {
                            setTitle(getString(R.string.success))
                            setMessage(getString(R.string.success_regist_message))
                            setPositiveButton(getString(R.string.cont)) { _, _ ->
                                val intent = Intent(context, LoginActivity::class.java)
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
            registerBinding.apply {
                pbRegister.visibility = View.VISIBLE

                etName.visibility = View.GONE

                etEmail.visibility = View.GONE

                etPassword.visibility = View.GONE

                civTranslate.visibility = View.GONE

                tvLogin.visibility = View.GONE

                btnRegister.visibility = View.GONE
            }
        } else {
            registerBinding.apply {
                pbRegister.visibility = View.GONE

                etName.visibility = View.VISIBLE

                etEmail.visibility = View.VISIBLE

                etPassword.visibility = View.VISIBLE

                civTranslate.visibility = View.VISIBLE

                tvLogin.visibility = View.VISIBLE

                btnRegister.visibility = View.VISIBLE
            }
        }
    }
}