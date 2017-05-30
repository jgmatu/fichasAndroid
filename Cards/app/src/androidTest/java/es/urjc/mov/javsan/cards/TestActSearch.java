package es.urjc.mov.javsan.cards;


import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import es.urjc.mov.javsan.cards.fichas.CardSearch;
import es.urjc.mov.javsan.cards.fichas.CardsDataBase;
import es.urjc.mov.javsan.cards.fichas.R;
import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.Entry;
import es.urjc.mov.javsan.cards.structures.Location;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestActSearch {

    private AppCompatActivity activity;

    private String[] categories;
    private String[] skills;

    @Rule
    public final ActivityTestRule<CardSearch> mRule =
            new ActivityTestRule<>(CardSearch.class);

    @Test
    public void testActivitySearch() throws Exception {
        activity = mRule.getActivity();
        categories = activity.getResources().getStringArray(R.array.category);
        skills = activity.getResources().getStringArray(R.array.skill);
        TestTools tools = new TestTools(activity);

        tools.cleanDB();

        createCard();
        searchCard(tools);

        tools.cleanDB();
    }


    private void createCard() {
        CardsDataBase db = new CardsDataBase(activity.getApplicationContext());
        db.insertCard(new Card(1, "testOne", "testDesc", skills[TestTools.SKILLS.NORMAL],
                categories[TestTools.CATEGORIES.BIKE], getEntries()));
        db.close();
    }

    private ArrayList<Entry> getEntries() {
        ArrayList<Entry> entries = new ArrayList<>();
        Entry entry = new Entry("mock.jpeg" , new Location(23.4f, 33.12f, 10));

        entries.add(entry);
        entry = new Entry("mockOne.jpeg", new Location(31.2f, 31.3f, 100));
        entries.add(entry);
        return entries;
    }

    private void checkUISearch() {
        onView(withId(R.id.search_cards)).check(matches(isDisplayed()));
        onView(withId(R.id.search_skill)).check(matches(isDisplayed()));
        onView(withId(R.id.search_category)).check(matches(isDisplayed()));
        onView(withId(R.id.create_cards)).check(matches(isDisplayed()));
    }

    public void searchCard(TestTools tools) {
        checkUISearch();

        // Select the card to search normal skill and none category..
        tools.selectSpinner(R.id.search_skill, TestTools.SKILLS.NORMAL,
                skills[TestTools.SKILLS.NORMAL]);
        tools.selectSpinner(R.id.search_category, TestTools.CATEGORIES.NONE,
                categories[TestTools.CATEGORIES.NONE]);
        tools.rotateScreen();

        // Search the card with normal skill and none category...
        onView(withId(R.id.search_cards)).perform(click());
        checkResultSearch(true);

        // Come back to UI search...
        onView(withId(R.id.search_button)).perform(click());
        checkUISearch();

        // Select the card to search none skill and bike category...
        tools.selectSpinner(R.id.search_skill, TestTools.SKILLS.NONE,
                skills[TestTools.SKILLS.NONE]);
        tools.selectSpinner(R.id.search_category, TestTools.CATEGORIES.BIKE,
                categories[TestTools.CATEGORIES.BIKE]);
        tools.rotateScreen();

        // Search the card with none skill and bike category...
        onView(withId(R.id.search_cards)).perform(click());
        checkResultSearch(true);

        // Come back to UI search...
        onView(withId(R.id.search_button)).perform(click());
        checkUISearch();

        // Select the card with normal skill and bike category...
        tools.selectSpinner(R.id.search_skill, TestTools.SKILLS.NORMAL,
                skills[TestTools.SKILLS.NORMAL]);
        tools.selectSpinner(R.id.search_category, TestTools.CATEGORIES.BIKE,
                categories[TestTools.CATEGORIES.BIKE]);
        tools.rotateScreen();

        // Search the card with normal skill and none category...
        onView(withId(R.id.search_cards)).perform(click());
        checkResultSearch(true);

        // Come back to UI search...
        onView(withId(R.id.search_button)).perform(click());
        checkUISearch();

        tools.selectSpinner(R.id.search_skill, TestTools.SKILLS.EASY,
                skills[TestTools.SKILLS.EASY]);
        tools.selectSpinner(R.id.search_category, TestTools.CATEGORIES.CLIMB,
                categories[TestTools.CATEGORIES.CLIMB]);
        tools.rotateScreen();

        // Search the card with none skill and bike category...
        onView(withId(R.id.search_cards)).perform(click());
        checkResultSearch(false);

        tools.selectSpinner(R.id.search_skill, TestTools.SKILLS.NONE,
                skills[TestTools.SKILLS.NONE]);
        tools.selectSpinner(R.id.search_category, TestTools.CATEGORIES.NONE,
                categories[TestTools.CATEGORIES.NONE]);
        tools.rotateScreen();

        // Search the card with none skill and bike category...
        onView(withId(R.id.search_cards)).perform(click());
        checkResultSearch(false);
    }

    private void checkResultSearch(boolean valid) {
        if (valid) {
            onView(withId(R.id.table_cards)).check(matches(isDisplayed()));
            onView(withId(R.id.search_button)).check(matches(isDisplayed()));
        } else {
            onView(withId(R.id.table_cards)).check(matches(not(isDisplayed())));
            onView(withId(R.id.search_button)).check(matches(not(isDisplayed())));
        }
    }
}
