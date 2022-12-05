package com.example.skydelight.navbar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.unity.UnityActivity
import com.example.skydelight.databinding.FragmentNavbarGamesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.ar.core.ArCoreApk
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

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

        val errorDialog = MaterialAlertDialogBuilder(findNavController().context)
            .setTitle("Error de Incompatibilidad con ARCore")
            .setMessage("Parece que tu dispositivo no es compatible con realidad aumentada (ARCore)")
            .setNeutralButton("¡No!"){ dialog, _ -> dialog.dismiss() }

        binding.relaxCard.setOnClickListener {
            if(isCompatibleWithArCore()) {
                val intent = Intent(requireContext(), UnityActivity::class.java)
                intent.putExtra("SceneName", "relaxAR")
                startActivity(intent)
            } else { updateDialogButton(errorDialog.show()) }
        }

        binding.extremeCard.setOnClickListener {
            if(isCompatibleWithArCore()) {
                val intent = Intent(requireContext(), UnityActivity::class.java)
                intent.putExtra("SceneName", "extremeAR")
                startActivity(intent)
            } else { updateDialogButton(errorDialog.show()) }
        }
    }

    private fun updateDialogButton(dialog: AlertDialog){
        // Changing neutral button position to center
        val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        positiveButton.layoutParams = layoutParams
    }

    private fun isCompatibleWithArCore(): Boolean{
        val availability = ArCoreApk.getInstance().checkAvailability(requireContext())

        // Continue to query availability at 5Hz while compatibility is checked in the background
        if (availability.isTransient)
            Handler(Looper.getMainLooper()).postDelayed({ isCompatibleWithArCore() }, 200)

        // The device is unsupported or unknown
        if (!availability.isSupported)
            return false

        return true
    }
}