package com.example.image2textapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class Activity_scanner extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageView imageView;
    private TextView textViewResult;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scanner);

        imageView = findViewById(R.id.imageView);
        textViewResult = findViewById(R.id.textViewResult);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnCaptureImage = findViewById(R.id.btnCaptureImage);
        Button btnExtractText = findViewById(R.id.btnExtractText);

        // Click listeners
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnCaptureImage.setOnClickListener(v -> captureImage());
        btnExtractText.setOnClickListener(v -> processImage());

        // Request Camera Permission
        requestCameraPermission();
    }

    // ✅ Select Image from Gallery
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    // ✅ Capture Image from Camera
    private void captureImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    // ✅ Handle Image Selection or Capture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE) {
                Uri imageUri = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST) {
                imageBitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(imageBitmap);
            }
        }
    }

    // ✅ Process Image with ML Kit
    private void processImage() {
        if (imageBitmap == null) {
            Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    StringBuilder extractedText = new StringBuilder();
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        extractedText.append(block.getText()).append("\n");
                    }
                    textViewResult.setText(extractedText.toString());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show());
    }

    // ✅ Request Camera Permission
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    // ✅ Handle Permission Request Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}