package es.urjc.mov.javsan.cards.fichas;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This class is used like a fragment in activity create card,
 * this class be responsible for add a new location selected by the
 * user on the form create card.
 */
public class EntryLocation extends Fragment implements
        OnMapReadyCallback {

    private final String TAG = EntryLocation.class.getSimpleName();

    private GoogleMap mMap;
    private Location point;

    private View fragmentLayout;
    private OnLocationsListener onLocationsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentLayout = inflater.inflate(R.layout.ticket_locations, container, false);
        point = new Location("");
        point.setLatitude(0);
        point.setLongitude(0);

        Button b = (Button) fragmentLayout.findViewById(R.id.confirm_location);
        b.setOnClickListener(new ShowLocation());

        b = (Button) fragmentLayout.findViewById(R.id.confirm_locations);
        b.setOnClickListener(new ConfirmLocation());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        return fragmentLayout;
    }

    /**
     * This method is used to compatibility with android OS with API < 23...
     *
     * @param activity Param necessary to attach de fragment to an activity
     *                 in android API < 23...
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setOnLocationListener(activity);
        }
    }

    /**
     * Necessary to force activity implement the interface when use the
     * class like a fragment...
     *
     * @param context The context of the activity to check if implements
     *                the interface.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setOnLocationListener(context);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng p) {
                TextView t = (TextView) fragmentLayout.findViewById(R.id.location_selected);
                t.setText(String.format("%f , %f", p.latitude, p.longitude));

                point = getPointLocation(p);
            }
        });
    }

    public void deviceActualLocation(LatLng actualLocation) {
        float zoomLevel = 18.0f;

        mMap.moveCamera(CameraUpdateFactory.newLatLng(actualLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actualLocation, zoomLevel));
    }

    public interface OnLocationsListener {
        void handleNewLocation(Location location);
    }

    /**
     * When the user click on the map this handler show
     * the location marked by the user on the map inside the UI.
     * confirmed.
     */
    private class ShowLocation implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mMap == null || point == null || isInvalid(point)) {
                return;
            }
            mMap.clear();

            LatLng p = new LatLng(point.getLatitude(), point.getLongitude());
            mMap.addMarker(new MarkerOptions().position(p).title("Location"));

            TextView t = (TextView) fragmentLayout.findViewById(R.id.location_list);
            t.setText(locationToString(point));
        }

        private boolean isInvalid(Location point) {
            return point.getLongitude() == 0 && point.getLatitude() == 0;
        }
    }

    /**
     * Button called from the user when the new location is confirmed then the event
     * is received by the activity whose implements the fragment and the data are
     * updated...
     */
    private class ConfirmLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            resetLocation();
            onLocationsListener.handleNewLocation(point);
            point.setLatitude(0);
            point.setLongitude(0    );
        }
    }

    private Location getPointLocation(LatLng location) {
        Location l = new Location("");

        l.setLatitude(location.latitude);
        l.setLongitude(location.longitude);
        return l;
    }

    private void resetLocation(){
        TextView t = (TextView) fragmentLayout.findViewById(R.id.location_list);
        t.setText("");

        t = (TextView) fragmentLayout.findViewById(R.id.location_selected);
        t.setText("");
        mMap.clear();
    }

    private String locationToString(Location l) {
        return String.format("%f, %f\n", l.getLatitude(), l.getLongitude());
    }

    private void setOnLocationListener (Context context) {
        try {
            onLocationsListener = (EntryLocation.OnLocationsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLocationListener");
        }
    }
}
