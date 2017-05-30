package es.urjc.mov.javsan.cards;


import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import es.urjc.mov.javsan.cards.fichas.CardsDataBase;
import es.urjc.mov.javsan.cards.fichas.R;
import es.urjc.mov.javsan.cards.structures.Entry;
import es.urjc.mov.javsan.cards.structures.Location;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class TestTools {

    static class CATEGORIES {
        public static final int BIKE = 0;
        public static final int CANOE = 1;
        public static final int DROPRAVINE = 2;
        public static final int HIKING = 3;
        public static final int CLIMB = 4;
        public static final int NONE = 5;
    }

    static class SKILLS {
        public static final int EASY = 0;
        public static final int NORMAL = 1;
        public static final int HARD = 2;
        public static final int NONE = 3;
    }

    private AppCompatActivity activity;
    private boolean landscape;

    TestTools(AppCompatActivity a) {
        activity = a;
    }

    public void createTestImage(File f) throws IOException {
        Bitmap b = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.category_bike);
        FileOutputStream fos = new FileOutputStream(f);

        b.compress(Bitmap.CompressFormat.JPEG, 100 , fos);
    }

    public void selectSpinner(int id, int idx, String item) {
        onView(withId(id)).perform(click());

        // Click on the first item from the list, which is a marker string: "Select category"
        onData(allOf(is(instanceOf(String.class)))).atPosition(idx).perform(click());
        onView(withId(id)).check(matches(withSpinnerText(containsString(item))));
    }

    public void rotateScreen() {
        if (landscape) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        landscape = !landscape;
    }


    public void cleanDB() {
        CardsDataBase db = new CardsDataBase(activity.getApplicationContext());
        db.onUpgrade(db.getWritableDatabase(), 0 , 1);
        db.close();
    }

    public ArrayList<Entry> getEntries(String name) throws IOException {
        ArrayList<Entry> entries = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory() + File.separator + name;

        new File(path).mkdirs();

        File f1 = new File(path + "/testOne.jpg");
        File f2 = new File(path + "/testTwo.jpg");

        createTestImage(f1);
        createTestImage(f2);

        entries.add(new Entry(f1.getPath(), new Location(3.003f, -133.44, 100)));
        entries.add(new Entry(f2.getPath(), new Location(-12.0f, 22.032, 99)));

        return entries;
    }

}
