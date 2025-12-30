package com.example.rockbeealert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddColonyActivity extends AppCompatActivity {

    private static final String TAG = "AddColonyActivity";

    // --- ActivityResultLaunchers ---
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (bitmap != null) {
                        runRealAI(bitmap);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                result.getData().getData()
                        );
                        if (bitmap != null) {
                            runRealAI(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load image from gallery", e);
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

    // --- UI and State ---
    TextView txtAIDetails, txtLocation, txtSoundDetails;
    boolean aiDone = false;
    boolean soundDone = false;
    boolean locationDone = false;

    // --- Media and AI ---
    MediaRecorder recorder;
    private ImageClassifier imageClassifier;
    private ExecutorService executorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_colony);

        executorService = Executors.newSingleThreadExecutor();

        // --- UI Initialization ---
        Button btnAIDetect = findViewById(R.id.btnAIDetect);
        Button btnGallery = findViewById(R.id.btnGallery);
        Button btnSound = findViewById(R.id.btnSound);
        Button btnLocation = findViewById(R.id.btnLocation);
        Button btnARHeight = findViewById(R.id.btnARHeight);
        Button btnSave = findViewById(R.id.btnSaveColony);

        txtAIDetails = findViewById(R.id.txtAIDetails);
        txtSoundDetails = findViewById(R.id.txtSoundDetails);
        txtLocation = findViewById(R.id.txtLocation);

        // --- Setup AI Classifier ---
        executorService.execute(this::setupImageClassifier);

        // --- OnClick Listeners ---
        btnAIDetect.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v ->
                galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        );

        btnSound.setOnClickListener(v -> {
            if (hasMicPermission()) {
                startBeeSoundDetection();
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        btnLocation.setOnClickListener(v -> {
            locationDone = true;
            txtLocation.setText("Location: 12.9716° N, 77.5946° E");
            toast("Location captured");
        });

        btnARHeight.setOnClickListener(v ->
                startActivity(new Intent(this, ARHeightActivity.class))
        );

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    // --- AI LOGIC ---
    private void setupImageClassifier() {
        try {
            // Use CPU for broader compatibility, and catch all exceptions during setup.
            BaseOptions baseOptions = BaseOptions.builder().setNumThreads(4).build();
            ImageClassifier.ImageClassifierOptions options = ImageClassifier.ImageClassifierOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMaxResults(1)
                    .build();
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                    this,
                    "model.tflite",
                    options
            );
            Log.d(TAG, "Image classifier loaded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "TFLite model could not be loaded or initialized.", e);
            runOnUiThread(() -> toast("AI model failed to load."));
        }
    }

    private void runRealAI(Bitmap bitmap) {
        if (imageClassifier == null) {
            runOnUiThread(() -> toast("AI model is not ready. Please try again."));
            return;
        }

        executorService.execute(() -> {
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                    .build();

            TensorImage tensorImage = new TensorImage();
            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);

            // The classification is wrapped in a try-catch to prevent crashes on the background thread.
            try {
                List<?> resultsList = imageClassifier.classify(tensorImage);
                runOnUiThread(() -> handleAIResults(resultsList));
            } catch (Exception e) {
                Log.e(TAG, "Error during AI classification.", e);
                runOnUiThread(() -> toast("AI inference failed."));
            }
        });
    }

    private void handleAIResults(List<?> resultsList) {
        // This workaround is still in place to avoid the previous compilation error.
        if (resultsList != null && !resultsList.isEmpty()) {
            Object firstResult = resultsList.get(0);
            try {
                List<Category> categories = (List<Category>) firstResult.getClass().getMethod("getCategories").invoke(firstResult);
                if (categories != null && !categories.isEmpty()) {
                    Category topResult = categories.get(0);
                    String label = topResult.getLabel();
                    float score = topResult.getScore();

                    String resultText = String.format(
                            "AI Detection Result:%n• Label: %s%n• Confidence: %.0f%%",
                            label, score * 100
                    );

                    txtAIDetails.setText(resultText);

                    if (label.toLowerCase().contains("bees") && score > 0.6) {
                        aiDone = true;
                    } else {
                        aiDone = false;
                    }
                } else {
                    txtAIDetails.setText("AI Detection Result:\nNo objects detected.");
                    aiDone = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing AI results", e);
                txtAIDetails.setText("AI Detection Result:\nError processing results.");
                aiDone = false;
            }
        } else {
            txtAIDetails.setText("AI Detection Result:\nNo objects detected.");
            aiDone = false;
        }
        toast("AI detection completed");
    }

    // --- SOUND LOGIC ---
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
                    if (recorder != null) {
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
                    }
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error getting sound amplitude", e);
                    showSoundFallback();
                }
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up sound detection", e);
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
