package com.kekhong95.floating.camera.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class CameraCustomView extends RelativeLayout implements SurfaceHolder.Callback {
    private Camera mCamera = null;
    private Camera.Parameters cameraParams;
    private SurfaceView surfaceView;
    private ImageView ivClose;
    private ImageView ivRotate;
    private ImageView ivResize;
    private ImageView ivFlash;
    private View container;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isTouch = false;


    public CameraCustomView(Context context) {
        super(context);
        init();
    }

    public CameraCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private CameraCustomViewListener listener;

    public void setListener(CameraCustomViewListener listener) {
        this.listener = listener;
    }

    public void close() {
        surfaceView.getHolder().removeCallback(this);
        releaseCamera();
    }

    public interface CameraCustomViewListener {
        void onClose();

        void onError(Exception exception);
    }

    void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        addView(inflater.inflate(R.layout.custom_camera_view, this, false));
        ivClose = findViewById(R.id.btn_close);
        ivFlash = findViewById(R.id.btn_flash);
        ivResize = findViewById(R.id.btn_resize);
        ivRotate = findViewById(R.id.btn_rotate);
        surfaceView = findViewById(R.id.surface_view);
        container = findViewById(R.id.container);
        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mCamera.stopPreview();
                    mCamera.release();
                    if (listener != null) listener.onClose();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) listener.onError(e);
                }
            }
        });
        ivResize.setOnTouchListener(new View.OnTouchListener() {
            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        container.getLayoutParams().width = container.getLayoutParams().width + x;
                        container.getLayoutParams().height = container.getLayoutParams().height + y;
                        container.requestLayout();
                        break;
                    case MotionEvent.ACTION_DOWN:
                        isTouch = true;
                        try {
                            mCamera.stopPreview();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        try {
                            mCamera.startPreview();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        runTimeout();
                        break;
                }
                return true;
            }
        });
        ivFlash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    cameraParams = mCamera.getParameters();
                    if (cameraParams.getFlashMode().equalsIgnoreCase(Camera.Parameters.FLASH_MODE_TORCH)) {
                        cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        ivFlash.setImageResource(R.drawable.ic_flash_on);
                    } else {
                        cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        ivFlash.setImageResource(R.drawable.ic_flash_off);
                    }
                    mCamera.setParameters(cameraParams);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) listener.onError(e);
                }
            }
        });
        ivRotate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    releaseCamera();
                    startNewCamera();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mCamera = Camera.open(currentCameraId);
        applyCameraSetting();
        surfaceView.getHolder().addCallback(this);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceView.setZOrderOnTop(false);
    }

    public void showButton() {
        isTouch = true;
        if (ivRotate != null) ivRotate.setVisibility(VISIBLE);
        if (ivClose != null) ivClose.setVisibility(VISIBLE);
        if (ivFlash != null) ivFlash.setVisibility(VISIBLE);
        if (ivResize != null) ivResize.setVisibility(VISIBLE);
    }

    public void hideController() {
        if (ivRotate != null) ivRotate.setVisibility(INVISIBLE);
        if (ivClose != null) ivClose.setVisibility(INVISIBLE);
        if (ivFlash != null) ivFlash.setVisibility(INVISIBLE);
        if (ivResize != null) ivResize.setVisibility(INVISIBLE);
    }


    public void runTimeout() {
        isTouch = false;
        postDelayed(new Runnable() {
            @Override
            public void run() {
               if (!isTouch) hideController();
            }
        },2000);
    }

    private void startNewCamera() {
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        try {
            mCamera = Camera.open(currentCameraId);
            applyCameraSetting();
            mCamera.setPreviewDisplay(surfaceView.getHolder());
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) listener.onError(e);
        }
    }

    private void releaseCamera() {
        try {
            mCamera.stopPreview();
            mCamera.release();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onError(e);
        }
    }

    void applyCameraSetting() {
        mCamera.setDisplayOrientation(90);
        SharedPreferences preferences = getContext().getSharedPreferences("camera_preferences", Context.MODE_PRIVATE);
        cameraParams = mCamera.getParameters();
        String picture_size = preferences.getString(getContext().getString(R.string.picture_size_key), "320x240");
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            picture_size = preferences.getString(getContext().getString(R.string.picture_size_key), "320x240");
        }
        String s[] = picture_size.split("x");
        cameraParams.setPictureSize(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

        mCamera.setParameters(cameraParams);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
