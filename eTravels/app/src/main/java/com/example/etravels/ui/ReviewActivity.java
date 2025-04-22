package com.example.etravels.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.etravels.R;
import com.example.etravels.network.ApiService;
import com.example.etravels.network.GenericResponse;
import com.example.etravels.network.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = ProfileActivity.EXTRA_NAME;

    private TextInputEditText etTitle, etAddress, etComment;
    private MaterialButton btnCreate, btnCancel;

    private String usuario;
    private double lat, lon;

    // Cliente para validar dirección con Nominatim
    private final OkHttpClient nominatim = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        etTitle   = findViewById(R.id.etTitle);
        etAddress = findViewById(R.id.etAddress);
        etComment = findViewById(R.id.etComment);
        btnCreate = findViewById(R.id.btnCreate);
        btnCancel = findViewById(R.id.btnCancel);

        usuario = getIntent().getStringExtra(EXTRA_NAME);
        lat     = getIntent().getDoubleExtra("lat", 0);
        lon     = getIntent().getDoubleExtra("lon", 0);

        btnCreate.setOnClickListener(v -> {
            String title   = etTitle.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String comment = etComment.getText().toString().trim();
            if (title.isEmpty() || address.isEmpty() || comment.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            validateAddress(address, () -> sendReview(title, address, comment));
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    /** Comprueba en Nominatim que la dirección existe antes de grabar */
    private void validateAddress(String address, Runnable onSuccess) {
        String url = "https://nominatim.openstreetmap.org/search?"
                + "q=" + Uri.encode(address)
                + "&format=json&limit=1";
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", getPackageName())
                .build();

        nominatim.newCall(req).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ReviewActivity.this,
                                "Error validando dirección", Toast.LENGTH_SHORT).show()
                );
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response resp) throws IOException {
                if (!resp.isSuccessful()) return;
                JsonArray arr = JsonParser.parseString(resp.body().string())
                        .getAsJsonArray();
                runOnUiThread(() -> {
                    if (arr.isEmpty()) {
                        Toast.makeText(ReviewActivity.this,
                                "Dirección no encontrada. Vuelva a introducirla.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        onSuccess.run();
                    }
                });
            }
        });
    }

    /** Envía la reseña al servidor usando Retrofit2 */
    private void sendReview(String title, String address, String comment) {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        Call<GenericResponse> call = api.addReview(
                usuario, title, address, comment, lat, lon
        );
        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    Toast.makeText(ReviewActivity.this,
                            "Reseña guardada", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = (resp.body() != null)
                            ? resp.body().getMessage()
                            : "HTTP " + resp.code();
                    Toast.makeText(ReviewActivity.this,
                            "Error: " + msg, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(ReviewActivity.this,
                        "Fallo de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
