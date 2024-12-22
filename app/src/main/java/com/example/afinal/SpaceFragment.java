package com.example.afinal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.afinal.application.MyApplication;
import com.example.afinal.database.AppDatabase;
import com.example.afinal.entity.User;
import com.google.android.material.imageview.ShapeableImageView;

public class SpaceFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ShapeableImageView avatarImageView, previewAvatarImageView;
    private TextView nicknameTextView;
    private Button changeAvatarButton, confirmButton;
    private Uri selectedImageUri;
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

        //组件内容初始化
        loadCotent();

        // Set up click listeners for buttons
        setupButtonClickListeners();
    }

    private void setupButtonClickListeners() {
        changeAvatarButton.setOnClickListener(v -> openImagePicker());

        confirmButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                // Handle image selection confirmation here
                // For example, update the main avatar ImageView and save the new avatar to storage or server.
                Glide.with(this).load(selectedImageUri).into(avatarImageView);
                // Optionally, reset selectedImageUri after successful upload
                selectedImageUri = null;
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //实现内容和信息在组件中的添加
    private void loadCotent() {
        //用户名
        User currentUser = myApp.getCurrentUser();
        nicknameTextView.setText(currentUser.getNick_name());

        //头像显示
        Glide.with(this)
                .load("http://10.0.2.2:8099/avatar/download?name="+currentUser.getAvatar())
                .skipMemoryCache(true) // 禁用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .into(avatarImageView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            // Display the selected image in the preview ImageView
            Glide.with(this).load(selectedImageUri).into(previewAvatarImageView);
        }
    }
}