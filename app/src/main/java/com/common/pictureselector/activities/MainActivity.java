package com.common.pictureselector.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.common.pictureselector.R;

/**
 * 图片选择主页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnInsertPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_main);

        btnInsertPic = (Button) findViewById(R.id.btn_insert_picture);
        btnInsertPic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_insert_picture:
                openPictureSelector();
                break;
        }
    }

    private void openPictureSelector() {
        Log.e(MainActivity.class.getSimpleName(), "picture selector open!");
    }
}
