package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.afinal.adapter.PostAdapter;
import com.example.afinal.entity.Post;
import com.example.afinal.res.ResponseModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    ImageButton refreshButton;
    private RecyclerView recyclerView;
    private static final String BASE_URL = "http://10.0.2.2:8099/post/all";
    OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshButton = requireView().findViewById(R.id.refreshButton);
        recyclerView = requireView().findViewById(R.id.recyclerView);

        fetchData();
        refreshButton.setOnClickListener(v -> fetchData());
    }

    private void fetchData() {
        // 发起网络请求
        Request request = new Request.Builder().url(BASE_URL).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    ResponseModel responseModel = gson.fromJson(responseData, ResponseModel.class);
                    if (responseModel != null && responseModel.getCode() == 200) {
                        final List<Post> posts = responseModel.getData();
                        requireActivity().runOnUiThread(() -> {
                            if (isAdded()) { // 检查Fragment是否已添加到Activity
                                if(posts!=null){
                                    setupRecyclerView(posts);
                                }else{
                                    Toast.makeText(getContext(), "暂无内容", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void setupRecyclerView(List<Post> posts) {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                2, // 每一行显示2个子项
                StaggeredGridLayoutManager.VERTICAL // 子项垂直排列
        ));

        PostAdapter adapter = new PostAdapter(requireContext(), posts); // 使用PostAdapter显示帖子

        // 设置适配器的点击监听器，并传递整个Post对象
        adapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("post", post); // 直接传递Post对象
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}