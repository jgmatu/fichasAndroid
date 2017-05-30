package es.urjc.mov.javsan.cards.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.urjc.mov.javsan.cards.structures.Card;

public class ReqCreateCard extends Message {

    private Card card;

    public ReqCreateCard(Card c) {
        type = Message.RQCREATECARD;
        card = c;
    }

    public ReqCreateCard() {
        type = Message.RQCREATECARD;
        card = new Card();
    }

    @Override
    public void read(InputStream rx) throws IOException {
        card.read(rx);
    }

    @Override
    public void write(OutputStream tx) throws IOException {
        super.write(tx);
        card.write(tx);
    }
}