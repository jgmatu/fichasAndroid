package es.urjc.mov.javsan.cards.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.urjc.mov.javsan.cards.structures.Location;

/**
 * This class request all the cards near from
 * one location sent to server...
 *
 * The request Cards contain the location
 * where the server must reply cards near
 * that location.
 */
public class ReqCards extends Message {

    private Location location;

    public ReqCards(Location l) {
        type = Message.REQCARDS;
        location = l;
    }

    public ReqCards() {
        type = Message.REQCARDS;
        location = new Location();
    }

    @Override
    public void read(InputStream rx) throws IOException {
        location.read(rx);
    }

    @Override
    public void write(OutputStream tx) throws IOException {
        super.write(tx);
        location.write(tx);
    }
}