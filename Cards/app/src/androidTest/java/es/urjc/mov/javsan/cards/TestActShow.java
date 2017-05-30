package es.urjc.mov.javsan.cards;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import es.urjc.mov.javsan.cards.fichas.CardShow;
import es.urjc.mov.javsan.cards.fichas.CardsDataBase;
import es.urjc.mov.javsan.cards.fichas.CardsTable;
import es.urjc.mov.javsan.cards.fichas.R;
import es.urjc.mov.javsan.cards.structures.Card;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class TestActShow {

    @Rule
    public final ActivityTestRule<CardShow> mRule =
            new ActivityTestRule<>(CardShow.class);

    private Card card;

    @Test
    public void testShowCard() throws Exception {
        TestTools tools = new TestTools(mRule.getActivity());

        createCardTest(tools);

        tools = new TestTools(launchActivity());

        checkUIShowCard();
        tools.rotateScreen();
        checkUIShowCard();
        tools.cleanDB();
    }

    private void checkUIShowCard() {
        onView(withId(R.id.card_name)).check(matches(isDisplayed()));
        onView(withId(R.id.card_description)).check(matches(isDisplayed()));
        onView(withId(R.id.images_cards)).check(matches(isDisplayed()));

        //onView(withId(R.id.card_name)).check(matches(withText(card.getName())));
        //onView(withId(R.id.card_description)).check(matches(withText(card.getCardMeta()
        //       .getDescription())));
    }

    private void createCardTest(TestTools tools) throws IOException {
        card = new Card(0, "Test", "Test Desc", "Easy", "Bike", tools.getEntries("0Test"));
        CardsDataBase db = new CardsDataBase(mRule.getActivity().getApplicationContext());

        db.onUpgrade(db.getWritableDatabase(), 0, 1);
        db.insertCard(card);
        db.close();
    }

    private AppCompatActivity launchActivity() {
        Intent i = new Intent();

        i.putExtra(CardsTable.IDCARD, 0);
        i.putExtra(CardsTable.NAMECARD, "Test");
        return mRule.launchActivity(i);
    }
}
