/**   
 * <p><h1>Copyright:</h1><strong><a href="http://www.smart-f.cn">
 * BeiJing Smart Future Technology Co.Ltd. 2015 (c)</a></strong></p>
 */
package smart.facerecognition.media;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

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
 * <li> 2017年5月25日 下午1:25:38    V1.0.0          jjj         first release</li>
 * </p> 
 * @Title FFmpegCommand.java 
 * @Description please add description for the class 
 * @author jjj
 * @email <a href="jiangjunjie@smart-f.cn">jiangjunjie@smart-f.cn</a>
 * @date 2017年5月25日 下午1:25:38 
 * @version V1.0   
 */
public class FFmpegCommand {

    private static final String  TAG                   = FFmpegCommand.class.getSimpleName();

    public static List<String> getRecordCmd(String srcUrl, String storePath,
            String name, String format, double volume) {
        Log.i(TAG, "getRecordCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format+", v"+volume);
        List<String> command= new ArrayList<String>();
        command.add(FFmpegConstant.getFfmpegPath());
        command.add("-rtsp_transport");
        command.add("tcp");
        command.add("-i");
        command.add(srcUrl);
        command.add("-vcodec");
        command.add("copy");
        //command.add("-y");
        command.add("-af");
        command.add("volume="+volume);
        command.add(storePath+name/*+format*/);
        return command;
    }

    public static List<String> getRecordMixCmd(String srcUrl, String storePath,
            String name, String format, double volume, String bgmPath, double mVolume) {
        Log.i(TAG, "getRecordMixCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format+", v"+volume
                +", bgmPath:"+bgmPath+", mVolume:"+mVolume);
        List<String> command= new ArrayList<String>();
        command.add(FFmpegConstant.getFfmpegPath());
        command.add("-rtsp_transport");
        command.add("tcp");
        command.add("-i");
        command.add(srcUrl);
        command.add("-vcodec");
        command.add("copy");
        //command.add("-y");
        command.add("-af");
        command.add("volume="+volume);
        command.add(storePath+name/*+format*/);
        return command;
    }

    public static List<String> getRecordSegmentCmd(String srcUrl, String storePath,
            String name, String format, double volume, int time) {
        Log.i(TAG, "getRecordSegmentCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format+", v"+volume);
        List<String> command= new ArrayList<String>();
        command.add(FFmpegConstant.getFfmpegPath());
        command.add("-rtsp_transport");
        command.add("tcp");
        command.add("-i");
        command.add(srcUrl);
        command.add("-vcodec");
        command.add("copy");
        //command.add("-y");
        command.add("-f");
        command.add("segment");
        command.add("-segment_time");
        command.add(""+time);
        command.add("-af");
        command.add("volume="+volume);
        command.add(storePath+name/*+format*/);
        return command;
    }

    public static List<String> getRecordSegmentMixCmd(String srcUrl, String storePath,
            String name, String format, double volume, int time, String bgmPath, double mVolume) {
        Log.i(TAG, "getRecordSegmentCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format+", v"+volume
                +", bgmPath:"+bgmPath+", mVolume:"+mVolume);
        List<String> command= new ArrayList<String>();
        command.add(FFmpegConstant.getFfmpegPath());
        command.add("-rtsp_transport");
        command.add("tcp");
        command.add("-i");
        command.add(srcUrl);
        command.add("-vcodec");
        command.add("copy");
        //command.add("-y");
        command.add("-f");
        command.add("segment");
        command.add("-segment_time");
        command.add(""+time);
        command.add("-af");
        command.add("volume="+volume);
        command.add(storePath+name/*+format*/);
        return command;
    }

    public static List<String> getSnapshotCmd(String srcUrl, String storePath,
            String name, String format, int interval) {
        Log.i(TAG, "getSnapshotCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format+", interval:"+interval);
        List<String> snapshot = new ArrayList<String>();
        snapshot.add(srcUrl);
        snapshot.add("-an");
        snapshot.add("-f");
        snapshot.add("image2");
        snapshot.add("-r");
        snapshot.add(""+interval);//1
        snapshot.add(storePath+name/*+format*/);
        return wrapCmd(snapshot);
    }

    /**
     * cut some segment from a video
     * */
    public static List<String> getCutCmd(String srcUrl, Pair<Float, Float> sectionTime, String storePath,
            String name) {
        Log.i(TAG, "getCutCmd, src:"+srcUrl+", start:"+sectionTime.first+", end:"+sectionTime.second+", store:"+storePath
                +", name:"+name);
        //ffmpeg -i input.mp4 -ss 7.326969 -to 10.477327 -vcodec copy -an output.ts
        List<String> command= new ArrayList<String>();
        command.add(srcUrl);
        command.add("-ss");
        command.add(""+sectionTime.first);
        command.add("-to");
        command.add(""+sectionTime.second);
        command.add("-vcodec");
        command.add("copy");
//        command.add("-vbsf");
//        command.add("h264_mp4toannexb");
        command.add("-an");
        command.add(storePath+name/*+format*/);
        Log.i(TAG, "getCutCmd, command:"+command);
        return wrapCmdWithoutTcp(command);
    }

    /**
     * connect some videos to one video
     * */
    public static List<String> getConnectCmd(List<String> sectionList, String storePath,
            String name) {
        Log.i(TAG, "getConnectCmd, sectionList:"+sectionList+", store:"+storePath
                +", name:"+name);
        //ffmpeg -i 1.mp4 -vcodec copy -acodec copy -vbsf h264_mp4toannexb 1.ts
        //ffmpeg -i 2.mp4 -vcodec copy -acodec copy -vbsf h264_mp4toannexb 2.ts
        //ffmpeg -i "concat:1.ts|2.ts" -acodec copy -vcodec copy -absf aac_adtstoasc output.mp4
        List<String> command= new ArrayList<String>();
        String sections = "";
        for (String s:sectionList) {
            sections += s+"|";
        }
        command.add("concat:"+sections.substring(0, sections.length()-1));
        command.add("-acodec");
        command.add("copy");
        command.add("-vcodec");
        command.add("copy");
        command.add("-absf");
        command.add("aac_adtstoasc");
        command.add(storePath+name/*+format*/);
        Log.i(TAG, "getConnectCmd, command:"+command);
        return wrapCmdWithoutTcp(command);
    }

    /**
     * mix a bgm into video
     * */
    public static List<String> getMixBGMCmd(String srcUrl, String bgmPath, String storePath,
            String name, double volume, double bmgVolume) {
        Log.i(TAG, "getMixBGMCmd, src:"+srcUrl+", bgmPath:"+bgmPath+", store:"+storePath
                +", name:"+name+", v"+volume+", bmgVolume"+bmgVolume);
        //ffmpeg -i 1.mp3 -i 1.mp4 -vcodec mpeg4 -acodec copy out.mp4
        List<String> command= new ArrayList<String>();
        command.add(srcUrl);
        command.add("-i");
        command.add(bgmPath);
        command.add("-vcodec");
        command.add("copy");
        command.add("-acodec");
        command.add("copy");
        command.add("-shortest");
        command.add(storePath+name/*+format*/);
        Log.i(TAG, "getMixBGMCmd, command:"+command);
        return wrapCmdWithoutTcp(command);
    }
    
    private static List<String> wrapCmd(List<String> params) {
        List<String> command= new ArrayList<String>();
        command.add(FFmpegConstant.getFfmpegPath());
        command.add("-rtsp_transport");
        command.add("tcp");
        command.add("-i");
        command.addAll(params);
        return command;
    }

    /**
     * generate a transform video from a video
     * */
    public static List<String> getGenVideoByFramesCmd(String srcUrl, String storePath,
            String name, String format) {
        Log.i(TAG, "getGenVideoByFramesCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format);
        List<String> command = new ArrayList<String>();
        command.add(srcUrl);
        command.add("-vcodec");
        command.add("libx264");
        command.add("-pix_fmt");
        command.add("yuv420p");
//        command.add("-vbsf");
//        command.add("h264_mp4toannexb");
        command.add(storePath+name+format);
        Log.i(TAG, "getGenVideoByFramesCmd:"+command);
        return wrapCmdWithoutTcp(command);
    }

    public static List<String> getSnapshotAllCmd(String srcUrl, String storePath,
            String name, String format,Pair<Float, Float> sectionTime) {
        Log.i(TAG, "getSnapshotAllCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format + ", start:"+sectionTime.first + ", end:"+sectionTime.second);
        List<String> snapshot = new ArrayList<String>();
        snapshot.add(srcUrl);
        snapshot.add("-ss");
        snapshot.add(String.valueOf(sectionTime.first));
        snapshot.add("-t"); // 添加参数＂-t＂，该参数指定持续时间
        snapshot.add(""+(sectionTime.second - sectionTime.first));
        //snapshot.add("-s"); // 添加参数＂-s＂，设置截取的图片分辨率
        //snapshot.add("640*360");
        snapshot.add(storePath+name+format);
        Log.i(TAG, "getSnapshotAllCmd:"+snapshot);
        return wrapCmdWithoutTcp(snapshot);
    }

    public static List<String> getSnapshotSomeCmd(String srcUrl, String storePath,
            String name, String format, int start, double interval, int end) {
        Log.i(TAG, "getSnapshotSomeCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format+", interval:"+interval + ", end:"+end);
        List<String> snapshot = new ArrayList<String>();
        snapshot.add(srcUrl);
        snapshot.add("-f");
        snapshot.add("image2");
        snapshot.add("-ss"); // 添加参数＂-ss＂，该参数指定截取的起始时间
        snapshot.add(String.valueOf(start)); // 添加起始时间为第17秒

        //snapshot.add("-t"); // 添加参数＂-t＂，该参数指定持续时间
        //snapshot.add("0.001"); // 添加持续时间为1毫秒
        //snapshot.add("-s"); // 添加参数＂-s＂，设置截取的图片分辨率
        //snapshot.add("640*360");
        //snapshot.add("-r");//设置帧率，0.1则10秒一张; 此方式可同时指定起始和间隔，但如果程序是arm版的速度会很慢，耗时=最后截取帧所在时间-视频开始时间
        //此方式缺点在于，抽帧后的图片，某些相邻图片处在相同时间
        //if output file count more than one, the first and second picture will be same, should delete one.
        //snapshot.add(""+(double)1/interval);

        snapshot.add("-vf");//设置每隔几秒一张; 此方式很快，但第一张图时间不随起始时间而增加，只是小于起始时间的会丢弃
        snapshot.add("fps=1/"+interval);
        snapshot.add("-frames");
        snapshot.add(String.valueOf((end-start)/interval+1));
        snapshot.add(storePath+name+format);
        Log.i(TAG, "getSnapshotSomeCmd:"+snapshot);
        return wrapCmdWithoutTcp(snapshot);
    }

    public static List<String> getKeyFrameInfoCmd(String srcUrl, String storePath,
            String name, String format) {
        Log.i(TAG, "getKeyFrameInfoCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format);
        List<String> snapshot = new ArrayList<String>();
        snapshot.add(srcUrl);
        snapshot.add("-vf");
        snapshot.add("select=eq(pict_type\\,PICT_TYPE_I)");
        snapshot.add("-vsync");
        snapshot.add("2");
        //snapshot.add("-frames");
        //snapshot.add("3");
        snapshot.add(storePath+name+format);
        snapshot.add("-loglevel");
        snapshot.add("debug");
        /*snapshot.add("2>&1");
        snapshot.add("|");
        snapshot.add("grep");
        snapshot.add("\"pict_type:I\"");*/
        Log.i(TAG, "getKeyFrameInfoCmd:"+snapshot);
        return wrapCmdWithoutTcp(snapshot);
    }

    public static List<String> getKeyFrameIntervalCmd(String srcUrl, String storePath,
                                                  String name, float interval, String format) {
        float r = 1/interval;
        Log.i(TAG, "getKeyFrameInfoCmd, src:"+srcUrl+", store:"+storePath
                +", name:"+name+", format:"+format);
        List<String> snapshot = new ArrayList<String>();
        snapshot.add(srcUrl);
        snapshot.add("-vf");
        snapshot.add("select=eq(pict_type\\,PICT_TYPE_I)");
        snapshot.add("-vsync");
        snapshot.add("2");
        snapshot.add("-r");
        snapshot.add(Float.toString(r));
        //snapshot.add("-frames");
        //snapshot.add("3");
        snapshot.add(storePath+name+format);
//        snapshot.add("-loglevel");
//        snapshot.add("debug");
        /*snapshot.add("2>&1");
        snapshot.add("|");
        snapshot.add("grep");
        snapshot.add("\"pict_type:I\"");*/
        Log.i(TAG, "getKeyFrameInfoCmd:"+snapshot);
        return wrapCmdWithoutTcp(snapshot);
    }

    public static List<String> getBlendCmd(String srcUrl1, String srcUrl2, String storePath,
            String name, String format) {
        //ffmpeg -i [第一个视频] -i [第二个视频] -filter_complex "blend=all_expr='A*(if(gte(T,1),1,T/1))+B*(1-(if(gte(T,1),1,T/1)))'" [输出视频]
        Log.i(TAG, "getBlendCmd, src1:"+srcUrl1+", src2:"+srcUrl2+", store:"+storePath
                +", name:"+name+", format:"+format);
        List<String> command = new ArrayList<String>();
        command.add(srcUrl1);
        command.add("-i");
        command.add(srcUrl2);
        command.add("-filter_complex");
        command.add("blend=all_expr='A*(if(gte(T,1),1,T/1))+B*(1-(if(gte(T,1),1,T/1)))'");
        command.add(storePath+name+format);
        Log.i(TAG, "getBlendCmd:"+command);
        return wrapCmdWithoutTcp(command);
    }
    private static List<String> wrapCmdWithoutTcp(List<String> command) {
        List<String> cmd= new ArrayList<String>();
        cmd.add(FFmpegConstant.getFfmpegPath());
        cmd.add("-i");
        cmd.addAll(command);
        return cmd;
    }
}
