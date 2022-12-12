package com.example.skydelight.navbar

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.skydelight.R
import com.example.skydelight.custom.ElementsEditor
import com.example.skydelight.databinding.FragmentNavbarProfileWebBinding

private const val TITLE_PARAM = "title"
private const val URL_PARAM = "url"

class ProfileWebFragment : Fragment() {
    // Binding variable to use elements in the xml layout
    private lateinit var binding : FragmentNavbarProfileWebBinding

    // Variables to receive data from other fragments
    private var title: String? = null
    private var originalUrl: String? = null

    // Getting data from other fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE_PARAM)
            originalUrl = it.getString(URL_PARAM)
        }
    }

    // Creating the fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentNavbarProfileWebBinding.inflate(inflater, container, false)
        return binding.root

        // return inflater.inflate(R.layout.fragment_navbar_profile, container, false)
    }

    // After the view is created we can do things
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enabling links in title
        Linkify.addLinks(binding.txtTitle, Linkify.WEB_URLS)
        binding.txtTitle.movementMethod = LinkMovementMethod.getInstance()

        // Showing link in title
        val txtTitle = "<a href=\"$originalUrl\">$title</a>"
        binding.txtTitle.text = Html.fromHtml(txtTitle, Html.FROM_HTML_MODE_COMPACT)
        try { binding.txtTitle.setLinkTextColor(ElementsEditor().getColor(context, R.attr.text_color))
        } catch(e: java.lang.IllegalStateException) {}

        // Instance of webview and settings
        // Blocking url redirecting to another website
        binding.webView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if(url == originalUrl || url.startsWith(originalUrl.toString()))
                    view?.loadUrl(url)
                return true
            }
        }
        // Allow button interaction
        binding.webView.settings.javaScriptEnabled = true

        // Loading url
        binding.webView.loadUrl(originalUrl!!)

        binding.btnReturn.setOnClickListener {
            // Fragment enters from left
            (parentFragment as NavBarFragment).updateNavBarHost(ProfileFragment(), R.id.nav_profile, false)
        }
    }
}