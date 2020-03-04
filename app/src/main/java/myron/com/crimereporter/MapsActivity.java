package myron.com.crimereporter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = "MapsActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private DatabaseReference flashReporterDatabase = FirebaseDatabase.getInstance().getReference();
    private List<MapPointer> mapPointers = new ArrayList<MapPointer>();
    private Double latitude, longitude;
    private Marker mMarker;
    public Integer vote;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //widgets
    private EditText nSearchText;
    private ImageView mReview, mInfo, mvoteID;

    //Variables
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderclient;

    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Map is ready",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted){
            getDeviceLocation();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
            storeUserReviews();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        nSearchText = (EditText) findViewById(R.id.input_search);
        mInfo = (ImageView) findViewById(R.id.place_info);

        mvoteID = (ImageView) findViewById(R.id.voteID);
        mvoteID.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
//                UserVotes();
            }
        });

        getLocationPermission();
//        generateRandomReviews();
    }


    private void storeUserReviews(){
        mReview = (ImageView) findViewById(R.id.write_review);
        mReview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                try{
                    Intent intent = new Intent(MapsActivity.this, ReviewLocation.class);
                    intent.putExtra("latitude",latitude);
                    intent.putExtra("longitude",longitude);
                    startActivity(intent);
                }catch(NullPointerException e){
                    Log.d(TAG, "storeUserReviews " + e);
                }
            }
        });

    }

    public void getLocationReview(){
        String displayReview = "";
        List<Double> storeRatingList = new ArrayList<Double>();
        List<Integer> storeVotes = new ArrayList<Integer>();
        Double total = 0.0, averageRating, averageRatingDouble;
        int counter = 0, max;

        for (MapPointer mapPointer: mapPointers){
            Log.d(TAG,"ArraySize " + mapPointers.size());
            counter = counter +1;

            //getting Latitude and longitude from the database for the place that the user has searched
            Double Lat = mapPointer.getLatitude();
            Double Lng = mapPointer.getLongitude();

            //Converting to LatLng
            LatLng latlng = new LatLng (Lat, Lng);

            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));

            if (mapPointer != null){
                try{
                    Log.d(TAG, "getLocationReview: moving camera to: latitude: " + latlng.latitude + ",longitude: " + latlng.longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
                    mMap.clear();

                    String title = "Top Voted Review: ";

                    storeRatingList.add(mapPointer.getRating());
                    storeVotes.add(mapPointer.getVotes());

                        //get the highest number of votes
                         max = Integer.MIN_VALUE;
                        for (int j = 0; j < storeVotes.size(); j++){
                            if (max < storeVotes.get(j)){
                                max = storeVotes.get(j);
                                Log.d("HighestVote","Highest vote is " + max);
                            }
                        }


                        for (int i = 0; i < storeRatingList.size(); i++){
                            if (counter == mapPointers.size()){
                                total = total + storeRatingList.get(i);
                            }
                        }


                    if (mapPointer.getVotes() >= max){
                        displayReview = mapPointer.getReviews();
                    }


                    averageRating = total / storeRatingList.size();
                    averageRatingDouble = Math.round(averageRating * 10) / 10.0;

                    //Display the information in infoWindow on the map
                    String snippet = displayReview + "\n" + "Average Safety rating: " + averageRatingDouble + "\n"
                            + "votes: " + max + "\n";
                    MarkerOptions options = new MarkerOptions().position(latlng).title(title).snippet(snippet);

                    mMarker = mMap.addMarker(options);
                    mMarker.showInfoWindow();


                }catch(NullPointerException e){
                    Log.d(TAG,"getMapPointers: NullPointerException " + e.getMessage());
                }

                Log.d(TAG,"User Reviews " + mapPointer.getReviews());
            }
        }
    }


    // using this read method to retrieve data from the database to display it onto the info windows
    private void getMapPointers() {
        Log.d(TAG,"getMapPointers");

        mapPointers.clear();

        final Toast errorToast = Toast.makeText(this, "Unable to get done items", Toast.LENGTH_LONG);
        Log.d(TAG,"filtering" + longitude);
        flashReporterDatabase.orderByChild("longitude").equalTo(longitude).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        MapPointer item = snapshot.getValue(MapPointer.class);
                        Log.d(TAG,"getMapPointers " + item);
                        mapPointers.add(item);
                    } catch (DatabaseException e){
                        Log.d(TAG,e.getMessage());
                        errorToast.show();
                        finish();
                    }
                }
                getLocationReview();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorToast.show();
                finish();
            }
        });
    }


    //overide the enter key on the map
    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    private void init(){
        Log.d(TAG, "init: initialising");

        nSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute the method for searching
                    geoLocate();
                }
                return false;
            }
        });

    }

    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = nSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        List<Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocationName(searchString, 1);

        }catch (IOException e){
            Log.d(TAG, "geoLocate: IOException " + e.getMessage());
        }

        if (list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: Found a location: " + address.toString());
//            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
//            writeNewItem(address.getLatitude(), address.getLongitude());

            latitude = address.getLatitude();
            longitude = address.getLongitude();

            getMapPointers();
            moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM, address.getAddressLine(0));

        }
    }

    /*
    private boolean writeNewItem(Double latitude, Double longitude) {
        try {
            String itemId = flashReporterDatabase.push().getKey();
//            Log.d(TAG, "itemId: " + itemId);
//            Log.d(TAG, "HHH: " + flashReporterDatabase.child(itemId));

            ReviewLocation item = new ReviewLocation(itemId, latitude, longitude, "Hello");

            flashReporterDatabase.child(itemId).setValue(item);
            Log.d(TAG, "itemId2: " + itemId);
        } catch (DatabaseException e){
            Log.d(TAG, "itemId3: " + e.getMessage());
            return false;
        }
        getMapPointers();

        for (ReviewLocation mapPointer: mapPointers){
            Log.d(TAG,"hiii" + mapPointer.getID());
        }

        return true;
    }*/


//Get single item from the database with its ID
//    toDoItemReference = FirebaseDatabase.getInstance().getReference().child(itemID);

    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the current device location");

        mfusedLocationProviderclient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if (mLocationPermissionGranted){

                final Task location = mfusedLocationProviderclient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                          Log.d(TAG, "onComplete: Found Location");
                          Location currentLocation = (Location) task.getResult();

                          moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My location");

                        }else{
                            Log.d(TAG, "onComplete: Current Location is NULL");
                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: securityexception: " + e.getMessage());

        }

    }

    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving camera to: latitude: " + latLng.latitude + ",longitude: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(options);
    }

    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    private void initialiseMap(){
        Log.d(TAG, "initialiseMap: initialising map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permission");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mLocationPermissionGranted = true;
                        initialiseMap();
            }else{
                ActivityCompat.requestPermissions(this,permissions,
                        LOCATION_PERMISSION_REQEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permissions,
                    LOCATION_PERMISSION_REQEST_CODE);
        }
    }

    //https://www.youtube.com/watch?v=OknMZUnTyds&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQEST_CODE: {
                if(grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults [i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //initilise your map
                    initialiseMap();
                }
            }
        }
    }

    //51.5324044,longitude: -0.4729931 - university
    // 51.5332359,longitude: -0.4735494 - costcutter
    //51.5331506,longitude: -0.4741797 - subway


    public void generateRandomReviews(){
        
            try {
                Double latitude = 51.5331506, longitude = -0.4741797;
                String reviewToPrint= "";
                Random rand = new Random();
                int vote = 0;

                for (int i = 0; i<=5; i++) {
                    Double SafetyScore = rand.nextDouble() * 5;

                    SafetyScore = Math.round(SafetyScore * 10) / 10.0;

                    int randomNumber =  rand.nextInt(4);

                    if (randomNumber == 0) {
                        reviewToPrint = "Theft of bicycles";
                        vote = rand.nextInt(100);

                    } else if (randomNumber == 1){
                        reviewToPrint = "Robberies at night";
                        vote = rand.nextInt(100);

                    } else if(randomNumber == 2) {
                        reviewToPrint = "Shop break in by a gang";
                        vote = rand.nextInt(100);

                    } else if (randomNumber == 3){
                        reviewToPrint = "car break in";
                        vote = rand.nextInt(100);

                    } else if (randomNumber == 4){
                        reviewToPrint = "Stolen bicycles";
                        vote = rand.nextInt(100);

                    } else {
                        reviewToPrint = "Shop break in by a gang";
                        vote = rand.nextInt(100);
                    }

                    String itemId = flashReporterDatabase.push().getKey();
                    MapPointer item = new MapPointer(itemId, latitude, longitude, reviewToPrint, SafetyScore, vote);

                    flashReporterDatabase.child(itemId).setValue(item);
                }

            } catch (DatabaseException e){
                Log.d("generateRandomReviews", "itemId3: " + e.getMessage());
            }
        }

}