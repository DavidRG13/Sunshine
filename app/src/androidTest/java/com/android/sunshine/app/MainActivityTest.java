package com.android.sunshine.app;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.PreferenceMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;
import com.android.sunshine.app.activities.MainActivity;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.android.sunshine.app.internal.EspressoInteractions.clickOnMenuOption;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
        MainActivity.class,
        true,     // initialTouchMode
        false);   // launchActivity. False so we can customize the intent per test method

    @Before
    public void setUp() {
        Intents.init();

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        App app = (App) instrumentation.getTargetContext().getApplicationContext();
        ApplicationTestComponent component = DaggerApplicationTestComponent.builder()
            .appModuleMock(new AppModuleMock(app))
            .build();
        app.setComponent(component);
        component.inject(this);
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void shouldShowCurrentSelectedLocationInMap() {
        MainActivity mainActivity = activityRule.launchActivity(new Intent());

        clickOnMenuOption(withText(R.string.action_settings), mainActivity);

        onData(Matchers.<Object>allOf(PreferenceMatchers.withKey(mainActivity.getString(R.string.pref_location_key)))).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(clearText(), typeText("99999"));
        onView(withText("OK")).perform(click());
        pressBack();

        clickOnMenuOption(withText(R.string.viewLocation), mainActivity);

        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse("geo:99999"))
        ));
    }

    @Test
    public void shouldContainMockedData() {
        activityRule.launchActivity(new Intent());

        onView(withText("manana")).check(matches(isDisplayed()));
    }

    @Test
    public void shouldDisplayDetailsOnItemClicked() {
        activityRule.launchActivity(new Intent());

        onView(withId(R.id.recycler_view_forecast)).perform(actionOnItem(hasDescendant(withText("manana")), click()));

        onView(withId(R.id.detail_date_textview)).check(matches(withText("manana")));
    }
}
