package edu.northeastern.tomato;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ImageView imageViewTogglePassword;
    private Button buttonLogin;
    private TextView textViewSignUp, textViewTitle;

    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        imageViewTogglePassword = findViewById(R.id.imageView_toggle_password);
        buttonLogin = findViewById(R.id.button_login);
        textViewSignUp = findViewById(R.id.textView_sign_up);
        textViewTitle = findViewById(R.id.textView_title);

        // Toggle password visibility
        imageViewTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Set password as invisible
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imageViewTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                // Set password as visible
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imageViewTogglePassword.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            // Move cursor to the end of the text
            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        // Login button click event
        buttonLogin.setOnClickListener(v -> loginWithEmail());

        // Sign-up hint click event
        textViewSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Login with email and password
     */
    private void loginWithEmail() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(buttonLogin, "Please enter email and password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        Snackbar.make(buttonLogin, "Login successful", Snackbar.LENGTH_SHORT).show();
                        // Navigate to the main page
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed, check exception type
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Snackbar.make(buttonLogin, "This email is not registered, please sign up first", Snackbar.LENGTH_LONG).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Snackbar.make(buttonLogin, "Incorrect password, please try again", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(buttonLogin, "Login failed: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
