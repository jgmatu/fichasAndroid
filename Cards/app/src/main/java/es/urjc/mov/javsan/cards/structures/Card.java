package es.urjc.mov.javsan.cards.structures;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import es.urjc.mov.javsan.cards.protocol.Message;

/**
 * This class implements the structure of one
 * card in the application, that is when one
 * card is created the fields of the card represent
 * this class, and the class know read and write
 * both in file and in a socket.
 *
 * Note : in this class we create the directory when
 * the images will be storage.
 *
 * The name of directory will be id+name card.
 */
public class Card {

    private static final String TAG = Card.class.getSimpleName();

    private CardMeta cardMeta;
    private ArrayList<Entry> entries;

    public Card (int i, String n, String d, String s, String c, ArrayList<Entry> e) {
        cardMeta = new CardMeta(i , n , d, s , c);
        entries = e;
    }

    public Card (CardMeta cM, ArrayList<Entry> e) {
        cardMeta = cM;
        entries = e;
        createDir(cardMeta.getId(), cardMeta.getName());
    }

    public Card() {
        cardMeta = new CardMeta(-1, "" , "" , "" , "");
        entries = new ArrayList<>();
    }

    public int getId () {
        return cardMeta.getId();
    }

    public String getName() {
        return cardMeta.getName();
    }

    public boolean isInvalid() {
        return cardMeta.isInvalid() || entries.size() < 1 || isInvalid(entries);
    }

    public CardMeta getCardMeta() {
        return cardMeta;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public ArrayList<String> getImages() {
        ArrayList<String> images = new ArrayList<>();

        for (Entry e : entries) {
            images.add(e.getPathImage());
        }
        return images;
    }

    public void write(OutputStream tx) throws IOException {
        cardMeta.write(tx);

        Message.writeInt(tx, entries.size());
        for (Entry e : entries) {
            e.write(tx);
        }
    }

    public void read(InputStream rx) throws IOException {
        cardMeta.read(rx);
        String dir = createDir(cardMeta.getId(), cardMeta.getName());

        int length = Message.readInt(rx);
        for (int i = 0 ; i < length ; i++) {
            Entry e = new Entry(dir);

            e.read(rx);
            entries.add(e);
        }
    }

    @Override
    public String toString() {
        String result = cardMeta.toString();

        for (Entry e : entries) {
            result += String.format("Entry :%s\n", e.toString());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }
        Card c = (Card) o;
        return cardMeta.equals(c.cardMeta) && isEntries(c.entries);
    }

    private boolean isInvalid(ArrayList<Entry> entries) {
        for (Entry e : entries) {
            if (e.isInvalid()){
                return true;
            }
        }
        return false;
    }

    private boolean isEntries(ArrayList<Entry> e) {
        if (e.size() != entries.size()) {
            return false;
        }

        for (int i = 0 ; i < e.size() ; i++) {
            if (!e.get(i).equals(entries.get(i))) {
                return false;
            }
        }
        return true;
    }

    private String createDir(int id, String name) {
        String path = Environment.getExternalStorageDirectory() +
                        File.separator + String.format("%d%s/", id , name);
        boolean create = new File(path).mkdirs();

        if (!create) {
            Log.w(TAG, String.format("Error creating directory : %s\n", path));
            Log.w(TAG, String.format("The directory maybe exist"));
        }
        return path;
    }

}
