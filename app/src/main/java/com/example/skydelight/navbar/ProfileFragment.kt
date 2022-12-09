package com.example.skydelight.navbar

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request

private const val URL_PARAM = "url"

class ProfileFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarProfileBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarProfileBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_profile, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Changing to updating data fragment
        binding.btnUpdateAccount.setOnClickListener {
            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(
                ProfileDataFragment(), R.id.navbar_profile_data_fragment, true)
        }

        // Changing to updating password fragment
        binding.btnChangePassword.setOnClickListener {
            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(
                ProfilePasswordFragment(), R.id.navbar_profile_password_fragment, true)
        }

        // Returning to the start screen fragment
        binding.btnCloseSession.setOnClickListener {
            MaterialAlertDialogBuilder(findNavController().context)
                .setTitle("¡No te Vayas!")
                .setMessage("¿Realmente Quieres Cerrar Sesión?")
                .setCancelable(false)
                .setNeutralButton("¡No!"){ dialog, _ -> dialog.dismiss() }
                .setPositiveButton("¡Sí!"){ dialog, _ ->
                    dialog.dismiss()

                    // Launching room database connection
                    MainScope().launch {
                        // Cleaning database and changing to start screen fragment
                        Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                            .fallbackToDestructiveMigration().build().userDao().deleteUsers()
                        findNavController().navigate(R.id.action_navBar_to_startScreen)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }.show()
        }

        // Returning to the start screen fragment
        binding.btnDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(findNavController().context)
                .setTitle("¡Cuidado!")
                .setMessage("¿Realmente Quieres Eliminar tu Cuenta?")
                .setCancelable(false)
                .setNeutralButton("¡No!"){ dialog, _ -> dialog.dismiss() }
                .setPositiveButton("¡Sí!"){ dialog, _ ->
                    dialog.dismiss()

                    // Deactivating clickable
                    (parentFragment as NavBarFragment).changeNavBarButtonsClickable(false)

                    // Launching room database connection
                    MainScope().launch {
                        // Creating connection to database
                        val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                            .fallbackToDestructiveMigration().build().userDao()
                        val user = userDao.getUser()[0]

                        // Making http request
                        val request = Request.Builder()
                            .url("https://apiskydelight.herokuapp.com/usuarios/eliminar-usuario/")
                            .put(FormBody.Builder().add("email", user.email).build())
                            .addHeader("Authorization", "Bearer " + user.token)
                            .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                            .build()

                        ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(),
                            requireActivity(), getString(R.string.profile_delete_account), binding.btnDeleteAccount,
                            binding.btnChangePassword, binding.btnCloseSession, binding.btnUpdateAccount, binding.progressBar,
                            404, getString(R.string.snackbar_error_recover), (parentFragment as NavBarFragment))
                        {
                            // Launching room database connection
                            MainScope().launch {
                                // Getting color according of theme
                                val btnColor = ElementsEditor().getColor(requireContext(), R.attr.btn_background_green)
                                val textColor = ElementsEditor().getColor(requireContext(), R.attr.btn_text_color_green)

                                ValidationsDialogsRequests().snackBar(requireView(), btnColor, textColor,
                                    getString(R.string.snackbar_success_delete_account), requireContext())

                                // Cleaning database and changing to start screen fragment
                                userDao.deleteUsers()
                                findNavController().navigate(R.id.action_navBar_to_startScreen)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            }
                        }
                    }
                }.show()
        }

        binding.btnPrivacy.setOnClickListener {
            // Setting parameters for the next fragment
            val fragment = ProfileWebFragment()
            fragment.arguments = bundleOf(URL_PARAM to
                    "https://sites.google.com/view/skydelight-poltica/página-principal")

            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(
                fragment, R.id.navbar_profile_web_fragment, true)
        }

        binding.btnAbout.setOnClickListener {
            // Setting parameters for the next fragment
            val fragment = ProfileWebFragment()
            fragment.arguments = bundleOf(URL_PARAM to
                    "https://sites.google.com/view/skydelight-acerca-de/página-principal")

            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(
                fragment, R.id.navbar_profile_web_fragment, true)
        }

        binding.btnTheme.setOnClickListener {
            if(!isDarkTheme(requireActivity())) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                updateSwitchTheme(true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                updateSwitchTheme(false)
            }
        }

        updateSwitchTheme(!isDarkTheme(requireActivity()))
    }

    private fun updateSwitchTheme(state: Boolean) {
        binding.btnTheme.isChecked = state

        val color = when(state) {
            true -> ElementsEditor().getColor(requireContext(),
                R.attr.btn_text_color_yellow)
            false -> ElementsEditor().getColor(requireContext(),
                com.google.android.material.R.attr.colorSecondaryVariant)
        }

        binding.btnTheme.setTextColor(ColorStateList.valueOf(color))
        binding.btnTheme.setShadowLayer(5f, 0f, 0f, color)
    }

    private fun isDarkTheme(activity: Activity): Boolean {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}