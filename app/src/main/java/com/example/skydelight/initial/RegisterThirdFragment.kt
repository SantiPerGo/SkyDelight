package com.example.skydelight.initial

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Showing elements
        Handler(Looper.getMainLooper()).postDelayed({ elementsVisibility(true) }, 500)

        // Changing to the principal fragment
        binding.btnUnderstand.setOnClickListener{
            binding.btnUnderstand.isClickable = false
            findNavController().navigate(R.id.action_registerThird_to_navBar)
        }
    }

    private fun elementsVisibility(state: Boolean){
        val elementsArray = arrayOf(binding.txtTitle, binding.txtExplanation1, binding.txtExplanation2,
            binding.txtExplanation3, binding.txtExplanation4, binding.txtExplanation5, binding.btnUnderstand)

        for(element in elementsArray)
            if(state)
                element.animate().alpha(1f)
            else
                element.animate().alpha(0f)
    }
}