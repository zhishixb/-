package com.example.afinal.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.afinal.R;
import com.example.afinal.dao.UserDao;
import com.example.afinal.database.AppDatabase;
import com.example.afinal.entity.Comment;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private static final String TAG = "CommentAdapter"; // 用于日志标签
    private List<Comment> commentList;
    private Context context;
    private static final String BASE_AVATAR_URL = "http://10.0.2.2:8099/avatar/download?name=";
    private AppDatabase db;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.commentList = commentList;
        this.context = context;
        this.db = AppDatabase.getInstance(context.getApplicationContext());
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        Log.d("comment_item", comment.toString());

        // 使用 Executor 创建单线程操作，来运行数据库查询
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            UserDao userDao = db.userDao();

            // 异步获取昵称和头像ID
            Integer avatarId = userDao.getAvatar(comment.getCommenterName());
            String nickname = userDao.getNickNameByUsername(comment.getCommenterName());

            // 更新UI线程上的TextView和ImageView
            holder.itemView.post(() -> {
                if (avatarId != null) {
                    String avatarUrl = BASE_AVATAR_URL + avatarId;
                    Glide.with(context)
                            .load(avatarUrl)
                            .into(holder.commentAvatarImageView);
                } else {
                    Log.w(TAG, "Failed to retrieve avatar for username: " + comment.getCommenterName());
                }

                if (nickname != null && !nickname.isEmpty()) {
                    holder.commenterNameTextView.setText(nickname);
                    holder.commenterNameTextView.setTextColor(context.getResources().getColor(android.R.color.black)); // 正常颜色
                } else {
                    holder.commenterNameTextView.setText("未知用户");
                    holder.commenterNameTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray)); // 警告颜色
                    Log.w(TAG, "Failed to retrieve nickname for username: " + comment.getCommenterName());
                }
            });
        });

        holder.commentContentTextView.setText(comment.getContent());

        //修改时间格式
        try {
            // 使用 ISO 8601 预定义格式化器来解析时间字符串
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            LocalDateTime dateTime = LocalDateTime.parse(comment.getCreateTime(), inputFormatter);
            String formattedTime = dateTime.format(outputFormatter);
            holder.commentTimeTextView.setText(formattedTime);
        } catch (Exception e) {
            Log.e("DetailActivity", "Error parsing time string: " + comment.getCreateTime(), e);
            holder.commentTimeTextView.setText("更新时间: 格式错误");
        }
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView commentAvatarImageView;
        TextView commenterNameTextView;
        TextView commentContentTextView;
        TextView commentTimeTextView;

        CommentViewHolder(View itemView) {
            super(itemView);
            commentAvatarImageView = itemView.findViewById(R.id.commentAvatarImageView);
            commenterNameTextView = itemView.findViewById(R.id.commenterNameTextView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
            commentTimeTextView = itemView.findViewById(R.id.commentTimeTextView);
        }
    }
}
