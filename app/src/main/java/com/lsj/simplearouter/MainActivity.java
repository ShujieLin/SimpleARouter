package com.lsj.simplearouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lsj.arouter_annotations.ARouter;

@ARouter(path = "/app/MainActivity",group = "app")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}