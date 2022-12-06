package com.example.skydelight.initial

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
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

        // Changing to the login fragment
        binding.btnLogin.setOnClickListener{findNavController().navigate(R.id.action_startScreen_to_login)}

        // Changing to the register first fragment
        binding.btnRegister.setOnClickListener {findNavController().navigate(R.id.action_startScreen_to_registerFirst)}

        // Changing to the recover password fragment
        binding.txtRecoverPassword.setOnClickListener {findNavController().navigate(R.id.action_startScreen_to_recoverPassword)}
    }
}