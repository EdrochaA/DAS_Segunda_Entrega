package com.example.etravels.ui;

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
    private double selectedLat = 43.2620, selectedLon = -2.9340;
    private boolean hasRetried = false;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar osmdroid
        Configuration.getInstance()
                .setUserAgentValue(getPackageName());
        Configuration.getInstance()
                .load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_full_map);

        // Recuperar usuario logueado
        username = getIntent().getStringExtra(ProfileActivity.EXTRA_NAME);

        mapView      = findViewById(R.id.mapView);
        svSearch     = findViewById(R.id.svSearch);
        btnNewReview = findViewById(R.id.btnNewReview);
        btnBack      = findViewById(R.id.btnBack);

        // Inicializar el mapa
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        MapController ctrl = (MapController) mapView.getController();
        ctrl.setZoom(18.0);
        GeoPoint start = new GeoPoint(selectedLat, selectedLon);
        ctrl.setCenter(start);
        addMarker(start, "Inicio", false);

        // Cargar reseñas existentes
        loadReviews();

        // Expandir SearchView al tocar cualquier parte del cuadro
        svSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Primero, dispara el click accesible
                v.performClick();
                // Después, abre el SearchView
                svSearch.setIconified(false);
                svSearch.requestFocus();
            }
            // devolvemos false para que siga procesando normalmente
            return false;
        });


        // Buscar al enviar texto
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hasRetried = false;
                searchLocation(query.trim());
                svSearch.clearFocus();
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Botón “Nueva reseña”
        btnNewReview.setOnClickListener(v -> {
            Intent i = new Intent(this, ReviewActivity.class);
            i.putExtra(ProfileActivity.EXTRA_NAME, username);
            i.putExtra("lat", selectedLat);
            i.putExtra("lon", selectedLon);
            reviewLauncher.launch(i);
        });

        // Botón “Volver”
        btnBack.setOnClickListener(v -> finish());
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

                    // Contador de cuántas reseñas hay en cada lat,lon exacto
                    Map<String, Integer> contador = new HashMap<>();

                    for (Review r : resp.body()) {
                        double lat = r.getLat();
                        double lon = r.getLon();
                        String key = lat + "," + lon;
                        int n = contador.getOrDefault(key, 0);
                        contador.put(key, n + 1);

                        // si ya hay al menos una, desplazamos
                        if (n > 0) {
                            // radio de 0.0001 grados (~11 m)
                            double radio = 0.0001;
                            // ángulo en radianes: separado cada uno 45°
                            double angulo = Math.toRadians(n * 45);
                            lat += radio * Math.sin(angulo);
                            lon += radio * Math.cos(angulo);
                        }

                        GeoPoint p = new GeoPoint(lat, lon);
                        addMarker(p, r.getTitulo() + "\n" + r.getUsuario(), true);
                    }

                    mapView.invalidate();
                });
            }
            @Override public void onFailure(Call<List<Review>> call, Throwable t) { }
        });
    }


    private void addMarker(GeoPoint point, String title, boolean red) {
        if (!red) return;   // <-- aquí cortas cualquier llamada con false

        Marker m = new Marker(mapView);
        m.setPosition(point);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setTitle(title);

        // Dibujar pin rojo escalado...
        Drawable d = AppCompatResources.getDrawable(this, R.drawable.ic_marker_red);
        int sizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        d.setBounds(0,0,sizePx,sizePx);
        d.draw(canvas);
        m.setIcon(new BitmapDrawable(getResources(), bmp));

        mapView.getOverlays().add(m);
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
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(FullMapActivity.this,
                                "Error en búsqueda", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response resp) throws IOException {
                if (!resp.isSuccessful()) return;
                String body = resp.body().string();
                JsonArray arr = JsonParser.parseString(body).getAsJsonArray();
                if (arr.isEmpty() && !hasRetried) {
                    hasRetried = true;
                    runOnUiThread(() ->
                            Toast.makeText(FullMapActivity.this,
                                    "No encontrado \"" + texto + "\"", Toast.LENGTH_SHORT).show());
                    return;
                }
                if (!arr.isEmpty()) {
                    JsonObject place = arr.get(0).getAsJsonObject();
                    selectedLat = place.get("lat").getAsDouble();
                    selectedLon = place.get("lon").getAsDouble();
                    String disp = place.has("display_name")
                            ? place.get("display_name").getAsString()
                            : texto;
                    runOnUiThread(() -> {
                        MapController c = (MapController) mapView.getController();
                        GeoPoint p = new GeoPoint(selectedLat, selectedLon);
                        c.setCenter(p);
                        c.setZoom(18.0);
                        mapView.getOverlays().clear();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
