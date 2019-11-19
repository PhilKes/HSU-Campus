package me.phil.madcampus.model;

import java.util.Calendar;

public class Event {
    public String description, title;
    public int _id;
    public Calendar start;
    public Calendar end;
    public boolean today;
    public Event(int _id, String description, String title,Long start,Long end) {
        this._id = _id;
        this.description = description;
        this.title = title;
        this.start= Calendar.getInstance();
        this.start.setTimeInMillis(start);
        this.end= Calendar.getInstance();
        this.end.setTimeInMillis(end);
        this.today=false;
    }
}
