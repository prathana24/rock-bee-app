package com.example.rockbeealert;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("Screen Loaded Successfully");
        tv.setTextSize(22f);
        tv.setPadding(40, 40, 40, 40);

        setContentView(tv);
    }
}
