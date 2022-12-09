package com.example.skydelight.navbar

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarTestAnswerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var res: Resources
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
    @SuppressLint("SetTextI18n", "DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Deactivating help fab
        (parentFragment as NavBarFragment).updateImgHelp(false)

        // Access to resources
        res = requireContext().resources

        // Update text
        initVariables()

        binding.btnLeftArrow.setOnClickListener {
            if(answer == min) {
                answer = -1f
                updateAnswer(null, "test_initial_option")
                ElementsEditor().updateButtonState(binding.btnNext, false, requireContext(), true)
            } else if(answer > min) {
                answer -= 1f
                updateAnswer(answer, null)
            }
        }

        binding.btnRightArrow.setOnClickListener {
            if(answer == -1f) {
                answer = min
                updateAnswer(answer, null)
                ElementsEditor().updateButtonState(binding.btnNext, true, requireContext(), true)
            } else if(answer < max) {
                answer += 1
                updateAnswer(answer, null)
            }
        }

        // Changing to next question or fragment
        binding.btnNext.setOnClickListener { nextButtonActions() }

        // Changing to previous question or fragment
        binding.btnReturn.setOnClickListener { returnButtonValidation() }

        // Disable next button
        ElementsEditor().updateButtonState(binding.btnNext, false, requireContext(), true)
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
        if(stringName.isNullOrEmpty() && answer != null)
            binding.txtAnswer.text = getString(res.getIdentifier
                (testAnswer+answer.toInt(), "string", requireContext().packageName))
        else if(stringName!!.isNotEmpty())
            binding.txtAnswer.text = getString(res.getIdentifier
                (stringName, "string", requireContext().packageName))
    }

    private fun nextButtonActions() {
        // Saving answer
        questionAnswers[questionNumber - 1] = answer.toInt()

        if(questionNumber < maxQuestionsNumber){
            // Clearing answer
            // binding.slider.value = min
            answer = -1f
            updateAnswer(null, "test_initial_option")
            ElementsEditor().updateButtonState(binding.btnNext, false, requireContext(), true)

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
            binding.txtQuestion.text = getString(res.getIdentifier(testQuestion+questionNumber,
                "string", requireContext().packageName))
        } else {
            binding.btnReturn.isClickable = false
            binding.btnNext.isClickable = false

            MaterialAlertDialogBuilder(findNavController().context)
                .setTitle("¡Espera!")
                .setMessage("¿Realmente Quieres dar por Finalizada la Prueba?")
                .setCancelable(false)
                .setNeutralButton("¡No!"){ dialog, _ ->
                    dialog.dismiss()
                    binding.btnReturn.isClickable = true
                    binding.btnNext.isClickable = true
                }
                .setPositiveButton("¡Sí!"){ _, _ ->
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
                            result = ((questionAnswers.sum().toFloat() - 20) * 100) / 80
                            explanation = when {
                                questionAnswers.sum() <= 49 -> getString(R.string.test_result_vulnerable_low)
                                questionAnswers.sum() in 50..69 -> getString(R.string.test_result_vulnerable_medium)
                                questionAnswers.sum() in 70..95 -> getString(R.string.test_result_vulnerable_high)
                                else -> getString(R.string.test_result_vulnerable_extreme)
                            }
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

                            explanation = when {
                                questionAnswers.sum() <= 13 -> getString(R.string.test_result_low)
                                questionAnswers.sum() in 14..26 -> getString(R.string.test_result_medium)
                                else -> getString(R.string.test_result_high)
                            }

                            result = (questionAnswers.sum().toFloat() * 100) / 40
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

                            result = (result * 100) / 4
                        }
                    }

                    saveTest(explanation, result, questionAnswers)
                }.show()
        }
    }

    fun returnButtonValidation(){
        if(questionNumber == 1 && btnCancelState == true) {
            // Showing introduction for the user
            val dialog = MaterialAlertDialogBuilder(findNavController().context)
                .setTitle("¡Opción Inválida!")
                .setMessage("\nRecuerda que debes completar este primer test para acceder a las demás " +
                        "funcionalidades de la aplicación = )\n")
                .setNeutralButton("¡Entendido!") { dialog, _ -> dialog.dismiss() }
                .show()

            // Changing neutral button position to center
            ElementsEditor().updateDialogButton(dialog)
        } else if(questionNumber == 1 && btnCancelState == false) {
            binding.btnReturn.isClickable = false
            binding.btnNext.isClickable = false

            MaterialAlertDialogBuilder(findNavController().context)
                .setTitle("¡No te Vayas!")
                .setMessage("¿Realmente Quieres Cancelar la Prueba?")
                .setCancelable(false)
                .setNeutralButton("¡No!"){ dialog, _ ->
                    dialog.dismiss()
                    binding.btnReturn.isClickable = true
                    binding.btnNext.isClickable = true
                }
                .setPositiveButton("¡Sí!"){ _, _ ->
                    // Fragment enters from right
                    (parentFragment as NavBarFragment).updateNavBarHost(
                        TestFragment(), R.id.nav_test, false)
                    (parentFragment as NavBarFragment).updateImgHelp(true)
                }.show()
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
            binding.txtQuestion.text = getString(res.getIdentifier(testQuestion+questionNumber, "string", requireContext().packageName))

            // Updating answer
            answer = questionAnswers[questionNumber-1].toFloat()
            updateAnswer(answer, null)
            ElementsEditor().updateButtonState(binding.btnNext, true, requireContext(), true)
        }
    }

    private fun saveTest(explanation: String, result: Float, questionAnswers: IntArray){
        // Deactivating clickable
        (parentFragment as NavBarFragment).changeNavBarButtonsClickable(false)

        context?.let { context ->
            // Launching room database connection
            MainScope().launch {
                // Creating connection to database
                val userDao = Room.databaseBuilder(context, AppDatabase::class.java, "user")
                    .fallbackToDestructiveMigration().build().userDao()
                val user = userDao.getUser()[0]

                // Arguments to Post Request
                val formBody = FormBody.Builder()
                formBody.add("usuario", user.email)
                for(i in questionAnswers.indices)
                    formBody.add("pregunta${i+1}", questionAnswers[i].toString())

                // Choosing api url
                var apiUrl = ""
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                when(testNumber) {
                    // SISCO Test
                    1 -> {
                        apiUrl = "https://apiskydelight.herokuapp.com/api/crear-testcisco"
                        user.siscoCalendar = LocalDateTime.now().format(formatter)
                    }
                    // SVQ Test
                    2 -> {
                        apiUrl = "https://apiskydelight.herokuapp.com/api/crear-testsqv"
                        user.svqCalendar = LocalDateTime.now().format(formatter)
                    }
                    // PSS Test
                    3 -> {
                        apiUrl = "https://apiskydelight.herokuapp.com/api/crear-testpss"
                        user.pssCalendar = LocalDateTime.now().format(formatter)
                    }
                    // SVS Test
                    4 -> {
                        apiUrl = "https://apiskydelight.herokuapp.com/api/crear-testsvs"
                        user.svsCalendar = LocalDateTime.now().format(formatter)
                    }
                }

                // Updating user date and hour of test in local database
                userDao.updateUser(user)

                // Making http request
                val request = Request.Builder()
                    .url(apiUrl)
                    .post(formBody.build())
                    .addHeader("Authorization", "Bearer " + user.token)
                    .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                    .build()

                ValidationsDialogsRequests().httpPetition(request, context, requireView(), requireActivity(),
                    getString(R.string.test_btn_end), binding.btnNext, binding.btnReturn, null, null,
                    binding.progressBar, null, null, (parentFragment as NavBarFragment))
                {
                    // Setting parameters for the next fragment
                    val bundle = bundleOf(TEST_PARAM to testNumber, SCORE_PARAM to result, STRESS_PARAM to explanation)

                    val fragment = TestDataFragment()
                    fragment.arguments = bundle

                    // Fragment enters from right
                    (parentFragment as NavBarFragment).updateNavBarHost(fragment, R.id.navbar_test_data_fragment, true)
                }
            }
        }
    }
}