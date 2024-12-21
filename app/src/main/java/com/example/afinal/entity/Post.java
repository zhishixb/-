package com.example.afinal.entity;
//使用viewpage是我能想到的最好的
//为了实现post的数据传输，添加Parcelable

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Post implements Parcelable {
    private String title;
    @SerializedName("username")
    private String userName;
    private String content;
    @SerializedName("id")
    private int id;
    @SerializedName("updatetime")
    private String updatetime;

    // 构造函数，不包括 id，因为它是服务器生成的
    public Post(String title, String userName, String content) {
        this.title = title;
        this.userName = userName;
        this.content = content;
    }

    // 从 Parcel 中读取数据的构造函数
    protected Post(Parcel in) {
        title = in.readString();
        userName = in.readString();
        content = in.readString();
        id = in.readInt();
        updatetime = in.readString();
    }

    // Getter 和 Setter 方法
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return userName;
    }

    // 注意这里应该是 setUserName 而不是 setUserId
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", username='" + userName + '\'' +
                ", content='" + content + '\'' +
                ", updatetime='" + updatetime + '\'' +
                '}';
    }

    // 必须实现的方法
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(userName);
        dest.writeString(content);
        dest.writeInt(id);
        dest.writeString(updatetime);
    }

    @Override
    public int describeContents() {
        return 0; // 默认返回 0 即可
    }

    // CREATOR 实现
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
}