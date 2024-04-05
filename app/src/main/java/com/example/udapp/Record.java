package com.example.udapp;

public class Record {
    private String fileName;
    private int duration;
    private String format;

    public Record(String fileName) {
        String[] parts = fileName.split("audio");
        this.fileName = parts[0];
        String[] timeAndFormat = parts[1].split("\\.");
        this.format = timeAndFormat[1];
        String[] timeParts = timeAndFormat[0].split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2]);
        this.duration = hours * 3600 + minutes * 60 + seconds;
    }

    public String getFileName() {
        return fileName;
    }

    public int getDuration() {
        return duration;
    }

    public String getFormat() {
        return format;
    }
}