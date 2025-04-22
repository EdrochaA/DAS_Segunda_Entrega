package com.example.etravels.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.etravels.R;

public class ReviewDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        TextView tvTitle     = findViewById(R.id.tvTitle);
        TextView tvUser      = findViewById(R.id.tvUser);
        TextView tvAddress   = findViewById(R.id.tvAddress);
        TextView tvComment   = findViewById(R.id.tvComment);
        Button   btnBack     = findViewById(R.id.btnBack);

        // Recuperar datos del Intent
        tvTitle.setText(getIntent().getStringExtra("titulo"));
        tvUser.setText("Por: " + getIntent().getStringExtra("usuario"));
        tvAddress.setText("Dirección: " + getIntent().getStringExtra("direccion"));
        tvComment.setText(getIntent().getStringExtra("comentario"));

        btnBack.setOnClickListener(v -> finish());
    }
}
