package com.example.skydelight.navbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.*
import com.example.skydelight.databinding.FragmentNavbarHomeBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.NullPointerException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarHomeBinding
    private lateinit var stressStatus: StressStatus

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarHomeBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_home, container, false)
    }

    private var isLoadingAdvice = false

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Loading pictures on view pager and connecting it with dots tab layout
        try {
            val loadingText = getString(R.string.loading)
            stressStatus = StressStatus(loadingText, loadingText, loadingText, loadingText,
                loadingText, loadingText, ElementsEditor().getDrawable(requireContext(), R.attr.heart_relaxed))
            binding.stressStatus = stressStatus

            binding.viewPagerMain.adapter = ViewPageAdapter(requireContext(), null, 2)
            binding.tabLayout.setupWithViewPager(binding.viewPagerMain, true)
        } catch(e: java.lang.IllegalStateException) {}

        // Enabling links
        Linkify.addLinks(binding.txtCredits, Linkify.WEB_URLS)
        binding.txtCredits.movementMethod = LinkMovementMethod.getInstance()

        // Showing initial random advice with reload icon
        showAdvice()

        // Getting last test dates
        for(testNumber in 1..4)
            updateTestCalendar(testNumber)

        // Pictures Actions
        var isAdviceLayout = true
        binding.viewPagerMain.addOnPageChangeListener ( object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // Change to Status View
                if(isAdviceLayout) {
                    binding.adviceLayout.visibility = View.GONE
                    binding.statusLayout.visibility = View.VISIBLE
                    isAdviceLayout = false

                    // Hide reload if isn't loading advice
                    if(!isLoadingAdvice)
                        updateImgReloadFromParent(false)
                }
                // Change to Advice View
                else {
                    binding.adviceLayout.visibility = View.VISIBLE
                    binding.statusLayout.visibility = View.GONE
                    isAdviceLayout = true

                    // Show reload if isn't loading advice
                    if(!isLoadingAdvice)
                        updateImgReloadFromParent(true)
                }
            }
        })
    }

    private fun updateImgReloadFromParent(state: Boolean) {
        try {
            if(parentFragment is NavBarFragment)
                (parentFragment as NavBarFragment).updateImgReload(state)
            else {
                val hostFragment = parentFragment as NavHostFragment
                (hostFragment.parentFragment as NavBarFragment).updateImgReload(state)
            }
        } catch (e: NullPointerException) {}
    }

    // Function to connect with the api
    fun showAdvice() {
        // Changing texts and hiding image reload
        isLoadingAdvice = true
        updateImgReloadFromParent(false)
        binding.txtTitle.text = getString(R.string.loading)
        binding.textView.text = getString(R.string.loading)
        binding.txtCredits.text = getString(R.string.loading)

        // Launching room database connection
        MainScope().launch {
            try {
                // Creating connection to database
                val userDao = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "user")
                    .fallbackToDestructiveMigration().build().userDao()
                val user = userDao.getUser()[0]

                // Making http request
                var request = Request.Builder().url("https://apiskydelight.herokuapp.com/api/lista-testsqv-personal/")
                    .addHeader("Authorization", "Bearer " + user.token)
                    .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                    .post(FormBody.Builder().add("email", user.email).build()).build()

                if(isAdded) {
                    ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(),
                        requireActivity(),null, null, null, null, null,
                        null, 500,null, null) {
                        // Changing http body to json
                        val arrayString = JSONObject(it).getString("data")

                        // Arguments to Post Request
                        val formBody = FormBody.Builder()

                        // If user has answered at least 1 SVQ test
                        if(arrayString != "[]" && user.advice) {
                            // Getting last SVQ test answered
                            val arrayObject = JSONArray(arrayString).getJSONObject(JSONArray(arrayString).length()-1)
                            val questionsList = ArrayList<Int>()

                            // Saving bad answers from user
                            for(i in 1..20)
                                if(arrayObject.getInt("pregunta$i") < 4)
                                    questionsList.add(i)

                            // Preparing selected answers to http petition
                            if(questionsList.size < 20)
                                formBody.add("type_advice", "especifico")
                                        .add("list_excluded", questionsList.toString())
                            else
                                formBody.add("type_advice", "general")
                                        .add("list_excluded", "[]")
                        } else
                            formBody.add("type_advice", "general")
                                    .add("list_excluded", "[]")

                        // Making http request
                        request = Request.Builder().url("https://apiskydelight.herokuapp.com/api/consejo")
                            .post(formBody.build()).build()

                        // Toggle advice boolean
                        user.advice = !user.advice

                        // Launching room database connection
                        MainScope().launch { userDao.updateUser(user) }

                        if(isAdded) {
                            try {
                                ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(),
                                    requireActivity(),null, null, null, null, null,
                                    null, 500,null, null) {
                                    // Changing http body to json and changing advice text
                                    MainScope().launch {
                                        binding.txtTitle.text = "Recomendación\n${JSONObject(it).getString("id")} de 50"
                                        binding.textView.text = JSONObject(it).getString("consejo")
                                        isLoadingAdvice = false

                                        // Show reload only on advice layout
                                        if(binding.adviceLayout.visibility == View.VISIBLE)
                                            updateImgReloadFromParent(true)

                                        // Show random background
                                        showBackground()
                                    }
                                }
                            } catch(e: IllegalStateException) {}
                        }
                    }
                }
            } catch(e: java.lang.IllegalStateException) {}
        }
    }

    // Getting wallpaper from pexels API
    private fun showBackground() {
        val pageNumber = (1..8000).shuffled().last()
        val request = Request.Builder()
            .url("https://api.pexels.com/v1/search?query=nature wallpaper&orientation=portrait&per_page=1&size=small&page=$pageNumber")
            .addHeader("Authorization", BuildConfig.API_KEY_PEXELS).get().build()

        try {
            ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(),
                requireActivity(),null, null, null, null, null,
                null, null,null, null) {

                // Getting data
                val imageUrlData = JSONObject(it).getString("photos")

                // Getting image url
                val imageUrl = imageUrlData.substringAfter("\"portrait\":\"")
                    .substringBefore("\"").replace("\\", "")

                // Getting photographer name
                val photographerName = imageUrlData.substringAfter("\"photographer\":\"")
                    .substringBefore("\"")

                // Getting photographer profile link
                val photographerProfile = imageUrlData.substringAfter("\"photographer_url\":\"")
                    .substringBefore("\"").replace("\\", "")

                // Creating photo, pexels and photographer links
                val photoUrl = "<a href=\"$imageUrl\">Photo</a>"
                val pexelsUrl = "<a href=\"https://www.pexels.com/\">Pexels</a>"
                val photographerUrl = "<a href=\"$photographerProfile\">$photographerName</a>"

                // Loading image
                MainScope().launch {
                    try {
                        // Showing photo, photographer and pexels links
                        binding.txtCredits.text = Html.fromHtml("$photoUrl by $photographerUrl\non $pexelsUrl",
                            Html.FROM_HTML_MODE_COMPACT)
                        binding.txtCredits.setLinkTextColor(ElementsEditor().getColor(context,
                            com.google.android.material.R.attr.colorSecondaryVariant))

                        // Loading image from api
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true)
                            .dontAnimate()
                            .dontTransform()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .priority(Priority.IMMEDIATE)
                            .format(DecodeFormat.DEFAULT)
                            .into(binding.imgBackground)
                    } catch(e: java.lang.IllegalStateException) {}
                }
            }
        } catch(e: java.lang.IllegalStateException) {}
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

                ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(),
                    requireActivity(), null, null, null, null, null,
                    null, null,null, null)
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

                        // Show user stress status
                        showStressStatus()
                    }
                }
            } catch(e: java.lang.IllegalStateException) {}
        }
    }

    private fun showStressStatus() {
        // Launching room database connection
        MainScope().launch {
            try {
                // Creating connection to database
                val userDao = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "user")
                    .fallbackToDestructiveMigration().build().userDao()
                val user = userDao.getUser()[0]

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")

                if(user.siscoCalendar != null && user.pssCalendar != null) {
                    val stressTests = arrayOf(LocalDateTime.parse(user.siscoCalendar ?: "", formatter),
                        LocalDateTime.parse(user.pssCalendar ?: "", formatter))

                    val maxStressTestDateTime: LocalDateTime? =
                        stressTests.maxByOrNull { item -> item.toEpochSecond(ZoneOffset.UTC) }

                    val stressDate = maxStressTestDateTime.toString().replace("T", " ")

                    if(stressDate == user.siscoCalendar)
                        showTestResult(1, stressDate)
                    else
                        showTestResult(3, stressDate)
                } else if(user.siscoCalendar != null || user.pssCalendar != null) {
                    val stressDate = LocalDateTime.parse(user.siscoCalendar ?: user.pssCalendar,
                        formatter).toString().replace("T", " ")

                    if(stressDate == user.siscoCalendar)
                        showTestResult(1, stressDate)
                    else
                        showTestResult(3, stressDate)
                } else {
                    stressStatus.status = getString(R.string.home_status_general, "Desconocido")
                    stressStatus.stress = getString(R.string.home_status_stress_test, "Ninguno")
                    stressStatus.stressResult = getString(R.string.home_status_stress_result, "Desconocido")
                }

                if(user.svqCalendar != null && user.svsCalendar != null) {
                    val vulnerabilityTests = arrayOf(LocalDateTime.parse(user.svqCalendar ?: "", formatter),
                        LocalDateTime.parse(user.svsCalendar ?: "", formatter))

                    val maxVulnerabilityTestDateTime: LocalDateTime? =
                        vulnerabilityTests.maxByOrNull { item -> item.toEpochSecond(ZoneOffset.UTC) }

                    val vulnerabilityDate = maxVulnerabilityTestDateTime.toString().replace("T", " ")

                    if(vulnerabilityDate == user.svqCalendar)
                        showTestResult(2, vulnerabilityDate)
                    else
                        showTestResult(4, vulnerabilityDate)
                } else if(user.svqCalendar != null || user.svsCalendar != null) {
                    val vulnerabilityDate = LocalDateTime.parse(user.svqCalendar ?: user.svsCalendar,
                        formatter).toString().replace("T", " ")

                    if(vulnerabilityDate == user.svqCalendar)
                        showTestResult(2, vulnerabilityDate)
                    else
                        showTestResult(4, vulnerabilityDate)
                } else {
                    stressStatus.risk = getString(R.string.home_status_risk, "Desconocido")
                    stressStatus.vulnerability = getString(R.string.home_status_vulnerable_test, "Ninguno")
                    stressStatus.vulnerabilityResult = getString(R.string.home_status_vulnerable_result, "Desconocido")
                }

                binding.stressStatus = stressStatus
            } catch(e: java.lang.IllegalStateException) {}
        }
    }

    private fun showTestResult(testNumber: Int, testDate: String) {
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

                ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(),
                    requireActivity(), null, null, null, null, null,
                    null, null,null, null)
                {
                    try {
                        // Getting data
                        val testData = JSONObject(it).getString("data")

                        // Saving string representation of tests in array
                        val testArray = testData.substring(2, testData.length-2).split("},{")

                        // Saving date format of tests in array
                        var dateObject: JSONObject
                        var dateOfTest: String
                        var testResult: String
                        var statusOrRisk: String
                        var testScore: Float
                        run breaker@ { testArray.forEach { item ->
                            // Casting strings into jsons
                            dateObject = JSONObject("{$item}")

                            // Getting date of test and preparing date format
                            dateOfTest = dateObject.getString("created_at").replace("T", " ")
                            testScore = dateObject.getString("total").toFloat()

                            if(dateOfTest == testDate) {
                                statusOrRisk = scoreToStatusOrRisk(testNumber, testScore)
                                testResult = floatToPercentage(testNumber, testScore)

                                MainScope().launch {
                                    try {
                                        updateStressRiskColors(testNumber, testScore, requireContext())
                                        when (testNumber) {
                                            // SISCO Test
                                            1 -> {
                                                stressStatus.img = scoreToImage(testNumber, testScore, requireContext())
                                                stressStatus.status = getString(R.string.home_status_general, statusOrRisk)
                                                stressStatus.stress = getString(R.string.home_status_stress_test, "SISCO")
                                                stressStatus.stressResult = getString(R.string.home_status_stress_result, testResult)
                                            }
                                            // SVQ Test
                                            2 -> {
                                                stressStatus.risk = getString(R.string.home_status_risk, statusOrRisk)
                                                stressStatus.vulnerability = getString(R.string.home_status_vulnerable_test, "SVQ")
                                                stressStatus.vulnerabilityResult = getString(R.string.home_status_vulnerable_result, testResult)
                                            }
                                            // PSS Test
                                            3 -> {
                                                stressStatus.img = scoreToImage(testNumber, testScore, requireContext())
                                                stressStatus.status = getString(R.string.home_status_general, statusOrRisk)
                                                stressStatus.stress = getString(R.string.home_status_stress_test, "PSS")
                                                stressStatus.stressResult = getString(R.string.home_status_stress_result, testResult)
                                            }
                                            // SVS Test
                                            else -> {
                                                stressStatus.risk = getString(R.string.home_status_risk, statusOrRisk)
                                                stressStatus.vulnerability = getString(R.string.home_status_vulnerable_test, "SVS")
                                                stressStatus.vulnerabilityResult = getString(R.string.home_status_vulnerable_result, testResult)
                                            }
                                        }
                                        binding.stressStatus = stressStatus
                                    } catch(e: java.lang.IllegalStateException) {}
                                }
                                return@breaker
                            }
                        } }
                    } catch(e: java.lang.IllegalStateException) {}
                }
            } catch(e: java.lang.IllegalStateException) {}
        }
    }

    private fun scoreToStatusOrRisk(testNumber: Int, result: Float): String {
        val explanation = when(testNumber){
            // SISCO Test
            1 -> {
                when {
                    result <= 33f -> getString(R.string.home_status_low)
                    result in 34f..66f -> getString(R.string.home_status_medium)
                    else -> getString(R.string.home_status_high)
                }
            }
            // SVQ Test
            2 -> {
                when {
                    result <= 49f -> getString(R.string.home_risk_none)
                    result in 50f..69f -> getString(R.string.home_risk_low)
                    result in 70f..95f -> getString(R.string.home_risk_medium)
                    else -> getString(R.string.home_risk_high)
                }
            }
            // PSS Test
            3 -> {
                when {
                    result <= 13f -> getString(R.string.home_status_low)
                    result in 14f..26f -> getString(R.string.home_status_medium)
                    else -> getString(R.string.home_status_high)
                }
            }
            // SVS Test
            else -> {
                when {
                    result < 1f -> getString(R.string.home_risk_none)
                    result < 2f -> getString(R.string.home_risk_low)
                    result < 3f -> getString(R.string.home_risk_medium)
                    else -> getString(R.string.home_risk_high)
                }
            }
        }
        return explanation
    }

    private fun floatToPercentage(testNumber: Int, score: Float): String {
        // Converting score to percentage according each test
        val scorePercentage = when(testNumber) {
            // SISCO Test
            1 -> (score * 100) / 105
            // SVQ Test
            2 -> ((score - 20) * 100) / 80
            // PSS Test
            3 -> (score * 100) / 40
            // SVS Test
            else -> (score  * 100) / 4
        }

        // Deleting decimals when necessary
        var scoreString = String.format("%.2f", scorePercentage)
        if(scorePercentage % 1f == 0f)
            scoreString = scorePercentage.toInt().toString()
        scoreString += "% de 100%"

        return scoreString
    }

    private fun scoreToImage(testNumber: Int, result: Float, context: Context): Drawable {
        val imageReference = when(testNumber){
            // SISCO Test
            1 -> {
                when {
                    result <= 33f -> R.attr.heart_laughing
                    result in 34f..66f -> R.attr.heart_happy
                    else -> R.attr.heart_sad
                }
            }
            // PSS Test
            else -> {
                when {
                    result <= 13f -> R.attr.heart_laughing
                    result in 14f..26f -> R.attr.heart_happy
                    else -> R.attr.heart_sad
                }
            }
        }
        return ElementsEditor().getDrawable(context, imageReference)
    }

    private fun updateStressRiskColors(testNumber: Int, result: Float, context: Context) {
        val stressTexts = arrayListOf(binding.txtStatus, binding.txtStress, binding.txtStressResult)
        val vulnerabilityTexts = arrayListOf(binding.txtRisk, binding.txtVulnerable, binding.txtVulnerableResult)
        val greenText = R.attr.btn_text_color_green
        val greenBtn = R.attr.btn_background_green
        val yellowText = R.attr.btn_text_color_yellow
        val yellowBtn = R.attr.btn_background_yellow
        val redText = R.attr.btn_text_color_red
        val redBtn = R.attr.btn_background_red

        when(testNumber){
            // SISCO Test
            1 -> {
                when {
                    result <= 33f -> ElementsEditor().updateColors(greenText,
                        greenBtn, context, stressTexts, null)
                    result in 34f..66f -> ElementsEditor().updateColors(yellowText,
                        yellowBtn, context, stressTexts, null)
                    else -> ElementsEditor().updateColors(redText,
                        redBtn, context, stressTexts, null)
                }
            }
            // SVQ Test
            2 -> {
                when {
                    result <= 49f -> ElementsEditor().updateColors(greenText,
                        greenBtn, context, vulnerabilityTexts, null)
                    result in 50f..69f -> ElementsEditor().updateColors(yellowText,
                        yellowBtn, context, vulnerabilityTexts, null)
                    result in 70f..95f -> ElementsEditor().updateColors(redText,
                        redBtn, context, vulnerabilityTexts, null)
                    else -> ElementsEditor().updateColors(redText,
                        redBtn, context, vulnerabilityTexts, null)
                }
            }
            // PSS Test
            3 -> {
                when {
                    result <= 13f -> ElementsEditor().updateColors(greenText,
                        greenBtn, context, stressTexts, null)
                    result in 14f..26f -> ElementsEditor().updateColors(yellowText,
                        yellowBtn, context, stressTexts, null)
                    else -> ElementsEditor().updateColors(redText,
                        redBtn, context, stressTexts, null)
                }
            }
            // SVS Test
            else -> {
                when {
                    result < 1f -> ElementsEditor().updateColors(greenText,
                        greenBtn, context, vulnerabilityTexts, null)
                    result < 2f -> ElementsEditor().updateColors(yellowText,
                        yellowBtn, context, vulnerabilityTexts, null)
                    result < 3f -> ElementsEditor().updateColors(yellowText,
                        yellowBtn, context, vulnerabilityTexts, null)
                    else -> ElementsEditor().updateColors(redText,
                        redBtn, context, vulnerabilityTexts, null)
                }
            }
        }
    }
}