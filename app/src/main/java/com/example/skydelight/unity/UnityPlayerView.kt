package com.example.skydelight.unity

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.example.skydelight.unity.UnityActivity

class UnityPlayerView : LinearLayout {
    private fun initView(context: Context) {
        val mainActivity = context as UnityActivity
        val myView = mainActivity.getUnityPlayer().view
        if (myView.parent != null) (myView.parent as LinearLayout).removeView(myView)
        mainActivity.getUnityPlayer().requestFocus()
        mainActivity.getUnityPlayer().windowFocusChanged(true)
        addView(myView)
    }

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }
}