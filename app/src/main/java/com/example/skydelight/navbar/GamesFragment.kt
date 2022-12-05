package com.example.skydelight.navbar

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.skydelight.unity.UnityActivity
import com.example.skydelight.databinding.FragmentNavbarGamesBinding

// TODO("Screen design")
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

        binding.relaxCard.setOnClickListener {
            val intent = Intent(requireContext(), UnityActivity::class.java)
            intent.putExtra("SceneName", "relaxAR")
            startActivity(intent)
        }
        binding.extremeCard.setOnClickListener {
            val intent = Intent(requireContext(), UnityActivity::class.java)
            intent.putExtra("SceneName", "extremeAR")
            startActivity(intent)
        }
    }
}