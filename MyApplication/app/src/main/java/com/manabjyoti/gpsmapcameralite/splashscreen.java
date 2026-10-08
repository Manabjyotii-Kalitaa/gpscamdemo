package com.manabjyoti.gpsmapcameralite;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.manabjyoti.gpsmapcameralite.databinding.ActivitySplashscreenBinding;

public class splashscreen extends AppCompatActivity {

    private static final int permission_code = 1234;
    Animation logo, logoback;

    ActivitySplashscreenBinding activitySplashscreenBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activitySplashscreenBinding = DataBindingUtil.setContentView(this, R.layout.activity_splashscreen);
        logo = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_anim);
        logoback = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logoback_anim);
        activitySplashscreenBinding.imagelogo.setAnimation(logo);
        activitySplashscreenBinding.appnamesplash.setAnimation(logo);

        reqpermission();
        activitySplashscreenBinding.buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reqpermission();
                    }
                }, 1000);
            }
        });


    }

    private void reqpermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_MEDIA_IMAGES};
                requestPermissions(permission, permission_code);
            } else {
                tocamera();
            }

        } else {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, permission_code);
            } else {
                tocamera();
            }

        }
    }

    private void tocamera() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }, 3000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case permission_code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    tocamera();
                } else {
                    Toast.makeText(this, "Permission needed to proceed", Toast.LENGTH_SHORT).show();
                    activitySplashscreenBinding.buttonProceed.setVisibility(View.VISIBLE);
                }
        }





       /* if (requestCode == permission_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    tocamera();
            } else {
                Toast.makeText(this, "Permission required to use this application", Toast.LENGTH_SHORT).show();
            }
        } else {
            reqpermission();
        }*/
    }
}