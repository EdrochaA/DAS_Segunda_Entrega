package com.example.etravels.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.example.etravels.R;
import com.example.etravels.network.ApiService;
import com.example.etravels.network.Review;
import com.example.etravels.network.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FullMapActivity extends AppCompatActivity {
    private MapView mapView;
    private SearchView svSearch;
    private Button btnNewReview, btnBack;
    private OkHttpClient http = new OkHttpClient();

    private String username;
    private double selectedLat = 0, selectedLon = 0;
    private boolean hasRetried = false;

    private MyLocationNewOverlay locationOverlay;

    // For starting ReviewActivity
    private final ActivityResultLauncher<Intent> reviewLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadReviews();
                            Toast.makeText(this, "Reseña creada", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    // For location permission
    private final ActivityResultLauncher<String> locPermLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            enableMyLocationOverlay();
                        } else {
                            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osmdroid config
        Configuration.getInstance()
                .setUserAgentValue(getPackageName());
        Configuration.getInstance()
                .load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_full_map);

        // Logged user
        username     = getIntent().getStringExtra(ProfileActivity.EXTRA_NAME);

        mapView      = findViewById(R.id.mapView);
        svSearch     = findViewById(R.id.svSearch);
        btnNewReview = findViewById(R.id.btnNewReview);
        btnBack      = findViewById(R.id.btnBack);

        // Initialize map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(18.0);

        // Request location permission / enable overlay
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == getPackageManager().PERMISSION_GRANTED) {
            enableMyLocationOverlay();
        } else {
            locPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Load existing reviews
        loadReviews();

        // Expand SearchView on touch
        svSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                svSearch.setIconified(false);
                svSearch.requestFocus();
            }
            return false;
        });

        // Search on submit
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                hasRetried = false;
                searchLocation(query.trim());
                svSearch.clearFocus();
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // New review button
        btnNewReview.setOnClickListener(v -> {
            Intent i = new Intent(this, ReviewActivity.class);
            i.putExtra(ProfileActivity.EXTRA_NAME, username);
            i.putExtra("lat", selectedLat);
            i.putExtra("lon", selectedLon);
            reviewLauncher.launch(i);
        });

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }

    private void enableMyLocationOverlay() {
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this),
                mapView
        );
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint loc = locationOverlay.getMyLocation();
            if (loc != null) {
                selectedLat = loc.getLatitude();
                selectedLon = loc.getLongitude();
                mapView.getController().setCenter(loc);
            }
        }));
    }

    private void loadReviews() {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        Call<List<Review>> call = api.getAllReviews();
        call.enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) return;
                runOnUiThread(() -> {
                    mapView.getOverlays().clear();
                    if (locationOverlay != null) {
                        mapView.getOverlays().add(locationOverlay); // re-add overlay
                    }
                    Map<String, Integer> contador = new HashMap<>();

                    for (Review r : resp.body()) {
                        double lat = r.getLat();
                        double lon = r.getLon();
                        String key = lat + "," + lon;
                        int n = contador.getOrDefault(key, 0);
                        contador.put(key, n + 1);

                        double baseRadio = 0.00050;
                        double radio     = (n > 0) ? baseRadio * (n + 1) : 0;
                        double angulo    = Math.toRadians(((n - 1) * 45) % 360);
                        double nuevoLat  = lat + radio * Math.sin(angulo);
                        double nuevoLon  = lon + radio * Math.cos(angulo);

                        GeoPoint p = new GeoPoint(nuevoLat, nuevoLon);
                        Marker marker = new Marker(mapView);
                        marker.setPosition(p);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setTitle(r.getTitulo());

                        // red icon 48dp
                        Drawable d = AppCompatResources.getDrawable(
                                FullMapActivity.this, R.drawable.ic_marker_red);
                        int sizePx = (int)(48 * getResources().getDisplayMetrics().density);
                        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bmp);
                        d.setBounds(0,0,sizePx,sizePx);
                        d.draw(canvas);
                        marker.setIcon(new BitmapDrawable(getResources(), bmp));

                        marker.setOnMarkerClickListener((m, map) -> {
                            Intent i = new Intent(FullMapActivity.this,
                                    ReviewDetailActivity.class);
                            i.putExtra("titulo",     r.getTitulo());
                            i.putExtra("usuario",    r.getUsuario());
                            i.putExtra("direccion",  r.getDireccion());
                            i.putExtra("comentario", r.getComentario());
                            startActivity(i);
                            return true;
                        });

                        mapView.getOverlays().add(marker);
                    }
                    mapView.invalidate();
                });
            }
            @Override public void onFailure(Call<List<Review>> call, Throwable t) { }
        });
    }

    private void searchLocation(String texto) {
        if (texto.isEmpty()) return;
        String url = "https://nominatim.openstreetmap.org/search?"
                + "q=" + Uri.encode(texto)
                + "&format=json&limit=1&countrycodes=es";
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", getPackageName())
                .build();

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(FullMapActivity.this,
                                "Error en búsqueda", Toast.LENGTH_SHORT).show()
                );
            }
            @Override public void onResponse(okhttp3.Call call, okhttp3.Response resp)
                    throws IOException {
                if (!resp.isSuccessful()) return;
                String body = resp.body().string();
                JsonArray arr = JsonParser.parseString(body).getAsJsonArray();
                if (arr.isEmpty() && !hasRetried) {
                    hasRetried = true;
                    runOnUiThread(() ->
                            Toast.makeText(FullMapActivity.this,
                                    "No encontrado \"" + texto + "\"",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                if (!arr.isEmpty()) {
                    JsonObject place = arr.get(0).getAsJsonObject();
                    selectedLat = place.get("lat").getAsDouble();
                    selectedLon = place.get("lon").getAsDouble();
                    runOnUiThread(() -> {
                        MapController c = (MapController) mapView.getController();
                        GeoPoint p = new GeoPoint(selectedLat, selectedLon);
                        c.setCenter(p);
                        c.setZoom(18.0);
                        loadReviews();
                    });
                }
            }
        });
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
