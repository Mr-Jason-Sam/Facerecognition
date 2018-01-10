/**   
 * <p><h1>Copyright:</h1><strong><a href="http://www.smart-f.cn">
 * BeiJing Smart Future Technology Co.Ltd. 2015 (c)</a></strong></p>
 */
package smart.facerecognition.media;

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
 * <li> 2016年8月27日 下午10:02:43    V1.0.0          jjj         first release</li>
 * </p> 
 * @Title MediaConstant.java 
 * @Description please add description for the class 
 * @author jjj
 * @email <a href="jiangjunjie@smart-f.cn">jiangjunjie@smart-f.cn</a>
 * @date 2016年8月27日 下午10:02:43 
 * @version V1.0   
 */
public class MediaConstant {

    /** sync with smartmediartsptask.h */
    public static final int SMART_OK                                  =  0x00;
    public static final int SMART_ERROR_INVALID_INPUT_PARAM           = -0x01;
    public static final int SMART_ERROR_OUTPUT_PATH_CAN_NOT_WRITE     = -0x02;
    public static final int SMART_ERROR_INPUT_RESOURCE_ERROR          = -0x03;
    public static final int SMART_ERROR_INPUT_RESOURCE_DECODER_ERROR  = -0x04;
    public static final int SMART_ERROR_FUNCTION_CALL_STATE_ERROR     = -0x05;
    public static final int SMART_ERROR_INTERNAL_ERROR                = -0xFF;

    /// arg1:
    public static final int SMART_MEDIA_RTSP_TASK_EVENT_SEGMENT_COMPLETE   = 0x0001;
    public static final int SMART_MEDIA_RTSP_TASK_EVENT_MP4_COMPLETE       = 0x0002;
    /// arg1: the jpeg file path witch has been saved.
    /// arg2: the jpeg file number
    //17
    public static final int SMART_MEDIA_RTSP_TASK_EVENT_IMAGE_COMPLETE     = 0x0011;
    /// arg1: the last jpeg file path witch has been saved.
    /// arg2: the total file number
    public static final int SMART_MEDIA_RTSP_TASK_EVENT_IMAGE_FULL_COMPLETE= 0x0012;
    /// arg1: NULL
    /// arg2: NULL
    //4097
    public static final int SMART_MEDIA_RTSP_TASK_EVENT_READ_FRAME_ERROR   = 0x1001;


    public static final int SMART_MEDIA_FFMPEG_QUIT                        = 0x1003;

    /// arg1: NULL
    /// arg2: NULL
    public static final int SMART_MEDIA_RTSP_TASK_EVENT_NETWORK_TIMEOUT    = 0x1002;
    public static final int SMART_MEDIA_TASK_INPUT_ERROR                   = 0x1003;
    //-1
    public static final int SMART_MEDIA_TASK_COMPLETE                      = 0xffff;
    public static final int PRODUCE_START                                  = 0x2001;
    public static final int PRODUCE_SNAPSHOT_PREVIEW                       = 0x2002;
    public static final int PRODUCE_SNAPSHOT_LIST                          = 0x2003;
    public static final int PRODUCE_GET_FPS                                = 0x2004;
    public static final int PRODUCE_VIDEO_CUT                              = 0x2005;
    public static final int PRODUCE_VIDEO_FRAME                            = 0x2006;
    public static final int PRODUCE_VIDEO_TRANS                            = 0x2007;
    public static final int PRODUCE_VIDEO_CONNECT                          = 0x2008;
    public static final int PRODUCE_MIX_BGM                                = 0x2009;
    public static final int PRODUCE_FINISH                                 = 0x2010;
    
    public static final String MEDIA_FORMAT_TS                             = ".ts";
    public static final String MEDIA_FORMAT_MP4                            = ".mp4";
    public static final String MEDIA_FORMAT_BMP                            = ".bmp";
    public static final String MEDIA_FORMAT_JPG                            = ".jpg";
    public static final String MEDIA_FORMAT_JPEG                           = ".jpeg";
    public static final String MEDIA_FORMAT_PNG                            = ".png";

    public static final String PREFIX_SNAP                                 = "snap";
    public static final String PREFIX_CUT                                  = "cut";
    public static final String PREFIX_ID                                   = "identified";

    public static final long     MIN_SEGMENT_DURATION                      = 2;

    public static final String   DEFAULT_VIDEO_SEGMENT_NAME_PATTERN        = "n%08d";
    public static final String   DEFAULT_MIX_SEGMENT_NAME_PATTERN          = "mix%08d";
    public static final String   DEFAULT_IMAGE_SUCCESSIVE_NAME_PATTERN     = "%08d";

    public static final String   DEFAULT_VIDEO_SEGMENT_NAME                = DEFAULT_VIDEO_SEGMENT_NAME_PATTERN+MediaConstant.MEDIA_FORMAT_TS;
    public static final String   DEFAULT_MIX_SEGMENT_NAME                  = DEFAULT_MIX_SEGMENT_NAME_PATTERN+MediaConstant.MEDIA_FORMAT_TS;
    public static final String   DEFAULT_IMAGE_SUCCESSIVE_NAME             = DEFAULT_IMAGE_SUCCESSIVE_NAME_PATTERN+MediaConstant.MEDIA_FORMAT_BMP;

    public static final String   DEFAULT_VIDEO_NAME                        = "normal"+MediaConstant.MEDIA_FORMAT_MP4;
    public static final String   DEFAULT_MIX_NAME                          = "mix"+MediaConstant.MEDIA_FORMAT_MP4;
    public static final String   DEFAULT_IMAGE_NAME                        = "image"+MediaConstant.MEDIA_FORMAT_BMP;


}
