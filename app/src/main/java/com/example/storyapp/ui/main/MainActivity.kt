package com.example.storyapp.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.databinding.ItemStoryBinding
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.add.AddStoryActivity
import com.example.storyapp.ui.detail.DetailActivity
import com.example.storyapp.ui.login.LoginActivity
import com.example.storyapp.utils.applySystemBarsPadding
import com.example.storyapp.widget.StoryWidget

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private val storyAdapter = StoryAdapter { story, itemBinding -> openDetail(story, itemBinding) }
    private var scrollToTopOnNextLoad = false

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            scrollToTopOnNextLoad = true
            viewModel.getStories()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding(topInsetTarget = binding.appBar)
        setSupportActionBar(binding.toolbar)

        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.getStories() }
        binding.fabAdd.setOnClickListener {
            addStoryLauncher.launch(Intent(this, AddStoryActivity::class.java))
        }

        viewModel.session.observe(this) { user ->
            if (!user.isLogin) {
                StoryWidget.refresh(this)
                navigateToLogin()
            } else if (viewModel.stories.value == null) {
                viewModel.getStories()
            }
        }
        viewModel.stories.observe(this) { result -> renderStories(result) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_language -> {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            true
        }
        R.id.action_logout -> {
            viewModel.logout()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun renderStories(result: ResultState<List<ListStoryItem>>) {
        when (result) {
            is ResultState.Loading -> {
                binding.progressBar.isVisible = !binding.swipeRefresh.isRefreshing
                binding.tvMessage.isVisible = false
            }
            is ResultState.Success -> {
                stopLoading()
                storyAdapter.submitList(result.data) {
                    if (scrollToTopOnNextLoad) {
                        binding.rvStory.scrollToPosition(0)
                        scrollToTopOnNextLoad = false
                    }
                }
                binding.tvMessage.text = getString(R.string.no_story)
                binding.tvMessage.isVisible = result.data.isEmpty()
            }
            is ResultState.Error -> {
                stopLoading()
                val message = result.message ?: getString(R.string.error_connection)
                if (storyAdapter.itemCount == 0) {
                    binding.tvMessage.text = getString(R.string.error_load_stories, message)
                    binding.tvMessage.isVisible = true
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun stopLoading() {
        binding.progressBar.isVisible = false
        binding.swipeRefresh.isRefreshing = false
    }

    private fun openDetail(story: ListStoryItem, itemBinding: ItemStoryBinding) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_NAME, story.name)
            putExtra(DetailActivity.EXTRA_PHOTO_URL, story.photoUrl)
            putExtra(DetailActivity.EXTRA_DESCRIPTION, story.description)
            putExtra(DetailActivity.EXTRA_CREATED_AT, story.createdAt)
        }
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            Pair<View, String>(itemBinding.ivItemPhoto, getString(R.string.transition_photo)),
            Pair<View, String>(itemBinding.tvItemName, getString(R.string.transition_name))
        )
        startActivity(intent, options.toBundle())
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
