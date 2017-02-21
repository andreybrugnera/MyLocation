package br.edu.ifsp.mylocation;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMaps;
    private double latitude;
    private double longitude;

    private final float MIN_ZOOM = 0;
    private final float MAX_ZOOM = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        latitude = getIntent().getDoubleExtra("latitude",0);
        longitude = getIntent().getDoubleExtra("longitude",0);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMaps = googleMap;

        LatLng atuaiLocation = new LatLng(latitude, longitude);
        googleMaps.addMarker(new MarkerOptions().position(atuaiLocation).title(getResources().getString(R.string.current_location)));
        googleMaps.moveCamera(CameraUpdateFactory.newLatLng(atuaiLocation));
        googleMaps.setMinZoomPreference(MIN_ZOOM);
        googleMaps.setMaxZoomPreference(MAX_ZOOM);
        //Aplica zoom
        googleMaps.animateCamera(CameraUpdateFactory.zoomTo(MAX_ZOOM), 1000, null);
    }
}
