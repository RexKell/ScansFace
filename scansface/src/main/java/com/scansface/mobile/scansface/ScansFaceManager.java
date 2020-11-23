package com.scansface.mobile.scansface;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * author: rexkell
 * date: 2020/10/26
 * explain:
 */
public class ScansFaceManager {
    private static final SparseIntArray ORIENTATIONS=new SparseIntArray();
    //预览surface
    private Surface previewSurface;
    private ImageReader cImageReader;
    private Surface captureSurface;
    String cameraId;
    CameraManager cameraManager;
    Size[] previewSize;
    int cOrientation;
    Size cPixelSize;
    int[] faceDetectModes;
    TextureView mTextureView;
    CameraDevice cameraDevice;
    CameraDevice.StateCallback cDeviceOpenCallback=null;
    CameraCaptureSession cSession;
    HandlerThread cHandlerThread;
    Handler cHandler;
    Size captureSize;
    Bitmap bitmap;
    ScansFaceListener scansListener;
    CaptureRequest previewRequest;//预览请求
    CaptureRequest.Builder previewRequestBuild;
    CameraCaptureSession.CaptureCallback previewCallback;//预览回调
    CameraCaptureSession.CaptureCallback captureCallback;
    CaptureRequest captureRequest;
    CaptureRequest.Builder captureRequestBuilder;
    Activity activity;
    ScansFaceView scansFaceView;

    public ScansFaceManager(ScansFaceListener scansListener, Activity activity) {
        this.scansListener = scansListener;
        this.activity=activity;
    }

    /**
     * 初始化相机
     * @param textureView
     */
    public void initCamera(TextureView textureView, ScansFaceView scansFaceView){
        front();
        cameraId= CameraCharacteristics.LENS_FACING_BACK+"";
        cameraManager=(CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics cameraCharacteristics=  cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map= cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewSize= map.getOutputSizes(SurfaceTexture.class);
            Size[] captureSize=map.getOutputSizes(ImageFormat.JPEG);
            cOrientation= cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Rect cRect= cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            cPixelSize= cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            //判断支持人脸检测
            faceDetectModes= cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
            int maxFaceCount=cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
            this.mTextureView=textureView;
            this.scansFaceView=scansFaceView;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    //打开相机
    public void openCamera(){
        SurfaceTexture surfaceTexture=mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize[0].getWidth(),previewSize[0].getHeight());
        //打开相机
        try {
            cameraManager.openCamera(cameraId,getCDeviceOpenCallback(),getCHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    
    private void front() {
        //前置时，照片竖直显示
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private void rear() {
        //后置时，照片竖直显示
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    /**
     *初始化相机，开启回调
     */
    private CameraDevice.StateCallback getCDeviceOpenCallback(){
        if (cDeviceOpenCallback==null){
            cDeviceOpenCallback=new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    //扫描动画
                    cameraDevice=camera;
                    try {
                        camera.createCaptureSession(Arrays.asList(getPreviewSurface(), getCaptureSurface()), new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                cSession = session;
                                try {
                                    cSession.setRepeatingRequest(getPreviewRequest(), getPreviewCallback(), getCHandler());
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                session.close();
                            }
                        }, getCHandler());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                }
            };
        }
        return cDeviceOpenCallback;
    }

    private Handler getCHandler(){
        if (cHandler==null){
            cHandlerThread=new HandlerThread("cHandlerThread");
            cHandlerThread.start();
            cHandler=new Handler(cHandlerThread.getLooper());
        }
        return cHandler;
    }
    /**
     * 获取预览surface
     */
    private Surface getPreviewSurface(){
        if (previewSurface==null){
            previewSurface=new Surface(mTextureView.getSurfaceTexture());
        }
        return previewSurface;
    }
    //初始化相关的
    private Surface getCaptureSurface(){
        if (cImageReader==null){
            cImageReader=ImageReader.newInstance(getCaptureSize().getWidth(),getCaptureSize().getHeight(),ImageFormat.JPEG,2);
            cImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    onCaptureFinished(reader);
                }
            },getCHandler());
            captureSurface=cImageReader.getSurface();
        }
        return captureSurface;
    }
    /**
     * 获取照片尺寸
     */
    public Size getCaptureSize(){
        if (captureSize!=null){
            return  captureSize;
        }else {
            return cPixelSize;
        }
    }
    private void onCaptureFinished(ImageReader reader){
        Image image= reader.acquireLatestImage();
        ByteBuffer buffer=image.getPlanes()[0].getBuffer();
        byte[] data=new byte[buffer.remaining()];
        buffer.get(data);
        image.close();
        buffer.clear();

        if (bitmap!=null){
            bitmap.recycle();
            bitmap=null;
        }
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        data=null;
        if (bitmap!=null){
            Matrix matrix=new Matrix();
            matrix.postScale(-1,1);
            matrix.postRotate(90);
            Bitmap imgToShow = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,false);
//            bitmap.recycle();
            scansListener.onScansSuccess(imgToShow);
        }
        Runtime.getRuntime().gc();
    }
    /**
     * 生成并获取预览请求
     */
    private CaptureRequest getPreviewRequest(){
        previewRequest=getPreviewRequestBuild().build();
        return previewRequest;
    }
    private CaptureRequest.Builder getPreviewRequestBuild(){
        if (previewRequestBuild==null){
            try {
                previewRequestBuild=cSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuild.addTarget(getPreviewSurface());
                previewRequestBuild.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        previewRequestBuild.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,getFaceDetectMode());
        return previewRequestBuild;
    }
    private CameraCaptureSession.CaptureCallback getPreviewCallback(){
        if (previewCallback==null){
            previewCallback=new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    onCameraImagePreviewed(result);
                }
            };

        }
        return previewCallback;
    }
    private int getFaceDetectMode(){
        if (faceDetectModes==null){
            return CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL;
        }else {
            return faceDetectModes[faceDetectModes.length-1];
        }
    }
    /**
     * 处理相机画面处理完成事件，检测到人脸坐标，换算并绘制方格
     * @param result
     */
    private void onCameraImagePreviewed(CaptureResult result){
        Face faces[]=result.get(CaptureResult.STATISTICS_FACES);
        Log.e("人脸个数：",faces.length+"");
        //获取当前屏幕照片
//        Canvas canvas=mTextureView.lockCanvas();
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (faces.length>0){
            for (int i=0;i<faces.length;i++){
                Rect rect=faces[i].getBounds();
                float scaleWidth=mTextureView.getHeight()*1.0f/cPixelSize.getWidth();
                float scaleHeight=mTextureView.getWidth()*1.0f/cPixelSize.getHeight();
                Log.e("scaleWidth:",scaleWidth+"");
                Log.e("scaleHeight:",scaleHeight+"");
                int l =(int)(rect.left*scaleWidth);
                int r =(int) (rect.right*scaleHeight);
                int t=(int)(rect.top*scaleHeight);
                int b=(int)(rect.bottom*scaleHeight);
                int drawLeft=mTextureView.getWidth()-b;
                int drawRight=mTextureView.getWidth()-t;
                int drawTop=mTextureView.getHeight()-r;
                int drawBottom=mTextureView.getHeight()-l;
                Log.e("left,top,right,bottom:",drawLeft+"、"+drawRight+"、"+drawTop+"、"+drawBottom);
//                canvas.drawRect(drawLeft,drawTop,drawRight,drawBottom,getPaint());
//             canvas.drawRect(canvas.getWidth()/2-300,canvas.getHeight()/2-300,canvas.getWidth()/2+300,canvas.getHeight()/2+300,getPaint());
                //当绘制脸的框，rect 中心坐标圆心中间100内
                int centerX=drawLeft+(drawRight-drawLeft)/2;
                int centerY=(drawBottom-drawTop)/2+drawTop;
                Point scansCenterPoint=scansFaceView.getCenterPoint();
                if (Math.abs(scansCenterPoint.x-centerX)<250&&Math.abs(scansCenterPoint.y-centerY)<250){
                    try {
                        Log.i(this.getClass().getName(), "发出请求");
                        scansFaceView.startLoading();
                        cSession.capture(getCaptureRequest(), getCaptureCallback(), getCHandler());
                    } catch (CameraAccessException e) {
                        Log.e("CameraAccessException:",e.toString());
                    }
                }
            }

        }
//        mTextureView.unlockCanvasAndPost(canvas);
    }
    private CaptureRequest getCaptureRequest(){
        captureRequest=getCaptureRequestBuilder().build();
        return captureRequest;

    }
    private CaptureRequest.Builder getCaptureRequestBuilder(){
        if (captureRequestBuilder==null){
            try {
                captureRequestBuilder=cSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);
                captureRequestBuilder.addTarget(getCaptureSurface());
                int rotation= activity.getWindowManager().getDefaultDisplay().getRotation();
//               int rotationTo=getcOrientation(rotation);
                captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }


        }
        return captureRequestBuilder;
    }
    private CameraCaptureSession.CaptureCallback getCaptureCallback(){
        if (captureCallback==null){
            captureCallback=new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    onCameraImagePreviewed(result);
                }
            };
        }
        return captureCallback;
    }
}
