package com.example.afinal;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.afinal.R;
import com.example.afinal.application.MyApplication;
import com.example.afinal.entity.Post;
import com.example.afinal.entity.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostFragment extends Fragment {

    private static final int REQUEST_PERMISSION = 2;
    private static final String BASE_URL_POST = "http://10.0.2.2:8099/post/add";
    private static final String BASE_URL_UPLOAD = "http://10.0.2.2:8099/common/upload";

    private EditText titleEditText, contentEditText;
    private ImageView imagePreview;
    private Uri imageUri;
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();
    private MyApplication myApp;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleEditText = view.findViewById(R.id.titleEditText);
        contentEditText = view.findViewById(R.id.contentEditText);
        imagePreview = view.findViewById(R.id.imagePreview);
        Button selectImageButton = view.findViewById(R.id.selectImageButton);
        Button postButton = view.findViewById(R.id.submitButton);

        // 设置选择图片按钮点击事件
        selectImageButton.setOnClickListener(v -> openImagePicker());

        // 设置发帖按钮点击事件
        postButton.setOnClickListener(v -> uploadPost());

        // 初始化用于启动图片选择器的 ActivityResultLauncher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleImagePickerResult(result));
    }

    private void openImagePicker() {
        if (checkPermissions()) {
            startImagePickerIntent();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED},
                    REQUEST_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    private void startImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void handleImagePickerResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                imagePreview.setImageURI(imageUri);
            } else {
                Toast.makeText(getContext(), "未选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startImagePickerIntent();
            } else {
                Toast.makeText(getContext(), "无法选择图片：缺少存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadPost() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        //判断图片是否选择
        if (imageUri == null) {
            Toast.makeText(getContext(), "未选择图片", Toast.LENGTH_SHORT).show();
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

            // 显示进度对话框
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("正在上传...");
            progressDialog.show();

            // 执行异步上传请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(PostFragment.this.getContext(), "上传失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            int code = jsonResponse.optInt("code", -1); // 默认值为 -1
                            String message = jsonResponse.optString("message", "No message from server");

                            runOnUiThread(() -> {
                                if (code == 200) {
                                    progressDialog.dismiss();
                                    Toast.makeText(PostFragment.this.getContext(), "上传成功: " + message, Toast.LENGTH_SHORT).show();
                                    sendPost(title, content);
                                } else {
                                    Toast.makeText(PostFragment.this.getContext(), "服务器返回错误: " + message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(PostFragment.this.getContext(), "解析响应失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(PostFragment.this.getContext(), "上传失败：" + response.message(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "无法读取图片：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPost(String title, String content) {
        myApp = (MyApplication) getActivity().getApplication();
        User currentUser = myApp.getCurrentUser();
        if(currentUser != null){
            Toast.makeText(PostFragment.this.getContext(), currentUser.getUser_name(), Toast.LENGTH_SHORT).show();
        }
        // 创建 Post 对象
        Post post = new Post(title, currentUser.getUser_name(), content);

        // 使用 Gson 将 Post 对象转换为 JSON 字符串
        String jsonPost = gson.toJson(post);

        // 构造 multipart 请求体
        RequestBody requestBody = RequestBody.create(jsonPost, MediaType.parse("application/json; charset=utf-8"));

        // 构造发送帖子内容的请求
        Request request = new Request.Builder()
                .url(BASE_URL_POST)
                .post(requestBody)
                .build();

        // 执行异步发帖请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PostFragment.this.getContext(), "发帖失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                                Toast.makeText(PostFragment.this.getContext(), "发帖成功", Toast.LENGTH_SHORT).show();
                                clearInputs();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(PostFragment.this.getContext(), "服务器返回异常：" + message, Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(PostFragment.this.getContext(), "解析响应失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(PostFragment.this.getContext(), "发帖失败：" + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void clearInputs() {
        titleEditText.setText("");
        contentEditText.setText("");
        imagePreview.setImageResource(0); // 清除预览图片
    }

    // 获取真实路径的方法
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    //保障使用的时主线程
    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }
}