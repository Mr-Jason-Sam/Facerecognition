/**   
 * <p><h1>Copyright:</h1><strong><a href="http://www.smart-f.cn">
 * BeiJing Smart Future Technology Co.Ltd. 2015 (c)</a></strong></p>
 */
package smart.facerecognition.media;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;


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
 * <li>2017年5月25日 下午1:14:42 V1.0.0 jjj first release</li>
 * </p>
 * 
 * @Title FFmpegMediaManager.java
 * @Description please add description for the class
 * @author jjj
 * @email <a href="jiangjunjie@smart-f.cn">jiangjunjie@smart-f.cn</a>
 * @date 2017年5月25日 下午1:14:42
 * @version V1.0
 */
public class FFmpegMediaProcesser {

    public static final String TAG = FFmpegMediaProcesser.class.getSimpleName();
    private static final int STATUS_READY                     = 0;
    private static final int STATUS_OPEN                      = 1;
    private static final int STATUS_START                     = 2;
    private static final int STATUS_CLOSE                     = 3;
    private Process p;
    private int status = STATUS_READY;

    private String outputType;
    private double segmentDuration, mainVolume, mixVolume;
    private String srcURL;
    private String mediaPath = "";
    private String videoPath;
    private String snapshotPathPattern;
    private String snapshotPath;
    private int snapshotIndex;
    private float keyframeTime1 = -1;
    private float keyframeTime2 = -1;
    private float keyframeTime3 = -1;

    public static final String   DEFAULT_IMAGE_SUCCESSIVE_NAME_PATTERN     = "%08d";
    public static final String NAME_PRE_SNAP                 = "snap";

    private FFmpegMediaTaskListener mListener;
    public interface FFmpegMediaTaskListener {
        void onMediaTaskEvent(int what, int arg1, int arg2, Object obj);
    }
    public void setFFmpegMeidaTaskListener(FFmpegMediaTaskListener listener) {
        mListener = listener;
    }

    public FFmpegMediaProcesser(){

    }

    public FFmpegMediaProcesser(long segmentDuration, double mVolume, double sVolume) {
        //Log.i(TAG, "construct, dur:"+segmentDuration + ", v:"+mVolume + ", sVolume:"+sVolume);
        this.segmentDuration = segmentDuration;
        this.mainVolume = mVolume;
        this.mixVolume = sVolume;
    }
    
    public int open(String srcURL) {
        Log.i(TAG, "open, srcURL:"+srcURL);
        if (status != STATUS_READY) {
            throw new RuntimeException("open, already opened.");
        }
        if (mListener == null) {
            throw new RuntimeException("open, mListener is null.");
        }
        status = STATUS_OPEN;
        this.srcURL = srcURL;
        return 0;
    }

    public int close() {
        Log.i(TAG, "close, srcURL:"+srcURL);
        if (status != STATUS_START) {
            throw new RuntimeException("close, err status.");
        }
        status = STATUS_CLOSE;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(p.getOutputStream(), "UTF-8");
            BufferedWriter bWriter = new BufferedWriter(writer);
            bWriter.write("q");
            bWriter.flush();
            bWriter.close();
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "close, e:"+e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "close, e:"+e);
        } finally {
            p.destroy();
        }
        clearStatus();
        return 0;
    }
    
    public int start() {
        return 0;
    }
    
    public int stop() {
        return 0;
    }

    public int startNormalRecord(String cachePath, String name) {
        Log.i(TAG, "startNormalRecord, cachePath:"+cachePath);
        if (status != STATUS_OPEN) {
            throw new RuntimeException("startNormalRecord, err status.");
        }
        status = STATUS_START;
        processFFmpegCmd(FFmpegCommand.getRecordCmd(srcURL, cachePath,
                name, MediaConstant.MEDIA_FORMAT_MP4, mainVolume));
        videoPath = cachePath+name;
        outputType = MediaConstant.MEDIA_FORMAT_MP4;
        return MediaConstant.SMART_OK;
    }

    public int startMixAudioRecord(String cachePath, String name, String bgmPath) {
        Log.i(TAG, "startMixAudioRecord, cachePath:"+cachePath);
        if (status != STATUS_OPEN) {
            throw new RuntimeException("startNormalRecord, err status.");
        }
        status = STATUS_START;
        processFFmpegCmd(FFmpegCommand.getRecordMixCmd(srcURL, cachePath,
                name, MediaConstant.MEDIA_FORMAT_MP4, mainVolume,
                bgmPath, mixVolume));
        videoPath = cachePath+name;
        outputType = MediaConstant.MEDIA_FORMAT_MP4;
        return MediaConstant.SMART_OK;
    }

    public int startSnapshot(String cachePath, int flag, String name) {
        Log.i(TAG, "startSnapshot, cachePath:"+cachePath);
        if (status != STATUS_OPEN) {
            throw new RuntimeException("startNormalRecord, err status.");
        }
        status = STATUS_START;
        processFFmpegCmd(FFmpegCommand.getSnapshotCmd(srcURL, cachePath,
                name, MediaConstant.MEDIA_FORMAT_BMP, 1));
        snapshotPathPattern = (cachePath+name).replace(MediaConstant.DEFAULT_IMAGE_SUCCESSIVE_NAME_PATTERN, "%s");
        snapshotIndex = 0;
        outputType = MediaConstant.MEDIA_FORMAT_BMP;
        return MediaConstant.SMART_OK;
    }

    public int startSnapshotKeyFrame(String srcUrl, String storePath, int interval, String format) {
        Log.i(TAG, "startSnapshotKeyFrame, srcUrl:"+srcUrl + ", status:"+status);
        status = STATUS_START;
        String name = File.separator + NAME_PRE_SNAP + FFmpegMediaProcesser.DEFAULT_IMAGE_SUCCESSIVE_NAME_PATTERN;
        /*processFFmpegCmd(FFmpegCommand.getSnapshotSomeCmd(srcUrl, storePath,
                name, format, start, interval, end));*/
        processFFmpegCmd(FFmpegCommand.getKeyFrameIntervalCmd(srcUrl, storePath, name, interval, format));
        snapshotPathPattern = (storePath+name).replace(FFmpegMediaProcesser.DEFAULT_IMAGE_SUCCESSIVE_NAME_PATTERN, "%s")
                + format;
        snapshotIndex = 0;
        keyframeTime1 = -1;
        keyframeTime2 = -1;
        keyframeTime3 = -1;
        outputType = format;
        return MediaConstant.SMART_OK;
    }

    public void processFFmpegCmd(final List<String> command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.command(command);
                    builder.redirectErrorStream(true);
                    Log.i(TAG, "command: " + command.toString());
                    p = builder.start();
                    BufferedReader buf = null;
                    String line = null;
                    buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while (status == STATUS_START && (line = buf.readLine()) != null) {
                        Log.i(TAG, "processFFmpegCmd, line:" + line);
                        int what = analyze(line);
                        if (what > 0) {
                            int arg2 = 0;
                            switch (what) {
                            case MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_MP4_COMPLETE:
                                mediaPath = videoPath;
                                arg2 = (int)(duration * 1000);
                                resetStatus();
                                break;
                            case MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_IMAGE_COMPLETE:
                                mediaPath = snapshotPath;
                                arg2 = snapshotIndex;
                                break;
                            case MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_READ_FRAME_ERROR:
                                break;
                            case MediaConstant.SMART_MEDIA_TASK_COMPLETE:
                                if (outputType.equals(MediaConstant.MEDIA_FORMAT_MP4)
                                        || outputType.equals(MediaConstant.MEDIA_FORMAT_MP4)) {
                                    Log.e(TAG, "processFFmpegCmd, exit self when process video.");
                                } else if (outputType.equals(MediaConstant.MEDIA_FORMAT_JPG)
                                        || outputType.equals(MediaConstant.MEDIA_FORMAT_BMP)
                                        || outputType.equals(MediaConstant.MEDIA_FORMAT_JPEG)) {
                                    Log.e(TAG, "processFFmpegCmd, exit self when process image.");
                                }
                                //what = MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_READ_FRAME_ERROR;
                                break;
                                case MediaConstant.SMART_MEDIA_FFMPEG_QUIT:
                                    Log.i(TAG, "FFmpeg is quit!");
                                    break;
                            default:
                                break;
                            }
                            Log.i(TAG, "processFFmpegCmd, mediaPath:"+mediaPath + ", dur/index:"+arg2);
                            int taskId = 0;
                            mListener.onMediaTaskEvent(what, taskId, arg2, mediaPath);
                            if (what == MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_MP4_COMPLETE) {
                                return;
                            }
                        }
                    }
                    p.waitFor();//wait for command completed.
                    Log.i(TAG, "processFFmpegCmd, process exit.");
                    if (status != STATUS_CLOSE) {//TODO
                        mListener.onMediaTaskEvent(MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_READ_FRAME_ERROR,
                                0, 0, "");
                    }
                } catch (Exception e) {
                    Log.i(TAG, "processFFmpegCmd failed!");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private int analyze(String line) {
        int result = -1;
        if (StringUtils.isEmpty(line))
            return result;
        if (line.contains(FFmpegConstant.VERSION)) {
            Log.i(TAG, "analyze, ffmpeg open.");
        } else if (line.contains(FFmpegConstant.CONFIG)) {
            Log.i(TAG, "analyze, ffmpeg config.");
        } else if (line.trim().startsWith("lib")) {
            //Log.i(TAG, "analyze, ffmpeg init lib.");
        } else if (isFFmpegFailed(line)) {
            return MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_READ_FRAME_ERROR;
        } else if (isFFmpegProcessing(line)) {
            if (isVideo(outputType)) {
                result = isRecordCompleted(line);
            } else if (isPic(outputType)) {
                result = calImageIndex(line);
            }
        } else if (isKeyFrameAnalyze(line)) {
            result = getKeyFrameInfo(line);
        } else if (isFFmpegExit(line)) {
            Log.i(TAG, "analyze, ffmpeg exit self.");
            return MediaConstant.SMART_MEDIA_TASK_COMPLETE;
        } else if (isFileNotExist(line)) {
            return MediaConstant.SMART_MEDIA_TASK_INPUT_ERROR;
        } else if (isFFmpegQuit(line)){
            return MediaConstant.SMART_MEDIA_FFMPEG_QUIT;
        }else {
            //Log.i(TAG, "analyze, unsupport line.");
        }
        return result;
    }



    double duration = 0;//seconds
    private int isRecordCompleted(String line) {
        int start = line.indexOf("time=") + "time=".length();
        int end = line.indexOf("bitrate");
        if (start > 0 && end > start) {
            String timelen = line.substring(start, end);
            duration = getTimelen(timelen);
            if (duration >= segmentDuration) {
                return MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_MP4_COMPLETE;
            }
        } else {
            Log.w(TAG, "isRecordCompleted, invalid line:"+line);
        }
        return MediaConstant.SMART_OK;
    }

    private static double getTimelen(String timelen) {
        int seconds = 0;
        String strs[] = timelen.split(":");
        if (strs[0].compareTo("0") > 0) {
            seconds += Integer.valueOf(strs[0]) * 60 * 60;
        }
        if (strs[1].compareTo("0") > 0) {
            seconds += Integer.valueOf(strs[1]) * 60;
        }
        if (strs[2].compareTo("0") > 0) {
            seconds += Float.valueOf(strs[2]);//Math.round()
        }
        return seconds;
    }

    private int getKeyFrameInfo(String line) {
        int result = -1;
        String before = " t:";
        String after = " key:";
        int start = line.indexOf(before)+before.length();
        int end = line.indexOf(after);
        Log.i(TAG, "getKeyFrameInfo, l:"+line+",s:"+start+",e:"+end);
        if (start > before.length() && end > start) {
            String time = line.substring(start, end);
            float t = Float.valueOf(time);
            if (keyframeTime1 < 0)
                keyframeTime1 = t;
            else if (keyframeTime2 < 0)
                keyframeTime2 = t;
            else if (keyframeTime3 < 0) {
                keyframeTime3 = t;
                result = MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_IMAGE_COMPLETE;
            } else {
                Log.e(TAG, "getKeyFrameInfo invalid line:"+line);
            }
            Log.i(TAG, "getKeyFrameInfo, t:"+time+",1:"+keyframeTime1+",2:"+keyframeTime2+",3:"+keyframeTime3);
        }
        return result;
    }

    private int calImageIndex(String line) {
        int index = -1;
        int start = line.indexOf("frame=") + "frame=".length();
        int end = line.indexOf("fps");
        if (start > 0 && end > start) {
            String frame = line.substring(start, end).trim();
            try {
                int frameIndex = Integer.valueOf(frame);
                if (frameIndex > snapshotIndex) {
                    snapshotIndex = frameIndex;
                    String name = String.format(MediaConstant.DEFAULT_IMAGE_SUCCESSIVE_NAME_PATTERN, snapshotIndex);
                    snapshotPath = String.format(snapshotPathPattern, name);
                    return MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_IMAGE_COMPLETE;
                }
            } catch (Exception e) {
                Log.e(TAG, "calImageIndex, e:"+e);
            }
        } else {
            Log.w(TAG, "calImageIndex, invalid line:"+line);
        }
        return index;
    }

    private static boolean isKeyFrameAnalyze(String line) {
        if (StringUtils.isEmpty(line))
            return false;
        if (line.contains(FFmpegConstant.KEY_FRAME)) {
            return true;
        }
        return false;
    }

    private static boolean isFileNotExist(String line) {
        if (StringUtils.isEmpty(line))
            return false;
        if (line.contains(FFmpegConstant.NO_FILE)) {
            return true;
        }
        return false;
    }

    private static boolean isFFmpegFailed(String line) {
        if (StringUtils.isEmpty(line))
            return false;
        if (line.contains(FFmpegConstant.FAIL_OPEN) || line.contains(FFmpegConstant.FAIL_OUTPUT)
                || line.contains(FFmpegConstant.FAIL_WRITE) || line.contains(FFmpegConstant.FAIL_PERM_DENY)
                || line.contains(FFmpegConstant.FAIL_INVALID_DATA) || line.contains(FFmpegConstant.FAIL_CONVERTE)) {
            return true;
        }
        return false;
    }

    private static boolean isFFmpegExit(String line) {
        if (StringUtils.isEmpty(line))
            return false;
        if (line.contains(FFmpegConstant.FINISH) || line.contains(FFmpegConstant.FINISH_PRE)) {
            return true;
        }
        return false;
    }

    private static boolean isFFmpegProcessing(String line) {
        if (StringUtils.isEmpty(line))
            return false;
        if (line.startsWith(FFmpegConstant.FRAME)) {
            return true;
        }
        return false;
    }

    private static boolean isFFmpegQuit(String line) {
        if (line.startsWith(FFmpegConstant.QUIT)){
            return true;
        }
        return false;
    }

    private void resetStatus() {
        duration = 0;
    }

    private void clearStatus() {
        outputType = "";
    }

    public static boolean isVideo(File f) {
        return f.getName().endsWith(MediaConstant.MEDIA_FORMAT_MP4)
                || f.getName().endsWith(MediaConstant.MEDIA_FORMAT_TS);
    }

    public static boolean isVideo(String format) {
        return format.equals(MediaConstant.MEDIA_FORMAT_MP4)
                || format.equals(MediaConstant.MEDIA_FORMAT_TS);
    }

    public static boolean isPic(File f) {
        return f.getName().endsWith(MediaConstant.MEDIA_FORMAT_JPG)
                || f.getName().endsWith(MediaConstant.MEDIA_FORMAT_BMP)
                || f.getName().endsWith(MediaConstant.MEDIA_FORMAT_JPEG)
                || f.getName().endsWith(MediaConstant.MEDIA_FORMAT_PNG);
    }

    public static boolean isPic(String format) {
        return format.equals(MediaConstant.MEDIA_FORMAT_JPG)
                || format.equals(MediaConstant.MEDIA_FORMAT_BMP)
                || format.equals(MediaConstant.MEDIA_FORMAT_JPEG)
                || format.equals(MediaConstant.MEDIA_FORMAT_PNG);
    }
}
