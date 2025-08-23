package com.dicoding.storyapp.view.testing


import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.dicoding.storyapp.R
import com.dicoding.storyapp.utils.EspressoIdlingResource
import com.dicoding.storyapp.view.authenticator.login.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Test

class LoginActivityTest {
    @Before
    fun setup() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingResource)
        ActivityScenario.launch(LoginActivity::class.java)

    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingResource)
    }

    @Test
    fun loginSuccessAndGoToMainActivity() {
        onView(withId(R.id.usernameInput))
            .perform(typeText("test@mail.com") , closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        onView(withId(R.id.rvStoryList)).check(matches(isDisplayed()))

    }

    @Test
    fun logoutSuccessBackToLoginActivity() {
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        onView(withText(R.string.logout)).perform(click())

        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))
    }


}