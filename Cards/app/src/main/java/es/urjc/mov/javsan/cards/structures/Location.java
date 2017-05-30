package es.urjc.mov.javsan.cards.structures;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.urjc.mov.javsan.cards.protocol.Message;


/**
 * Location : this class represent the location of
 * geolocation feature, neccesary to localize the mobile
 * and the cards in a map.
 *
 * The ratio is used to different the locations of the
 * cards.
 */
public class Location {
    double latitude;
    double longitude;
    int ratio; // m.

    public Location (android.location.Location loc) {
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        ratio = 1000;
    }

    public Location (double ltd, double lng, int r) {
        latitude = ltd;
        longitude = lng;
        ratio = r;
    }

    public Location () {
        latitude = 0;
        longitude = 0;
        ratio = 0;
    }

    public int getRatio() {return ratio;}

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void read(InputStream rx) throws IOException {
        latitude = Message.readDouble(rx);
        longitude = Message.readDouble(rx);
        ratio = Message.readInt(rx);
    }

    public void write(OutputStream tx) throws IOException {
        Message.writeDouble(tx, latitude);
        Message.writeDouble(tx, longitude);
        Message.writeInt(tx, ratio);
    }

    public boolean isInvalid() {
        return longitude == 0 && latitude == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        Location l = (Location) o;
        boolean equal = Math.abs(latitude - l.latitude) < 0.1;

        equal &= Math.abs(longitude - l.longitude) < 0.1;
        equal &= ratio == l.ratio;
        return equal;
    }

    @Override
    public String toString() {
        return String.format("Ltd : %f\n Lng : %f\n Ratio : %d\n", latitude, longitude, ratio);
    }
}