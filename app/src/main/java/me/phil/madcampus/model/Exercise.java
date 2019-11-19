package me.phil.madcampus.model;

import java.util.Date;

public class Exercise {
    public long _id;
    public String name;
    public Date dueDate;
    public int number;
    public String lessonName;
    public String lessonShort;

    public Exercise(long _id, String name, Date dueDate, int number,String lesson,String lessonShort) {
        this._id = _id;
        this.name = name;
        this.dueDate = dueDate;
        this.number = number;
        this.lessonName=lesson;
        this.lessonShort=lessonShort;
    }
}
