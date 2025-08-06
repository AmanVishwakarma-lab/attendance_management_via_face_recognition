package com.example.year4th;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    TextView display;

    ListView listView;
    ArrayAdapter<String> adapter;
    List<String> displayList = new ArrayList<>();

    FirebaseFirestore db;
    FirebaseAuth auth;

    enum State { SUBJECTS, DATES, STUDENTS }

    State currentState = State.SUBJECTS;

    String selectedSubject = "";
    String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        display=findViewById(R.id.display);
        listView = findViewById(R.id.listHistory);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadSubjects();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = displayList.get(position);

            if (currentState == State.SUBJECTS) {
                display.setText("Select Any Date");
                selectedSubject = selectedItem;
                loadDates(selectedSubject);
            } else if (currentState == State.DATES) {
                selectedDate = selectedItem;
                loadStudentNames(selectedSubject, selectedDate);
            }
        });
    }

    private void loadSubjects() {
        currentState = State.SUBJECTS;
        displayList.clear();

        db.collection("users")
                .document(auth.getUid())
                .collection("subjects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        displayList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load subjects", Toast.LENGTH_SHORT).show());
    }

    private void loadDates(String subject) {
        currentState = State.DATES;
        displayList.clear();
        db.collection("users")
                .document(auth.getUid())
                .collection("subjects")
                .document(subject)
                .collection("dates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        displayList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load dates", Toast.LENGTH_SHORT).show());
    }

    private void loadStudentNames(String subject, String date) {
        Intent intent=new Intent(this, ExcelViewActivity.class);
        intent.putExtra("subject",subject);
        intent.putExtra("date",date);
        startActivity(intent);
//        currentState = State.STUDENTS;
//        displayList.clear();
//
//        db.collection("users")
//                .document(auth.getUid())
//                .collection("subjects")
//                .document(subject)
//                .collection("dates")
//                .document(date)
//                .collection("studentNames")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                        displayList.add(doc.getId());
//                    }
//                    adapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load student names", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (currentState == State.STUDENTS) {
            loadDates(selectedSubject);
        } else if (currentState == State.DATES) {
            loadSubjects();
        } else {
            super.onBackPressed();
        }
    }
}
