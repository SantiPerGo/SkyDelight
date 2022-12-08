package com.example.skydelight.navbar

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.databinding.FragmentNavbarTestDataBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
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

        // Updating text colors
        updateColorsAndChart()

        // Updating text according of test result
        updateTestResult()

        // Changing to answer test fragment
        binding.btnFinish.setOnClickListener {
            // Fragment enters from right
            (parentFragment as NavBarFragment).updateNavBarHost(HomeFragment(), R.id.nav_home, false)
            (parentFragment as NavBarFragment).updateImgHelp(true)
            (parentFragment as NavBarFragment).updateImgReload(true)
        }
    }

    private fun updateColorsAndChart() {
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
        if(score!! >= maxNum) {
            createPieChart(score!!, R.attr.btn_text_color_red, R.attr.btn_background_red)
            updateColors(R.attr.btn_text_color_red, R.attr.btn_background_red)
        } else if(score!! >= minNum && score!! < maxNum) {
            createPieChart(score!!, R.attr.btn_text_color_yellow, R.attr.btn_background_yellow)
            updateColors(R.attr.btn_text_color_yellow, R.attr.btn_background_yellow)
        } else {
            createPieChart(score!!, R.attr.btn_text_color_green, R.attr.btn_background_green)
            updateColors(R.attr.btn_text_color_green, R.attr.btn_background_green)
        }
    }

    private fun createPieChart(score: Float, textColorResource: Int, backgroundColorResource: Int) {
        val textColor = getColor(textColorResource)
        val backgroundColor = getColor(backgroundColorResource)
        val centerColor = getColor(R.attr.fragment_background)

        // Disable description and legend in chart
        binding.pieChart.legend.isEnabled = false
        binding.pieChart.description.isEnabled = false

        // Friction when rotating chart
        binding.pieChart.dragDecelerationFrictionCoef = 0.95f

        // Center of chart
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.setHoleColor(centerColor)

        // Ring semi-transparent around center of chart
        binding.pieChart.setTransparentCircleColor(Color.TRANSPARENT)
        binding.pieChart.setTransparentCircleAlpha(0)

        // Initial angle for chart
        binding.pieChart.rotationAngle = 0f

        // Enable the user to rotate the chart
        binding.pieChart.isRotationEnabled = true

        // Highlight slice when it's tapped
        binding.pieChart.isHighlightPerTapEnabled = true

        // Animation when loading chart
        binding.pieChart.animateY(1000, Easing.EaseInOutQuad)

        // Setting dataset for chart
        val entries = arrayListOf(PieEntry(score), PieEntry(100-score))
        val dataset = PieDataSet(entries, "Test Result")

        // Setting chart colors
        val colors = arrayListOf(textColor, backgroundColor)
        dataset.colors = colors

        // Disable icons and texts
        dataset.setDrawIcons(false)
        dataset.setDrawValues(false)

        // Loading data in chart
        val data = PieData(dataset)
        binding.pieChart.data = data

        // Loading chart
        binding.pieChart.invalidate()
    }

    private fun updateColors(textColorResource: Int, btnColorResource: Int) {
        val textColor = getColor(textColorResource)
        val btnColor = getColor(btnColorResource)

        // Changing colors
        val elementsArray = arrayOf(binding.txtTitle, binding.txtDescription,
            binding.txtNumber, binding.btnFinish, binding.txtChartFirst)

        for(element in elementsArray) {
            element.setTextColor(textColor)
            element.setShadowLayer(5f,0f, 0f, textColor)
        }

        // Changing finish button background color and when it's tapped
        (binding.btnFinish as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)
        binding.btnFinish.backgroundTintList = ColorStateList.valueOf(btnColor)

        // Changing circles colors of chart
        binding.txtChartFirst.compoundDrawables[0].setTint(textColor)
        binding.txtChartSecond.compoundDrawables[0].setTint(btnColor)

        // Changing second chart label color
        binding.txtChartSecond.setTextColor(btnColor)
        binding.txtChartSecond.setShadowLayer(5f,0f, 0f, btnColor)
    }

    // Getting reference to resource color
    private fun getColor(colorReference: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(colorReference, typedValue, true)
        return typedValue.data
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
                binding.txtChartFirst.text = "Estrés Académico"
                binding.txtChartSecond.text = "Tranquilidad"

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
            2 -> {
                binding.txtNumber.text = "Eres $scoreString% vulnerable al estrés"
                binding.txtChartFirst.text = "Vulnerabilidad al Estrés"
                binding.txtChartSecond.text = "Resistencia al Estrés"
            }
            // PSS Test
            3 -> {
                binding.txtNumber.text = "Presentas $scoreString% de estrés"
                binding.txtChartFirst.text = "Estrés"
                binding.txtChartSecond.text = "Tranquilidad"
            }
            // SVS Test
            4 -> {
                binding.txtNumber.text = "Eres $scoreString% vulnerable al estrés"
                binding.txtChartFirst.text = "Vulnerabilidad al Estrés"
                binding.txtChartSecond.text = "Resistencia al Estrés"
            }
        }
    }
}