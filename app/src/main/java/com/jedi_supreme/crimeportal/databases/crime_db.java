package com.jedi_supreme.crimeportal.databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.jedi_supreme.crimeportal.models.c_Distress;
import com.jedi_supreme.crimeportal.models.c_User;

import java.util.ArrayList;

public class crime_db extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "crime.db";
    private static final int DATABASE_VERSION = 1;

    public crime_db(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String distress_query = "CREATE TABLE IF NOT EXISTS " + c_User.DISTRESS_TABLE + " ( "

                + c_User.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + c_User.COLUMN_PUSHKEY + " TEXT UNIQUE NOT NULL, "
                + c_User.COLUMN_ADD_DATE + " TEXT, "
                + c_User.COLUMN_DISTRESS_STATE + " TEXT, "

                //==================DISTRESS USER OBJECTS==============
                + c_User.COLUMN_FIRE_ID + " TEXT, "
                + c_User.COLUMN_FN + " TEXT, "
                + c_User.COLUMN_LN + " TEXT, "
                + c_User.COLUMN_LATITUDE + " INTEGER, "
                + c_User.COLUMN_LONGITUDE + " INTEGER );";
                //==================DISTRESS USER OBJECTS==============

                 db.execSQL(distress_query);

        String user_query = "CREATE TABLE IF NOT EXISTS " + c_User.TABLE + " ( "

                + c_User.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

                //=========================USER OBJECT================
                + c_User.COLUMN_FIRE_ID + " TEXT UNIQUE NOT NULL, "
                + c_User.COLUMN_FN + " TEXT, "
                + c_User.COLUMN_LN + " TEXT, "
                + c_User.COLUMN_MOBILE + " TEXT, "
                + c_User.COLUMN_HOME_ADD + " TEXT, "
                + c_User.COLUMN_LATITUDE + " INTEGER, "
                + c_User.COLUMN_LONGITUDE + " INTEGER, "
                //=========================USER OBJECT================

                + c_User.COLUMN_EMERGENCY_KEY + " TEXT); ";

        db.execSQL(user_query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + c_User.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + c_User.DISTRESS_TABLE);
        onCreate(db);

    }

    //--------------------------------------------METHODS-------------------------------------------

    // add distress user to distress table ->
    // use fire_id to fetch user data online ->
    // add user_data to users_table ->
    // create relations table ->
    // insert emergency contact

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=CALLED EXTERNALLY-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public void add_distress(c_Distress cDistress){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues distress_values = new ContentValues();

        distress_values.put(c_User.COLUMN_PUSHKEY,cDistress.getPushkey());
        distress_values.put(c_User.COLUMN_ADD_DATE,cDistress.getSos_user().getDate_time());

        distress_values.put(c_User.COLUMN_FIRE_ID,cDistress.getSos_user().getFire_id());
        distress_values.put(c_User.COLUMN_FN,cDistress.getSos_user().getFn());
        distress_values.put(c_User.COLUMN_LN,cDistress.getSos_user().getLn());
        distress_values.put(c_User.COLUMN_DISTRESS_STATE,cDistress.getSos_user().getState());
        distress_values.put(c_User.COLUMN_LATITUDE,cDistress.getSos_user().getE_latitude());
        distress_values.put(c_User.COLUMN_LONGITUDE,cDistress.getSos_user().getE_longitude());

        try{
            db.insertOrThrow(c_User.DISTRESS_TABLE,null,distress_values);
        }catch (SQLiteConstraintException ignored){
            update_distress_state(cDistress);
        }
    }

    private void update_distress_state(c_Distress cDistress){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues up_values = new ContentValues();
        up_values.put(c_User.COLUMN_DISTRESS_STATE,cDistress.getSos_user().getState());

        db.update(c_User.DISTRESS_TABLE,up_values,c_User.COLUMN_PUSHKEY + " =? ",new String[]{cDistress.getPushkey()});
    }

    public c_User find_fulluser_profile(String fire_id){

        //use mobile number to find user from user table and use relation key from that table to find emergency contacts
        // then build and return full user profile

        SQLiteDatabase sqDB = getReadableDatabase();
        ArrayList<String>contact_list = new ArrayList<>();

        Cursor user_cursor = user_cursor(fire_id);
        user_cursor.moveToFirst();
        if (user_cursor.getCount() > 0){
            String rel_key = user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_EMERGENCY_KEY));

            char[] upkey_arr = rel_key.toCharArray();

            for (char ck : upkey_arr){
                if (!decode_key(ck).equals("")){
                    @SuppressLint("Recycle") Cursor rel_curs = sqDB.rawQuery("SELECT * FROM " + decode_key(ck) + " WHERE "
                            + c_User.COLUMN_FIRE_ID + " = \"" + fire_id + "\"",null );
                    rel_curs.moveToFirst();
                    contact_list.add(decode_key(ck)+"-"+rel_curs.getString(rel_curs.getColumnIndexOrThrow(c_User.COLUMN_REL_NUMBER)));
                }
            }

            return new c_User(
                    user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_FIRE_ID)),
                    user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_FN)),
                    user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_LN)),
                    user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_MOBILE)),
                    user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_HOME_ADD)),
                    user_cursor.getDouble(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_LATITUDE)),
                    user_cursor.getDouble(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_LONGITUDE)),
                    contact_list);
        }else {
            return null;
        }

    }

    //try to add new user profile if and emergency contact catch error and update instead
    public void add_user_profile(c_User user){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues user_values = new ContentValues();

        user_values.put(c_User.COLUMN_FIRE_ID,user.getFire_id());
        user_values.put(c_User.COLUMN_FN,user.getFn());
        user_values.put(c_User.COLUMN_LN,user.getLn());
        user_values.put(c_User.COLUMN_MOBILE,user.getMobile());
        user_values.put(c_User.COLUMN_HOME_ADD,user.getHomeAddress());
        user_values.put(c_User.COLUMN_LATITUDE,user.getHome_latitude());
        user_values.put(c_User.COLUMN_LONGITUDE,user.getHome_longitude());

        //add relations key to user table
        user_values.put(c_User.COLUMN_EMERGENCY_KEY,build_key(user.getIce_contacts_list()));

        //insert relation into relation table
        for (String rel_numb : user.getIce_contacts_list()){
            insert_emergency_contacts(user.getFire_id(),rel_numb);
        }

        try {
            db.insertOrThrow(c_User.TABLE,null,user_values);
        }catch (SQLiteConstraintException constraint_exception){
            update_user_profile(user);
        }

    }

    //list of sos_users
    public ArrayList<c_Distress> distress_users_list(){

        ArrayList<c_Distress> users_list = new ArrayList<>();

        SQLiteDatabase sqdb = getReadableDatabase();

        String sos_users_query = " SELECT * FROM " + c_User.DISTRESS_TABLE + " ORDER BY " + c_User.COLUMN_ADD_DATE + " DESC";

        Cursor c = sqdb.rawQuery(sos_users_query,null);

        while (c.moveToNext()){

            c_User sosUser = new c_User(
                    c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_FIRE_ID)),
                    c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_FN)),
                    c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_LN)),
                    c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_ADD_DATE)),
                    c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_DISTRESS_STATE)),
                    c.getDouble(c.getColumnIndexOrThrow(c_User.COLUMN_LATITUDE)),
                    c.getDouble(c.getColumnIndexOrThrow(c_User.COLUMN_LONGITUDE)));

            c_Distress distress_user = new c_Distress(c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_PUSHKEY)),sosUser);

            //add sos user to list
            users_list.add(distress_user);

        }

        c.close();

        //return list of users
        return users_list;
    }
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=CALLED EXTERNALLY-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

    //update user profile and emergency contact
    private void update_user_profile(c_User user){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues user_values = new ContentValues();

        user_values.put(c_User.COLUMN_FIRE_ID,user.getFire_id());
        user_values.put(c_User.COLUMN_FN,user.getFn());
        user_values.put(c_User.COLUMN_LN,user.getLn());
        user_values.put(c_User.COLUMN_MOBILE,user.getMobile());
        user_values.put(c_User.COLUMN_HOME_ADD,user.getHomeAddress());
        user_values.put(c_User.COLUMN_LATITUDE,user.getHome_latitude());
        user_values.put(c_User.COLUMN_LONGITUDE,user.getHome_longitude());

        db.update(c_User.TABLE,user_values,c_User.COLUMN_FIRE_ID + " =? ",new String[]{user.getFire_id()});
        emergency_contact(user);

    }

    //determine if emergency key needs update or replacing
    private void emergency_contact(c_User user){

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = user_cursor(user.getFire_id());
        c.moveToFirst();

        String oldkey = c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_EMERGENCY_KEY));

        //if oldkey equals newkey update directly
        if (oldkey.equals(build_key(user.getIce_contacts_list()))){

            for (String rel_numb : user.getIce_contacts_list()){
                update_ice_contact(user.getFire_id(),rel_numb);
            }

        }else {

            //delete all old entries and add new entries
            char[] oldkey_arr = oldkey.toCharArray();

            for (char ck : oldkey_arr){
                if (!decode_key(ck).equals("")){
                    db.execSQL("DELETE FROM " + decode_key(ck) + " WHERE " + c_User.COLUMN_MOBILE
                            + " = \"" + user.getMobile() + "\"" );
                }
            }

            for (String rel_numb : user.getIce_contacts_list()){
                insert_emergency_contacts(user.getMobile(),rel_numb);
            }
        }

    }

    // insert new emergency contact, create new table if not exists
    private void insert_emergency_contacts(String fire_id, String rel_number){

        SQLiteDatabase db = getWritableDatabase();

        String[]rel_arr = rel_number.split("-");
        ContentValues e_values = new ContentValues();

        e_values.put(c_User.COLUMN_FIRE_ID,fire_id);
        e_values.put(c_User.COLUMN_REL_NUMBER,rel_arr[1]);


       try {
           db.insertOrThrow(rel_arr[0],null,e_values);
       }catch (SQLiteException table_exception){
           create_relation_table(rel_arr[0]);
           db.insertOrThrow(rel_arr[0],null,e_values);
       }
    }

    //called from emergency contact method to update emergency contact
    private void update_ice_contact(String user_fire_id, String rel_number){

        SQLiteDatabase db = getWritableDatabase();

        String[]rel_arr = rel_number.split("-");
        ContentValues e_values = new ContentValues();

        e_values.put(c_User.COLUMN_FIRE_ID,user_fire_id);
        e_values.put(c_User.COLUMN_REL_NUMBER,rel_arr[1]);

        db.update(rel_arr[0],e_values,c_User.COLUMN_FIRE_ID + " =? ",new String[]{user_fire_id});
    }

    //create relations table
    private void create_relation_table(String relation){

        SQLiteDatabase db = getWritableDatabase();

        String query = " CREATE TABLE IF NOT EXISTS " + relation + " ( "
                + c_User.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + c_User.COLUMN_FIRE_ID + " TEXT, "
                + c_User.COLUMN_REL_NUMBER + " TEXT );";

        db.execSQL(query);
    }

    //convert emergency key to relation
    private String decode_key(char key){

        String relation = "";

        switch (key){

            case 'M':
                relation = "Mother";
                break;

            case 'D':
                relation = "Father";
                break;

            case 'G':
                relation = "Guardian";
                break;

            case 'B':
                relation = "Brother";
                break;

            case 'S':
                relation = "Sister";
                break;

            case 'F':
                relation = "Friend";
                break;

            case 'X':
                relation = "Ext. Family";
                break;
        }

        return relation;
    }

    //convert relation to emergency key
    private String build_key(ArrayList<String> contacts_list) {

        StringBuilder key_builder = new StringBuilder();

        for (String emergency_contact : contacts_list) {

            String arr[] = emergency_contact.split("-");

            String relation = arr[0];

            switch (relation) {

                case "Mother":
                    key_builder.append("M");
                    break;

                case "Father":
                    key_builder.append("D");
                    break;

                case "Guardian":
                    key_builder.append("G");
                    break;

                case "Brother":
                    key_builder.append("B");
                    break;

                case "Sister":
                    key_builder.append("S");
                    break;

                case "Friend":
                    key_builder.append("F");
                    break;

                case "Ext. Family":
                    key_builder.append("X");
                    break;

            }

        }

        return key_builder.toString();
    }

    //return user cursor, find by fire_id for emergency contact method
    private Cursor user_cursor(String fire_id){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + c_User.TABLE + " WHERE "
                + c_User.COLUMN_FIRE_ID + " = \"" + fire_id + "\"";
        return db.rawQuery(query,null);
    }

    //--------------------------------------------METHODS-------------------------------------------

}
