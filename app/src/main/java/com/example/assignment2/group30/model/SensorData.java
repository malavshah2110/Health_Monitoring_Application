package com.example.assignment2.group30.model;

/**
 * Created by Dishank on 3/5/2018.
 */

public class SensorData {

    private String timestamp;
    private float xValue;
    private float yValue;
    private float zValue;


    public SensorData(){

    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getxValue() {
        return xValue;
    }

    public void setxValue(float xValue) {
        this.xValue = xValue;
    }

    public float getyValue() {
        return yValue;
    }

    public void setyValue(float yValue) {
        this.yValue = yValue;
    }

    public float getzValue() {
        return zValue;
    }

    public void setzValue(float zValue) {
        this.zValue = zValue;
    }
}
