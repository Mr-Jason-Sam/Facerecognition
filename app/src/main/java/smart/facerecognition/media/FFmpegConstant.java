/**   
 * <p><h1>Copyright:</h1><strong><a href="http://www.smart-f.cn">
 * BeiJing Smart Future Technology Co.Ltd. 2015 (c)</a></strong></p>
 */
package smart.facerecognition.media;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p>
 * <h1>Copyright:</h1><strong><a href="http://www.smart-f.cn"> BeiJing Smart
 * Future Technology Co.Ltd. 2015 (c)</a></strong>
 * </p>
 *
 * <p>
 * <h1>Reviewer:</h1> <a href="mailto:jiangjunjie@smart-f.cn">jjj</a>
 * </p>
 * 
 * <p>
 * <h1>History Trace:</h1>
 * <li>2017年5月25日 下午1:25:38 V1.0.0 jjj first release</li>
 * </p>
 * 
 * @Title FFmpegCommand.java
 * @Description please add description for the class
 * @author jjj
 * @email <a href="jiangjunjie@smart-f.cn">jiangjunjie@smart-f.cn</a>
 * @date 2017年5月25日 下午1:25:38
 * @version V1.0
 */
public class FFmpegConstant {

    private static final String  TAG                = FFmpegConstant.class.getSimpleName();

    public static final String FFMPEG_SYSTEM_PATH   = "/system/bin/ffmpeg";
    public static String ffmpegPath                 = "";

    public static final String FFMPEG_NAME          = "ffmpeg";

    public static final String VERSION              = "ffmpeg version";
    public static final String CONFIG               = "configuration:";
    public static final String FRAME                = "frame=";
    public static final String QUIT                = "video:";

    public static final String FAIL_OPEN            = "Failed to open segment";
    public static final String FAIL_WRITE           = "Could not write header";
    public static final String FAIL_OUTPUT          = "Error initializing output stream";
    public static final String FAIL_CONVERTE        = "Conversion failed!";
    public static final String FAIL_PERM_DENY       = "Permission denied";
    public static final String FAIL_INVALID_DATA    = "Invalid data found when processing input";

    public static final String INVALID_ARG          = "Invalid argument";
    public static final String ERR_PROCESS          = "Error while processing";
    public static final String ERR_NO_FILTER        = "No such filter";
    public static final String ERR_INIT_FILTER      = "Error reinitializing filters";
    public static final String ERR_DECODE           = "error while decoding";

    public static final String KEY_FRAME            = "pict_type:I";
    //"Failed to inject frame into filter network";
    //Input
    //Press [q] to stop, [?] for help
    //Output

    //video:291603kB audio:0kB subtitle:0kB other streams:0kB global headers:0kB muxing overhead: unknown
    public static final String FINISH_PRE           = "other streams:";
    public static final String FINISH               = "Qavg";
    public static final String NO_FILE              = "No such file or directory";

    public static final String MEDIA_ASSET_DIR      = "media";
    private static boolean ffmpegInited;
    public static boolean initFFmpeg(Context context) {
        if (ffmpegInited) {
            return true;
        }
        if (isSystemFFmpegAvailable()) {
            ffmpegPath = FFMPEG_SYSTEM_PATH;
            ffmpegInited = true;
            return true;
        }
        AssetManager assetManager = context.getAssets();
        Log.i(TAG, "initFFmpeg, assetManager:"+assetManager);
        if (assetManager == null) {
            return false;
        }
        try {
            String ffmpegName = FFmpegConstant.FFMPEG_NAME;
            String[] assets = assetManager.list(MEDIA_ASSET_DIR);
            for (String asset:assets) {
                Log.i(TAG, "initFFmpeg, asset:"+asset);
                if (asset.equals(ffmpegName)) {
                    File file = new File(context.getFilesDir(), ffmpegName);
                    if (!file.exists()) {
                        Log.i(TAG, "initFFmpeg, not exist.");
                        file.createNewFile();
                    }
                    if (file.exists()) {
                        Log.i(TAG, "initFFmpeg, create.");
                        InputStream is = assetManager.open(MEDIA_ASSET_DIR+File.separator+ffmpegName);
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int len = is.read(buffer);
                            if (len == -1) {
                                break;
                            }
                            fos.write(buffer, 0, len);
                        }
                        is.close();
                        fos.close();
                        Log.i(TAG, "initFFmpeg, ffmpeg exist:" + file.exists() + ", path:" + file.getAbsolutePath()
                                + ", size:" + file.length());
                        if (chmod(file.getAbsolutePath())) {
                            ffmpegPath = file.getAbsolutePath();
                            ffmpegInited = true;
                            return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFfmpegAvailable() {
        return ffmpegInited;
    }

    public static String getFfmpegPath() {
        return ffmpegPath;
    }

    private static boolean isSystemFFmpegAvailable() {
        Log.i(TAG, "isSystemFFmpegAvailable");
        boolean result = false;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(FFMPEG_SYSTEM_PATH);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader buf = null;
            String line = null;
            buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = buf.readLine()) != null) {
                Log.i(TAG, "processFfmpegCmd, line:" + line);
                if (line.contains(VERSION)) {
                    result = true;
                    break;
                }
            }
            p.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "isSystemFFmpegAvailable, exe ffmpeg failed!");
            e.printStackTrace();
        }
        return result;
    }

    private static boolean chmod(String path) {
        Log.i(TAG, "chmod, file:"+path);
        try {
            Process p = Runtime.getRuntime().exec("chmod 777 " +  path);
            Log.i(TAG, "chmod, p:"+p);
            int status = p.waitFor();
            Log.i(TAG, "chmod, status:"+status);
            if (status == 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "chmod, chmod failed!");
            e.printStackTrace();
        }
        return false;
    }
}
