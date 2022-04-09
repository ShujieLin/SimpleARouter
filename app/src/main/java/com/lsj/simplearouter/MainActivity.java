package com.lsj.simplearouter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lsj.arouter_annotations.ARouter;
import com.lsj.module_one.ModuleFirstActivity;
import com.lsj.module_second.ModuleSecondActivity;

@ARouter(path = "/app/MainActivity",group = "app")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoModuleFirst(View view) {
        Intent intent = new Intent(this, ModuleFirstActivity.class);
        startActivity(intent);
    }

    public void gotoModuleSecond(View view) {
        Intent intent = new Intent(this, ModuleSecondActivity.class);
        startActivity(intent);
    }
}