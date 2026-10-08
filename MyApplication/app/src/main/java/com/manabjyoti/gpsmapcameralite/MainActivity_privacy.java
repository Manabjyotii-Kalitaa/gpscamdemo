package com.manabjyoti.gpsmapcameralite;

import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import androidx.databinding.DataBindingUtil;

import com.manabjyoti.gpsmapcameralite.databinding.ActivityMainPrivacyBinding;

public class MainActivity_privacy extends BaseActivity {
    ActivityMainPrivacyBinding activityMainPrivacyBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activityMainPrivacyBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_privacy);
        setCommonToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityMainPrivacyBinding.policyWebview.setWebViewClient(new WebViewClient());
        activityMainPrivacyBinding.policyWebview.loadUrl("file:///android_asset/privacy.html");
        WebSettings webSettings = activityMainPrivacyBinding.policyWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
    }
}