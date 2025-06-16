package com.assingment.fotnews;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.assignment.fotwavenewsapp.model.News;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {
    private static final String TAG = "HomeActivity";
    private BottomNavigationView bottomNavigationView;
    private LinearLayout newsContainer;
    private FirebaseFirestore db;
    private List<News> academicList;
    private DrawerLayout drawerLayout;
    private LinearLayout navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeViews();
        setupBottomNavigation();
        setupFirestore();
        fetchAcademicNews();
        configureStatusBar();
        setupToolbarWithUsername();
        setupDrawer();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        newsContainer = findViewById(R.id.news_container);
        drawerLayout = findViewById(R.id.drawer_layout);
        navDrawer = findViewById(R.id.nav_drawer);

        if (newsContainer == null) {
            Log.e(TAG, "news_container not found in layout");
        }

        // Setup toolbar navigation icon click
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setupDrawer() {
        TextView userInfo = findViewById(R.id.nav_user_info);
        TextView developerInfo = findViewById(R.id.nav_developer_info);
        TextView logout = findViewById(R.id.nav_logout);

        userInfo.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Handle user info click
            Toast.makeText(this, "User Information clicked", Toast.LENGTH_SHORT).show();
        });

        developerInfo.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Handle developer info click
            Toast.makeText(this, "Developer Information clicked", Toast.LENGTH_SHORT).show();
        });

        logout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Handle logout
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_sports) {
                    startActivity(new Intent(HomeActivity.this, SportsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_events) {
                    startActivity(new Intent(HomeActivity.this, EventsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_academic) {
                    startActivity(new Intent(HomeActivity.this, AcademicNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        academicList = new ArrayList<>();
    }

    private void fetchAcademicNews() {
        db.collection("news")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        academicList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            News news = document.toObject(News.class);
                            academicList.add(news);
                            Log.d(TAG, "News fetched: " + news.getTitle());
                        }
                        displayNews();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(HomeActivity.this, "Failed to fetch news", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching academic news", e);
                    Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayNews() {
        if (newsContainer == null) {
            Log.e(TAG, "newsContainer is null, cannot display news");
            return;
        }

        // Clear existing views
        newsContainer.removeAllViews();

        for (News news : academicList) {
            createNewsCard(news);
        }

        if (academicList.isEmpty()) {
            showNoNewsMessage();
        }
    }

    private void createNewsCard(News news) {
        Typeface playRegular = ResourcesCompat.getFont(this, R.font.play_regular);
        Typeface playBold = ResourcesCompat.getFont(this, R.font.play_bold);

        // Main Card View with white background and rounded corners
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(16)); // Only bottom margin
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(dpToPx(4));
        cardView.setRadius(dpToPx(16));
        cardView.setCardBackgroundColor(Color.WHITE);

        // Main horizontal layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Left side - Profile circle and content
        LinearLayout leftLayout = new LinearLayout(this);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        leftLayout.setLayoutParams(leftParams);
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Text content layout
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textLayout.setOrientation(LinearLayout.VERTICAL);

        // Header (Title) - Black text
        TextView headerTextView = new TextView(this);
        headerTextView.setText(news.getTitle());
        headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        headerTextView.setTypeface(playBold);
        headerTextView.setTextColor(Color.BLACK);
        headerTextView.setMaxLines(1);
        headerTextView.setEllipsize(TextUtils.TruncateAt.END);

        // Subhead (Description) - Gray text
        TextView subheadTextView = new TextView(this);
        subheadTextView.setText(news.getDescription());
        subheadTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        subheadTextView.setTypeface(playRegular);
        subheadTextView.setTextColor(Color.parseColor("#666666"));
        subheadTextView.setMaxLines(2);
        subheadTextView.setEllipsize(TextUtils.TruncateAt.END);

        TextView dateView = new TextView(this);
        dateView.setText(news.getDate());
        dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        dateView.setTypeface(playBold);
        dateView.setTextColor(Color.parseColor("#666666"));
        dateView.setMaxLines(2);
        dateView.setEllipsize(TextUtils.TruncateAt.END);

        textLayout.addView(headerTextView);
        textLayout.addView(dateView);
        textLayout.addView(subheadTextView);


//        leftLayout.addView(profileCircle);
        leftLayout.addView(textLayout);

        // Right side - Rounded Image
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                dpToPx(80),
                dpToPx(80)
        );
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Create rounded corners for image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setBackground(createRoundedDrawable(Color.parseColor("#F5F5F5"), dpToPx(12)));
            imageView.setClipToOutline(true);
        }

        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(news.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Add click listener for the entire card
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewsDetailActivity.class);
            intent.putExtra("news_title", news.getTitle());
            intent.putExtra("news_content", news.getContent());
            intent.putExtra("news_description", news.getDescription());
            intent.putExtra("news_date", news.getDate());
            intent.putExtra("news_image_url", news.getImageUrl());
            intent.putExtra("source_activity", "academic");
            startActivity(intent);
        });

        // Add long click for admin delete functionality
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String loggedInUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if ("admin@gmail.com".equalsIgnoreCase(loggedInUserEmail)) {
                cardView.setOnLongClickListener(v -> {
                    // Show delete confirmation
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Delete News")
                            .setMessage("Are you sure you want to delete this news?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                db.collection("news")
                                        .whereEqualTo("title", news.getTitle())
                                        .whereEqualTo("date", news.getDate())
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                snapshot.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(this, "News deleted", Toast.LENGTH_SHORT).show();
                                                            fetchAcademicNews();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            }
        }

        // Assemble the card
        mainLayout.addView(leftLayout);
        mainLayout.addView(imageView);
        cardView.addView(mainLayout);
        newsContainer.addView(cardView);
    }

    // Helper method to create circular drawable
    private GradientDrawable createCircleDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    // Helper method to create rounded drawable
    private GradientDrawable createRoundedDrawable(int color, int cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        return drawable;
    }

    private void showNoNewsMessage() {
        TextView noNewsText = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(32), 0, 0);
        noNewsText.setLayoutParams(params);
        noNewsText.setText("No news available at the moment.");
        noNewsText.setTextSize(16);
        noNewsText.setTextColor(Color.WHITE);
        noNewsText.setGravity(android.view.Gravity.CENTER);
        newsContainer.addView(noNewsText);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}