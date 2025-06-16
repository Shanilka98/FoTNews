package com.assingment.fotnews;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

public class NewsDetailActivity extends BaseActivity {

    private ImageView newsImageView;
    private TextView newsTitleTextView;
    private TextView newsDateTextView;
    private TextView newsDescriptionTextView;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private String sourceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        MaterialButton backBtn = findViewById(R.id.back);

        sourceActivity = getIntent().getStringExtra("source_activity");
        if (sourceActivity == null) {
            sourceActivity = "academic";
        }

        initializeViews();
        loadNewsData();
        setupBottomNavigation();

        setupToolbarWithUsername();

        backBtn.setOnClickListener(v -> {
            // Navigate back to the correct activity based on source
            Intent intent;
            switch (sourceActivity) {
                case "events":
                    intent = new Intent(NewsDetailActivity.this, EventsNewsActivity.class);
                    break;
                case "sports":
                    intent = new Intent(NewsDetailActivity.this, SportsNewsActivity.class);
                    break;
                case "academic":
                default:
                    intent = new Intent(NewsDetailActivity.this, AcademicNewsActivity.class);
                    break;
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {
        newsImageView = findViewById(R.id.news_detail_image);
        newsTitleTextView = findViewById(R.id.news_detail_title);
        newsDateTextView = findViewById(R.id.news_detail_date);
        newsDescriptionTextView = findViewById(R.id.news_detail_description);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("News Detail");
        }
    }

    private void loadNewsData() {
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("news_title");
            String content = intent.getStringExtra("news_content");
            String description = intent.getStringExtra("news_description");
            String date = intent.getStringExtra("news_date");
            String imageUrl = intent.getStringExtra("news_image_url");

            // Set data to views
            newsTitleTextView.setText(title != null ? title : "No Title");
            newsDescriptionTextView.setText(description != null ? description : "No Description");
            newsDateTextView.setText(date != null ? date : "No Date");

            // Load image
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(newsImageView);
            } else {
                newsImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    private void setupBottomNavigation() {
        // Set the selected item based on source activity
        switch (sourceActivity) {
            case "events":
                bottomNavigationView.setSelectedItemId(R.id.nav_events);
                break;
            case "sports":
                bottomNavigationView.setSelectedItemId(R.id.nav_sports);
                break;
            case "academic":
            default:
                bottomNavigationView.setSelectedItemId(R.id.nav_academic);
                break;
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_sports) {
                    startActivity(new Intent(NewsDetailActivity.this, SportsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_events) {
                    startActivity(new Intent(NewsDetailActivity.this, EventsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_academic) {
                    startActivity(new Intent(NewsDetailActivity.this, AcademicNewsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}