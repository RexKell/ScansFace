package com.scansface.mobile.scansface;

import android.graphics.Bitmap;

/**
 * author: rexkell
 * date: 2020/10/27
 * explain:
 */
 interface ScansFaceListener {
    void onScansSuccess(Bitmap bitmap);
    void onScansFail();
}
