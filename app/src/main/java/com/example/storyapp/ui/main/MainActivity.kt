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
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.databinding.ItemStoryBinding
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.add.AddStoryActivity
import com.example.storyapp.ui.detail.DetailActivity
import com.example.storyapp.ui.login.LoginActivity
import com.example.storyapp.ui.maps.MapsActivity
import com.example.storyapp.utils.applySystemBarsPadding
import com.example.storyapp.widget.StoryWidget

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private val storyAdapter = StoryAdapter { story, itemBinding -> openDetail(story, itemBinding) }
    private var previousRefreshState: LoadState? = null
    private var scrollToTopOnNextLoad = false

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            scrollToTopOnNextLoad = true
            storyAdapter.refresh()
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
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }
        storyAdapter.addLoadStateListener { loadStates -> renderLoadStates(loadStates) }
        storyAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (scrollToTopOnNextLoad && positionStart == 0) {
                    binding.rvStory.scrollToPosition(0)
                    scrollToTopOnNextLoad = false
                }
            }
        })
        binding.swipeRefresh.setOnRefreshListener { storyAdapter.refresh() }
        binding.fabAdd.setOnClickListener {
            addStoryLauncher.launch(Intent(this, AddStoryActivity::class.java))
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                StoryWidget.refresh(this)
                navigateToLogin()
            } else if (!viewModel.stories.hasObservers()) {
                viewModel.stories.observe(this) { pagingData ->
                    storyAdapter.submitData(lifecycle, pagingData)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_map -> {
            startActivity(Intent(this, MapsActivity::class.java))
            true
        }
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

    private fun renderLoadStates(loadStates: CombinedLoadStates) {
        val refreshState = loadStates.refresh
        val isListEmpty = storyAdapter.itemCount == 0

        binding.progressBar.isVisible = refreshState is LoadState.Loading && !binding.swipeRefresh.isRefreshing
        if (refreshState !is LoadState.Loading) binding.swipeRefresh.isRefreshing = false

        when {
            refreshState is LoadState.Error && isListEmpty -> {
                val message = refreshState.error.localizedMessage ?: getString(R.string.error_connection)
                binding.tvMessage.text = getString(R.string.error_load_stories, message)
                binding.tvMessage.isVisible = true
            }
            refreshState is LoadState.NotLoading && loadStates.append.endOfPaginationReached && isListEmpty -> {
                binding.tvMessage.text = getString(R.string.no_story)
                binding.tvMessage.isVisible = true
            }
            else -> binding.tvMessage.isVisible = false
        }

        if (refreshState is LoadState.Error && !isListEmpty && previousRefreshState !is LoadState.Error) {
            Toast.makeText(this, R.string.error_connection, Toast.LENGTH_SHORT).show()
        }
        previousRefreshState = refreshState
    }

    private fun openDetail(story: ListStoryItem, itemBinding: ItemStoryBinding) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            Pair<View, String>(itemBinding.ivItemPhoto, getString(R.string.transition_photo)),
            Pair<View, String>(itemBinding.tvItemName, getString(R.string.transition_name))
        )
        startActivity(DetailActivity.createIntent(this, story), options.toBundle())
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
