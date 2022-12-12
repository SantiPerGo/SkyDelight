package com.example.skydelight.custom

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.skydelight.R

class CustomDialog(globalContext: Context?) {
    private val context = globalContext
    private lateinit var dialog: Dialog

    fun init(title: String, message: String, buttonText: String,
             drawableReference: Int, backgroundReference: Int): Dialog {
        try {
            // Creating instance of dialog
            dialog = Dialog(context!!)

            // Creating dialog with layout
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Using user texts, image and colors
            dialog.findViewById<TextView>(R.id.txtTitle).text = title
            dialog.findViewById<TextView>(R.id.txtMessage).text = message
            dialog.findViewById<TextView>(R.id.btnClose).text = buttonText
            dialog.findViewById<ImageView>(R.id.imgIcon)
                .setImageDrawable(ElementsEditor().getDrawable(context, drawableReference))
            dialog.findViewById<CardView>(R.id.cardView).backgroundTintList =
                    ColorStateList.valueOf(ElementsEditor().getColor(context, backgroundReference))

            // Setting button method
            dialog.findViewById<TextView>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
        } catch (e: IllegalStateException) {}

        // Showing dialog
        return dialog
    }
}