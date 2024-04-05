package com.example.udapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Record {
    private String fileName;
    private int duration;
    private String format;
    private String time;
    private LocalDateTime dateTime;

    public Record(String fileName) {
        if (!fileName.contains("/audio_")) {
            throw new IllegalArgumentException("Invalid fileName: " + fileName);
        }
        String[] parts = fileName.split("/audio_");
        this.fileName = parts[0];
        String[] timeAndDateTimeAndFormat = parts[1].split("\\.");
        this.format = timeAndDateTimeAndFormat[1];
        String[] timeAndDateTime = timeAndDateTimeAndFormat[0].split("_");
        String[] timeParts = timeAndDateTime[0].split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2]);
        this.duration = hours * 3600 + minutes * 60 + seconds;
        this.time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        this.dateTime = LocalDateTime.parse(timeAndDateTime[1], dtf);
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

    public String getTime() {
        return time;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}