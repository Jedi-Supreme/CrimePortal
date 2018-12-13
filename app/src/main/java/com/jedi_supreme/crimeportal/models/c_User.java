package com.jedi_supreme.crimeportal.models;

import java.util.ArrayList;

public class c_User {

    public static final String DISTRESS_TABLE = "DISTRESS";
    public static final String TABLE = "USER";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIRE_ID = "fire_id";
    public static final String COLUMN_FN = "firstname";
    public static final String COLUMN_LN = "lastname";
    public static final String COLUMN_MOBILE = "mobile_number";
    public static final String COLUMN_HOME_ADD = "home_address";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_EMERGENCY_KEY = "relation_key";

    public static final String COLUMN_PUSHKEY = "firebase_pushkey";
    public static final String COLUMN_ADD_DATE = "date_added";
    public static final String COLUMN_DISTRESS_STATE = "state";

    public static final String COLUMN_REL_NUMBER = "emergency_contact";

    private String fn;
    private String ln;
    private String mobile;
    private String fire_id;
    private String date_time;
    private ArrayList<String>ice_contacts_list;
    private String homeAddress;
    private double home_latitude;
    private double home_longitude;
    private double e_latitude;
    private double e_longitude;
    private String state;

    public c_User() {
    }

    //enforcer request backup
    public c_User(String fire_id, double e_latitude, double e_longitude) {
        this.fire_id = fire_id;
        this.e_latitude = e_latitude;
        this.e_longitude = e_longitude;
    }

    //sos_user constructor
    public c_User(
            String fire_id,String fn, String ln, String date_time,
            String state, double e_latitude, double e_longitude) {
        this.state = state;
        this.fire_id = fire_id;
        this.fn = fn;
        this.ln = ln;
        this.date_time = date_time;
        this.e_latitude = e_latitude;
        this.e_longitude = e_longitude;
    }

    //firebase constructor
    public c_User(
            String fire_id, String fn, String ln,
            String mobile, String homeAddress,
            double home_latitude, double home_longitude,
            ArrayList<String> ice_contacts_list) {
        this.fire_id = fire_id;
        this.fn = fn;
        this.ln = ln;
        this.mobile = mobile;
        this.homeAddress = homeAddress;
        this.ice_contacts_list = ice_contacts_list;
        this.home_latitude = home_latitude;
        this.home_longitude = home_longitude;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public String getLn() {
        return ln;
    }

    public void setLn(String ln) {
        this.ln = ln;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public double getHome_latitude() {
        return home_latitude;
    }

    public void setHome_latitude(double home_latitude) {
        this.home_latitude = home_latitude;
    }

    public double getHome_longitude() {
        return home_longitude;
    }

    public void setHome_longitude(double home_longitude) {
        this.home_longitude = home_longitude;
    }

    public ArrayList<String> getIce_contacts_list() {
        return ice_contacts_list;
    }

    public void setIce_contacts_list(ArrayList<String> ice_contacts_list) {
        this.ice_contacts_list = ice_contacts_list;
    }

    public double getE_longitude() {
        return e_longitude;
    }

    public void setE_longitude(double e_longitude) {
        this.e_longitude = e_longitude;
    }

    public double getE_latitude() {
        return e_latitude;
    }

    public void setE_latitude(double e_latitude) {
        this.e_latitude = e_latitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getFire_id() {
        return fire_id;
    }

    public void setFire_id(String fire_id) {
        this.fire_id = fire_id;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }
}
