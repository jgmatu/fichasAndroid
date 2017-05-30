package es.urjc.mov.javsan.cards.protocol;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.urjc.mov.javsan.cards.structures.Cards;

public class RespCards extends Message {

    private static final String TAG = RespCards.class.getSimpleName();

    private Cards cards;

    public RespCards() {
        type = Message.RESPCARDS;
        cards = new Cards();
    }

    public Cards getCards() {
        return cards;
    }

    @Override
    public void read (InputStream rx) throws IOException {
        cards.read(rx);
    }

    @Override
    public void write(OutputStream tx) throws IOException {
        super.write(tx);
        cards.write(tx);
    }

    @Override
    public String toString() {
        return String.format("Response Cards : %s", cards.toString());
    }
}
