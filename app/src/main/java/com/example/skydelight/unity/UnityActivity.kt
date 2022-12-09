package com.example.skydelight.unity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.skydelight.MainActivity
import com.example.skydelight.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.unity3d.player.UnityPlayer

class UnityActivity : AppCompatActivity() {
    private lateinit var unityPlayer: UnityPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unityPlayer = UnityPlayer(this)
        val sceneName = intent.getStringExtra("SceneName")
        UnityPlayer.UnitySendMessage("Scene Loader", "LoadScene", sceneName)
        setContentView(R.layout.activity_unity)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener{
            //unityPlayer.quit()
            restartApp(this)
        }
    }

    private fun restartApp(activity: UnityActivity) {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity.finishAffinity()
    }

    fun getUnityPlayer() : UnityPlayer { return unityPlayer }

    override fun onPause() {
        super.onPause()
        unityPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        unityPlayer.resume()
    }
}