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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.year4th.databinding.ActivityHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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


        auth = FirebaseAuth.getInstance();


        String[] course = {"Btech", "Diploma", "MBA", "Bforma"};
        String[] department={"CSE","AIML","IT","ECE","EEE","CIVIL","MECHANICAL","ELECTRONICS"};
        String[]section={"7B1","7B2","7B3","A","B"};
        String[] years = {"1st", "2nd", "3rd", "4th"};
        String[] subjects={"COI","CN","CD","OS","HUMAN VALUES","LR","QUANTS"};


        // Adapter to bind course with Spinner
        ArrayAdapter<String> adapterCourse = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                course
        );
        adapterCourse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCourse.setAdapter(adapterCourse);
        binding.spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        // Adapter to bind department with Spinner
        ArrayAdapter<String> adapterDepartment = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                department
        );
        adapterDepartment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDepartment.setAdapter(adapterDepartment);
        binding.spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        // Adapter to bind section with Spinner
        ArrayAdapter<String> adapterSection=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,section);
        adapterSection.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSection.setAdapter(adapterSection);
        binding.spinnerSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });



        // Adapter to bind year with Spinner
        ArrayAdapter<String> adapterYear=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,years);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerYear.setAdapter(adapterYear);
        binding.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });



        // Adapter to bind subject with Spinner
        ArrayAdapter<String> adapterSubjects=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,subjects);
        adapterSubjects.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSubject.setAdapter(adapterSubjects);
        binding.spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        binding.btnHistory.setOnClickListener(v->{
            completePath=selectedCourse+"/"+selectedDepartment+"/"+selectedYear+"/"+selectedSection+"/"+selectedSubject;
            startActivity(new Intent(getApplicationContext(),DateAndLectureSelection.class).putExtra("pathForDataFetch",completePath));
        });

    }

}
