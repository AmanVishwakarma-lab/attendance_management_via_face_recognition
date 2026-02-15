package com.example.year4th;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.year4th.databinding.ActivityChooesBranchBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChooesBranchActivity extends AppCompatActivity {

    String selectedCourse;
    String selectedSubject;
    String selectedDepartment;
    String selectedSection;
    String selectedYear;
    String selectedLecture;
    String completePath;
    ActivityChooesBranchBinding binding;

    private DatabaseReference mDatabase;
    private ArrayList<String> courseList = new ArrayList<>();
    private ArrayAdapter<String> adapterCourse;
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



        // 1. Initialize Firebase Reference
        mDatabase = FirebaseDatabase.getInstance().getReference("AppData");

        // 2. Set up all Dynamic Spinners
        // Note: These strings must match your Firebase Keys exactly (Case-Sensitive)
        setupDynamicSpinner("Course", binding.spinnerCourse, binding.tvCourse, "course");
        setupDynamicSpinner("Department", binding.spinnerDepartment, binding.tvDepartment, "dept");
        setupDynamicSpinner("Year", binding.spinnerYear, binding.tvYear, "year");
        setupDynamicSpinner("Section", binding.spinnerSection, binding.tvSection, "section");
        setupDynamicSpinner("Subject", binding.spinnerSubject, binding.tvSubject, "subject");
        setupDynamicSpinner("Lecture", binding.spinnerLecture, binding.tvLecture, "lecture");

        // 3. UI Interactions - Click Card to open hidden Spinner
        binding.cardCourse.setOnClickListener(v -> binding.spinnerCourse.performClick());
        binding.cardDepartment.setOnClickListener(v -> binding.spinnerDepartment.performClick());
        binding.cardYear.setOnClickListener(v -> binding.spinnerYear.performClick());
        binding.cardSection.setOnClickListener(v -> binding.spinnerSection.performClick());
        binding.cardSubject.setOnClickListener(v -> binding.spinnerSubject.performClick());
        binding.cardLecture.setOnClickListener(v -> binding.spinnerLecture.performClick());

        // 4. Action Buttons
        binding.btnCapture.setOnClickListener(v -> {
            if (validateSelection()) {
                String completePath = selectedCourse + "/" + selectedDepartment + "/" +
                        selectedYear + "/" + selectedSection + "/" +
                        selectedSubject + "/" + selectedLecture;

                Intent intent = new Intent(this, CameraActivity.class);
                intent.putExtra("pathForDb", completePath);
                startActivity(intent);
            }
        });

        binding.btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

    }

    /**
     * Helper method to fetch data from Firebase and bind to a specific Spinner.
     */
    private void setupDynamicSpinner(String firebaseKey, Spinner spinner, TextView displayTv, String selectionType) {
        ArrayList<String> list = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Fetch data from Firebase
        // Inside setupDynamicSpinner helper method:
        mDatabase.child(firebaseKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    // FIX: Get as Object first to avoid conversion crash
                    Object value = data.getValue();
                    if (value != null) {
                        list.add(String.valueOf(value)); // Safely converts Long/Double to String
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChooesBranchActivity.this, "Error fetching " + firebaseKey, Toast.LENGTH_SHORT).show();
            }
        });


        // Handle selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = parent.getItemAtPosition(position).toString();
                displayTv.setText(selection);

                // Store in global variable based on type
                switch (selectionType) {
                    case "course": selectedCourse = selection; break;
                    case "dept": selectedDepartment = selection; break;
                    case "year": selectedYear = selection; break;
                    case "section": selectedSection = selection; break;
                    case "subject": selectedSubject = selection; break;
                    case "lecture": selectedLecture = selection; break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });


        // Inside onCreate, add these specific listeners for your FABs
        binding.fbCourse.setOnClickListener(v -> showAddDialog("Course"));
        binding.fbDepartment.setOnClickListener(v -> showAddDialog("Department"));
        binding.fbYear.setOnClickListener(v -> showAddDialog("Year"));
        binding.fbSection.setOnClickListener(v -> showAddDialog("Section"));
        binding.fbSubject.setOnClickListener(v -> showAddDialog("Subject"));
        binding.fbLecture.setOnClickListener(v -> showAddDialog("Lecture"));




//
//
//
//
//
//
//
//        // Dropdown menu
        binding.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Create the PopupMenu object
                PopupMenu popup = new PopupMenu(ChooesBranchActivity.this, v);

                // 2. Inflate the menu resource
                popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

                // 3. Set click listeners for the menu items
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.menuProfile) {
                            Intent intent = new Intent(ChooesBranchActivity.this, UserProfileActivity.class);
                            startActivity(intent);
                            return true;
                        } else if (id == R.id.menuContactUs) {
                            new androidx.appcompat.app.AlertDialog.Builder(ChooesBranchActivity.this)
                                    .setTitle("About Us")
                                    .setMessage("Hii \n Reach us at   amanvishwakarma1949@gmail.com")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok",((dialog, which) -> dialog.dismiss()))
                                    .show();
                            return true;
                        }else if(id == R.id.menuLogout){
                                new androidx.appcompat.app.AlertDialog.Builder(ChooesBranchActivity.this)
                                        .setTitle("Logout")
                                        .setMessage("Are you sure you want to logout?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", (dialog, which) -> {
                                            FirebaseAuth.getInstance().signOut();
                                            startActivity(new Intent(ChooesBranchActivity.this, LoginPage.class));
                                            finish();
                                        })
                                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                        .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                                        .setIcon(R.drawable.app_icon_img)
                                        .show();
                        }
                        return false;
                    }
                });

                // 4. Show the menu
                popup.show();
            }
        });

//
//        // Data for dropdown
////        String[] course = {"Btech", "Diploma", "MBA", "Bforma"};
//        String[] department={"CSE","AIML","IT","ECE","EEE","CIVIL","MECHANICAL","ELECTRONICS"};
//        String[]section={"7B1","7B2","7B3","A","B"};
//        String[] year = {"1st", "2nd", "3rd", "4th"};
//        String[]lecture={"1","2","3","4","5","6"};
//        String[] subjects={"COI","CN","CD","OS","HUMAN VALUES","LR","QUANTS"};
//
//
//
//
//        binding.cardCourse.setOnClickListener(v ->
//                binding.spinnerCourse.performClick());
//
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
//        binding.cardLecture.setOnClickListener(v ->
//                binding.spinnerLecture.performClick());
//
//
//
//
//        //  Floating button actions
//        binding.fbCourse.setOnClickListener(v ->
//                Toast.makeText(this, "hii", Toast.LENGTH_SHORT).show());
//
//


        // Adapter to bind subject with Spinner
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
//
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
//        ArrayAdapter<String> adapterYear=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,year);
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
//        // Adapter to bind lecture with Spinner
//        ArrayAdapter<String> adapterLecture=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,lecture);
//        adapterLecture.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerLecture.setAdapter(adapterLecture);
//        binding.spinnerLecture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedLecture = parent.getItemAtPosition(position).toString();
//                binding.tvLecture.setText(selectedLecture);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
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
//        binding.btnCapture.setOnClickListener(v -> {
//            completePath = selectedCourse + "/" + selectedDepartment + "/" + selectedYear + "/" + selectedSection + "/" +selectedSubject+"/"+ selectedLecture;
//
//            Intent intent = new Intent(this, CameraActivity.class);
//            intent.putExtra("pathForDb", completePath);
//            startActivity(intent);
//        });
//
//        binding.btnHistory.setOnClickListener(v -> {
//            Intent intent = new Intent(this, HistoryActivity.class);
//            startActivity(intent);
//        });
    }
    private boolean validateSelection() {
        if (selectedCourse == null || selectedDepartment == null || selectedYear == null) {
            Toast.makeText(this, "Please complete all selections", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void showAddDialog(String category) {
        android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add New " + category)
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        addNewItemToFirebase(category, newValue);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addNewItemToFirebase(String category, String value) {
        // This finds the next available ID (0, 1, 2...) and adds the new value
        mDatabase.child(category).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long count = task.getResult().getChildrenCount();
                mDatabase.child(category).child(String.valueOf(count)).setValue(value)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added successfully!", Toast.LENGTH_SHORT).show());
            }
        });

    }

}