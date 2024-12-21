package com.example.afinal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.afinal.application.MyApplication;
import com.example.afinal.database.AppDatabase;
import com.example.afinal.entity.User;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private AppDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // 初始化数据库
        db = AppDatabase.getInstance(getContext());

        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        return view;
    }

    private void performLogin() {
        EditText usernameField = getView().findViewById(R.id.username);
        EditText passwordField = getView().findViewById(R.id.password);

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        String url = "http://10.0.2.2:8099/user/login"; // Use 10.0.2.2 in emulator

        FormBody formBody = new FormBody.Builder()
                .add("name", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "请求失败.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        final String errorMessage = jsonObject.optString("message", "未知错误");
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "登录失败: " + errorMessage, Toast.LENGTH_SHORT).show());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "解析响应失败.", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                try {
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    int code = jsonObject.getInt("code");
                    String message = jsonObject.getString("message");

                    if (code == 200 && "成功".equals(message)) {
                        final String data = jsonObject.getString("data"); // 获取用户名

                        // 检查用户是否已存在
                        User user = db.userDao().findByUsername(data);
                        if (user == null) {
                            // 用户不存在，创建新用户并保存
                            user = new User(data); // 假设构造函数只接受username
                            db.userDao().insert(user);
                            // 设置当前用户
                            ((MyApplication) getActivity().getApplication()).setCurrentUser(user);
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "新用户 " + data + " 你好!", Toast.LENGTH_SHORT).show());
                        } else {
                            // 设置当前用户
                            ((MyApplication) getActivity().getApplication()).setCurrentUser(user);
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "欢迎回来, " + data + "!", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "登录失败: " + message, Toast.LENGTH_SHORT).show());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "解析响应失败.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}