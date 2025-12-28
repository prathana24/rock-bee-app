package com.example.rockbeealert;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LearnActivity extends AppCompatActivity {

    int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        TextView txtScore = findViewById(R.id.txtScore);

        Button btn1 = findViewById(R.id.btnOpt1);
        Button btn2 = findViewById(R.id.btnOpt2);
        Button btn3 = findViewById(R.id.btnOpt3);

        // Correct answer
        btn1.setOnClickListener(v -> {
            score += 10;
            txtScore.setText("Score: " + score);
            Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show();
        });

        // Wrong answers
        btn2.setOnClickListener(v ->
                Toast.makeText(this, "Wrong answer ❌", Toast.LENGTH_SHORT).show()
        );

        btn3.setOnClickListener(v ->
                Toast.makeText(this, "Wrong answer ❌", Toast.LENGTH_SHORT).show()
        );
    }
}
