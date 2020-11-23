package com.scansface.mobile.scansface;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * author: rexkell
 * date: 2020/10/27
 * explain:
 */
public class FileUitl {
    public static String getFileBase64String(File file){
            String base64 = null;
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                byte[] bytes = new byte[in.available()];
                int length = in.read(bytes);
                base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return base64;
    }

    public static File compressBmpToFileByOptionSize(Bitmap bmp, File file, int optionSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = optionSize;
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        FileOutputStream fos = null;
        while (baos.toByteArray().length / 1024 >200) {//控制压缩文件大小
            baos.reset();
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
            if (options==50){//控制压缩比例大小
                break;
            }
            options -= 10;
        }
        try {
            fos = new FileOutputStream(file);
            if (fos != null) {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            }
            if (baos != null) {
                baos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                    baos=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                    fos=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bmp!=null){
                bmp=null;
            }
        }
        return file;
    }
}
