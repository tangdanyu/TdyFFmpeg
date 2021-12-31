package com.example.tdyffmpeg.ffmpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.tdyffmpeg.R;

public class FFmpegTranscoderActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startButton;
    private EditText cmdEt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_transcoder);
        initView();
        initListener();
    }
    private void initView(){
        startButton = findViewById(R.id.btn_start);
        cmdEt = findViewById(R.id.et_cmd);
    }
    private void initListener(){
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                String cmdline=cmdEt.getText().toString();
                String[] argv=cmdline.split(" ");
                Integer argc=argv.length;
                ffmpegcore(argc,argv);
                break;
        }
    }

    //JNI
    public native int ffmpegcore(int argc,String[] argv);
    static{
        System.loadLibrary("avutil-54");
        System.loadLibrary("swresample-1");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swscale-3");
        System.loadLibrary("postproc-53");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("avdevice-56");
        System.loadLibrary("sfftranscoder");
    }

}