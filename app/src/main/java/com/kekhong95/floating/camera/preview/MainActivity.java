package com.kekhong95.floating.camera.preview;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.kekhong95.floating.camera.lib.CameraCustomView;
import com.kekhong95.floating.camera.lib.CameraWindow;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_CODE_OVERLAY_PERMISSION = 100;
    public final static int REQUEST_CODE_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();
        else {
            doWork();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkPermissions() {
        boolean a = ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED;

        if (a)
            checkDrawOverlayPermission();
        else
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CODE_PERMISSION);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                checkDrawOverlayPermission();
            } else
                finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        } else
            doWork();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                doWork();
            } else
                finish();
        }
    }

    private void doWork() {
        CameraWindow window = new CameraWindow(this, new CameraCustomView.CameraCustomViewListener() {
            @Override
            public void onClose() {
                finish();
            }

            @Override
            public void onError(Exception exception) {

            }
        });
        window.create();
        moveTaskToBack(true);
    }

}