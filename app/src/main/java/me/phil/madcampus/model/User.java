package me.phil.madcampus.model;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{
    public String name;
    public String passwd;
    public String studies;
    public String avatar;

    public User(String name, String passwd, String studies,String avatar) {
        this.name = name;
        this.passwd = passwd;
        this.studies=studies;
        this.avatar=avatar;
    }

    protected User(Parcel in) {
        name = in.readString();
        passwd = in.readString();
        studies = in.readString();
        avatar=in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    @Override
    public String toString() {
        return "Name: "+name+ " , Password: "+passwd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(passwd);
        parcel.writeString(studies);
        parcel.writeString(avatar);
    }
}