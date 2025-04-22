package com.example.etravels.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.etravels.R;
import com.example.etravels.network.ApiService;
import com.example.etravels.network.LoginResponse;
import com.example.etravels.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername  = findViewById(R.id.etUsername);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvRegister  = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorDialog("Campos vacíos", "Rellena todos los campos");
            return;
        }

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        Call<LoginResponse> call = api.loginUser(username, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!response.isSuccessful()) {
                    showErrorDialog("Error HTTP: " + response.code(), "No se pudo conectar con el servidor.");
                    return;
                }
                LoginResponse body = response.body();
                if (body == null) {
                    showErrorDialog("Error", "Respuesta vacía del servidor.");
                    return;
                }
                if (body.isSuccess()) {
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    intent.putExtra(ProfileActivity.EXTRA_NAME,     username);
                    intent.putExtra(ProfileActivity.EXTRA_PHONE,    body.getPhone());
                    intent.putExtra(ProfileActivity.EXTRA_EMAIL,    body.getEmail());
                    intent.putExtra(ProfileActivity.EXTRA_PHOTO_URL, body.getPhotoUrl());
                    intent.putExtra(ProfileActivity.EXTRA_ID, body.getId());
                    startActivity(intent);
                    finish();
                } else {
                    showErrorDialog("Error en el login", body.getMessage());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showErrorDialog("Fallo de conexión", t.getMessage());
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
