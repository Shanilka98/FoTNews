package com.assingment.fotnews;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.assignment.fotwavenewsapp.model.News;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EventsNewsActivity extends BaseActivity {
    private static final String TAG = "EventsNewsActivity";
    private BottomNavigationView bottomNavigationView;
    private LinearLayout newsContainer;
    private FirebaseFirestore db;
    private List<News> eventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_news);

        initializeViews();
        setupBottomNavigation();
        setupFirestore();
        fetchEventsNews();

        setupToolbarWithUsername();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_events);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find the LinearLayout inside ScrollView where cards will be added
        newsContainer = findViewById(R.id.news_container);
        if (newsContainer == null) {
            Log.e(TAG, "news_container not found in layout");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_sports) {
                    startActivity(new Intent(EventsNewsActivity.this, SportsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_academic) {
                    startActivity(new Intent(EventsNewsActivity.this, AcademicNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_events) {
                    return true;
                }

                return false;
            }
        });
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        eventsList = new ArrayList<>();
    }

    private void fetchEventsNews() {
        db.collection("news")
                .whereEqualTo("newsType", "events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            News news = document.toObject(News.class);
                            eventsList.add(news);
                            Log.d(TAG, "News fetched: " + news.getTitle());
                        }
                        displayNews();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(EventsNewsActivity.this, "Failed to fetch news", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching events news", e);
                    Toast.makeText(EventsNewsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayNews() {
        if (newsContainer == null) {
            Log.e(TAG, "newsContainer is null, cannot display news");
            return;
        }
        newsContainer.removeAllViews();
        for (News news : eventsList) {
            createNewsCard(news);
        }
        if (eventsList.isEmpty()) {
            showNoNewsMessage();
        }
    }

    private void createNewsCard(News news) {
        Typeface playRegular = ResourcesCompat.getFont(this, R.font.play_regular);
        Typeface playBold = ResourcesCompat.getFont(this, R.font.play_bold);

        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(dpToPx(8));
        cardView.setRadius(dpToPx(16));
        cardView.setCardBackgroundColor(Color.WHITE);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // TOP TITLE (above image)
        LinearLayout topTitleContainer = new LinearLayout(this);
        topTitleContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        topTitleContainer.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        topTitleContainer.setBackgroundColor(Color.WHITE);

        TextView topTitleTextView = new TextView(this);
        topTitleTextView.setText(news.getTitle());
        topTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        topTitleTextView.setTypeface(playBold);
        topTitleTextView.setTextColor(Color.BLACK);
        topTitleTextView.setMaxLines(2);
        topTitleTextView.setEllipsize(TextUtils.TruncateAt.END);

        topTitleContainer.addView(topTitleTextView);

        // Image View
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(180)
        );
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(news.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Content Layout
        LinearLayout contentLayout = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contentLayout.setLayoutParams(contentParams);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(20));
        contentLayout.setBackgroundColor(Color.WHITE);

        // SECOND TITLE (below image, underlined)
        TextView titleBelowTextView = new TextView(this);
        titleBelowTextView.setText(news.getTitle());
        titleBelowTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleBelowTextView.setTypeface(playRegular);
        titleBelowTextView.setTextColor(Color.BLACK);
        titleBelowTextView.setMaxLines(2);
        titleBelowTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleBelowTextView.setPaintFlags(titleBelowTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        LinearLayout.LayoutParams titleBelowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleBelowParams.setMargins(0, 0, 0, dpToPx(8));
        titleBelowTextView.setLayoutParams(titleBelowParams);

        // Date TextView
        TextView dateTextView = new TextView(this);
        dateTextView.setText(news.getDate());
        dateTextView.setTypeface(playRegular);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        dateTextView.setTextColor(Color.parseColor("#666666"));
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.setMargins(0, 0, 0, dpToPx(12));
        dateTextView.setLayoutParams(dateParams);

        // Description TextView
        TextView descriptionTextView = new TextView(this);
        descriptionTextView.setText(news.getDescription());
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        descriptionTextView.setTextColor(Color.parseColor("#333333"));
        descriptionTextView.setMaxLines(3);
        descriptionTextView.setTypeface(playRegular);
        descriptionTextView.setEllipsize(TextUtils.TruncateAt.END);
        descriptionTextView.setLineSpacing(dpToPx(2), 1.0f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.setMargins(0, 0, 0, dpToPx(16));
        descriptionTextView.setLayoutParams(descParams);

        // Button Layout
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setGravity(Gravity.END);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // Read News Button
        MaterialButton readButton = new MaterialButton(this);
        LinearLayout.LayoutParams readButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(40)
        );
        readButtonParams.setMargins(0, 0, dpToPx(12), 0);
        readButton.setLayoutParams(readButtonParams);
        readButton.setText("Read News");
        readButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        readButton.setCornerRadius(dpToPx(20));
        readButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        readButton.setTextColor(Color.WHITE);
        readButton.setPadding(dpToPx(16), 0, dpToPx(16), 0);
        readButton.setAllCaps(false);
        readButton.setTypeface(playRegular);
        readButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventsNewsActivity.this, NewsDetailActivity.class);
            intent.putExtra("news_title", news.getTitle());
            intent.putExtra("news_content", news.getContent());
            intent.putExtra("news_description", news.getDescription());
            intent.putExtra("news_date", news.getDate());
            intent.putExtra("news_image_url", news.getImageUrl());
            intent.putExtra("source_activity", "events");
            startActivity(intent);
        });

        buttonLayout.addView(readButton);

        // Admin Delete Button (only for specific user)
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String loggedInUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if ("admin@gmail.com".equalsIgnoreCase(loggedInUserEmail)) {
                MaterialButton deleteButton = new MaterialButton(this);
                LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(40)
                );
                deleteButton.setLayoutParams(deleteButtonParams);
                deleteButton.setText("Delete News");
                deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                deleteButton.setCornerRadius(dpToPx(20));
                deleteButton.setTypeface(playRegular);
                deleteButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                deleteButton.setTextColor(Color.WHITE);
                deleteButton.setPadding(dpToPx(16), 0, dpToPx(16), 0);
                deleteButton.setAllCaps(false);

                deleteButton.setOnClickListener(v -> {
                    db.collection("news")
                            .whereEqualTo("title", news.getTitle())
                            .whereEqualTo("date", news.getDate())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    snapshot.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "News deleted", Toast.LENGTH_SHORT).show();
                                                fetchEventsNews();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });
                });

                buttonLayout.addView(deleteButton);
            }
        }

        // Add all views to content layout
        contentLayout.addView(titleBelowTextView); // Second title (underlined)
        contentLayout.addView(dateTextView);
        contentLayout.addView(descriptionTextView);
        contentLayout.addView(buttonLayout);

        // Add to main layout IN CORRECT ORDER
        mainLayout.addView(topTitleContainer); // FIRST: Top title
        mainLayout.addView(imageView);         // SECOND: Image
        mainLayout.addView(contentLayout);     // THIRD: Content with second title, date, description, button

        cardView.addView(mainLayout);
        newsContainer.addView(cardView);
    }

    private TextView createTextView(String text, int textSize, boolean bold, int color) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(4), 0, 0);
        textView.setLayoutParams(params);
        textView.setText(text != null ? text : "");
        textView.setTextSize(textSize);
        textView.setTextColor(color);
        if (bold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return textView;
    }

    private void showNoNewsMessage() {
        TextView noNewsText = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(32), 0, 0);
        noNewsText.setLayoutParams(params);
        noNewsText.setText("No Events news available at the moment.");
        noNewsText.setTextSize(16);
        noNewsText.setTextColor(0xFF666666);
        noNewsText.setGravity(android.view.Gravity.CENTER);
        newsContainer.addView(noNewsText);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}