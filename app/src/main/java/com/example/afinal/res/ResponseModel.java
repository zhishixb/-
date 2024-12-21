package com.example.afinal.res;
//解析返回的json

import android.util.Log;

import com.example.afinal.entity.Post;

import java.util.List;

public class ResponseModel {
    private int code;
    private String message;
    private List<Post> data;

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Post> getData() {
        Log.d("Posts", data.toString());
        return data;
    }

    public void setData(List<Post> data) {
        this.data = data;
    }
}
