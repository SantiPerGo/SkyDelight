package com.example.skydelight.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.example.skydelight.R
import java.util.*

class ViewPageAdapter(context: Context, imagesArray: Array<Int>?, count: Int = 1): PagerAdapter() {
    private val mImagesArray = imagesArray
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mCount = count

    override fun getCount(): Int { return mImagesArray?.size ?: mCount }

    override fun isViewFromObject(view: View, mObject: Any): Boolean { return view == mObject }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = layoutInflater.inflate(R.layout.image_item, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.imageViewMain)
        if(mImagesArray != null)
            imageView.setImageResource(mImagesArray[position])
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, mObject: Any) {
        container.removeView(mObject as LinearLayout)
    }
}