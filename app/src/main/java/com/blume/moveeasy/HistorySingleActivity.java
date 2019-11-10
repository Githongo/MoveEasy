package com.blume.moveeasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.blume.moveeasy.directionhelpers.FetchURL;
import com.blume.moveeasy.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    String rideId, currentUserId, driverId;

    Polyline currentPolyline;

    TextView pickupLocationView, destinationLocationView, distanceView, dateView, priceView, nameView, phoneView, vehicleView;
    Marker pickupMarker, destinationMarker;

    //Database Reference
    DatabaseReference historyRideInfoRef, historyDriverInfoRef;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        rideId = getIntent().getExtras().getString("rideId");

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        //initializing text views
        pickupLocationView = findViewById(R.id.pickupLocation);
        destinationLocationView = findViewById(R.id.destinationLocation);
        distanceView = findViewById(R.id.rideDistance);
        dateView = findViewById(R.id.rideDate);
        priceView = findViewById(R.id.ridePrice);
        nameView = findViewById(R.id.userName);
        phoneView = findViewById(R.id.userPhone);
        vehicleView = findViewById(R.id.userVehicle);

        currentUserId = FirebaseAuth.getInstance().getUid();

        historyRideInfoRef = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);
        getRideInformation();

    }


    private void getRideInformation() {
        historyRideInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot child:dataSnapshot.getChildren()){

                        if (child.getKey().equals("timestamp")){
                            dateView.setText("Date: "+ getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("distance")){
                            Float dist = Float.valueOf(child.getValue().toString());
                            int distance = Math.round(dist)/1000;
                            distanceView.setText("Distance: "+ distance + " Kilometres");
                        }
                        if (child.getKey().equals("pickup")){
                            pickupLocationView.setText("From: " +child.getValue().toString());
                        }
                        if (child.getKey().equals("destination")){
                            destinationLocationView.setText("Destination: " +child.getValue().toString());
                        }
                        if (child.getKey().equals("price")){
                            priceView.setText("Price: " + child.getValue().toString() + " KES");
                        }
                        if (dataSnapshot.child("driver")!=null){
                            driverId = dataSnapshot.child("driver").getValue().toString();
                            getRideDriverInfo(driverId);
                        }
                        if (child.getKey().equals("location")){
                            LatLng pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            LatLng destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));

                            MarkerOptions uMarkerOptions = new MarkerOptions();
                            uMarkerOptions.position(pickupLatLng);
                            uMarkerOptions.title("Pickup Location");
                            uMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            pickupMarker = mMap.addMarker(uMarkerOptions);

                            MarkerOptions destMarkerOptions = new MarkerOptions();
                            destMarkerOptions.position(destinationLatLng);
                            destMarkerOptions.title("Destination Location");
                            destMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                            destinationMarker = mMap.addMarker(destMarkerOptions);

                            if(destinationLatLng != new LatLng(0,0)){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 11));
                                new FetchURL(HistorySingleActivity.this).execute(getUrl(pickupMarker.getPosition(), destinationMarker.getPosition(), "driving"), "driving");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("dd-MM-yyyy, hh:mm", cal).toString();
        return date;
    }

    private void getRideDriverInfo(String driverId) {
        historyDriverInfoRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverId);
        historyDriverInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                    if(dataSnapshot.child("Username").exists()){
                        nameView.setText("Name: "+dataSnapshot.child("Username").getValue().toString());
                    }
                    if(dataSnapshot.child("Phone").exists()){
                        phoneView.setText("Phone: 0"+dataSnapshot.child("Phone").getValue().toString());
                    }
                    String VehicleType = "";
                    if(dataSnapshot.child("Vehicle").exists()){
                        VehicleType = dataSnapshot.child("Vehicle").getValue().toString();
                    }
                    if(dataSnapshot.child("Reg_no").exists()){
                        vehicleView.setText(VehicleType+" : "+dataSnapshot.child("Reg_no").getValue().toString());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
