package org.opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import org.opencv.camera.CameraManager;
import org.opencv.decoding.InactivityTimer;
import org.opencv.decoding.OcrActivityHandler;
import org.opencv.view.ViewfinderView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class OcrFragment extends Fragment implements SurfaceHolder.Callback {

    public long handle;

    public OcrFragment(long handle) {
        this.handle = handle;
    }

    private final String TAG = "OcrFragment";
    private OcrActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private ImageView resultBitmap,previewBitmap;
    private TextView tvResultOcr,tvPreviewOcr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraManager.init(getActivity().getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ocr, null);


        viewfinderView = view.findViewById(R.id.viewfinder_view);
        surfaceView = view.findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
        resultBitmap = view.findViewById(R.id.result_bitmap);
        previewBitmap = view.findViewById(R.id.preview_bitmap);
        tvResultOcr = view.findViewById(R.id.result_bitmap_ocr);
        tvPreviewOcr=view.findViewById(R.id.preview_bitmap_ocr);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        if (camera != null) {
            if (camera != null && CameraManager.get().isPreviewing()) {
                if (!CameraManager.get().isUseOneShotPreviewCallback()) {
                    camera.setPreviewCallback(null);
                }
                camera.stopPreview();
                CameraManager.get().getPreviewCallback().setHandler(null, 0);
                CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
                CameraManager.get().setPreviewing(false);
            }
        }
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(String result, Bitmap barcode) {
        inactivityTimer.onActivity();
        Log.d("OcrFragment","plantNo = " + result);
        previewBitmap.setImageBitmap(barcode);
        tvPreviewOcr.setText(result);

        //playBeepSoundAndVibrate();

        /*if (result == null || TextUtils.isEmpty(result.getText())) {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeFailed();
            }
        } else {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeSuccess(barcode, result.getText());
            }
        }*/

        new AlertDialog.Builder(getActivity())
                .setTitle("识别结果")
                .setMessage(result)
                .setPositiveButton("确定", (dialog, which) -> finishValue(result))
                .setNegativeButton("重新识别", (dialog, which) -> handler.restartPreview())
                .show();
    }

    public void createBitmap(Bitmap result,String ocr){
        Log.e(TAG,"createBitmap : length = " + result.getByteCount());
        resultBitmap.setImageBitmap(result);
        tvResultOcr.setText(ocr);
    }

    private void finishValue(String card) {
        Intent intent = new Intent();
        intent.putExtra("card", card);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            camera = CameraManager.get().getCamera();
        } catch (Exception e) {
            e.printStackTrace();
            if (callBack != null) {
                callBack.callBack(e);
            }
            return;
        }
        if (callBack != null) {
            callBack.callBack(null);
        }

        if (handler == null) {
            handler = new OcrActivityHandler(this,handle);
        }
    }


    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    @Nullable
    CameraInitCallBack callBack;

    /**
     * Set callback for Camera check whether Camera init success or not.
     */
    public void setCameraInitCallBack(CameraInitCallBack callBack) {
        this.callBack = callBack;
    }

    interface CameraInitCallBack {
        /**
         * Callback for Camera init result.
         * @param e If is's null,means success.otherwise Camera init failed with the Exception.
         */
        void callBack(Exception e);
    }

}
