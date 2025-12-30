package com.example.rockbeealert;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    // Simple data class to hold user profile information
    private static class UserProfile {
        final String name;
        final String role;
        final int reportsSubmitted;
        final String status;

        UserProfile(String name, String role, int reportsSubmitted, String status) {
            this.name = name;
            this.role = role;
            this.reportsSubmitted = reportsSubmitted;
            this.status = status;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- Get UI elements ---
        TextView userName = findViewById(R.id.user_name);
        TextView userRole = findViewById(R.id.user_role);
        TextView reportsCount = findViewById(R.id.reports_count);
        TextView userStatus = findViewById(R.id.user_status);
        Button editProfileButton = findViewById(R.id.edit_profile_button);
        Button logoutButton = findViewById(R.id.logout_button);

        // --- Load user data ---
        UserProfile user = new UserProfile("P. Saha", "Field Observer", 5, "Active");

        // --- Populate the UI ---
        userName.setText(user.name);
        userRole.setText(user.role);
        reportsCount.setText(String.valueOf(user.reportsSubmitted));
        userStatus.setText(user.status);

        // --- Add button functionality ---
        editProfileButton.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to an EditProfileActivity or show a dialog
        });

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement logout logic (e.g., clear session, navigate to login)
            finish(); // Close the profile activity
        });
    }
}
