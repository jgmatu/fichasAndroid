package es.urjc.mov.javsan.cards.structures;


import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * This class is used to create spinners in the UI.
 *
 * Don't repeat yourself.
 */
public class SpinnerFunction {

    private AppCompatActivity appCompatActivity;

    public SpinnerFunction (AppCompatActivity a) {
        appCompatActivity = a;
    }

    public Spinner createSpinner (int id, int list) {
        Spinner spinner = (Spinner) appCompatActivity.findViewById(id);

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(appCompatActivity,
                list, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);

        return spinner;
    }

}
