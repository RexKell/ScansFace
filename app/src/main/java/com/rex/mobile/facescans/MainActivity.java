package com.rex.mobile.facescans;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.scansface.mobile.scansface.ScansFaceActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void openScans(View view){
        if (Build.VERSION.SDK_INT>23){
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA},0x11);
            }else {
                scans();
            }
        }else {
           scans();
        }


    }
    public void scans(){
        String path= getExternalFilesDir(null).getAbsolutePath()+"/"+"image/";
        Intent faceIntent =new Intent(this, ScansFaceActivity.class);
        String imgPath=path+"face_set"+".jpg";
        faceIntent.putExtra("imgPath",imgPath);
        startActivityForResult(faceIntent,0x120);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==0x11&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            scans();
        }
    }
}