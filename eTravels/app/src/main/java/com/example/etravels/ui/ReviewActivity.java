package com.example.etravels.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.etravels.R;
import com.example.etravels.network.ApiService;
import com.example.etravels.network.GenericResponse;
import com.example.etravels.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = ProfileActivity.EXTRA_NAME;

    private EditText etTitle, etAddress, etComment;
    private Button   btnCreate, btnCancel;

    private String usuario;
    private double lat, lon;

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
            ApiService api = RetrofitClient.getInstance().create(ApiService.class);
            Call<GenericResponse> call = api.addReview(
                    usuario, title, address, comment, lat, lon
            );
            call.enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> resp) {
                    if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                        Toast.makeText(ReviewActivity.this, "Reseña guardada", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String msg = resp.body() != null
                                ? resp.body().getMessage()
                                : "HTTP " + resp.code();
                        Toast.makeText(ReviewActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    Toast.makeText(ReviewActivity.this, "Fallo red: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
