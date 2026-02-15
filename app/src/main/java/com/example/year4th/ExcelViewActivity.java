package com.example.year4th;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelViewActivity extends AppCompatActivity {
    ListView listView;
    File file;
    AppCompatButton saveExcelBtn,shareExcelBtn;
    List<String> presentStudents = new ArrayList<>();
    ArrayAdapter<String> adapter;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String pathToGetData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_excel_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        listView = findViewById(R.id.listView);
        saveExcelBtn=findViewById(R.id.saveExcelBtn);
        shareExcelBtn=findViewById(R.id.shareExcelBtn);


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, presentStudents);
        listView.setAdapter(adapter);

        fetchAttendance();

        saveExcelBtn.setOnClickListener(v -> {
            List<Map<String, Object>> attendanceList = new ArrayList<>();

            for (String student : presentStudents) {
                Map<String, Object> record = new HashMap<>();
                record.put("name", student);
                record.put("status", "Present"); // ✅ since this is attendance
                record.put("date", getIntent().getStringExtra("date")); // ✅ from intent
                attendanceList.add(record);
            }

            exportToExcel(attendanceList);
        });
        shareExcelBtn.setOnClickListener(v -> {

            if (file == null || !file.exists()) {

                if (presentStudents.isEmpty()) {
                    Toast.makeText(this, "No data to export!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create attendance list again
                List<Map<String, Object>> attendanceList = new ArrayList<>();

                for (String student : presentStudents) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("name", student);
                    record.put("status", "Present");
                    attendanceList.add(record);
                }

                exportToExcel(attendanceList);
            }

            shareExcelFile(file);
        });

    }

    private void fetchAttendance() {
        pathToGetData = getIntent().getStringExtra("pathToGetStudentNames");

        if (pathToGetData == null) {
            Toast.makeText(this, "Subject not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] pathDb = pathToGetData.split("/");

        db.collection("users").document(auth.getUid())
                .collection("Course").document(pathDb[0])
                .collection("Department").document(pathDb[1])
                .collection("Year").document(pathDb[2])
                .collection("Section").document(pathDb[3])
                .collection("Subjects").document(pathDb[4])
                .collection("Lecture").document(pathDb[5])
                .collection("dates").document(pathDb[6])
                .collection("studentNames")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    presentStudents.clear(); // ✅ avoid duplicates
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        presentStudents.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load student names", Toast.LENGTH_SHORT).show()
                );
    }

    private void exportToExcel(List<Map<String, Object>> attendanceList) {
        try {
            // Create workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Student Name", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Attendance rows
            int rowIndex = 1;
            for (Map<String, Object> record : attendanceList) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(record.get("name") != null ? record.get("name").toString() : "");
                row.createCell(1).setCellValue(record.get("status") != null ? record.get("status").toString() : "");
            }

            // ✅ Set column widths dynamically (no autoSizeColumn → avoids AWT crash)
            for (int i = 0; i < headers.length; i++) {
                int maxLength = headers[i].length();
                for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                    Row row = sheet.getRow(j);
                    if (row != null && row.getCell(i) != null) {
                        int length = row.getCell(i).toString().length();
                        if (length > maxLength) {
                            maxLength = length;
                        }
                    }
                }
                sheet.setColumnWidth(i, (maxLength + 2) * 256); // padding +2
            }

            // Save to Downloads folder
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            file = new File(downloadsDir, "Attendance.xlsx");
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(this, "Excel exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    private void shareExcelFile(File file) {
        if (file == null || !file.exists()) {
            Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Share Excel File"));
    }

//    private void shareExcelFile(File file) {
//        try {
//            Uri fileUri = FileProvider.getUriForFile(
//                    this,
//                    getApplicationContext().getPackageName() + ".provider",
//                    file
//            );
//
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(Intent.createChooser(intent, "Share Excel File"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }



}