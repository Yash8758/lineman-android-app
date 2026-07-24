package com.bsnl.lineman;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LinemanMainActivity";
    private static final int PERM_REQUEST_CODE = 8871;

    private WebView webView;
    private ImageButton btnSettings;
    private String fingerprint;
    private String serverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("LinemanPrefs", MODE_PRIVATE);
        fingerprint = prefs.getString("fingerprint", null);
        if (fingerprint == null) {
            fingerprint = "LM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            prefs.edit().putString("fingerprint", fingerprint).apply();
        }

        serverUrl = prefs.getString("server_url", "http://127.0.0.1:5000");

        webView = findViewById(R.id.webView);
        btnSettings = findViewById(R.id.btnSettings);

        setupWebView();
        setupSettingsButton();

        checkAndRequestPermissions();
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        loadLinemanApp();
    }

    private void loadLinemanApp() {
        String url = serverUrl + "/pwa/lineman";
        Log.d(TAG, "Loading URL: " + url);
        webView.loadUrl(url);
    }

    private void setupSettingsButton() {
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showServerUrlDialog();
            }
        });
    }

    private void showServerUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Settings");
        builder.setMessage("Enter your Fiber Manager Server IP / URL (e.g. http://192.168.1.5:5000 or Tailscale IP):");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(serverUrl);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newUrl = input.getText().toString().trim();
                if (!newUrl.isEmpty()) {
                    if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                        newUrl = "http://" + newUrl;
                    }
                    serverUrl = newUrl;
                    getSharedPreferences("LinemanPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("server_url", serverUrl)
                            .apply();

                    Toast.makeText(MainActivity.this, "Server URL updated!", Toast.LENGTH_SHORT).show();
                    loadLinemanApp();
                    startTrackingService();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkAndRequestPermissions() {
        List<String> needed = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERM_REQUEST_CODE);
        } else {
            requestBackgroundLocationPermission();
        }
    }

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERM_REQUEST_CODE + 1);
            } else {
                startTrackingService();
            }
        } else {
            startTrackingService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE) {
            requestBackgroundLocationPermission();
        } else if (requestCode == PERM_REQUEST_CODE + 1) {
            startTrackingService();
        }
    }

    private void startTrackingService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Log.d(TAG, "LocationService started from MainActivity");
    }
}
