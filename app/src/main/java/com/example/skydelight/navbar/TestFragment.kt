package com.example.skydelight.navbar

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.custom.ViewPageAdapter
import com.example.skydelight.databinding.FragmentNavbarTestBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TEST_PARAM = "test"

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

        // Loading pictures on view pager and connecting it with dots tab layout
        val imagesArray = arrayOf(R.drawable.wallpaper_beach, R.drawable.wallpaper_wingsuit,
            R.drawable.wallpaper_beach, R.drawable.wallpaper_night)
        val viewPagerAdapter = ViewPageAdapter(requireContext(), imagesArray)
        binding.viewPagerMain.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPagerMain, true)

        // SISCO
        binding.btnStart.setOnClickListener { updateTestCalendar(1) }

        // Pictures Actions
        binding.viewPagerMain.addOnPageChangeListener ( object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when (position) {
                    // SISCO
                    0 -> {
                        binding.txtSubtitle.text = getString(R.string.test_sisco)
                        binding.txtDescription.text = getString(R.string.test_sisco_description)
                        binding.txtNumberOfQuestions.text = getString(R.string.test_sisco_number)
                        binding.btnStart.setOnClickListener { updateTestCalendar(1) }
                        updateColors(R.attr.btn_text_color_green)
                    }
                    // SVQ
                    1 -> {
                        binding.txtSubtitle.text = getString(R.string.test_svq)
                        binding.txtDescription.text = getString(R.string.test_svq_description)
                        binding.txtNumberOfQuestions.text = getString(R.string.test_svq_number)
                        binding.btnStart.setOnClickListener { updateTestCalendar(2) }
                        updateColors(R.attr.btn_text_color_blue)
                    }
                    // PSS
                    2 -> {
                        binding.txtSubtitle.text = getString(R.string.test_pss)
                        binding.txtDescription.text = getString(R.string.test_pss_description)
                        binding.txtNumberOfQuestions.text = getString(R.string.test_pss_number)
                        binding.btnStart.setOnClickListener { updateTestCalendar(3) }
                        updateColors(R.attr.btn_text_color_yellow)
                    }
                    // SVS
                    3 -> {
                        binding.txtSubtitle.text = getString(R.string.test_svs)
                        binding.txtDescription.text = getString(R.string.test_svs_description)
                        binding.txtNumberOfQuestions.text = getString(R.string.test_svs_number)
                        binding.btnStart.setOnClickListener { updateTestCalendar(4) }
                        updateColors(R.attr.btn_text_color_red)
                    }
                }
            }
        })
    }

    private fun updateColors(resource: Int) {
        // Getting reference to resource color
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(resource, typedValue, true)
        val textColor = typedValue.data

        // Changing colors
        val elementsArray = arrayOf(binding.txtTitle, binding.txtSubtitle,
            binding.txtNumberOfQuestions, binding.txtDescription, binding.btnStart)

        for(element in elementsArray){
            element.setTextColor(textColor)
            element.setShadowLayer(5f,0f, 0f, textColor)
        }

        // Changing button design
        (binding.btnStart as MaterialButton).strokeColor = ColorStateList.valueOf(textColor)
        (binding.btnStart as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)
        binding.tabLayout.tabRippleColor = ColorStateList.valueOf(textColor)
        binding.progressBar.indeterminateTintList = ColorStateList.valueOf(textColor)
    }

    private fun updateTestCalendar(testNumber: Int){
        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao()
            val user = userDao.getUser()[0]

            // Choosing api url
            val apiUrl = when(testNumber) {
                // SISCO Test
                1 -> "https://apiskydelight.herokuapp.com/api/lista-testcisco-personal/"
                // SVQ Test
                2 -> "https://apiskydelight.herokuapp.com/api/lista-testsqv-personal/"
                // PSS Test
                3 -> "https://apiskydelight.herokuapp.com/api/lista-testpss-personal/"
                // SVS Test
                else -> "https://apiskydelight.herokuapp.com/api/lista-testsvs-personal/"
            }

            // Making http request
            val request = Request.Builder()
                .url(apiUrl)
                .post(FormBody.Builder().add("email", user.email).build())
                .addHeader("Authorization", "Bearer " + user.token)
                .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                .build()

            ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(), requireActivity(),
                getString(R.string.btn_start), binding.btnStart, null, null, null,
                binding.progressBar, null,null, (parentFragment as NavBarFragment))
            {
                val testData = JSONObject(it).getString("data")

                if(testData != "[]"){
                    val lastTest = testData.substring(testData.lastIndexOf("{"), testData.lastIndexOf("}")+1)
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
                var isLessThanOneDay = false
                if(testCalendar.year == LocalDateTime.now().year &&
                    testCalendar.month == LocalDateTime.now().month){
                    if(testCalendar.dayOfMonth == LocalDateTime.now().dayOfMonth) {
                        isSameDay = true
                    } else if(LocalDateTime.now().dayOfMonth - testCalendar.dayOfMonth == 1
                        && LocalDateTime.now().hour - testCalendar.hour < 0){
                        isLessThanOneDay = true
                    }
                    else if(LocalDateTime.now().dayOfMonth - testCalendar.dayOfMonth == 1
                        && LocalDateTime.now().hour - testCalendar.hour == 0
                        && LocalDateTime.now().minute - testCalendar.minute < 0){
                        isLessThanOneDay = true
                    }
                    else if(LocalDateTime.now().dayOfMonth - testCalendar.dayOfMonth == 1
                        && LocalDateTime.now().hour - testCalendar.hour == 0
                        && LocalDateTime.now().minute - testCalendar.minute == 0
                        && LocalDateTime.now().second - testCalendar.second < 0){
                        isLessThanOneDay = true
                    }
                }

                if(isSameDay || isLessThanOneDay){
                    // Calculating time left to answer the test again
                    var hour = if (testCalendar.hour > LocalDateTime.now().hour)
                            (testCalendar.hour - LocalDateTime.now().hour) else (LocalDateTime.now().hour - testCalendar.hour)

                    var minute = if (testCalendar.minute > LocalDateTime.now().minute)
                        (testCalendar.minute - LocalDateTime.now().minute) else (LocalDateTime.now().minute - testCalendar.minute)

                    var second = if (testCalendar.second > LocalDateTime.now().second)
                        (testCalendar.second - LocalDateTime.now().second) else (LocalDateTime.now().second - testCalendar.second)

                    if(isSameDay) {
                        hour = 23 - hour
                        minute = 59 - minute
                        second = 59 - second
                    }

                    // Showing introduction for the user
                    val dialog = MaterialAlertDialogBuilder(findNavController().context)
                        .setTitle("¡Espera!")
                        .setMessage("\nNo puedes responder más de una vez la prueba de estrés $testName en un lapso de 24 horas\n\n" +
                                "Debes esperar $hour hora(s) con $minute minuto(s) y $second segundo(s) para responder de nuevo\n")
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
                val fragment = TestAnswerFragment()
                fragment.arguments = bundleOf(TEST_PARAM to testNumber)

                // Fragment enters from right
                (parentFragment as NavBarFragment).updateNavBarHost(fragment, R.id.navbar_test_answer_fragment, true)
            }
        }
    }
}