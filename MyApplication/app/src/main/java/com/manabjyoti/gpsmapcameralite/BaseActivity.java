package com.manabjyoti.gpsmapcameralite;

import static java.lang.Math.log;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.pm.PackageInfoCompat;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import java.util.Objects;

public class BaseActivity extends AppCompatActivity {
    public static String MY_VERSION_NAME;
    public static int MY_VERSION_CODE;
    private AppUpdateManager appUpdateManager;

    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.camera) {
            startActivity(new Intent(this, MainActivity.class));
            //Toast.makeText(this, "Privacy policy", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.helpcenter) {
            helpcenterdetails();
        } else if (id == R.id.info) {
            infodialog();
        } else if (id == R.id.privacypolicy) {
            startActivity(new Intent(this, MainActivity_privacy.class));
            Toast.makeText(this, "Privacy policy", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void helpcenterdetails() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.contact_form);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        TextView bt = bottomSheetDialog.findViewById(R.id.send_textview);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Uri uri = Uri.parse("https://gpsmapcameralite.godaddysites.com/");
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    if (i != null) {
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Website is under maintenance", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bottomSheetDialog.show();
    }

    private void infodialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.version);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //Objects.requireNonNull(bottomSheetDialog.getWindow()).setBackgroundDrawableResource(R.drawable.bg);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        PackageManager manager = getApplicationContext().getPackageManager();
        TextView tv1 = bottomSheetDialog.findViewById(R.id.appinfo_name_textview);
        TextView tv2 = bottomSheetDialog.findViewById(R.id.appinfo_version_textview);
        TextView tv3 = bottomSheetDialog.findViewById(R.id.appinfo_reserved_textview);
        ImageView im = bottomSheetDialog.findViewById(R.id.appinfo_image);
        try {
            PackageInfo info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
            MY_VERSION_NAME = info.versionName;
            MY_VERSION_CODE = (int) PackageInfoCompat.getLongVersionCode(info);
            tv2.setText("VERSION: " + MY_VERSION_NAME + MY_VERSION_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            MY_VERSION_NAME = "Unknown-01";
            tv2.setText(MY_VERSION_NAME);
        }
        bottomSheetDialog.show();
    }

    protected void setCommonToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
    }

    private void appinUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        activityResultLauncher,
                        // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
            }
        });
        registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // handle callback
                        if (result.getResultCode() != RESULT_OK) {
                            log(Double.parseDouble("Update flow failed! Result code: " + result.getResultCode()));
                            // If the update is canceled or fails,
                            // you can request to start the update again.
                        }
                    }
                });
    }


}
