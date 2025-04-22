package com.example.etravels.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.etravels.R;

public class MapActivity extends AppCompatActivity {
    private ImageView imgProfile, imgMap;
    private String name, phone, email, photoUrl;
    private int    userId;

    private final ActivityResultLauncher<Intent> fullMapLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Si FullMapActivity pasa datos de vuelta, los recuperas aquí
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            name     = data.getStringExtra(ProfileActivity.EXTRA_NAME);
                            phone    = data.getStringExtra(ProfileActivity.EXTRA_PHONE);
                            email    = data.getStringExtra(ProfileActivity.EXTRA_EMAIL);
                            photoUrl = data.getStringExtra(ProfileActivity.EXTRA_PHOTO_URL);
                            userId   = data.getIntExtra(ProfileActivity.EXTRA_ID, userId);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Recoge datos del Intent (desde LoginActivity)
        Intent intent = getIntent();
        userId   = intent.getIntExtra(ProfileActivity.EXTRA_ID,    -1);
        name     = intent.getStringExtra(ProfileActivity.EXTRA_NAME);
        phone    = intent.getStringExtra(ProfileActivity.EXTRA_PHONE);
        email    = intent.getStringExtra(ProfileActivity.EXTRA_EMAIL);
        photoUrl = intent.getStringExtra(ProfileActivity.EXTRA_PHOTO_URL);

        imgProfile = findViewById(R.id.imgProfile);
        imgMap     = findViewById(R.id.imgMap);

        imgProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra(ProfileActivity.EXTRA_ID,        userId);
            i.putExtra(ProfileActivity.EXTRA_NAME,      name);
            i.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            i.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            i.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            startActivity(i);
        });

        imgMap.setOnClickListener(v -> {
            Intent i = new Intent(this, FullMapActivity.class);
            i.putExtra(ProfileActivity.EXTRA_ID,        userId);
            i.putExtra(ProfileActivity.EXTRA_NAME,      name);
            i.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            i.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            i.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            fullMapLauncher.launch(i);
        });
    }
}
