package com.example.afinal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.afinal.application.MyApplication;
import com.example.afinal.dao.UserDao;
import com.example.afinal.database.AppDatabase;
import com.example.afinal.entity.User;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpaceFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String BASE_AVATAR_URL = "http://10.0.2.2:8099/avatar/download?name=";
    private static final String BASE_UPLOAD_URL = "http://10.0.2.2:8099/avatar/upload";
    private ShapeableImageView avatarImageView, previewAvatarImageView;
    private TextView nicknameTextView;
    private Button changeAvatarButton, confirmButton, changeNicknameButton;
    private Uri selectedImageUri;
    private EditText nicknameEditText;
    private OkHttpClient client = new OkHttpClient();
    private AppDatabase db; //sqlite操作类
    private  MyApplication myApp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_space, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //获取对宿主 Activity 的引用
        if (getActivity() != null) {
            db = AppDatabase.getInstance(getActivity().getApplication());
            myApp = (MyApplication) getActivity().getApplication();
        }

        avatarImageView = view.findViewById(R.id.avatarImageView);
        nicknameTextView = view.findViewById(R.id.nicknameTextView);
        previewAvatarImageView = view.findViewById(R.id.previewAvatarImageView);
        changeAvatarButton = view.findViewById(R.id.changeAvatarButton);
        confirmButton = view.findViewById(R.id.confirmButton);
        nicknameEditText = view.findViewById(R.id.nicknameEditText);
        changeNicknameButton = view.findViewById(R.id.changeNicknameButton);

        //组件内容初始化
        loadCotent();

        // Set up click listeners for buttons
        setupButtonClickListeners();
    }

    //为按钮设置监听器绑定事件
    private void setupButtonClickListeners() {
        changeAvatarButton.setOnClickListener(v -> openImagePicker());

        confirmButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                // 更新头像
                Glide.with(this).load(selectedImageUri).into(avatarImageView);
                selectedImageUri = null;
            }
        });

        // 绑定修改昵称按钮的点击事件
        changeNicknameButton.setOnClickListener(v -> changeNickName());
        // 绑定修改头像
        confirmButton.setOnClickListener(v -> changeAvatar());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }

    //实现内容和信息在组件中的添加
    private void loadCotent() {
        //用户名
        User currentUser = myApp.getCurrentUser();
        nicknameTextView.setText(currentUser.getNick_name());

        //头像显示
        Glide.with(this)
                .load(BASE_AVATAR_URL+currentUser.getAvatar())
                .skipMemoryCache(true) // 禁用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .into(avatarImageView);
    }

    private void changeNickName() {
        // 获取新的昵称
        String newNickName = nicknameEditText.getText().toString().trim();

        if (newNickName.isEmpty()) {
            // 如果新昵称为空，显示错误信息并返回
            Toast.makeText(getContext(), "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前用户
        User currentUser = myApp.getCurrentUser();
        if (currentUser == null) {
            // 如果没有当前用户，显示错误信息并返回
            Toast.makeText(getContext(), "未找到当前用户", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新用户的昵称
        currentUser.setNick_name(newNickName);

        // 使用异步任务来更新数据库
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            UserDao userDao = db.userDao();
            userDao.update(currentUser); // 更新用户信息到数据库

            // 在UI线程上更新昵称显示
            getActivity().runOnUiThread(() -> {
                nicknameTextView.setText(newNickName);
                Toast.makeText(getContext(), "昵称已成功更新", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void changeAvatar() {
        // 检查是否选择了图片
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "未选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示进度对话框
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("正在上传...");
        progressDialog.show();

        try {
            File file = new File(getRealPathFromURI(selectedImageUri));
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

            // 构造上传图片的请求
            Request request = new Request.Builder()
                    .url(BASE_UPLOAD_URL)
                    .post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addPart(body)
                            .build())
                    .build();

            // 执行异步上传请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(SpaceFragment.this.getContext(), "上传失败：请检查您的网络连接", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> progressDialog.dismiss());

                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "{}";

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            int code = jsonResponse.optInt("code", -1); // 默认值为 -1
                            String message = jsonResponse.optString("message", "No message from server");
                            String avatarIdStr = jsonResponse.optString("data", "0");

                            if (code == 200 ) {
                                try {
                                    final int avatarId = Integer.parseInt(avatarIdStr);

                                    Executor executor = Executors.newSingleThreadExecutor();
                                    executor.execute(() -> {
                                        User currentUser = myApp.getCurrentUser();
                                        if (currentUser != null) {
                                            currentUser.setAvatar(avatarId);

                                            UserDao userDao = db.userDao();
                                            userDao.update(currentUser); // 更新用户信息到数据库

                                            // 在UI线程上更新头像显示
                                            runOnUiThread(() -> {
                                                Glide.with(SpaceFragment.this)
                                                        .load(BASE_AVATAR_URL + avatarId)
                                                        .skipMemoryCache(true) // 禁用内存缓存
                                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                                        .into(avatarImageView);
                                                Toast.makeText(getContext(), "上传成功，头像已更新", Toast.LENGTH_SHORT).show();
                                            });
                                        } else {
                                            runOnUiThread(() -> Toast.makeText(getContext(), "无法获取当前用户信息", Toast.LENGTH_SHORT).show());
                                        }
                                    });
                                } catch (NumberFormatException nfe) {
                                    runOnUiThread(() -> Toast.makeText(getContext(), "无效的头像 ID 格式", Toast.LENGTH_SHORT).show());
                                }
                            } else {
                                runOnUiThread(() -> Toast.makeText(getContext(), "服务器返回错误: " + message, Toast.LENGTH_SHORT).show());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(getContext(), "解析响应失败，请重试", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(getContext(), "上传失败：" + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "无法读取图片：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    //处理来自图像选择器的结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            // Display the selected image in the preview ImageView
            Glide.with(this).load(selectedImageUri).into(previewAvatarImageView);
        }
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
}