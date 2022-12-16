package com.example.skydelight.custom

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.LinearLayout
import com.example.skydelight.R
import com.example.skydelight.databinding.CustomDialogBinding

class CustomDialog() {
    // Dialog and optional two buttons
    private lateinit var dialog: Dialog
    private var isOneButton = true
    private lateinit var binding: CustomDialogBinding

    constructor(title: String, message: String, drawableReference: Int,
             backgroundReference: Int, context: Context, oneButton: Boolean = true,
                isMiniSize: Boolean = false, buttonsBicolor: Boolean = true) : this() {
        try {
            // Initializing connection to view elements
            val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = CustomDialogBinding.inflate(inflater)

            // Creating instance of dialog
            dialog = Dialog(context)

            // Creating dialog with layout
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(binding.root)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.window?.setGravity(Gravity.CENTER)

            // Using user texts, image and colors
            binding.txtTitle.text = title
            binding.txtMessage.text = message
            binding.imgIcon.setImageDrawable(ElementsEditor().getDrawable(context, drawableReference))
            binding.cardView.backgroundTintList = ColorStateList.valueOf(
                ElementsEditor().getColor(context, backgroundReference))

            // Showing two buttons
            isOneButton = oneButton
            if(!oneButton)   {
                binding.btnClose.visibility = View.GONE
                binding.linearLayoutButtons.visibility = View.VISIBLE

                if(!buttonsBicolor)
                    // Updating colors
                    ElementsEditor().updateColors(R.attr.btn_text_color_blue,
                        R.attr.btn_background_blue, context, null,
                        arrayListOf(binding.btnLeft, binding.btnRight),
                        R.attr.btn_background_blue)
            }

            // Changing constraint layout sizes
            if(isMiniSize) {
                // Changing linear layout distribution
                binding.linearLayoutElements.weightSum = 3f

                // Changing dialog message size
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.5f)
                params.setMargins(100, 0, 100, 0)
                binding.txtMessage.layoutParams = params
                binding.txtMessage.text = message

                // Changing guidelines percentages
                binding.guidelineTop.setGuidelinePercent(0.3f)
                binding.guidelineBottom.setGuidelinePercent(0.7f)
                binding.guidelineLeft.setGuidelinePercent(0.1f)
                binding.guidelineRight.setGuidelinePercent(0.9f)
            }
        } catch (e: IllegalStateException) {}
    }

    fun firstButton(buttonText: String, function: () -> (Unit)) {
        val button = if(isOneButton) binding.btnClose else binding.btnLeft

        button.text = buttonText
        button.setOnClickListener {
            dialog.dismiss()
            function()
        }
    }

    fun secondButton(buttonText: String, function: () -> (Unit)) {
        binding.btnRight.text = buttonText
        binding.btnRight.setOnClickListener {
            dialog.dismiss()
            function()
        }
    }

    fun show(){ dialog.show() }
}