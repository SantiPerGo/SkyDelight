package com.example.skydelight.initial

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.User
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentRegisterSecondBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

private const val NAME_PARAM = "name"
private const val AGE_PARAM = "age"
private const val SEX_PARAM = "sex"

class RegisterSecondFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding: FragmentRegisterSecondBinding

    // Variables to receive data from other fragments
    private var name: String? = null
    private var age: String? = null
    private var sex: String? = null

    // Getting data from other fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            name = it.getString(NAME_PARAM)
            age = it.getString(AGE_PARAM)
            sex = it.getString(SEX_PARAM)
        }
    }

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentRegisterSecondBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_register_second, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        binding.editTxtConfirmPassword.doOnTextChanged { _, _, _, _ ->
            if(binding.FieldConfirmPassword.error != null) binding.FieldConfirmPassword.error = null

            // Disable or enable login button
            validateInputsNotEmpty()
        }

        // Showing elements
        Handler(Looper.getMainLooper()).postDelayed({ elementsVisibility(true) }, 500)

        binding.btnCreateAccount.setOnClickListener{
            val email = binding.editTxtEmail.text.toString()
            val password = binding.editTxtPassword.text.toString()
            val confirmedPassword = binding.editTxtConfirmPassword.text.toString()

            if(ValidationsDialogsRequests().validateEmail(email, binding.FieldEmail)
                && ValidationsDialogsRequests().validatePassword(password, binding.FieldPassword)
                && ValidationsDialogsRequests().validateConfirmedPassword(password, confirmedPassword, binding.FieldConfirmPassword)){
                ElementsEditor().elementsClickableState(false,
                    null, arrayListOf(binding.btnCreateAccount, binding.btnReturn))
                createUser(email, password, name.toString(), sex.toString(), age.toString())
            }
        }

        // Returning to the register first fragment
        binding.btnReturn.setOnClickListener {
            binding.btnReturn.isClickable = false

            // Setting parameters for the next fragment
            val bundle = bundleOf(NAME_PARAM to name, SEX_PARAM to sex, AGE_PARAM to age)

            // Starting previous fragment
            elementsVisibility(false)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_registerSecond_to_registerFirst, bundle)
                findNavController().popBackStack(R.id.register_second_fragment, true)
            }, 500)
        }

        // Disable login button
        ElementsEditor().updateButtonState(binding.btnCreateAccount, false, requireContext(), false)
    }

    private fun validateInputsNotEmpty() {
        // Disable or enable login button
        if(binding.editTxtEmail.text!!.isNotEmpty() && binding.editTxtPassword.text!!.isNotEmpty()
            && binding.editTxtConfirmPassword.text!!.isNotEmpty())
            ElementsEditor().updateButtonState(binding.btnCreateAccount, true, requireContext(), false)
        else
            ElementsEditor().updateButtonState(binding.btnCreateAccount, false, requireContext(), false)
    }

    fun elementsVisibility(state: Boolean){
        val elementsArray = arrayOf(binding.txtTitle, binding.txtEmail, binding.FieldEmail,
            binding.txtPassword, binding.FieldPassword, binding.txtConfirmPassword,
            binding.FieldConfirmPassword, binding.btnCreateAccount, binding.btnReturn)

        for(element in elementsArray)
            if(state)
                element.animate().alpha(1f)
            else
                element.animate().alpha(0f)
    }

    // Function to connect with the api
    private fun createUser(email: String, password: String, name: String, sex: String, age: String) {
        /* Alternative to get api key
        val info: ApplicationInfo = findNavController().context.packageManager
            .getApplicationInfo(findNavController().context.packageName, PackageManager.GET_META_DATA)
        info.metaData.getString("com.google.android.geo.API_KEY").toString()*/

        // Arguments to Post Request
        var formBody: RequestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .add("name", name)
            .add("sex", sex)
            .add("edad", age)
            .build()

        // Making http request
        var request = Request.Builder()
            .url("https://apiskydelight.herokuapp.com/usuarios/crearusuario/")
            .post(formBody)
            .header("KEY-CLIENT", BuildConfig.API_KEY)
            .build()

        ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
            getString(R.string.registerScreen_btn_create), binding.btnCreateAccount, binding.btnReturn, null,
            null,  binding.progressBar,400, getString(R.string.snackbar_error_register),null)
        {
            // Arguments to Post Request
            formBody = FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build()

            // Making http request
            request = Request.Builder()
                .url("https://apiskydelight.herokuapp.com/usuarios/token/obtener/")
                .post(formBody)
                .header("KEY-CLIENT", BuildConfig.API_KEY)
                .build()

            ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
                getString(R.string.registerScreen_btn_create), binding.btnCreateAccount, binding.btnReturn, null,
                null,  binding.progressBar,null, null,null)
            { responseString: String ->
                // Changing http body to json
                val json = JSONObject(responseString)

                // Launching room database connection
                MainScope().launch {
                    // Creating connection to database
                    val userDao = Room.databaseBuilder(findNavController().context,
                        AppDatabase::class.java,"user").fallbackToDestructiveMigration().build().userDao()

                    // If user exists, we have to delete it
                    val user = userDao.getUser()
                    if (user.isNotEmpty())
                        userDao.deleteUser(user[0])

                    // Adding the new user to the database
                    userDao.insertUser(User(json.getString("user"),
                        json.getString("name"), json.getString("sex"),
                        json.getInt("age"), json.getString("access"),
                        json.getString("refresh")))

                    // Getting color according of theme
                    val btnColor = ElementsEditor().getColor(requireContext(), R.attr.btn_background_green)
                    val textColor = ElementsEditor().getColor(requireContext(), R.attr.btn_text_color_green)

                    ValidationsDialogsRequests().snackBarOnUIThread(
                        getString(R.string.snackbar_success_register), null, requireView(), btnColor, textColor,
                        requireActivity(), requireContext(), null, null, null, null,
                        null, null) {
                        elementsVisibility(false)
                        Handler(Looper.getMainLooper()).postDelayed({
                            findNavController().navigate(R.id.action_registerSecond_to_registerThird)
                        }, 500)
                    }
                }
            }
        }
    }
}