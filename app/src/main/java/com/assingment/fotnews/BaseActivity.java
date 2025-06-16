package com.assingment.fotnews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    protected MaterialToolbar toolbar;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureStatusBar();
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.success_color));
        }

        int color = ContextCompat.getColor(this, R.color.success_color); // Replace with your color
        setStatusBarColorWithInsets(this, color);

    }


    public void setStatusBarColorWithInsets(Activity activity, int color) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // API 34+
            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars());
                v.setBackgroundColor(color);

                // Adjust padding to prevent overlap
                v.setPadding(0, statusBarInsets.top, 0, 0);

                return insets;
            });
        } else {
            // For Android 14 and below
            window.setStatusBarColor(color);
        }
    }
    protected void showPopupMenu(View anchor) {
        try {
            Log.d(TAG, "Creating popup menu");

            // Create PopupMenu with custom theme
            Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
            PopupMenu popupMenu = new PopupMenu(wrapper, anchor);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                popupMenu.setGravity(Gravity.END);
            }

            popupMenu.getMenuInflater().inflate(R.menu.top_dropdown_menu, popupMenu.getMenu());

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || !"admin@gmail.com".equals(user.getEmail())) {
                popupMenu.getMenu().removeItem(R.id.menu_add_news);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                // Your existing click handling code
                int id = item.getItemId();
                Log.d(TAG, "Popup menu item clicked: " + id);

                if (id == R.id.menu_user_info) {
                    Intent intent = new Intent(this, UserInfoActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menu_dev_info) {
                    Intent intent = new Intent(this, DeveloperInfoActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menu_add_news) {
                    Intent intent = new Intent(this, AddNewsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }

                return false;
            });

            popupMenu.setOnDismissListener(menu -> Log.d(TAG, "Popup menu dismissed"));
            popupMenu.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing popup menu", e);
            Toast.makeText(this, "Error showing menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    protected void setupToolbarWithUsername() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = documentSnapshot.getString("username");
                        if (username == null || username.isEmpty()) {
                            username = firebaseUser.getEmail().split("@")[0];
                        }
                        addRightAlignedUsername(username);
                    })
                    .addOnFailureListener(e -> addRightAlignedUsername("User"));
        } else {
            addRightAlignedUsername("User");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void addRightAlignedUsername(String username) {
        TextView textView = new TextView(this);
        textView.setText(username + " \u25BE"); // Unicode down arrow ▼
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        textView.setTypeface(null, Typeface.BOLD);

        androidx.appcompat.widget.Toolbar.LayoutParams layoutParams =
                new androidx.appcompat.widget.Toolbar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;

        textView.setLayoutParams(layoutParams);
        textView.setPadding(0, 0, 24, 0);
        textView.setOnClickListener(v -> showPopupMenu(v));

        toolbar.addView(textView);
    }

    public void setStatusBarColor(Activity activity, int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(ContextCompat.getColor(activity, colorResId));
        }
    }
    public void configureStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Method 1: Use your existing setStatusBarColor method
            try {
                setStatusBarColor(this, R.color.success_color);
            } catch (Exception e) {
                try {
                    setStatusBarColor(this, R.color.primary_color);
                } catch (Exception e2) {
                    getWindow().setStatusBarColor(Color.parseColor("#4CAF50")); // Green color
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }

            Log.d(TAG, "Status bar color configured successfully");
        }
    }
}