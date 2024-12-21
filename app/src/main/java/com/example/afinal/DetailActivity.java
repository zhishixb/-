package com.example.afinal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.afinal.entity.Post;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 获取传递过来的Post对象
        Post post = getIntent().getParcelableExtra("post");

        if (post != null) {
            // 更新UI以展示Post的详细信息
            updateUIWithPostDetails(post);
        }
    }

    private void updateUIWithPostDetails(Post post) {
        // 假设你有一个TextView来显示帖子标题和其他信息
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView contentTextView = findViewById(R.id.contentTextView);
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView updatetimeTextView = findViewById(R.id.updatetimeTextView);

        titleTextView.setText(post.getTitle());
        contentTextView.setText(post.getContent());
        usernameTextView.setText("作者: " + post.getUsername());
        updatetimeTextView.setText("更新时间: " + post.getUpdatetime());
    }
}
