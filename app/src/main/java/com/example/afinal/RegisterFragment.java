package com.example.afinal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        Button registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        return view;
    }

    private void performRegistration() {
        EditText nameField = getView().findViewById(R.id.name);
        EditText passwordField = getView().findViewById(R.id.password);

        String name = nameField.getText().toString();
        String password = passwordField.getText().toString();

        // 创建 JSON 对象
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "创建 JSON 数据失败.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2:8099/user/register"; // Registration URL

        // 将 JSON 对象转换为字符串，并创建 RequestBody
        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.get("application/json; charset=utf-8")
        );

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(body)
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
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "注册失败: " + errorMessage, Toast.LENGTH_SHORT).show());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "解析响应失败.", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                final String myResponse = response.body().string();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "注册成功!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
