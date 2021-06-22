package com.example.client.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.client.DAOClass;
import com.example.client.R;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        TextView label = (TextView) findViewById(R.id.textRow1);
        TextView describe = (TextView) findViewById(R.id.textRow2);
        TextView date = (TextView) findViewById(R.id.textRow3);

        SQLiteDatabase db = DAOClass.Create();
        Cursor query = db.rawQuery("SELECT * FROM history;", null);
        while(query.moveToNext()){
            String l = query.getString(0);
            String des = query.getString(1);
            String d = query.getString(2);
            label.append(l);
            describe.append(des);
            date.append(d);
        }
        query.close();
        db.close();
    }
}
