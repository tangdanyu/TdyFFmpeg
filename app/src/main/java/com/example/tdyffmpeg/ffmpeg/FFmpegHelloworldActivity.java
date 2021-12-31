package com.example.tdyffmpeg.ffmpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tdyffmpeg.R;

/**
 * 可以打印出FFmpeg类库的下列信息：
 * Protocol:  FFmpeg类库支持的协议
 * AVFormat:  FFmpeg类库支持的封装格式
 * AVCodec:   FFmpeg类库支持的编解码器
 * AVFilter:  FFmpeg类库支持的滤镜
 * Configure: FFmpeg类库的配置信息
 */
public class FFmpegHelloworldActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView libinfoTv;
    private Button configurationBtn;
    private Button urlprotocolBtn;
    private Button avformatBtn;
    private Button avcodecBtn;
    private Button avfilterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_helloworld);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        libinfoTv = findViewById(R.id.tv_libinfo);
        configurationBtn = findViewById(R.id.btn_configuration);
        urlprotocolBtn = findViewById(R.id.btn_urlprotocol);
        avformatBtn = findViewById(R.id.btn_avformat);
        avcodecBtn = findViewById(R.id.btn_avcodec);
        avfilterBtn = findViewById(R.id.btn_avfilter);
    }
    private void initListener(){
        configurationBtn.setOnClickListener(this);
        urlprotocolBtn.setOnClickListener(this);
        avformatBtn.setOnClickListener(this);
        avcodecBtn.setOnClickListener(this);
        avfilterBtn.setOnClickListener(this);
    }

    private void initData(){
        libinfoTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        libinfoTv.setText(configurationinfo());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_configuration:
                libinfoTv.setText(configurationinfo());
                break;
            case R.id.btn_urlprotocol:
                libinfoTv.setText(urlprotocolinfo());
                break;
            case R.id.btn_avformat:
                libinfoTv.setText(avformatinfo());
                break;
            case R.id.btn_avcodec:
                libinfoTv.setText(avcodecinfo());
                break;
            case R.id.btn_avfilter:
                libinfoTv.setText(avfilterinfo());
                break;

        }
    }
    //JNI
    public native String urlprotocolinfo();
    public native String avformatinfo();
    public native String avcodecinfo();
    public native String avfilterinfo();
    public native String configurationinfo();

    static{
        System.loadLibrary("avutil-54");
        System.loadLibrary("swresample-1");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swscale-3");
        System.loadLibrary("postproc-53");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("avdevice-56");
        System.loadLibrary("sffhelloworld");
    }

}