package com.example.afinal.entity;

import java.util.Objects;

public class Comment {

    private int id;
    private int postId;
    private String commenterName;
    private String content; // 注意这里修正了属性名的大小写
    private String createTime;

    // 无参构造函数
    public Comment() {
    }

    // 全参构造函数
    public Comment(int id, int postId, String commenterName, String content, String createTime) {
        this.id = id;
        this.postId = postId;
        this.commenterName = commenterName;
        this.content = content;
        this.createTime = createTime;
    }

    // Getter 和 Setter 方法

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id == comment.id &&
                postId == comment.postId &&
                Objects.equals(commenterName, comment.commenterName) &&
                Objects.equals(content, comment.content) &&
                Objects.equals(createTime, comment.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, postId, commenterName, content, createTime);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", postId=" + postId +
                ", commenterName='" + commenterName + '\'' +
                ", content='" + content + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}