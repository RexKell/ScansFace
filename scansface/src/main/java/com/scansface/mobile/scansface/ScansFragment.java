package com.scansface.mobile.scansface;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.scansface.mobile.http.ScansService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * author: rexkell
 * date: 2020/10/28
 * explain:
 */
public class ScansFragment extends Fragment implements TextureView.SurfaceTextureListener,ScansFaceListener{
    private ScansFaceManager scansFaceManager;
    private View rootView;
    TextureView mTextureView;
    private ImageView imageView;
    private ScansFaceView scansFaceView;
    private String imgFilePath;
    private String refreshToken;
    private String systemCode;
    private String mode=FACE_ADD_MODE;
    public static final String FACE_ADD_MODE="add";
    public static final String FACE_SCANS_MODE="scans";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView =LayoutInflater.from(getContext()).inflate(R.layout.fg_scans_face,null);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       Bundle bundle= getArguments();
       imgFilePath= bundle.getString("imgPath");
       refreshToken=bundle.getString("refreshToken");
       systemCode=bundle.getString("systemCode");
       mode=bundle.getString("mode");
        initData();
    }
    private void initData(){
        scansFaceView=rootView.findViewById(R.id.scansface_view);
        mTextureView=rootView.findViewById(R.id.textureView);
        imageView=rootView.findViewById(R.id.img_preview);
        scansFaceManager=new ScansFaceManager(this,getActivity());
        scansFaceManager.initCamera(mTextureView,scansFaceView);
        mTextureView.setSurfaceTextureListener(this);
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (Build.VERSION.SDK_INT>=23){
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                scansFaceManager.openCamera();
            }else {
                requestPermissions(new String[]{Manifest.permission.CAMERA},0x11);

            }
        }else {
            scansFaceManager.openCamera();

        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onScansSuccess(Bitmap bitmap) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
                File file=new File(imgFilePath);
                FileUitl.compressBmpToFileByOptionSize(bitmap,file,70);
                String imgBase64Str=FileUitl.getFileBase64String(file);
                if (mode==FACE_ADD_MODE) {
                    ScansService.getInstance().faceSet(imgBase64Str,new ScansService.ScansListener() {
                        @Override
                        public void onSuccess(JSONObject resultJson) {
                            //添加成功
                            ((ScansFaceActivity)getActivity()).jumpResult(true);
                        }

                        @Override
                        public void onError() {
                            //添加失败
                            ((ScansFaceActivity)getActivity()).jumpResult(false);
                        }
                    });
                }else {
                    //识别登录
                    ScansService.getInstance().FaceSearch(imgBase64Str, new ScansService.ScansListener() {
                        @Override
                        public void onSuccess(JSONObject resultJSON) {
                            //返回登录信息
                            try {
                                String token = resultJSON.getString("Token");
                                String tokenExpiryDate = resultJSON.getString("TokenExpiryDate");
                                Intent resultIntent=new Intent();
                                resultIntent.putExtra("Token",token);
                                resultIntent.putExtra("TokenExpiryDate",tokenExpiryDate);
                                getActivity().setResult(Activity.RESULT_OK,resultIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
                }


            }
        });
    }

    @Override
    public void onScansFail() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==0x11){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                scansFaceManager.openCamera();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
