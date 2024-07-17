package com.laurens.storyappdicoding.view.Cerita

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.laurens.storyappdicoding.R
import com.laurens.storyappdicoding.data.pref.ListStoryItem
import com.laurens.storyappdicoding.databinding.ActivityCeritaDetailBinding

class CeritaDetailActivity : AppCompatActivity() {
    private lateinit var customBinding: ActivityCeritaDetailBinding
    private  var customStory: ListStoryItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customBinding = ActivityCeritaDetailBinding.inflate(layoutInflater)
        setContentView(customBinding.root)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            customStory = intent.getParcelableExtra(EXTRA_STORY)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            customStory = intent.getParcelableExtra(EXTRA_STORY, ListStoryItem::class.java)

        }
        showCustomDetail()
    }

    private fun showCustomDetail() {
        customBinding.tvStoryName.text = customStory?.name
        customBinding.tvStoryDesc.text = customStory?.description
        Glide.with(this)
            .load(customStory?.photoUrl)
            .into(customBinding.ivPhoto)
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}