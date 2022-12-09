package com.example.skydelight.initial

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.User
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentLoginBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject

class LoginFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentLoginBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

        //return inflater.inflate(R.layout.fragment_login, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hiding and showing elements
        Handler(Looper.getMainLooper()).postDelayed({ elementsVisibility(true) }, 500)

        // Clearing errors when produced
        binding.editTxtEmail.doOnTextChanged { _, _, _, _ ->
            if(binding.FieldEmail.error != null) binding.FieldEmail.error = null

            // Disable or enable login button
            validateInputsNotEmpty()
        }

        binding.editTxtPassword.doOnTextChanged { _, _, _, _ ->
            if(binding.FieldPassword.error != null) binding.FieldPassword.error = null

            // Disable or enable login button
            validateInputsNotEmpty()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.editTxtEmail.text.toString()
            val password = binding.editTxtPassword.text.toString()
            if(ValidationsDialogsRequests().validateEmail(email, binding.FieldEmail)
                && ValidationsDialogsRequests().validatePassword(password, binding.FieldPassword)){
                ElementsEditor().elementsClickableState(false, null, arrayListOf(binding.btnLogin, binding.btnReturn))
                login(email, password)
            }
        }

        // Returning to the start screen fragment
        binding.btnReturn.setOnClickListener {
            ElementsEditor().elementsClickableState(false, null, arrayListOf(binding.btnLogin, binding.btnReturn))
            elementsVisibility(false)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_login_to_startScreen)
                findNavController().popBackStack(R.id.login_fragment, true)
            }, 500)
        }

        // Disable login button
        ElementsEditor().updateButtonState(binding.btnLogin, false, requireContext(), false)
    }

    private fun validateInputsNotEmpty() {
        // Disable or enable login button
        if(binding.editTxtEmail.text!!.isNotEmpty() && binding.editTxtPassword.text!!.isNotEmpty())
            ElementsEditor().updateButtonState(binding.btnLogin, true, requireContext(), false)
        else
            ElementsEditor().updateButtonState(binding.btnLogin, false, requireContext(), false)
    }

    fun elementsVisibility(state: Boolean){
        val elementsArray = arrayListOf(binding.loginTitle, binding.txtEmail,  binding.txtPassword,
            binding.btnLogin, binding.btnReturn, binding.FieldEmail, binding.FieldPassword, binding.rememberSession)

        for(element in elementsArray)
            if(state)
                element.animate().alpha(1f)
            else
                element.animate().alpha(0f)
    }

    // Function to connect with the api
    private fun login(email: String, password: String) {
        // Making http request
        val request = Request.Builder()
            .url("https://apiskydelight.herokuapp.com/usuarios/token/obtener/")
            .post(FormBody.Builder().add("email", email).add("password", password).build())
            .build()

        context?.let { context ->
            ValidationsDialogsRequests().httpPetition(
                request, context, requireView(), requireActivity(),
                getString(R.string.btn_login), binding.btnLogin, binding.btnReturn, null, null,
                binding.progressBar, 401, getString(R.string.snackbar_error_login), null)
            { responseString: String ->
                // Changing http body to json
                val json = JSONObject(responseString)

                // Launching room database connection
                MainScope().launch {
                    // Creating connection to database
                    val userDao = Room.databaseBuilder(context, AppDatabase::class.java, "user")
                        .fallbackToDestructiveMigration().build().userDao()

                    // If user exists, we have to delete it
                    val user = userDao.getUser()
                    if (user.isNotEmpty())
                        userDao.deleteUser(user[0])

                    // Adding the new user to the database
                    userDao.insertUser(
                        User(
                            json.getString("user"), json.getString("name"),
                            json.getString("sex"), json.getInt("age"), json.getString("access"),
                            json.getString("refresh"), binding.rememberSession.isChecked
                        )
                    )

                    // Changing to the principal fragment
                    activity?.runOnUiThread { findNavController().navigate(R.id.action_login_to_navBar) }
                }
            }
        }
    }
}