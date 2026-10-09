package com.example.locationapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationAppUiTest {

    @Test
    fun mainScreen_isDisplayed() {
        ActivityScenario.launch(MainActivity::class.java).use {

            onView(withId(R.id.addLocationButton))
                .check(matches(isDisplayed()))

            onView(withId(R.id.viewVisitedButton))
                .check(matches(isDisplayed()))

            onView(withId(R.id.analysisButton))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun addLocationButton_opensAddLocationScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {

            onView(withId(R.id.addLocationButton))
                .perform(click())

            onView(withId(R.id.nameInput))
                .check(matches(isDisplayed()))

            onView(withId(R.id.categoryText))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun emptyName_showsNameRequiredError() {
        ActivityScenario.launch(MainActivity::class.java).use {

            onView(withId(R.id.addLocationButton))
                .perform(click())

            onView(withId(R.id.saveButton))
                .perform(scrollTo(), click())

            onView(withId(R.id.nameInput))
                .check(matches(hasErrorText("Name is required")))
        }
    }

    @Test
    fun emptyCategory_showsCategoryRequiredError() {
        ActivityScenario.launch(MainActivity::class.java).use {

            onView(withId(R.id.addLocationButton))
                .perform(click())

            onView(withId(R.id.nameInput))
                .perform(
                    replaceText("UI Test Location"),
                    closeSoftKeyboard()
                )

            onView(withId(R.id.saveButton))
                .perform(scrollTo(), click())

            onView(withId(R.id.categoryText))
                .check(matches(hasErrorText("Category is required")))
        }
    }

    @Test
    fun visitedButton_opensVisitedLocationsScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {

            onView(withId(R.id.viewVisitedButton))
                .perform(click())

            onView(withId(R.id.recyclerViewVisited))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun analysisButton_opensAnalysisScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {

            onView(withId(R.id.analysisButton))
                .perform(click())

            onView(withText("Review Analysis"))
                .check(matches(isDisplayed()))
        }
    }
}