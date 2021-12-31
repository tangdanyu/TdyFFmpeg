package com.example.tdyffmpeg.javacv.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.tdyffmpeg.R;
import com.example.tdyffmpeg.javacv.myUtils.PermissionUtil;
import com.example.tdyffmpeg.javacv.view.CameraGLSurfaceView;

public class CameraDemoActivity extends AppCompatActivity implements View.OnClickListener , ActivityCompat.OnRequestPermissionsResultCallback  {

    private String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 100;

    private Button mTakePicBtn;
    private Button mRecordBtn;
    private Button switchButton;
    private CameraGLSurfaceView mCameraSurfaceView;
    private SeekBar mSeekBar;

    public final static String LOG_TAG = CameraGLSurfaceView.LOG_TAG;

    public static CameraDemoActivity mCurrentInstance = null;

    public static CameraDemoActivity getInstance() {
        return mCurrentInstance;
    }

    public static final String FilterNames[] = {
            "原始",
            "波纹",
            "浮雕",
            "查找边缘",
            "LerpBlur"
    };

    public static final CameraGLSurfaceView.FilterButtons[] FilterTypes = {
            CameraGLSurfaceView.FilterButtons.Filter_Origin,
            CameraGLSurfaceView.FilterButtons.Filter_Wave,
            CameraGLSurfaceView.FilterButtons.Filter_Emboss,
            CameraGLSurfaceView.FilterButtons.Filter_Edge,
            CameraGLSurfaceView.FilterButtons.Filter_BlurLerp
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.takeShotBtn:
                Log.i(LOG_TAG, "Taking Picture...");
                mCameraSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        mCameraSurfaceView.takeShot();
                    }
                });
                break;
            case R.id.recordBtn:
                if (mCameraSurfaceView.isRecording()) {
                    Log.i(LOG_TAG, "End recording...");
                    mCameraSurfaceView.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                    mCameraSurfaceView.endRecording();
                    Log.i(LOG_TAG, "End recording OK");
                    Toast.makeText(CameraDemoActivity.this, "End Recording...", Toast.LENGTH_LONG).show();
                } else {
                    Log.i(LOG_TAG, "Start recording...");
                    mCameraSurfaceView.setClearColor(1.0f, 0.0f, 0.0f, 0.6f);
                    mCameraSurfaceView.startRecording(null);
                    Toast.makeText(CameraDemoActivity.this, "Start Recording...", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.switchBtn:
                mCameraSurfaceView.switchCamera();
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_demo);
        if (PermissionUtil.checkAndRequestPermissions(this, PERMISSIONS, requestCode)) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.menuLinearLayout);

            for (int i = 0; i != FilterTypes.length; ++i) {
                MyButtons button = new MyButtons(this);
                button.filterType = FilterTypes[i];
                button.setText(FilterNames[i]);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyButtons btn = (MyButtons)view;
                        mCameraSurfaceView.setFrameRenderer(btn.filterType);
                    }
                });
                layout.addView(button);
            }

            initView();
            initListener();
            mCurrentInstance = this;
        }
    }


    private void initView() {
        mTakePicBtn = findViewById(R.id.takeShotBtn);
        mRecordBtn = findViewById(R.id.recordBtn);
        switchButton = findViewById(R.id.switchBtn);
        mCameraSurfaceView = findViewById(R.id.myGLSurfaceView);
        mSeekBar = findViewById(R.id.seekBar);
    }

    private void initListener() {
        mTakePicBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);
        switchButton.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCameraSurfaceView.setIntensity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public class MyButtons extends androidx.appcompat.widget.AppCompatButton {

        public CameraGLSurfaceView.FilterButtons filterType;

        public MyButtons(Context context) {
            super(context);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGrant = true;

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("提示").setMessage("权限被禁止。\n请在【设置-应用信息-权限】中重新授权").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                isAllGrant = false;
                break;
            }
        }

        if (isAllGrant) {
            Log.e(LOG_TAG,"已经获得全部权限");
        }
    }
}
