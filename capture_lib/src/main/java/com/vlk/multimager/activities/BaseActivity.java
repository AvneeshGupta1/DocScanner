package com.vlk.multimager.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Created by vansikrishna on 08/06/2016.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public boolean hasStoragePermission(Context context) {
        int writePermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (writePermissionCheck == PackageManager.PERMISSION_DENIED
                || readPermissionCheck == PackageManager.PERMISSION_DENIED));
    }

    public boolean hasCameraPermission(Context context) {
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && cameraPermissionCheck == PackageManager.PERMISSION_DENIED);
    }


    public boolean validateGrantedPermissions(int[] grantResults) {
        boolean isGranted = true;
        if (grantResults != null && grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    isGranted = false;
                    break;
                }
            }
            return isGranted;
        } else {
            isGranted = false;
            return isGranted;
        }
    }

}
