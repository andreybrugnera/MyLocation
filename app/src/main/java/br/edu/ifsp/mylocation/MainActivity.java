package br.edu.ifsp.mylocation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //Cliente da Api do google services
    private GoogleApiClient googleApiClient;
    //Úlitma localização obtida
    private Location lastLocation;

    //Intervalo de atualização da localização
    private static int UPDATE_INTERVAL = 5000; //5 segundos
    private static int DISPLACEMENT = 10; // Atualização a cada 10 metros
    private LocationRequest locationRequest;
    private boolean autoUpdateLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
        createLocationRequest();

        //Botão para iniciar ou interromper a atualização automática da localização
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.tbt_update_location);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });
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
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoUpdateLocation) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissão aceita", Toast.LENGTH_SHORT).show();
                    loadLastLocation();
                } else {
                    Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Carrega a última localização
     * do dispositivo
     */
    public void loadLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        try{
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
        }catch(SecurityException ex){
            Log.e(TAG, getResources().getString(R.string.error_load_last_location), ex);
        }
        /*
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> locationProviders = locationManager.getProviders(true);
        try {
            //Busca a localização mais precisa e guarda em lastLocation
            for (String provider : locationProviders) {
                Location location = locationManager.getLastKnownLocation(provider);
                Log.i(TAG, "Obtendo localização por (" + provider + "): " + location);
                if (location == null) {
                    continue;
                }
                if (lastLocation == null || location.getAccuracy() > lastLocation.getAccuracy()) {
                    lastLocation = location;
                }
            }
        } catch (SecurityException se) {
            Log.e(TAG, getResources().getString(R.string.error_load_last_location), se);
        }*/
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
        showLastLocation();
    }

    private void showLastLocation() {
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

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    /**
     * Inicia a atualização da localização de tempos em tempos
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
            autoUpdateLocation = true;
        } catch (SecurityException ex) {
            Log.e(TAG, getResources().getString(R.string.error_load_last_location), ex);
        }
    }

    /**
     * Interrompoe a atualização da localização
     */
    protected void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
        autoUpdateLocation = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Ao se conectar, obtém a última localização
        if (autoUpdateLocation) {
            startLocationUpdates();
        } else {
            loadLastLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        Log.i(TAG, "Localização atualizada");
        showLastLocation();
    }

    /**
     * Abre google maps com a localização
     * atual do dispositivo
     * @param v
     */
    public void showGoogleMaps(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("location", lastLocation);
        startActivity(intent);
    }
}
