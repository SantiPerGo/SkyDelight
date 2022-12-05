package com.example.skydelight.navbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject

class NavBarFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarBinding
    private var backEventState = true

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarBinding.inflate(inflater, container, false)

        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar, container, false)
    }

    // Variable to save actual fragment
    private var itemId = R.id.nav_home

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user has done the initial test
        initialTest()

        // Updating user token
        updateToken()

        // Setting back button
        backAction()

        // Setting fab buttons
        fabAction()

        // Setting navbar buttons
        navBarActions()
    }

    // Function to activate or deactivate navbar buttons
    fun changeNavBarButtonsClickable(state: Boolean){
        binding.navBar.menu.forEach { it.isEnabled = state }
        backEventState = state
    }

    fun updateFab(state: Boolean){
        binding.fab.isClickable = state

        if(state)
            binding.fab.animate().alpha(1f)
        else
            binding.fab.animate().alpha(0f)
    }

    fun updateFabReload(state: Boolean){
        binding.fabReload.isClickable = state

        if(state)
            binding.fabReload.animate().alpha(1f)
        else
            binding.fabReload.animate().alpha(0f)
    }

    // Function to change fragment of navbar host
    fun updateNavBarHost(fragment : Fragment, navId: Int, direction: Boolean){
        // Variable to change navigation bar fragments
        val transaction = childFragmentManager.beginTransaction()

        // Fragment enters from right
        if(direction)
            transaction.setCustomAnimations(
                R.anim.slide_from_right, R.anim.slide_exit_to_left,
                R.anim.slide_from_left, R.anim.slide_exit_to_right
            )
        // Fragment enters from left
        else
            transaction.setCustomAnimations(
                R.anim.slide_from_left, R.anim.slide_exit_to_right,
                R.anim.slide_from_right, R.anim.slide_exit_to_left
            )

        // Changing fragment and actual fragment id
        transaction.replace(binding.navbarHostFragment.id, fragment).commit()
        itemId = navId
        binding.navBar.selectedItemId = navId
    }

    private fun initialTest(){
        // Deactivating navbar
        changeNavBarButtonsClickable(false)
        updateFabReload(false)
        updateFab(false)

        // Creating tutorial dialogs

        // Explaining initial test
        val sixthDialog = MaterialAlertDialogBuilder(findNavController().context)
            .setTitle("¡Casi Terminamos!")
            .setMessage("\nAntes de empezar es necesario que respondas un pequeño test de 21 preguntas para " +
                    "conocer tu estrés académico = )\n")
            .setNeutralButton("¡Entendido!") { sixthDialog, _ -> sixthDialog.dismiss() }
            .setCancelable(false)

        // Explaining settings or profile screen
        val fifthDialog = MaterialAlertDialogBuilder(findNavController().context)
            .setTitle("Pantalla 4 de 4")
            .setMessage("\nEn la pantalla de configuración podrás actualizar tus datos personales (con excepción del correo " +
                    "electrónico), cerrar sesión o eliminar tu cuenta = )\n")
            .setNeutralButton("Siguiente") {
                fifthDialog, _ -> fifthDialog.dismiss()

                // Setting parameters for the next fragment
                val fragment = TestDataFragment()
                fragment.arguments = bundleOf("test" to 1, "btn_cancel" to true)

                // Changing fragment
                updateNavBarHost(fragment, R.id.navbar_test_data_fragment, false)
                itemId = R.id.nav_test
                binding.navBar.selectedItemId = R.id.nav_test
                updateDialogButton(sixthDialog.show())
            }
            .setCancelable(false)

        // Explaining games screen
        val fourthDialog = MaterialAlertDialogBuilder(findNavController().context)
            .setTitle("Pantalla 3 de 4")
            .setMessage("\nEn la pantalla de juegos podrás disfrutar de actividades relajantes o extremas con realidad " +
                    "aumentada (sólo si tu teléfono es compatible con ARCore) = )\n")
            .setNeutralButton("Siguiente") {
                fourthDialog, _ -> fourthDialog.dismiss()

                // Changing fragment
                updateNavBarHost(ProfileFragment(), R.id.navbar_profile_fragment, true)
                itemId = R.id.nav_profile
                binding.navBar.selectedItemId = R.id.nav_profile
                fifthDialog.show()
            }
            .setCancelable(false)

        // Explaining test screen
        val thirdDialog = MaterialAlertDialogBuilder(findNavController().context)
            .setTitle("Pantalla 2 de 4")
            .setMessage("\nEn la pantalla de test podrás encontrar diversos cuestionarios que puedes responder cada 24 horas " +
                    "para conocer tu estrés y tu vulnerabilidad a este = )\n")
            .setNeutralButton("Siguiente") {
                thirdDialog, _ -> thirdDialog.dismiss()

                // Changing fragment
                updateNavBarHost(GamesFragment(), R.id.navbar_games_fragment, true)
                itemId = R.id.nav_games
                binding.navBar.selectedItemId = R.id.nav_games
                fourthDialog.show()
            }
            .setCancelable(false)

        // Explaining home screen
        val secondDialog = MaterialAlertDialogBuilder(findNavController().context)
            .setTitle("Pantalla 1 de 4")
            .setMessage("\nEn la pantalla de inicio podrás encontrar diversas recomendaciones para prevención " +
                    "del estrés académico = )\n")
            .setNeutralButton("Siguiente") {
                secondDialog, _ -> secondDialog.dismiss()

                // Changing fragment
                updateNavBarHost(TestFragment(), R.id.navbar_test_fragment, true)
                itemId = R.id.nav_test
                binding.navBar.selectedItemId = R.id.nav_test
                thirdDialog.show()
            }
            .setCancelable(false)

        // Showing introduction for the user
        val firstDialog = MaterialAlertDialogBuilder(findNavController().context)
            .setTitle("¡Bienvenido(a)!")
            .setMessage("\nTe recomendamos completar un pequeño tutorial antes de usar la aplicación = )\n")
            .setNeutralButton("¡Empecemos!") {
                firstDialog, _ -> firstDialog.dismiss()
                secondDialog.show()
            }
            .setPositiveButton("Omitir") {
                firstDialog, _ -> firstDialog.dismiss()
                // Setting parameters for the next fragment
                val fragment = TestDataFragment()
                fragment.arguments = bundleOf("test" to 1, "btn_cancel" to true)

                // Changing fragment
                updateNavBarHost(fragment, R.id.navbar_test_data_fragment, true)
                itemId = R.id.nav_test
                binding.navBar.selectedItemId = R.id.nav_test
                updateDialogButton(sixthDialog.show())
            }
            .setCancelable(false)

        // Setting return buttons for dialogs

        fifthDialog.setPositiveButton("Anterior"){
                fifthDialog, _ -> fifthDialog.dismiss()
            // Changing fragment
            updateNavBarHost(GamesFragment(), R.id.navbar_games_fragment, false)
            itemId = R.id.nav_games
            binding.navBar.selectedItemId = R.id.nav_games
            fourthDialog.show()
        }
        fourthDialog.setPositiveButton("Anterior"){
                fourthDialog, _ -> fourthDialog.dismiss()
            thirdDialog.show()
            // Changing fragment
            updateNavBarHost(TestFragment(), R.id.navbar_test_fragment, false)
            itemId = R.id.nav_test
            binding.navBar.selectedItemId = R.id.nav_test
        }
        thirdDialog.setPositiveButton("Anterior"){
                thirdDialog, _ -> thirdDialog.dismiss()
            // Changing fragment
            updateNavBarHost(HomeFragment(), R.id.navbar_home_fragment, false)
            itemId = R.id.nav_home
            binding.navBar.selectedItemId = R.id.nav_home
            secondDialog.show()
        }
        secondDialog.setPositiveButton("Regresar"){
                secondDialog, _ -> secondDialog.dismiss()
            firstDialog.show()
        }

        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val user = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao().getUser()[0]

            // Making http request
            val request = Request.Builder().url("https://apiskydelight.herokuapp.com/api/lista-testcisco-personal/")
                .addHeader("Authorization", "Bearer " + user.token)
                .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                .post(FormBody.Builder().add("email", user.email).build()).build()

            ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(),
                requireActivity(),null, null, null, null, null,
                null, 500,null, null) {
                // Changing http body to json
                val arrayString = JSONObject(it).getString("data")

                // If user hasn't done the initial test:
                // + Showing app tutorial
                // + Applying initial test
                if(arrayString == "[]" && !user.initialTest){
                    // App tutorial
                    activity?.runOnUiThread{ firstDialog.show() }
                } else {
                    // Updating result
                    if(!user.initialTest)
                        user.initialTest = true

                    // Changing fragment and actual fragment id
                    childFragmentManager.beginTransaction().add(binding.navbarHostFragment.id, HomeFragment()).commit()
                    itemId = R.id.nav_home

                    // Activating navbar
                    activity?.runOnUiThread {
                        binding.navBar.selectedItemId = R.id.nav_home
                        changeNavBarButtonsClickable(true)
                        updateFabReload(true)
                        updateFab(true)
                    }

                    backEventState = true
                }
            }
        }
    }

    private fun updateDialogButton(dialog: AlertDialog){
        // Changing neutral button position to center
        val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        positiveButton.layoutParams = layoutParams
    }

    private fun updateToken(){
        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao()
            val user = userDao.getUser()[0]

            // Making http request
            val request = Request.Builder()
                .url("https://apiskydelight.herokuapp.com/usuarios/token/refrescar/")
                .post(FormBody.Builder().add("refresh", user.refresh).build())
                .build()

            ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
                null, null, null, null, null,
                null, null,null, null) {
                // Changing http body to json
                val json = JSONObject(it)

                // Launching room database connection
                MainScope().launch {
                    // Updating user info in local database
                    user.token = json.getString("access")
                    userDao.updateUser(user)
                }
            }
        }
    }

    private fun backAction(){
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(backEventState)
                    when (itemId) {
                        R.id.navbar_test_data_fragment ->{
                            updateNavBarHost(TestFragment(), R.id.nav_test, false)
                            updateFab(true)
                        }
                        R.id.navbar_test_answer_fragment ->
                            binding.navbarHostFragment.getFragment<TestAnswerFragment>().returnButtonValidation()
                        R.id.navbar_profile_data_fragment -> updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
                        R.id.navbar_profile_password_fragment -> updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
                        else -> requireActivity().moveTaskToBack(true)
                    }
                else
                    when (itemId) {
                        R.id.navbar_test_answer_fragment ->
                            binding.navbarHostFragment.getFragment<TestAnswerFragment>().returnButtonValidation()
                        else -> requireActivity().moveTaskToBack(true)
                    }
            }
        })
    }

    private fun fabAction(){
        binding.fabReload.setOnClickListener {
            // Hiding reload fab
            updateFabReload(false)

            // Updating home advice
            val fragment = binding.navbarHostFragment.getFragment() as HomeFragment
            // Showing reload fab after advice
            fragment.showAdvice{ updateFabReload(true) }
        }

        binding.fab.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(findNavController().context)
                .setNeutralButton("¡Entendido!") { dialog, _ -> dialog.dismiss() }

            // Showing introduction for the user
            when(itemId){
                R.id.nav_home -> {
                    dialog.setTitle("¿Para qué es la pantalla de inicio?")
                    dialog.setMessage("\nEn esta pantalla encontrarás consejos para prevenir el estrés (algunos personalizados " +
                            "si has respondido al menos una vez el test SVQ), dichos consejos han sido definidos por " +
                            "personas con conocimiento en el tema = )\n")
                }
                R.id.nav_test -> {
                    dialog.setTitle("¿Para qué son los test?")
                    dialog.setMessage("\nEn esta pantalla encontrarás cuestionarios que puedes responder cada 24 horas para " +
                            "conocer diversos aspectos relacionados con tu estrés, pero...\n\n¡No olvides visitar el sitio web " +
                            "para dar seguimiento a los resultados de las pruebas que respondas!\n")
                }
                R.id.nav_games -> {
                    dialog.setTitle("¿Para qué son los juegos?")
                    dialog.setMessage("\n¡Para divertirte con realidad aumentada! (sólo si tu teléfono es compatible con ARCore)\n")
                }
            }
            val showedDialog = dialog.show()

            // Changing neutral button position to center
            val positiveButton: Button = showedDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 10f
            positiveButton.layoutParams = layoutParams
        }
    }

    private fun navBarActions(){
        // Bottom navigation bar actions
        binding.navBar.setOnItemSelectedListener{
            // Showing fab
            updateFab(true)
            updateFabReload(false)

            when(it.itemId){
                R.id.nav_home -> {
                    // Showing reload fab
                    updateFabReload(true)

                    // Changing fragment if actual fragment is not the same
                    // Fragment enters from left
                    if(itemId != R.id.nav_home)
                        updateNavBarHost(HomeFragment(), R.id.nav_home, false)
                    true
                }
                R.id.nav_test -> {
                    // Changing fragment if actual fragment is not the same
                    when (itemId) {
                        // Fragment enters from right
                        R.id.nav_home -> updateNavBarHost(TestFragment(), R.id.nav_test, true)

                        // Fragment enters from left
                        R.id.nav_games -> updateNavBarHost(TestFragment(), R.id.nav_test, false)

                        // Fragment enters from left
                        R.id.nav_profile -> updateNavBarHost(TestFragment(), R.id.nav_test, false)
                        R.id.navbar_profile_data_fragment -> updateNavBarHost(TestFragment(), R.id.nav_test, false)
                        R.id.navbar_profile_password_fragment -> updateNavBarHost(TestFragment(), R.id.nav_test, false)
                    }
                    true
                }
                R.id.nav_games -> {
                    // Changing fragment if actual fragment is not the same
                    when (itemId) {
                        // Fragment enters from right
                        R.id.nav_home -> updateNavBarHost(GamesFragment(), R.id.nav_games, true)

                        // Fragment enters from right
                        R.id.nav_test -> updateNavBarHost(GamesFragment(), R.id.nav_games, true)
                        R.id.navbar_test_data_fragment -> updateNavBarHost(GamesFragment(), R.id.nav_games, true)
                        R.id.navbar_test_answer_fragment -> updateNavBarHost(GamesFragment(), R.id.nav_games, true)

                        // Fragment enters from left
                        R.id.nav_profile -> updateNavBarHost(GamesFragment(), R.id.nav_games, false)
                        R.id.navbar_profile_data_fragment -> updateNavBarHost(TestFragment(), R.id.nav_test, false)
                        R.id.navbar_profile_password_fragment -> updateNavBarHost(TestFragment(), R.id.nav_test, false)
                    }
                    true
                }
                R.id.nav_profile -> {
                    // Hiding fab
                    updateFab(false)

                    // Changing fragment if actual fragment is not the same
                    if(itemId != R.id.nav_profile)
                    // Fragment enters from left
                        if(itemId == R.id.navbar_profile_data_fragment || itemId == R.id.navbar_profile_password_fragment)
                            updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
                        // Fragment enters from right
                        else
                            updateNavBarHost(ProfileFragment(), R.id.nav_profile, true)

                    true
                }
                else -> false
            }
        }
    }
}