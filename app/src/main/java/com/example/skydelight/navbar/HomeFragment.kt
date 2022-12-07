package com.example.skydelight.navbar

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.skydelight.BuildConfig
import com.example.skydelight.MainActivity
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ValidationsDialogsRequests
import com.example.skydelight.databinding.FragmentNavbarHomeBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class HomeFragment : Fragment() {

    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarHomeBinding

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarHomeBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_home, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Showing initial random advice
        showAdvice{}
    }

    // Function to connect with the api
    @SuppressLint("SetTextI18n")
    fun showAdvice(function:() -> Unit) {
        // Changing texts
        binding.txtTitle.text = getString(R.string.loading)
        binding.textView.text = getString(R.string.loading)

        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val userDao = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao()
            val user = userDao.getUser()[0]

            // Making http request
            var request = Request.Builder().url("https://apiskydelight.herokuapp.com/api/lista-testsqv-personal/")
                .addHeader("Authorization", "Bearer " + user.token)
                .addHeader("KEY-CLIENT", BuildConfig.API_KEY)
                .post(FormBody.Builder().add("email", user.email).build()).build()

            if(isAdded)
                ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(),
                    requireActivity(),null, null, null, null, null,
                    null, 500,null, null) {
                    // Changing http body to json
                    val arrayString = JSONObject(it).getString("data")

                    // Arguments to Post Request
                    val formBody = FormBody.Builder()

                    // If user has answered at least 1 SVQ test
                    if(arrayString != "[]" && user.advice) {
                        // Getting last SVQ test answered
                        val arrayObject = JSONArray(arrayString).getJSONObject(JSONArray(arrayString).length()-1)
                        val questionsList = ArrayList<Int>()

                        // Saving bad answers from user
                        for(i in 1..20)
                            if(arrayObject.getInt("pregunta$i") < 4)
                                questionsList.add(i)

                        // Preparing selected answers to http petition
                        if(questionsList.size < 20)
                            formBody.add("type_advice", "especifico")
                                    .add("list_excluded", questionsList.toString())
                        else
                            formBody.add("type_advice", "general")
                                    .add("list_excluded", "[]")
                    } else
                        formBody.add("type_advice", "general")
                                .add("list_excluded", "[]")

                    // Making http request
                    request = Request.Builder().url("https://apiskydelight.herokuapp.com/api/consejo")
                        .post(formBody.build()).build()

                    // Toggle advice boolean
                    user.advice = !user.advice

                    // Launching room database connection
                    MainScope().launch { userDao.updateUser(user) }

                    if(isAdded)
                        ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(),
                            requireActivity(),null, null, null, null, null,
                            null, 500,null, null) {
                            // Changing http body to json and changing advice text
                            MainScope().launch {
                                binding.txtTitle.text = "Recomendación\n${JSONObject(it).getString("id")} de 50"
                                binding.textView.text = JSONObject(it).getString("consejo")
                                function()

                                // Show random background
                                showBackground(true)
                            }
                        }
                }
        }
    }

    private fun showBackground(animation: Boolean = false) {
        val imageArray = arrayOf(R.drawable.wallpaper_beach_3, R.drawable.wallpaper_flowers,
            R.drawable.wallpaper_leafs, R.drawable.wallpaper_mountain, R.drawable.wallpaper_mountain_2,
            R.drawable.wallpaper_road, R.drawable.wallpaper_sea, R.drawable.wallpaper_sky,
            R.drawable.wallpaper_tropical, R.drawable.wallpaper_wheat)

        if(animation) {
            val fadeOutAnimation: Animation = AlphaAnimation(1.0f, 0.0f)
            val fadeInAnimation: Animation = AlphaAnimation(0.0f, 1.0f)

            fadeOutAnimation.duration = 500
            fadeInAnimation.duration = 500

            binding.imgBackground.startAnimation(fadeOutAnimation)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.imgBackground.setImageResource(imageArray[(imageArray.indices).random()])
                binding.imgBackground.startAnimation(fadeInAnimation)
            }, 500)
        } else
            binding.imgBackground.setImageResource(imageArray[(imageArray.indices).random()])
    }
}