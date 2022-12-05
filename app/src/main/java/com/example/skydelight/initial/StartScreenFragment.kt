package com.example.skydelight.initial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        // Changing to the login fragment
        binding.btnLogin.setOnClickListener{findNavController().navigate(R.id.action_startScreen_to_login)}

        // Changing to the register first fragment
        binding.btnRegister.setOnClickListener {findNavController().navigate(R.id.action_startScreen_to_registerFirst)}

        // Changing to the recover password fragment
        binding.txtRecoverPassword.setOnClickListener {findNavController().navigate(R.id.action_startScreen_to_recoverPassword)}
    }
}