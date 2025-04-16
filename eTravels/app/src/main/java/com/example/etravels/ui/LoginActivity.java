package com.example.etravels.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
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

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(username, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(LoginActivity.this, "Login exitoso", Toast.LENGTH_LONG).show();
                        // Redirige a la pantalla principal o a donde necesites
                        finish();
                    } else {
                        showErrorDialog("Error en el login", response.body().getMessage());
                    }
                } else {
                    showErrorDialog("Error", "No se pudo conectar con el servidor.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showErrorDialog("Fallo de conexión", t.getMessage());
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
