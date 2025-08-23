package com.example.year4th;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;

    private Uri photoUri;
    private ImageView imagePreview;
    AppCompatButton clickImage, btnUpload;
    private Uri imageUri;
    private ListView recognizedListView;
    private List<String> recognizedNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    TextView totalPresentStudents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        totalPresentStudents=findViewById(R.id.totalPresentStudents);
        imagePreview = findViewById(R.id.imagePreview);
        clickImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        recognizedListView = findViewById(R.id.recognizedListView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recognizedNames);
        recognizedListView.setAdapter(adapter);

        clickImage.setOnClickListener(v -> showImageSourceDialog());

        btnUpload.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToServer(imageUri);
            } else {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            imagePreview.setImageURI(imageUri);
        }else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }
    private void uploadImageToServer(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] byteArray = stream.toByteArray();

            // Send image to server
            sendToFlaskServer(byteArray);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void sendToFlaskServer(byte[] imageBytes) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "photo.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2:10000/recognize") // Use 10.0.2.2    for emulator
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Error: ", e);
                runOnUiThread(() ->
                        Toast.makeText(CameraActivity.this, "API call failed", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonString = response.body().string();
                    try {

                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONArray namesArray = jsonObject.getJSONArray("recognized_faces");

                        recognizedNames.clear();
                        for (int i = 0; i < namesArray.length(); i++) {
                            recognizedNames.add(namesArray.getString(i));
                        }

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            saveToFirebase(recognizedNames);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveToFirebase(List<String> names) {
        String pathForDb = getIntent().getStringExtra("pathForDb");



        if (pathForDb == null) {
            Toast.makeText(this, "Path not found!", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] pathDb = pathForDb.split("/");
        if (pathDb.length != 6) {
            return;
        }


        totalPresentStudents.setText(null);
        String str="Total Present Students = "+(String.valueOf(names.size()));
        totalPresentStudents.setText(str);
        if (pathForDb == null) {
            Toast.makeText(this, "Subject not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get today's date
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());


        for (String name : names) {
            Map<String, Object> attendanceData = new HashMap<>();
            attendanceData.put("status", "present");
            db.collection("users").document(auth.getUid())
                    .collection("Course").document(pathDb[0])
                    .collection("Department").document(pathDb[1])
                    .collection("Year").document(pathDb[2])
                    .collection("Section").document(pathDb[3])
                    .collection("Subjects").document(pathDb[4])
                    .collection("Lecture").document(pathDb[5])
                    .collection("dates").document(date)
                    .collection("studentNames").document(name)
                    .set(attendanceData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(CameraActivity.this, "Attendance marked", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CameraActivity.this, "Attendance Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}
