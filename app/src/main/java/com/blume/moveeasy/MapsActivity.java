package com.blume.moveeasy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseError;
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
    Location mLastLocation, mPickupLocation, DestinationLocation;
    LocationRequest mLocationRequest;
    Marker pickupLocationMarker, destinationMarker;
    Polyline currentPolyline;
    private MarkerOptions place1, place2;
    LatLng userLatLng, pickupLatLng;
    CoordinatorLayout coordinatorLayout;

    private MaterialSearchBar materialSearchBar;
    //material searchbar1
    private  MaterialSearchBar materialSearchBar1;

    private DrawerLayout drawerLayout;
    private View mapView;
    private Button mRequest;
    private TextView nameView, regnoView, phonenoView;


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

        //finding layout components
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar1 = findViewById(R.id.searchBar1);
        nameView = findViewById(R.id.namevalue);
        regnoView = findViewById(R.id.regnovalue);
        phonenoView = findViewById(R.id.phonenovalue);

        //drawerLayout =findViewById(R.id.drawer_layout);

        //BOTTOM SHEET HIDDEN AT PEAK HEIGHT
        linearLayout = findViewById(R.id.driverbottomsheet);
        BottomSheetBehavior bottomSheetBehavior1 = BottomSheetBehavior.from(linearLayout);
        bottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Destination material search bar hidden
        materialSearchBar.setVisibility(View.INVISIBLE);

        //Confirm Request Button
        mRequest = findViewById(R.id.request);
        coordinatorLayout = findViewById(R.id.maplayout);
        mRequest.setVisibility(View.GONE);



        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (destinationMarker == null) {

                    Toast.makeText(MapsActivity.this, "Please Enter Destination", Toast.LENGTH_LONG).show();
                }else {

                    //Setting pick up location marker
                    /*MarkerOptions uMarkerOptions = new MarkerOptions();
                    uMarkerOptions.position(userLatLng);
                    uMarkerOptions.title("Pick-up Location");
                    uMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    pickupLocationMarker = mMap.addMarker(uMarkerOptions);
                    */

                    //Setting pickup location details
                    String userId = FirebaseAuth.getInstance().getUid();
                    DatabaseReference customerAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest");
                    GeoFire geoFire = new GeoFire(customerAvailabilityRef);
                    geoFire.setLocation(userId, new GeoLocation(mPickupLocation.getLatitude(), mPickupLocation.getLongitude()));

                    //Setting Destination and fare details
                    DatabaseReference destinationRef = FirebaseDatabase.getInstance().getReference().child("RequestDestination");
                    GeoFire destnGeoFire = new GeoFire(destinationRef);
                    destnGeoFire.setLocation(userId, new GeoLocation(DestinationLocation.getLatitude(), DestinationLocation.getLongitude()));

                    destinationRef.child(userId).child("Price").setValue(mDescription[0]);

                    /* mRequest.setText("Submitting your request...");*/
                    final Snackbar snack = Snackbar.make(coordinatorLayout, "Submitting your request...", Snackbar.LENGTH_LONG );
                    snack.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    });
                    snack.show();
                    mRequest.setVisibility(View.INVISIBLE);


                    //Removing input components
                    materialSearchBar1.setVisibility(View.GONE);
                    materialSearchBar.setVisibility(View.GONE);
                    BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    getClosestDriver();
                }
            }
        });

        //initialize place1
        place1 = new MarkerOptions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapView = mapFragment.getView();
        Places.initialize(MapsActivity.this, "AIzaSyAyvaHbjQDxr3r73C4KilsixSCFzA5Tlj8");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //Pickup Location Searchbar
        materialSearchBar1.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
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
                    //drawerLayout.openDrawer(Gravity.LEFT);


                }else if(buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar1.disableSearch();
                }

            }
        });

        materialSearchBar1.addTextChangeListener(new TextWatcher() {
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
                                materialSearchBar1.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar1.isSuggestionsVisible()){
                                    materialSearchBar1.showSuggestionsList();
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
        materialSearchBar1.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar1.getLastSuggestions().get(position).toString();
                materialSearchBar1.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar1.clearSuggestions();
                    }
                },1000);


                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                if (imm != null){
                    imm.hideSoftInputFromWindow(materialSearchBar1.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
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

                                pickupLatLng = latLngOfPlace;
                                mPickupLocation = new Location("");
                                mPickupLocation.setLatitude(latLngOfPlace.latitude);
                                mPickupLocation.setLongitude(latLngOfPlace.longitude);

                                //setting destination marker
                                if(pickupLocationMarker != null){
                                    pickupLocationMarker.remove();
                                }

                                MarkerOptions uMarkerOptions = new MarkerOptions();
                                uMarkerOptions.position(latLngOfPlace);
                                uMarkerOptions.title("Pickup Location");
                                uMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                                pickupLocationMarker = mMap.addMarker(uMarkerOptions);

                                materialSearchBar.setVisibility(View.VISIBLE);


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
                            if (materialSearchBar1.isSuggestionsVisible())
                                materialSearchBar1.clearSuggestions();
                            if (materialSearchBar1.isSearchEnabled())
                                materialSearchBar1.disableSearch();
                            return false;
                        }
                    });
                }
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });





        //Destination location searchbar
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

                                //setting destination location
                                DestinationLocation = new Location("");
                                DestinationLocation.setLatitude(latLngOfPlace.latitude);
                                DestinationLocation.setLongitude(latLngOfPlace.longitude);

                                //setting destination marker
                                if(destinationMarker != null){
                                    destinationMarker.remove();
                                }

                                MarkerOptions uMarkerOptions = new MarkerOptions();
                                uMarkerOptions.position(latLngOfPlace);
                                uMarkerOptions.title("Destination Location");
                                uMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                Location.distanceBetween(pickupLatLng.latitude, pickupLatLng.longitude, latLngOfPlace.latitude, latLngOfPlace.longitude, toDestination);
                                uMarkerOptions.snippet(toDestination[0] + " Metres away");
                                getEstimate(toDestination);//getting estimate

                                destinationMarker = mMap.addMarker(uMarkerOptions);
                                new FetchURL(MapsActivity.this).execute(getUrl(pickupLocationMarker.getPosition(), destinationMarker.getPosition(), "driving"), "driving");

                                //listview adapter
                                listView=findViewById(R.id.listView);

                                MyAdapter adapter = new MyAdapter(MapsActivity.this, mTitle, mDescription, images);
                                listView.setAdapter(adapter);
                                linearLayout = findViewById(R.id.bottom_sheet);

                                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
                                //list view visible
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

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
                    /*mRequest.setText("Driver Found");*/

                    final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Driver Found", Snackbar.LENGTH_LONG );
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();

                    //get driver details
                    getDriverInfo();

                    //show driver details bottomsheet
                    linearLayout = findViewById(R.id.driverbottomsheet);
                    BottomSheetBehavior bottomSheetBehavior1 = BottomSheetBehavior.from(linearLayout);
                    bottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);

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

                    if (distance<500){
                        final Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Driver's here", Snackbar.LENGTH_LONG );
                        snackbar1.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar1.dismiss();
                            }
                        });
                        snackbar1.show();
                        //mRequest.setText("Driver's Here");
                    }else{
                        final Snackbar snackbar2 = Snackbar.make(coordinatorLayout, "Driver Found  " + distance, Snackbar.LENGTH_LONG );
                        snackbar2.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar2.dismiss();
                            }
                        });
                        snackbar2.show();
                        //mRequest.setText("Driver Found: " + distance);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getDriverInfo(){
        DatabaseReference mDriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverFoundID);
        mDriverDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("name")!=null){
                        nameView.setText(dataSnapshot.child("Username").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        regnoView.setText(dataSnapshot.child("Reg_no").getValue().toString());
                    }
                    if(dataSnapshot.child("name")!=null){
                        phonenoView.setText(dataSnapshot.child("Phone").getValue().toString());
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