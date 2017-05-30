package es.urjc.mov.javsan.cards.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * When any request is send the server
 * can response with error because the
 * server crash or the card created is
 * already created or there is not cards
 * near of location send to server.
 */
public class RespError extends Message {

    private String err;

    public RespError() {
        type = Message.RESPERR;
    }

    @Override
    public void read(InputStream rx) throws IOException {
        err = readString(rx);
    }

    @Override
    public void write(OutputStream tx) throws IOException {
        super.write(tx);
        writeString(tx, err);
    }

    @Override
    public String toString() {
        return "Error : " + err;
    }
}
