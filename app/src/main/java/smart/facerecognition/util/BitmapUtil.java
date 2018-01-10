/**   
 * <p><h1>Copyright:</h1><strong><a href="http://www.smart-f.cn">
 * BeiJing Smart Future Technology Co.Ltd. 2015 (c)</a></strong></p>
 */
package smart.facerecognition.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import cn.smart.droid.media.jni.MediaConstant;
import smart.facerecognition.SmartAnalyze.Face;

/**  
 * <p><h1>Copyright:</h1><strong><a href="http://www.smart-f.cn">
 * BeiJing Smart Future Technology Co.Ltd. 2015 (c)</a></strong></p> 
 *
 * <p>
 * <h1>Reviewer:</h1> 
 * <a href="mailto:jiangjunjie@smart-f.cn">jjj</a>
 * </p>
 * 
 * <p>
 * <h1>History Trace:</h1>
 * <li> 2016年8月16日 下午8:37:15    V1.0.0          jjj         first release</li>
 * </p> 
 * @Title BitmapUtil.java 
 * @Description please add description for the class 
 * @author jjj
 * @email <a href="jiangjunjie@smart-f.cn">jiangjunjie@smart-f.cn</a>
 * @date 2016年8月16日 下午8:37:15 
 * @version V1.0   
 */
public class BitmapUtil {

    private static final String TAG = BitmapUtil.class.getSimpleName();
    public static final String MEDIA_FORMAT_JPEG                            = ".jpeg";
    
    /**
     * 按给定的Face的规格裁切图片
     */
    public static Bitmap cutFace(Bitmap bitmap, Face face) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int retX = (int)(w*face.left);
        int retY = (int)(h*face.top);
//        int width = (int)(w*(face.right - face.left));
//        int height = (int)(h*(face.bottom - face.top));
        int width = (int)(w*face.width);
        int height = (int)(h*face.height);

        Log.i(TAG, "cutFace, l:"+retX+", top:"+retY+", r:"+(retY+width)+", b:"+(retY+height));
        return Bitmap.createBitmap(bitmap, retX, retY, width, height, null, false);
    }
    /**
     * 按给定的Face和scale裁切图片
     */
    public static Bitmap cutFace(Bitmap bitmap, Face face, double scale) {
        Log.i(TAG, "cutFace, l:"+face.left+", top:"+face.top+", r:"+(face.width)+", b:"+(face.height)
                + ", scale:"+scale);
        if (scale < 1) {
            scale = 1;
        }
        if (scale > 2) {
            scale = 2;
        }
        
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        
//        double wMinus = (face.right - face.left)*(scale-1)/2;
//        double hMinus = (face.bottom - face.top)*(scale-1)/2;

        double wMinus = face.width*(scale-1)/2;
        double hMinus = face.height*(scale-1)/2;

        //Log.i(TAG, "cutFace, wMinus:"+wMinus+", hMinus:"+hMinus);
        int retX = (int)((face.left-wMinus));
        if (retX < 0) retX = 0;
        int retY = (int)((face.top-hMinus));
        if (retY < 0) retY = 0;
        int width = (int)(face.width*scale);
        if (width + retX > w)
            width = w - retX;
        int height = (int)(face.height*scale);
        if (height + retY > h)
            height = h - retY;
        
        //Log.i(TAG, "cutFace, l:"+retX+", top:"+retY+", r:"+(retX+width)+", b:"+(retY+height));
        return Bitmap.createBitmap(bitmap, retX, retY, width, height, null, false);
    }
    /**
     * 按给定的Face数组和scale裁切图片
     */
    public static List<Bitmap> cutFaces(Bitmap bitmap, Face[] faces, double scale) {
        Log.i(TAG, "cutFaces, faces:" + faces + ", scale:"+scale);
        if (faces == null) {
            return null;
        }
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        for (Face face:faces) {
            Bitmap temp = cutFace(bitmap, face);
            if (temp == null) {
                continue;
            }
            bitmaps.add(temp);
        }
        return bitmaps;
    }
    
    /** 
     * 将bitmap生成为图片存到本地 
     */  
    public static void saveBitmapToPng(Bitmap bitMap, String dstPath, String name) throws IOException {
        File file = new File(dstPath+"/"+name+".png");
        file.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitMap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }  
    }
    
    /** 
     * 将bitmap生成为图片存到本地 
     */  
    public static File saveBitmapToJpeg(Bitmap bitMap, String dstPath, String name) {
        File file = new File(dstPath+File.separator + name + MEDIA_FORMAT_JPEG);
        FileOutputStream fOut = null;
        try {
            if (!file.exists())
                file.createNewFile();
            fOut = new FileOutputStream(file);
            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static Bitmap getFaceInfoBitmap(Face[] faceinfos, Bitmap oribitmap) {
        Bitmap tmp;
        tmp = oribitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas localCanvas = new Canvas(tmp);
        Paint localPaint = new Paint();
        localPaint.setColor(0xffff0000);
        localPaint.setStyle(Paint.Style.STROKE);
        for (Face localFaceInfo : faceinfos) {
            RectF rect = new RectF(oribitmap.getWidth() * localFaceInfo.left, oribitmap.getHeight() * localFaceInfo.top,
                    oribitmap.getWidth() * localFaceInfo.right, oribitmap.getHeight() * localFaceInfo.bottom);
            Log.i(TAG, "getFaceInfoBitmap, trackId:" + localFaceInfo.trackingID);
            localCanvas.drawRect(rect, localPaint);
        }
        return tmp;
    }

    public static Bitmap getBitmapScaled(String pathName, int dstWidth) {
        BitmapFactory.Options localOptions = new BitmapFactory.Options();
        localOptions.inJustDecodeBounds = false;
        BitmapFactory.decodeFile(pathName, localOptions);
        int originWidth = localOptions.outWidth;
        int originHeight = localOptions.outHeight;
        localOptions.inSampleSize = originWidth > originHeight ? originWidth / dstWidth : originHeight / dstWidth;
        localOptions.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, localOptions);
    }
}