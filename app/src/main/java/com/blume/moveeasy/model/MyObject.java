package com.blume.moveeasy.model;

public class MyObject {
    String Vehicle;
    String Price;

    public MyObject(String veh, String price){
        this.Vehicle = veh;
        this.Price = price;
    }

    public String getVehicle(){
        return Vehicle;
    }
    public String getPrice(){
        return Price;
    }
}
