package com.example.skydelight.navbar

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.databinding.FragmentNavbarTestDataBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


private const val TEST_PARAM = "test"
private const val SCORE_PARAM = "score"
private const val STRESS_PARAM = "stress"

class TestDataFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarTestDataBinding

    // Variables to receive data from other fragments
    private var testNumber: Int? = null
    private var stressLevel: String? = null
    private var score: Float? = null

    // Getting data from other fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            testNumber = it.getInt(TEST_PARAM)
            score = it.getFloat(SCORE_PARAM)
            stressLevel = it.getString(STRESS_PARAM)
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hiding cancel button
        binding.btnCancel.visibility = View.GONE

        // Changing start button location
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.constraintLayout)
        constraintSet.connect(binding.btnStart.id, ConstraintSet.BOTTOM, binding.guidelineBottom.id, ConstraintSet.BOTTOM)
        constraintSet.applyTo(binding.constraintLayout)

        // Updating text colors
        updateTextAndColors()

        // Updating text according of test result
        updateTestResult()

        // Changing to answer test fragment
        binding.btnStart.setOnClickListener {
            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(HomeFragment(), R.id.nav_home, false)
            (parentFragment as NavBarFragment).updateImgHelp(true)
            (parentFragment as NavBarFragment).updateImgReload(true)
        }
    }

    private fun updateTextAndColors() {
        // Updating text
        var maxNum = 0f
        var minNum = 0f
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

        // Getting color and title according of results
        if(score!! >= maxNum){
            updateColors(R.attr.btn_text_color_red, R.attr.btn_background_red)
            binding.txtTitle.text = getString(R.string.test_title_5)
        } else if(score!! >= minNum && score!! < maxNum){
            updateColors(R.attr.btn_text_color_yellow, R.attr.btn_background_yellow)
            binding.txtTitle.text = getString(R.string.test_title_6)
        } else {
            updateColors(R.attr.btn_text_color_green, R.attr.btn_background_green)
            binding.txtTitle.text = getString(R.string.test_title_3)
        }
    }

    private fun updateColors(textColorResource: Int, btnColorResource: Int) {
        // Getting reference to resource color
        val typedValueText = TypedValue()
        val typedValueBtn = TypedValue()

        requireContext().theme.resolveAttribute(textColorResource, typedValueText, true)
        requireContext().theme.resolveAttribute(btnColorResource, typedValueBtn, true)

        val textColor = typedValueText.data
        val btnColor = typedValueBtn.data

        // Changing colors
        val elementsArray = arrayOf(binding.txtTitle, binding.txtTestTitle,
            binding.txtDescription, binding.txtNumber, binding.btnStart)

        for(element in elementsArray) {
            element.setTextColor(textColor)
            element.setShadowLayer(5f,0f, 0f, textColor)
        }

        (binding.btnStart as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)
        (binding.btnStart as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)
        binding.btnStart.backgroundTintList = ColorStateList.valueOf(btnColor)
    }

    private fun updateTestResult() {
        // Showing text result
        binding.txtDescription.text = stressLevel

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
    }
}