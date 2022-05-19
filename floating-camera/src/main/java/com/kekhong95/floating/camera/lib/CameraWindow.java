package com.kekhong95.floating.camera.lib;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import static android.content.Context.WINDOW_SERVICE;

public class CameraWindow {
    private final Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams rootParams;
    private RelativeLayout rootLayout;
    private CameraCustomView mPreview;
    private int xMargin = 0;
    private int yMargin = 0;
    private int statusBarHeight = 0;
    private boolean dragFlag = false;
    private CameraCustomView.CameraCustomViewListener listener;

    public CameraWindow(Context context, CameraCustomView.CameraCustomViewListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void create() {
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        statusBarHeight = (int) (24 * context.getResources().getDisplayMetrics().density);
        rootLayout = new RelativeLayout(context);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        rootParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        rootParams.gravity = Gravity.TOP | Gravity.START;


        mPreview = new CameraCustomView(context);
        mPreview.setListener(new CameraCustomView.CameraCustomViewListener() {
            @Override
            public void onClose() {
                try {
                    windowManager.removeView(rootLayout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listener != null) listener.onClose();
            }

            @Override
            public void onError(Exception exception) {
                if (listener != null) listener.onError(exception);
            }
        });
        rootLayout.addView(mPreview);
        windowManager.addView(rootLayout, rootParams);
        addDragFunction();
    }

    public void close() {
        if (mPreview != null) {
            try {
                mPreview.close();
                windowManager.removeView(rootLayout);
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) listener.onClose();
            }
        }
    }

    private void addDragFunction() {
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction = MotionEvent.ACTION_UP;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    xMargin = (int) motionEvent.getX();
                    yMargin = (int) motionEvent.getY();
                    dragFlag = rootParams.height - yMargin < dpToPx(20) && rootParams.width - xMargin < dpToPx(20);
                    if (lastAction != MotionEvent.ACTION_DOWN && mPreview != null) {
                        mPreview.showButton();
                        lastAction = motionEvent.getAction();
                    }
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP && lastAction == MotionEvent.ACTION_DOWN) {
                    mPreview.runTimeout();
                    lastAction = motionEvent.getAction();
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int rawX = (int) motionEvent.getRawX();
                    int rawY = (int) motionEvent.getRawY();
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    WindowManager.LayoutParams rootParams = (WindowManager.LayoutParams) rootLayout.getLayoutParams();

                    if (dragFlag) {
                        rootParams.width = x;
                        rootParams.height = y;
                    } else {
                        rootParams.x = rawX - xMargin;
                        rootParams.y = rawY - yMargin - statusBarHeight;
                    }
                    windowManager.updateViewLayout(rootLayout, rootParams);

                }

                return true;
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
