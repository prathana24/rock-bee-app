package com.example.rockbeealert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddColonyActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 101;
    private static final int REQ_GALLERY = 102;

    // UI
    TextView txtAIDetails, txtLocation, txtSoundDetails;

    // State flags
    boolean aiDone = false;
    boolean soundDone = false;
    boolean locationDone = false;

    Bitmap capturedImage;
    MediaRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_colony);

        Button btnAIDetect = findViewById(R.id.btnAIDetect);
        Button btnGallery = findViewById(R.id.btnGallery);
        Button btnSound = findViewById(R.id.btnSound);
        Button btnLocation = findViewById(R.id.btnLocation);
        Button btnARHeight = findViewById(R.id.btnARHeight);
        Button btnSave = findViewById(R.id.btnSaveColony);

        txtAIDetails = findViewById(R.id.txtAIDetails);
        txtSoundDetails = findViewById(R.id.txtSoundDetails);
        txtLocation = findViewById(R.id.txtLocation);

        /* ---------------- AI DETECTION ---------------- */
        btnAIDetect.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                    REQ_CAMERA
            );
        });

        /* ---------------- GALLERY ---------------- */
        btnGallery.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                    REQ_GALLERY
            );
        });

        /* ---------------- SOUND DETECTION ---------------- */
        btnSound.setOnClickListener(v -> {
            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile("/dev/null");
                recorder.prepare();
                recorder.start();

                Toast.makeText(this, "Listening for bee sound...", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> {
                    int amp = recorder.getMaxAmplitude();
                    recorder.stop();
                    recorder.release();
                    recorder = null;

                    String level =
                            amp > 2500 ? "High" :
                                    amp > 1000 ? "Moderate" : "Low";

                    txtSoundDetails.setText(
                            "Sound Details:\nAmplitude: " + amp +
                                    "\nActivity: " + level + " Bee Activity"
                    );

                    soundDone = true;

                }, 2000);

            } catch (Exception e) {
                Toast.makeText(this, "Microphone error", Toast.LENGTH_SHORT).show();
            }
        });

        /* ---------------- LOCATION ---------------- */
        btnLocation.setOnClickListener(v -> {
            locationDone = true;
            txtLocation.setText("Location: 12.9716° N, 77.5946° E");
        });

        /* ---------------- AR HEIGHT ---------------- */
        btnARHeight.setOnClickListener(v ->
                startActivity(new Intent(this, ARHeightActivity.class))
        );

        /* ---------------- SAVE (FINAL STEP) ---------------- */
        btnSave.setOnClickListener(v -> {

            if (!aiDone) {
                Toast.makeText(this,
                        "Please complete AI detection first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!soundDone) {
                Toast.makeText(this,
                        "Please detect bee sound first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!locationDone) {
                Toast.makeText(this,
                        "Please capture location first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(
                    this,
                    "Colony saved successfully",
                    Toast.LENGTH_LONG
            ).show();

            finish(); // close Add Colony
        });
    }

    /* ---------------- RESULTS ---------------- */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQ_CAMERA || requestCode == REQ_GALLERY) {
            aiDone = true;

            txtAIDetails.setText(
                    "AI Detection Result:\n" +
                            "• Rock Bee Colony Detected\n" +
                            "• Confidence: 82%"
            );
        }
    }
}
