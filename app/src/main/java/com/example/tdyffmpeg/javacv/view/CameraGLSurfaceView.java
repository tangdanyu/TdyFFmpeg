package com.example.tdyffmpeg.javacv.view;

/**
 * Created by wangyang on 15/7/27.
 */


import static org.bytedeco.opencv.global.opencv_core.IPL_DEPTH_8U;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.tdyffmpeg.javacv.camera.CameraInstance;
import com.example.tdyffmpeg.javacv.myUtils.Common;
import com.example.tdyffmpeg.javacv.myUtils.ImageUtil;
import com.example.tdyffmpeg.javacv.recorder.MyRecorderWrapper;
import com.example.tdyffmpeg.javacv.texUtils.TextureRenderer;
import com.example.tdyffmpeg.javacv.texUtils.TextureRendererBlur;
import com.example.tdyffmpeg.javacv.texUtils.TextureRendererDrawOrigin;
import com.example.tdyffmpeg.javacv.texUtils.TextureRendererEdge;
import com.example.tdyffmpeg.javacv.texUtils.TextureRendererEmboss;
import com.example.tdyffmpeg.javacv.texUtils.TextureRendererLerpBlur;
import com.example.tdyffmpeg.javacv.texUtils.TextureRendererWave;


import org.bytedeco.opencv.opencv_core.IplImage;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wangyang on 15/7/17.
 */
public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    public static final String LOG_TAG = Common.LOG_TAG;

    public int viewWidth;
    public int viewHeight;

    private TextureRenderer mMyRenderer;

    private SurfaceTexture mSurfaceTexture;
    private int mTextureID;

    private MyRecorderWrapper mVideoRecorder;

    private static final int MAX_CACHED_FRAMES = 5;

    private IplImage[] mIplImageCaches;
    private LinkedList<IplImage> mImageList;

    private boolean mShouldRecord = false;

    public synchronized boolean isRecording() {
        return mShouldRecord;
    }

    private int[] mRecordStateLock = new int[0];

    private Context mContext;

    private float[] mTransformMatrix = new float[16];

    class CachedFrame {
        IplImage image;
        long frameTimeMillis;
        long frameNanoTime;

        CachedFrame(IplImage img, long timeMill, long timeNano) {
            image = img;
            frameTimeMillis = timeMill;
            frameNanoTime = timeNano;
        }
    }

    private Queue<CachedFrame> mFrameQueue;

    private CachedFrame makeFrame(IplImage img) {
        return new CachedFrame(img, System.currentTimeMillis(), System.nanoTime());
    }

    private void pushCachedFrame(IplImage img) {
        synchronized (mFrameQueue) {
            mFrameQueue.offer(makeFrame(img));
        }
    }

    private CachedFrame getCachedFrame() {
        synchronized (mFrameQueue) {
            return mFrameQueue.poll();
        }
    }

    //回收使用过的缓存帧
    private void recycleCachedFrame(CachedFrame frame) {
        synchronized (mImageList) {
            mImageList.offer(frame.image);
        }
    }

    //获取空闲的缓存帧
    private IplImage getImageCache() {
        synchronized (mImageList) {
            return mImageList.poll();
        }
    }


    class RecordingRunnable implements Runnable {

        @Override
        public void run() {
            while(mShouldRecord)
            {
                CachedFrame frame = getCachedFrame();

                if(frame == null)
                {
                    synchronized (this) {
                        try {
                            this.wait(50);
                        } catch (InterruptedException e) {
                            Log.e(LOG_TAG, "Recording runnable wait() : " + e.getMessage());
                        }

                        if(!mShouldRecord) {
                            return;
                        }
                    }
                    continue;
                }

                if(mVideoRecorder != null && mVideoRecorder.isRecording()) {
                    mVideoRecorder.writeFrame(frame.image);
                }
                recycleCachedFrame(frame);
            }
        }
    }

    RecordingRunnable mRecordingRunnable;
    Thread mRecordingThread;

    public TextureRenderer.Viewport drawViewport;

    public class ClearColor {
        public float r, g, b, a;
    }

    public ClearColor clearColor;

    public void setClearColor(float r, float g, float b, float a) {
        clearColor.r = r;
        clearColor.g = g;
        clearColor.b = b;
        clearColor.a = a;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
            }
        });
    }

    public enum FilterButtons {
        Filter_Origin,
        Filter_Wave,
        Filter_Blur,
        Filter_Emboss,
        Filter_Edge,
        Filter_BlurLerp,
    }

    public synchronized void switchCamera() {
        if(mMyRenderer != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if(mMyRenderer == null) {
                        Log.e(LOG_TAG, "Error: switchCamera after release!!");
                        return;
                    }

                    cameraInstance().stopCamera();

                    int facing = (cameraInstance().getFacing() + 1) % 2;

                    mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
                    mMyRenderer.setFlipscale(1.0f, 1.0f);

                    cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
                        @Override
                        public void cameraReady() {
                            if (!cameraInstance().isPreviewing()) {
                                Log.i(LOG_TAG, "## switch camera -- start preview...");
                                cameraInstance().startPreview(mSurfaceTexture);
                                mMyRenderer.setTextureSize(cameraInstance().previewHeight(), cameraInstance().previewWidth());
                            }
                        }
                    }, facing);

                    requestRender();
                }
            });
        }
    }

    public synchronized void setIntensity(final int value) {
        if(mMyRenderer == null)
            return ;

        if(mMyRenderer instanceof TextureRendererBlur) {
            final TextureRendererBlur r = (TextureRendererBlur)mMyRenderer;

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    r.setSamplerRadius(value);
                }
            });
        }
        else if(mMyRenderer instanceof TextureRendererLerpBlur) {
            final TextureRendererLerpBlur r = (TextureRendererLerpBlur)mMyRenderer;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    r.setIntensity((int)(value / 100.0f * 16.0f));
                }
            });
        }
    }

    public synchronized void setFrameRenderer(final FilterButtons filterID) {
        Log.i(LOG_TAG, "setFrameRenderer to " + filterID);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                TextureRenderer renderer = null;
                boolean isExternalOES = true;
                switch (filterID) {
                    case Filter_Origin:
                        renderer = TextureRendererDrawOrigin.create(isExternalOES);
                        break;
                    case Filter_Wave:
                        renderer = TextureRendererWave.create(isExternalOES);
                        if (renderer != null)
                            ((TextureRendererWave) renderer).setAutoMotion(0.4f);
                        break;
                    case Filter_Blur:
                        renderer = TextureRendererBlur.create(isExternalOES);
                        if(renderer != null) {
                            ((TextureRendererBlur) renderer).setSamplerRadius(50.0f);
                        }
                        break;
                    case Filter_Edge:
                        renderer = TextureRendererEdge.create(isExternalOES);
                        break;
                    case Filter_Emboss:
                        renderer = TextureRendererEmboss.create(isExternalOES);
                        break;
                    case Filter_BlurLerp:
                        renderer = TextureRendererLerpBlur.create(isExternalOES);
                        if(renderer != null) {
                            ((TextureRendererLerpBlur) renderer).setIntensity(16);
                        }
                        break;
                    default:
                        break;
                }

                if (renderer != null) {
                    mMyRenderer.release();
                    mMyRenderer = renderer;
                    mMyRenderer.setTextureSize(cameraInstance().previewHeight(), cameraInstance().previewWidth());
                    mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
                    mMyRenderer.setFlipscale(1.0f, 1.0f);
                }

                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

                Common.checkGLError("setFrameRenderer...");
            }
        });

    }

    private CameraInstance cameraInstance() {
        return CameraInstance.getInstance();
    }

    public synchronized void startRecording(String filePath) {

        if(mShouldRecord) {
            endRecording();
        }
        mImageList = new LinkedList<>();
        mFrameQueue = new LinkedList<>();
        if(filePath == null || filePath.isEmpty())
            mVideoRecorder = new MyRecorderWrapper(mContext);
        else
            mVideoRecorder = new MyRecorderWrapper(filePath);

        mVideoRecorder.startRecording();
        mRecordingRunnable = new RecordingRunnable();
        mRecordingThread = new Thread(mRecordingRunnable);
        mRecordingThread.start();

        for( IplImage img :  mIplImageCaches) {
            mImageList.add(img);
        }

        synchronized (mRecordStateLock) {
            mShouldRecord = true;
        }
    }

    public synchronized void endRecording() {
        Log.i(LOG_TAG, "notify quit...");
        synchronized (mRecordStateLock) {
            mShouldRecord = false;
        }

        synchronized (mRecordingRunnable) {
            try {
                mRecordingRunnable.notifyAll();
            } catch (Exception e) {
            }
        }

        Log.i(LOG_TAG, "joining thread...");
        try {
            mRecordingThread.join();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Join recording thread err: " + e.getMessage());
        }

        mRecordingRunnable = null;
        mRecordingThread = null;

        Log.i(LOG_TAG, "saving recoring...");
        mVideoRecorder.saveRecording(mContext);

        mImageList.clear();
        mImageList = null;
        mFrameQueue.clear();
        mFrameQueue = null;

        Log.i(LOG_TAG, "recording OK");
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(LOG_TAG, "MyGLSurfaceView Construct...");

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 8, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setZOrderOnTop(true);

        clearColor = new ClearColor();

        mIplImageCaches = new  IplImage[MAX_CACHED_FRAMES];

        for(int i = 0; i != MAX_CACHED_FRAMES; ++i)
        {
            mIplImageCaches[i] =  IplImage.create(640, 480,  IPL_DEPTH_8U, 4);
        }
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(LOG_TAG, "onSurfaceCreated...");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_STENCIL_TEST);

        mTextureID = genSurfaceTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        TextureRendererWave rendererWave = new TextureRendererWave();
        if(!rendererWave.init(true)) {
            Log.e(LOG_TAG, "init filter failed!\n");
        }

        rendererWave.setAutoMotion(0.4f);

        mMyRenderer = rendererWave;
        mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
        mMyRenderer.setFlipscale(1.0f, 1.0f);

        requestRender();

        cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
            @Override
            public void cameraReady() {
                Log.i(LOG_TAG, "tryOpenCamera OK...");
            }
        });
      
    }

    private void calcViewport() {
        float camHeight = (float)cameraInstance().previewWidth();
        float camWidth = (float)cameraInstance().previewHeight();

        drawViewport = new TextureRenderer.Viewport();

        float scale = Math.min(viewWidth / camWidth, viewHeight / camHeight);
        drawViewport.width = (int)(camWidth * scale);
        drawViewport.height = (int)(camHeight * scale);
        drawViewport.x = (viewWidth - drawViewport.width) / 2;
        drawViewport.y = (viewHeight - drawViewport.height) / 2;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(LOG_TAG, String.format("onSurfaceChanged: %d x %d", width, height));

        GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

        viewWidth = width;
        viewHeight = height;

        if(!cameraInstance().isPreviewing()) {
            cameraInstance().startPreview(mSurfaceTexture);
        }

        calcViewport();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mMyRenderer.release();
    }

    private long mTimeCount = 0;
    private long mFramesCount = 0;
    private long mLastTimestamp = 0;

    @Override
    public void onDrawFrame(GL10 gl) {

        if(mMyRenderer == null || mSurfaceTexture == null) {
            Log.e(LOG_TAG, "Invalid Texture Renderer!");
            return;
        }

        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransformMatrix);
        mMyRenderer.setTransform(mTransformMatrix);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mMyRenderer.renderTexture(mTextureID, drawViewport);

        synchronized (mRecordStateLock) {

            if (mShouldRecord && mVideoRecorder != null && mVideoRecorder.isRecording()) {
                IplImage imgCache = getImageCache();
                if (imgCache != null) {
                    GLES20.glReadPixels(drawViewport.x, drawViewport.y, 640, 480, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, imgCache.getByteBuffer());
                    pushCachedFrame(imgCache);

                    if(mRecordingRunnable != null) {
                        synchronized (mRecordingRunnable) {
                            try {
                                mRecordingRunnable.notifyAll();
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Notify failed: " + e.getMessage());
                            }
                        }
                    }

                } else {
                    Log.d(LOG_TAG, "Frame loss...");
                }
            }
        }

    }

    private IntBuffer mBuffer;

    public synchronized void takeShot() {
//        cameraInstance().stopPreview();

        if(mBuffer == null || mBuffer.limit() != drawViewport.width * drawViewport.height)
        {
            mBuffer = IntBuffer.allocate(drawViewport.width * drawViewport.height);
        }

        queueEvent(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "Taking shot...");
                GLES20.glReadPixels(drawViewport.x, drawViewport.y, drawViewport.width, drawViewport.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mBuffer);

                Bitmap bmp = Bitmap.createBitmap(drawViewport.width, drawViewport.height, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(mBuffer);
                ImageUtil.saveBitmap(bmp);
            }
        });

//        cameraInstance().startPreview(mSurfaceTexture);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume...");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause...");
        cameraInstance().stopCamera();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        Log.i(LOG_TAG, "onFrameAvailable...");
        if(mLastTimestamp == 0)
            mLastTimestamp = mSurfaceTexture.getTimestamp();

        long currentTimestamp = mSurfaceTexture.getTimestamp();

        ++mFramesCount;
        mTimeCount += currentTimestamp - mLastTimestamp;
        mLastTimestamp = currentTimestamp;
        if(mTimeCount >= 1e9)
        {
            Log.i(LOG_TAG, String.format("TimeCount: %d, Fps: %d", mTimeCount, mFramesCount));
            mTimeCount -= 1e9;
            mFramesCount = 0;
        }

//        Log.i(LOG_TAG, String.format("timestamp: %d", mSurfaceTexture.getTimestamp()));
        requestRender();
    }

    private int genSurfaceTextureID() {
        int[] texID = new int[1];
        GLES20.glGenTextures(1, texID, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texID[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texID[0];
    }
}
