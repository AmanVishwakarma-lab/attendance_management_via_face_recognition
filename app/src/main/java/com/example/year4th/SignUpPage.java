package com.example.year4th;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SignUpPage extends AppCompatActivity {
    FirebaseAuth auth;
    AppCompatButton signUpBtn;
    EditText signUpMail;
    EditText signUpPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        signUpBtn=findViewById(R.id.signUpBtn);
        signUpMail=findViewById(R.id.signUpEmail);
        signUpPass=findViewById(R.id.signUpPassword);
        auth=FirebaseAuth.getInstance();
//        when login successfully goto subjectSelection page
        signUpBtn.setOnClickListener(v -> {
            signUp();
        });
    }
    private void signUp(){
        String email = signUpMail.getText().toString().trim();
        String password = signUpPass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show();
                        // Navigate to home or login
                        startActivity(new Intent(this, SubjectSelection.class));
                        finish();
                    } else {
                        String errorMessage = task.getException().getMessage();

                        if (errorMessage != null && errorMessage.contains("The email address is already in use")) {
                            Toast.makeText(this, "User already exists! Try logging in.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Signup Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}