package com.lsj.module_one;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lsj.arouter_annotations.ARouter;

@ARouter(path = "/module_one/ModuleFirstActivity")
public class ModuleFirstActivity extends AppCompatActivity {
//com.lsj.module_one.ModuleFirstActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_first);
    }
}