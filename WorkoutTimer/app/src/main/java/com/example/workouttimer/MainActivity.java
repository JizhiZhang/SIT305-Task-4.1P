package com.example.workouttimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView mTvLastHours;
    private Chronometer chronometer;
    private ImageView mIvStart, mIvPause, mIvStop;
    private EditText mEdType;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private long pauseOffset = 0;
    private boolean running;

    @SuppressLint({"CommitPrefEdits", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("00:%s");

        mTvLastHours = findViewById(R.id.tv_lasthours);
        mIvStart = findViewById(R.id.iv_start);
        mIvPause = findViewById(R.id.iv_pause);
        mIvStop = findViewById(R.id.iv_stop);
        mEdType = findViewById(R.id.et_type);

        mSharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        String workoutType = mSharedPreferences.getString("type", "");
        long time = mSharedPreferences.getLong("time", 0);
        long hours = (time % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (time % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (time % (1000 * 60)) / 1000;
        String spendTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTvLastHours.setText("You spent " + spendTime + " on " + workoutType + " last time.");

        if (savedInstanceState != null) {
            pauseOffset = savedInstanceState.getLong("pauseOffset", 0);
            running = savedInstanceState.getBoolean("running", true);
            long getBaseTime = savedInstanceState.getLong("getBaseTime", 0);
            if (!running) {
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                chronometer.stop();
                running = false;
            } else if (running) {
                chronometer.setBase(SystemClock.elapsedRealtime() - (SystemClock.elapsedRealtime() - getBaseTime));
                chronometer.start();
                running = true;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("pauseOffset", pauseOffset);
        outState.putBoolean("running", running);
        outState.putLong("getBaseTime", chronometer.getBase());
    }

    public void startClick(View view) {
        if (mEdType.length() == 0) {
            Toast.makeText(MainActivity.this, "Please enter your workout type!", Toast.LENGTH_SHORT).show();
        } else {
            if (!running) {
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                chronometer.start();
                running = true;
            }
        }
    }

    public void pauseClick(View view) {
        if (running) {
            //.stop() only stops updating the text, but ont stops the chronometer server
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    @SuppressLint("SetTextI18n")
    public void stopClick(View view) {
        if (!running) {
            chronometer.stop();
            // Make totalTime equal to pauseOffset in pauseClick (Or we can say the value after clicking the Pause button)
            long totalTime = pauseOffset;
            mEditor.putLong("time", totalTime);
            mEditor.putString("type", mEdType.getText().toString());
            mEditor.apply();
            pauseOffset = 0;
            chronometer.setBase(SystemClock.elapsedRealtime());
        } else {
            Toast.makeText(MainActivity.this, "You need to click the pause at first!", Toast.LENGTH_SHORT).show();
        }
    }
}