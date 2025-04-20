package com.example.etravels.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.etravels.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity {
    private ImageView imgProfile;
    private MapView   mapView;
    private MyLocationNewOverlay locationOverlay;

    private String name, phone, email, photoUrl;

    private final ActivityResultLauncher<String> locPermLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) enableMyLocation();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance()
                .load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_map);

        // Recogemos datos del Intent
        name     = getIntent().getStringExtra(ProfileActivity.EXTRA_NAME);
        phone    = getIntent().getStringExtra(ProfileActivity.EXTRA_PHONE);
        email    = getIntent().getStringExtra(ProfileActivity.EXTRA_EMAIL);
        photoUrl = getIntent().getStringExtra(ProfileActivity.EXTRA_PHOTO_URL);

        imgProfile = findViewById(R.id.imgProfile);
        mapView    = findViewById(R.id.mapView);

        imgProfile.setOnClickListener(v -> {
            Intent i = new Intent(MapActivity.this, ProfileActivity.class);
            i.putExtra(ProfileActivity.EXTRA_NAME,      name);
            i.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            i.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            i.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            startActivity(i);
        });

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            locPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableMyLocation() {
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this),
                mapView
        );
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint loc = locationOverlay.getMyLocation();
            if (loc != null) {
                mapView.getController().setCenter(loc);
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationOverlay != null) locationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationOverlay != null) locationOverlay.disableMyLocation();
    }
}
