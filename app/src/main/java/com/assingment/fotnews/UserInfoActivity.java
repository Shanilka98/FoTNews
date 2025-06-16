package com.assingment.fotnews;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserInfoActivity extends BaseActivity {
    private static final String TAG = "UserInfoActivity";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private TextView tvUsername, tvEmail;
    private Button btnUpdateInfo;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences sharedPreferences;

    private Button logOutBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initViews();
        setupBottomNavigation();
        setupToolbarWithUsername();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserDataFromFirebase(userId);
        }

        setupClickListeners();
        logOutBtn = findViewById(R.id.logoutButton);

        logOutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnUpdateInfo = findViewById(R.id.btn_update_info);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = "User";
                        if (documentSnapshot.exists()) {
                            username = documentSnapshot.getString("username");
                            if (username == null || username.isEmpty()) {
                                username = firebaseUser.getEmail().split("@")[0];
                            }
                        }

                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setTitle(username);
                        }

                        toolbar.setNavigationOnClickListener(v -> onBackPressed());
                    })
                    .addOnFailureListener(e -> {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setTitle("User");
                        }

                        toolbar.setNavigationOnClickListener(v -> onBackPressed());
                    });

        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("User");
            }

            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_academic) {
                    startActivity(new Intent(UserInfoActivity.this, AcademicNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_events) {
                    startActivity(new Intent(UserInfoActivity.this, EventsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_sports) {
                    startActivity(new Intent(UserInfoActivity.this, SportsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadUserDataFromFirebase(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");

                        // Also get email from Firebase Auth if not in Firestore
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (email == null && currentUser != null) {
                            email = currentUser.getEmail();
                        }

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_USERNAME, username != null ? username : "Guest User");
                        editor.putString(KEY_EMAIL, email != null ? email : "guest@example.com");
                        editor.apply();

                        tvUsername.setText(username != null ? username : "Guest User");
                        tvEmail.setText(email != null ? email : "guest@example.com");

                        Log.d(TAG, "User data fetched from Firebase: " + username + ", " + email);
                    } else {
                        Log.d(TAG, "No user document found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user data", e);
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        btnUpdateInfo.setOnClickListener(v -> showUpdateDialog());
    }

    private void showUpdateDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_user);
        dialog.setCancelable(true);

        // Initialize views
        TextInputLayout tilUsername = dialog.findViewById(R.id.til_username);
        TextInputEditText etUsername = dialog.findViewById(R.id.et_username);
        TextInputLayout tilEmail = dialog.findViewById(R.id.til_email);
        TextInputEditText etEmail = dialog.findViewById(R.id.et_email);
        TextInputLayout tilPassword = dialog.findViewById(R.id.til_password);
        TextInputEditText etPassword = dialog.findViewById(R.id.et_password);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Pre-fill current data
        String currentUsername = sharedPreferences.getString(KEY_USERNAME, "");
        String currentEmail = sharedPreferences.getString(KEY_EMAIL, "");
        etUsername.setText(currentUsername);
        etEmail.setText(currentEmail);

        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();

            // Clear previous errors
            tilUsername.setError(null);
            tilEmail.setError(null);
            tilPassword.setError(null);

            boolean hasErrors = false;

            // Validate username
            if (newUsername.isEmpty()) {
                tilUsername.setError("Username cannot be empty");
                hasErrors = true;
            } else if (newUsername.length() < 3) {
                tilUsername.setError("Username must be at least 3 characters");
                hasErrors = true;
            }

            // Validate email
            if (newEmail.isEmpty()) {
                tilEmail.setError("Email cannot be empty");
                hasErrors = true;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                tilEmail.setError("Please enter a valid email address");
                hasErrors = true;
            }

            // Validate password if provided
            if (!newPassword.isEmpty() && newPassword.length() < 6) {
                tilPassword.setError("Password must be at least 6 characters");
                hasErrors = true;
            }

            if (hasErrors) {
                return;
            }

            // Show progress
            btnSave.setEnabled(false);
            btnSave.setText("Updating...");

            // Update user information
            updateUserInformation(dialog, newUsername, newEmail, newPassword);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(R.drawable.dialog_background);
        }
    }

    private void updateUserInformation(Dialog dialog, String newUsername, String newEmail, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            resetDialogButton(dialog);
            return;
        }

        // Start with updating username and email in Firestore
        updateUsernameAndEmailInFirestore(dialog, newUsername, newEmail, newPassword);
    }

    private void updateUsernameAndEmailInFirestore(Dialog dialog, String newUsername, String newEmail, String newPassword) {
        // Update username and email in Firestore
        firestore.collection("users").document(userId)
                .update("username", newUsername, "email", newEmail)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore updated successfully");

                    // Update Firebase Auth email if it's different
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && !newEmail.equals(user.getEmail())) {
                        updateFirebaseAuthEmail(dialog, user, newUsername, newEmail, newPassword);
                    } else {
                        // Email is the same, check if password needs to be updated
                        if (!newPassword.isEmpty()) {
                            updatePassword(dialog, user, newUsername, newEmail, newPassword);
                        } else {
                            // Only username updated, finish the process
                            finishUpdate(dialog, newUsername, newEmail);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update user information", Toast.LENGTH_SHORT).show();
                    resetDialogButton(dialog);
                    Log.e(TAG, "Firestore update failed", e);
                });
    }

    private void updateFirebaseAuthEmail(Dialog dialog, FirebaseUser user, String newUsername, String newEmail, String newPassword) {
        user.updateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase Auth email updated successfully");

                    // Update password if provided
                    if (!newPassword.isEmpty()) {
                        updatePassword(dialog, user, newUsername, newEmail, newPassword);
                    } else {
                        finishUpdate(dialog, newUsername, newEmail);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetDialogButton(dialog);
                    Log.e(TAG, "Firebase Auth email update failed", e);
                });
    }

    private void updatePassword(Dialog dialog, FirebaseUser user, String newUsername, String newEmail, String newPassword) {
        user.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Password updated successfully");
                    finishUpdate(dialog, newUsername, newEmail);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetDialogButton(dialog);
                    Log.e(TAG, "Password update failed", e);
                });
    }

    private void finishUpdate(Dialog dialog, String newUsername, String newEmail) {
        // Update SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, newUsername);
        editor.putString(KEY_EMAIL, newEmail);
        editor.apply();

        // Update UI
        tvUsername.setText(newUsername);
        tvEmail.setText(newEmail);

        Toast.makeText(this, "Information updated successfully", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        Log.d(TAG, "User info updated - Username: " + newUsername + ", Email: " + newEmail);
    }

    private void resetDialogButton(Dialog dialog) {
        Button btnSave = dialog.findViewById(R.id.btn_save);
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setText("Update Information");
        }
    }

    // Optional utility method to save user data
    public static void saveUserData(android.content.Context context, String username, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }
}