package com.lsj.module_second;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lsj.arouter_annotations.ARouter;
import com.lsj.common.KLog;

@ARouter(path = "/module_second/ModuleSecondActivity")
public class ModuleSecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_second);

        KLog.d(getIntent().getStringExtra("name"));
        KLog.d(getIntent().getIntExtra("testInt",0));
        KLog.d(getIntent().getBooleanExtra("testBoolean",false));
    }
}