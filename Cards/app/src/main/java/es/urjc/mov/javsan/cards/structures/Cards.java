package es.urjc.mov.javsan.cards.structures;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import es.urjc.mov.javsan.cards.protocol.Message;


/**
 * This class represent a collection of cards.
 *
 * When is written or read from the socket the
 * cards know write and read itself.
 */
public class Cards {

    private static final String TAG = Cards.class.getSimpleName();
    private ArrayList<Card> cards;

    public Cards () {
        cards = new ArrayList<>();
    }

    public Cards (ArrayList<Card> c) {
        cards = c;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void write(OutputStream tx) throws IOException {
        Message.writeInt(tx, cards.size());

        for (Card c : cards) {
            c.write(tx);
        }
    }

    public void read(InputStream rx) throws IOException {
        int length = Message.readInt(rx);

        for (int i = 0; i < length ; i++) {
            Card c = new Card();
            c.read(rx);
            cards.add(c);
        }
    }

    @Override
    public String toString() {
        String result = "Cards\n";

        for (Card c : cards) {
            result += String.format("Card : %s\n", c.toString());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Cards)) {
            return false;
        }
        Cards c = (Cards) o;
        if (cards.size() != c.cards.size()) {
            return false;
        }
        for (int i = 0 ; i < cards.size() ; i++) {
            if (!cards.get(i).equals(c.cards.get(i))) {
                return false;
            }
        }
        return true;
    }
}
