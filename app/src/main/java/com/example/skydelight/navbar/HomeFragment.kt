package com.example.skydelight.navbar

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.skydelight.BuildConfig
import com.example.skydelight.R
import com.example.skydelight.custom.AppDatabase
import com.example.skydelight.custom.ElementsEditor
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

        // Enabling links
        Linkify.addLinks(binding.txtCredits, Linkify.WEB_URLS)
        binding.txtCredits.movementMethod = LinkMovementMethod.getInstance()
    }

    // Function to connect with the api
    fun showAdvice(function:() -> Unit) {
        // Changing texts
        binding.txtTitle.text = getString(R.string.loading)
        binding.textView.text = getString(R.string.loading)
        binding.txtCredits.text = getString(R.string.loading)

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
                                showBackground()
                            }
                        }
                }
        }
    }

    // Getting wallpaper from pexels API
    private fun showBackground() {
        val pageNumber = (1..8000).shuffled().last()
        val request = Request.Builder()
            .url("https://api.pexels.com/v1/search?query=nature wallpaper&orientation=portrait&per_page=1&size=small&page=$pageNumber")
            .addHeader("Authorization", BuildConfig.API_KEY_PEXELS).get().build()

        ValidationsDialogsRequests().httpPetition(request, findNavController().context, requireView(),
            requireActivity(),null, null, null, null, null,
            null, null,null, null) {

            // Getting data
            val imageUrlData = JSONObject(it).getString("photos")

            // Getting image url
            var imageUrl = imageUrlData.substringAfter("https:\\/\\/images.pexels.com\\/photos\\/")
                .substringBefore(".jpeg").replace("\\", "")
            imageUrl = "https://images.pexels.com/photos/$imageUrl.jpeg"

            // Getting photographer info
            val photographerName = imageUrlData.substringAfter("\"photographer\":\"")
                .substringBefore("\"")

            // Getting photographer profile link
            var photographerUrl = imageUrlData.substringAfter("https:\\/\\/www.pexels.com\\/@")
                .substringBefore("\"")
            photographerUrl = "https://www.pexels.com/@$photographerUrl/"
            photographerUrl = "<a href=\"$photographerUrl\">$photographerName</a>"

            // Creating photo and pexels links
            val photoUrl = "<a href=\"$imageUrl\">Photo</a>"
            val pexelsUrl = "<a href=\"https://www.pexels.com/\">Pexels</a>"
            
            // Loading image
            MainScope().launch {
                activity?.let { activity ->
                    context?.let { context ->
                        if(!activity.isDestroyed) {
                            // Showing photo, photographer and pexels links
                            binding.txtCredits.text = Html.fromHtml("$photoUrl by $photographerUrl\non $pexelsUrl",
                                Html.FROM_HTML_MODE_COMPACT)
                            binding.txtCredits.setLinkTextColor(ElementsEditor().getColor(requireContext(), R.attr.text_color))

                            // Verify if user saved dark theme
                            updateTheme {
                                // Creating animation instances
                                val fadeOutAnimation: Animation = AlphaAnimation(1.0f, 0.0f)
                                val fadeInAnimation: Animation = AlphaAnimation(0.0f, 1.0f)

                                // Setting animation duration
                                fadeOutAnimation.duration = 500
                                fadeInAnimation.duration = 500

                                binding.imgBackground.startAnimation(fadeOutAnimation)
                                Glide.with(context)
                                    .load(imageUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .skipMemoryCache(true)
                                    .dontAnimate()
                                    .dontTransform()
                                    .priority(Priority.IMMEDIATE)
                                    .format(DecodeFormat.DEFAULT)
                                    .into(binding.imgBackground)
                                binding.imgBackground.startAnimation(fadeInAnimation)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateTheme(function:() -> Unit) {
        // Launching room database connection
        MainScope().launch {
            // Creating connection to database
            val user = Room.databaseBuilder(findNavController().context, AppDatabase::class.java, "user")
                .fallbackToDestructiveMigration().build().userDao().getUser()[0]

            if(user.isDarkTheme != null)
                if(user.isDarkTheme != isDarkTheme(requireActivity()))
                    if(user.isDarkTheme == true)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                else function()
            else function()
        }
    }

    private fun isDarkTheme(activity: Activity): Boolean {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}