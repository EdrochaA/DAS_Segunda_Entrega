package com.example.etravels.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.etravels.R;
import com.example.etravels.network.ApiService;
import com.example.etravels.network.GenericResponse;
import com.example.etravels.network.RetrofitClient;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    public static final String EXTRA_NAME      = "extra_name";
    public static final String EXTRA_PHONE     = "extra_phone";
    public static final String EXTRA_EMAIL     = "extra_email";
    public static final String EXTRA_PHOTO_URL = "extra_photo_url";
    public static final String EXTRA_ID        = "extra_id";

    private static final String PREFS_NAME     = "prefs";
    private static final String PREF_PHOTO_URL = "prefs_photo_url";

    private ImageView imgUserIcon;
    private TextView  tvName, tvPhone, tvEmail;
    private Button    btnBack;
    private String    username, initialPhotoUrl;

    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras    = result.getData().getExtras();
                            Bitmap thumbnail = (Bitmap) extras.get("data");
                            imgUserIcon.setImageBitmap(thumbnail);
                            uploadImageToServer(thumbnail);
                        } else {
                            Toast.makeText(this, "No se tomó foto", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) openCamera();
                        else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgUserIcon = findViewById(R.id.imgUserIcon);
        tvName      = findViewById(R.id.tvName);
        tvPhone     = findViewById(R.id.tvPhone);
        tvEmail     = findViewById(R.id.tvEmail);
        btnBack     = findViewById(R.id.btnBack);

        Intent intent    = getIntent();
        username         = intent.getStringExtra(EXTRA_NAME);
        String phone     = intent.getStringExtra(EXTRA_PHONE);
        String email     = intent.getStringExtra(EXTRA_EMAIL);
        initialPhotoUrl  = intent.getStringExtra(EXTRA_PHOTO_URL);

        tvName.setText(username);
        tvPhone.setText("Teléfono: " + phone);
        tvEmail.setText("Email: " + email);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String urlFromPrefs    = prefs.getString(PREF_PHOTO_URL + "_" + username, null);
        String urlToLoad       =
                (initialPhotoUrl != null && !initialPhotoUrl.isEmpty())
                        ? initialPhotoUrl
                        : urlFromPrefs;

        if (urlToLoad != null && !urlToLoad.isEmpty()) {
            Glide.with(this)
                    .load(urlToLoad)
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .into(imgUserIcon);
        } else {
            imgUserIcon.setImageResource(R.drawable.usuario);
        }

        imgUserIcon.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnBack.setOnClickListener(v -> finish());

        // ——— Nuevo botón de Cerrar sesión ———
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Borrar solo SharedPreferences de sesión
            SharedPreferences sessionPrefs =
                    getSharedPreferences("SessionPrefs", MODE_PRIVATE);
            sessionPrefs.edit().clear().apply();

            // Volver a LoginActivity limpiando toda la pila
            Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            logoutIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(logoutIntent);
            finish();
        });
    }

    private void openCamera() {
        Intent intent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE
        );
        takePictureLauncher.launch(intent);
    }

    private void uploadImageToServer(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        String base64Image = Base64.encodeToString(
                stream.toByteArray(),
                Base64.DEFAULT
        );

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        Call<GenericResponse> call = api.uploadProfileImage(
                username, base64Image
        );
        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(
                    Call<GenericResponse> call,
                    Response<GenericResponse> resp
            ) {
                if (resp.isSuccessful() && resp.body() != null) {
                    GenericResponse body = resp.body();
                    if (body.isSuccess()) {
                        String imageUrl = body.getUrl();
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                .edit()
                                .putString(
                                        PREF_PHOTO_URL + "_" + username,
                                        imageUrl
                                ).apply();

                        Glide.with(ProfileActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.usuario)
                                .error(R.drawable.usuario)
                                .into(imgUserIcon);
                        Toast.makeText(
                                ProfileActivity.this,
                                "Foto subida",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                ProfileActivity.this,
                                "Error: " + body.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                } else {
                    Toast.makeText(
                            ProfileActivity.this,
                            "Error al conectar",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
            @Override
            public void onFailure(
                    Call<GenericResponse> call, Throwable t
            ) {
                Toast.makeText(
                        ProfileActivity.this,
                        "Fallo: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
