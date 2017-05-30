package es.urjc.mov.javsan.cards.structures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.urjc.mov.javsan.cards.protocol.Message;


/**
 * This class represent how abstract the images and the
 * location of the card, that is, one entry represent
 * a image and a location.
 *
 * The class knows write and read in a file or a socket.
 */
public class Entry {

    private static final String TAG = Entry.class.getSimpleName();

    private String pathImage;
    private Location location;

    public Entry (String pathImg, Location loc) {
        pathImage = pathImg;
        location = loc;
    }

    public Entry (String pathImg, android.location.Location loc) {
        pathImage = pathImg;
        location = new Location(loc);
    }

    public Entry(String pathImg) {
        pathImage = pathImg;
        location = new Location(0 , 0 , 0);
    }

    public void write(OutputStream tx) throws IOException {
        Message.writeString(tx, getNameImg());
        Message.writeImage(tx, new File(pathImage));
        location.write(tx);
    }

    public void read(InputStream rx) throws IOException {
        pathImage += Message.readString(rx);
        Message.readImage(rx, new File(pathImage));
        location.read(rx);
    }

    public String getPathImage() {
        return pathImage;
    }

    public Location getLocation () {
        return location;
    }

    public String formatLocation() {
        return String.format("%f,%f", location.getLatitude(), location.getLongitude());
    }

    @Override
    public String toString() {
        return String.format("Image : %s\n Location : %s\n", pathImage, location.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Entry)) {
            return false;
        }
        Entry e = (Entry) o;
        return location.equals(e.location) && e.pathImage.equals(pathImage);
    }

    public boolean isInvalid() {
        return pathImage.equals("") || location.isInvalid();
    }

    private String getNameImg() {
        String[] routes = pathImage.split("/");

        return routes[routes.length - 1];
    }

}
