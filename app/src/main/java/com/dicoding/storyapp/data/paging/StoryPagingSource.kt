package com.dicoding.storyapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.data.retrofit.ApiService

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, StoryEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
        val page = params.key ?: 1
        val size = params.loadSize
        return try {
            val resp = apiService.getStories(token, page, size)
            val items = resp.listStory ?: emptyList()
            val entities = items.map {
                StoryEntity(
                    id = it.id,
                    name = it.name,
                    description = it.description ?: "",
                    photoUrl = it.photoUrl,
                    createdAt = it.createdAt,
                    lat = it.lat,
                    lon = it.lon
                )
            }
            val nextKey = if (items.isEmpty()) null else page + 1
            LoadResult.Page(
                data = entities,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }
}
