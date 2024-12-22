package com.example.afinal.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.afinal.R;
import com.example.afinal.dao.UserDao;
import com.example.afinal.database.AppDatabase;
import com.example.afinal.entity.Post;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ImageViewHolder> {

    private static final String TAG = "PostAdapter"; // 用于日志标签
    private List<Post> posts;
    private Context context;
    private AppDatabase db; //sqlite操作类
    private AdapterView.OnItemClickListener listener; // 添加监听器实现点击item得页面跳转
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8099/common/download?name=";
    private static final String BASE_AVATAR_URL = "http://10.0.2.2:8099/avatar/download?name=";

    // 定义一个接口用于处理点击事件
    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    private OnItemClickListener clickListener;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.db = AppDatabase.getInstance(context.getApplicationContext());
    }

    // 提供一个方法来设置监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_block, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        final Post post = posts.get(position);

        //为了实时加载绕过glide的磁盘和内存缓存
        // 构造图片 URL 并加载图片
        String imageUrl = BASE_IMAGE_URL + post.getId();
        Glide.with(context)
                .load(imageUrl)
                .skipMemoryCache(true) // 禁用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .into(holder.imageView);

        holder.titleTextView.setText(post.getTitle());

        // 设置默认文本
        holder.nicknameTextView.setText("加载中...");

        // 使用 Executor 创建单线程操作，来运行数据库查询
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            UserDao userDao = db.userDao();

            // 异步获取昵称和头像ID
            Integer avatarId = userDao.getAvatar(post.getUsername());
            String nickname = userDao.getNickNameByUsername(post.getUsername());

            // 更新UI线程上的TextView和ImageView
            holder.itemView.post(() -> {
                if (avatarId != null) {
                    String avatarUrl = BASE_AVATAR_URL + avatarId;
                    Glide.with(context)
                            .load(avatarUrl)
                            .skipMemoryCache(true) // 禁用内存缓存
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                            .into(holder.avatarBlock);
                } else {
                    Log.w(TAG, "Failed to retrieve avatar for username: " + post.getUsername());
                }

                if (nickname != null && !nickname.isEmpty()) {
                    holder.nicknameTextView.setText(nickname);
                    holder.nicknameTextView.setTextColor(context.getResources().getColor(android.R.color.black)); // 正常颜色
                } else {
                    holder.nicknameTextView.setText("未知用户");
                    holder.nicknameTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray)); // 警告颜色
                    Log.w(TAG, "Failed to retrieve nickname for username: " + post.getUsername());
                }
            });
        });

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        ImageView avatarBlock;
        TextView nicknameTextView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            avatarBlock = itemView.findViewById(R.id.avatarBlock);
            nicknameTextView = itemView.findViewById(R.id.nicknameTextView); // 确保 item_block.xml 中有这个 ID
        }
    }
}