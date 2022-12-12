package com.example.skydelight.custom

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.skydelight.R

class CustomDialog() {
    private lateinit var dialog: Dialog
    private var isOneButton = true

    constructor(title: String, message: String, drawableReference: Int,
             backgroundReference: Int, context: Context, oneButton: Boolean = true) : this() {
        try {
            // Creating instance of dialog
            dialog = Dialog(context)

            // Creating dialog with layout
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Using user texts, image and colors
            dialog.findViewById<TextView>(R.id.txtTitle).text = title
            dialog.findViewById<TextView>(R.id.txtMessage).text = message
            dialog.findViewById<ImageView>(R.id.imgIcon)
                .setImageDrawable(ElementsEditor().getDrawable(context, drawableReference))
            dialog.findViewById<CardView>(R.id.cardView).backgroundTintList =
                    ColorStateList.valueOf(ElementsEditor().getColor(context, backgroundReference))

            // Showing two buttons
            isOneButton = oneButton
            if(!oneButton)   {
                dialog.findViewById<TextView>(R.id.btnClose).visibility = View.GONE
                dialog.findViewById<LinearLayout>(R.id.linearLayoutButtons).visibility = View.VISIBLE
            }
        } catch (e: IllegalStateException) {}
    }

    fun firstButton(buttonText: String, function: () -> (Unit)) {
        val btnId = if(isOneButton) R.id.btnClose else R.id.btnLeft

        dialog.findViewById<Button>(btnId).text = buttonText
        dialog.findViewById<Button>(btnId).setOnClickListener {
            dialog.dismiss()
            function()
        }
    }

    fun secondButton(buttonText: String, function: () -> (Unit)) {
        dialog.findViewById<Button>(R.id.btnRight).text = buttonText
        dialog.findViewById<Button>(R.id.btnRight).setOnClickListener {
            dialog.dismiss()
            function()
        }
    }

    fun show(){ dialog.show() }
}