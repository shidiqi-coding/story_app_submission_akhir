package com.dicoding.storyapp.view.testing

import com.dicoding.storyapp.data.database.StoryEntity

object DataStoryDummy {
     fun generateDummyStoryEntity(): List<StoryEntity>{
         val storyList = ArrayList<StoryEntity>()
         for (i in 0..10) {
             val story = StoryEntity(
                 id = i.toString(),
                 name ="Story $i",
                 description = "Description for story $i",
                 photoUrl = "https://picsum.photos/200/300?random=$i",
                 createdAt = "2023-01-01T00:00:0$i",
                 lat = -6.2 + i,
                 lon = 106.8 + i

             )
             storyList.add(story)

         }
         return storyList
     }
}