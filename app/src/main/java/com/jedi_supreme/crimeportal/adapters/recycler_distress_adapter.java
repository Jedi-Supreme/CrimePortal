package com.jedi_supreme.crimeportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jedi_supreme.crimeportal.R;
import com.jedi_supreme.crimeportal.activities.Dashboard;
import com.jedi_supreme.crimeportal.activities.UserProfile;
import com.jedi_supreme.crimeportal.common;
import com.jedi_supreme.crimeportal.databases.crime_db;
import com.jedi_supreme.crimeportal.models.c_Distress;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.jedi_supreme.crimeportal.common.EMERGENCY_REFERENCE;
import static com.jedi_supreme.crimeportal.common.POLICE_EMERGENCY;

public class recycler_distress_adapter extends RecyclerView.Adapter {

    private ArrayList<c_Distress> distress_users;
    private Location location;
    private AppCompatActivity activity;

    public recycler_distress_adapter (AppCompatActivity activity,Location current_loc, ArrayList<c_Distress> distress_users) {
        this.distress_users = distress_users;
        location = current_loc;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.distress_layout,parent,false);
        return new distress_list_holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((distress_list_holder) holder).bind_views(distress_users.get(position));
    }

    @Override
    public int getItemCount() {
        return distress_users.size();
    }

    public class distress_list_holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView
                tv_diss_name,tv_diss_time,
                tv_diss_body,tv_diss_state;

        c_Distress global_distress;

        Button bt_diss_map, bt_diss_profile, bt_diss_direction;

        WeakReference<Context> weak_mcontext;
        crime_db crimeDb;

        distress_list_holder(View itemView) {
            super(itemView);

            weak_mcontext = new WeakReference<>(itemView.getContext());
            crimeDb = new crime_db(weak_mcontext.get(),null);

            bt_diss_map = itemView.findViewById(R.id.bt_diss_map);
            bt_diss_direction = itemView.findViewById(R.id.bt_diss_directions);
            bt_diss_profile = itemView.findViewById(R.id.bt_diss_profile);

            tv_diss_name = itemView.findViewById(R.id.tv_diss_name);
            tv_diss_time = itemView.findViewById(R.id.tv_diss_date_time);
            tv_diss_body = itemView.findViewById(R.id.tv_diss_body);
            tv_diss_state = itemView.findViewById(R.id.tv_diss_state);

            tv_diss_state.setOnClickListener(this);
            tv_diss_time.setOnClickListener(this);

        }

        void bind_views(final c_Distress distress_user){

            global_distress = distress_user;

            SimpleDateFormat date_timeformat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            final String fullname = distress_user.getSos_user().getFn() + " " + distress_user.getSos_user().getLn();
            String state = distress_user.getSos_user().getState();

            try {
                String time = timeformat.format(date_timeformat.parse(distress_user.getSos_user().getDate_time()));
                tv_diss_time.setText(time);
            }catch (Exception ignored){}

            tv_diss_name.setText(fullname);

            if (state != null){

                switch (state){

                    case "N":
                        tv_diss_state.setTextColor(weak_mcontext.get().getResources().getColor(R.color.colorPrimary));
                        break;
                    case "I":
                        tv_diss_state.setTextColor(weak_mcontext.get().getResources().getColor(R.color.oaPrimary));
                        break;
                    case "S":
                        tv_diss_state.setTextColor(weak_mcontext.get().getResources().getColor(R.color.stc_green));

                }

                tv_diss_state.setText(state);
            }

            Location soslocation = new Location("SOS_USER");
            soslocation.setLatitude(distress_user.getSos_user().getE_latitude());
            soslocation.setLongitude(distress_user.getSos_user().getE_longitude());

            float distance = location.distanceTo(soslocation);
            float bearing = location.bearingTo(soslocation);

            distance_bearing(distance,bearing);

            bt_diss_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profile_intent(distress_user.getSos_user().getFire_id());
                }
            });

            bt_diss_direction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Uri gmmIntentUri= Uri.parse("google.navigation:q="+distress_user.getSos_user().getE_latitude()+","
                            +distress_user.getSos_user().getE_longitude());

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                    if (mapIntent.resolveActivity(weak_mcontext.get().getPackageManager()) != null) {
                        weak_mcontext.get().startActivity(mapIntent);
                    }else {
                        Toast.makeText(weak_mcontext.get(),"Map Application Not Found",Toast.LENGTH_LONG).show();
                    }

                }
            });

            bt_diss_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Uri gmmIntentUri = Uri.parse("geo:0,0?q="+distress_user.getSos_user().getE_latitude()
                            +","+distress_user.getSos_user().getE_longitude()+"("+fullname+")");

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                    if (mapIntent.resolveActivity(weak_mcontext.get().getPackageManager()) != null) {
                        weak_mcontext.get().startActivity(mapIntent);
                    }else {
                        Toast.makeText(weak_mcontext.get(),"Map Application Not Found",Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

        void profile_intent(String fire_id) {
            Intent profile_intent = new Intent(weak_mcontext.get(), UserProfile.class);
            profile_intent.putExtra(common.BUNDLE_KEY, fire_id);
            weak_mcontext.get().startActivity(profile_intent);
        }

        void change_state_online(c_Distress cDistress) throws ParseException {

            SimpleDateFormat date_timeformat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            SimpleDateFormat timeformat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

            DatabaseReference comp_ref = FirebaseDatabase.getInstance().getReference(EMERGENCY_REFERENCE)
                    .child(POLICE_EMERGENCY)
                    .child(timeformat.format(date_timeformat.parse(cDistress.getSos_user().getDate_time())))
                    .child(cDistress.getPushkey());
            comp_ref.setValue(cDistress.getSos_user());

            ((Dashboard) activity).get_crime_reports();

        }

        void distance_bearing(float distance, float bearing){

            DecimalFormat decimal_format = new DecimalFormat("####.####");

            //if distance is greater or equal to a kilometer
            if (distance>=1000){

                //append bearing by result
                if (bearing> 0 && bearing<90){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°NE";
                    tv_diss_body.setText(location_distance);

                }else if (bearing==90){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°E";
                    tv_diss_body.setText(location_distance);

                }else if (bearing>90 && bearing<180){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°SE";
                    tv_diss_body.setText(location_distance);

                }else if (bearing==180){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°S";
                    tv_diss_body.setText(location_distance);

                }else if (bearing>-90 && bearing<0){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°NW";
                    tv_diss_body.setText(location_distance);

                }else if (bearing==-90){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°W";
                    tv_diss_body.setText(location_distance);

                }else if (bearing>-180 && bearing<-90){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°SW";
                    tv_diss_body.setText(location_distance);

                }else if (bearing == -180){
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°S";
                    tv_diss_body.setText(location_distance);

                }else {
                    String location_distance = String.valueOf(decimal_format.format(distance/1000)) + "km Away, Bearing "
                            + String.valueOf(bearing)+ "°N";
                    tv_diss_body.setText(location_distance);

                }
            }else {
                if (bearing> 0 && bearing<90){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°NE";
                    tv_diss_body.setText(location_distance);

                }else if (bearing==90){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°E";
                    tv_diss_body.setText(location_distance);

                }else if (bearing>90 && bearing<180){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°SE";
                    tv_diss_body.setText(location_distance);

                }else if (bearing==180){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°S";
                    tv_diss_body.setText(location_distance);

                }else if (bearing>-90 && bearing<0){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°NW";
                    tv_diss_body.setText(location_distance);

                }else if (bearing==-90){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°W";
                    tv_diss_body.setText(location_distance);

                }else if (bearing>-180 && bearing<-90){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°SW";
                    tv_diss_body.setText(location_distance);

                }else if (bearing == -180){
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°S";
                    tv_diss_body.setText(location_distance);

                }else {
                    String location_distance = String.valueOf(distance) + "m Away, Bearing "
                            + String.valueOf(bearing)+ "°N";
                    tv_diss_body.setText(location_distance);
                }
            }
        }

        @Override
        public void onClick(View v) {

            PopupMenu popupMenu = new PopupMenu(weak_mcontext.get(),tv_diss_state);

            popupMenu.getMenuInflater().inflate(R.menu.action_menu,popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (global_distress.getSos_user() != null){

                        switch (item.getItemId()){

                            case R.id.action_menu_investigate:
                                global_distress.getSos_user().setState(common.STATE_INVESTIGATE);
                                try {
                                    change_state_online(global_distress);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case R.id.action_menu_solved:
                                global_distress.getSos_user().setState(common.STATE_SOLVED);
                                try {
                                    change_state_online(global_distress);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }

                    return true;
                }
            });
            popupMenu.show();
        }
    }

}
