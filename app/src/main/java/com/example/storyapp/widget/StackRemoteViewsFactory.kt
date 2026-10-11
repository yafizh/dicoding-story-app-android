package com.example.storyapp.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.di.Injection
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutionException

class StackRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val repository = Injection.provideRepository(context)
    private var stories: List<ListStoryItem> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        stories = when (val result = runBlocking { repository.getStories() }) {
            is ResultState.Success -> result.data
            else -> emptyList()
        }
    }

    override fun onDestroy() {
        stories = emptyList()
    }

    override fun getCount(): Int = stories.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.item_widget)
        val story = stories.getOrNull(position) ?: return views

        views.setTextViewText(R.id.tv_widget_name, story.name)
        try {
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(story.photoUrl)
                .centerCrop()
                .submit(IMAGE_SIZE, IMAGE_SIZE)
                .get()
            views.setImageViewBitmap(R.id.iv_widget_photo, bitmap)
        } catch (e: ExecutionException) {
            views.setImageViewResource(R.id.iv_widget_photo, R.drawable.ic_place_holder)
        } catch (e: InterruptedException) {
            views.setImageViewResource(R.id.iv_widget_photo, R.drawable.ic_place_holder)
        }
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false

    companion object {
        private const val IMAGE_SIZE = 400
    }
}
