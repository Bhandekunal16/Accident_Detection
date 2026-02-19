package com.example.accidentdetection;

public class PlaceModel {
    private String name;
    private String type;
    private double lat;
    private double lng;

    public PlaceModel() {
    }

    public PlaceModel(String name, String type, double lat, double lng) {
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}

