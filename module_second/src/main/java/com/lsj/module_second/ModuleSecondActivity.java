package com.lsj.module_second;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lsj.arouter_annotations.ARouter;

@ARouter(path = "/module_second/ModuleSecondActivity")
public class ModuleSecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_second);
    }
}