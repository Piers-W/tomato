package edu.northeastern.tomato;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private ImageView imageViewTogglePassword, imageViewToggleConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogIn, textViewTitle;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI components
        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        editTextConfirmPassword = findViewById(R.id.editText_confirm_password);
        imageViewTogglePassword = findViewById(R.id.imageView_toggle_password);
        imageViewToggleConfirmPassword = findViewById(R.id.imageView_toggle_confirm_password);
        buttonRegister = findViewById(R.id.button_register);
        textViewLogIn = findViewById(R.id.textView_log_in);
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

        // Toggle confirm password visibility
        imageViewToggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                // Set password as invisible
                editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imageViewToggleConfirmPassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                // Set password as visible
                editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imageViewToggleConfirmPassword.setImageResource(R.drawable.ic_eye_open);
            }
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            // Move cursor to the end of the text
            editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
        });

        // Register button click event
        buttonRegister.setOnClickListener(v -> registerUser());

        // Log in hint click event
        textViewLogIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Register the user
     */
    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Snackbar.make(buttonRegister, "Please fill in all required fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Snackbar.make(buttonRegister, "Passwords do not match", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Register using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        createNewUserInDatabase(user);
                        Snackbar.make(buttonRegister, "Registration successful", Snackbar.LENGTH_SHORT).show();
                        // Navigate to the main page or another page
                        finish();
                    } else {
                        // Registration failed, check exception type
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Snackbar.make(buttonRegister, "This email is already registered, please log in", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(buttonRegister, "Registration failed: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Create new user data structure in the database
     */
    private void createNewUserInDatabase(FirebaseUser user) {
        String userId = user.getUid();

        // Get the number of existing users to generate a new username
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long userCount = snapshot.getChildrenCount() + 1;
                String username = "user" + userCount;

                // Update user profile
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();
                user.updateProfile(profileUpdates);

                // Create user node in the database
                DatabaseReference userRef = usersRef.child(userId);

                // Create profile node
                userRef.child("profile").setValue(new UserProfile(username, ""));

                // Create location node
                userRef.child("location").setValue(new UserLocation(0, 0, false));

                // Create stats node
                userRef.child("stats").setValue(new UserStats(0, -1));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(buttonRegister, "Unable to access database: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * User profile class
     */
    public static class UserProfile {
        public String username;
        public String avatarUrl;

        public UserProfile() {}

        public UserProfile(String username, String avatarUrl) {
            this.username = username;
            this.avatarUrl = avatarUrl;
        }
    }

    /**
     * User location information class
     */
    public static class UserLocation {
        public double latitude;
        public double longitude;
        public boolean locationPermission;

        public UserLocation() {}

        public UserLocation(double latitude, double longitude, boolean locationPermission) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.locationPermission = locationPermission;
        }
    }

    /**
     * User stats information class
     */
    public static class UserStats {
        public int score;
        public int ranking;

        public UserStats() {}

        public UserStats(int score, int ranking) {
            this.score = score;
            this.ranking = ranking;
        }
    }
}
