package com.dicoding.storyapp.utils

import com.dicoding.storyapp.data.database.StoryEntity

object DataStoryDummy {
    fun generateDummyStoryEntity(): List<StoryEntity> {
        val storyList = ArrayList<StoryEntity>()
        for (i in 0..10) {
            val story = StoryEntity(
                id = "1" ,
                name = "Story 1" ,
                description = "Deskripsi Story 1" ,
                photoUrl = "https://example.com/photo1.jpg" ,
                lat = -6.2 ,
                lon = 106.8 ,
                createdAt = "2025-08-26T10:00:00Z"
            )

            storyList.add(story)
        }
        return storyList
    }
}
