package com.example.udapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Record {
    private String fileName;
    private String filePath;
    private String duration;
    private String format;
    private String time;
    private String lastModified;
    private LocalDateTime dateTime;

    public Record(String fileName, String duration, long lastModified) {
        this.filePath = fileName;
        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        //fileName+= "/audio"+ dtf.format(now)+"." + audioFormat;

        //Seperate the file name from the path
        this.fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        this.duration = duration;
        this.format = fileName.substring(fileName.lastIndexOf('.') + 1);
        //get time by split dtf.format(now)
        this.time = fileName.substring(fileName.lastIndexOf('/') + 6, fileName.lastIndexOf('.'));
        this.lastModified = String.valueOf(lastModified);

    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDuration() {
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