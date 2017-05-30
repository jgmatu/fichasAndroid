package es.urjc.mov.javsan.cards;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;

import es.urjc.mov.javsan.cards.fichas.CardClient;
import es.urjc.mov.javsan.cards.fichas.CardSearch;
import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.Cards;
import es.urjc.mov.javsan.cards.structures.ReplyError;
import es.urjc.mov.javsan.cards.structures.Location;

import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TestProtocol {

    private final String IP = "10.0.2.2";
    private final int PORT = 2000;

    private AppCompatActivity activity;

    @Rule
    public final ActivityTestRule<CardSearch> mRule =
            new ActivityTestRule<>(CardSearch.class);

    @Test
    public void testProtocol() throws Exception {
        activity = mRule.getActivity();

        requestCreateCards();
        requestCardsBadLoc();
        reqCardsAlCreated();
        requestCardsGoodLoc();
        new TestTools(activity).cleanDB();
    }

    private void requestCreateCards() throws IOException {
        Cards cards = new Cards(getCards());

        try {
            for (Card c : cards.getCards()) {
                CardClient cli = new CardClient(IP, PORT);
                cli.createCard(c);
                cli.close();
            }
        } catch (ReplyError e) {
            fail();
        }
    }

    private void reqCardsAlCreated() throws IOException {
        Cards cards = new Cards(getCards());

        for (Card c : cards.getCards()) {
            try {
                CardClient cli = new CardClient(IP, PORT);
                cli.createCard(c);
                cli.close();
                fail();
            } catch (ReplyError e) {
            }
        }
    }

    private void requestCardsGoodLoc () throws IOException {
        try {
            CardClient cli = new CardClient(IP, PORT);
            cli.ratioCards(new Location(-12.0f, 22.032f, 99));
            cli.close();
        } catch (ReplyError e) {
            fail();
        }
    }

    private void requestCardsBadLoc() throws IOException {
        try {
            CardClient cli = new CardClient(IP, PORT);
            cli.ratioCards(new Location(0f, 0f, 100));
            cli.close();
            fail();
        } catch (ReplyError e) {
            ;
        }
    }

    private ArrayList<Card> getCards() throws IOException {
        ArrayList<Card> cards = new ArrayList<>();
        TestTools tools = new TestTools(activity);

        Card c1 = new Card(1, "Card1", "This is a card1", "easy", "Climb", tools.getEntries("1Card1"));
        Card c2 = new Card(2, "Card2", "This is a card2", "normal", "Hiking", tools.getEntries("2Card2"));
        Card c3 = new Card(3, "Card3", "This is a card3", "hard", "Bike", tools.getEntries("3Card3"));

        cards.add(c1);
        cards.add(c2);
        cards.add(c3);
        return cards;
    }
}
