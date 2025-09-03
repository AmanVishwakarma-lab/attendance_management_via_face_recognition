package com.example.year4th;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.window.OnBackInvokedDispatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;

public class DateAndLectureSelection extends AppCompatActivity {
    FirebaseAuth auth;
    ListView listView;
    FirebaseFirestore db;
    ArrayAdapter<String> adapter;
    List<String> itemList = new ArrayList<>();
    String pathForDb,pathOfStudentList;

    boolean showingLectures = true; // 🔹 toggle state
    String selectedLecture = null;  // 🔹 store clicked lecture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_and_lecture_selection);

        listView = findViewById(R.id.datesRecyclerView);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        pathForDb = getIntent().getStringExtra("pathForDataFetch");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);

        loadLectures();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (showingLectures) {
                // lecture clicked → load its dates
                selectedLecture = itemList.get(position);
                loadDates(selectedLecture);
            } else {
                // date clicked → open attendance sheet
                //TOD
                //open excel view of present student
                String selectedDate=itemList.get(position);
                pathOfStudentList=pathForDb+"/"+selectedLecture+"/"+selectedDate;
                startActivity(new Intent(getApplicationContext(), ExcelViewActivity.class).putExtra("pathToGetStudentNames",pathOfStudentList));
            }
        });

        //back press button working to move from date list to lecture list
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    () -> {
                        if (!showingLectures) {
                            loadLectures();
                        } else {
                            finish(); // exit activity
                        }
                    }
            );
        }
    }

    private void loadLectures() {
        if (pathForDb == null) return;

        String[] pathDb = pathForDb.split("/");
        if (pathDb.length != 5) return;

        db.collection("users").document(auth.getUid())
                .collection("Course").document(pathDb[0])
                .collection("Department").document(pathDb[1])
                .collection("Year").document(pathDb[2])
                .collection("Section").document(pathDb[3])
                .collection("Subjects").document(pathDb[4])
                .collection("Lecture")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        itemList.add(doc.getId()); // lecture IDs
                    }
                    adapter.notifyDataSetChanged();
                    showingLectures = true; // back to lecture mode
                });
    }

    private void loadDates(String lectureId) {
        if (pathForDb == null) return;

        String[] pathDb = pathForDb.split("/");
        if (pathDb.length != 5) return;

        db.collection("users").document(auth.getUid())
                .collection("Course").document(pathDb[0])
                .collection("Department").document(pathDb[1])
                .collection("Year").document(pathDb[2])
                .collection("Section").document(pathDb[3])
                .collection("Subjects").document(pathDb[4])
                .collection("Lecture").document(lectureId)
                .collection("dates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        itemList.add(doc.getId()); // date IDs
                    }
                    adapter.notifyDataSetChanged();
                    showingLectures = false; // now in date mode
                });
    }

}
