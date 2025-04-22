package com.example.etravels.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.etravels.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationForegroundService extends Service {
    private static final String CHANNEL_ID = "location_channel";
    private static final int    NOTIF_ID   = 1;

    private FusedLocationProviderClient fusedClient;
    private LocationRequest             locationRequest;
    private LocationCallback            locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Inicializamos el cliente de location
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // 2. Creamos la petición de ubicación
        locationRequest = LocationRequest.create()
                .setInterval(10_000)               // cada 10 segundos
                .setFastestInterval(5_000)         // como máximo cada 5 segundos
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 3. Definimos el callback que recibirá las actualizaciones
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                double lat = result.getLastLocation().getLatitude();
                double lon = result.getLastLocation().getLongitude();
                // TODO: aquí podrías enviar un broadcast, guardar en BD, etc.
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 4. Creamos el canal de notificación y la notific. persistente
        createNotificationChannel();
        Intent tapIntent = new Intent(this, MapActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("eTravels")
                .setContentText("Rastreo de ubicación activo")
                .setSmallIcon(R.drawable.ic_notification)  // tu icono
                .setContentIntent(pi)
                .setOngoing(true);

        startForeground(NOTIF_ID, notif.build());

        // 5. Solicitar actualizaciones de ubicación
        fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );

        // Si el sistema mata el servicio, lo reviva
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Parar las actualizaciones
        fusedClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No es bound service
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID,
                    "Servicio de ubicación",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager mgr = getSystemService(NotificationManager.class);
            mgr.createNotificationChannel(chan);
        }
    }
}
