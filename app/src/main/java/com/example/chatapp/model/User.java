package com.example.chatapp.model;

import java.io.Serializable;

public class User implements Serializable {
    String name;
    String ProfileImage;
    String id="";
    String token;

    public String getProfileImage() {
        return ProfileImage;
    }

    public User() {
        name = "";
        ProfileImage = "";
        token="";
    }

    public User(String name, String profileImage,String token) {
        this.name = name;
        ProfileImage = profileImage;
        this.token=token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getName() {
        return name;
    }
}
