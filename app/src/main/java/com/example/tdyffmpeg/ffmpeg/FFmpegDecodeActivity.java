package com.example.tdyffmpeg.ffmpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.tdyffmpeg.R;

/**
 * 将输入的视频数据解码成YUV像素数据。
 */
public class FFmpegDecodeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startButton;
    private EditText urlInputEt;
    private EditText urlOutputEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_decode);
        initView();
        initListener();
    }
    private void initView(){
        startButton = findViewById(R.id.btn_start);
        urlInputEt = findViewById(R.id.et_input_url);
        urlOutputEt = findViewById(R.id.et_output_url);
    }
    private void initListener(){
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                String folderurl= Environment.getExternalStorageDirectory().getPath();

                String urltext_input=urlInputEt.getText().toString();
                String inputurl=folderurl+"/"+urltext_input;

                String urltext_output=urlOutputEt.getText().toString();
                String outputurl=folderurl+"/"+urltext_output;

                Log.e("inputurl",inputurl);
                Log.e("outputurl",outputurl);

                decode(inputurl,outputurl);
                break;
        }
    }

    //JNI
    public native int decode(String inputurl, String outputurl);

    static{
        System.loadLibrary("avutil-54");
        System.loadLibrary("swresample-1");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swscale-3");
        System.loadLibrary("postproc-53");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("avdevice-56");
        System.loadLibrary("sffdecoder");
    }

}