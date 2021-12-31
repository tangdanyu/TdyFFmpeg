package com.example.tdyffmpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

/**
 * 使用VideoView播放，暂停，停止视频
 */
public class VideoViewActivity extends AppCompatActivity implements View.OnClickListener {
    private VideoView videoView;
    private Button setButton;
    private Button startButton;
    private Button pauseButton;
    private Button stopButton;
    private EditText urlEdittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        initView();
        initListener();
    }

    private void initView() {
        videoView = findViewById(R.id.videoView);
        setButton = findViewById(R.id.btn_set);
        startButton = findViewById(R.id.btn_start);
        pauseButton = findViewById(R.id.btn_pause);
        stopButton = findViewById(R.id.btn_stop);
        urlEdittext = findViewById(R.id.et_input_url);
    }

    private void initListener() {
        setButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set:
                String folderurl = Environment.getExternalStorageDirectory().getPath();
                String urltext = urlEdittext.getText().toString();
                //如果以 / 开头，从第二个字符截取字符串
                if (urltext.charAt(0) == '/') {
                    urltext = urltext.substring(1);
                }
                String inputurl = folderurl + "/" + urltext;
                Log.e("url", inputurl);

                videoView.setVideoPath(inputurl);
                //videoView.setMediaController(new MediaController(MainActivity.this));

                break;
            case R.id.btn_start:
                videoView.start();
                break;
            case R.id.btn_pause:
                videoView.pause();
                break;
            case R.id.btn_stop:
                videoView.stopPlayback();
                break;

        }
    }
}