package es.urjc.mov.javsan.cards.fichas;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.Entry;
import es.urjc.mov.javsan.cards.structures.ReplyError;
import es.urjc.mov.javsan.cards.structures.SpinnerFunction;
import es.urjc.mov.javsan.cards.structures.ToastMsg;

/**
 * Class CardCreate launch the activity where the user
 * will create a new mountain card, the activity implements
 * several fragments, the fragment to have the actual locaion
 * of the devices and two fragments to get the images from the
 * gallery and the locations of the card from a google map.
 *
 * The form and its design is defined in its layout, and the
 * fragment used.
 *
 * Note : To recover state we use strings and arrayStrings to
 * the data, that is, when activity restart the entries locations
 * and images path are saved in string arrays to locations is
 * necessary a conversion from string to location we send
 * the location like %f,%f and split the string by commas to
 * recover the locations.
 *
 * When create an entry the image is saved with state because
 * until we haven't got the location the entry is not saved.
 *
 * When step location is finished we save the entry and restart
 * the state of path image to a senseless value.
 */
public class CardCreate extends AppCompatActivity implements
        EntryImage.OnImagesListener,
        EntryLocation.OnLocationsListener,
        ActualLocation.OnLocationListener {

    private final String TAG = CardCreate.class.getSimpleName();

    private final int MAXENTRIES = 4;
    private final String MAXENTRIESINFO = "Max Card entries images...";

    private final String KEYRESTORELOC = "fromLocation";
    private final String KEYRESTOREIMG = "fromImage";
    private final String KEYIMG = "images";
    private final String KEYLOC = "locations";
    private final String KEYNAME = "name";
    private final String KEYDESC = "description";
    private final String KEYCATE = "category";
    private final String KEYSKIL = "skill";

    private final String[] Skills = { "Easy" , "Normal", "Hard", "None" };

    private String name;
    private String description;
    private String category;
    private String skill;

    private ArrayList<Entry> entries;

    private boolean fromImages;
    private boolean fromLocations;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_ticket);

        entries = new ArrayList<>();
        imagePath = "";

        recoverState(savedInstanceState);
        setUpUICreateCard();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setCardValuesFromUI();

        outState.putString(KEYNAME, name);
        outState.putString(KEYDESC, description);
        outState.putString(KEYSKIL, skill);
        outState.putString(KEYCATE, category);

        outState.putStringArrayList(KEYIMG, getEntryImages(entries));
        outState.putStringArrayList(KEYLOC, getEntryLocations(entries));

        outState.putBoolean(KEYRESTORELOC, fromLocations);
        outState.putBoolean(KEYRESTOREIMG, fromImages);
    }

    @Override
    public void handleNewImage(String image) {
        imagePath = image;

        hideFragmentUI(R.id.ticket_images_fragment);
        fromImages = false;

        showFragmentUI(R.id.ticket_locations_fragment);
        fromLocations = true;
    }

    @Override
    public void handleNewLocation(Location location) {
        addNewEntry(imagePath, location);

        hideFragmentUI(R.id.ticket_locations_fragment);
        fromLocations = false;

        showFragmentUI(R.id.form_ticket);
        showResultEntries();
        imagePath = "";
    }

    @Override
    public void handleNewActualLocation(Location location) {
        LatLng actualLocation = new LatLng(location.getLatitude(), location.getLongitude());
        EntryLocation f = (EntryLocation) getSupportFragmentManager().findFragmentById(R.id.id_fragment_locations);

        // Send actual location to fragment locations, prepare the maps
        // when the user go to mark the locations of the mountain activity.
        f.deviceActualLocation(actualLocation);
    }

    private ArrayList<String> getEntryImages (ArrayList<Entry> entries) {
        ArrayList<String> images = new ArrayList<>();

        for (Entry e : entries) {
            images.add(e.getPathImage());
        }
        return images;
    }

    private ArrayList<String> getEntryLocations(ArrayList<Entry> entries) {
        ArrayList<String> locations = new ArrayList<>();

        for (Entry e : entries) {
            locations.add(e.formatLocation());
        }
        return locations;
    }

    private void addNewEntry(String image, Location location) {
        Entry e = new Entry(image, location);

        if (e.isInvalid()) {
            new ToastMsg(this).show("Invalid entry");
            return;
        }
        if (entries.size() < MAXENTRIES) {
            entries.add(e);
        } else {
            new ToastMsg(this).show(MAXENTRIESINFO);
        }
    }

    private void showResultEntries() {
        TextView t = (TextView) findViewById(R.id.entry_result);
        t.setText(getEntriesResult(entries));
    }

    private String getEntriesResult(ArrayList<Entry> entries) {
        String result = "";

        for (Entry e : entries) {
            result += String.format("%s -> %s\n",e.getPathImage(), e.getLocation());
        }
        return result;
    }

    private void recoverState(Bundle savedInstance) {
        fromImages = fromLocations = false;
        if (savedInstance != null) {
            if (isEntriesRecover(savedInstance)) {
                setEntriesRecover(savedInstance);
            }
            fromLocations = savedInstance.getBoolean(KEYRESTORELOC);
            fromImages = savedInstance.getBoolean(KEYRESTORELOC);
        }
    }

    private void setEntriesRecover(Bundle savedInstance) {
        ArrayList<String> images = savedInstance.getStringArrayList(KEYIMG);
        ArrayList<Location> locations = getLocations(savedInstance.getStringArrayList(KEYLOC));

        for (int i = 0 ; i < images.size() && i < locations.size(); i++) {
            entries.add(new Entry(images.get(i), locations.get(i)));
        }
        showResultEntries();
    }

    private boolean isEntriesRecover(Bundle savedState) {
        return savedState.getStringArrayList(KEYIMG) != null &&
                savedState.getStringArrayList(KEYLOC) != null;
    }

    private ArrayList<Location> getLocations (ArrayList<String> data) {
        ArrayList<Location> locations = new ArrayList<>();

        for (String d : data) {
            String[] format = d.split(",");
            if (format.length != 2) {
                continue;
            }
            Location aux = new Location("");
            aux.setLatitude(Float.valueOf(format[0]));
            aux.setLongitude(Float.valueOf(format[1]));
            locations.add(aux);
        }
        return locations;
    }

    private void setUpUICreateCard() {
        Button b = (Button) findViewById(R.id.cancel);
        b.setOnClickListener(new Cancel());

        b = (Button) findViewById(R.id.create_card);
        b.setOnClickListener(new ButtonCreate());

        b = (Button) findViewById(R.id.entry_card);
        b.setOnClickListener(new ButtonEntryTicket());

        SpinnerFunction sF = new SpinnerFunction(this);
        Spinner spinner = sF.createSpinner(R.id.card_category, R.array.category);
        spinner.setOnItemSelectedListener(new SpinnerListener());

        if (fromImages) {
            hideFragmentUI(R.id.form_ticket);
            showFragmentUI(R.id.ticket_images_fragment);
        }

        if (fromLocations) {
            hideFragmentUI(R.id.form_ticket);
            showFragmentUI(R.id.ticket_locations_fragment);
        }
    }

    private class SpinnerListener extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String str = parent.getItemAtPosition(pos).toString();
            category = str;
        }
        public void onNothingSelected(AdapterView<?> parent) {
            return;
        }
    }

    private class ButtonEntryTicket implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            hideFragmentUI(R.id.form_ticket);
            showFragmentUI(R.id.ticket_images_fragment);
            showFragmentUI(R.id.ticket_images_fragment);
            fromImages = true;
        }
    }

    private void showFragmentUI(int id) {
        LinearLayout fragmentUI = (LinearLayout) findViewById(id);

        fragmentUI.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
    }

    private void hideFragmentUI(int id) {
        LinearLayout fragmentUI = (LinearLayout) findViewById(id);

        fragmentUI.setLayoutParams(new LinearLayout.LayoutParams(0 , 0));
    }

    private class Cancel implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent activityIntent = new Intent(CardCreate.this, CardSearch.class);
            Bundle newActivityInfo = new Bundle();

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtras(newActivityInfo);
            startActivity(activityIntent);
            finish();
        }

    }

    private class ButtonCreate implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new RunCreateCard().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     *
     * This method is called when the card is prepare to become created.
     * once checked the card values, first we send the request to server
     * to see if the ticket is created, once the ticket is checked  in the
     * server and the database of the mobile phone if the card not exist
     * is created in the server and when the server reply card created
     * the card is inserted in the database.
     *
     */
    private class RunCreateCard extends AsyncTask <Void, Void, String> {

        private final String IP = "10.0.2.2";
        private final int PORT = 2000;

        private final String INVALIDCARD = "Invalid Card try to fill out the form again";
        private final String EXISTINMOB = "The card already exist on the mobile.";
        private final String CREATED = "Card created";
        private final String EXISTINSERV = "The card already exist in the server";

        private Card card;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setCardValuesFromUI();
        }

        @Override
        protected String doInBackground(Void... params) {
            CardsDataBase db = new CardsDataBase(getApplicationContext());
            card = new Card(db.getCardId(), name, description, skill, category, entries);

            if (card.isInvalid()) {
                return INVALIDCARD;
            }
            if (db.isExistCard(card)) {
                return EXISTINMOB;
            }
            String result;
            try {
                commCreateCardServer();
                db.insertCard(card);
                result = CREATED;
            } catch (IOException e) {
                result = e.toString();
            } catch (ReplyError e) {
                result = EXISTINSERV;
            } finally {
                db.close();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            new ToastMsg(CardCreate.this).show(result);
            if (result.equals(CREATED)) {
                launchSearch();
            }
        }

        private void launchSearch() {
            Intent activityIntent = new Intent(CardCreate.this, CardSearch.class);
            Bundle newActivityInfo = new Bundle();

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtras(newActivityInfo);
            startActivity(activityIntent);
        }

        private void commCreateCardServer() throws ReplyError, IOException {
            CardClient cardClient = null;

            try {

                cardClient = new CardClient(IP, PORT);
                cardClient.createCard(card);

            } catch (IOException e) {

                throw  new IOException("Error in IO" + e.toString());

            } catch (ReplyError e) {

                throw new ReplyError("Card already exists on the server");

            } finally {
                if (cardClient != null) {
                    cardClient.close();
                }
            }
        }
    }

    private void setCardValuesFromUI() {
        EditText t = (EditText) findViewById(R.id.card_name);
        name = t.getText().toString();

        t = (EditText) findViewById(R.id.card_description);
        description = t.getText().toString();

        RadioGroup s = (RadioGroup) findViewById(R.id.skill);
        int id = s.getCheckedRadioButtonId();
        skill = getSkill(id);
    }

    private String getSkill(int id) {
        switch (id)  {
            case R.id.create_easy :
                return Skills[0];
            case R.id.create_normal :
                return Skills[1];
            case R.id.create_hard :
                return Skills[2];
            default:
                return Skills[3];
        }
    }
}
