package es.urjc.mov.javsan.cards.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class implement the response from the
 * server when the client send a card to become
 * created.
 *
 * The contain is ignored.
 *
 * We only see if the response created is received
 * in signal of the card was created on the server
 * repository.
 */
public class RespCardCreated extends Message {

    private String info;

    public RespCardCreated() {
        type = Message.REQCARDS;
        info = "?";
    }

    @Override
    public void read(InputStream rx) throws IOException {
        info = readString(rx);
    }

    @Override
    public void write(OutputStream tx) throws IOException {
        super.write(tx);
        writeString(tx, info);
    }

    @Override
    public String toString() {
        return info;
    }

}
