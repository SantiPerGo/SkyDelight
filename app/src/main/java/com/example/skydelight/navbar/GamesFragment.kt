package com.example.skydelight.navbar

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.skydelight.R
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.custom.ViewPageAdapter
import com.example.skydelight.databinding.FragmentNavbarGamesBinding
import com.example.skydelight.unity.UnityActivity
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
        try {
            val imagesArray = arrayOf(R.drawable.wallpaper_beach_2, R.drawable.wallpaper_wingsuit)
            val viewPagerAdapter = ViewPageAdapter(requireContext(), imagesArray)
            binding.viewPagerMain.adapter = viewPagerAdapter
            binding.tabLayout.setupWithViewPager(binding.viewPagerMain, true)
        } catch(e: java.lang.IllegalStateException) {}

        // Relax AR Button
        binding.btnStart.setOnClickListener {
            try {
                if(isCompatibleWithArCore(requireContext())) {
                    val intent = Intent(context, UnityActivity::class.java)
                    intent.putExtra("SceneName", "extremeAR")
                    startActivity(intent)
                } else { ElementsEditor().updateDialogButton(errorDialog(requireContext())) }
            } catch(e: java.lang.IllegalStateException) {}
        }

        // Pictures Actions
        binding.viewPagerMain.addOnPageChangeListener ( object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // Extreme AR
                if(binding.txtSubtitle.text == getString(R.string.games_relaxAR_title)) {
                    binding.txtSubtitle.text = getString(R.string.games_extremeAR_title)
                    binding.txtDescription.text = getString(R.string.games_extremeAR_description)
                    binding.btnStart.setOnClickListener {
                        try {
                            if(isCompatibleWithArCore(requireContext())) {
                                val intent = Intent(context, UnityActivity::class.java)
                                intent.putExtra("SceneName", "extremeAR")
                                startActivity(intent)
                            } else { ElementsEditor().updateDialogButton(errorDialog(requireContext())) }
                        } catch(e: java.lang.IllegalStateException) {}
                    }
                    updateColors(R.attr.btn_text_color_blue)
                }
                // Relax AR
                else {
                    binding.txtSubtitle.text = getString(R.string.games_relaxAR_title)
                    binding.txtDescription.text = getString(R.string.games_relaxAR_description)
                    binding.btnStart.setOnClickListener {
                        try {
                            if(isCompatibleWithArCore(requireContext())) {
                                val intent = Intent(context, UnityActivity::class.java)
                                intent.putExtra("SceneName", "relaxAR")
                                startActivity(intent)
                            } else { ElementsEditor().updateDialogButton(errorDialog(requireContext())) }
                        } catch(e: java.lang.IllegalStateException) {}
                    }
                    updateColors(R.attr.btn_text_color_green)
                }
            }
        })
    }

    private fun errorDialog(context: Context): AlertDialog {
        // Error validation
        val errorDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Error de Incompatibilidad con ARCore")
            .setMessage("Parece que tu dispositivo no es compatible con realidad aumentada (ARCore)")
            .setNeutralButton("¡No!"){ dialog, _ -> dialog.dismiss() }

        // Loading pictures on view pager and connecting it with dots tab layout
        val imagesArray = arrayOf(R.drawable.wallpaper_beach_2, R.drawable.wallpaper_wingsuit)
        val viewPagerAdapter = ViewPageAdapter(context, imagesArray)
        binding.viewPagerMain.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPagerMain, true)

        return errorDialog.show()
    }

    private fun updateColors(resource: Int) {
        // Creating arrays
        val textsArray = arrayListOf(binding.txtTitle, binding.txtSubtitle,
            binding.txtDescription, binding.btnStart)
        val buttonsArray = arrayListOf(binding.btnStart)

        try {
            // Updating design
            ElementsEditor().updateColors(resource, context, textsArray, buttonsArray)

            // Changing button design
            binding.tabLayout.tabRippleColor =
                ColorStateList.valueOf(ElementsEditor().getColor(context, resource))
        } catch(e: java.lang.IllegalStateException) {}
    }

    private fun isCompatibleWithArCore(context: Context): Boolean{
        val availability = ArCoreApk.getInstance().checkAvailability(context)

        // Continue to query availability at 5Hz while compatibility is checked in the background
        if (availability.isTransient)
            Handler(Looper.getMainLooper()).postDelayed({ isCompatibleWithArCore(context) }, 200)

        // The device is unsupported or unknown
        if (!availability.isSupported)
            return false

        return true
    }
}