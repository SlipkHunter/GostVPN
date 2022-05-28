package com.slipkprojects.gostvpn.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.slipkprojects.gostvpn.Utils
import com.slipkprojects.gostvpn.databinding.ActivityAboutBinding
import java.io.IOException

class AboutActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setLinksFromAssets()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setLinksFromAssets() {
        try {
            val aboutText = assets.open("LINKS.txt")
                .bufferedReader()
                .readText()

            binding.tvLinks.text = Utils.fromHtmlCompat(aboutText)
        } catch (e: IOException) {
            binding.tvLinks.text = ""
        }
    }
}