package com.example.clark.rxjava2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_lifecycle).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            //跳转到绑定生命周期的Activity
            case R.id.btn_lifecycle:
                intent.setClass(this, LifecycleActivity.class);
                break;

            default:
                break;
        }
        startActivity(intent);
    }
}
