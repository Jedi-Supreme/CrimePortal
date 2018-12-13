package com.jedi_supreme.crimeportal.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi_supreme.crimeportal.R;
import com.jedi_supreme.crimeportal.adapters.recycler_distress_adapter;
import com.jedi_supreme.crimeportal.common;
import com.jedi_supreme.crimeportal.databases.crime_db;
import com.jedi_supreme.crimeportal.models.c_Distress;
import com.jedi_supreme.crimeportal.models.c_User;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class Dashboard extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnLongClickListener {

    ConstraintLayout const_dashboard;
    TextView tv_no_report;
    RecyclerView recycler_distress_list;
    FloatingActionButton fab_call_backup;
    ProgressBar pro_bar_dashboard;

    static final String LOCATION_PERMISSION_REQ = Manifest.permission.ACCESS_FINE_LOCATION;
    static final int LOCATION_REQ_CODE = 123;
    private static final int REQUEST_CHECK_SETTINGS = 1000;

    private GoogleApiClient googleApiClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location user_curr_location;

    private static final long
            UPDATE_INTERVAL = 15*1000,
            FASTEST_INTERVAL = 7*1000;

    private FusedLocationProviderClient fusedLocationProviderClient;

    WeakReference<Dashboard> weak_dashboard;
    crime_db crimeDb;

    //============================================ON CREATE=========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        weak_dashboard = new WeakReference<>(this);
        const_dashboard = findViewById(R.id.dashboard_layout);
        recycler_distress_list = findViewById(R.id.recycler_crime_reports);
        fab_call_backup = findViewById(R.id.fab_call_backup);
        tv_no_report = findViewById(R.id.tv_no_report);
        pro_bar_dashboard = findViewById(R.id.pro_bar_dashboard);

        crimeDb = new crime_db(getApplicationContext(),null);

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            login_screen();
        }

        //build google api client
        googleApiClient = new GoogleApiClient.Builder(weak_dashboard.get()).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        //location request builder
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        //location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        user_curr_location = location;
                        refresh_list(location);
                    }
                }
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(weak_dashboard.get());

        fab_call_backup.setOnLongClickListener(this);

        if (crimeDb.distress_users_list().size() > 0){
            recycler_distress_list.setVisibility(View.VISIBLE);
            tv_no_report.setVisibility(View.VISIBLE);
            tv_no_report.setText(R.string.loading_label);
            pro_bar_dashboard.setVisibility(View.VISIBLE);

        }else {
            recycler_distress_list.setVisibility(View.GONE);
            tv_no_report.setVisibility(View.VISIBLE);
            tv_no_report.setText(R.string.empty_report);
            pro_bar_dashboard.setVisibility(View.GONE);
        }


        try {
            get_crime_reports();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error getting crime reports " + e.toString(),Toast.LENGTH_LONG).show();
        }

    }
    //============================================ON CREATE=========================================


    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=Button Long Click-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    @Override
    public boolean onLongClick(View v) {

        //permission granted
        if (ActivityCompat.checkSelfPermission(weak_dashboard.get(), LOCATION_PERMISSION_REQ) ==
                PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(weak_dashboard.get(),
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                c_User enforcer = new c_User(
                                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                                        location.getLatitude(),
                                        location.getLongitude());

                                send_for_backup(enforcer);
                                common.snackbar(const_dashboard,"Backup Request Sent").show();

                            } else {
                                //request location
                                start_location_updates();
                            }

                        }
                    });

        } else {
            //request permission
            request_permission();
        }

        return true;
    }
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=Button Long Click-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    //----------------------------------------OVERRIDE METHODS--------------------------------------

    //====================================ACTIVITY RESULTS==========================================

    @Override
    public void onRequestPermissionsResult
            (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the task you need to do.
            check_locations_settings();

        } else {
            // permission denied, boo! Disable the functionality that depends on this permission.
            String msg = "Routing Disabled";
            common.snackbar(const_dashboard,msg).show();
        }
    }
    //====================================ACTIVITY RESULTS==========================================

    @Override
    protected void onResume(){
        super.onResume();

        check_permissions();
        get_crime_reports();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        start_location_updates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //========================================LOCATION LISTENER=====================================
    @Override
    public void onLocationChanged(Location location) {

        refresh_list(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        check_permissions();
    }

    @Override
    public void onProviderDisabled(String provider) {
        check_permissions();
    }
    //========================================LOCATION LISTENER=====================================

    //----------------------------------------OVERRIDE METHODS--------------------------------------



    //===========================================METHODS============================================

    void check_permissions(){
        if (ContextCompat.checkSelfPermission(weak_dashboard.get(), LOCATION_PERMISSION_REQ)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, Request permission
            request_permission();
        }else {
            //Request location updates
            check_locations_settings();
        }
    }

    void request_permission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(weak_dashboard.get(),LOCATION_PERMISSION_REQ)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(weak_dashboard.get(),
                    LOCATION_PERMISSION_REQ)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                String explanation = "Location permissions are needed for Routing";
                common.snackbar(const_dashboard,explanation).show();

                ActivityCompat.requestPermissions(weak_dashboard.get(), new String[]{LOCATION_PERMISSION_REQ},LOCATION_REQ_CODE);

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(weak_dashboard.get(), new String[]{LOCATION_PERMISSION_REQ},LOCATION_REQ_CODE);

                // LOCATION_REQ_CODE is an app-defined int constant.
                // The callback method gets the result of the request.
            }
        } else {
            // Permission has already been granted
            //Request location update
            check_locations_settings();
        }
    }

    void check_locations_settings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(false); //this is the key ingredient
        //**************************

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(weak_dashboard.get()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    start_location_updates();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult( weak_dashboard.get(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                // Ignore the error.
                            }
                            break;

                    }
                }
            }
        });

    }

    void start_location_updates() {
        if (ActivityCompat.checkSelfPermission(weak_dashboard.get(), LOCATION_PERMISSION_REQ)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).requestLocationUpdates(
                    locationRequest, locationCallback, null);
        }else {
            request_permission();
        }
    }

    void refresh_list(Location location){

        if (crimeDb.distress_users_list().size() > 0){
            recycler_distress_list.setVisibility(View.VISIBLE);
            tv_no_report.setVisibility(View.GONE);
            pro_bar_dashboard.setVisibility(View.GONE);

            recycler_distress_adapter distress_adapter = new
                    recycler_distress_adapter(this,location,crimeDb.distress_users_list());
            recycler_distress_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recycler_distress_list.setAdapter(distress_adapter);
        }else {
            recycler_distress_list.setVisibility(View.GONE);
            tv_no_report.setVisibility(View.VISIBLE);
            pro_bar_dashboard.setVisibility(View.GONE);
        }



    }

    void send_for_backup(c_User enforcer){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());

        String date = dateFormat.format(calendar.getTime());

        DatabaseReference comp_ref = FirebaseDatabase.getInstance().getReference(common.REQUEST_BACKUP);

        comp_ref.child(date).push().setValue(enforcer);

    }

    //Threaded process
    public void get_crime_reports(){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());

        String date = dateFormat.format(calendar.getTime());

        final DatabaseReference crime_ref = FirebaseDatabase.getInstance().getReference(common.EMERGENCY_REFERENCE)
                .child(common.POLICE_EMERGENCY).child(date);
        crime_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final Iterable<DataSnapshot> crimes_snap = dataSnapshot.getChildren();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (DataSnapshot distress_snap : crimes_snap){

                            c_User distress_user = distress_snap.getValue(c_User.class);
                            crimeDb.add_distress(new c_Distress(distress_snap.getKey(),distress_user));

                        }mHandler.sendEmptyMessage(0);
                    }
                }).start();

                crime_ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //handler for thread
    Handler mHandler = new Handler(new HandlerCallback());

    //handler callback
    class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {

            // Handle message code
            if (user_curr_location!= null){
                refresh_list(user_curr_location);
            }else {
                check_permissions();
            }
            return true;
        }
    }

    /* void profile_intent(String mobile, String pushkey){
        Intent profile_intent = new Intent(this,Profile_view.class);
        profile_intent.putExtra(c_User.COLUMN_MOBILE,mobile);
        profile_intent.putExtra(c_User.COLUMN_PUSHKEY,pushkey);
        startActivity(profile_intent);
    }

   TODO USER PROFILE AND MENUS

    void change_state_online(String push_key,c_User distress_user){
        DatabaseReference comp_ref = fire_DB.getReference(EMERGENCY_REFERENCE).child(POLICE_EMERGENCY).child(push_key);
        comp_ref.setValue(distress_user);
        refresh_distresslist();
    }*/

    //===========================================METHODS============================================


    //------------------------------------------INTENTS---------------------------------------------
    void login_screen(){
        Intent login_intent = new Intent(getApplicationContext(),login_activity.class);
        startActivity(login_intent);
        super.finish();
    }
    //------------------------------------------INTENTS---------------------------------------------
}
