package com.example.afinal.application;

import android.app.Application;

import com.example.afinal.entity.User;

public class MyApplication extends Application {
    private User currentUser; //用于用户的信息显示

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
