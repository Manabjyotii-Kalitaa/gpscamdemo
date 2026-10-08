package com.manabjyoti.gpsmapcameralite;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.manabjyoti.gpsmapcameralite.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {
    ActivityMainBinding activityMainBinding;
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    Camera camera;
    Boolean isSelected = true, flashOn = false, isCaptured = false;
    Preview preview;
    Dialog dialog;
    private ImageCapture imagecapture;
    OrientationEventListener orientationEventListener, orientationLayoutListener;
    Uri uri;
    Bitmap bitmap;
    String timeStamp1;
    String latitude_value_txt, longitude_value_txt;
    List<Address> addressList;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    public static final int INT = 30;
    public static final int INT1 = 5;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setCommonToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        final Loadingbar loadingbar = new Loadingbar(MainActivity.this);
        loadingbar.showdialog();
        loadingbar.dismissbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                    .setIntervalMillis(100 * INT)
                    .setMinUpdateIntervalMillis(100 * INT1)
                    .setMaxUpdateDelayMillis(100 * 2 * INT1)
                    .setMinUpdateDistanceMeters(0)
                    .build();
        }
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUI(locationResult.getLastLocation());
            }
        };

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        openCamera();
        locationDetail();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!addressList.isEmpty()) {
                        activityMainBinding.addressValue.setText(addressList.get(0).getAddressLine(0));
                    } else {
                        activityMainBinding.addressValue.setText(R.string.data_is_not_available_now);
                    }
                    if (!latitude_value_txt.isEmpty()) {
                        activityMainBinding.latitudeValue.setText(latitude_value_txt+"\u00B0");
                    } else {
                        activityMainBinding.latitudeValue.setText(R.string.data_is_not_available_now);
                    }
                    if (!longitude_value_txt.isEmpty()) {
                        activityMainBinding.longitudeValue.setText(longitude_value_txt+"\u00B0");
                    } else {
                        activityMainBinding.longitudeValue.setText(R.string.data_is_not_available_now);
                    }
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Press the trace button again", Toast.LENGTH_SHORT).show();
                }
            }
        }, 3000);

        activityMainBinding.captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCaptured = true;
                loadingbar.showcapturedialog();
                loadingbar.dismissbar();
                locationDetail();
                onMapReady(mMap);
                //snapmap();
                //caputrPhoto();

            }
        });
        activityMainBinding.rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Release the current camera
                    cameraProvider.unbindAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Switch camera selector
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    isSelected = false;
                } else {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    isSelected = true;
                }
                flashOn = false;
                activityMainBinding.flashlight.setImageResource(R.drawable.baseline_flash_off_24);

                // Restart camera with new camera selector
                startcamerax(cameraProvider);
            }
        });
        activityMainBinding.thumbnailview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null) {
                    showThumbnail();
                } else {
                    Toast.makeText(MainActivity.this, "capture an image first", Toast.LENGTH_SHORT).show();
                }

            }
        });
        activityMainBinding.flashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flashOn) {
                    setFlashOn(camera);
                    flashOn = true;
                } else {
                    camera.getCameraControl().enableTorch(false);
                    activityMainBinding.flashlight.setImageResource(R.drawable.baseline_flash_off_24);
                    flashOn = false;
                }
            }
        });
    }

    private void setFlashOn(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                activityMainBinding.flashlight.setImageResource(R.drawable.baseline_flash_on_24);
            }
        }
    }

    private void updateUI(Location location1) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            addressList = geocoder.getFromLocation(location1.getLatitude(), location1.getLongitude(), 1);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to get address right now", Toast.LENGTH_SHORT).show();
        }
        latitude_value_txt = (String.valueOf(location1.getLatitude()));
        longitude_value_txt = (String.valueOf(location1.getLongitude()));
        updateTime();

    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mma 'GMT' Z");
        int timeZoneOffsetHours = TimeZone.getTimeZone("GMT+05:30").getRawOffset() / (60 * 60 * 1000);
        int timeZoneOffsetMinutes = Math.abs(TimeZone.getTimeZone("GMT+05:30").getRawOffset() / (60 * 1000)) % 60;
        //String timeZoneOffset = String.format("%+03d:%02d", timeZoneOffsetHours, timeZoneOffsetMinutes);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        timeStamp1 = sdf.format(new Date());
        activityMainBinding.timeValue.setText(timeStamp1);
    }

    @SuppressLint("MissingPermission")
    private void locationDetail() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                updateUI(location);
            }
        });


    }

    private Bitmap flipBitmapVertically(Bitmap bitmap2) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, bitmap2.getWidth() / 2f, bitmap2.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, true);
    }

    private void caputrPhoto() {
        if (imagecapture == null) return;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();
        imagecapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uri = outputFileResults.getSavedUri();
                        bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int maxWidth = displayMetrics.widthPixels;
                            int maxHeight = displayMetrics.heightPixels;
                            bitmap = resizeBitmap(bitmap, maxWidth, maxHeight);
                            if (!isSelected) {
                                bitmap = flipBitmapVertically(bitmap);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        saveImage(addLayoutToBitmap(bitmap, activityMainBinding.linearTop));

                    }
                }, 2000);

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();

            }
        });
    }

    public Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float aspectRatio = (float) width / (float) height;

        if (width > maxWidth || height > maxHeight) {
            if (aspectRatio > 1) {
                width = maxWidth;
                height = (int) (width / aspectRatio);
            } else {
                height = maxHeight;
                width = (int) (height * aspectRatio);
            }
        }

        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }

    private void saveImage(Bitmap bitmap2) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        try {
            if (uri != null) {
                FileOutputStream outputStream = (FileOutputStream) getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                    //Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        activityMainBinding.thumbnailview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (bitmap2 != null) {
            activityMainBinding.thumbnailview.setImageBitmap(bitmap2);
        } else {
            Toast.makeText(this, "Image not captured", Toast.LENGTH_SHORT).show();
        }
    }
    // T0 ADD LAYOUT VIEW TO BITMAP

    private Bitmap addLayoutToBitmap(Bitmap bitmap, View layout) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Bitmap lbitmap = getBitmapFrom(layout);
        float scaleX = (float) mutableBitmap.getWidth() / (float) lbitmap.getWidth();
        float scaleY = (float) mutableBitmap.getHeight() / (float) lbitmap.getHeight();
        Bitmap scaledLinearTopBitmap = Bitmap.createScaledBitmap(lbitmap, (int) (lbitmap.getWidth() * scaleX), lbitmap.getHeight(), true);
        int bw = mutableBitmap.getWidth();
        int lw = scaledLinearTopBitmap.getWidth();
        int bh = mutableBitmap.getHeight();
        int lh = scaledLinearTopBitmap.getHeight();
        int centreX = (bw - lw) / 2;
        int centreY = (bh - lh);
        canvas.drawBitmap(scaledLinearTopBitmap, centreX, centreY, null);
        return mutableBitmap;
    }

    private Bitmap getBitmapFrom(View layout) {
        Bitmap l_bitmap1 = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(l_bitmap1);
        layout.draw(canvas);
        return l_bitmap1;
    }

    private void showThumbnail() {
        Bitmap bit;
        try {
            bit = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.activity_dialog_details);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView rootImage = dialog.findViewById(R.id.root_image);
        Button button = dialog.findViewById(R.id.button_ok);

        rootImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rootImage.setImageBitmap(bit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    //TO OPEN THE CAMERA
    private void openCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = (ProcessCameraProvider) cameraProviderListenableFuture.get();
                    startcamerax(cameraProvider);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }, ContextCompat.getMainExecutor(this));

    }
    private int aspectratio(int width, int height) {
        double previewratio = Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewratio - 4.0/3.0) <= Math.abs(previewratio - 16.0/9.0)) {
            return AspectRatio.RATIO_4_3;
        } else {
            return AspectRatio.RATIO_16_9;
        }
    }

    //TO PREVIEW THE CAMERA
    private void startcamerax(ProcessCameraProvider cameraProvider) {
        if (isSelected) {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        } else {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        }
        int screenAspectRatio = aspectratio(activityMainBinding.l1.getWidth(), activityMainBinding.l1.getHeight());
        int rotation = activityMainBinding.l1.getDisplay().getRotation();
        ResolutionSelector resolutionSelector = new ResolutionSelector.Builder().setAspectRatioStrategy(new AspectRatioStrategy(screenAspectRatio,AspectRatioStrategy.FALLBACK_RULE_AUTO)).build();
        preview = new Preview.Builder().setResolutionSelector(resolutionSelector).setTargetRotation(rotation).build();
        preview.setSurfaceProvider(activityMainBinding.l1.getSurfaceProvider());
        imagecapture = new ImageCapture.Builder().build();
        orientationEventListener = new OrientationEventListener((Context)this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;

                // Monitors orientation values to determine the target rotation value
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }

                imagecapture.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imagecapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LatLng loco = new LatLng(Double.parseDouble(latitude_value_txt), Double.parseDouble(longitude_value_txt));
                mMap.addMarker(new MarkerOptions().position(loco).title("You are here"));
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loco, 18));
                if (isCaptured) {
                    isCaptured = false;
                    snapmap1();
                }

            }
        }, 4000);

    }

    private void snapmap1() {
        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    activityMainBinding.gmaps.setVisibility(View.VISIBLE);
                    activityMainBinding.gmaps.setImageBitmap(bitmap);
                }
            }
        });
        caputrPhoto();
    }

    private void snapmap() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(@Nullable Bitmap bitmap) {
                        if (bitmap != null) {
                            activityMainBinding.gmaps.setVisibility(View.VISIBLE);
                            activityMainBinding.gmaps.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        }, 4000);
    }
}