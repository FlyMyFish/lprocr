package org.opencv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapUtils {

    public static Bitmap compressBitmap(Bitmap origin) {
        if (origin != null) {
            Log.e("BitmapUtils", "compressBitmap -> origin:byteCount = " + origin.getByteCount());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        origin.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();
            //重置baos即清空baos
            origin.compress(Bitmap.CompressFormat.JPEG, options, baos);
            //这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;
            //每次都减少10
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap result = BitmapFactory.decodeStream(isBm, null, null);
        if (result != null) {
            Log.e("BitmapUtils", "compressBitmap -> result:byteCount = " + result.getByteCount());
        }
        return result;
        //把ByteArrayInputStream数据生成图片
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 裁剪
     *
     * @param bitmap 原图
     * @return 裁剪后的图像
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int width, int height, int x, int y) {
        //Log.e("BitmapUtils", "cropBitmap -> origin:byteCount = " + bitmap.getByteCount());

        Bitmap result = Bitmap.createBitmap(bitmap, x, y, width, height, null, false);
        //Log.e("BitmapUtils", "cropBitmap -> result:byteCount = " + result.getByteCount());

        return result;
    }

    /**
     * 根据给定的宽和高进行拉伸
     *
     * @param origin    原图
     * @param newWidth  新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    private static Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    /**
     * 按比例缩放图片
     *
     * @param origin      原图
     * @param targetWidth 图片目标宽
     * @return 新的bitmap
     */
    private static Bitmap scaleBitmap(Bitmap origin, float targetWidth) {
        if (origin == null) {
            return null;
        }
        float width = origin.getWidth();
        float height = origin.getHeight();
        float ratio = targetWidth / width;
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, (int) width, (int) height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static Bitmap rotateAndScaleBitmap(Bitmap origin, float alpha, float targetWidth) {
        //Log.e("BitmapUtils", "rotateAndScaleBitmap -> origin:byteCount = " + origin.getByteCount() + " width = " + origin.getWidth() + " height = " + origin.getHeight());

        if (origin == null) {
            return null;
        }
        float width = origin.getWidth();
        float height = origin.getHeight();
        float ratio = targetWidth / height;
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, (int) width, (int) height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        //Log.e("BitmapUtils", "rotateAndScaleBitmap -> result:byteCount = " + newBM.getByteCount() + " width = " + newBM.getWidth() + " height = " + newBM.getHeight());

        return newBM;
    }
}
