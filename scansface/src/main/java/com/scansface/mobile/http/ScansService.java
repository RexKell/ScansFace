package com.scansface.mobile.http;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author: rexkell
 * date: 2020/10/27
 * explain:
 */
public class ScansService {
    private static ScansService instance;
    private static Retrofit retrofit;
   public interface ScansListener{
        void onSuccess(JSONObject reusltJson);
        void onError();
    }
    public static ScansService getInstance(){
        if (instance==null){
            synchronized (ScansService.class){
                if (instance==null){
                    instance=new ScansService();
                }
            }
        }
        return instance;
    }

    /**
     * 传入请求的retrofit
     * @param client
     */
    public  void setRetrofitClient(Retrofit client){
        retrofit=client;
    }
    public void faceSet(String baseImgStr,@NonNull ScansListener listener){
        if (retrofit==null){
            throw new IllegalArgumentException("请设置网络请求客户端");
        }

        FaceSetModel faceSetModel=new FaceSetModel(baseImgStr);
        retrofit.create(ScansAPI.class).faceSet(faceSetModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ERROR",e.toString());
                        listener.onError();

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            Log.e("result:",responseBody.string().toString());

                            listener.onSuccess(new JSONObject(""));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });


    }
    public void FaceSearch(String baseImgStr,@NonNull ScansListener listener){
        if (retrofit==null){
            throw new IllegalArgumentException("请设置网络请求客户端");
        }

        FaceSearchModel faceSetModel=new FaceSearchModel(baseImgStr);
        retrofit.create(ScansAPI.class).faceSearch(faceSetModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ERROR",e.toString());
                        listener.onError();

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            ResultResponse resultResponse = new Gson().fromJson(responseBody.string().toString(), ResultResponse.class);
                            if (resultResponse.getStatusCode() == 200) {
                                JSONObject resultJSON = new JSONObject(resultResponse.getData());


                                listener.onSuccess(resultJSON);
                            } else {
                                listener.onError();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onError();
                        }


                    }
                });


    }
}
