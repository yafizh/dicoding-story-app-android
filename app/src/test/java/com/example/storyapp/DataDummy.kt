package com.example.storyapp

import com.example.storyapp.data.remote.response.ListStoryItem

object DataDummy {

    fun generateDummyStories(): List<ListStoryItem> = (0 until 100).map { index ->
        ListStoryItem(
            id = "story-$index",
            name = "User $index",
            description = "Description $index",
            photoUrl = "https://story-api.dicoding.dev/images/stories/photos-$index.jpg",
            createdAt = "2022-01-08T06:34:18.598Z",
            lat = -6.2 + index * 0.01,
            lon = 106.8 + index * 0.01
        )
    }
}
