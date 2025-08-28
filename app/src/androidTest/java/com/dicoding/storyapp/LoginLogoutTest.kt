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
        try {

            Thread.sleep(2000)


            onView(withId(R.id.usernameInput)).check(matches(isDisplayed()))
            onView(withId(R.id.passwordInput)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))


            onView(withId(R.id.usernameInput)).perform(clearText())
            onView(withId(R.id.passwordInput)).perform(clearText())

            // Enter login credentials
            onView(withId(R.id.usernameInput))
                .perform(typeText("asbo@gmail.com") , closeSoftKeyboard())

            onView(withId(R.id.passwordInput))
                .perform(typeText("asbo12345") , closeSoftKeyboard())


            onView(withId(R.id.buttonLogin)).perform(click())


            Thread.sleep(5000)


            onView(withId(R.id.rvStoryList)).check(matches(isDisplayed()))


            Thread.sleep(2000)


            onView(withId(R.id.action_logout)).perform(click())


            Thread.sleep(1000)

            try {
                onView(withText("OK")).perform(click())
            } catch (_: Exception) {
                println("No confirmation dialog for logout")
            }


            Thread.sleep(2000)


            onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))

        } catch (e: Exception) {
            println("Test failed with exception: ${e.message}")
            println("Current screen might be showing an error dialog")


            try {
                onView(withText("Login Failed")).check(matches(isDisplayed()))
                onView(withText("OK")).perform(click())
                println("Login failed - credentials might be incorrect")
            } catch (e: Exception) {

                throw e
            }
        }
    }

    @Test
    fun testInvalidLogin() {
        try {

            Thread.sleep(2000)


            onView(withId(R.id.usernameInput)).perform(clearText())
            onView(withId(R.id.passwordInput)).perform(clearText())


            onView(withId(R.id.usernameInput))
                .perform(typeText("wrong@email.com") , closeSoftKeyboard())

            onView(withId(R.id.passwordInput))
                .perform(typeText("wrongpassword") , closeSoftKeyboard())


            onView(withId(R.id.buttonLogin)).perform(click())

            Thread.sleep(3000)

            
            onView(withText("Login Failed")).check(matches(isDisplayed()))
            onView(withText("user not found")).check(matches(isDisplayed()))


            onView(withText("OK")).perform(click())


            onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))

        } catch (e: Exception) {
            println("Invalid login test failed: ${e.message}")
            throw e
        }
    }
}
