package com.example.afinal;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.afinal.adapter.CommentAdapter;
import com.example.afinal.application.MyApplication;
import com.example.afinal.dao.UserDao;
import com.example.afinal.database.AppDatabase;
import com.example.afinal.entity.Comment;
import com.example.afinal.entity.Post;
import com.example.afinal.entity.User;
import com.example.afinal.res.CommentResponse;
import com.example.afinal.res.ResponseModel;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8099/common/download?name=";
    private static final String BASE_AVATAR_URL = "http://10.0.2.2:8099/avatar/download?name=";
    private static final String BASE_COMMENT_URL = "http://10.0.2.2:8099/comment/get/";
    private static final String BASE_SEND_URL = "http://10.0.2.2:8099/comment/add";
    private TextView titleTextView;
    private TextView contentTextView ;
    private TextView usernameTextView;
    private TextView updatetimeTextView;
    private ImageView avatarImageView;
    private ImageView imageView;
    private EditText commentEditText;
    private Button sendButton;
    private RecyclerView recyclerViewComments;
    private TextView noCommentsTextView;
    private CommentAdapter adapter;
    private AppDatabase db;
    private MyApplication myApp;
    private Gson gson = new Gson();
    private OkHttpClient client = new OkHttpClient();
    //为了发帖功能的额外添加
    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 获取传递过来的Post对象
        Post post = getIntent().getParcelableExtra("post");

        postId = post.getId();

        loadComments();

        db = AppDatabase.getInstance(getApplicationContext());

        if (post != null) {
            // 更新UI以展示Post的详细信息
            updateUIWithPostDetails(post);
        }
    }

    private void updateUIWithPostDetails(Post post) {
         titleTextView = findViewById(R.id.titleTextView);
         contentTextView = findViewById(R.id.contentTextView);
         usernameTextView = findViewById(R.id.usernameTextView);
         updatetimeTextView = findViewById(R.id.updatetimeTextView);
         avatarImageView = findViewById(R.id.avatarImageView);
         imageView = findViewById(R.id.imageView);
        commentEditText = findViewById(R.id.commentEditText);
        sendButton = findViewById(R.id.sendButton);
       recyclerViewComments = findViewById(R.id.recyclerViewComments);
       noCommentsTextView = findViewById(R.id.noCommentsTextView);

        titleTextView.setText(post.getTitle());
        contentTextView.setText(post.getContent());

        sendButton.setOnClickListener(v -> sendComment());

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
    private void loadComments() {
        OkHttpClient client = new OkHttpClient();
        String commentUrl = BASE_COMMENT_URL + postId;

        Request request = new Request.Builder()
                .url(commentUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("DetailActivity", "Failed to load comments", e);
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                });
            }

            //确保所有 UI 更新操作都在主线程上执行
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        CommentResponse commentResponse = gson.fromJson(responseData, CommentResponse.class);

                        if (commentResponse != null && commentResponse.getCode() == 200) {
                            final List<Comment> comments = commentResponse.getData();
                            runOnUiThread(() -> {
                                if (comments == null || comments.isEmpty()) {
                                    showNoComments();
                                } else {
                                    setupRecyclerView(comments);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("DetailActivity", "Error parsing response", e);
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "内容显示失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e("DetailActivity", "Request failed: " + response.message());
                    runOnUiThread(() -> {
                        Toast.makeText(DetailActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void sendComment() {
        String commentContent = commentEditText.getText().toString().trim();

        // 检查评论内容是否为空
        if (commentContent.isEmpty()) {
            Toast.makeText(DetailActivity.this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        myApp = (MyApplication) getApplication();
        User currentUser = myApp.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(DetailActivity.this, "无法获取当前用户信息", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment(postId, currentUser.getUser_name(), commentContent);

        // 使用 Gson 将 Comment 对象转换为 JSON 字符串
        String jsonPost = gson.toJson(comment);

        // 构造 multipart 请求体
        RequestBody requestBody = RequestBody.create(jsonPost, MediaType.parse("application/json"));

        // 构造发送评论内容的请求
        Request request = new Request.Builder()
                .url(BASE_SEND_URL)
                .post(requestBody)
                .build();

        // 显示进度对话框
        final ProgressDialog progressDialog = new ProgressDialog(DetailActivity.this);
        progressDialog.setMessage("正在发表评论...");
        progressDialog.show();

        // 执行异步发帖请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(DetailActivity.this, "发表失败：请检查您的网络连接", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(progressDialog::dismiss);

                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 200) {
                            runOnUiThread(() -> {
                                Toast.makeText(DetailActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                                clearInputs();
                                loadComments();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(DetailActivity.this, "服务器返回异常：" + message, Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(DetailActivity.this, "解析响应失败，请重试", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(DetailActivity.this, "评论失败：" + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    //清除输入框
    private void clearInputs() {
        commentEditText.setText("");
    }

    //控制组件显隐性实现评论是否为空的播报
    private void showNoComments() {
        recyclerViewComments.setVisibility(View.GONE);
        noCommentsTextView.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView(List<Comment> comments) {
        recyclerViewComments.setVisibility(View.VISIBLE);
        noCommentsTextView.setVisibility(View.GONE);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(this, comments);
        recyclerViewComments.setAdapter(adapter);
    }
}
