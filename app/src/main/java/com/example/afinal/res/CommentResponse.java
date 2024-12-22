package com.example.afinal.res;
//用于处理comment的信息返回

import com.example.afinal.entity.Comment;

import java.util.List;

public class CommentResponse {
    private int code;
    private String message;
    private List<Comment> data;

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

    public List<Comment> getData() {
        return data;
    }

    public void setData(List<Comment> data) {
        this.data = data;
    }
}
