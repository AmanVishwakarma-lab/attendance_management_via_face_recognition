package com.example.year4th;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelViewActivity extends AppCompatActivity {
    ListView listView;
    AppCompatButton btnExport;
    List<String> presentStudents = new ArrayList<>();
    ArrayAdapter<String> adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String subject, date;
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
        btnExport=findViewById(R.id.excelBtn);

        subject = getIntent().getStringExtra("subject");
        date = getIntent().getStringExtra("date");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, presentStudents);
        listView.setAdapter(adapter);

        fetchAttendance();

        btnExport.setOnClickListener(v -> {
            exportToExcel();
        });
    }

    private void fetchAttendance() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseAuth auth=FirebaseAuth.getInstance();
        db.collection("users")
                .document(auth.getUid())
                .collection("subjects")
                .document(subject)
                .collection("dates")
                .document(date)
                .collection("studentNames")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        presentStudents.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load student names", Toast.LENGTH_SHORT).show());
    }

    private void exportToExcel() {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Subject: " + subject);
            header.createCell(1).setCellValue("Date: " + date);

            for (int i = 0; i < presentStudents.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(presentStudents.get(i));
                row.createCell(1).setCellValue("Present");
            }

            File dir = new File(getExternalFilesDir(null), "AttendanceSheets");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, subject + "_" + date + ".xlsx");
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            workbook.close();

            Toast.makeText(this, "Excel saved at: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            shareExcelFile(file); // 🔄 Call share method here

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void shareExcelFile(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Excel File"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to share file", Toast.LENGTH_SHORT).show();
        }
    }



}