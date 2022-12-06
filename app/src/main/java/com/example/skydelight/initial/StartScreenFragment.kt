package com.example.skydelight.initial

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.example.skydelight.MainActivity
import com.example.skydelight.R
import com.example.skydelight.databinding.FragmentStartScreenBinding

class StartScreenFragment : Fragment() {

    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentStartScreenBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentStartScreenBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_start_screen, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Showing image view
        (activity as MainActivity).imgBackgroundVisibility(true)

        // Showing elements
        Handler(Looper.getMainLooper()).postDelayed({ elementsVisibility(true) }, 500)

        // Changing to the login fragment
        binding.btnLogin.setOnClickListener{
            elementsVisibility(false)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_login)
            }, 500)
        }

        // Changing to the register first fragment
        binding.btnRegister.setOnClickListener {
            elementsVisibility(false)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_registerFirst)
            }, 500)
        }

        // Changing to the recover password fragment
        binding.txtRecoverPassword.setOnClickListener {
            elementsVisibility(false)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_recoverPassword)
            }, 500)
        }
    }

    private fun elementsVisibility(state: Boolean){
        val elementsArray = arrayOf(binding.txtWelcome, binding.imgLogo, binding.txtRelax,
            binding.btnLogin, binding.btnRegister, binding.txtRecoverPassword)

        for(element in elementsArray)
            if(state)
                element.animate().alpha(1f)
            else
                element.animate().alpha(0f)
    }
}