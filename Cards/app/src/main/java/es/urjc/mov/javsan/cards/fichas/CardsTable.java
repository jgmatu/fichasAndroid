package es.urjc.mov.javsan.cards.fichas;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import es.urjc.mov.javsan.cards.structures.CardMeta;


/**
 * This class Create a table with cards results sending to it...
 * That is, the class create the rows with the different cards
 * whose were passed.
 *
 * Also we send the bundle to activity CardShow to select the
 * card to show from the activity CardShow, see the button
 * implementation code.
 */
public class CardsTable {

    public static final String NAMECARD = "nameCard";
    public static final String IDCARD= "idCard";

    private final String TAG = CardsTable.class.getSimpleName();

    private final int NEXTICON = 1;
    private final int ERRMIPMAP = -33;

    private AppCompatActivity appCompatActivity;

    CardsTable(AppCompatActivity a) {
        appCompatActivity = a;
    }

    public void createUITableCards(ArrayList<CardMeta> cards){
        TableLayout tab = (TableLayout) appCompatActivity.findViewById(R.id.table_cards);

        TableLayout.LayoutParams lt = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 0,
                (1.0f / (float) cards.size())) ;

        int id = 0;
        for (CardMeta c : cards) {
            TableRow row = new TableRow(appCompatActivity);

            row.setLayoutParams(lt);
            row.setGravity(Gravity.CENTER);
            setMetaCard(row, c, id);
            tab.addView(row);
            id++;
        }
    }

    public int getSkillIcon(String skill) {
        int mipmap = R.mipmap.skill_easy;
        String[] skills = appCompatActivity.getResources().getStringArray(R.array.skill);

        for (String s : skills) {
            if (s.equals(skill)) {
                return mipmap;
            }
            mipmap += NEXTICON;
        }
        return ERRMIPMAP;
    }

    public int getCategoryIcon(String category) {
        int mipmap = R.mipmap.category_bike;
        String[] categories = appCompatActivity.getResources().getStringArray(R.array.category);

        for (String c : categories) {
            if (c.equals(category)) {
                return mipmap;
            }
            mipmap += NEXTICON;
        }
        return ERRMIPMAP;
    }

    private void setMetaCard(TableRow row, CardMeta cardMeta, int id) {
        String IdName = String.format("%d:%s", cardMeta.getId(), cardMeta.getName());

        setButton(row, IdName, id);
        setCategory(row, cardMeta.getCategory());
        setSkill(row, cardMeta.getSkill());
    }

    private void setButton(TableRow row, String name, int id) {
        TableRow.LayoutParams ltr = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.MATCH_PARENT, 0.40f);

        Button b = new Button(appCompatActivity);

        b.setOnClickListener(new ShowCard(id));
        b.setText(name);
        b.setId(id);
        b.setLayoutParams(ltr);

        row.addView(b);

    }

    private void setCategory(TableRow row, String category) {
        TableRow.LayoutParams ltr = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.MATCH_PARENT, 0.30f);

        TextView t = new TextView(appCompatActivity);
        t.setText(category);

        ImageView img = new ImageView(appCompatActivity);
        img.setImageResource(getCategoryIcon(category));
        img.setLayoutParams(ltr);
        row.addView(t);
        row.addView(img);
    }

    private void setSkill (TableRow row, String skill) {
        TableRow.LayoutParams ltr = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.MATCH_PARENT, 0.30f);

        TextView t = new TextView(appCompatActivity);
        t.setText(skill);

        ImageView img = new ImageView(appCompatActivity);
        img.setImageResource(getSkillIcon(skill));
        img.setLayoutParams(ltr);
        row.addView(t);
        row.addView(img);
    }

    /**
     * This button is replicated for each rows whose contain a card metadata,
     * when you click on this button you show the ticket on the Activity show
     * ticket whose were selected.
     */
    private class ShowCard implements View.OnClickListener {
        private int idRatioCard;

        ShowCard(int i) {
            idRatioCard = i;
        }

        @Override
        public void onClick(View v) {
            Button b = (Button) appCompatActivity.findViewById(idRatioCard);
            String IdName = b.getText().toString();

            String[] fields = IdName.split(":");
            goShowCard(fields[0], fields[1]);
        }

        private void goShowCard(String idCard, String nameCard) {
            Intent activityIntent = new Intent(appCompatActivity, CardShow.class);
            Bundle newActivityInfo = new Bundle();

            newActivityInfo.putInt(IDCARD, Integer.parseInt(idCard));
            newActivityInfo.putString(NAMECARD, nameCard);

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtras(newActivityInfo);

            appCompatActivity.startActivity(activityIntent);
            appCompatActivity.finish();
        }
    }
}
