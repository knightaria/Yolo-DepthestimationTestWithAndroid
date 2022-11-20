package com.daniel.yolodepthestimationtest.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;

import android.annotation.SuppressLint;
import android.content.pm.ModuleInfo;
import android.os.Bundle;
import android.util.Size;
import android.view.WindowManager;
import android.widget.TextView;

import com.daniel.yolodepthestimationtest.R;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NonNls;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class YoloActivity extends AppCompatActivity {

    PreviewView prvv_cameraView;
    TextView txt_Result;
    Executor executor = Executors.newSingleThreadExecutor();
    Module module;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    List<String> imagenet_classes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_yolo);

        init();
        imagenet_classes = loadClasses("imagenet-classes.txt");
        loadTorchModel("YoloModel.ptl");

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e){

            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void init(){
        txt_Result = findViewById(R.id.txt_Result);
        prvv_cameraView = findViewById(R.id.prvv_cameraView);
    }



    void startCamera (@NonNls ProcessCameraProvider cameraProvider){
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(prvv_cameraView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(224, 224)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(executor, image -> {
            int rotation = image.getImageInfo().getRotationDegrees();
            analyzeImage(image, rotation);
            image.close();
        });

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
    }

    void loadTorchModel(String fileName){
        File madeFile = new File(this.getFilesDir(), fileName);
        try {
            if (!madeFile.exists()){
                InputStream inputStream = getAssets().open(fileName);
                FileOutputStream outputStream = new FileOutputStream(madeFile);
                byte[] buffer = new byte[2048];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();
            }
            module = LiteModuleLoader.load(madeFile.getAbsolutePath());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    void analyzeImage(ImageProxy image,  int rotation){
        @SuppressLint("UnsafeOptInUsageError") Tensor inputTensor = TensorImageUtils.imageYUV420CenterCropToFloat32Tensor(image.getImage(), rotation,
                224, 224, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

                Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        float[] score = outputTensor.getDataAsFloatArray();
        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;
        for (int i = 0; i < score.length; i++){
            if (score[i] > maxScore){
                maxScore = score[i];
                maxScoreIdx = i;
            }
        }
        String classResult = imagenet_classes.get(maxScoreIdx);

        runOnUiThread(() -> {
            txt_Result.setText(classResult);
        });
    }

    List<String> loadClasses(String fileName){
        List<String> classes = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));
            String line;
            while ((line = br.readLine()) != null){
                classes.add(line);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        return classes;
    }

}