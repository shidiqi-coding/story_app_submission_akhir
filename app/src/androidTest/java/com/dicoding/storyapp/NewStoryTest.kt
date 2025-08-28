ackage com.dicoding.storyapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dicoding.storyapp.utils.EspressoIdlingResource
import com.dicoding.storyapp.view.authenticator.login.LoginActivity
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NewStoryTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        // Atur timeout IdlingResource biar ga cepat gagal
        IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS)
        IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS)

        // Register IdlingResource
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        // Unregister setelah test selesai
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    /**
     * Fungsi helper untuk login dan dismiss dialog sukses login
     */
    private fun loginToApp() {
        onView(withId(R.id.usernameInput))
            .perform(typeText("test@email.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordInput))
            .perform(typeText("12345678"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())

        // ✅ Handle dialog sukses login
        onView(withText("congratulations!"))
            .check(matches(isDisplayed()))
        onView(withText("continue"))
            .perform(click())

        // Pastikan RecyclerView tampil setelah login
        onView(withId(R.id.rvStoryList))
            .check(matches(isDisplayed()))
    }

    /**
     * Test: mencoba menambahkan cerita tanpa memilih gambar
     */
    @Test
    fun testAddStoryWithoutImage() {
        loginToApp()

        // Klik tombol tambah cerita (FAB)
        onView(withId(R.id.fabAddStory)).perform(click())

        // Isi deskripsi tanpa gambar
        onView(withId(R.id.descriptionEditText))
            .perform(typeText("Testing story"), closeSoftKeyboard())
        onView(withId(R.id.uploadButton)).perform(click())

        // Cek apakah muncul pesan error
        onView(withText("Please select an image first"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test: menambahkan cerita dengan gambar berhasil
     */
    @Test
    fun testAddStoryWithImage() {
        loginToApp()

        // Klik tombol tambah cerita (FAB)
        onView(withId(R.id.fabAddStory)).perform(click())

        // Pilih gambar dari galeri
        onView(withId(R.id.galleryButton)).perform(click())
        // ⬆️ Biasanya kamu perlu tambahkan Intent Stub / Espresso-Intents
        // untuk mock pilih gambar. Kalau sudah di-mock, ini cukup.

        // Isi deskripsi
        onView(withId(R.id.descriptionEditText))
            .perform(typeText("Story with image"), closeSoftKeyboard())

        // Upload
        onView(withId(R.id.uploadButton)).perform(click())

        // Cek apakah muncul notifikasi / dialog sukses
        onView(withText("Story uploaded successfully"))
            .check(matches(isDisplayed()))
    }
}
