package com.laurens.storyappdicoding.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKeys(remoteKeysList: List<RemoteKeys>)

    @Query("SELECT * FROM remote_keys WHERE id = :keyId")
    suspend fun fetchRemoteKeysById(keyId: String): RemoteKeys?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}