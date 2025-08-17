package com.dicoding.storyapp.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.TextWatcher
import android.text.Editable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.dicoding.storyapp.R

class CustomPasswordInput @JvmOverloads constructor(
    context: Context ,
    attrs: AttributeSet? = null ,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context , attrs , defStyleAttr) {

    private var isPasswordVisible = false
    private var leftIcon: Drawable? = ContextCompat.getDrawable(context , R.drawable.lock_ic)
    private var eyeIcon: Drawable? =
        ContextCompat.getDrawable(context , R.drawable.ic_visibility_off)

    init {

        background = ContextCompat.getDrawable(context , R.drawable.bg_edittext)
        hint = context.getString(R.string.password_input)
        setTextColor(ContextCompat.getColor(context , R.color.text_color))
        setHintTextColor(ContextCompat.getColor(context , R.color.dark_grey))

        leftIcon?.let {
            DrawableCompat.setTint(it , ContextCompat.getColor(context , R.color.orange))
        }
        eyeIcon?.let {
            DrawableCompat.setTint(it , ContextCompat.getColor(context , R.color.orange))
        }

        setCompoundDrawablesWithIntrinsicBounds(leftIcon , null , eyeIcon , null)
        compoundDrawablePadding = 16
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        setPadding(48 , 32 , 48 , 32)


        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence? ,
                start: Int ,
                count: Int ,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence? , start: Int , before: Int , count: Int) {
                if ((s?.length ?: 0) < 8) {
                    error = context.getString(R.string.error_password)
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        setOnTouchListener { _ , event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val iconWidth = eyeIcon?.intrinsicWidth ?: 0
                if (event.x >= (width - paddingRight - iconWidth)) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        setSelection(text?.length ?: 0)

        val iconRes = if (isPasswordVisible) R.drawable.ic_visible else R.drawable.ic_visibility_off
        eyeIcon = ContextCompat.getDrawable(context , iconRes)
        eyeIcon?.let {
            DrawableCompat.setTint(it , ContextCompat.getColor(context , R.color.orange))
        }
        setCompoundDrawablesWithIntrinsicBounds(leftIcon , null , eyeIcon , null)
    }

    fun setErrorMessage(message: String?) {
        error = message
    }

    fun getTextString(): String = text.toString()
}
