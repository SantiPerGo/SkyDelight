package com.example.skydelight.initial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Changing to the principal fragment
        binding.btnUnderstand.setOnClickListener{findNavController().navigate(R.id.action_registerThird_to_navBar)}
    }
}