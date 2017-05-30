package es.urjc.mov.javsan.cards.fichas;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * This class have the responsability of get the actual geolocation
 * of the devices using the google api...
 *
 * The method to get the geolocation are with the mobile sensor and
 * a request to google api to get the actual geolocation of the devices.
 *
 * The location are server from the class implements a callback or handler
 * whose will executed when a new location is received from google server.
 *
 * The activity whose need the location will insert like a fragment in its
 * layout the class and will must implement the callback in its java class
 * to work.
 */
public class ActualLocation extends Fragment implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = ActualLocation.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 111;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private View fragmentLayout;
    private OnLocationListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentLayout = inflater.inflate(R.layout.location_fragment, container, false);

        requestPermission();
        setOnLocationListener(getActivity());
        return fragmentLayout;
    }

    @Override
    public void onStart() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
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

    /**
     * This method is used to compatibility with android OS with API < 23...
     *
     * @param activity Param neccesary to attach de fragment to an activity
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

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            mListener.handleNewActualLocation(location);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, String.format("%s\n", "Connection suspended please reconnect..."));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, String.format("Connection failed : %s\n", connectionResult.toString()));

        if (!connectionResult.hasResolution()) {
            Log.e(TAG, "MyLocation services connection failed with code " + connectionResult.getErrorCode());
            return;
        }

        try {
            connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mListener.handleNewActualLocation(location);
    }

    public interface OnLocationListener {
        void handleNewActualLocation(Location location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    Log.e(TAG, "Permission granted...");
                    buildLocation();
                } else {
                    // Permission was denied.
                    Log.e(TAG, "Permission denied...");
                    System.exit(1);
                }
            }
        }
    }

    private void requestPermission() {
        if (isPermissionLocationDeny()) {
            requestLocationPermission();
        } else {
            buildLocation();
        }
    }

    private boolean isPermissionLocationDeny(){
        return ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    private void buildLocation() {
        if (mGoogleApiClient == null) {
            // Create the GoogleApiClient Object.
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
        if (mLocationRequest == null) {
            // Create the LocationRequest Object.
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        }
    }

    private void setOnLocationListener(Context context) {
        try {
            mListener = (OnLocationListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLocationListener");
        }
    }
}
