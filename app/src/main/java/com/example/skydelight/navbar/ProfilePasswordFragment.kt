package com.example.skydelight.navbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarProfilePasswordBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody

class ProfilePasswordFragment : Fragment() {

    // Binding variable to use elements in the xml layout
    private lateinit var binding: FragmentNavbarProfilePasswordBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarProfilePasswordBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_profile_password, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Clearing errors when produced
        binding.editTxtPassword.doOnTextChanged { _, _, _, _ ->
            if(binding.FieldPassword.error != null) binding.FieldPassword.error = null

            // Disable or enable login button
            validateInputsNotEmpty()
        }
        binding.editTxtConfirmPassword.doOnTextChanged { _, _, _, _ ->
            if(binding.FieldConfirmPassword.error != null) binding.FieldConfirmPassword.error = null

            // Disable or enable login button
            validateInputsNotEmpty()
        }

        // Changing to updating password fragment
        binding.btnCancel.setOnClickListener {
            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
        }

        binding.btnUpdate.setOnClickListener{
            val password = binding.editTxtPassword.text.toString()
            val confirmedPassword = binding.editTxtConfirmPassword.text.toString()

            if(ValidationsDialogsRequests().validatePassword(password, binding.FieldPassword)
                && ValidationsDialogsRequests().validateConfirmedPassword(password, confirmedPassword, binding.FieldConfirmPassword))
                updatePassword(password)
        }

        // Disable login button
        ElementsEditor().updateButtonState(binding.btnUpdate, false, requireContext(), true)
    }

    private fun validateInputsNotEmpty() {
        // Disable or enable login button
        if(binding.editTxtPassword.text!!.isNotEmpty()
            && binding.editTxtConfirmPassword.text!!.isNotEmpty())
            ElementsEditor().updateButtonState(binding.btnUpdate, true, requireContext(), true)
        else
            ElementsEditor().updateButtonState(binding.btnUpdate, false, requireContext(), true)
    }

    // Function to connect with the api
    private fun updatePassword(password: String){
        // Deactivating clickable
        (parentFragment as NavBarFragment).changeNavBarButtonsClickable(false)

        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao()
            val user = userDao.getUser()[0]

            // Arguments to Post Request
            val formBody: RequestBody = FormBody.Builder()
                .add("email", user.email)
                .add("password", password)
                .build()

            // Making http request
            val request = Request.Builder()
                .url("https://apiskydelight.herokuapp.com/usuarios/cambiar-contrasena/")
                .put(formBody)
                .addHeader("Authorization", "Bearer " + user.token)
                .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                .build()

            ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
                getString(R.string.btn_update), binding.btnUpdate, binding.btnCancel, null, null,
                binding.progressBar, 404, getString(R.string.snackbar_error_recover), (parentFragment as NavBarFragment))
            {
                // Getting color according of theme
                val btnColor = ElementsEditor().getColor(requireContext(), R.attr.btn_background_green)
                val textColor = ElementsEditor().getColor(requireContext(), R.attr.btn_text_color_green)

                // Showing succesful dialog
                ValidationsDialogsRequests().snackBarOnUIThread(
                    getString(R.string.snackbar_success_update_password), null, requireView(), btnColor, textColor,
                    requireActivity(), requireContext(), null, null, null, null,
                    null, null) {
                    // Fragment enters from right
                    (parentFragment as NavBarFragment).updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
                }
            }
        }
    }
}