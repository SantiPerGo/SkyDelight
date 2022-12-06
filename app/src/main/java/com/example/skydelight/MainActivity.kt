package com.example.skydelight

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.skydelight.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //hide action bar
        supportActionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.decorView.windowInsetsController!!.hide(android.view.WindowInsets.Type.navigationBars())
    }

    fun imgBackgroundVisibility(status: Boolean){
        if(status)
            binding.imgBackground.visibility = View.VISIBLE
        else
            binding.imgBackground.visibility = View.GONE
    }
}