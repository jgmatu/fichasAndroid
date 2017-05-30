package es.urjc.mov.javsan.cards.fichas;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.CardMeta;
import es.urjc.mov.javsan.cards.structures.Entry;
import es.urjc.mov.javsan.cards.structures.Location;
import es.urjc.mov.javsan.cards.structures.ToastMsg;

/**
 *
 * This class show a card in an Activity, that is, select the data from the database
 * with the name pass from the activity whose call CardTable, CardTable pass the bundle
 * with the name of the ticket to show in this activity.
 *
 */
public class CardShow extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = CardShow.class.getSimpleName();

    private final String NAMECARD = CardsTable.NAMECARD;
    private final String IDCARD = CardsTable.IDCARD;
    private final String KEYIMG = "numImage";

    private GoogleMap mMap;

    private int idCard;
    private String nameCard;

    private int numImage;
    private SeePhotos photosTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha);

        Button cardsRatio = (Button) findViewById(R.id.ratio_cards);
        cardsRatio.setOnClickListener(new ButtonCardsSearch());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // That is, get google map fragment to the activity and notified when map is ready.
        // in a callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        idCard = getIdCard();
        nameCard = getNameCard();
        numImage = getInitImage(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEYIMG, 0);
        if (photosTicket != null) {
            outState.putInt(KEYIMG, photosTicket.actualPhoto());
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     *
     * This is where we can add markers or lines, add listeners or move the camera.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     * When the map is ready we can show all the data cards inside the activity.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        new DataCard().execute();
    }

    private void addMarker(Card card) {
        ArrayList<Entry> entries = card.getEntries();
        Location loc = entries.get(0).getLocation();
        LatLng zMarker = new LatLng(loc.getLatitude(), loc.getLongitude());

        for (Entry e : entries) {
            setMarker(e.getLocation());
        }
        moveCamera(zMarker);
    }

    private void moveCamera(LatLng marker) {
        float zoomLevel = 16.5f;

        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, zoomLevel));
    }

    private void setMarker(Location loc) {
        LatLng marker = new LatLng(loc.getLatitude(), loc.getLongitude());

        mMap.addMarker(new MarkerOptions().position(marker).title("Marker in the route"));
    }

    private String getNameCard() {
        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {
            return "";
        }
        return bundle.getString(NAMECARD);
    }

    private int getIdCard() {
        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {
            return -1;
        }
        return bundle.getInt(IDCARD);
    }

    private int getInitImage(Bundle savedInstanceState) {
        int initImage = 0;

        if (savedInstanceState != null) {
            initImage = savedInstanceState.getInt(KEYIMG);
        }
        return initImage;
    }

    /**
     * This class implements the handler of button in show cards to
     * come back to search cards, from show cards activity.
     */
    private class ButtonCardsSearch implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent activityIntent = new Intent(CardShow.this, CardSearch.class);
            Bundle newActivityInfo = new Bundle();

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtras(newActivityInfo);
            startActivity(activityIntent);
            finish();
        }
    }

    /*
     * This AsyncTask query the all the data card on the database and show all
     * the info on the activity with the database results..
     */
    private class DataCard extends AsyncTask <Void, Void, Card> {

        private final String NOTFOUND = "The card is not founded on the database";

        /*
         * Get from the database all the data card.
         */
        @Override
        protected Card doInBackground(Void... params) {
            CardsDataBase db = new CardsDataBase(CardShow.this);
            Card card = null;

            try {
                card = db.getCard(idCard, nameCard);
            } catch (RuntimeException e) {
            }
            db.close();
            return card;
        }

        /**
         * Show on the Activity UI all the data from the card selected
         * on the data base.
         *
         * @param card Card selected on the database.
         */
        @Override
        protected void onPostExecute(Card card) {
            super.onPostExecute(card);
            if (card == null) {
                new ToastMsg(CardShow.this).show(NOTFOUND);
                return;
            }
            addMarker(card);

            photosTicket = new SeePhotos(CardShow.this, numImage , card.getImages());
            CardMeta cardMeta = card.getCardMeta();

            TextView t = (TextView) findViewById(R.id.card_name);
            t.setText(cardMeta.getName());

            t = (TextView) findViewById(R.id.card_description);
            t.setText(cardMeta.getDescription());

            setImagesIcon(card);
        }

        private void setImagesIcon(Card card) {
            CardsTable cardsTable = new CardsTable(CardShow.this);

            ImageView img = (ImageView) findViewById(R.id.card_skill);
            img.setImageResource(cardsTable
                    .getSkillIcon(card
                            .getCardMeta()
                            .getSkill()));

            img = (ImageView) findViewById(R.id.card_category);
            img.setImageResource(cardsTable
                    .getCategoryIcon(card
                            .getCardMeta()
                            .getCategory()));
        }
    }
}
