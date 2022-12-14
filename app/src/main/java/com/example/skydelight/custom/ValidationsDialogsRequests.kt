package com.example.skydelight.custom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.skydelight.MainActivity
import com.example.skydelight.R
import com.example.skydelight.navbar.NavBarFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import okhttp3.*
import java.io.IOException
import java.util.regex.Pattern

class ValidationsDialogsRequests {
    fun snackBar(view: View, BackgroundColor: Int, fontColor: Int,
                 text: String, context: Context){
        // Creating snackbar
        val mySnackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)

        // Changing background
        val snackBarView = mySnackbar.view
        snackBarView.setBackgroundColor(BackgroundColor)

        // Changing position on screen
        val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        snackBarView.layoutParams = params

        // Changing text style
        val textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(fontColor)
        textView.textSize = 15f
        textView.gravity = Gravity.CENTER
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.inter), Typeface.BOLD)

        // Showing
        mySnackbar.show()
    }

    fun snackBarOnUIThread(message: String, btnText: String?, view: View, backgroungColor: Int, fontColor: Int,
                           activity: Activity, context: Context, button1: Button?, button2: Button?, button3: Button?,
                           button4: Button?, progressBar: ProgressBar?, parentFragment: NavBarFragment?,
                              function: () -> (Unit)){
        try {
            activationOfButtons(true, btnText, activity, button1,
                button2, button3, button4, progressBar, parentFragment)

            activity.runOnUiThread {
                snackBar(view, backgroungColor, fontColor, message, context)

                //dialog.dismiss()
                function()
            }
        } catch (e: java.lang.IllegalStateException) {}
    }

    private fun errorDialog(activity: Activity, context: Context) {
        activity.runOnUiThread {
            val errorDialog = CustomDialog(activity.getString(R.string.custom_dialog_error_title),
                activity.getString(R.string.custom_dialog_error_description), R.attr.heart_dead,
                R.attr.fragment_background, context)
            errorDialog.firstButton(activity.getString(R.string.custom_dialog_error_button)) {
                val intent = Intent(activity, MainActivity::class.java)
                activity.startActivity(intent)
                activity.finishAffinity()
            }
            errorDialog.show()
        }
    }

    fun httpPetition(request: Request, context: Context, view: View, activity: Activity, btnText: String?, button1: Button?,
                     button2: Button?, button3: Button?, button4: Button?, progressBar: ProgressBar?, codeError: Int?,
                     errorMessage: String?, parentFragment: NavBarFragment?, function: (String) -> (Unit)){
        try {
            // Getting color according of theme
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.btn_background_red, typedValue, true)
            val btnColor = typedValue.data
            context.theme.resolveAttribute(R.attr.btn_text_color_red, typedValue, true)
            val textColor = typedValue.data

            // Deactivating navbar and buttons when necessary
            activationOfButtons(false, btnText, activity, button1, button2, button3, button4, progressBar, parentFragment)

            // Making HTTP request and getting response
            OkHttpClient().newCall(request).enqueue(object : Callback {
                // Changing to principal fragment if it's successful
                override fun onResponse(call: Call, response: Response){
                    // Printing api answer
                    val responseString = response.body()?.string().toString()
                    Log.d("OKHTTP3-CODE", response.code().toString())
                    Log.d("OKHTTP3-BODY", responseString)

                    // Code 200 = account verified
                    if(response.code() in 200..202) {
                        // Activating navbar when necessary
                        activationOfButtons(true, btnText, activity, button1,
                            button2, button3, button4, progressBar, parentFragment)
                        function(responseString)
                    }
                    // Custom error code
                    else if(codeError != null)
                        if(response.code() == codeError && errorMessage != null)
                            snackBarOnUIThread(errorMessage, btnText, view, btnColor, textColor, activity,
                                context, button1, button2, button3, button4, progressBar, parentFragment) {}
                        else if(response.code() == codeError)
                            errorDialog(activity, context)
                    // Connection or server errors
                    else if(response.code() in 400..405 || response.code() == 500)
                        errorDialog(activity, context)
                }

                // Print dialog if it's error
                override fun onFailure(call: Call, e: IOException){
                    // Printing api answer
                    Log.d("OKHTTP3-ERROR", e.toString())

                    // Showing message to the user
                    errorDialog(activity, context)
                }
            })
        } catch (e: java.lang.IllegalStateException) {}
    }

    fun activationOfButtons(state: Boolean, btnText: String?, activity: Activity, button1: Button?, button2: Button?,
                            button3: Button?, button4: Button?, progressBar: ProgressBar?, parentFragment: NavBarFragment?){
        activity.runOnUiThread {
            button1?.isClickable = state
            button2?.isClickable = state
            button3?.isClickable = state
            button4?.isClickable = state
            parentFragment?.changeNavBarButtonsClickable(state)

            if(state) {
                progressBar?.visibility = View.INVISIBLE
                button1?.text = btnText
            } else {
                progressBar?.visibility = View.VISIBLE
                button1?.text = ""
            }
        }
    }

    fun validateEmail(email: String, fieldEmail: TextInputLayout) : Boolean{
        when {
            // Showing alert dialog if email field is empty
            email.isEmpty() -> fieldEmail.error = "Olvidaste colocar tu correo"
            // Showing alert dialog if email field is not an email
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> fieldEmail.error = "Formato de correo no válido"
            // Showing alert dialog if email has more than 50 characters
            email.length > 50 -> fieldEmail.error = "La longitud máxima es de 50 caracteres"
            // Connection to the api and sending the password to the email
            else -> return true
        }
        return false
    }

    fun validatePassword(password: String, fieldPassword: TextInputLayout) : Boolean{
        when {
            // Showing alert dialog if password field is empty
            password.isEmpty() -> fieldPassword.error = "Olvidaste colocar tu contraseña"
            // Showing alert dialog if password has less than 8 characters
            password.length < 8 -> fieldPassword.error = "La longitud mínima es de 8 caracteres"
            // Showing alert dialog if password has blank spaces
            password.contains(" ") -> fieldPassword.error = "No se permiten espacios en blanco"
            // Showing alert dialog if password has more than 50 characters
            password.length > 50 -> fieldPassword.error = "La longitud máxima es de 50 caracteres"
            // Connection to the api and sending the password to the email
            else -> return true
        }
        return false
    }

    fun validateConfirmedPassword(password: String, confirmedPassword: String, fieldPassword: TextInputLayout) : Boolean{
        when {
            // Showing alert dialog if confirm password field is empty
            confirmedPassword.isEmpty() -> fieldPassword.error = "Olvidaste confirmar tu contraseña"
            // Showing alert dialog if password and confirmedPassword don't match
            password != confirmedPassword -> fieldPassword.error = "Esta contraseña es distinta a la primera"
            else -> return true
        }
        return false
    }

    fun validateName(name: String, fieldName: TextInputLayout) : Boolean{
        // Showing alert dialog if name field is empty
        when {
            name.isEmpty() -> fieldName.error = "Olvidaste colocar tu nombre"
            // Showing alert dialog if name contains numbers or special characters
            !Pattern.matches("[a-zA-ZñÑ áéíóúÁÉÍÓÚ]+", name) -> fieldName.error = "No se permiten ese tipo de caracteres"
            // Showing alert dialog if name has more than 50 characters
            name.length > 50 -> fieldName.error = "La longitud máxima es de 50 caracteres"
            else -> return true
        }
        return false
    }

    fun validateSex(sexId: Int, view: View, context: Context, message: String): Boolean {
        // Getting color according of theme
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.btn_background_red, typedValue, true)
        val btnColor = typedValue.data
        context.theme.resolveAttribute(R.attr.btn_text_color_red, typedValue, true)
        val textColor = typedValue.data

        if(sexId != -1)
            return true
        else
            snackBar(view, btnColor, textColor, message, context)
        return false
    }
}