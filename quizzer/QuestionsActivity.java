package com.example.quizzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionsActivity extends AppCompatActivity {

    public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUESTIONS";

    FirebaseDatabase database;
    DatabaseReference myRef;

    private TextView questions, no_indicator;
    private FloatingActionButton bookmark_btn;
    private LinearLayout options_container;
    private Button share_btn, next_btn;
    private int count = 0;
    private List<QuestionModel> list;
    private int position = 0;
    private int score = 0;
    private String category;
    private int setNO;
    private Dialog loadingDialog;

    private List<QuestionModel> bookList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private int matchedQuestionPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questions = findViewById(R.id.question);
        no_indicator = findViewById(R.id.no_indicator);
        bookmark_btn = findViewById(R.id.bookmark_btn);
        options_container = findViewById(R.id.options_container);
        share_btn = findViewById(R.id.share_btn);
        next_btn = findViewById(R.id.next_btn);

        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        editor = preferences.edit();
        gson = new Gson();
        getBookmarks();

        bookmark_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modelMatch()) {
                    bookList.remove(matchedQuestionPosition);
                    bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                } else {


                    bookList.add(list.get(position));
                    bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark));

                }
            }
        });
        database = FirebaseDatabase.getInstance("https://quizzer-6dcbf-default-rtdb.firebaseio.com/");
        myRef = database.getReference();

        category = getIntent().getStringExtra("category");
        setNO = getIntent().getIntExtra("setNO", 1);

        loadAds();
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        loadingDialog.setCancelable(false);


        list = new ArrayList<>();

        loadingDialog.show();
        myRef.child("SETS").child(category).child("questions").orderByChild("setNO").equalTo(setNO).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    list.add(snapshot.getValue(QuestionModel.class));

                }
                if (list.size() > 0) {


                    for (int i = 0; i < 4; i++) {
                        options_container.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                checkAnswer((Button) view);
                            }
                        });
                    }
                    playAnim(questions, 0, list.get(position).getQuestion());

                    next_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            next_btn.setEnabled(false);
                            next_btn.setAlpha(0.7f);
                            enableOption(true);
                            position++;

                            if (position == list.size()) {

                                Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                                scoreIntent.putExtra("score", score);
                                scoreIntent.putExtra("total", list.size());
                                startActivity(scoreIntent);
                                finish();
                                return;
                            }

                            count = 0;
                            playAnim(questions, 0, list.get(position).getQuestion());
                        }
                    });

                    share_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String body = list.get(position).getQuestion() + "\n A)" +
                                    list.get(position).getOptionA() + "\n B)" +
                                    list.get(position).getOptionB() + "\n C)" +
                                    list.get(position).getOptionC() + "\n D)" +
                                    list.get(position).getOptionD();
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quizzer challenge");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, body);
                            startActivity(Intent.createChooser(shareIntent, "Share via"));
                        }
                    });

                } else {
                    finish();
                    Toast.makeText(QuestionsActivity.this, "no questions", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playAnim(View view, final int value, final String data) {
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (value == 0 && count < 4) {
                    String option = " ";
                    if (count == 0) {
                        option = list.get(position).getOptionA();
                    } else if (count == 1) {
                        option = list.get(position).getOptionB();
                    } else if (count == 2) {
                        option = list.get(position).getOptionC();
                    } else if (count == 3) {
                        option = list.get(position).getOptionD();
                    }
                    playAnim(options_container.getChildAt(count), 0, option);
                    count++;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                if (value == 0) {
                    try {
                        ((TextView) view).setText(data);
                        no_indicator.setText(position + 1 + "/" + list.size());

                        if (modelMatch()) {
                            bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark));
                        } else {
                            bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                        }


                    } catch (ClassCastException e) {
                        ((Button) view).setText(data);
                    }
                    view.setTag(data);
                    playAnim(view, 1, data);
                }

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void checkAnswer(Button selectOption) {
        enableOption(false);
        next_btn.setEnabled(true);
        next_btn.setAlpha(1);
        if (selectOption.getText().toString().equals(list.get(position).getCorrectANS())) {
            score++;
            selectOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            selectOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            Button correctOption = (Button) options_container.findViewWithTag(list.get(position).getCorrectANS());
            correctOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

        }

    }

    private void enableOption(boolean enable) {
        for (int i = 0; i < 4; i++) {
            options_container.getChildAt(i).setEnabled(enable);
            if (enable) {
                options_container.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));

            }
        }
    }

    private void getBookmarks() {
        String json = preferences.getString(KEY_NAME, "");

        Type type = new TypeToken<List<QuestionModel>>() {
        }.getType();

        bookList = gson.fromJson(json, type);

        if (bookList == null) {
            bookList = new ArrayList<>();
        }

    }

    private boolean modelMatch() {
        boolean matched = false;
        int i = 0;
        for (QuestionModel model : bookList) {

            if (model.getQuestion().equals(list.get(position).getQuestion())
                    && model.getCorrectANS().equals(list.get(position).getCorrectANS())
                    && model.getSetNo() == list.get(position).getSetNo()) {

                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return matched;
    }


    private void storeBookmarks() {
        String json = gson.toJson(bookList);
        editor.putString(KEY_NAME, json);
        editor.commit();
    }

    private void loadAds() {

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

}