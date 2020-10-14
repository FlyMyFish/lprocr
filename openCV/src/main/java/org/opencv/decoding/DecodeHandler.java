package org.opencv.decoding;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import org.opencv.OcrFragment;
import org.opencv.R;
import org.opencv.android.Utils;
import org.opencv.camera.CameraManager;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.utils.PlateRecognition;

import java.util.Hashtable;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;


public class DecodeHandler extends Handler {
    private final String TAG = "DecodeHandler";
    private final OcrFragment fragment;
    private long handle;

    DecodeHandler(OcrFragment fragment, Hashtable<String, Object> rariable) {
        this.fragment = fragment;
        handle = (Long) rariable.get(DecodeThread.OPENCV_HANDLE);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == R.id.decode) {
            decode((byte[]) msg.obj, msg.arg1, msg.arg2);
            Log.e(TAG, "handleMessage : what = R.id.decode {msg.arg1 = " + msg.arg1 + " ; msg.arg2 = " + msg.arg2);
        } else if (msg.what == R.id.quit) {
            Looper.myLooper().quit();
        }
    }

    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();


        Bitmap resultBitmap = CameraManager.get().buildLuminanceSource(data, width, height);
        String text = "";
        if (resultBitmap!=null){
            int resultWidth = resultBitmap.getWidth();
            int resultHeight = resultBitmap.getHeight();
            Mat m = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);
//                    Mat m = new Mat(width, height, CvType.CV_8UC2);
            Utils.bitmapToMat(resultBitmap, m);
            text =  PlateRecognition.SimpleRecognization(m.getNativeObjAddr(), handle);
            Message message = Message.obtain(fragment.getHandler(), R.id.create_bitmap);
            message.obj = text;
            Bundle bundle = new Bundle();
            bundle.putParcelable(DecodeThread.OCR_RESULT_BITMAP, resultBitmap);
            message.setData(bundle);
            message.sendToTarget();
        }

        if (matchPlant(text)) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found plant (" + (end - start) + " ms):\n" + text);
            Message message = Message.obtain(fragment.getHandler(), R.id.decode_succeeded, text);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DecodeThread.OCR_RESULT_BITMAP, resultBitmap);
            message.setData(bundle);
            //Log.d(TAG, "Sending decode succeeded message...");
            message.sendToTarget();
        } else {
            Message message = Message.obtain(fragment.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }

    private boolean matchPlant(String text) {
        if (text == null) {
            return false;
        }
        if (text.isEmpty()) {
            return false;
        }
        String plateNumMatch =
                "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$";
        return Pattern.matches(plateNumMatch, text.replace(" ", ""));
    }

}
