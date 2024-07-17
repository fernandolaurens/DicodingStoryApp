package com.laurens.storyappdicoding.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.laurens.storyappdicoding.data.pref.ListStoryItem

@Dao
interface CeritaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItem>)

    @Query("SELECT * FROM cerita")
    fun getAllStory(): PagingSource<Int, ListStoryItem>

    @Query("DELETE FROM cerita")
    suspend fun deleteAll()
}