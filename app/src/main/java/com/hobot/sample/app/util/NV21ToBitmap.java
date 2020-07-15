package com.hobot.sample.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;

/**
 * Created by caydencui on 2018/12/7.
 */
public class NV21ToBitmap {
    private static final String TAG = NV21ToBitmap.class.getSimpleName();

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    private static NV21ToBitmap sInstance;

    public static NV21ToBitmap getInstance() {
        if (sInstance == null) {
            sInstance = new NV21ToBitmap();
        }
        return sInstance;
    }

    public NV21ToBitmap init(Context context) {
        if (rs == null) {
            rs = RenderScript.create(context);
            yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
            yuvType = new Type.Builder(rs, Element.U8(rs));
            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs));
        }
        return this;
    }

    private NV21ToBitmap() {

    }
    public Bitmap nv21ToBitmap(byte[] nv21, int width, int height){
        Log.d(TAG, "nv21ToBitmap+++");
        yuvType.setX(nv21.length);
        in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        rgbaType.setX(width).setY(height);
        out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        in.copyFrom(nv21);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        out.destroy();
        in.destroy();
        Log.d(TAG, "nv21ToBitmap---");
        return bmpout;
    }
}
