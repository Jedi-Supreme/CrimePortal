package com.jedi_supreme.crimeportal.models;

public class c_Distress {

    private String pushkey;
    private c_User sos_user;

    public c_Distress(String pushkey, c_User sos_user) {
        this.pushkey = pushkey;
        this.sos_user = sos_user;
    }

    public String getPushkey() {
        return pushkey;
    }

    public void setPushkey(String pushkey) {
        this.pushkey = pushkey;
    }

    public c_User getSos_user() {
        return sos_user;
    }

    public void setSos_user(c_User sos_user) {
        this.sos_user = sos_user;
    }
}
