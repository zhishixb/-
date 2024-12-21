package com.example.afinal.res;

import com.example.afinal.entity.Post;
import com.google.gson.annotations.SerializedName;

public class PostResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Post data;

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

    public Post getData() {
        return data;
    }

    public void setData(Post data) {
        this.data = data;
    }

    // 用于显示成功的消息
    public boolean isSuccess() {
        return code == 200 && "成功".equals(message);
    }
}