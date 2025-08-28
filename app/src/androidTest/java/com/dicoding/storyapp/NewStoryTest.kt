package com.dicoding.storyapp

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.dicoding.storyapp.utils.EspressoIdlingResource
import com.dicoding.storyapp.view.main.MainActivity
import com.dicoding.storyapp.view.newstory.NewStoryActivity
import org.hamcrest.Matchers.allOf
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewStoryTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA
    )

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    @Test
    fun addStoryFlow() {

        ActivityScenario.launch(MainActivity::class.java)


        onView(withId(R.id.fabAddStory)).perform(click())


        onView(withId(R.id.descriptionEditText)).check(matches(isDisplayed()))


        onView(withId(R.id.descriptionEditText))
            .perform(typeText("Cerita dari UI test"), closeSoftKeyboard())


        val expectedIntent = allOf(
            hasAction(Intent.ACTION_PICK),
            hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )

        val resultData = Intent().apply {
            data = Uri.parse("android.resource://${ApplicationProvider.getApplicationContext<android.content.Context>().packageName}/drawable/ic_launcher_background")
        }
        val result = Instrumentation.ActivityResult(android.app.Activity.RESULT_OK, resultData)
        Intents.intending(expectedIntent).respondWith(result)

        onView(withId(R.id.galleryButton)).perform(click())

      
        onView(withId(R.id.uploadButton)).perform(click())

  
        onView(withId(R.id.rvStoryList)).check(matches(isDisplayed()))
    }

    @Test
    fun testCancelAddStory() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.fabAddStory)).perform(click())

 
        onView(withContentDescription("Navigate up")).perform(click())

        onView(withId(R.id.rvStoryList)).check(matches(isDisplayed()))
    }

}
