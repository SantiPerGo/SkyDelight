package com.example.skydelight.navbar

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.skydelight.R
import com.example.skydelight.custom.ViewPageAdapter
import com.example.skydelight.databinding.FragmentNavbarGamesBinding
import com.example.skydelight.unity.UnityActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.ar.core.ArCoreApk

class GamesFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarGamesBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarGamesBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Loading pictures on view pager and connecting it with dots tab layout
        val imagesArray = arrayOf(R.drawable.wallpaper_beach_2, R.drawable.wallpaper_wingsuit)
        val viewPagerAdapter = ViewPageAdapter(requireContext(), imagesArray)
        binding.viewPagerMain.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPagerMain, true)

        binding.viewPagerMain.addOnPageChangeListener ( object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                val errorDialog = MaterialAlertDialogBuilder(findNavController().context)
                    .setTitle("Error de Incompatibilidad con ARCore")
                    .setMessage("Parece que tu dispositivo no es compatible con realidad aumentada (ARCore)")
                    .setNeutralButton("¡No!"){ dialog, _ -> dialog.dismiss() }

                // Relax AR
                if(binding.txtARTitle.text == getString(R.string.games_relaxAR_title)) {
                    binding.txtARTitle.text = getString(R.string.games_extremeAR_title)
                    binding.txtDescription.text = getString(R.string.games_extremeAR_description)
                    binding.btnStart.setOnClickListener {
                        if(isCompatibleWithArCore()) {
                            val intent = Intent(requireContext(), UnityActivity::class.java)
                            intent.putExtra("SceneName", "extremeAR")
                            startActivity(intent)
                        } else { updateDialogButton(errorDialog.show()) }
                    }
                    updateColors(R.attr.btn_text_color_blue)
                }
                // Extreme AR
                else {
                    binding.txtARTitle.text = getString(R.string.games_relaxAR_title)
                    binding.txtDescription.text = getString(R.string.games_relaxAR_description)
                    binding.btnStart.setOnClickListener {
                        if(isCompatibleWithArCore()) {
                            val intent = Intent(requireContext(), UnityActivity::class.java)
                            intent.putExtra("SceneName", "relaxAR")
                            startActivity(intent)
                        } else { updateDialogButton(errorDialog.show()) }
                    }
                    updateColors(R.attr.btn_text_color_green)
                }
            }
        })
    }

    private fun updateColors(resource: Int) {
        // Getting reference to resource color
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(resource, typedValue, true)

        // Getting color and shadow
        val textColor = typedValue.data
        val shadowRadius = 5f

        // Changing colors
        binding.txtTitle.setTextColor(textColor)
        binding.txtTitle.setShadowLayer(shadowRadius,0f, 0f, textColor)
        binding.txtARTitle.setTextColor(textColor)
        binding.txtARTitle.setShadowLayer(shadowRadius,0f, 0f, textColor)
        binding.txtDescription.setTextColor(textColor)
        binding.txtDescription.setShadowLayer(shadowRadius,0f, 0f, textColor)
        binding.btnStart.setTextColor(textColor)
        binding.btnStart.setShadowLayer(shadowRadius,0f, 0f, textColor)
        (binding.btnStart as MaterialButton).strokeColor = ColorStateList.valueOf(textColor)
        (binding.btnStart as MaterialButton).rippleColor = ColorStateList.valueOf(textColor)
        binding.tabLayout.tabRippleColor = ColorStateList.valueOf(textColor)
    }

    private fun updateDialogButton(dialog: AlertDialog){
        // Changing neutral button position to center
        val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        positiveButton.layoutParams = layoutParams
    }

    private fun isCompatibleWithArCore(): Boolean{
        val availability = ArCoreApk.getInstance().checkAvailability(requireContext())

        // Continue to query availability at 5Hz while compatibility is checked in the background
        if (availability.isTransient)
            Handler(Looper.getMainLooper()).postDelayed({ isCompatibleWithArCore() }, 200)

        // The device is unsupported or unknown
        if (!availability.isSupported)
            return false

        return true
    }
}