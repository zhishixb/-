package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private static final String DEFAULT_AVATAR_PATH = "/storage/self/primary/Pictures/Avatar/avatar.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 显式地将findViewById的结果转换为ImageView
        ImageView imageView = findViewById(R.id.imageView);

        // 检查并请求读取外部存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            loadAvatar(imageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
                                           @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ImageView imageView = findViewById(R.id.imageView);
            loadAvatar(imageView);
        } else {
            // 处理权限被拒绝的情况
        }
    }

    private void loadAvatar(ImageView imageView) {
        // 获取默认头像的 Uri
        Uri avatarUri = getAvatarUri();

        // 使用 Glide 加载图片到 ImageView 中
        Glide.with(this)
                .load(avatarUri)
                .into(imageView);
    }

    private Uri getAvatarUri() {
        // 对于 Android 10 及以上版本，推荐使用 MediaStore API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getAvatarUriFromMediaStore();
        } else {
            // 对于较低版本，可以使用 file:// URI
            return Uri.fromFile(new java.io.File(DEFAULT_AVATAR_PATH));
        }
    }

    private Uri getAvatarUriFromMediaStore() {
        // 查询 MediaStore 以获取头像的 Uri
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = {DEFAULT_AVATAR_PATH};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                long id = cursor.getLong(idColumn);
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}