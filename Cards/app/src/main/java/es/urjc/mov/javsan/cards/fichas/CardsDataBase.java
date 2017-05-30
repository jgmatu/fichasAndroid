package es.urjc.mov.javsan.cards.fichas;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.CardMeta;
import es.urjc.mov.javsan.cards.structures.Entry;
import es.urjc.mov.javsan.cards.structures.Location;


public class CardsDataBase extends SQLiteOpenHelper {

    private final static String NAME = "cards.db";
    private final static int VERSION = 1;
    private static final String TAG = CardsDataBase.class.getName();

    private final String CARDS = "Cards";
    private final String LOCATIONS = "Locations";
    private final String IMAGES = "Images";
    private final String POSITIONS = "Positions";
    private final String ROUTES = "Routes";

    private final int KILOMETER = 1000; // meters.
    private final int MINRATIO = 100; // meters.

    /**
     * Create the schema of the database on the mobile phone
     * tables CARDS, LOCATION, IMAGES, POSITIONS AND ROUTES
     *
     * Advice read carefully to see the relationships data between
     * the tables.
     */
    private final String CREATECARDS = "CREATE TABLE " + CARDS + " (" +
            " Id INTEGER AUTO INCREMENT,"+
            " Name TEXT NOT NULL,"+
            " Description TEXT,"+
            " Category TEXT,"+
            " Skill TEXT,"+
            " PRIMARY KEY (ID, Name)"+
    ");";

    private final String CREATELOCATIONS = "CREATE TABLE " + LOCATIONS + "(" +
            " Latitude REAL NOT NULL," +
            " Longitude REAL NOT NULL," +
            " Ratio INTEGER," + // Meters...
            " PRIMARY KEY (Latitude, Longitude)" +
     ");";

    private final String CREATEIMAGES = "CREATE TABLE " + IMAGES + " (Path TEXT PRIMARY KEY);";

    private final String CREATEPOSITIONS = "CREATE TABLE " + POSITIONS + " (" +
            " Path TEXT NOT NULL," +
            " Latitude REAL NOT NULL," +
            " Longitude REAL NOT NULL," +
            " Ratio INT NOT NULL," +
            " PRIMARY KEY (Path, Latitude, Longitude)," +

            " FOREIGN KEY (Path) REFERENCES " + IMAGES + " (Path)" +
            "    ON DELETE CASCADE ON UPDATE NO ACTION," +

            " FOREIGN KEY (Latitude, Longitude) REFERENCES " + LOCATIONS +
            "             (Latitude, Longitude)" +
            "    ON DELETE CASCADE ON UPDATE NO ACTION" +
     ");";

    private final String CREATEROUTES = "CREATE TABLE " + ROUTES + " ( " +
            " Id INTEGER NOT NULL," +
            " Name TEXT NOT NULL," +
            " Latitude REAL NOT NULL," +
            " Longitude REAL NOT NULL," +
            " Ratio INT NOT NULL," +
            " Path TEXT NOT NULL," +

            " PRIMARY KEY (Id, Name, Latitude, Longitude, Path)," +

            " FOREIGN KEY (Id, Name) REFERENCES " + CARDS + " (Id, Name)" +
            "    ON DELETE CASCADE ON UPDATE NO ACTION," +

            " FOREIGN KEY (Path, Latitude, Longitude) REFERENCES " + POSITIONS +
                        " (Path, Latitude, Longitude)" +

            "    ON DELETE CASCADE ON UPDATE NO ACTION" +
     ");";

    /**
     * Create the data base if not exists
     * @param context Activity where create the data base.
     */
    public CardsDataBase(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");

        db.execSQL(CREATECARDS);
        db.execSQL(CREATEIMAGES);
        db.execSQL(CREATELOCATIONS);
        db.execSQL(CREATEPOSITIONS);
        db.execSQL(CREATEROUTES);
    }

    /**
     * onUpgrade is used to debug the data base this upgrade reset the tables
     * of the database only.
     *
     * @param db The database in writable mode...
     * @param oldVersion almost 0 to debug.
     * @param newVersion almost 1 to debug.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CARDS);
        db.execSQL("DROP TABLE IF EXISTS " + IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + POSITIONS);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTES);
        onCreate(db);
    }

    public Card getCard(int id, String name) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT Cards.id, description, skill, category, " +
                "latitude, longitude, ratio, Cards.Name, path FROM "+CARDS+","+ROUTES+" "+
                "WHERE Cards.Id == Routes.Id AND Cards.Name == Routes.Name AND " +
                "Cards.Id == ? AND Cards.Name == ?";

        String selection[] = {String.valueOf(id), name};
        Cursor cursor = db.rawQuery(query, selection);
        if (cursor.getCount() < 1) {
            cursor.close();
            throw new RuntimeException("Card does not exist");
        }
        Card card = getAllDataCard(cursor);

        cursor.close();
        db.close();
        return card;
    }

    public ArrayList<CardMeta> getCardsMeta(String category, String skill) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + CARDS + " WHERE category ==  ? AND skill == ?;";
        String selection[] = {category, skill};

        Cursor cursor = db.rawQuery(query, selection);
        if (cursor.getCount() < 1) {
            cursor.close();
            db.close();
            throw new RuntimeException("Card does not exist with this category and skill...");
        }
        ArrayList<CardMeta> metaCards = new ArrayList<>();

        while (cursor.moveToNext()) {
            metaCards.add(getCardMetadata(cursor));
        }
        cursor.close();
        db.close();
        return metaCards;
    }

    public ArrayList<CardMeta> getCardsMetaByFeature(String key, String value) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + CARDS +
                " WHERE " + key + " = ?";
        String[] selection = {value};
        Cursor cursor = db.rawQuery(query, selection);

        if (cursor.getCount() < 1) {
            cursor.close();
            db.close();
            throw new RuntimeException("There is not cards in db with this category...");
        }
        ArrayList<CardMeta> cards = new ArrayList<>();

        while (cursor.moveToNext()) {
            cards.add(getCardMetadata(cursor));
        }
        cursor.close();
        db.close();
        return cards;
    }

    public boolean isExistCard(Card card) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT Id, Name FROM " + CARDS +
                " WHERE Id = ? AND Name = ?";
        String[] selection = {String.format("%d", card.getId()), card.getName()};
        Cursor cursor = db.rawQuery(query, selection);

        boolean exist = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exist;
    }

    public int getCardId() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT max(Id) FROM CARDS";
        String[] selection = {};
        Cursor cursor = db.rawQuery(query, selection);

        if (!cursor.moveToFirst()) {
            return 0;
        }
        int id = cursor.getInt(0);
        cursor.close();
        db.close();
        return id + 1;
    }

    public boolean insertCard(Card card) {
        SQLiteDatabase db = getWritableDatabase();
        CardMeta cardMeta = card.getCardMeta();

        insertMetadataCard(db, cardMeta);
        insertLocations(db, card);
        insertImages(db, card);
        insertPositions(db, card);
        insertRoutes(db, card);

        db.close();
        return true;
    }

    private boolean insertMetadataCard(SQLiteDatabase db, CardMeta cardMeta) {
        db.execSQL(" INSERT INTO " + CARDS + " (Id, Name, Description, Category, Skill) VALUES " +
                        String.format("(%d, '%s','%s', '%s', '%s');",
                        cardMeta.getId(), cardMeta.getName(), cardMeta.getDescription(),
                        cardMeta.getCategory(), cardMeta.getSkill()));
        return true;
    }

    private boolean insertLocations(SQLiteDatabase db, Card card) {
        ArrayList<Entry> entries = card.getEntries();

        for (Entry e : entries) {
            Location loc = e.getLocation();
            db.execSQL("INSERT INTO " + LOCATIONS + " (latitude, longitude, ratio) VALUES " +
                    String.format("(%.5f,%.5f,%d);", loc.getLatitude() , loc.getLongitude(), MINRATIO));
        }
        return true;
    }

    private boolean insertImages(SQLiteDatabase db, Card card) {
        ArrayList<String> images = card.getImages();

        for (String i : images) {
            try {
                db.execSQL("INSERT INTO " + IMAGES + " (Path) VALUES " + String.format("('%s');", i));
            } catch (SQLiteConstraintException e) {
                ;
            }
        }
        return true;
    }

    private boolean insertPositions(SQLiteDatabase db, Card card) {
        ArrayList<Entry> entries = card.getEntries();

        for (Entry e : entries) {
            Location loc = e.getLocation();

            db.execSQL("INSERT INTO " + POSITIONS + " (latitude, longitude, ratio, path) VALUES " +
                    String.format("(%.5f,%.5f,%d,'%s');", loc.getLatitude(), loc.getLongitude(), loc.getRatio(), e.getPathImage()));
        }
        return true;
    }

    private boolean insertRoutes (SQLiteDatabase db, Card card) {
        ArrayList<Entry> entries = card.getEntries();

        for (Entry e : entries) {
            Location loc = e.getLocation();

            db.execSQL("INSERT INTO " + ROUTES + " (Id, Name, Latitude, Longitude, Ratio , Path) VALUES " +
                                String.format("('%d', '%s', %.5f, %.5f, %d , '%s');",
                                card.getId(), card.getName(), loc.getLatitude(), loc.getLongitude(),
                                loc.getRatio(), e.getPathImage()));
        }
        return true;
    }

    private Card getAllDataCard(Cursor cursor) {
        ArrayList<Entry> entries = new ArrayList<>();

        while (cursor.moveToNext()) {
            entries.add(getEntry(cursor));
        }
        cursor.moveToFirst();
        return new Card(getCardMetadata(cursor) ,entries);
    }

    private CardMeta getCardMetadata(Cursor cursor) {
        int i = cursor.getInt(cursor.getColumnIndex("Id"));
        String n = cursor.getString(cursor.getColumnIndex("Name"));
        String d = cursor.getString(cursor.getColumnIndex("Description"));
        String s = cursor.getString(cursor.getColumnIndex("Skill"));
        String c = cursor.getString(cursor.getColumnIndex("Category"));

        return new CardMeta(i, n, d, s, c);
    }

    private Entry getEntry(Cursor cursor) {
        String i = cursor.getString(cursor.getColumnIndex("Path"));

        return new Entry(i, getLocation(cursor));
    }

    private Location getLocation(Cursor cursor) {
        float latitude = cursor.getFloat(cursor.getColumnIndex("Latitude"));
        float longitude = cursor.getFloat(cursor.getColumnIndex("Longitude"));
        int ratio = cursor.getInt(cursor.getColumnIndex("Ratio"));

        return new Location(latitude ,longitude , ratio);
    }
}
