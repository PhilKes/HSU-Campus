package me.phil.madcampus.model;

public class Lesson {
    public String courseShort,courseLong,room,prof;
    public long _id=-1;
    public Lesson(String course,String courseLong,String room,String prof) {
        this.courseShort = course;
        this.courseLong=courseLong;
        this.room=room;
        this.prof=prof;
    }
    public void setId(long id){
        _id=id;
    }

}
