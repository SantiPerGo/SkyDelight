package com.example.skydelight.initial

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentRecoverPasswordBinding
import okhttp3.*

class RecoverPasswordFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentRecoverPasswordBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)
        return binding.root

        //return inflater.inflate(R.layout.fragment_recover_password, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Clearing errors when produced
        binding.editTxtEmail.doOnTextChanged { _, _, _, _ -> if(binding.FieldEmail.error != null) binding.FieldEmail.error = null }

        binding.btnRecover.setOnClickListener {
            val email = binding.editTxtEmail.text.toString()
            if(ValidationsDialogsRequests().validateEmail(email, binding.FieldEmail)){
                // Deactivating buttons
                binding.btnRecover.isClickable = false
                binding.btnReturn.isClickable = false
                recoverPassword(email)
            }
        }

        // Returning to the start screen fragment
        binding.btnReturn.setOnClickListener {
            findNavController().navigate(R.id.action_recoverPassword_to_startScreen)
            findNavController().popBackStack(R.id.recover_password_fragment, true) }
    }

    // Function to connect with the api
    private fun recoverPassword(email: String) {
        // Making http request
        val request = Request.Builder()
            .url("https://apiskydelight.herokuapp.com/usuarios/recuperar-contraseña/")
            .post(FormBody.Builder().add("email", email).build())
            .header("KEY-CLIENT", BuildConfig.API_KEY)
            .build()

        ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
            getString(R.string.recoverScreen_btn_recover), binding.btnRecover, binding.btnReturn, null, null,
            binding.progressBar, 404, getString(R.string.snackbar_error_recover), null)
        {
            // Getting color according of theme
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(R.attr.btn_background_green, typedValue, true)
            val btnColor = typedValue.data
            requireContext().theme.resolveAttribute(R.attr.btn_text_color_green, typedValue, true)
            val textColor = typedValue.data

            ValidationsDialogsRequests().snackBarOnUIThread(
                getString(R.string.snackbar_success_recover), null, requireView(), btnColor, textColor,
                requireActivity(), requireContext(), null, null, null, null,
                null, null) {
                findNavController().navigate(R.id.action_recoverPassword_to_startScreen)
            }
        }
    }
}