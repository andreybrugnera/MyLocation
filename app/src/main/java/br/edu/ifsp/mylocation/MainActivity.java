package br.edu.ifsp.mylocation;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //Cliente da Api do google services
    private GoogleApiClient googleApiClient;
    //Úlitma localização obtida
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Carrega a última localização
     * do dispositivo
     */
    public void loadLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> locationProviders = locationManager.getProviders(true);
        try{
            //Busca a localização mais precisa e guarda em lastLocation
            for (String provider : locationProviders) {
                Location location = locationManager.getLastKnownLocation(provider);
                Log.i(TAG, "Obtendo localização por ("+provider+"): "+location);
                if (location == null) {
                    continue;
                }
                if (lastLocation == null || location.getAccuracy() > lastLocation.getAccuracy()) {
                    lastLocation = location;
                }
            }
        }catch(SecurityException se){
            Log.e(TAG, getResources().getString(R.string.error_load_last_location), se);
        }
    }

    /**
     * Cria o cliente do google play services
     */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    public void showLastLocation(View v) {
        loadLastLocation();
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();

            StringBuilder message = new StringBuilder()
                    .append(getResources().getString(R.string.latitude))
                    .append(": ").append(latitude).append(" ")
                    .append(getResources().getString(R.string.longitude))
                    .append(": ").append(longitude);

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error_load_last_location), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        loadLastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
