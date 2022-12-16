package com.example.skydelight.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
            val shadowRadius = ResourcesCompat.getFloat(context!!.resources, R.dimen.shadow_radius)
            button.setTextColor(textColor)
            button.setShadowLayer(shadowRadius,0f, 0f, btnColor)

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

    fun updateColors(textResource: Int, shadowResource: Int, context: Context?,
                     textsArray: ArrayList<TextView>?, buttonsArray: ArrayList<Button>?,
                     backgroundResource: Int? = null) {
        try {
            // Getting reference to resource color
            val textColor = getColor(context, textResource)
            val shadowColor = getColor(context, shadowResource)
            var btnColor = textColor
            if(backgroundResource != null)
                btnColor = getColor(context, backgroundResource)

            val shadowRadius = ResourcesCompat.getFloat(context!!.resources, R.dimen.shadow_radius)
            // Changing text colors
            textsArray?.forEach {
                it.setTextColor(textColor)
                it.setShadowLayer(shadowRadius,0f, 0f, shadowColor)
            }

            // Changing button colors
            buttonsArray?.forEach {
                it.setTextColor(textColor)
                it.setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
                (it as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)

                if(backgroundResource != null)
                    it.backgroundTintList = ColorStateList.valueOf(btnColor)
                else
                    it.strokeColor = ColorStateList.valueOf(textColor)
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