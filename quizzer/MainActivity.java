package com.example.quizzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    private Button start_quiz,bookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_quiz=findViewById(R.id.startquiz);
        bookmarks=findViewById(R.id.bookmarks);


        MobileAds.initialize(this);

        loadAds();



        start_quiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent categoriesIntent=new Intent(MainActivity.this,CategoriesActivity.class);
                startActivity(categoriesIntent);
                finish();
            }
        });

        bookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookmarkIntent=new Intent(MainActivity.this,BookmarkActivity.class);
                startActivity(bookmarkIntent);
                finish();
            }
        });

    }

    private void loadAds() {

       AdView adView=findViewById(R.id.adView);
        AdRequest adRequest=new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }
}