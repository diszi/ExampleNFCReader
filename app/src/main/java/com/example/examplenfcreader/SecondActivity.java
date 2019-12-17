package com.example.examplenfcreader;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by szidonia.laszlo on 2018. 02. 14..
 */

public class SecondActivity extends AppCompatActivity {


    Button scanBtn;
    TextView formatTx, contentTx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);



    }
}
