package com.example.skydelight.initial

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pl.bclogic.pulsator4droid.library.PulsatorLayout

class LoadingScreenFragment : Fragment() {
    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading_screen, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialise pulsator
        view.findViewById<PulsatorLayout>(R.id.pulsator).start()

        // Start heart beat pulse
        val pulse = AnimationUtils.loadAnimation(findNavController().context, R.anim.pulse)
        view.findViewById<ImageView>(R.id.imgHeartLogo).startAnimation(pulse)

        // Changing to the start fragment after random time delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Launching room database connection
            MainScope().launch {
                context?.let {
                    // Creating connection to database
                    val userDao = Room.databaseBuilder(it, AppDatabase::class.java, "user")
                        .fallbackToDestructiveMigration().build().userDao()

                    // If user exists, changing to principal fragments
                    if (userDao.getUser().isNotEmpty())
                        // If session has to be open
                        if (userDao.getUser()[0].session)
                            // Verify if user saved dark theme
                            updateTheme(userDao.getUser()[0].isDarkTheme)
                        // Else deleting user session
                        else {
                            userDao.deleteUsers()
                            findNavController().navigate(R.id.action_loadingScreen_to_startScreen)
                        }
                    // Else changing to initial fragments
                    else
                        findNavController().navigate(R.id.action_loadingScreen_to_startScreen)
                }
            }
        }, (1000..5000).shuffled().last().toLong())
    }

    private fun updateTheme(isDarkTheme: Boolean?) {
        if(isDarkTheme != null)
            if(isDarkTheme != isDarkTheme(requireActivity()))
                if(isDarkTheme == true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else
                findNavController().navigate(R.id.action_loadingScreen_to_navBar)
        else
            findNavController().navigate(R.id.action_loadingScreen_to_navBar)
    }

    private fun isDarkTheme(activity: Activity): Boolean {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}