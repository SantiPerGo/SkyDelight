package com.example.skydelight.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.skydelight.R
import com.google.android.material.button.MaterialButton

class ElementsEditor {
    fun updateButtonState(button: Button, state: Boolean, context: Context?, background: Boolean) {
        // Disable button
        button.isClickable = state

        // Selecting Color
        val textColor: Int
        val btnColor: Int
        try {
            when(state) {
                true -> {
                    textColor = getColor(context, R.attr.btn_text_color_blue)
                    btnColor = getColor(context, R.attr.btn_background_blue)
                }
                false -> {
                    textColor = getColor(context, R.attr.btn_text_color_gray)
                    btnColor = getColor(context, R.attr.btn_background_gray)
                }
            }

            // Changing button color when it's tapped
            (button as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)

            // Changing button text color and glow effect
            button.setTextColor(textColor)
            button.setShadowLayer(5f,0f, 0f, textColor)

            // Changing button background
            if (background)
                button.backgroundTintList = ColorStateList.valueOf(btnColor)
            else
                button.strokeColor = ColorStateList.valueOf(textColor)
        } catch (e: java.lang.IllegalStateException) {}
    }

    // Getting reference to resource color
    fun getColor(context: Context?, colorReference: Int): Int {
        val typedValue = TypedValue()
        context?.theme?.resolveAttribute(colorReference, typedValue, true)
        return typedValue.data
    }

    fun getDrawable(context: Context, drawableReference: Int): Drawable {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(drawableReference, typedValue, true)
        return ContextCompat.getDrawable(context, typedValue.resourceId)!!
    }

    fun updateColors(textResource: Int, context: Context?, textsArray: ArrayList<TextView>?,
                     buttonsArray: ArrayList<Button>, backgroundResource: Int? = null) {
        try {
            // Getting reference to resource color
            val textColor = getColor(context, textResource)
            var btnColor = textColor
            if(backgroundResource != null)
                btnColor = getColor(context, backgroundResource)

            // Changing text colors
            textsArray?.forEach {
                it.setTextColor(textColor)
                it.setShadowLayer(5f,0f, 0f, textColor)
            }

            // Changing button colors
            for(element in buttonsArray){
                element.setTextColor(textColor)
                element.setShadowLayer(5f,0f, 0f, textColor)
                (element as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)

                if(backgroundResource != null)
                    element.backgroundTintList = ColorStateList.valueOf(btnColor)
                else
                    element.strokeColor = ColorStateList.valueOf(textColor)
            }
        } catch(e: java.lang.IllegalStateException) {}
    }

    fun elementsClickableState(state: Boolean, textsArray: ArrayList<TextView>?,
                               buttonsArray: ArrayList<Button>){
        if(textsArray != null)
            for(element in textsArray)
                element.isClickable = state

        for(element in buttonsArray)
            element.isClickable = state
    }
}