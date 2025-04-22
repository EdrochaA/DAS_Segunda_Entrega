package com.example.etravels.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.etravels.R;

public class MapActivity extends AppCompatActivity {
    // Intervalo de notificaciones: 30 s
    private static final long   INTERVAL_MS      = 30_000L;
    private static final String CHANNEL_ID       = "periodic_channel";
    private static final int    NOTIF_ID         = 3001;
    private static final int    PERM_REQUEST_CODE = 1001;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            showNotification("eTravels", "Estás inactivo");
            handler.postDelayed(this, INTERVAL_MS);
        }
    };

    // Datos de sesión
    private String name, phone, email, photoUrl;
    private int    userId;

    private final ActivityResultLauncher<Intent> fullMapLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> { /* manejar resultados si hace falta */ }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 1) Leer extras de LoginActivity
        Intent intent = getIntent();
        name     = intent.getStringExtra(ProfileActivity.EXTRA_NAME);
        phone    = intent.getStringExtra(ProfileActivity.EXTRA_PHONE);
        email    = intent.getStringExtra(ProfileActivity.EXTRA_EMAIL);
        photoUrl = intent.getStringExtra(ProfileActivity.EXTRA_PHOTO_URL);
        userId   = intent.getIntExtra   (ProfileActivity.EXTRA_ID, -1);

        // 2) Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        PERM_REQUEST_CODE
                );
            }
        }

        // 3) Crear canal de notificaciones
        createNotificationChannel();

        // 4) Iniciar el bucle de notificaciones
        handler.post(ticker);

        // 5) Inicializar UI y listeners
        ImageView imgProfile = findViewById(R.id.imgProfile);
        ImageView imgMap     = findViewById(R.id.imgMap);

        imgProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra(ProfileActivity.EXTRA_NAME,      name);
            i.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            i.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            i.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            i.putExtra(ProfileActivity.EXTRA_ID,        userId);
            startActivity(i);
        });

        imgMap.setOnClickListener(v -> {
            Intent i = new Intent(this, FullMapActivity.class);
            i.putExtra(ProfileActivity.EXTRA_NAME,      name);
            i.putExtra(ProfileActivity.EXTRA_PHONE,     phone);
            i.putExtra(ProfileActivity.EXTRA_EMAIL,     email);
            i.putExtra(ProfileActivity.EXTRA_PHOTO_URL, photoUrl);
            i.putExtra(ProfileActivity.EXTRA_ID,        userId);
            fullMapLauncher.launch(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(ticker);
    }

    /** Crear canal de notificación en Android 8+ */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones periódicas",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Recordatorios cada 30 segundos");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    /**
     * Muestra notificación en la barra de estado,
     * tras verificar permiso en Android 13+.
     */
    private void showNotification(String title, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            // No tenemos permiso: abortar
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this)
                .notify(NOTIF_ID, builder.build());
    }

    /** Gestionar resultado del permiso POST_NOTIFICATIONS */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE) {
            if (grantResults.length == 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Usuario negó el permiso: podemos parar el ticker si queremos
                handler.removeCallbacks(ticker);
            }
        }
    }
}
