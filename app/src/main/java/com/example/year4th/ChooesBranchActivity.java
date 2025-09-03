package com.example.year4th;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.year4th.databinding.ActivityChooesBranchBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ChooesBranchActivity extends AppCompatActivity {

    String selectedCourse;
    String selectedSubject;
    String selectedDepartment;
    String selectedSection;
    String selectedYear;
    String selectedLecture;
    String completePath;
    ActivityChooesBranchBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChooesBranchBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Data for dropdown
        String[] course = {"Btech", "Diploma", "MBA", "Bforma"};
        String[] department={"CSE","AIML","IT","ECE","EEE","CIVIL","MECHANICAL","ELECTRONICS"};
        String[]section={"7B1","7B2","7B3","A","B"};
        String[] year = {"1st", "2nd", "3rd", "4th"};
        String[]lecture={"1","2","3","4","5","6"};
        String[] subjects={"COI","CN","CD","OS","HUMAN VALUES","LR","QUANTS"};
        // Adapter to bind subject with Spinner
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
        ArrayAdapter<String> adapterYear=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,year);
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


        // Adapter to bind lecture with Spinner
        ArrayAdapter<String> adapterLecture=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,lecture);
        adapterLecture.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLecture.setAdapter(adapterLecture);
        binding.spinnerLecture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLecture = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        // Adapter to bind subject with Spinner
        ArrayAdapter<String> adapterSubjects=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,subjects);
        adapterLecture.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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



        binding.btnCapture.setOnClickListener(v -> {
            completePath = selectedCourse + "/" + selectedDepartment + "/" + selectedYear + "/" + selectedSection + "/" +selectedSubject+"/"+ selectedLecture;

            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("pathForDb", completePath);
            startActivity(intent);
        });

        binding.btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });
        binding.logoutBtn.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginPage.class));
            finishAffinity();
        });
    }

}