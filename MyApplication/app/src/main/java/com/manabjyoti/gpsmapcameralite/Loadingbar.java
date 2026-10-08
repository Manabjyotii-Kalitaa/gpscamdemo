package com.manabjyoti.gpsmapcameralite;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.view.LayoutInflater;

public class Loadingbar {
    Activity activity;
    AlertDialog alertDialog;

    public Loadingbar(Activity activity) {
        this.activity = activity;
    }

    void showdialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.prpgress_layout, null));
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    void showcapturedialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_capturing, null));
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    void dismissbar(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                alertDialog.dismiss();;
            }
        }, 5000);

    }
}
