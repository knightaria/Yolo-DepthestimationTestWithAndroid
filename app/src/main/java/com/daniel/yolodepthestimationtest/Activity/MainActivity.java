package com.daniel.yolodepthestimationtest.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import com.daniel.yolodepthestimationtest.R;

public class MainActivity extends AppCompatActivity {

    Button btn_Yolo, btn_DepthEstimation;
    private int REQUEST_CODE_PERMISSION = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        init();


    }

    private void init() {
        btn_Yolo = findViewById(R.id.btn_Yolo);
        btn_DepthEstimation = findViewById(R.id.btn_DepthEstimation);

        if (!checkPermission()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }

        listeners();
    }

    private void listeners() {
        btn_Yolo.setOnClickListener(v -> {
            Intent yoloIntent = new Intent(this, YoloActivity.class);
            startActivity(yoloIntent);
        });

        btn_DepthEstimation.setOnClickListener(v -> {
            Intent depthEstimationIntent = new Intent(this, DepthEstimationActivity.class);
            startActivity(depthEstimationIntent);
        });
    }

    private boolean checkPermission() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
