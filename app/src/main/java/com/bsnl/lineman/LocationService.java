package com.bsnl.lineman;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationService extends Service {
    private static final String TAG = "LinemanLocationService";
    private static final String CHANNEL_ID = "channel_lineman_tracking";
    private static final int NOTIF_ID = 9912;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private OkHttpClient httpClient;
    private String fingerprint;
    private String serverUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService created");
        httpClient = new OkHttpClient();

        SharedPreferences prefs = getSharedPreferences("LinemanPrefs", MODE_PRIVATE);
        fingerprint = prefs.getString("fingerprint", null);
        if (fingerprint == null) {
            fingerprint = "LM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            prefs.edit().putString("fingerprint", fingerprint).apply();
        }

        serverUrl = prefs.getString("server_url", "http://127.0.0.1:5000");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        sendPingToServer(location);
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = buildNotification("📡 BSNL Live Location Active");
        startForeground(NOTIF_ID, notification);

        startLocationUpdates();
        return START_STICKY; // Re-create service if killed by system
    }

    private void startLocationUpdates() {
        try {
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000)
                    .setMinUpdateIntervalMillis(10000)
                    .setWaitForAccurateLocation(false)
                    .build();

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.d(TAG, "Location updates requested (15s interval)");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException requesting location updates: " + e.getMessage());
        }
    }

    private void sendPingToServer(Location loc) {
        Log.d(TAG, "Location ping: " + loc.getLatitude() + ", " + loc.getLongitude() + " ±" + loc.getAccuracy() + "m");

        try {
            JSONObject json = new JSONObject();
            json.put("fingerprint", fingerprint);
            json.put("latitude", loc.getLatitude());
            json.put("longitude", loc.getLongitude());
            json.put("accuracy_m", loc.getAccuracy());
            json.put("is_active", 1);

            SharedPreferences prefs = getSharedPreferences("LinemanPrefs", MODE_PRIVATE);
            String url = prefs.getString("server_url", "http://127.0.0.1:5000") + "/pwa/api/lineman/location";

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.w(TAG, "Ping failed (offline): " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Ping uploaded successfully");
                    }
                    response.close();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error building ping payload: " + e.getMessage());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lineman Live Location Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Keeps live location tracking active in the background");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String text) {
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BSNL Field Engineer")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        Log.d(TAG, "LocationService destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
