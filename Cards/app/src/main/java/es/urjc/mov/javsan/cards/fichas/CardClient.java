package es.urjc.mov.javsan.cards.fichas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import es.urjc.mov.javsan.cards.protocol.Message;
import es.urjc.mov.javsan.cards.protocol.ReqCards;
import es.urjc.mov.javsan.cards.protocol.ReqCreateCard;
import es.urjc.mov.javsan.cards.protocol.RespCardCreated;
import es.urjc.mov.javsan.cards.protocol.RespCards;
import es.urjc.mov.javsan.cards.protocol.RespError;
import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.Cards;
import es.urjc.mov.javsan.cards.structures.ReplyError;
import es.urjc.mov.javsan.cards.structures.Location;


/**
 * This class realize the task to communicate with the server.
 *
 * Use a protocol transparent to other class those classes only
 * realize the function call and card client realize the communication
 * with the respository server and return the result to the class whose
 * called CardClient.
 */
public class CardClient {

    private static final String TAG = CardClient.class.getSimpleName();

    private Socket socket;
    private InputStream rx;
    private OutputStream tx;

    public CardClient (String hostname , int port) throws ConnectException, UnknownHostException, IOException {
        boolean fail = false;
        try {

            socket = new Socket(hostname ,port);
            tx =  socket.getOutputStream();
            rx = socket.getInputStream();

        } catch (ConnectException c) {

            fail = true;
            throw new ConnectException("Connection refused : " + c);

        } catch (UnknownHostException e) {

            fail = true;
            throw new UnknownHostException("Can't connect to host : " + e);

        } catch (IOException e){

            fail = true;
            throw new IOException("IO error in stream socket :" + e);

        } finally {
            connectFail(fail);
        }
    }

    /**
     * Method to create card, the methos comm with the server and return
     * the result of the request to create card.
     *
     * @param c Card neccesary to send at server...
     * @return A string saying if the card was created.
     * @throws ReplyError when the card was not created.
     * @throws IOException Error in the socket...
     */
    public void createCard(Card c) throws ReplyError, IOException {
        ReqCreateCard reqCreateCard = new ReqCreateCard(c);

        try {
            reqCreateCard.write(tx);
            Message msg = Message.produce(rx);

            if (msg instanceof RespCardCreated) {
                ;
            } else if (msg instanceof RespError) {
                RespError respError = (RespError) msg;
                throw new ReplyError(respError.toString());
            }
        } catch (IOException e) {
            throw new IOException(String.format("Error creating card protocol : %s", e.toString()));
        }
    }

    /**
     * Method whose request the cards from location passed like parameter
     * to server respository.
     *
     * @param location Location where the server calc the cards in 10 KM ratio.
     * @return Cards whose where founded on the ratio in other case of error null.
     * @throws Exception No cards where founded then you not must check cards results returned.
     */
    public Cards ratioCards(Location location) throws ReplyError, IOException {
        ReqCards reqCards = new ReqCards(location);
        Cards cards = null;

        try {
            reqCards.write(tx);
            Message msg = Message.produce(rx);
            if (msg instanceof RespCards) {

                RespCards respCards = (RespCards) msg;
                cards = respCards.getCards();

            } else if (msg instanceof RespError) {
                RespError respError = (RespError) msg;
                throw new ReplyError(respError.toString());
            }
        } catch (IOException e) {
            throw new IOException(e.toString());
        }
        return cards;
    }

    /**
     * Close the connection with the server when the user of this class
     * need it.
     */
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            ;
        }
    }

    private void connectFail (boolean fail) {
        String infofail = "Closing Socket connection fail...";
        String err = "Error closing connection later fail : ";

        try {
            if (fail && socket != null) {
                System.out.println(infofail);
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println(err + e);
        }
    }
}
