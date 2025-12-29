package com.example.rockbeealert;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EmergencyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // UI references
        Button btnSOS = findViewById(R.id.btnSOS);
        Button btnHospital = findViewById(R.id.btnHospital);
        TextView txtInfo = findViewById(R.id.txtInfo);

        // 📞 SOS – opens dialer (SAFE, no CALL permission needed)
        btnSOS.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:108"));
            startActivity(dialIntent);
        });

        // 🏥 Nearby hospitals using Google Maps
        btnHospital.setOnClickListener(v -> {
            Intent mapIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=hospital")
            );
            mapIntent.setPackage("com.google.android.apps.maps");

            try {
                startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        this,
                        "Maps app not found",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // 🐝 First-aid info
        txtInfo.setText(
                "Bee Sting – Seek Help Immediately If:\n\n" +
                        "• Swelling of face, lips, or throat\n" +
                        "• Difficulty breathing\n" +
                        "• Dizziness or fainting\n" +
                        "• Multiple stings\n\n" +
                        "⚠ Do NOT disturb the colony.\n" +
                        "📞 Use SOS or reach nearest hospital."
        );
    }
}
