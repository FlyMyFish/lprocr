package org.opencv.decoding;

import android.os.Handler;
import android.os.Looper;


import org.opencv.OcrFragment;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;

public final class DecodeThread extends Thread {

    public static final String OCR_RESULT_BITMAP = "ocr_result_bitmap";
    public static final String OPENCV_HANDLE = "opencv_handle";
    private final OcrFragment fragment;
    private final Hashtable<String, Object> rariable;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(
            OcrFragment fragment,
            long handle
    ) {
        this.fragment= fragment;
        handlerInitLatch = new CountDownLatch(1);
        rariable = new Hashtable<>(1);
        rariable.put(OPENCV_HANDLE,handle);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(fragment, rariable);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}