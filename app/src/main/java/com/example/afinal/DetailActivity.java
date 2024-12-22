package com.example.afinal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.afinal.adapter.CommentAdapter;
import com.example.afinal.dao.UserDao;
import com.example.afinal.database.AppDatabase;
import com.example.afinal.entity.Comment;
import com.example.afinal.entity.Post;
import com.example.afinal.res.CommentResponse;
import com.example.afinal.res.ResponseModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8099/common/download?name=";
    private static final String BASE_AVATAR_URL = "http://10.0.2.2:8099/avatar/download?name=";
    private static final String BASE_COMMENT_URL = "http://10.0.2.2:8099/comment/get/";
    private AppDatabase db;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 获取传递过来的Post对象
        Post post = getIntent().getParcelableExtra("post");

        loadComments(post.getId());

        db = AppDatabase.getInstance(getApplicationContext());

        if (post != null) {
            // 更新UI以展示Post的详细信息
            updateUIWithPostDetails(post);
        }
    }

    private void updateUIWithPostDetails(Post post) {
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView contentTextView = findViewById(R.id.contentTextView);
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView updatetimeTextView = findViewById(R.id.updatetimeTextView);
        ImageView avatarImageView = findViewById(R.id.avatarImageView);
        ImageView imageView = findViewById(R.id.imageView);

        titleTextView.setText(post.getTitle());
        contentTextView.setText(post.getContent());

        // 修改时间格式
        try {
            // 使用 ISO 8601 预定义格式化器来解析时间字符串
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            LocalDateTime dateTime = LocalDateTime.parse(post.getUpdatetime(), inputFormatter);
            String formattedTime = dateTime.format(outputFormatter);
            updatetimeTextView.setText(formattedTime);
        } catch (Exception e) {
            Log.e("DetailActivity", "Error parsing time string: " + post.getUpdatetime(), e);
            updatetimeTextView.setText("更新时间: 格式错误");
        }
        // 设置默认用户名文本
        usernameTextView.setText("加载中...");

        // 异步获取昵称和头像ID
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                UserDao userDao = db.userDao();

                Integer avatarId = userDao.getAvatar(post.getUsername());
                String nickname = userDao.getNickNameByUsername(post.getUsername());

                runOnUiThread(() -> {
                    if (avatarId != null) {
                        String avatarUrl = BASE_AVATAR_URL + Integer.toString(avatarId);
                        Glide.with(this)
                                .load(avatarUrl)
                                .skipMemoryCache(true) // 禁用内存缓存
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                .into(avatarImageView);
                    } else {
                        Log.w("DetailActivity", "Failed to retrieve avatar for username: " + post.getUsername());
                    }

                    String imageUrl = BASE_IMAGE_URL + post.getId();
                    Glide.with(this)
                            .load(imageUrl)
                            .skipMemoryCache(true) // 禁用内存缓存
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                            .into(imageView);

                    if (nickname != null && !nickname.isEmpty()) {
                        usernameTextView.setText(nickname);
                        usernameTextView.setTextColor(getResources().getColor(android.R.color.black));
                    } else {
                        usernameTextView.setText("作者: 未知用户");
                        usernameTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        Log.w("DetailActivity", "Failed to retrieve nickname for username: " + post.getUsername());
                    }
                });
            } catch (Exception e) {
                Log.e("DetailActivity", "Error retrieving user info", e);
            }
        });
    }
    private void loadComments(int postId) {
        OkHttpClient client = new OkHttpClient();
        String commentUrl = BASE_COMMENT_URL + postId;
        Log.d("DetailActivity", "Fetching comments from URL: " + commentUrl);

        Request request = new Request.Builder()
                .url(commentUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("DetailActivity", "Failed to load comments", e);
                runOnUiThread(() -> {
                    // 显示错误信息给用户，比如 Toast 或 Snackbar
                });
            }

            @Override //帖子内容的获取
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    //请求返回内容字符化
                    String responseData = response.body().string();
                    //字符串json化，Gson形成java类,json的信息注入结果处理类
                    CommentResponse commentResponse = gson.fromJson(responseData, CommentResponse.class);
                    //判断请求返回不为空。code是后端的返回状态封装码，代表请求处理成功
                    if (commentResponse != null && commentResponse.getCode() == 200) {
                        final List<Comment> comments = commentResponse.getData();
                        if (comments == null || comments.isEmpty()) {
                            showNoComments();
                        } else {
                            setupRecyclerView(comments);
                        }
                    }
                }
            }
        });
    }
    //控制组件显隐性实现评论是否为空的播报
    private void showNoComments() {
        RecyclerView recyclerViewComments = findViewById(R.id.recyclerViewComments);
        TextView noCommentsTextView = findViewById(R.id.noCommentsTextView);

        recyclerViewComments.setVisibility(View.GONE);
        noCommentsTextView.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView(List<Comment> comments) {
        RecyclerView recyclerViewComments = findViewById(R.id.recyclerViewComments);
        TextView noCommentsTextView = findViewById(R.id.noCommentsTextView);

        recyclerViewComments.setVisibility(View.VISIBLE);
        noCommentsTextView.setVisibility(View.GONE);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        CommentAdapter adapter = new CommentAdapter(this, comments);
        recyclerViewComments.setAdapter(adapter);
    }
}
