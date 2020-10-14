package org.opencv.decoding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import org.opencv.BitmapUtils;

import java.io.ByteArrayOutputStream;

public class PlanteImageSource {
    /**
     * @param data      相机数据
     * @param width     相机图像宽度
     * @param height    相机图像高度
     * @param x         裁剪时x轴起始坐标
     * @param y         裁剪时y轴起始坐标
     * @param croWidth  裁剪的宽
     * @param croHeight 裁剪的高
     * @return
     */
    public static Bitmap create(byte[] data, int width, int height, int x, int y, int croWidth, int croHeight) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
        byte[] jdata = baos.toByteArray();


        Bitmap source = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, null);
        return BitmapUtils.rotateAndScaleBitmap(BitmapUtils.cropBitmap(source, croHeight, croWidth, y, x), 90, 300);
        //return BitmapUtils.compressBitmap(BitmapUtils.cropBitmap(BitmapUtils.rotateAndScaleBitmap(source, 90, 600), croWidth, croHeight, x, y));
    }

}
