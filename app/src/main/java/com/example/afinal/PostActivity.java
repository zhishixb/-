package com.example.afinal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.afinal.entity.Post;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;

import android.Manifest;

public class PostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private static final int REQUEST_PERMISSION = 2;
    private static final String BASE_URL_POST = "http://10.0.2.2:8099/post/add";
    private static final String BASE_URL_UPLOAD = "http://10.0.2.2:8099/common/upload";
    private EditText titleEditText, contentEditText;
    private ImageView imagePreview;
    private Uri imageUri;
    private OkHttpClient client = new OkHttpClient();
    private String userName = "11111"; // 假设 userid 是固定的或从其他地方获取
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // 初始化视图
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        imagePreview = findViewById(R.id.imagePreview);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button postButton = findViewById(R.id.submitButton);

        // 设置选择图片按钮点击事件
        selectImageButton.setOnClickListener(v -> openImagePicker());

        // 设置发帖按钮点击事件
        postButton.setOnClickListener(v -> uploadPost());
    }

    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_PERMISSION);
            } else {
                startImagePickerIntent();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED},
                        REQUEST_PERMISSION);
            } else {
                startImagePickerIntent();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);
            } else {
                startImagePickerIntent();
            }
        }
    }

    private void startImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startImagePickerIntent();
            } else {
                Toast.makeText(this, "无法选择图片：缺少存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPost() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

//        if (title.isEmpty() || content.isEmpty()) {
//            Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
//            return;
//        }

        //判断图片是否选择
        if (imageUri == null) {
            Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果选择了图片，则上传
        try {
            File file = new File(getRealPathFromURI(imageUri));
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

            // 构造上传图片的请求
            Request request = new Request.Builder()
                    .url(BASE_URL_UPLOAD)
                    .post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addPart(body)
                            .build())
                    .build();

            // 执行异步上传请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(PostActivity.this, "上传失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();

                        try {
                            // 解析 JSON 响应（假设返回的是 JSON 格式）
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            // 使用 opt 方法以避免 JSONException
                            int code = jsonResponse.optInt("code", -1); // 默认值为 -1
                            String message = jsonResponse.optString("message", "No message from server");

                            // 显示 JSON 响应中的信息
                            runOnUiThread(() -> {
                                if ( code == 200) {
                                    Toast.makeText(PostActivity.this, "上传成功: " + message, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PostActivity.this, "服务器返回错误: " + message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(PostActivity.this, "解析响应失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(PostActivity.this, "上传失败：" + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法读取图片：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // 创建 Post 对象
        Post post = new Post(title, userName, content);

        // 使用 Gson 将 Post 对象转换为 JSON 字符串
        String jsonPost = gson.toJson(post);

        // 构造 multipart 请求体
        RequestBody requestBody = RequestBody.create(jsonPost, MediaType.parse("application/json; charset=utf-8"));

        // 构造发送帖子内容和图片的请求
        Request request = new Request.Builder()
                .url(BASE_URL_POST)
                .post(requestBody)
                .build();

        // 执行异步发帖请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PostActivity.this, "发帖失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 200) {
                            runOnUiThread(() -> {
                                Toast.makeText(PostActivity.this, "发帖成功", Toast.LENGTH_SHORT).show();
                                // 清空输入框和其他UI更新...
                                titleEditText.setText("");
                                contentEditText.setText("");
                                imagePreview.setImageResource(0); // 清除预览图片
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(PostActivity.this, "服务器返回异常：" + message, Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(PostActivity.this, "解析响应失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(PostActivity.this, "发帖失败：" + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 获取真实路径的方法
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
}