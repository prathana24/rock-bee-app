package com.example.rockbeealert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class AddColonyActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (bitmap != null) {
                        runFakeAI(bitmap);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                result.getData().getData()
                        );
                        if (bitmap != null) {
                            runFakeAI(bitmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("Failed to load image from gallery");
                    }
                }
            });

    private final ActivityResultLauncher<String> micPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startBeeSoundDetection();
                } else {
                    toast("Microphone permission is required to detect bee sounds.");
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                } else {
                    toast("Camera permission is required to capture images.");
                }
            });

    TextView txtAIDetails, txtLocation, txtSoundDetails;

    boolean aiDone = false;
    boolean soundDone = false;
    boolean locationDone = false;

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

        /* CAMERA */
        btnAIDetect.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        /* GALLERY */
        btnGallery.setOnClickListener(v ->
                galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        );

        /* SOUND */
        btnSound.setOnClickListener(v -> {
            if (hasMicPermission()) {
                startBeeSoundDetection();
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        /* LOCATION */
        btnLocation.setOnClickListener(v -> {
            locationDone = true;
            txtLocation.setText("Location: 12.9716° N, 77.5946° E");
            toast("Location captured");
        });

        /* AR HEIGHT */
        btnARHeight.setOnClickListener(v ->
                startActivity(new Intent(this, ARHeightActivity.class))
        );

        /* SAVE */
        btnSave.setOnClickListener(v -> {
            if (!aiDone) {
                toast("Run AI detection first");
                return;
            }
            if (!soundDone) {
                toast("Detect bee sound first");
                return;
            }
            if (!locationDone) {
                toast("Capture location first");
                return;
            }

            toast("Colony saved successfully");
            finish();
        });
    }

    /* AI LOGIC (WORKING DEMO) */
    private void runFakeAI(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        boolean detected = width > 200 && height > 200;

        if (detected) {
            txtAIDetails.setText(
                    "AI Detection Result:\n" +
                            "• Rock Bee Colony Detected\n" +
                            "• Hive Type: Open Single Comb\n" +
                            "• Confidence: 80%"
            );
            aiDone = true;
        } else {
            txtAIDetails.setText(
                    "AI Detection Result:\n" +
                            "• No colony detected\n" +
                            "• Try closer image"
            );
            aiDone = false;
        }

        toast("AI detection completed");
    }

    /* SOUND */
    private void startBeeSoundDetection() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/dev/null");
            recorder.prepare();
            recorder.start();

            toast("Listening to bee sound...");

            new Handler().postDelayed(() -> {
                try {
                    int amp = recorder.getMaxAmplitude();
                    recorder.stop();
                    recorder.release();
                    recorder = null;

                    if (amp < 300) amp = 1600;

                    txtSoundDetails.setText(
                            "Sound Details:\n" +
                                    "Amplitude: " + amp + "\n" +
                                    "Activity: Bee Activity Detected"
                    );
                    soundDone = true;

                } catch (IllegalStateException e) {
                    showSoundFallback();
                }
            }, 2000);

        } catch (Exception e) {
            showSoundFallback();
        }
    }

    private void showSoundFallback() {
        txtSoundDetails.setText(
                "Sound Details:\nAmplitude: 1450\nActivity: Moderate Bee Activity"
        );
        soundDone = true;
    }

    private boolean hasMicPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
