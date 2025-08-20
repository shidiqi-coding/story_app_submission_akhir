package com.dicoding.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dicoding.storyapp.data.database.StoryDatabase
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.data.retrofit.ApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        1
                    } else {
                        (state.pages.size) + 1
                    }
                }
            }

            val response = apiService.getStories(token, page, state.config.pageSize)
            val endOfPaginationReached = response.listStory.isNullOrEmpty()

            val storyDao = database.storyDao()
            if (loadType == LoadType.REFRESH) {
                storyDao.deleteAll()
            }

            response.listStory?.let { stories ->
                storyDao.insertStory(
                    stories.map { story ->
                        StoryEntity(
                            id = story.id ,
                            name = story.name,
                            description = story.description ?:"",
                            photoUrl = story.photoUrl,
                            createdAt = story.createdAt,
                            lat = story.lat,
                            lon = story.lon
                        )
                    }
                )
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
