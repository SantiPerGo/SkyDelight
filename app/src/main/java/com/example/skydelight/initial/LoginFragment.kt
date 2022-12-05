package com.example.skydelight.initial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.User
import com.example.skydelight.databinding.FragmentLoginBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import com.example.skydelight.custom.ValidationsDialogsRequests

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

        // Clearing errors when produced
        binding.editTxtEmail.doOnTextChanged { _, _, _, _ -> if(binding.FieldEmail.error != null) binding.FieldEmail.error = null }
        binding.editTxtPassword.doOnTextChanged { _, _, _, _ -> if(binding.FieldPassword.error != null) binding.FieldPassword.error = null }

        binding.btnLogin.setOnClickListener {
            val email = binding.editTxtEmail.text.toString()
            val password = binding.editTxtPassword.text.toString()
            if(ValidationsDialogsRequests().validateEmail(email, binding.FieldEmail)
                && ValidationsDialogsRequests().validatePassword(password, binding.FieldPassword)){
                login(email, password)
            }
        }

        // Returning to the start screen fragment
        binding.btnReturn.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_startScreen)
            findNavController().popBackStack(R.id.login_fragment, true) }
    }

    // Function to connect with the api
    private fun login(email: String, password: String) {
        // Making http request
        val request = Request.Builder()
            .url("https://apiskydelight.herokuapp.com/usuarios/token/obtener/")
            .post(FormBody.Builder().add("email", email).add("password", password).build())
            .build()

        ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
            getString(R.string.btn_login), binding.btnLogin, binding.btnReturn, null, null,
            binding.progressBar, 401, getString(R.string.snackbar_error_login), null)
        { responseString: String ->
            // Changing http body to json
            val json = JSONObject(responseString)

            // Launching room database connection
            MainScope().launch {
                // Creating connection to database
                val userDao =
                    Room.databaseBuilder(
                        findNavController().context,
                        AppDatabase::class.java,
                        "user"
                    )
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