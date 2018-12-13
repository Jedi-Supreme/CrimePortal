package com.jedi_supreme.crimeportal.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi_supreme.crimeportal.R;
import com.jedi_supreme.crimeportal.adapters.ice_contact_array_adapter;
import com.jedi_supreme.crimeportal.common;
import com.jedi_supreme.crimeportal.databases.crime_db;
import com.jedi_supreme.crimeportal.models.c_User;

import java.util.ArrayList;

public class UserProfile extends AppCompatActivity {

    TextInputEditText et_profile_fn, et_profile_ln, et_profile_homeAddress, et_profile_number;
    Button bt_profile_map,bt_profile_direction;
    ConstraintLayout const_profile_layout;
    ProgressBar pro_bar_profile;

    ListView lv_profile_contacts;
    Bundle profile_bundle;

    crime_db crimeDb;
    c_User sos_user;

    String sos_fire_id;

    //check local db for user data
    //if null fetch online and save to local db
    //after save reload user data
    //if not null load data and check online for details update, repeat step 3

    //==============================================ON CREATE=======================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        crimeDb = new crime_db(getApplicationContext(),null);
        profile_bundle = getIntent().getExtras();


        et_profile_number = findViewById(R.id.et_profile_number);
        et_profile_fn = findViewById(R.id.et_profile_fn);
        et_profile_ln = findViewById(R.id.et_profile_ln);
        et_profile_homeAddress = findViewById(R.id.et_profile_homeadd);

        lv_profile_contacts = findViewById(R.id.lv_profile_contacts);
        const_profile_layout = findViewById(R.id.user_profile_layout);

        bt_profile_map = findViewById(R.id.bt_profile_map);
        bt_profile_direction = findViewById(R.id.bt_profile_directions);
        pro_bar_profile = findViewById(R.id.pro_bar_profile);

        if (profile_bundle != null){

            sos_fire_id = profile_bundle.getString(common.BUNDLE_KEY);

            try {
                bind_user_data();
            }catch (Exception ignored){}

            fetch_details_onliine();
        }



    }
    //==============================================ON CREATE=======================================

    //------------------------------------------------METHODS---------------------------------------

    public void bind_user_data(){

        sos_user = crimeDb.find_fulluser_profile(sos_fire_id);

        if (sos_user != null){
            //set user values to views
            bt_profile_map.setEnabled(true);
            bt_profile_direction.setEnabled(true);

            et_profile_fn.setText(sos_user.getFn());
            et_profile_ln.setText(sos_user.getLn());

            et_profile_number.setText(sos_user.getMobile());
            et_profile_homeAddress.setText(sos_user.getHomeAddress());

            refresh_contacts_list(sos_user.getIce_contacts_list());

        }else{
            bt_profile_map.setEnabled(false);
            bt_profile_direction.setEnabled(false);
            fetch_details_onliine();
        }

    }

    public void fetch_details_onliine(){

        final DatabaseReference users_profile_ref = FirebaseDatabase.getInstance().getReference(common.USERS_PROFILE_REF);
        users_profile_ref.child(sos_fire_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                c_User sos_fire_user = dataSnapshot.getValue(c_User.class);
                crimeDb.add_user_profile(sos_fire_user);

                bind_user_data();
             users_profile_ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void refresh_contacts_list(ArrayList<String> contactslist){
        ice_contact_array_adapter iceContactArrayAdapter = new ice_contact_array_adapter(getApplicationContext(),contactslist);
        lv_profile_contacts.setAdapter(iceContactArrayAdapter);
    }

    //------------------------------------------------METHODS---------------------------------------

    //--------------------------------------------BUTTON CLICK LISTENERS----------------------------
    public void user_map_view(View view) {

        Uri gmmIntentUri;
        Intent mapIntent;

        switch (view.getId()){

            case R.id.bt_profile_map:

                String name = sos_user.getFn() + " " + sos_user.getLn();
                gmmIntentUri = Uri.parse("geo:0,0?q="+sos_user.getHome_latitude()+","+sos_user.getHome_longitude()+"("+name+")");
                mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }else {
                    common.snackbar(const_profile_layout,"Map Application Not Found").show();
                }

                break;

            case R.id.bt_profile_directions:

                gmmIntentUri = Uri.parse("google.navigation:q="+sos_user.getHome_latitude()+","+sos_user.getHome_longitude());
                mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }else {
                    common.snackbar(const_profile_layout,"Map Application Not Found").show();
                }

                break;
        }

    }
    //--------------------------------------------BUTTON CLICK LISTENERS----------------------------
}
