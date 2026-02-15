package com.example.year4th;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.year4th.databinding.ActivityHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

public class HistoryActivity extends AppCompatActivity {
    FirebaseAuth auth;
    ActivityHistoryBinding binding;
    String date;
    String selectedCourse;
    String selectedSubject;
    String selectedDepartment;
    String selectedSection;
    String selectedYear;
    String completePath;
    private DatabaseReference mDatabase;

    // Selection variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // 1. Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("AppData");

        // 2. Setup dynamic data fetching for each spinner
        // Ensure these keys match your Firebase exactly (Case-Sensitive)
        setupDynamicSpinner("Course", binding.spinnerCourse, binding.tvCourse, "course");
        setupDynamicSpinner("Department", binding.spinnerDepartment, binding.tvDepartment, "dept");
        setupDynamicSpinner("Year", binding.spinnerYear, binding.tvYear, "year");
        setupDynamicSpinner("Section", binding.spinnerSection, binding.tvSection, "section");
        setupDynamicSpinner("Subject", binding.spinnerSubject, binding.tvSubject, "subject");

        // 3. UI logic to trigger hidden spinners
        binding.cardCourse.setOnClickListener(v -> binding.spinnerCourse.performClick());
        binding.cardDepartment.setOnClickListener(v -> binding.spinnerDepartment.performClick());
        binding.cardYear.setOnClickListener(v -> binding.spinnerYear.performClick());
        binding.cardSection.setOnClickListener(v -> binding.spinnerSection.performClick());
        binding.cardSubject.setOnClickListener(v -> binding.spinnerSubject.performClick());

        // 4. Submit button logic
        binding.btnHistory.setOnClickListener(v -> {
            if (validateSelection()) {
                String completePath = selectedCourse + "/" + selectedDepartment + "/" +
                        selectedYear + "/" + selectedSection + "/" + selectedSubject;

                Intent intent = new Intent(this, DateAndLectureSelection.class);
                intent.putExtra("pathForDataFetch", completePath);
                startActivity(intent);
            }
        });
    }

    /**
     * Helper method to sync Firebase data with a Spinner and handle selection.
     */
    private void setupDynamicSpinner(String firebaseKey, Spinner spinner, TextView displayTv, String type) {
        ArrayList<String> list = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mDatabase.child(firebaseKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    // FIX: Convert to String safely in case numbers were used as keys in Firebase
                    Object value = data.getValue();
                    if (value != null) {
                        list.add(String.valueOf(value));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String result = parent.getItemAtPosition(position).toString();
                displayTv.setText(result);

                // Map the selection to the correct variable
                switch (type) {
                    case "course": selectedCourse = result; break;
                    case "dept": selectedDepartment = result; break;
                    case "year": selectedYear = result; break;
                    case "section": selectedSection = result; break;
                    case "subject": selectedSubject = result; break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private boolean validateSelection() {
        if (selectedCourse == null || selectedSubject == null || selectedDepartment == null) {
            Toast.makeText(this, "Please select all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


//        auth = FirebaseAuth.getInstance();
//
//
//        String[] course = {"Btech", "Diploma", "MBA", "Bforma"};
//        String[] department={"CSE","AIML","IT","ECE","EEE","CIVIL","MECHANICAL","ELECTRONICS"};
//        String[]section={"7B1","7B2","7B3","A","B"};
//        String[] years = {"1st", "2nd", "3rd", "4th"};
//        String[] subjects={"COI","CN","CD","OS","HUMAN VALUES","LR","QUANTS"};
//
//
//        binding.cardCourse.setOnClickListener(v ->
//                binding.spinnerCourse.performClick());
//        binding.cardDepartment.setOnClickListener(v ->
//                binding.spinnerDepartment.performClick());
//
//        binding.cardYear.setOnClickListener(v ->
//                binding.spinnerYear.performClick());
//
//        binding.cardSection.setOnClickListener(v ->
//                binding.spinnerSection.performClick());
//
//        binding.cardSubject.setOnClickListener(v ->
//                binding.spinnerSubject.performClick());
//
//
//
//        // Adapter to bind course with Spinner
//        ArrayAdapter<String> adapterCourse = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_spinner_item,
//                course
//        );
//        adapterCourse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerCourse.setAdapter(adapterCourse);
//        binding.spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedCourse = parent.getItemAtPosition(position).toString();
//                binding.tvCourse.setText(selectedCourse);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
//
//
//        // Adapter to bind department with Spinner
//        ArrayAdapter<String> adapterDepartment = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_spinner_item,
//                department
//        );
//        adapterDepartment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerDepartment.setAdapter(adapterDepartment);
//        binding.spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedDepartment = parent.getItemAtPosition(position).toString();
//                binding.tvDepartment.setText(selectedDepartment);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
//
//
//        // Adapter to bind section with Spinner
//        ArrayAdapter<String> adapterSection=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,section);
//        adapterSection.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerSection.setAdapter(adapterSection);
//        binding.spinnerSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedSection = parent.getItemAtPosition(position).toString();
//                binding.tvSection.setText(selectedSection);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
//
//
//
//        // Adapter to bind year with Spinner
//        ArrayAdapter<String> adapterYear=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,years);
//        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerYear.setAdapter(adapterYear);
//        binding.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedYear = parent.getItemAtPosition(position).toString();
//                binding.tvYear.setText(selectedYear);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
//
//
//
//        // Adapter to bind subject with Spinner
//        ArrayAdapter<String> adapterSubjects=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,subjects);
//        adapterSubjects.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerSubject.setAdapter(adapterSubjects);
//        binding.spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedSubject = parent.getItemAtPosition(position).toString();
//                binding.tvSubject.setText(selectedSubject);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
//
//
//        binding.btnHistory.setOnClickListener(v->{
//            completePath=selectedCourse+"/"+selectedDepartment+"/"+selectedYear+"/"+selectedSection+"/"+selectedSubject;
//            startActivity(new Intent(getApplicationContext(),DateAndLectureSelection.class).putExtra("pathForDataFetch",completePath));
//        });
//
//    }

}
