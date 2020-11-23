package com.scansface.mobile.http;

/**
 * author: rexkell
 * date: 2019/2/25
 * explain:接口返回结果
 */
public class ResultResponse {
    private int statusCode;
    private String statusDesc;
    private String data;

    public ResultResponse(int statusCode, String statusDesc, String data) {
        this.statusCode = statusCode;
        this.statusDesc = statusDesc;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
