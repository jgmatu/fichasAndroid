package es.urjc.mov.javsan.cards.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class implements the base of protocol to comm with
 * the repository server.
 */
public class Message {

    protected final static int REQCARDS = 0;
    protected final static int RQCREATECARD = 1;
    protected final static int RESPCARDS = 2;
    protected final static int RESPCREATECARD = 3;
    protected final static int RESPERR = 4;

    private final static int DECIMALS = 5;

    private final static int MAXBYTES = 65536;
    private static final String TAG = Message.class.getSimpleName();

    protected int type;

    /**
     * Message produce read a message from socket and create and instance of
     * kind of message that read from socket.
     *
     * @param rx Socket necessary to read the image.
     * @return A kind of message read from socket.
     * @throws IOException Error while read the message from socket.
     */
    public static Message produce(InputStream rx) throws IOException {
        Message msg = null;
        int type = readInt(rx);

        switch (type) {
            case REQCARDS:
                msg = new ReqCards();
                break;

            case RESPCARDS:
                msg = new RespCards();
                break;

            case RESPERR :
                msg = new RespError();
                break;

            case RQCREATECARD:
                msg = new ReqCreateCard();
                break;
            case RESPCREATECARD:
                msg = new RespCardCreated();
                break;
        }
        msg.read(rx);
        return msg;
    }

    public void read(InputStream rx) throws IOException {
        ;
    }

    public void write(OutputStream tx) throws IOException {
        writeInt(tx, type);
    }

    public static void writeInt(OutputStream tx, int value) throws IOException {
        DataOutputStream odata = new DataOutputStream(tx);

        odata.writeInt(value);
    }

    public static int readInt(InputStream rx) throws IOException {
        DataInputStream idata = new DataInputStream(rx);

        return idata.readInt();
    }

    public static void writeDouble(OutputStream tx, double numb) throws IOException {
        int n = (int) numb;
        int d = (int) ((numb - (float)n) * Math.pow(10, DECIMALS));

        writeInt(tx, n);
        writeInt(tx, d);
        writeInt(tx, DECIMALS);
    }

    public static double readDouble(InputStream rx) throws IOException {
        DataInputStream idata = new DataInputStream(rx);

        double numb = (double) idata.readInt();
        double dec = (double) idata.readInt();
        int numDec = idata.readInt();

        return numb + dec / Math.pow(10, numDec);
    }

    public static void writeString(OutputStream tx, String data) throws IOException {
        DataOutputStream odata = new DataOutputStream(tx);
        int length = data.getBytes("UTF-8").length;

        odata.writeInt(length);
        odata.write(data.getBytes("UTF-8"));
    }

    public static String readString(InputStream rx) throws IOException {
        DataInputStream idata = new DataInputStream(rx);
        int length = idata.readInt();
        byte[] data = new byte[length];

        idata.readFully(data, 0 , length);
        return new String(data , "UTF-8");
    }

    public static void readImage(InputStream rx, File file) throws IOException {
        FileOutputStream fos = null; // write image inside the disk once reading from socket..

        try {
            fos = new FileOutputStream(file);

            int size = readInt(rx);
            while (size > 0) {
                byte[] data = new byte[MAXBYTES];
                if (size < MAXBYTES) {
                    data = new byte[size];
                }

                int nr = rx.read(data, 0, data.length);
                if (nr <= 0) {
                    break;
                }
                fos.write(data, 0, nr);
                size -= nr;
            }

        } catch (IOException e) {

            throw new IOException(e);

        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static void writeImage(OutputStream tx, File file) throws IOException {
        FileInputStream fis = null; // Read image from disk to write in the socket...

        try {
            fis = new FileInputStream(file);
            int size = (int) file.length();

            writeInt(tx, size);
            while (size > 0) {
                byte[] data = new byte[MAXBYTES];
                if (size < MAXBYTES) {
                    data = new byte[size];
                }

                int nr = fis.read(data, 0, data.length);
                if (nr <= 0) {
                    break;
                }
                tx.write(data, 0 , nr);
                size -= nr;
            }

        } catch (IOException e) {

            throw new IOException(e);

        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
}

