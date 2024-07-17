package com.laurens.storyappdicoding.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.laurens.storyappdicoding.data.database.CeritaDatabase
import com.laurens.storyappdicoding.data.database.RemoteKeys
import com.laurens.storyappdicoding.data.pref.ListStoryItem
import com.laurens.storyappdicoding.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class CeritaRemoteMediator(
    private val db: CeritaDatabase,
    private val apiService: ApiService,
    private val authToken: String,
) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE = 1
    }

    override suspend fun initialize(): InitializeAction {
        Log.i("CeritaRemoteMediator", "Initialization triggered")
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                Log.i("CeritaRemoteMediator", "Load type: REFRESH")
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE
            }

            LoadType.PREPEND -> {
                Log.i("CeritaRemoteMediator", "Load type: PREPEND")
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                Log.i("CeritaRemoteMediator", "Load type: APPEND")
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getStories(authToken, page, state.config.pageSize).execute()
            }

            if (response.isSuccessful) {
                val storyResponse = response.body()
                if (storyResponse != null && !storyResponse.error) {
                    val responseData = storyResponse.listStory
                    Log.i("CeritaRemoteMediator", "Successfully loaded data: $responseData")
                    val endOfPaginationReached = responseData.isEmpty()

                    db.withTransaction {
                        if (loadType == LoadType.REFRESH) {
                            Log.i("CeritaRemoteMediator", "Clearing database for REFRESH load type")
                            db.remoteKeysDao().clearRemoteKeys()
                            db.getStoryDao().deleteAll()
                        }
                        Log.i("CeritaRemoteMediator", "Inserting data into database")
                        val prevKey = if (page == 1) null else page - 1
                        val nextKey = if (endOfPaginationReached) null else page + 1
                        val keys = responseData.map {
                            RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                        }
                        db.remoteKeysDao().insertAllKeys(keys)
                        db.getStoryDao().insertStory(responseData)
                        responseData.map {
                            Log.i("CeritaRemoteMediator", "Story name: ${it.name}")
                        }
                    }
                    return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                } else {
                    Log.e("CeritaRemoteMediator", "API error: ${storyResponse?.message}")
                    return MediatorResult.Error(Exception("API error: ${storyResponse?.message}"))
                }
            } else {
                Log.e("CeritaRemoteMediator", "Request failed: ${response.errorBody()}")
                return MediatorResult.Error(HttpException(response))
            }
        } catch (exception: Exception) {
            Log.e("CeritaRemoteMediator", "Exception during load: $exception")
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(pagingState: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return pagingState.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            db.remoteKeysDao().fetchRemoteKeysById(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(pagingState: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return pagingState.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            db.remoteKeysDao().fetchRemoteKeysById(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(pagingState: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return pagingState.anchorPosition?.let { position ->
            pagingState.closestItemToPosition(position)?.id?.let { id ->
                db.remoteKeysDao().fetchRemoteKeysById(id)
            }
        }
    }

}