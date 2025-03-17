package com.example.pathfinder;

import com.yandex.mapkit.geometry.Point;

import java.io.Serializable;

public class Marker implements Serializable {
    private String name;
    private String description;
    private double latitude;
    private double longitude;
    private String ownerid;

    public Marker(String name, String description, double latitude, double longitude, String ownerid){

        this.name=name;
        this.description=description;
        this.latitude=latitude;
        this.longitude=longitude;
        this.ownerid=ownerid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Point getCords() {
        return new Point(latitude, longitude);
    }

    public String getOwner() {return ownerid; }

    public void setFlagResource(Point geometryPoint) {
        this.latitude = geometryPoint.getLatitude();
        this.longitude = geometryPoint.getLongitude();
    }
}
