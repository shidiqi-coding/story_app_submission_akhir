package com.dicoding.storyapp.view.authenticator.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.ResultState
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.view.WelcomeActivity
import com.dicoding.storyapp.view.helper.LocaleHelper
import com.dicoding.storyapp.view.main.MainActivity
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityLoginBinding

    override fun attachBaseContext(newBase: Context?) {
        val langCode = LocaleHelper.getSavedLanguage(newBase ?: return)
        val contextWithLocale = LocaleHelper.applyLanguage(newBase , langCode)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()

        binding.btnLogBack.setOnClickListener {
            startActivity(Intent(this , WelcomeActivity::class.java))
            finish()
        }

        binding.usernameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.usernameInput.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    binding.passwordInput.error =
                        getString(R.string.error_password)
                } else {
                    binding.passwordInput.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.usernameInput.getTextString()
            val password = binding.passwordInput.getTextString()
            var isValid = true

            if (email.isEmpty()) {
                binding.usernameInput.error = getString(R.string.email_empty)
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.usernameInput.error = getString(R.string.email_invalid)
                isValid = false
            }

            if (password.isEmpty()) {
                binding.passwordInput.error = getString(R.string.password_empty)
                isValid = false
            } else if (password.length < 8) {
                binding.passwordInput.error = getString(R.string.error_password)
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            viewModel.login(email , password).observe(this) { result ->
                when (result) {
                    is ResultState.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                    is ResultState.Success -> {
                        binding.loadingOverlay.visibility = View.GONE
                        val user = result.data
                        viewModel.saveSession(
                            UserModel(
                                email = email ,
                                token = user.token ?: "" ,
                                isLogin = true ,
                                name = user.name ?: ""
                            )
                        )
                        showSuccessDialog()
                    }
                    is ResultState.Error -> {
                        binding.loadingOverlay.visibility = View.GONE
                        showErrorDialog(result.error)
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.success_title))
            setMessage(getString(R.string.login_success_message))
            setPositiveButton(getString(R.string.continue_button)) { _ , _ ->
                val intent = Intent(this@LoginActivity , MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }.create()

        dialog.setOnShowListener {
            dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(
                ContextCompat.getColor(this@LoginActivity , R.color.text_color)
            )
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this@LoginActivity , R.color.button_text_color)
            )
        }
        dialog.show()
    }

    private fun showErrorDialog(error: String) {
        val localizedMessage = try {
            val json = JSONObject(error)
            getLocalizedErrorMessage(json.getString("message"))
        } catch (_: Exception) {
            getLocalizedErrorMessage(error)
        }

        val dialog = AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.login_failed_title))
            setMessage(localizedMessage)
            setPositiveButton(getString(R.string.ok_button) , null)
        }.create()

        dialog.setOnShowListener {
            dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(
                ContextCompat.getColor(this@LoginActivity , R.color.text_color)
            )
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this@LoginActivity , R.color.button_text_color)
            )
        }
        dialog.show()
    }

    private fun getLocalizedErrorMessage(messageFromServer: String?): String {
        return when (messageFromServer) {
            "User not found" -> getString(R.string.user_not_found)
            "Invalid password" -> getString(R.string.error_invalid_password)
            else -> messageFromServer ?: getString(R.string.unknown_error)
        }
    }
}
