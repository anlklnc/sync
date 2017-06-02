package com.nutomic.syncthingandroid.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nutomic.syncthingandroid.R;

public class FirstStartActivity extends Activity implements Button.OnClickListener {

    private static final int REQUEST_WRITE_STORAGE = 142;

    private SharedPreferences mPreferences;

    /**
     * Handles activity behaviour depending on {@link #isFirstStart()} and {@link #haveStoragePermission()}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Button cont = (Button) findViewById(R.id.cont);
        cont.setOnClickListener(this);

        // Set VM policy to avoid crash when sending folder URI to file manager.
        StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build();
        StrictMode.setVmPolicy(policy);

        if (!isFirstStart()) {
            if (haveStoragePermission()) {
                startApp();
            }
            else {
                requestStoragePermission();
            }
        }
    }

    private boolean isFirstStart() {
        return mPreferences.getBoolean("first_start", true);
    }

    private void startApp() {
        boolean isFirstStart = isFirstStart();
        if (isFirstStart) {
            mPreferences.edit().putBoolean("first_start", false).apply();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_FIRST_START, isFirstStart);
        startActivity(intent);
        finish();

    }

    private boolean haveStoragePermission() {
        int permissionState = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    @Override
    public void onClick(View v) {
        if (!haveStoragePermission()) {
            requestStoragePermission();
        }
        else {
            startApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length == 0 ||
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.toast_write_storage_permission_required,
                            Toast.LENGTH_LONG).show();
                } else {
                    startApp();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
