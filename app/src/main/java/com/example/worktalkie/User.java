package com.example.worktalkie;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String uuid;
    private String username;
    private String profileUrl;
    private String password;
    private String token;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    private boolean online;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }



    public User(){

    }

    public User(String uuid, String username, String profileUrl, String password) {
        this.uuid = uuid;
        this.username = username;
        this.profileUrl = profileUrl;
        this.password = password;
    }

    protected User(Parcel in) {
        uuid = in.readString();
        username = in.readString();
        profileUrl = in.readString();
        password = in.readString();
        token = in.readString();
        online = in.readInt() == 1;
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

    public String getPassword() {
        return password;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uuid);
        parcel.writeString(username);
        parcel.writeString(profileUrl);
        parcel.writeString(password);
        parcel.writeString(token);
        parcel.writeInt(online ? 1 : 0);
    }
}
