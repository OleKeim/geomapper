package com.example.olekeim.gpsoppgave;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.security.PublicKey;
import java.util.List;

import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geocoding.utils.LocationAddress;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    MapView mapView;
    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private TextView tilText;
    private TextView fraText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //activity maps?


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SmartLocation.with(this).geocoding()
                .direct("Karvebakken 5, Mo i Rana, Norway", new OnGeocodingListener() {
                    @Override
                    public void onLocationResolved(String name, List<LocationAddress> results) {
                        // name is the same you introduced in the parameters of the call
                        // results could come empty if there is no match, so please add some checks around that
                        // LocationAddress is a wrapper class for Address that has a Location based on its data
                        if (results.size() > 0) {
                            Location mestallaLocation = results.get(0).getLocation();
                            Log.d("response", mestallaLocation.toString());
                        }
                    }


                });

        tilText = (TextView) findViewById(R.id.Til);
        fraText = (TextView) findViewById(R.id.Fra);

        Button sokeknapp = findViewById(R.id.sokeKnapp);
        sokeknapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String adresse1 = fraText.getText().toString();
                String adresse2 = tilText.getText().toString();

                if(adresse1.isEmpty() || adresse2.isEmpty()){
                   Toast toast = Toast.makeText(getApplicationContext(), "Du må huske å fylle inn feltene",Toast.LENGTH_SHORT);
                   toast.show();
                   return;
                }
                LatLng fra = getLatLong(adresse1);
                LatLng til = getLatLong(adresse2);

                if (fra == null || til == null){
                    Toast toast = Toast.makeText(getApplicationContext(), "Fant ikke adressene du oppgav",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                mMap.addMarker(new MarkerOptions().position(fra).title(adresse1));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(til));

                Polyline linje = mMap.addPolyline(new PolylineOptions().add(
                        fra, til)
                        .width(15)
                        .color(Color.GREEN));

            }
        });


        GoogleDirection.withServerKey("AIzaSyBtWKH927EIIXBFuURfl_-uwT8y2rsNMA8")
                .from(new LatLng(37.7681994, -122.444538))
                .to(new LatLng(37.7749003, -122.4034934))
                .avoid(AvoidType.FERRIES)
                .avoid(AvoidType.HIGHWAYS)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            // Do something
                        } else {
                            // Do something
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                    }
                });


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            initLocation();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }


    }


    public void initLocation() {
        //Find user location using SmartLocation lib

        SmartLocation.with(this).location().oneFix().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {
                Log.d("lokasjon", location.toString());

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng etSted = new LatLng(66.317055, 14.142619); //mo i rana
        mMap.addMarker(new MarkerOptions().position(etSted).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(etSted));


        LatLng annetSted = new LatLng(69.656601, 18.956197);//tromsø

        Polyline linje = mMap.addPolyline(new PolylineOptions().add(
                etSted, annetSted)
                .width(15)
                .color(Color.GREEN)
        );
    }

    private LatLng getLatLong(String adresse){
        Geocoder geocoder = new Geocoder(getApplicationContext());

        try {
            List<Address> lokasjon = geocoder.getFromLocationName(adresse,1);
            if (lokasjon.isEmpty()){
                return null;
            }
            Address funnet = lokasjon.get(0);
            LatLng latlong = new LatLng(funnet.getLatitude(),funnet.getLongitude());
            return latlong;
        } catch (IOException e) {
            return null;
        }

    }

}


