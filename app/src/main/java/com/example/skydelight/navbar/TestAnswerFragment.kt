package com.example.skydelight.navbar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.CustomDialog
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarTestAnswerBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TEST_PARAM = "test"
private const val SCORE_PARAM = "score"
private const val STRESS_PARAM = "stress"
private const val BTNCANCEL_PARAM = "btn_cancel"

class TestAnswerFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarTestAnswerBinding
    private lateinit var questionAnswers: IntArray
    private lateinit var testQuestion: String
    private lateinit var testAnswer: String
    private var maxQuestionsNumber = 1
    private var questionNumber = 1

    // Variable to receive data from other fragments
    private var testNumber: Int? = null
    private var btnCancelState: Boolean? = null

    // Variable to change and save test questions
    private var min = 0f
    private var max = 10f
    private var answer = -1f

    // Getting data from other fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            testNumber = it.getInt(TEST_PARAM)
            btnCancelState = it.getBoolean(BTNCANCEL_PARAM)
        }
    }

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarTestAnswerBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_test, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Deactivating help fab
        (parentFragment as NavBarFragment).updateImgHelp(false)

        // Update text
        initVariables()

        binding.btnLeftArrow.setOnClickListener {
            try {
                when {
                    answer == min -> {
                        answer = -1f
                        updateAnswer(null, "test_initial_option")
                        ElementsEditor().updateButtonState(binding.btnNext, false, context, true)
                    }
                    answer > min -> {
                        answer -= 1f
                        updateAnswer(answer, null)
                    }
                }
                updateArrows()
            } catch(e: java.lang.IllegalStateException) {}
        }

        binding.btnRightArrow.setOnClickListener {
            try {
                when {
                    answer == -1f -> {
                        answer = min
                        updateAnswer(answer, null)
                        ElementsEditor().updateButtonState(binding.btnNext, true, context, true)
                    }
                    answer < max -> {
                        answer += 1
                        updateAnswer(answer, null)
                    }
                }
                updateArrows()
            } catch(e: java.lang.IllegalStateException) {}
        }

        // Changing to next question or fragment
        binding.btnNext.setOnClickListener { nextButtonActions() }

        // Changing to previous question or fragment
        binding.btnReturn.setOnClickListener { returnButtonValidation() }

        // Disable next button
        try { ElementsEditor().updateButtonState(binding.btnNext, false, context, true)
        } catch(e: java.lang.IllegalStateException) {}

        // Disable left arrow button
        updateArrows()
    }

    private fun updateArrows(){
        if(answer == -1f) {
            binding.btnLeftArrow.isClickable = false
            binding.btnLeftArrow.animate().alpha(0f)
        } else {
            binding.btnLeftArrow.isClickable = true
            binding.btnLeftArrow.animate().alpha(1f)
        }

        if(answer == max) {
            binding.btnRightArrow.isClickable = false
            binding.btnRightArrow.animate().alpha(0f)
        } else {
            binding.btnRightArrow.isClickable = true
            binding.btnRightArrow.animate().alpha(1f)
        }
    }

    private fun initVariables() {
        // Updating text
        when(testNumber){
            // SISCO Test
            1 -> { binding.txtQuestion.text = getString(R.string.test_sisco_question_1)
                testQuestion = "test_sisco_question_"
                testAnswer = "test_sisco_answer_"
                maxQuestionsNumber = 21
                questionAnswers = IntArray(21)
                max = 5f
            }
            // SVQ Test
            2 -> { binding.txtQuestion.text = getString(R.string.test_svq_question_1)
                testQuestion = "test_svq_question_"
                testAnswer = "test_svq_answer_"
                maxQuestionsNumber = 20
                questionAnswers = IntArray(20)
                min = 1f
                max = 5f
            }
            // PSS Test
            3 -> { binding.txtQuestion.text = getString(R.string.test_pss_question_1)
                testQuestion = "test_pss_question_"
                testAnswer = "test_pss_answer_"
                maxQuestionsNumber = 10
                questionAnswers = IntArray(10)
                max = 4f
            }
            // SVS Test
            4 -> { binding.txtQuestion.text = getString(R.string.test_svs_question_1)
                testQuestion = "test_svs_question_"
                testAnswer = "test_svs_answer_"
                maxQuestionsNumber = 20
                questionAnswers = IntArray(20)
                max = 4f
            }
        }

        // Changing title
        binding.txtTitle.text = "Pregunta $questionNumber de $maxQuestionsNumber"
    }

    private fun updateAnswer(answer: Float?, stringName: String?) {
        try {
            if(!requireActivity().isDestroyed) {
                if(stringName.isNullOrEmpty() && answer != null)
                    binding.txtAnswer.text = getString(requireActivity().resources.getIdentifier
                        (testAnswer+answer.toInt(), "string", requireContext().packageName))
                else if(stringName!!.isNotEmpty())
                    binding.txtAnswer.text = getString(requireActivity().resources.getIdentifier
                        (stringName, "string", requireContext().packageName))
            }
        } catch(e: java.lang.IllegalStateException) {}
    }

    private fun nextButtonActions() {
        try {
            // Saving answer
            questionAnswers[questionNumber - 1] = answer.toInt()

            if(questionNumber < maxQuestionsNumber){
                // Clearing answer
                // binding.slider.value = min
                answer = -1f
                updateArrows()
                updateAnswer(null, "test_initial_option")
                ElementsEditor().updateButtonState(binding.btnNext, false, context, true)

                // Changing text of button if it's first question
                if(questionNumber == 1)
                    binding.btnReturn.text = getString(R.string.btn_return)

                // Increasing number of question
                questionNumber = questionNumber.plus(1)

                // Changing text of button if it's last question
                if(questionNumber == maxQuestionsNumber)
                    binding.btnNext.text = getString(R.string.test_btn_end)

                // Changing to next question
                binding.txtTitle.text = "Pregunta $questionNumber de $maxQuestionsNumber"
                binding.txtQuestion.text = getString(requireActivity().resources.getIdentifier
                    (testQuestion+questionNumber,"string", requireContext().packageName))
            } else {
                binding.btnReturn.isClickable = false
                binding.btnNext.isClickable = false

                val dialog = CustomDialog(getString(R.string.test_time_error_title),
                    getString(R.string.test_finish), R.attr.heart_confused,
                    R.attr.fragment_background, requireContext(), false, true)
                dialog.firstButton(getString(R.string.test_cancel_btn_no)) {
                    binding.btnReturn.isClickable = true
                    binding.btnNext.isClickable = true
                }
                dialog.secondButton(getString(R.string.test_cancel_btn_yes)) {
                    // Updating text
                    var explanation = ""
                    var result = questionAnswers.sum().toFloat()
                    when(testNumber){
                        // SISCO Test
                        1 -> {
                            result = (questionAnswers.sum().toFloat() * 100) / 105
                            explanation = when {
                                result <= 33f -> getString(R.string.test_result_low)
                                result in 34f..66f -> getString(R.string.test_result_medium)
                                else -> getString(R.string.test_result_high)
                            }
                        }
                        // SVQ Test
                        2 -> {
                            explanation = when {
                                result <= 49f -> getString(R.string.test_result_vulnerable_low)
                                result in 50f..69f -> getString(R.string.test_result_vulnerable_medium)
                                result in 70f..95f -> getString(R.string.test_result_vulnerable_high)
                                else -> getString(R.string.test_result_vulnerable_extreme)
                            }
                            explanation += getString(R.string.test_svq_result)
                        }
                        // PSS Test
                        3 -> {
                            for(i in questionAnswers.indices)
                                if(i+1 == 4 || i+1 == 5 || i+1 == 7 || i+1 == 8)
                                    when {
                                        questionAnswers[i] == 0 -> questionAnswers[i] = 4
                                        questionAnswers[i] == 1 -> questionAnswers[i] = 3
                                        questionAnswers[i] == 2 -> questionAnswers[i] = 2
                                        questionAnswers[i] == 3 -> questionAnswers[i] = 1
                                        else -> questionAnswers[i] = 0
                                    }

                            result = questionAnswers.sum().toFloat()
                            explanation = when {
                                result <= 13f -> getString(R.string.test_result_low)
                                result in 14f..26f -> getString(R.string.test_result_medium)
                                else -> getString(R.string.test_result_high)
                            }
                        }
                        // SVS Test
                        4 -> {
                            result = questionAnswers.sum().toFloat() / 20
                            explanation = when {
                                result < 1f -> getString(R.string.test_result_vulnerable_low)
                                result < 2f -> getString(R.string.test_result_vulnerable_medium)
                                result < 3f -> getString(R.string.test_result_vulnerable_high)
                                else -> getString(R.string.test_result_vulnerable_extreme)
                            }
                        }
                    }
                    saveTest(explanation, result, questionAnswers)
                }
                dialog.show()
            }
        } catch(e: java.lang.IllegalStateException) {}
    }

    fun returnButtonValidation(){
        try {
            if(questionNumber == 1 && btnCancelState == true) {
                // Showing introduction for the user
                val dialog = CustomDialog(getString(R.string.test_tutorial_error_title),
                    getString(R.string.test_tutorial_error_description), R.attr.heart_happy,
                    R.attr.fragment_background, requireContext(), isMiniSize = true)
                dialog.firstButton(getString(R.string.test_btn_understand)) {}
                dialog.show()
            } else if(questionNumber == 1 && btnCancelState == false) {
                binding.btnReturn.isClickable = false
                binding.btnNext.isClickable = false

                val dialog = CustomDialog(getString(R.string.test_cancel_title),
                    getString(R.string.test_cancel_description), R.attr.heart_sad,
                    R.attr.fragment_background, requireContext(), false, true)
                dialog.firstButton(getString(R.string.test_cancel_btn_no)) {
                    binding.btnReturn.isClickable = true
                    binding.btnNext.isClickable = true
                }
                dialog.secondButton(getString(R.string.test_cancel_btn_yes)) {
                    // Fragment enters from right
                    (parentFragment as NavBarFragment).updateNavBarHost(
                        TestFragment(), R.id.nav_test, false)
                    (parentFragment as NavBarFragment).updateImgHelp(true)
                }
                dialog.show()
            } else {
                // Changing text of button if it's not last question
                if(questionNumber == maxQuestionsNumber)
                    binding.btnNext.text = getString(R.string.btn_next)

                // Decreasing number of question
                questionNumber = questionNumber.minus(1)

                // Changing text of button if it's first question
                if(questionNumber == 1)
                    binding.btnReturn.text = getString(R.string.btn_cancel)

                // Changing to previous question
                binding.txtTitle.text = "Pregunta $questionNumber de $maxQuestionsNumber"
                binding.txtQuestion.text = getString(requireActivity().resources.getIdentifier
                    (testQuestion+questionNumber, "string", requireContext().packageName))

                // Updating answer
                answer = questionAnswers[questionNumber-1].toFloat()
                updateArrows()
                updateAnswer(answer, null)
                ElementsEditor().updateButtonState(binding.btnNext, true, context, true)
            }
        } catch(e: java.lang.IllegalStateException) {}
    }

    private fun saveTest(explanation: String, result: Float, questionAnswers: IntArray){
        // Deactivating clickable
        (parentFragment as NavBarFragment).changeNavBarButtonsClickable(false)

        // Launching room database connection
        MainScope().launch {
            try {
                // Creating connection to database
                val userDao = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "user")
                    .fallbackToDestructiveMigration().build().userDao()
                val user = userDao.getUser()[0]

                // Arguments to Post Request
                val formBody = FormBody.Builder()
                formBody.add("usuario", user.email)
                for(i in questionAnswers.indices)
                    formBody.add("pregunta${i+1}", questionAnswers[i].toString())

                // Choosing api url
                val apiUrl = when(testNumber) {
                    // SISCO Test
                    1 -> "https://apiskydelight.herokuapp.com/api/crear-testcisco"
                    // SVQ Test
                    2 -> "https://apiskydelight.herokuapp.com/api/crear-testsqv"
                    // PSS Test
                    3 -> "https://apiskydelight.herokuapp.com/api/crear-testpss"
                    // SVS Test
                    else -> "https://apiskydelight.herokuapp.com/api/crear-testsvs"
                }

                // Making http request
                val request = Request.Builder()
                    .url(apiUrl)
                    .post(formBody.build())
                    .addHeader("Authorization", "Bearer " + user.token)
                    .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                    .build()


                ValidationsDialogsRequests().httpPetition(request, requireContext(), requireView(), requireActivity(),
                    getString(R.string.test_btn_end), binding.btnNext, binding.btnReturn, null, null,
                    binding.progressBar, null, null, (parentFragment as NavBarFragment))
                {
                    // Setting parameters for the next fragment
                    val bundle = bundleOf(TEST_PARAM to testNumber,
                        SCORE_PARAM to result, STRESS_PARAM to explanation)

                    val fragment = TestDataFragment()
                    fragment.arguments = bundle

                    // Fragment enters from right
                    (parentFragment as NavBarFragment).updateNavBarHost(fragment,
                        R.id.navbar_test_data_fragment, true)
                }
            } catch(e: java.lang.IllegalStateException) {}
        }
    }
}