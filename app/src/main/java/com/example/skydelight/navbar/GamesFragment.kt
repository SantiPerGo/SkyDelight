package com.example.skydelight.navbar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.skydelight.R
import com.example.skydelight.custom.CustomDialog
import com.example.skydelight.databinding.FragmentNavbarGamesBinding
import com.example.skydelight.unity.UnityActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.FatalException

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

        // Relax AR Button
        binding.btnStart.setOnClickListener {
            try { checkArCoreInstallation(requireContext(), requireActivity())
            } catch(e: java.lang.IllegalStateException) {}
        }
    }

    // Ask for camera permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) loadUnityGame(requireContext())
        else errorCameraDialog()
    }

    // Ensure that Google Play Services for AR are installed and up to date.
    private fun checkArCoreInstallation(context: Context, activity: FragmentActivity) {
        // Verify ARCore compatibility
        if(isCompatibleWithArCore(context)) {
            // requestInstall(Activity, true) will triggers installation of
            // Google Play Services for AR if necessary.
            try {
                if (ArCoreApk.getInstance().requestInstall(activity, true) == ArCoreApk.InstallStatus.INSTALLED) {
                    // Check and request camera permission
                    if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                     else loadUnityGame(context)
                }
            } catch(e: FatalException) { errorValidationDialog() }
        } else errorDialog()
    }

    private fun loadUnityGame(context: Context) {
        // Safe to create the AR session
        val intent = Intent(context, UnityActivity::class.java)
        intent.putExtra("SceneName", "relaxAR")
        startActivity(intent)
    }

    // Error validation
    private fun errorDialog() {
        val dialog = CustomDialog(getString(R.string.games_errorAR_title),
            getString(R.string.games_errorAR_description), R.attr.heart_sad,
            R.attr.fragment_background, requireContext(), isMiniSize = true)
        dialog.firstButton(getString(R.string.games_errorAR_understand)) {}
        dialog.show()
    }

    // Error validation
    private fun errorCameraDialog() {
        val dialog = CustomDialog(getString(R.string.games_errorCamera_title),
            getString(R.string.games_errorCamera_description), R.attr.heart_sad,
            R.attr.fragment_background, requireContext())
        dialog.firstButton(getString(R.string.games_errorAR_understand)) {}
        dialog.show()
    }

    // Error validation
    private fun errorValidationDialog() {
        val dialog = CustomDialog(getString(R.string.games_errorValidation_title),
            getString(R.string.games_errorValidation_description), R.attr.heart_sad,
            R.attr.fragment_background, requireContext())
        dialog.firstButton(getString(R.string.games_errorAR_understand)) {}
        dialog.show()
    }

    private fun isCompatibleWithArCore(context: Context): Boolean{
        val availability = ArCoreApk.getInstance().checkAvailability(context)

        // Continue to query availability at 5Hz while compatibility is checked in the background
        if (availability.isTransient)
            Handler(Looper.getMainLooper()).postDelayed({ isCompatibleWithArCore(context) }, 200)

        // The device is unsupported or unknown
        if (!availability.isSupported)
            return false

        return true
    }
}