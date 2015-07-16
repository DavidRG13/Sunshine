package com.android.sunshine.app.internal;

import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;

public class EspressoInteractions {

    public static ViewInteraction clickOnMenuOption(final Matcher<View> viewMatcher, final Context context) {
        openActionBarOverflowOrOptionsMenu(context);
        return onView(viewMatcher).perform(click());
    }
}
