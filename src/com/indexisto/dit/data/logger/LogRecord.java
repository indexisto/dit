package com.indexisto.dit.data.logger;

import java.util.Date;

public class LogRecord {

    private String message;
    private Date time;

    public LogRecord() {
        this("");
    }

    public LogRecord(String message) {
        this(new Date(), message);
    }

    public LogRecord(Date time, String message) {
        this.time = time;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}