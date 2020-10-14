package org.opencv;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.utils.DeepAssetUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class OcrActivity extends AppCompatActivity {

    private OcrFragment ocrFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lpr);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(this, dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(this, dm.heightPixels);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isNeedCheck) {
            //通过注解反射得到该页面需要的权限
            String[] needPermissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (needPermissions.length > 0) {
                checkPermissions(needPermissions);
            }
        }
    }

    private static final int PERMISSON_REQUESTCODE = 0;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    public void checkPermissions(String... permissions) {
        List<String> needRequestPermissionList = findDeniedPermissions(permissions);
        if (null != needRequestPermissionList
                && needRequestPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissionList.toArray(
                            new String[needRequestPermissionList.size()]),
                    PERMISSON_REQUESTCODE);
        } else {
            allPermissionsOk(permissions);
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String... permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissionList.add(perm);
            }
        }
        return needRequestPermissionList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(grantResults)) {
                permissionsDenied(deniedPermissions(permissions, grantResults));
                isNeedCheck = false;
            } else {
                allPermissionsOk(permissions);
            }
        }
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults 授权结果
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private List<String> deniedPermissions(@NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        List<String> deniedPermissionList = new ArrayList<>();
        for (int index = 0; index < grantResults.length; index++) {
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permissions[index]);
            }
        }
        return deniedPermissionList;
    }

    public void allPermissionsOk(String[] permissions) {
    }

    public void permissionsDenied(List<String> deniedPermissions) {
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void onManagerConnected(int status) {
            //在加载openCV 成功后, 开始加载 vcd so 文件
            if (status == LoaderCallbackInterface.SUCCESS) {
                System.loadLibrary("lpr");
                new AsyncTask<Void, Void, Long>() {
                    @Override
                    protected Long doInBackground(Void... voids) {
                        return DeepAssetUtil.initRecognizer(OcrActivity.this);
                    }

                    @Override
                    protected void onPostExecute(Long aVoid) {
                        super.onPostExecute(aVoid);
                        ocrFragment = new OcrFragment(aVoid);
                        //captureFragment.setAnalyzeCallback(analyzeCallback);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_ocr_container, ocrFragment).commit();
                        ocrFragment.setCameraInitCallBack(new OcrFragment.CameraInitCallBack() {
                            @Override
                            public void callBack(Exception e) {
                                if (e == null) {

                                } else {
                                    Log.e("TAG", "callBack: ", e);
                                }
                            }
                        });
                    }
                }.execute();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    /**
     * 自定义的FaceTask类，开启一个线程分析数据
     */
    /*@SuppressLint("StaticFieldLeak")
    private class PalmTask extends AsyncTask<Void, Void, String> {
        private byte[] mData;

        //构造函数
        PalmTask(byte[] data) {
            this.mData = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            try {
                YuvImage image = new YuvImage(mData, ImageFormat.NV21, size.width, size.height, null);
                ByteArrayOutputStream stream = new ByteArrayOutputStream(mData.length);
                //image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                if (!image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream)) {
                    return null;
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                //旋转图片
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                //mBmp = Bitmap.createBitmap(bmp, viewFinderView.center.top + actionHeight, viewFinderView.center.left,viewFinderView.centerHeight, viewFinderView.centerWidth, matrix, true);
                stream.close();
                Bitmap bitmap = Bitmap.createBitmap(bmp, viewFinderView.center.top, viewFinderView.center.left,
                        viewFinderView.centerHeight, viewFinderView.centerWidth, matrix, true);


                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Mat m = new Mat(width, height, CvType.CV_8UC4);
//                    Mat m = new Mat(width, height, CvType.CV_8UC2);
                Utils.bitmapToMat(bitmap, m);
                return PlateRecognition.SimpleRecognization(m.getNativeObjAddr(), handle);

            } catch (Exception ex) {
                Log.e("Sys", "Error:" + ex.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            if (!"".equals(str)&&str!=null) {
                isStarus = false;
                String[] list = str.split(",");
                if (list.length > 1) {
                    new AlertDialog.Builder(OcrActivity.this)
                            .setTitle("请点击选择")
                            .setItems(list, (dialog, which) -> finishValue(list[which]))
                            .setNegativeButton("重新识别", (dialog, which) -> isStarus = true)
                            .show();
                } else
                    new AlertDialog.Builder(OcrActivity.this)
                            .setTitle("识别结果")
                            .setMessage(str)
                            .setPositiveButton("确定", (dialog, which) -> finishValue(str))
                            .setNegativeButton("重新识别", (dialog, which) -> isStarus = true)
                            .show();
            }

        }
    }*/
}