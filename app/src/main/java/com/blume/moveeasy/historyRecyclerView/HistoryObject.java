package com.blume.moveeasy.historyRecyclerView;

public class HistoryObject {

    private String rideId;
    private String time;
    private String pickup;
    private String destination;

    public HistoryObject(String rideID, String time, String pickup, String destination){
        this.rideId = rideID;
        this.time = time;
        this.pickup = pickup;
        this.destination = destination;

    }

    public String getRideId(){
        return rideId;
    }
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }

    public String getPickup(){
        return pickup;
    }
    public String getDestination(){
        return destination;
    }

}
