package es.urjc.mov.javsan.cards.structures;

/**
 * This class represent the received of a message
 * of reply error, when this occurs the client card
 * throw this exception to say that a reply error
 * was received.
 */
public class ReplyError extends Exception {
    public ReplyError(String message) {
        super(message);
    }
}
