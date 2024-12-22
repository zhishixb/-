//package com.example.afinal;
//
//import android.os.Bundle;
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.StaggeredGridLayoutManager;
//
//import com.example.afinal.adapter.PostAdapter; // 假设你有一个适配器来显示帖子
//import com.example.afinal.entity.Post;
//import com.example.afinal.res.ResponseModel;
//import com.google.gson.Gson;
//
//import java.io.IOException;
//import java.util.List;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//public class HomeActivity extends AppCompatActivity {
//
//    private static final String BASE_URL = "http://10.0.2.2:8099/post/all";
//    private Gson gson = new Gson();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.testactivity_main);
//
//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url(BASE_URL)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // Handle failure
//                e.printStackTrace();
//            }
//
//            @Override //帖子内容的获取
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String responseData = response.body().string();
//                    Log.d("Post", responseData);
//                    ResponseModel responseModel = gson.fromJson(responseData, ResponseModel.class);
//                    if (responseModel != null && responseModel.getCode() == 200) {
//                        final List<Post> posts = responseModel.getData();
//                        runOnUiThread(() -> setupRecyclerView(posts));
//                    }
//                }
//            }
//        });
//    }
//
//    private void setupRecyclerView(List<Post> posts) {
//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
//        // 设置 RecyclerView 的 LayoutManager 为 StaggeredGridLayoutManager
//        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
//                2, // 每一行显示2个子项
//                StaggeredGridLayoutManager.VERTICAL // 子项垂直排列
//        ));
//
//        PostAdapter adapter = new PostAdapter(this, posts); // 使用PostAdapter显示帖子
//        recyclerView.setAdapter(adapter);
//    }
//}