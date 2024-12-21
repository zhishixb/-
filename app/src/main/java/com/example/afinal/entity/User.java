package com.example.afinal.entity;
/**
 * 为了体现sqlite的使用，将用户名和头像id存入本地
 * 在用户注册时不会实现新user的插入，当用户首次登录时，舒适化一个用户对象
 */

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull //主键非空
    private String user_name;
    private String nick_name;
    private int avatar; // 更改为int类型，默认值为0

    //为使用Room添加无参构造函数
    public User() {}

    public User(String username) {
        this.user_name = username;
        this.nick_name = "新用户" + username; // 设置默认昵称
        this.avatar = 0; // 设置默认头像ID
    }

    // Getter and Setter methods
    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}