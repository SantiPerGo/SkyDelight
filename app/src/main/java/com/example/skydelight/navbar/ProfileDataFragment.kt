package com.example.skydelight.navbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.User
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarProfileDataBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody

class ProfileDataFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarProfileDataBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarProfileDataBinding.inflate(inflater, container, false)
        return binding.root

        //return inflater.inflate(R.layout.fragment_navbar_profile_data, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Changing Default Values of Age Number Picker
        binding.numberPickerAge.minValue = 18
        binding.numberPickerAge.maxValue = 25

        // Showing user data in profile screen fragment
        showUserData()

        // Clearing errors when produced
        binding.editTxtName.doOnTextChanged { _, _, _, _ ->
            if(binding.FieldName.error != null) binding.FieldName.error = null

            // Disable or enable login button
            validateInputsNotEmpty()
        }

        // Disable or enable login button
        binding.radioGroupSex.setOnCheckedChangeListener { _, _ -> validateInputsNotEmpty() }

        // Changing to updating password fragment
        binding.btnCancel.setOnClickListener {
            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
        }

        binding.btnUpdate.setOnClickListener {
            // Getting sex option selected and email
            val sexId = binding.radioGroupSex.checkedRadioButtonId

            // Getting user answers
            val name = binding.editTxtName.text.toString()
            val age = binding.numberPickerAge.value
            val sex = binding.radioGroupSex.findViewById<RadioButton>(sexId)?.text.toString()

            // Connection to the api and sending the password to the email
            try {
                if(ValidationsDialogsRequests().validateName(name, binding.FieldName) &&
                    ValidationsDialogsRequests().validateSex(sexId, view, requireContext(),
                        getString(R.string.snackbar_error_sex)))
                    updateData(name, sex, age)
            } catch(e: java.lang.IllegalStateException) {}
        }

        // Disable login button
        try { ElementsEditor().updateButtonState(binding.btnUpdate,
            false, context, true) } catch(e: java.lang.IllegalStateException) {}
    }

    private fun validateInputsNotEmpty() {
        // Disable or enable login button
        try {
            if(binding.editTxtName.text!!.isNotEmpty() &&
                binding.radioGroupSex.checkedRadioButtonId != -1)
                ElementsEditor().updateButtonState(binding.btnUpdate, true, context, true)
            else
                ElementsEditor().updateButtonState(binding.btnUpdate, false, context, true)
        } catch(e: java.lang.IllegalStateException) {}
    }

    // Function to connect with the api
    private fun updateData(name: String, sex: String, age: Int){
        // Deactivating clickable
        (parentFragment as NavBarFragment).changeNavBarButtonsClickable(false)

        // Launching room database connection
        MainScope().launch {
            try {
                // Creating connection to database
                val userDao = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "user")
                    .fallbackToDestructiveMigration().build().userDao()
                val user = userDao.getUser()[0]

                // Getting color according of theme
                val btnColor = ElementsEditor().getColor(context, R.attr.btn_background_red)
                val textColor = ElementsEditor().getColor(context, R.attr.btn_text_color_red)

                if(name == user.name && sex == user.sex && age == user.age) {
                    ValidationsDialogsRequests().snackBar(requireView(), btnColor, textColor,
                        getString(R.string.snackbar_error_update_data), requireContext())

                    (parentFragment as NavBarFragment).changeNavBarButtonsClickable(true)
                } else {
                    // Arguments to Post Request
                    val formBody: RequestBody = FormBody.Builder()
                        .add("name", name)
                        .add("edad", age.toString())
                        .add("sex", sex)
                        .build()

                    // Making http request
                    val request = Request.Builder()
                        .url("https://apiskydelight.herokuapp.com/usuarios/actualizar-informacion/")
                        .put(formBody)
                        .addHeader("Authorization", "Bearer " + user.token)
                        .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                        .build()

                    ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(), requireActivity(),
                        getString(R.string.btn_update), binding.btnUpdate, binding.btnCancel, null, null,
                        binding.progressBar, 404, getString(R.string.snackbar_error_recover), (parentFragment as NavBarFragment))
                    {
                        // Launching room database connection
                        MainScope().launch {
                            try {
                                // Updating user info in local database
                                userDao.updateUser(User(user.email, name, sex, age, user.token, user.refresh))

                                // Getting color according of theme
                                val buttonColor = ElementsEditor().getColor(context, R.attr.btn_background_green)
                                val txtColor = ElementsEditor().getColor(context, R.attr.btn_text_color_green)

                                // Showing succesful dialog
                                ValidationsDialogsRequests().snackBarOnUIThread(
                                    getString(R.string.snackbar_success_update_data), null, requireView(), buttonColor,
                                    txtColor, requireActivity(), requireContext(), null, null, null, null,
                                    null, null) {
                                    // Fragment enters from right
                                    (parentFragment as NavBarFragment).updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
                                }
                            } catch(e: java.lang.IllegalStateException) {}
                        }
                    }
                }
            } catch(e: java.lang.IllegalStateException) {}
        }
    }

    // Function to connect with the database
    private fun showUserData(){
        // Launching room database connection
        MainScope().launch {
            try {
                // Creating connection to database
                val user = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "user")
                    .fallbackToDestructiveMigration().build().userDao().getUser()[0]

                // Setting screen data
                binding.FieldEmail.text = user.email
                binding.editTxtName.setText(user.name)
                binding.numberPickerAge.value = user.age

                if (binding.btnMale.text.toString() == user.sex)
                    binding.btnMale.isChecked = true
                else
                    binding.btnFemale.isChecked = true
            } catch(e: java.lang.IllegalStateException) {}
        }
    }
}