package com.scansface.mobile.http;

/**
 * author: rexkell
 * date: 2020/10/27
 * explain:
 */
class FaceSearchModel {
    private String image_base64;

    public FaceSearchModel(String image_base64) {
        this.image_base64 = image_base64;
    }

    public String getImage_base64() {
        return image_base64;
    }

    public void setImage_base64(String image_base64) {
        this.image_base64 = image_base64;
    }
}
