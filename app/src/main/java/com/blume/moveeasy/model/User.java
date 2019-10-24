package com.blume.moveeasy.model;


public class User {
    public String uid, email, uname;
    public int phone;

    public User(){

    }

    public User(String userid, String email, String uname, int phone){
        this.uid = userid;
        this.uname = uname;
        this.email = email;
        this.phone = phone;


    }
    public String get_uid(){
        return uid;
    }
    public void set_uid(String uid){
        this.uid = uid;
    }
    public String get_email(){
        return email;
    }
    public void set_email(String email){
        this.email = email;
    }
    public String get_uname(){
        return uname;
    }
    public void set_uname(String uname){
        this.uname = uname;
    }
    public int get_phone(){
        return phone;
    }
    public void set_phone(int phone){
        this.phone = phone;
    }

}