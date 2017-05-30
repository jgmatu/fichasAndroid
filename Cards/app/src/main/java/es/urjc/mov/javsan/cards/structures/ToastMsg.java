package es.urjc.mov.javsan.cards.structures;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * This class is a simple tool to write
 * messages of info to the user.
 *
 * Is also to debug the application through
 * the activity.
 */
public class ToastMsg {

    Activity appCompatActivity;

    public ToastMsg(AppCompatActivity a) {
        appCompatActivity = a;
    }

    public void show (String txt) {
        int time = Toast.LENGTH_SHORT;

        Toast msg = Toast.makeText(appCompatActivity, txt, time);
        msg.show();
    }
}
