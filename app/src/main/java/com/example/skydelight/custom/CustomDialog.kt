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
    // Dialog and optional two buttons
    private lateinit var dialog: Dialog
    private var isOneButton = true

    // Linking variables to elements in view
    private lateinit var txtMessage: TextView
    private lateinit var txtMessageMini: TextView
    private lateinit var txtTitle: TextView
    private lateinit var imgIcon: ImageView
    private lateinit var cardView: CardView
    private lateinit var linearLayoutButtons: LinearLayout
    private lateinit var btnClose: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button

    constructor(title: String, message: String, drawableReference: Int,
             backgroundReference: Int, context: Context, oneButton: Boolean = true,
                isMiniSize: Boolean = false, buttonsBicolor: Boolean = true) : this() {
        try {
            // Creating instance of dialog
            dialog = Dialog(context)

            // Creating dialog with layout
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Initializing
            initVariables()

            // Using user texts, image and colors
            txtTitle.text = title
            txtMessage.text = message
            imgIcon.setImageDrawable(ElementsEditor().getDrawable(context, drawableReference))
            cardView.backgroundTintList = ColorStateList.valueOf(
                ElementsEditor().getColor(context, backgroundReference))

            // Showing two buttons
            isOneButton = oneButton
            if(!oneButton)   {
                btnClose.visibility = View.GONE
                linearLayoutButtons.visibility = View.VISIBLE

                if(!buttonsBicolor)
                    // Updating colors
                    ElementsEditor().updateColors(R.attr.btn_text_color_blue, context,
                        null, arrayListOf(btnLeft, btnRight), R.attr.btn_background_blue)
            }

            // Changing text size
            if(isMiniSize) {
                txtMessageMini.text = message
                txtMessage.visibility = View.GONE
                txtMessageMini.visibility = View.VISIBLE
            }
        } catch (e: IllegalStateException) {}
    }

    private fun initVariables() {
        txtMessage = dialog.findViewById(R.id.txtMessage)
        txtMessageMini = dialog.findViewById(R.id.txtMessageMini)
        txtTitle = dialog.findViewById(R.id.txtTitle)
        imgIcon = dialog.findViewById(R.id.imgIcon)
        cardView = dialog.findViewById(R.id.cardView)
        linearLayoutButtons = dialog.findViewById(R.id.linearLayoutButtons)
        btnClose = dialog.findViewById(R.id.btnClose)
        btnLeft = dialog.findViewById(R.id.btnLeft)
        btnRight = dialog.findViewById(R.id.btnRight)
    }

    fun firstButton(buttonText: String, function: () -> (Unit)) {
        val button = if(isOneButton) btnClose else btnLeft

        button.text = buttonText
        button.setOnClickListener {
            dialog.dismiss()
            function()
        }
    }

    fun secondButton(buttonText: String, function: () -> (Unit)) {
        btnRight.text = buttonText
        btnRight.setOnClickListener {
            dialog.dismiss()
            function()
        }
    }

    fun show(){ dialog.show() }
}