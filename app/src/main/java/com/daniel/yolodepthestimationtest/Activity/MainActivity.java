package com.daniel.yolodepthestimationtest.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import com.daniel.yolodepthestimationtest.R;

public class MainActivity extends AppCompatActivity {

    Button btn_Yolo, btn_DepthEstimation;

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
}
