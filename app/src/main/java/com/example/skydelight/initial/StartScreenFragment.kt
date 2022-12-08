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

        // Showing elements
        Handler(Looper.getMainLooper()).postDelayed({ elementsVisibility(true) }, 500)

        // Overriding back actions
        backAction()

        // Changing to the login fragment
        binding.btnLogin.setOnClickListener{
            ElementsEditor().elementsClickableState(false, arrayListOf(binding.txtRecoverPassword),
                arrayListOf(binding.btnLogin, binding.btnRegister))
            elementsVisibility(false)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_login)
            }, 500)
        }

        // Changing to the register first fragment
        binding.btnRegister.setOnClickListener {
            ElementsEditor().elementsClickableState(false, arrayListOf(binding.txtRecoverPassword),
                arrayListOf(binding.btnLogin, binding.btnRegister))
            elementsVisibility(false)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_startScreen_to_registerFirst)
            }, 500)
        }

        // Changing to the recover password fragment
        binding.txtRecoverPassword.setOnClickListener {
            ElementsEditor().elementsClickableState(false, arrayListOf(binding.txtRecoverPassword),
                arrayListOf(binding.btnLogin, binding.btnRegister))
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

    private fun backAction(){
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    when (findNavController().currentDestination?.id) {
                        R.id.start_screen_fragment -> { requireActivity().moveTaskToBack(true) }
                        R.id.login_fragment -> {
                            (requireParentFragment().childFragmentManager.fragments.first()
                                    as LoginFragment).elementsVisibility(false)
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().navigate(R.id.action_login_to_startScreen)
                                findNavController().popBackStack(R.id.login_fragment, true)
                            }, 500)
                        }
                        R.id.register_first_fragment -> {
                            (requireParentFragment().childFragmentManager.fragments.first()
                                    as RegisterFirstFragment).elementsVisibility(false)
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().navigate(R.id.action_registerFirst_to_startScreen)
                                findNavController().popBackStack(R.id.register_first_fragment, true)
                            }, 500)
                        }
                        R.id.register_second_fragment -> {
                            (requireParentFragment().childFragmentManager.fragments.first()
                                    as RegisterSecondFragment).elementsVisibility(false)
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().navigate(R.id.action_registerSecond_to_registerFirst)
                                findNavController().popBackStack(R.id.register_second_fragment, true)
                            }, 500)
                        }
                        R.id.recover_password_fragment -> {
                            (requireParentFragment().childFragmentManager.fragments.first()
                                    as RecoverPasswordFragment).elementsVisibility(false)
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().navigate(R.id.action_recoverPassword_to_startScreen)
                                findNavController().popBackStack(R.id.recover_password_fragment, true)
                            }, 500)
                        }
                    }
                } catch (e: IllegalArgumentException) {}
            }
        })
    }
}