package com.example.etravels.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.etravels.R;

public class MapActivity extends AppCompatActivity {
    private ImageView imgProfile;
    private ImageView imgMap;

    private String name, phone, email, photoUrl;

    // Lanzador para recibir el resultado de FullMapActivity
    private ActivityResultLauncher<Intent> fullMapLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            // Recupera los datos actualizados al volver de FullMapActivity
                            name     = data.getStringExtra(ProfileActivity.EXTRA_NAME);
                            phone    = data.getStringExtra(ProfileActivity.EXTRA_PHONE);
                            email    = data.getStringExtra(ProfileActivity.EXTRA_EMAIL);
                            photoUrl = data.getStringExtra(ProfileActivity.EXTRA_PHOTO_URL);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Recoge los extras pasados desde LoginActivity o ProfileActivity
        Intent intent = getIntent();
        name     = intent.getStringExtra(ProfileActivity.EXTRA_NAME);
        phone    = intent.getStringExtra(ProfileActivity.EXTRA_PHONE);
        email    = intent.getStringExtra(ProfileActivity.EXTRA_EMAIL);
        photoUrl = intent.getStringExtra(ProfileActivity.EXTRA_PHOTO_URL);

        imgProfile = findViewById(R.id.imgProfile);
        imgMap     = findViewById(R.id.imgMap);

        // Click en el icono de perfil: abre ProfileActivity
        imgProfile.setOnClickListener(v -> {
            Intent i = new Intent(MapActivity.this, ProfileActivity.class);
            i.putExtra(ProfileActivity.EXTRA_NAME,      name);
            i.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            i.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            i.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            startActivity(i);
        });

        // Click en la imagen del mapa: abre FullMapActivity a pantalla completa
        imgMap.setOnClickListener(v -> {
            Intent i = new Intent(MapActivity.this, FullMapActivity.class);
            i.putExtra(ProfileActivity.EXTRA_NAME,      name);
            i.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            i.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            i.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            fullMapLauncher.launch(i);
        });
    }
}
