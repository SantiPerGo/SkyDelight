package com.example.skydelight.navbar

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.User
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarTestBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

private const val TEST_PARAM = "test"
private const val RESULT_PARAM = "test_result"

class TestFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarTestBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarTestBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_test, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Changing to test data fragment
        binding.btnSISCO.setOnClickListener { updateTestCalendar(1) }
        binding.btnSVQ.setOnClickListener { updateTestCalendar(2) }
        binding.btnPSS.setOnClickListener { updateTestCalendar(3) }
        binding.btnSVS.setOnClickListener { updateTestCalendar(4) }
    }

    private fun updateTestCalendar(testNumber: Int){
        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao()
            val user = userDao.getUser()[0]

            // Choosing api url
            val apiUrl: String
            val btnText: String
            val progressBar: ProgressBar
            val buttonArray = arrayListOf(binding.btnSISCO, binding.btnSVQ, binding.btnPSS, binding.btnSVS)

            when(testNumber) {
                // SISCO Test
                1 -> {
                    apiUrl = "https://apiskydelight.herokuapp.com/api/lista-testcisco-personal/"
                    btnText = binding.btnSISCO.text.toString()
                    progressBar = binding.progressBarSISCO
                }
                // SVQ Test
                2 -> {
                    apiUrl = "https://apiskydelight.herokuapp.com/api/lista-testsqv-personal/"
                    btnText = binding.btnSVQ.text.toString()
                    progressBar = binding.progressBarSVQ
                    buttonArray.remove(binding.btnSVQ)
                    buttonArray.add(0, binding.btnSVQ)
                }
                // PSS Test
                3 -> {
                    apiUrl = "https://apiskydelight.herokuapp.com/api/lista-testpss-personal/"
                    btnText = binding.btnPSS.text.toString()
                    progressBar = binding.progressBarPSS
                    buttonArray.remove(binding.btnPSS)
                    buttonArray.add(0, binding.btnPSS)
                }
                // SVS Test
                else -> {
                    apiUrl = "https://apiskydelight.herokuapp.com/api/lista-testsvs-personal/"
                    btnText = binding.btnSVS.text.toString()
                    progressBar = binding.progressBarSVS
                    buttonArray.remove(binding.btnSVS)
                    buttonArray.add(0, binding.btnSVS)
                }
            }

            // Making http request
            val request = Request.Builder()
                .url(apiUrl)
                .post(FormBody.Builder().add("email", user.email).build())
                .addHeader("Authorization", "Bearer " + user.token)
                .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                .build()

            ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
                btnText, buttonArray[0], buttonArray[1], buttonArray[2], buttonArray[3],
                progressBar, null,null, (parentFragment as NavBarFragment))
            {
                val testData = JSONObject(it).getString("data")

                if(testData != "[]"){
                    val lastTest = testData.substring(testData.indexOf("{"), testData.indexOf("}")+1)
                    var dateOfTest = JSONObject(lastTest).getString("created_at").replace("T", " ")
                    dateOfTest = dateOfTest.substring(0, dateOfTest.indexOf(".")+4)
                    when(testNumber){
                        1 -> user.siscoCalendar = dateOfTest
                        2 -> user.svqCalendar = dateOfTest
                        3 -> user.pssCalendar = dateOfTest
                        4 -> user.svsCalendar = dateOfTest
                    }

                    // Updating user info in local database
                    MainScope().launch { userDao.updateUser(user) }
                }

                changeToTestDataFragment(testNumber)
            }
        }
    }

    private fun changeToTestDataFragment(testNumber: Int){
        val testName = when (testNumber) {
            1 -> "SISCO"
            2 -> "SVQ"
            3 -> "PSS"
            4 -> "SVS"
            else -> null
        }

        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao()
            val user = userDao.getUser()[0]

            // Getting last date of test answered
            val date = when (testNumber) {
                1 -> user.siscoCalendar
                2 -> user.svqCalendar
                3 -> user.pssCalendar
                4 -> user.svsCalendar
                else -> null
            }

            // If user hasn't answered the test for 24 hours he/she can answer again
            var startTest = false
            if(date != null){
                val testCalendar = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

                // Validating if is the same day
                var isSameDay = false
                if(testCalendar.year == LocalDateTime.now().year &&
                    testCalendar.month == LocalDateTime.now().month){
                    if(testCalendar.dayOfMonth == LocalDateTime.now().dayOfMonth) {
                        isSameDay = true
                    } else if(LocalDateTime.now().dayOfMonth - testCalendar.dayOfMonth == 1
                        && LocalDateTime.now().hour - testCalendar.hour < 0){
                        isSameDay = true
                    }
                    else if(LocalDateTime.now().dayOfMonth - testCalendar.dayOfMonth == 1
                        && LocalDateTime.now().hour - testCalendar.hour == 0
                        && LocalDateTime.now().minute - testCalendar.minute < 0){
                        isSameDay = true
                    }
                    else if(LocalDateTime.now().dayOfMonth - testCalendar.dayOfMonth == 1
                        && LocalDateTime.now().hour - testCalendar.hour == 0
                        && LocalDateTime.now().minute - testCalendar.minute == 0
                        && LocalDateTime.now().second - testCalendar.second < 0){
                        isSameDay = true
                    }
                }

                if(isSameDay){
                    // Calculating time left to answer the test again
                    var hour = if (testCalendar.hour > LocalDateTime.now().hour)
                            (testCalendar.hour - LocalDateTime.now().hour) else (LocalDateTime.now().hour - testCalendar.hour)

                    var minute = if (testCalendar.minute > LocalDateTime.now().minute)
                        (testCalendar.minute - LocalDateTime.now().minute) else (LocalDateTime.now().minute - testCalendar.minute)

                    var second = if (testCalendar.second > LocalDateTime.now().second)
                        (testCalendar.second - LocalDateTime.now().second) else (LocalDateTime.now().second - testCalendar.second)

                    hour = 23 - hour
                    minute = 59 - minute
                    second = 59 - second

                    // Showing introduction for the user
                    val dialog = MaterialAlertDialogBuilder(findNavController().context)
                        .setTitle("¡Espera!")
                        .setMessage("\nNo puedes responder más de una vez la prueba de estrés $testName en un lapso de 24 horas\n\n" +
                                "Debes esperar $hour horas con $minute minutos y $second segundos para responder de nuevo\n")
                        .setNeutralButton("¡Entendido!") { dialog, _ -> dialog.dismiss() }
                        .show()

                    // Changing neutral button position to center
                    val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                    val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
                    layoutParams.weight = 10f
                    positiveButton.layoutParams = layoutParams
                } else{
                    startTest = true
                }
            } else{
                startTest = true
            }

            if(startTest){
                // Setting parameters for the next fragment
                val fragment = TestDataFragment()
                fragment.arguments = bundleOf(TEST_PARAM to testNumber, RESULT_PARAM to false)

                // Fragment enters from right
                (parentFragment as NavBarFragment).updateNavBarHost(fragment, R.id.navbar_test_data_fragment, true)
            }
        }
    }
}