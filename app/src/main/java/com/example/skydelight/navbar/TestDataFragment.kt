package com.example.skydelight.navbar

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.databinding.FragmentNavbarTestDataBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


private const val TEST_PARAM = "test"
private const val RESULT_PARAM = "test_result"
private const val SCORE_PARAM = "score"
private const val STRESS_PARAM = "stress"
private const val BTNCANCEL_PARAM = "btn_cancel"

class TestDataFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarTestDataBinding

    // Variables to receive data from other fragments
    private var testNumber: Int? = null
    private var stressLevel: String? = null
    private var score: Float? = null
    private var testResult: Boolean? = null
    private var btnCancelState: Boolean? = null

    // Getting data from other fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            testNumber = it.getInt(TEST_PARAM)
            testResult = it.getBoolean(RESULT_PARAM)
            score = it.getFloat(SCORE_PARAM)
            stressLevel = it.getString(STRESS_PARAM)
            btnCancelState = it.getBoolean(BTNCANCEL_PARAM)
        }
    }

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarTestDataBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_test, container, false)
    }

    // After the view is created we can do things
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Deactivating help fab
        (parentFragment as NavBarFragment).updateImgHelp(false)

        if(btnCancelState == true || testResult == true) {
            // Hiding cancel button
            binding.btnCancel.visibility = View.GONE

            // Changing start button location
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.constraintLayout)
            constraintSet.connect(binding.btnStart.id, ConstraintSet.BOTTOM, binding.guidelineBottom.id, ConstraintSet.BOTTOM)
            constraintSet.applyTo(binding.constraintLayout)
        }

        // If user has finished a test
        if(testResult  == true) {
            binding.txtTestTitle.text = getString(R.string.test_title_4)
            binding.txtDescription.text = stressLevel
            binding.btnStart.text = getString(R.string.test_btn_end)

            var maxNum = 0f
            var minNum = 0f

            // Updating text
            when(testNumber) {
                // SISCO Test
                1 -> { maxNum = 67f
                    minNum = 34f }
                // SVQ Test
                2 -> { maxNum = 70f
                    minNum = 50f }
                // PSS Test
                3 -> { maxNum = 67.5f
                    minNum = 35f }
                // SVS Test
                4 -> { maxNum = 75f
                    minNum = 50f }
            }

            // Variable to get color
            val typedValue = TypedValue()
            val typedValueBtn = TypedValue()

            // Getting color and title according of results
            if(score!! >= maxNum){
                requireContext().theme.resolveAttribute(R.attr.btn_text_color_red, typedValue, true)
                requireContext().theme.resolveAttribute(R.attr.btn_background_red, typedValueBtn, true)
                binding.txtTitle.text = getString(R.string.test_title_5)
            } else if(score!! >= minNum && score!! < maxNum){
                requireContext().theme.resolveAttribute(R.attr.btn_text_color_yellow, typedValue, true)
                requireContext().theme.resolveAttribute(R.attr.btn_background_yellow, typedValueBtn, true)
                binding.txtTitle.text = getString(R.string.test_title_6)
            } else {
                requireContext().theme.resolveAttribute(R.attr.btn_text_color_green, typedValue, true)
                requireContext().theme.resolveAttribute(R.attr.btn_background_green, typedValueBtn, true)
                binding.txtTitle.text = getString(R.string.test_title_3)
            }

            // Changing text colors
            val textColor = typedValue.data
            binding.txtTitle.setTextColor(textColor)
            binding.txtTestTitle.setTextColor(textColor)
            binding.txtDescription.setTextColor(textColor)
            binding.txtNumber.setTextColor(textColor)
            binding.btnStart.setTextColor(textColor)
            binding.btnStart.backgroundTintList = ColorStateList.valueOf(typedValueBtn.data)

            requireContext().theme.resolveAttribute(R.color.black, typedValueBtn, true)
            binding.btnStart.highlightColor = typedValueBtn.data

            // Deleting decimals when necessary
            var scoreString = String.format("%.2f", score)
            if(score!! % 1f == 0f)
                scoreString = score?.toInt().toString()

            // Updating text
            when(testNumber) {
                // SISCO Test
                1 -> {
                    binding.txtNumber.text = "Presentas $scoreString% de estrés académico"

                    // Launching room database connection
                    MainScope().launch {
                        // Creating connection to database
                        val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                            .fallbackToDestructiveMigration().build().userDao()
                        val user = userDao.getUser()[0]

                        // Updating initial test boolean
                        if(!user.initialTest){
                            user.initialTest = true
                            userDao.updateUser(user)
                            (parentFragment as NavBarFragment).changeNavBarButtonsClickable(true)
                        }
                    }
                }
                // SVQ Test
                2 -> { binding.txtNumber.text = "Eres $scoreString% vulnerable al estrés" }
                // PSS Test
                3 -> { binding.txtNumber.text = "Presentas $scoreString% de estrés" }
                // SVS Test
                4 -> { binding.txtNumber.text = "Eres $scoreString% vulnerable al estrés" }
            }
        } else {
            // Updating text
            when(testNumber){
                // SISCO Test
                1 -> {
                    binding.txtTestTitle.text = getString(R.string.test_sisco)
                    binding.txtDescription.text = getString(R.string.test_sisco_description)
                    binding.txtNumber.text = getString(R.string.test_sisco_number)
                }
                // SVQ Test
                2 -> { binding.txtTestTitle.text = getString(R.string.test_svq)
                    binding.txtDescription.text = getString(R.string.test_svq_description)
                    binding.txtNumber.text = getString(R.string.test_svq_number) }
                // PSS Test
                3 -> { binding.txtTestTitle.text = getString(R.string.test_pss)
                    binding.txtDescription.text = getString(R.string.test_pss_description)
                    binding.txtNumber.text = getString(R.string.test_pss_number) }
                // SVS Test
                4 -> { binding.txtTestTitle.text = getString(R.string.test_svs)
                    binding.txtDescription.text = getString(R.string.test_svs_description)
                    binding.txtNumber.text = getString(R.string.test_svs_number) }
            }
        }

        // Changing to test fragment
        binding.btnCancel.setOnClickListener {
            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(TestFragment(), R.id.nav_test, false)
            (parentFragment as NavBarFragment).updateImgHelp(true)
        }

        // Changing to answer test fragment
        binding.btnStart.setOnClickListener {
            if(testResult  == true) {
                // Fragment enters from right
                (parentFragment as NavBarFragment).updateNavBarHost(HomeFragment(), R.id.nav_home, false)
                (parentFragment as NavBarFragment).updateImgHelp(true)
                (parentFragment as NavBarFragment).updateImgReload(true)
            } else {
                // Setting parameters for the next fragment
                var bundle = bundleOf(TEST_PARAM to testNumber)

                // Sending btn cancel state when it's initial test
                if(btnCancelState == true)
                    bundle = bundleOf(TEST_PARAM to testNumber, BTNCANCEL_PARAM to btnCancelState)

                val fragment = TestAnswerFragment()
                fragment.arguments = bundle

                // Fragment enters from right
                (parentFragment as NavBarFragment).updateNavBarHost(fragment, R.id.navbar_test_answer_fragment, true)
            }
        }
    }
}