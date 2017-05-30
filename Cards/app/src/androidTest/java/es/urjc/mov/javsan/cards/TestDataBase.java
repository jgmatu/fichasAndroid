package es.urjc.mov.javsan.cards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import es.urjc.mov.javsan.cards.fichas.CardSearch;
import es.urjc.mov.javsan.cards.fichas.CardsDataBase;
import es.urjc.mov.javsan.cards.fichas.R;
import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.CardMeta;
import es.urjc.mov.javsan.cards.structures.Cards;
import es.urjc.mov.javsan.cards.structures.Entry;
import es.urjc.mov.javsan.cards.structures.Location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestDataBase {
    private AppCompatActivity activity;

    @Rule
    public final ActivityTestRule<CardSearch> mRule =
            new ActivityTestRule<>(CardSearch.class);

    @Test
    public void testDataBase() throws Exception {
        activity = mRule.getActivity();

        cleanDB();

        testCreateCards();
        testGetId();

        testSearchDB();
        cleanDB();
    }

    private void cleanDB() {
        CardsDataBase db = new CardsDataBase(activity.getApplicationContext());
        db.onUpgrade(db.getWritableDatabase(), 0 , 1);
        db.close();
    }

    private void testCreateCards () throws IOException {
        CardsDataBase db = new CardsDataBase(activity.getApplicationContext());
        Cards cards = new Cards(getCards());

        for (Card c : cards.getCards()) {
            db.insertCard(c);
        }

        for (Card c : cards.getCards()) {
            assertTrue(db.isExistCard(c));
        }
        db.close();
    }

    private void testGetId() {
        CardsDataBase db = new CardsDataBase(activity.getApplicationContext());

        for (int i = 0 ; i < 5 ; i++) {
            float id = (float) db.getCardId();

            ArrayList <Entry> entries = new ArrayList<>();
            entries.add(new Entry("mock.jpeg", new Location(id , id, 100)));

            Card c = new Card(db.getCardId(), "CardId", "MOcK", "Easy", "Bike", entries);

            db.insertCard(c);
            assertEquals(db.getCardId() - 1, (int) id);
        }

        db.close();
    }

    private void testSearchDB () {
        testGetCardMeta("Bike", "Easy");
        testGetCardMeta("Climb", "Easy");
        testGetCardMeta("Bike", "Hard");
        testGetCardByFeature("category", "Bike");
        testGetCardByFeature("skill", "Easy");
    }

    private void testGetCardByFeature(String feature, String value) {
        CardsDataBase db = new CardsDataBase(activity.getApplicationContext());
        ArrayList<CardMeta> cardMetas = db.getCardsMetaByFeature(feature, value);

        for (CardMeta cardMeta : cardMetas) {
            if (feature.equals("category")) {
                assertEquals(cardMeta.getCategory(), value);
            }
            if (feature.equals("skill")) {
                assertEquals(cardMeta.getSkill(), value);
            }
        }

        db.close();
    }

    private void testGetCardMeta(String category, String skill) {
        CardsDataBase db = new CardsDataBase(activity.getApplicationContext());
        ArrayList<CardMeta> cardsMeta = db.getCardsMeta(category, skill);

        for (CardMeta cMeta : cardsMeta) {
            assertEquals(cMeta.getCategory() , category);
            assertEquals(cMeta.getSkill(), skill);
        }
        db.close();
    }

    private ArrayList<Card> getCards() throws IOException {
        ArrayList<Card> cards = new ArrayList<>();
        float ltdlng = 100f;

        Card c1 = new Card(1, "Card1", "This is a card1", "Easy", "Climb",
                getEntries("1Card1", ltdlng));

        Card c2 = new Card(2, "Card2", "This is a card2", "Normal", "Hiking",
                getEntries("2Card2", ltdlng + 100f));

        Card c3 = new Card(3, "Card3", "This is a card3", "Hard", "Bike",
                getEntries("3Card3", ltdlng + 200f));

        cards.add(c1);
        cards.add(c2);
        cards.add(c3);
        return cards;
    }

    private ArrayList<Entry> getEntries(String name, float ltdlng) throws IOException {
        ArrayList<Entry> entries = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory() + File.separator + "/" + name;

        new File(path).mkdirs();

        File f1 = new File(path + "testOne.jpg");
        File f2 = new File(path + "testTwo.jpg");

        createTestImage(f1);
        createTestImage(f2);

        entries.add(new Entry(f1.getPath(), new Location(ltdlng, ltdlng, 100)));
        entries.add(new Entry(f2.getPath(), new Location(ltdlng - 20, ltdlng - 20, 100)));

        return entries;
    }


    private void createTestImage(File f) throws IOException {
        Bitmap b = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.category_bike);
        FileOutputStream fos = new FileOutputStream(f);

        b.compress(Bitmap.CompressFormat.JPEG, 100 , fos);
    }
}
