package com.example.tdyffmpeg.camera.encode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tdyffmpeg.R;
import com.example.tdyffmpeg.camera1.AutoFitTextureView;
import com.example.tdyffmpeg.camera1.Camera1Helper;
import com.example.tdyffmpeg.camera1.CameraListener;
import com.example.tdyffmpeg.javacv.camera.MyLogUtil;

import java.io.File;

public class FFmpegCameraEncodeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_CODE = 1000;
    private String TAG = "FFmpegCameraEncodeActivity";

    private Button switchCameraBtn;
    private Button takePictureBtn;
    private Button encodeMP4Btn;
    private AutoFitTextureView textureView;
    private Camera1Helper camera1Helper;
    private CameraListener cameraListener;
    private NativeEncoder mNativeFrame;
    private Size mPreviewSize;
    private boolean isEncoding = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_surface);
        applyPermission();
    }

    private void applyPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
        } else {
            initView();
            initListener();
            initDate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE && grantResults != null && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initView();
                initListener();
                initDate();
            }
        }
    }

    private void initView() {
        textureView = findViewById(R.id.texture_view);
        switchCameraBtn = findViewById(R.id.switch_camera_btn);
        takePictureBtn = findViewById(R.id.take_picture_btn);
        encodeMP4Btn = findViewById(R.id.encode_btn);
    }

    private void initListener() {
        switchCameraBtn.setOnClickListener(this);
        takePictureBtn.setOnClickListener(this);
        encodeMP4Btn.setOnClickListener(this);
        cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(int width, int height, int displayOrientation){
                byte[] buffer = new byte[1024];
                mNativeFrame.onPreviewFrame(buffer, 240, 480);
                mPreviewSize  = new Size(width,height);
            }

            @Override
            public void onCameraPreview(byte[] data, int width, int height, int displayOrientation) {
                if (mNativeFrame != null) {
                    mNativeFrame.onPreviewFrame(data, width, height);
                }
            }
        };
    }

    private void initDate() {
        camera1Helper = new Camera1Helper(this, textureView, cameraListener);
        mNativeFrame = new NativeEncoder();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch_camera_btn:
                if (camera1Helper != null) {
                    camera1Helper.switchCamera();
                }
                break;
            case R.id.take_picture_btn:
                if (camera1Helper != null) {
//                    camera1Helper.takePicture();
                    File outputFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpeg");
                    encodeJPEG(outputFile.getAbsolutePath());
                    MyLogUtil.e(TAG,outputFile.getAbsolutePath());
                }
                break;
            case R.id.encode_btn:
                if (camera1Helper != null) {
//                    if (camera1Helper.isRecordVideo()) {
                        if (isEncoding) {
                            encodeMP4Btn.setText("开始编码");
                            MyLogUtil.e(TAG, "停止编码");
                            encodeStop();
                            isEncoding = false;
//                            camera1Helper.stopRecord();
                        } else {
                            encodeMP4Btn.setText("停止编码");
                            MyLogUtil.e(TAG, "开始编码");
//                            camera1Helper.startRecord();
                            //".h264"
                            File outputFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".mp4");
                            MyLogUtil.e(TAG,outputFile.getAbsolutePath());
                            encodeStart(outputFile.getAbsolutePath());
                            isEncoding = true;
                        }
//                    } else {
//                        Toast.makeText(this, "没有开启录像", Toast.LENGTH_SHORT);
//                    }
                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera1Helper != null) {
            camera1Helper.onPause();
        }

    }

    @Override
    public void onResume() {
        if (camera1Helper != null) {
            camera1Helper.onResume();
        }
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera1Helper != null) {
            camera1Helper.onDestroy();
        }
    }

    public void encodeJPEG(String jpegPath) {
        if (mNativeFrame != null && mPreviewSize != null) {
            mNativeFrame.encodeJPEG(jpegPath, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }
    }
    public void encodeStart(String outputPath) {
        if (mNativeFrame != null && mPreviewSize != null) {
            mNativeFrame.encodeMP4Start(outputPath, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }
    }

    public void encodeStop() {
        if (mNativeFrame != null) {
            mNativeFrame.encodeMP4Stop();
        }
    }
}
