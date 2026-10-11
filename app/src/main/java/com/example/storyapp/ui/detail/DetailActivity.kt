package com.example.storyapp.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.utils.applySystemBarsPadding
import com.example.storyapp.utils.toFormattedDate

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding(topInsetTarget = binding.appBar)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvDetailName.text = intent.getStringExtra(EXTRA_NAME)
        binding.tvDetailDescription.text = intent.getStringExtra(EXTRA_DESCRIPTION)
        binding.tvDetailDate.text = intent.getStringExtra(EXTRA_CREATED_AT)?.toFormattedDate()
        Glide.with(this)
            .load(intent.getStringExtra(EXTRA_PHOTO_URL))
            .placeholder(R.drawable.ic_place_holder)
            .into(binding.ivDetailPhoto)
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFinishAfterTransition()
        return true
    }

    companion object {
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_PHOTO_URL = "extra_photo_url"
        private const val EXTRA_DESCRIPTION = "extra_description"
        private const val EXTRA_CREATED_AT = "extra_created_at"

        fun createIntent(context: Context, story: ListStoryItem): Intent =
            Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_NAME, story.name)
                putExtra(EXTRA_PHOTO_URL, story.photoUrl)
                putExtra(EXTRA_DESCRIPTION, story.description)
                putExtra(EXTRA_CREATED_AT, story.createdAt)
            }
    }
}
