package com.example.quizzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUESTIONS";

    private RecyclerView recyclerView;
    private List<QuestionModel> modelList;

    private List<QuestionModel> bookList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bookmarks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadAds();

        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        editor = preferences.edit();
        gson = new Gson();

        getBookmarks();

        recyclerView=findViewById(R.id.rv_bookmarks);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        BookmarkAdapter adapter=new BookmarkAdapter(bookList);

        recyclerView.setAdapter(adapter);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getBookmarks() {
        String json = preferences.getString(KEY_NAME,"");

        Type type=new TypeToken<List<QuestionModel>>(){}.getType();

        bookList=gson.fromJson(json,type);

        if(bookList==null){
            bookList=new ArrayList<>();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }


    private void storeBookmarks()
    {
        String json=gson.toJson(bookList);
        editor.putString(KEY_NAME,json);
        editor.commit();
    }

    private void loadAds() {

        AdView adView=findViewById(R.id.adView);
        AdRequest adRequest=new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

}