package com.example.etravels.ui;

import android.content.Intent;
import android.content.SharedPreferences;
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

        // 1) Comprobar si hay sesión activa
        SharedPreferences prefs = getSharedPreferences("SessionPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String name     = prefs.getString("name", "");
            String email    = prefs.getString("email", "");
            String phone    = prefs.getString("phone", "");
            int    userId   = prefs.getInt("userId", -1);
            String photoUrl = prefs.getString("photoUrl", "");

            // Saltar al mapa directamente
            Intent intent = new Intent(LoginActivity.this, MapActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_NAME,      name);
            intent.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            intent.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            intent.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            intent.putExtra(ProfileActivity.EXTRA_ID,        userId);
            startActivity(intent);
            finish();
            return;
        }

        // 2) Si no hay sesión, mostrar pantalla de login
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

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
                    showErrorDialog("Error HTTP: " + response.code(),
                            "No se pudo conectar con el servidor.");
                    return;
                }
                LoginResponse body = response.body();
                if (body == null) {
                    showErrorDialog("Error", "Respuesta vacía del servidor.");
                    return;
                }
                if (body.isSuccess()) {
                    // 3) Guardar en SharedPreferences la sesión iniciada
                    SharedPreferences prefs = getSharedPreferences("SessionPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("name", username);
                    editor.putString("email", body.getEmail());
                    editor.putString("phone", body.getPhone());
                    editor.putInt("userId", body.getId());
                    editor.putString("photoUrl", body.getPhotoUrl());
                    editor.apply();

                    // 4) Lanzar MapActivity
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    intent.putExtra(ProfileActivity.EXTRA_NAME,      username);
                    intent.putExtra(ProfileActivity.EXTRA_PHONE,     body.getPhone());
                    intent.putExtra(ProfileActivity.EXTRA_EMAIL,     body.getEmail());
                    intent.putExtra(ProfileActivity.EXTRA_PHOTO_URL, body.getPhotoUrl());
                    intent.putExtra(ProfileActivity.EXTRA_ID,        body.getId());
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
