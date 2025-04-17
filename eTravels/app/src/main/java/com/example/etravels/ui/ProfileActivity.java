package com.example.etravels.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.etravels.R;

public class ProfileActivity extends AppCompatActivity {
    public static final String EXTRA_NAME  = "extra_name";
    public static final String EXTRA_PHONE = "extra_phone";
    public static final String EXTRA_EMAIL = "extra_email";

    private ImageView imgUserIcon;
    private TextView tvName, tvPhone, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgUserIcon = findViewById(R.id.imgUserIcon);
        tvName      = findViewById(R.id.tvName);
        tvPhone     = findViewById(R.id.tvPhone);
        tvEmail     = findViewById(R.id.tvEmail);

        // Recogemos datos del Intent
        String name  = getIntent().getStringExtra(EXTRA_NAME);
        String phone = getIntent().getStringExtra(EXTRA_PHONE);
        String email = getIntent().getStringExtra(EXTRA_EMAIL);

        // Mostramos en pantalla
        tvName.setText(name);
        tvPhone.setText("Teléfono: " + phone);
        tvEmail.setText("Email: " + email);
    }
}
