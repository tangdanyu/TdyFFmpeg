package com.example.tdyffmpeg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tdyffmpeg.ffmpeg.FFmpegActivity;
import com.example.tdyffmpeg.javacv.JavacvActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button videoViewBtn;
    private Button javacvBtn;
    private Button ffmpegBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
    }

    private void initView() {
        videoViewBtn = findViewById(R.id.btn_videoView);
        javacvBtn = findViewById(R.id.btn_javacv);
        ffmpegBtn = findViewById(R.id.btn_ffmpeg);
    }

    private void initListener() {
        videoViewBtn.setOnClickListener(this);
        javacvBtn.setOnClickListener(this);
        ffmpegBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_videoView:
                startActivity(new Intent(this, VideoViewActivity.class));
                break;
            case R.id.btn_javacv:
                startActivity(new Intent(this, JavacvActivity.class));
                break;
            case R.id.btn_ffmpeg:
                startActivity(new Intent(this, FFmpegActivity.class));
                break;
        }
    }
}
