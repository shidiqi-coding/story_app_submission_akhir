package com.dicoding.storyapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dicoding.storyapp.utils.EspressoIdlingResource
import com.dicoding.storyapp.view.authenticator.login.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginLogoutTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginAndLogoutFlow() {

        onView(withId(R.id.usernameInput))
            .perform(typeText("asbo@gmail.com") , closeSoftKeyboard())


        onView(withId(R.id.passwordInput))
            .perform(typeText("asbo12345") , closeSoftKeyboard())


        onView(withId(R.id.buttonLogin)).perform(click())


        onView(withId(R.id.rvStoryList)).check(matches(isDisplayed()))


        onView(withId(R.id.action_logout)).perform(click())


        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))
    }
}
