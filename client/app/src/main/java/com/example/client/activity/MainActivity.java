package com.example.client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.client.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private  EditText mPasswordField;

        Button translateButton;
        Button aboutButton;
        Button translateVideoButton;
        Button historyButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            translateButton = findViewById(R.id.buttonTranslate);
            aboutButton = findViewById(R.id.buttonAbout);
            translateVideoButton = findViewById(R.id.buttonTranslateVideo);
            historyButton = findViewById(R.id.buttonHistory);

            mStatusTextView = (TextView)findViewById(R.id.textViewStatus);
            mDetailTextView = (TextView)findViewById(R.id.textViewDetail);

            translateButton.setOnClickListener(this);
            aboutButton.setOnClickListener(this);
            translateVideoButton.setOnClickListener(this);
            historyButton.setOnClickListener(this);

        }

    // [START on_start_check_user]
//        @Override
//        public  void onStart() {
//            super.onStart ();
//
//        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.buttonTranslate: {
                    Intent intent = new Intent(this, TranslateActivity.class);
                    startActivity(intent);
                    break;
                }
                case R.id.buttonTranslateVideo: {
                    Intent intent = new Intent(this, TranslateVideoActivity.class);
                    startActivity(intent);
                    break;
                }
                case  R.id.buttonHistory: {
                    Intent intent = new Intent(this, HistoryActivity.class);
                    startActivity(intent);
                    break;
                }
                case R.id.buttonAbout: {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        }
    }
