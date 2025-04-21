package com.example.etravels.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;              // ← Import correcto
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.etravels.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.MapController;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FullMapActivity extends AppCompatActivity {
    private MapView mapView;
    private SearchView svSearch;                        // ← AndroidX SearchView
    private Button btnBack;
    private OkHttpClient http = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configura osmdroid
        Configuration.getInstance()
                .setUserAgentValue(getPackageName());
        Configuration.getInstance()
                .load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_full_map);

        mapView   = findViewById(R.id.mapView);
        svSearch  = findViewById(R.id.svSearch);         // ← Ahora coincide con el import
        btnBack   = findViewById(R.id.btnBack);

        // Inicializa el mapa
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        MapController controller = (MapController) mapView.getController();
        controller.setZoom(18.0);

        // Marcador fijo en Plaza Moyúa
        GeoPoint moyua = new GeoPoint(43.2620, -2.9340);
        controller.setCenter(moyua);
        Marker moyuaMarker = new Marker(mapView);
        moyuaMarker.setPosition(moyua);
        moyuaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        moyuaMarker.setTitle("Plaza Moyúa");
        mapView.getOverlays().add(moyuaMarker);

        // Listener de búsqueda
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                svSearch.clearFocus();
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        svSearch.setOnClickListener(v -> {
            svSearch.setIconified(false);       // Despliega el campo
            svSearch.requestFocus();            // Pide el foco
        });


        // Botón Volver
        btnBack.setOnClickListener(v -> finish());
    }

    private void searchLocation(String texto) {
        String url = "https://nominatim.openstreetmap.org/search?q="
                + Uri.encode(texto)
                + "&format=json&limit=1";
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", getPackageName())
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(FullMapActivity.this, "Error en búsqueda", Toast.LENGTH_SHORT).show()
                );
            }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) return;
                String body = resp.body().string();
                try {
                    JsonArray arr = JsonParser.parseString(body).getAsJsonArray();
                    if (arr.size() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(FullMapActivity.this, "No encontrado", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }
                    JsonObject place = arr.get(0).getAsJsonObject();
                    double lat = place.get("lat").getAsDouble();
                    double lon = place.get("lon").getAsDouble();
                    String name = place.has("display_name")
                            ? place.get("display_name").getAsString()
                            : texto;

                    runOnUiThread(() -> {
                        GeoPoint point = new GeoPoint(lat, lon);
                        MapController ctrl = (MapController) mapView.getController();
                        ctrl.setZoom(18.0);
                        ctrl.setCenter(point);

                        Marker m = new Marker(mapView);
                        m.setPosition(point);
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        m.setTitle(name);
                        mapView.getOverlays().add(m);
                        mapView.invalidate();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() ->
                            Toast.makeText(FullMapActivity.this, "Error parseando resultado", Toast.LENGTH_SHORT).show()
                    );
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
