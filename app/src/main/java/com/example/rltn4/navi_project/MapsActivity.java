package com.example.rltn4.navi_project;

import android.location.Address;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.location.Geocoder;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
        mMap = googleMap;
        geocoder = new Geocoder(this);

        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName("중앙대학교 서울캠퍼스", 10);
        }
        catch(IOException e){
            Log.e(this.getClass().getName(), "geocoder error\n");
        }

        Log.e(this.getClass().getName(), addressList.get(0).toString());

        String []splitStr = addressList.get(0).toString().split(",");

//        Log.e(this.getClass().getName(), addressList.get(0).toString().substring(
//                addressList.get(0).toString().indexOf("latitude="),
//                addressList.get(0).toString().indexOf(addressList.get(0).toString().indexOf("latitude=") + "latitude=".length())
//        ));

//        for(int i = 0; i < splitStr.length ; i++)
//        {
//            Log.e(this.getClass().getName(), splitStr[i].toString());
//        }

//        Double latitude = Double.parseDouble(
//            addressList.get(0).toString().substring(
//                    addressList.get(0).toString().indexOf("latitude="),
//                    addressList.get(0).toString().indexOf("latitude=") + "latitude=".length())
//        );
//        Double longitude = Double.parseDouble(
//            addressList.get(0).toString().substring(
//                    addressList.get(0).toString().indexOf("longitude="),
//                    addressList.get(0).toString().indexOf("longitude=") + "longitude=".length()
//            )
//        );
        Double latitude = Double.parseDouble(splitStr[13].substring("latitude=".length()));
        Double longitude = Double.parseDouble(splitStr[15].substring("longitude=".length()));

        LatLng dest = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(dest).title("Marker in dest"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dest, 15));

//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                MarkerOptions mOptions = new MarkerOptions();
//
//                mOptions.title("Here");
//                mOptions.position(latLng);
//                mMap.addMarker(mOptions);
//            }
//        });
        // Add a marker in Sydney and move the camera
//        LatLng seoul = new LatLng(37.56, 126.97);
//        mMap.addMarker(new MarkerOptions().position(seoul).title("Marker in Seoul"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
    }
}
