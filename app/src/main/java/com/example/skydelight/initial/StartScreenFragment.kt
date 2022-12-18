package com.example.skydelight.initial

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skydelight.MainActivity
import com.example.skydelight.R
import com.example.skydelight.custom.ElementsEditor
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

        // If user press back button then close app
        try { requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if(findNavController().currentDestination?.id == R.id.start_screen_fragment)
                            try { requireActivity().moveTaskToBack(true)
                            } catch (e: java.lang.IllegalStateException) {}
                    } })
        } catch (e: java.lang.IllegalStateException) {}

        // Changing to the login fragment
        binding.btnLogin.setOnClickListener{
            ElementsEditor().elementsClickableState(false, arrayListOf(binding.txtRecoverPassword),
                arrayListOf(binding.btnLogin, binding.btnRegister))
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_login)
            }, 500)
        }

        // Changing to the register first fragment
        binding.btnRegister.setOnClickListener {
            ElementsEditor().elementsClickableState(false, arrayListOf(binding.txtRecoverPassword),
                arrayListOf(binding.btnLogin, binding.btnRegister))
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_registerFirst)
            }, 500)
        }

        // Changing to the recover password fragment
        binding.txtRecoverPassword.setOnClickListener {
            ElementsEditor().elementsClickableState(false, arrayListOf(binding.txtRecoverPassword),
                arrayListOf(binding.btnLogin, binding.btnRegister))
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_recoverPassword)
            }, 500)
        }
    }
}