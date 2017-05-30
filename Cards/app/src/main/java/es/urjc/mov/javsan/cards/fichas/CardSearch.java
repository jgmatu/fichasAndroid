package es.urjc.mov.javsan.cards.fichas;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;

import java.io.IOException;
import java.util.ArrayList;

import es.urjc.mov.javsan.cards.structures.Card;
import es.urjc.mov.javsan.cards.structures.CardMeta;
import es.urjc.mov.javsan.cards.structures.Cards;
import es.urjc.mov.javsan.cards.structures.SpinnerFunction;
import es.urjc.mov.javsan.cards.structures.ToastMsg;


/**
 * Activity to search cards from your actual location this is the main activity
 * when the user begin to use the application this is the activity where begins...
 */
public class CardSearch extends AppCompatActivity implements ActualLocation.OnLocationListener {

    private final String TAG = CardSearch.class.getSimpleName();

    private final String KEYCATE = "category";
    private final String KEYSKIL = "skill";
    private final String KEYRESU = "fromResults";


    private String category;
    private String skill;

    private boolean fromResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_ticket);

        setUISearchTicket();
        category = skill = "";
        fromResults = false;
        restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEYRESU, fromResults);
        outState.putString(KEYCATE, category);
        outState.putString(KEYSKIL, skill);
    }

    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }
        fromResults = savedState.getBoolean(KEYRESU);
        skill = savedState.getString(KEYSKIL);
        category = savedState.getString(KEYCATE);

        if (fromResults) {
            new RunSearch().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void setUISearchTicket() {
        Button b = (Button) findViewById(R.id.search_cards);
        b.setOnClickListener(new ButtonSearch());

        b = (Button) findViewById(R.id.create_cards);
        b.setOnClickListener(new CreateCard());

        SpinnerFunction sF = new SpinnerFunction(this);

        Spinner spinner = sF.createSpinner(R.id.search_skill, R.array.skill);
        spinner.setOnItemSelectedListener(new SpinnerListener(R.id.search_skill));

        spinner = sF.createSpinner(R.id.search_category, R.array.category);
        spinner.setOnItemSelectedListener(new SpinnerListener(R.id.search_category));
    }

    @Override
    public void handleNewActualLocation(Location location) {
        new GetTickets(location).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * This spinner situated on the activity.
     */
    private class SpinnerListener extends Activity implements AdapterView.OnItemSelectedListener {
        private int idR;

        SpinnerListener (int i) {
            idR = i;
        }

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String str = parent.getItemAtPosition(pos).toString();

            if (idR == R.id.search_category) {
                category = str;
            } else if (idR == R.id.search_skill) {
                skill = str;
            } else {
                Log.e(TAG, "Spinner Listener bad item selected skill, category");
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            return;
        }
    }

    /**
     * This is the button on the down of activity with this button you going to
     * activity to create a new card...
     */
    private class CreateCard implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent activityIntent = new Intent(CardSearch.this, CardCreate.class);
            Bundle newActivityInfo = new Bundle();

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtras(newActivityInfo);
            startActivity(activityIntent);
        }
    }


    /**
     * This button run a search in the database to show the cards in the mobile
     * downloaded from the server...
     *
     * The results are showed in way of table.
     */
    private class ButtonSearch implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new RunSearch().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * The search use a database then you need create and async task to run
     * the search and not block the UI main thread.
     */
    private class RunSearch extends AsyncTask<String, String, ArrayList<CardMeta>> {

        private final String EMPTY = "No exists cards with that's features";
        private boolean empty;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            empty = false;
        }

        @Override
        protected ArrayList<CardMeta> doInBackground(String... params) {
            CardsDataBase db = new CardsDataBase(getApplicationContext());
            ArrayList<CardMeta> cards = null;

            try {
                if (!category.equals("None") && !skill.equals("None")) {
                    cards = db.getCardsMeta(category, skill);
                } else if (category.equals("None") && !skill.equals("None")) {
                    cards = db.getCardsMetaByFeature("skill", skill);
                } else if (!category.equals("None") && skill.equals("None")) {
                    cards = db.getCardsMetaByFeature("category", category);
                } else if (category.equals("None") && skill.equals("None")){
                    empty = true;
                }
            } catch (RuntimeException e) {
                empty = true;
            }
            db.close();
            return cards;
        }

        @Override
        protected void onPostExecute(ArrayList<CardMeta> cards) {
            super.onPostExecute(cards);
            if (empty) {
                emptyCards();
            } else {
                showCards(cards);
            }
        }

        private void emptyCards() {
            new ToastMsg(CardSearch.this).show(EMPTY);
        }

        /**
         * Launch the table with the cards founded on the database whose have the
         * features, category and skill marked by the user.
         *
         * The table is created by the class CardsTable... and from this class we
         * jump to the activity show cards when clicked on one card.
         *
         * @param cards Cards founded on the search.
         */
        private void showCards(ArrayList<CardMeta> cards) {
            hideFragmentUI(R.id.search_container);
            showResultSearchUI();
            fromResults = true;

            CardsTable cardsTable = new CardsTable(CardSearch.this);
            cardsTable.createUITableCards(cards);
        }

    }

    /**
     * This button is used to hide the table result once showed and show again
     * the form to search mountain cards.
     */
    private class ShowSearchForm implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            showFragmentUI(R.id.search_container);
            hideResultSearchUI();
            fromResults = false;
        }

    }

    /************************************************************************
     * This methods are used to play with the UI in the CardSearch activity,
     * show and hide the search form and the table of results.
     *
     ************************************************************************/

    private void showResultSearchUI() {
        TableLayout tabTickets = (TableLayout) findViewById(R.id.table_cards);
        Button button = (Button) findViewById(R.id.search_button);

        tabTickets.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0 , 0.8f));

        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.2f));
        button.setOnClickListener(new ShowSearchForm());
    }

    private void hideResultSearchUI() {
        TableLayout tabTickets = (TableLayout) findViewById(R.id.table_cards);
        Button button = (Button) findViewById(R.id.search_button);

        tabTickets.setLayoutParams(new LinearLayout.LayoutParams(0, 0 , 0f));
        tabTickets.removeAllViews();

        button.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0f));
    }

    private void showFragmentUI(int id) {
        LinearLayout containerSearch = (LinearLayout) findViewById(id);

        containerSearch.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
    }

    private void hideFragmentUI(int id) {
        LinearLayout containerSearch = (LinearLayout) findViewById(id);
        containerSearch.setLayoutParams(new LinearLayout.LayoutParams(0 , 0 , 0f));
    }

    /* End of methods to show and hide table results and form search */


    /**
     * This AsyncTask request the cards from the actual location of the
     * device to the repository server if the server reply cards this
     * cards are inserted in the database inside the mobile devices.
     */
    private class GetTickets extends AsyncTask<Void, Void, String> {

        private final String IP = "10.0.2.2";
        private final int PORT = 2000;

        private es.urjc.mov.javsan.cards.structures.Location location;

        GetTickets (Location loc) {
            location = new es.urjc.mov.javsan.cards.structures.Location(loc);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;

            try {
                CardClient cardClient = new CardClient(IP, PORT);
                Cards cards = cardClient.ratioCards(location);

                cardClient.close();
                insertCards(cards);

                result = "Cards found in your ratio";
            } catch (IOException e) {

                result = e.toString();

            } catch (Exception e) {

                result = "No cards found in your ratio";

            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            new ToastMsg(CardSearch.this).show(result);
        }

        private void insertCards (Cards cards) {
            CardsDataBase db = new CardsDataBase(getApplicationContext());
            for (Card c : cards.getCards()) {
                if (!db.isExistCard(c)) {
                    db.insertCard(c);
                }
            }
            db.close();
        }
    }
}
