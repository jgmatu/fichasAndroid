package es.urjc.mov.javsan.cards;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.urjc.mov.javsan.cards.fichas.CardCreate;
import es.urjc.mov.javsan.cards.fichas.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class TestActCreate {

    private AppCompatActivity activity;

    private final String NAME = "TestOne";
    private final String DESC = "TestDesc";

    private String[] categories;

    @Rule
    public final ActivityTestRule<CardCreate> rule =
            new ActivityTestRule<>(CardCreate.class);


    @Test
    public void testCreateCard() {
        activity = rule.getActivity();
        categories = activity.getResources().getStringArray(R.array.category);
        TestTools tools = new TestTools(activity);

        checkUICreate();
        tools.rotateScreen();
        checkUICreate();
        tools.rotateScreen();

        fillForm(tools);

        checkForm();
        tools.rotateScreen();
        checkForm();

        // Create card not valid.
        onView(withId(R.id.create_card)).perform(click());
        onView(withId(R.id.create_hard)).check(matches(isDisplayed()));
        tools.cleanDB();
    }

    private void fillForm(TestTools tools) {
        onView(withId(R.id.card_name)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.card_name)).perform(typeText(NAME), closeSoftKeyboard());

        onView(withId(R.id.card_description)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.card_description)).perform(typeText(DESC), closeSoftKeyboard());

        onView(withId(R.id.create_hard)).perform(click());

        tools.selectSpinner(R.id.card_category, TestTools.CATEGORIES.CLIMB,
                categories[TestTools.CATEGORIES.CLIMB]);
    }

    private void checkForm() {
//        onView(withId(R.id.card_name)).check(matches(withText(NAME)));
//        onView(withId(R.id.card_description)).check(matches(withText(DESC)));

        onView(withId(R.id.create_hard)).check(matches(isChecked()));
        onView(withId(R.id.card_category)).check(matches(withSpinnerText(
                categories[TestTools.CATEGORIES.CLIMB])));
    }

    private void checkUICreate() {
        onView(withId(R.id.card_name)).check(matches(isDisplayed()));
        onView(withId(R.id.card_description)).check(matches(isDisplayed()));
        onView(withId(R.id.entry_card)).check(matches(isDisplayed()));
        onView(withId(R.id.create_card)).check(matches(isDisplayed()));
        onView(withId(R.id.cancel)).check(matches(isDisplayed()));
        onView(withId(R.id.card_category)).check(matches(isDisplayed()));
    }
}
