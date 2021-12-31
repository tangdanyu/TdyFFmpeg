package com.example.tdyffmpeg.ffmpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.tdyffmpeg.R;

public class FFmpegEncodeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startButton;
    private EditText urlInputEt;
    private EditText urlOutputEt;
    private EditText resolutionEt;
    private EditText settingsEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_encode);
        initView();
        initListener();
    }

    private void initView() {
        startButton = findViewById(R.id.btn_start);
        urlInputEt = findViewById(R.id.et_input_url);
        urlOutputEt = findViewById(R.id.et_output_url);
        resolutionEt = findViewById(R.id.et_input_yuv_resolution);
        settingsEt = findViewById(R.id.et_encode_settings);
    }

    private void initListener() {
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                String folderurl = Environment.getExternalStorageDirectory().getPath();

                String str_input = urlInputEt.getText().toString();
                String str_input_full = folderurl + "/" + str_input;

                String str_resolution = resolutionEt.getText().toString();
                String str_settings = settingsEt.getText().toString();

                String str_output = urlOutputEt.getText().toString();
                String str_output_full = folderurl + "/" + str_output;

                Log.i("inputurl", str_input_full);
                Log.i("outputurl", str_output_full);

                encode(str_input_full, str_resolution, str_settings, str_output_full);
                break;
        }
    }

    //JNI
    public native int encode(String inputurl, String resolution, String settings, String outputurl);

    static {
        System.loadLibrary("avutil-54");
        System.loadLibrary("swresample-1");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swscale-3");
        System.loadLibrary("postproc-53");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("avdevice-56");
        System.loadLibrary("sffencoder");
    }

}