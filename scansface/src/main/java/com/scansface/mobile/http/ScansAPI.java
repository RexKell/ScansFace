package com.scansface.mobile.http;


import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;


/**
 * author: rexkell
 * date: 2020/10/27
 * explain:
 */
public interface ScansAPI {
    @POST("FaceSearch")
    Observable<ResponseBody> faceSearch(@Body FaceSearchModel faceSearchModel);

    @POST("FaceSet")
    Observable<ResponseBody> faceSet(@Body FaceSetModel faceSetModel);


}
