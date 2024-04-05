package com.example.udapp;

public class Record {
    private String fileName;
    private int duration;

    public Record(String fileName, int duration) {
        this.fileName = fileName;
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public int getDuration() {
        return duration;
    }
}