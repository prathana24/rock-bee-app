package com.example.rockbeealert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Button btnOpenMap = findViewById(R.id.btnOpenMap);

        btnOpenMap.setOnClickListener(v -> {
            // Open real Google Maps with colony search
            Uri uri = Uri.parse("geo:0,0?q=rock+bee+colony");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });
    }
}
