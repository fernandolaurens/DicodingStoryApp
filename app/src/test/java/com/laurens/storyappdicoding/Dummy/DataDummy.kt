package com.laurens.storyappdicoding.Dummy

import com.laurens.storyappdicoding.data.pref.ListStoryItem

object DataDummy {

    fun createDummyStories(): List<ListStoryItem> {
        val storyList: MutableList<ListStoryItem> = arrayListOf()
        for (index in 0..100) {
            val story = ListStoryItem(
                photoUrl = "photo $index",
                createdAt = "createdAt + $index",
                name = "name $index",
                description = "desc $index",
                lon = index.toDouble(),
                id = index.toString(),
                lat = index.toDouble()
            )
            storyList.add(story)
        }
        return storyList
    }
}