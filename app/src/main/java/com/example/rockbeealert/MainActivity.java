package com.example.rockbeealert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAddColony = findViewById(R.id.btnAddColony);
        btnAddColony.setOnClickListener(v ->
                startActivity(new Intent(this, AddColonyActivity.class))
        );

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
            }


            if (id == R.id.nav_emergency) {
                startActivity(new Intent(this, EmergencyActivity.class));
            } else if (id == R.id.nav_learn) {
                startActivity(new Intent(this, LearnActivity.class));
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            }

            return true;
        });
    }
}
