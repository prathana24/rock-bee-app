package com.example.rockbeealert;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView txtProfile = findViewById(R.id.txtProfile);

        txtProfile.setText(
                "User Profile\n\n" +
                        "Name: User\n" +
                        "Role: Field Observer\n" +
                        "Reports Submitted: 5\n" +
                        "Status: Active"
        );
    }
}
