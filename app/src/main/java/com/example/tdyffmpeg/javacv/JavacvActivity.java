package com.example.tdyffmpeg.javacv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.tdyffmpeg.R;
import com.example.tdyffmpeg.javacv.activity.CameraDemoActivity;
import com.example.tdyffmpeg.javacv.activity.VideoPlayerActivity;

public class JavacvActivity extends AppCompatActivity implements View.OnClickListener {


    private Button cameraBtn;
    private Button playerBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_javacv);
        initView();
        initListener();
    }

    private void initView() {
        cameraBtn = findViewById(R.id.btn_camera);
        playerBtn = findViewById(R.id.btn_player);

    }

    private void initListener() {
        cameraBtn.setOnClickListener(this);
        playerBtn.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                startActivity(new Intent(this, CameraDemoActivity.class));
                break;
            case R.id.btn_player:
                startActivity(new Intent(this, VideoPlayerActivity.class));
                break;
        }
    }
}