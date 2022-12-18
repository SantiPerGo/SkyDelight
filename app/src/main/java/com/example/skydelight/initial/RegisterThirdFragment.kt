package com.example.skydelight.initial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skydelight.R
import com.example.skydelight.databinding.FragmentRegisterThirdBinding

class RegisterThirdFragment : Fragment() {

    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentRegisterThirdBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentRegisterThirdBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_register_third, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Overriding back actions
        backAction()

        // Changing to the principal fragment
        binding.btnUnderstand.setOnClickListener{
            binding.btnUnderstand.isClickable = false
            findNavController().navigate(R.id.action_registerThird_to_navBar)
        }
    }

    private fun backAction(){
        try {
            requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_registerThird_to_navBar) } })
        } catch(e: java.lang.IllegalStateException) {}
    }
}