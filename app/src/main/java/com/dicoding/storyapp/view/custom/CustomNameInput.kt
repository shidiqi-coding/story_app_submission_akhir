package com.dicoding.storyapp.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.dicoding.storyapp.R

class CustomNameInput @JvmOverloads constructor(
    context: Context ,
    attrs: AttributeSet? = null ,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context , attrs , defStyleAttr) {

    init {

        background = ContextCompat.getDrawable(context , R.drawable.bg_edittext)


        hint = context.getString(R.string.username_input)
        setTextColor(ContextCompat.getColor(context , R.color.text_color))
        setHintTextColor(ContextCompat.getColor(context , R.color.dark_grey))


        val leftIcon: Drawable? = ContextCompat.getDrawable(context , R.drawable.user_ic)
        leftIcon?.let {
            DrawableCompat.setTint(it , ContextCompat.getColor(context , R.color.orange))
            setCompoundDrawablesWithIntrinsicBounds(it , null , null , null)
            compoundDrawablePadding = 16
        }


        setPadding(48 , 32 , 48 , 32)
    }


    fun setError(message: String?) {
        error = message
    }


    fun getTextString(): String {
        return text.toString()
    }
}
