package com.example.year4th;

import static java.lang.reflect.Array.set;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class SubjectSelection extends AppCompatActivity {
    FloatingActionButton logoutBtn;
    EditText subjectInput;
    AppCompatButton addBtn;
    ListView subjectListView;

    ArrayList<String> subjectList;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db=FirebaseFirestore.getInstance();;
    FirebaseAuth auth;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subject_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> { logout();});

        subjectInput = findViewById(R.id.subjectInput);
        addBtn = findViewById(R.id.addBtn);
        subjectListView = findViewById(R.id.subjectListView);

        subjectList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjectList);
        subjectListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        loadSubjects();

        addBtn.setOnClickListener(v -> {
            String subjectName = subjectInput.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                addSubjectToFirebase(subjectName);
            }
        });

        subjectListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSubject = subjectList.get(position);

            Intent intent = new Intent(SubjectSelection.this, MainActivity.class);
            intent.putExtra("subject", selectedSubject); // 👈 Pass subject here
            startActivity(intent);
        });
        subjectListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedSubject = subjectList.get(position);
            showEditDeleteDialog(selectedSubject);
            return true;
        });
    }
    private void logout(){
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginPage.class));
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss(); // Just close the dialog
                })
                .show();
    }
    private void loadSubjects() {
        db.collection("users")
                .document(userId)
                .collection("subjects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    subjectList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        subjectList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void addSubjectToFirebase(String subjectName) {
        db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(subjectName)
                .set(new HashMap<>())  // Creates an empty document
                .addOnSuccessListener(unused -> {
                    subjectList.add(subjectName);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add subject", Toast.LENGTH_SHORT).show();
                });
    }



    private void showEditDeleteDialog(String oldSubject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete Subject");

        final EditText input = new EditText(this);
        input.setText(oldSubject);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newSubject = input.getText().toString().trim();
            if (!newSubject.isEmpty() && !newSubject.equals(oldSubject)) {
                updateSubjectInFirebase(oldSubject, newSubject);
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            deleteSubjectFromFirebase(oldSubject);
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateSubjectInFirebase(String oldSubject, String newSubject) {
        // Delete old, then add new
        deleteSubjectFromFirebase(oldSubject);
        addSubjectToFirebase(newSubject);
    }

    private void deleteSubjectFromFirebase(String subject) {
        db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(subject)
                .delete()
                .addOnSuccessListener(unused -> {
                    subjectList.remove(subject);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                });
    }
}