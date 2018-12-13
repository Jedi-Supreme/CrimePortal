package com.jedi_supreme.crimeportal;

import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.Calendar;

public class common {

    public static final String EMERGENCY_REFERENCE = "Emergencies";
    public static final String POLICE_EMERGENCY = "crime";

    public static final String BUNDLE_KEY = "bundle_key";
    public static final String USERS_PROFILE_REF = "User_Profiles";

    public static final String STATE_SOLVED = "S";
    public static final String STATE_INVESTIGATE = "I";

    public static final String REQUEST_BACKUP = "Backup_Request";

    public static String default_pass(){

        String pass;
        Calendar calendar = Calendar.getInstance();

        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH)+1);

        pass = "gh"+month+"_enforcer@"+year;
        return pass;
    }

    public static Snackbar snackbar(View parent, String message){

        final Snackbar snackbar = Snackbar.make(parent,message,Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(parent.getContext().getResources().getColor(R.color.colorPrimary));
        snackbar.setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        return snackbar;
    }
}
