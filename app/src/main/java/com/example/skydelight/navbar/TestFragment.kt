package com.example.skydelight.navbar

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.*
import com.example.skydelight.databinding.FragmentNavbarTestBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
        try {
            val imagesArray = arrayOf(R.drawable.wallpaper_heart, R.drawable.wallpaper_study,
                R.drawable.wallpaper_coast, R.drawable.wallpaper_woman)
            val viewPagerAdapter = ViewPageAdapter(requireContext(), imagesArray)
            binding.viewPagerMain.adapter = viewPagerAdapter
            binding.tabLayout.setupWithViewPager(binding.viewPagerMain, true)
        } catch(e: java.lang.IllegalStateException) {}

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
                        updateColors(R.attr.btn_text_color_green, R.attr.btn_background_green)
                    }
                    // SVQ
                    1 -> {
                        binding.txtSubtitle.text = getString(R.string.test_svq)
                        binding.txtDescription.text = getString(R.string.test_svq_description)
                        binding.txtNumberOfQuestions.text = getString(R.string.test_svq_number)
                        binding.btnStart.setOnClickListener { updateTestCalendar(2) }
                        updateColors(R.attr.btn_text_color_yellow, R.attr.btn_background_yellow)
                    }
                    // PSS
                    2 -> {
                        binding.txtSubtitle.text = getString(R.string.test_pss)
                        binding.txtDescription.text = getString(R.string.test_pss_description)
                        binding.txtNumberOfQuestions.text = getString(R.string.test_pss_number)
                        binding.btnStart.setOnClickListener { updateTestCalendar(3) }
                        updateColors(com.google.android.material.R.attr.colorSecondaryVariant,
                            com.google.android.material.R.attr.colorPrimaryVariant)
                    }
                    // SVS
                    3 -> {
                        binding.txtSubtitle.text = getString(R.string.test_svs)
                        binding.txtDescription.text = getString(R.string.test_svs_description)
                        binding.txtNumberOfQuestions.text = getString(R.string.test_svs_number)
                        binding.btnStart.setOnClickListener { updateTestCalendar(4) }
                        updateColors(R.attr.btn_text_color_red, R.attr.btn_background_red)
                    }
                }
            }
        })
    }

    private fun updateColors(textResource: Int, shadowResource: Int) {
        // Getting reference to resource color
        val textColor = ElementsEditor().getColor(context, textResource)

        // Changing button design
        binding.tabLayout.tabRippleColor = ColorStateList.valueOf(textColor)
        binding.progressBar.indeterminateTintList = ColorStateList.valueOf(textColor)

        // Changing colors
        val textsArray = arrayListOf(binding.txtTitle, binding.txtSubtitle,
            binding.txtNumberOfQuestions, binding.txtDescription)

        val buttonsArray = arrayListOf(binding.btnStart)

        // Updating colors
        ElementsEditor().updateColors(textResource,
            shadowResource, context, textsArray, buttonsArray)
    }

    private fun updateTestCalendar(testNumber: Int){
        // Launching room database connection
        MainScope().launch {
            try {
                // Creating connection to database
                val userDao = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "user")
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

                ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(), requireActivity(),
                    getString(R.string.btn_start), binding.btnStart, null, null, null,
                    binding.progressBar, null,null, (parentFragment as NavBarFragment))
                {
                    val testData = JSONObject(it).getString("data")

                    if(testData != "[]"){
                        // Saving string representation of tests in array
                        val testArray = testData.substring(2, testData.length-2).split("},{")

                        // Saving date format of tests in array
                        val testArrayDates = ArrayList<LocalDateTime>()
                        var dateObject: JSONObject
                        var dateOfTest: String
                        testArray.forEach { item ->
                            // Casting strings into jsons
                            dateObject = JSONObject("{$item}")

                            // Getting date of test and preparing date format
                            dateOfTest = dateObject.getString("created_at").replace("T", " ")

                            // Saving date of test as localDateTime object
                            testArrayDates.add(LocalDateTime.parse(dateOfTest,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")))
                        }

                        // Saving test with last date in database
                        val maxDateTime: LocalDateTime? =
                            testArrayDates.maxByOrNull { item -> item.toEpochSecond(ZoneOffset.UTC) }
                        when(testNumber){
                            1 -> user.siscoCalendar = maxDateTime.toString().replace("T", " ")
                            2 -> user.svqCalendar = maxDateTime.toString().replace("T", " ")
                            3 -> user.pssCalendar = maxDateTime.toString().replace("T", " ")
                            4 -> user.svsCalendar = maxDateTime.toString().replace("T", " ")
                        }

                        // Updating user info in local database
                        MainScope().launch { userDao.updateUser(user) }
                    }

                    changeToTestDataFragment(testNumber)
                }
            } catch(e: java.lang.IllegalStateException) {}
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
            try {
                // Creating connection to database
                val userDao = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "user")
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
                    val testCalendar = LocalDateTime.parse(date,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))

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
                        var fromDateTime = LocalDateTime.from(LocalDateTime.now())
                        val toDateTime = testCalendar.plusDays(1)

                        // Getting hours and adding it to get correct minutes
                        val hour = fromDateTime.until(toDateTime, ChronoUnit.HOURS)
                        fromDateTime = fromDateTime.plusHours(hour)

                        // Getting minutes and adding it to get correct seconds
                        val minute = fromDateTime.until(toDateTime, ChronoUnit.MINUTES)
                        fromDateTime = fromDateTime.plusMinutes(minute)

                        // Getting seconds
                        val second = fromDateTime.until(toDateTime, ChronoUnit.SECONDS)

                        // Showing introduction for the user
                        val dialog = CustomDialog(getString(R.string.test_time_error_title),
                            getString(R.string.test_time_error_description, testName, hour.toString(), minute.toString(),
                                second.toString()), R.attr.heart_sad, R.attr.fragment_background, requireContext())
                        dialog.firstButton(getString(R.string.test_btn_understand)) {}
                        dialog.show()
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
                    (parentFragment as NavBarFragment).updateNavBarHost(fragment,
                        R.id.navbar_test_answer_fragment, true)
                }
            } catch(e: java.lang.IllegalStateException) {}
        }
    }
}