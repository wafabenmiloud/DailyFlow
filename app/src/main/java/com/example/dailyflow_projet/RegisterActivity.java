package com.example.dailyflow_projet;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button registerButton, backToLoginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.register_email);
        passwordInput = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.register_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);

        registerButton.setOnClickListener(v -> registerUser());
        backToLoginButton.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, ActivityLogin.class))
        );
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, ActivityLogin.class));
                        finish();
                    } else {
                        Exception e = task.getException();
                        Toast.makeText(this, "Registration failed: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                        Log.e("RegisterActivity", "Error creating user", e);                    }
                });
    }
}
