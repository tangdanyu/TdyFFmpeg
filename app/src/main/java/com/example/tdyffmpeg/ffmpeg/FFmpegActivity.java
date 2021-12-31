package com.example.tdyffmpeg.ffmpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.tdyffmpeg.R;
import com.example.tdyffmpeg.camera.encode.FFmpegCameraEncodeActivity;

public class FFmpegActivity extends AppCompatActivity implements View.OnClickListener {

    private Button  cameraEncodeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);
        initView();
        initListener();
    }

    private void initView() {
        cameraEncodeBtn = findViewById(R.id.btn_camera_encode);

    }

    private void initListener() {
        cameraEncodeBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera_encode:
                startActivity(new Intent(this, FFmpegCameraEncodeActivity.class));
                break;
        }
    }
}