package com.example.skydelight.custom

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.skydelight.R
import com.google.android.material.button.MaterialButton

class ElementsEditor {
    fun updateButtonState(button: Button, state: Boolean, context: Context, background: Boolean) {
        // Disable button
        button.isClickable = state

        // Selecting Color
        val textColor: Int
        val btnColor: Int
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
    }

    // Getting reference to resource color
    fun getColor(context: Context, colorReference: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(colorReference, typedValue, true)
        return typedValue.data
    }

    fun updateColors(resource: Int, context: Context, textsArray: ArrayList<TextView>, buttonsArray: ArrayList<Button>) {
        // Getting reference to resource color
        val textColor = getColor(context, resource)

        // Changing text colors
        for(element in textsArray){
            element.setTextColor(textColor)
            element.setShadowLayer(5f,0f, 0f, textColor)
        }

        // Changing button colors
        for(element in buttonsArray){
            element.setTextColor(textColor)
            element.setShadowLayer(5f,0f, 0f, textColor)
            (element as MaterialButton).strokeColor = ColorStateList.valueOf(textColor)
            element.rippleColor = ColorStateList.valueOf(textColor)
        }
    }

    fun updateDialogButton(dialog: AlertDialog){
        // Changing neutral button position to center
        val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        positiveButton.layoutParams = layoutParams
    }

    fun elementsClickableState(state: Boolean, textsArray: ArrayList<TextView>?, buttonsArray: ArrayList<Button>){
        if(textsArray != null)
            for(element in textsArray)
                element.isClickable = state

        for(element in buttonsArray)
            element.isClickable = state
    }
}