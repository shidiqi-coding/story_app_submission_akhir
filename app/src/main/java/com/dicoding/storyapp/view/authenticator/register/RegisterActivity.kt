package com.dicoding.storyapp.view.authenticator.register

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
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
import com.dicoding.storyapp.databinding.ActivityRegisterBinding
import com.dicoding.storyapp.view.WelcomeActivity
import com.dicoding.storyapp.view.helper.LocaleHelper
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: RegisterViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        val langCode = LocaleHelper.getSavedLanguage(newBase ?: return)
        val contextWithLocale = LocaleHelper.applyLanguage(newBase , langCode)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideStatusBar()
        setupPasswordToggle()
        setupAction()

        binding.btnRegBack.setOnClickListener {
            startActivity(Intent(this , WelcomeActivity::class.java))
            finish()
        }
    }

    private fun hideStatusBar() {
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

    private fun setupPasswordToggle() {

        val eyeIcon = ContextCompat.getDrawable(this , R.drawable.ic_visibility_off)
        eyeIcon?.let {
            binding.passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                binding.passwordInput.compoundDrawables[0] ,
                null ,
                it ,
                null
            )
        }


        binding.passwordInput.setOnTouchListener { v , event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP &&
                event.rawX >= (binding.passwordInput.right - binding.passwordInput.compoundDrawables[DRAWABLE_RIGHT].bounds.width())
            ) {
                binding.passwordInput.togglePasswordVisibility()
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun setupAction() {
        binding.buttonRegister.setOnClickListener {
            val username = binding.usernameInput.getTextString().trim()
            val email = binding.emailInput.getTextString().trim()
            val password = binding.passwordInput.getTextString().trim()

            when {
                username.isEmpty() -> binding.usernameInput.setError(getString(R.string.required_name))
                email.isEmpty() -> binding.emailInput.setError(getString(R.string.required_email))
                password.isEmpty() -> binding.passwordInput.setError(getString(R.string.required_password))
                password.length < 8 -> binding.passwordInput.setError(getString(R.string.minimum_password))
                else -> doRegister(username , email , password)
            }
        }
    }

    private fun doRegister(username: String , email: String , password: String) {
        viewModel.register(username , email , password).observe(this) { result ->
            when (result) {
                is ResultState.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.loadingOverlay.visibility = View.GONE
                    showSuccessDialog(email)
                }

                is ResultState.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    showErrorDialog(parseErrorMessage(result.error))
                }
            }
        }
    }

    private fun showSuccessDialog(email: String) {
        val successMessage = String.format(getString(R.string.register_success_message) , email)
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.success_title))
            .setMessage(successMessage)
            .setPositiveButton(getString(R.string.continue_button)) { _ , _ -> finish() }
            .create()

        dialog.setOnShowListener { styleDialog(dialog) }
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.register_failed))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button) , null)
            .create()

        dialog.setOnShowListener { styleDialog(dialog) }
        dialog.show()
    }

    private fun styleDialog(dialog: AlertDialog) {
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(
            ContextCompat.getColor(this , R.color.text_color)
        )
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(this , R.color.button_text_color)
        )
    }

    private fun parseErrorMessage(error: String): String {
        return try {
            val json = JSONObject(error)
            getLocalizedErrorMessage(json.getString("message"))
        } catch (_: Exception) {
            getLocalizedErrorMessage(error)
        }
    }

    private fun getLocalizedErrorMessage(messageFromServer: String?): String {
        return when (messageFromServer) {
            "Email is already taken" -> getString(R.string.email_taken)
            else -> messageFromServer ?: getString(R.string.unknown_error)
        }
    }
}
