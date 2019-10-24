package com.blume.moveeasy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.gesture.Prediction;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blume.moveeasy.directionhelpers.FetchURL;
import com.blume.moveeasy.directionhelpers.TaskLoadedCallback;
import com.blume.moveeasy.model.MyObject;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, TaskLoadedCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 99;
    private GoogleMap mMap;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, mPickupLocation;
    LocationRequest mLocationRequest;
    Marker pickupLocationMarker, destinationMarker;
    Polyline currentPolyline;
    private MarkerOptions place1, place2;
    LatLng requestLatLng;

    private MaterialSearchBar materialSearchBar;
    private DrawerLayout drawerLayout;
    private View mapView;
    private Button mRequest;

    //listview
    LinearLayout linearLayout;
    ListView listView;
    String mTitle[] = {"Motorbike", "Pickup", "Truck"};
    String mDescription[] = {"0", "0", "0"};
    int images[] = {R.drawable.ic_motorcycle,R.drawable.ic_pickup_truck,R.drawable.ic_delivery_truck};

    //distance between pickup and destination
    float toDestination[] = new float[10];


    private final float DEFAULT_ZOOM = 14;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        materialSearchBar = findViewById(R.id.searchBar);
        //Confirm Request Button
        mRequest = findViewById(R.id.request);
        mRequest.setVisibility(View.GONE);



        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (destinationMarker == null) {

                    Toast.makeText(MapsActivity.this, "Please Enter Destination", Toast.LENGTH_LONG).show();
                }else {

                    //requestLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    //place1.position(requestLatLng);

                    MarkerOptions uMarkerOptions = new MarkerOptions();
                    uMarkerOptions.position(userLatLng);
                    uMarkerOptions.title("Pick-up Location");
                    uMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    pickupLocationMarker = mMap.addMarker(uMarkerOptions);

                    String userId = FirebaseAuth.getInstance().getUid();
                    DatabaseReference customerAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest");

                    GeoFire geoFire = new GeoFire(customerAvailabilityRef);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    mRequest.setText("Submitting your request...");

                    getClosestDriver();
                }
            }
        });

        place1 = new MarkerOptions().position(new LatLng(-1.310136, 36.813501)).title("Strathmore");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapView = mapFragment.getView();
        Places.initialize(MapsActivity.this, "AIzaSyAyvaHbjQDxr3r73C4KilsixSCFzA5Tlj8");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();



        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null,true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION){
                    //opening a nav bar
                   // drawerLayout.openDrawer(Gravity.LEFT);


                }else if(buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar.disableSearch();
                }

            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("ke")
                        //.setTypeFilter(TypeFilter.CITIES)
                        .setSessionToken(token)
                        .setQuery(charSequence.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful()){
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null){
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i=0; i<predictionList.size(); i++ ){
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()){
                                    materialSearchBar.showSuggestionsList();
                                }
                            }

                        } else {
                            Log.i("mytag", "Prediction Fetching task unsuccessful");

                        }
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                },1000);


                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                if (imm != null){
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    String placeId = selectedPrediction.getPlaceId();
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                    FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                    placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                            Place place = fetchPlaceResponse.getPlace();
                            Log.i("my tag", "Place found: " + place.getName());
                            LatLng latLngOfPlace =place.getLatLng();

                            if (latLngOfPlace != null){
                                        /*mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngOfPlace));
                                        mMap.animateCamera(CameraUpdateFactory.zoomBy(DEFAULT_ZOOM));*/
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                                // LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                                /*if(destinationMarker != null){
                                    destinationMarker.remove();
                                }*/

                                MarkerOptions uMarkerOptions = new MarkerOptions();
                                uMarkerOptions.position(latLngOfPlace);
                                uMarkerOptions.title("Destination Location");
                                uMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                Location.distanceBetween(userLatLng.latitude, userLatLng.longitude, latLngOfPlace.latitude, latLngOfPlace.longitude, toDestination);
                                uMarkerOptions.snippet(toDestination[0] + " Metres away");
                                getEstimate(toDestination);

                                //listview adapter
                                listView=findViewById(R.id.listView);

                                MyAdapter adapter = new MyAdapter(MapsActivity.this, mTitle, mDescription, images);
                                listView.setAdapter(adapter);
                                linearLayout = findViewById(R.id.bottom_sheet);



                                final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);

                                destinationMarker = mMap.addMarker(uMarkerOptions);
                                new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), destinationMarker.getPosition(), "driving"), "driving");
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


                                /*listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        //mRequest.setVisibility(View.VISIBLE);
                                        //String vehicleType = parent.getItemAtPosition(position).toString();
                                        MyObject tmp=(MyObject) parent.getItemAtPosition(position);
                                        Toast.makeText(MapsActivity.this, "Vehicle: "+tmp.getVehicle(), Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });*/

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        if(position == 0){
                                            Toast.makeText(MapsActivity.this, "Motorcycle Selected", Toast.LENGTH_SHORT).show();
                                            mRequest.setVisibility(View.VISIBLE);
                                        }
                                        if(position == 1){
                                            Toast.makeText(MapsActivity.this, "Pick-up Selected", Toast.LENGTH_SHORT).show();
                                            mRequest.setVisibility(View.VISIBLE);
                                        }
                                        if(position == 2){
                                            Toast.makeText(MapsActivity.this, "Truck Selected", Toast.LENGTH_SHORT).show();
                                            mRequest.setVisibility(View.VISIBLE);
                                        }

                                    }
                                });

                                //mRequest.setVisibility(View.VISIBLE);


                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof ApiException){
                                ApiException apiException = (ApiException) e ;
                                apiException.printStackTrace();
                                int statusCode = apiException.getStatusCode();
                                Log.i("my tag", "Place not found: " + e.getMessage());
                                Log.i("my tag", "status code: "+ statusCode);
                            }

                        }
                    });

                    mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        @Override
                        public boolean onMyLocationButtonClick() {
                            if (materialSearchBar.isSuggestionsVisible())
                                materialSearchBar.clearSuggestions();
                            if (materialSearchBar.isSearchEnabled())
                                materialSearchBar.disableSearch();
                            return false;
                        }
                    });
                }
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }



    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String rTitle[];
        String rDescreption[];
        int rImgs[];

        MyAdapter (Context c, String title[],String description[], int imgs[]){
            super(c, R.layout.row, R.id.textView1, title);
            this.context=c;
            this.rTitle = title;
            this.rDescreption=description;
            this.rImgs = imgs;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images = row.findViewById(R.id.img1);
            TextView mTitle =row.findViewById(R.id.textView1);
            TextView mDescription =row.findViewById(R.id.textView2);

            images.setImageResource(rImgs[position]);
            mTitle.setText(rTitle[position]);
            mDescription.setText(rDescreption[position]);



            return row;
        }
    }

    //Price Caclulator
    private void getEstimate(float distance[]){
        float approxDist = distance[0];

        //for bike
        float bikeDouble = ((approxDist/1000)*10)+50;
        int bikePrice = Math.round(bikeDouble);

        //for pickup
        float pickupDouble = ((approxDist/1000)*20)+100;
        int pickupPrice = Math.round(pickupDouble);

        //for Truck
        float truckDouble = ((approxDist/1000)*50)+200;
        int truckPrice = Math.round(truckDouble);

        mDescription[0] = Integer.toString(bikePrice);
        mDescription[1] = Integer.toString(pickupPrice);
        mDescription[2] = Integer.toString(truckPrice);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();
        buildGoogleApiClient();

        //mMap.addMarker(place1);
        //mMap.addMarker(place2);

        if(mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,40,200);

        }


    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("Driver Availability");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(userLatLng.latitude, userLatLng.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound){
                    driverFound = true;
                    driverFoundID = key;

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverFoundID).child("customerRequest");
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    //map.put("destination", destination);
                    //map.put("destinationLat", destinationLatLng.latitude);
                    //map.put("destinationLng", destinationLatLng.longitude);
                    driverRef.updateChildren(map);

                    getDriverLocation();
                    mRequest.setText("Looking for driver location...");

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    ValueEventListener driverLocationRefListener;

    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    mRequest.setText("Driver Found");

                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLong);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver"));

                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(driverLatLng.latitude);
                    driverLocation.setLongitude(driverLatLng.longitude);
                    float distance = mPickupLocation.distanceTo(driverLocation);

                    if (distance<100){
                        mRequest.setText("Driver's Here");
                    }else{
                        mRequest.setText("Driver Found: " + distance);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

    LatLng userLatLng;
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //to be changed
        mPickupLocation = mLastLocation;


        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        place1.position(userLatLng);


        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15));

        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
        }
        else{
            Toast.makeText(this, "Location Disabled!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}