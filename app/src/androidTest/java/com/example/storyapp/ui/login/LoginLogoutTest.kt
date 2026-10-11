package com.example.storyapp.ui.login

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.storyapp.R
import com.example.storyapp.di.Injection
import com.example.storyapp.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginLogoutTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val email = "storyapp.uitest.${System.currentTimeMillis()}@mail.com"

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        val repository = Injection.provideRepository(context)
        runBlocking {
            repository.logout()
            repository.register(NAME, email, PASSWORD)
        }
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginAndLogout_success() {
        ActivityScenario.launch(LoginActivity::class.java)

        onView(withId(R.id.ed_login_email)).perform(typeText(email), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())

        onView(withId(R.id.rv_story)).check(matches(isDisplayed()))

        openActionBarOverflowOrOptionsMenu(context)
        onView(withText(R.string.menu_logout)).perform(click())

        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
    }

    private companion object {
        const val NAME = "UI Test"
        const val PASSWORD = "password123"
    }
}
