package com.example.udapp;

public class RecordFirebase {
    private String username;
    private String downloadUrl;
    private String recordId;
    private String filename;
    private int duration;
    private String uploadDate;

    public RecordFirebase() {
    }

    public RecordFirebase(String recordId, String username, String downloadUrl, String filename, int duration, String uploadDate) {
        this.recordId = recordId;
        this.username = username;
        this.downloadUrl = downloadUrl;
        this.filename = filename;
        this.duration = duration;
        this.uploadDate = uploadDate;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getDownloadUrl() {
        return downloadUrl;
    }
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    public String getRecordId() {
        return recordId;
    }
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
    public String getFileName() {
        return filename;
    }
    public void setFileName(String filename) {
        this.filename = filename;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public String getUploadDate() {
        return uploadDate;
    }
    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

}
