package smart.facerecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.baidu.aip.face.AipFace;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smart.facerecognition.SmartAnalyze.ApiCallInfo;
import smart.facerecognition.SmartAnalyze.Child;
import smart.facerecognition.SmartAnalyze.Face;
import smart.facerecognition.gson.FaceDetectResult;
import smart.facerecognition.gson.FaceIdentifyResult;
import smart.facerecognition.gson.FaceInfo;
import smart.facerecognition.gson.IdentifyInfo;
import smart.facerecognition.media.FFmpegMediaProcesser;
import smart.facerecognition.media.MediaConstant;
import smart.facerecognition.media.SnapshotTask;
import smart.facerecognition.util.BitmapUtil;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String APP_ID = "10607929";
    public static final String API_KEY = "7ATH4LEjdxZCuIXrsNO56GlI";
    public static final String SECRET_KEY = "iWTGbiEGPjxVcG4PCzUQoKi9eRHy5uhd";
    public static final String VIDEO = "Video";
    public static final String STORAGE = Environment.getExternalStorageDirectory().getPath() + File.separator;
    public static final String CLASSIFY = STORAGE  + "SmartAnalyst" + File.separator;
    public static final String GROUP = "tansuoTest";
    public static final String FACEPATH = "face" + File.separator;
    public static final String VIDEOPATH = "video";
    public static final int    API_CALL_MAX = 5;
    public static final int INTERVAL = 3;
    private Handler mTaskHandler;
    private Handler mApiHandler;
    public static final double CUT_SCALE                      = 2.5;
    private List<Child> mChildren = new ArrayList<Child>();
    public AipFace client;
    private Gson gson = new Gson();
    private List<File> videos = new ArrayList<File>();
    private int num = 0;
    private int length = 0;
    private static int apiCallCnt = 1;
    private static int apiCallCount = 1;
    private List<ApiCallInfo>  monitor= new ArrayList<>();
//    private static ThreadLocal<Integer> x = new ThreadLocal<Integer>();
//    private static Map<Thread, Integer> threadData = new HashMap<Thread,Integer>();

    private static final class TASK {
        private static final int TAKE_FRAMES         = 1;
        private static final int MKDIR_FRAMES_FOLDER = 2;
        private static final int MKDIR_CHILD_FOLDER  = 3;
        private static final int CLASSIFY            = 4;
        private static final int COPY_VIDEO          = 5;
        private static final int INIT_DATA           = 6;
        private static final int SNAPSHOT            = 7;
        private static final int CUTFACE             = 8;
    }

    private static final class API_TASK {
        private static final int DETECT              = 1;
        private static final int CLASSIFY            = 2;
        private static final int MONITOR               = 3;
    }

    Handler.Callback task = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            Log.i(TAG, "handleMessage");
            switch (msg.what){
                case TASK.TAKE_FRAMES:
                    takeFrames(CLASSIFY + VIDEOPATH);
                    break;
                case TASK.MKDIR_FRAMES_FOLDER:
                    break;
                case TASK.MKDIR_CHILD_FOLDER:
                    break;
                case TASK.COPY_VIDEO:
                    Bundle data = (Bundle)msg.obj;
                    String originVideoPath = data.getString("originVideoPath");
                    String newVideoPath = data.getString("newVideoPath");
                    Log.i(TAG, "handleMessage : ----" + "originVideoPath :" + originVideoPath + " newVideoPath: " + newVideoPath);
                    nioTransferCopy(new File(originVideoPath), new File(newVideoPath));
                    break;
                case TASK.INIT_DATA:
                    initData();
                    break;
                case TASK.SNAPSHOT: {
                    FFmpegMediaProcesser processer = new FFmpegMediaProcesser();
                    SnapshotTask task = (SnapshotTask)msg.obj;
                    int interval = task.getInterval();
                    String srcPath = task.getSrcPath();
                    String storePath = task.getStorePath();
                    String type = task.getType();
                    processer.startSnapshotKeyFrame(srcPath, storePath, interval, type);
                    processer.setFFmpegMeidaTaskListener(mListener);
                }
                    break;
                default:
            }
            return false;
        }
    };

    Handler.Callback apiTask = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case API_TASK.DETECT: {
                    String snapshopPath = (String) msg.obj;
                    Log.i(TAG, "API_TASK.DETECT--snapshopPath: " + snapshopPath);
                    detectFrames(snapshopPath);
                }
                break;
                case API_TASK.CLASSIFY: {
                    String snapshopPath = (String) msg.obj;
                    Log.i(TAG, "API_TASK.CLASSIFY--snapshopPath: " + snapshopPath);
                    faceClassify(CLASSIFY + FACEPATH, snapshopPath, GROUP);
                }
                break;
            }
            return false;
        }
    };

//    Handler.Callback apiTask = new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            int what = msg.what;
//            switch (what) {
//                case API_TASK.MONITOR:
//                    long useTime = (long)msg.obj;
//                    ApiCallInfo info = new ApiCallInfo();
//                    info.setUseTime(useTime);
//                    monitor.add(info);
//                    if (monitor.size() == API_CALL_MAX){
//                        ApiCallInfo minUseTime = monitor.get(0);
//                        for (ApiCallInfo information : monitor) {
//                            if (information.getUseTime() < minUseTime.getUseTime())
//                                minUseTime = information;
//                        }
//                        Log.i(TAG, "minUseTime: " + minUseTime.getUseTime());
//                        try {
//                            if (minUseTime.getUseTime() < 1000) {
//                                Thread.sleep(1000 - minUseTime.getUseTime());
//                                monitor.remove(minUseTime);
//                                apiCallCount--;
//                            }
//                            else {
//                                Thread.sleep(1000);
//                                monitor.clear();
//                                apiCallCount = 0;
//                                break;
//                            }
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                    break;
//                case API_TASK.DETECT: {
//                    apiCallCount++;
//                    while (apiCallCount > 5){
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    String snapshopPath = (String) msg.obj;
//                    Log.i(TAG, "API_TASK.DETECT--snapshopPath: " + snapshopPath);
//                    detectFrames(snapshopPath);
//                }
//                break;
//                case API_TASK.CLASSIFY: {
//                    apiCallCount++;
//                    while (apiCallCount > 5){
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    String snapshopPath = (String) msg.obj;
//                    Log.i(TAG, "API_TASK.CLASSIFY--snapshopPath: " + snapshopPath);
//                    faceClassify(CLASSIFY + FACEPATH, snapshopPath, GROUP);
//                }
//                break;
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        test();

        task();
    }

    public void task(){
        mTaskHandler = new Handler(task);
        mApiHandler  = new Handler(apiTask);
//        threadData.put(Thread.currentThread(),0);
        mTaskHandler.sendEmptyMessage(TASK.INIT_DATA);
    }

    private void setPermission(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void initAipFace(){
        client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
    }

    public void initChildFloder(final String childPhotoPath){
        File childPhotoFolder = new File(childPhotoPath);
        for (File childPhoto : childPhotoFolder.listFiles()) {
            if (childPhoto.getAbsolutePath().endsWith(".jpg") ||
                    childPhoto.getAbsolutePath().endsWith(".png") ||
                    childPhoto.getAbsolutePath().endsWith(".jpeg")) {
                mkdirChildVideoFloder(childPhoto);
            }
        }
    }

    private void initData(){
        setPermission();
        initAipFace();
        initChildFloder(CLASSIFY + "face");
        mkdirVideoFramesFloder(CLASSIFY + VIDEOPATH);
        clearData();
        mTaskHandler.sendEmptyMessage(TASK.TAKE_FRAMES);
    }

    private void clearData() {
        mChildren.clear();
    }

//    public void initFramesFloder(String framesPath){
//        File frameFolder = new File(videoFolder.getAbsolutePath() + File.separator + video.getName().split("\\.")[0]);
//        if (!frameFolder.exists())
//            frameFolder.mkdir();
//    }

    FFmpegMediaProcesser.FFmpegMediaTaskListener mListener = new FFmpegMediaProcesser.FFmpegMediaTaskListener() {
        @Override
        public void onMediaTaskEvent(int what, int arg1, int arg2, Object obj) {
            switch (what) {
                case  MediaConstant.SMART_MEDIA_TASK_COMPLETE:
                    num++;
                    if (num < length) {
                        File video = videos.get(num);
                        Message snapshot = Message.obtain();
                        snapshot.obj = obj;
                        snapshot.what = TASK.SNAPSHOT;
                        SnapshotTask task = new SnapshotTask();
                        task.setInterval(INTERVAL);
                        task.setSrcPath(video.getAbsolutePath());
                        task.setStorePath(video.getParentFile().getAbsolutePath() + File.separator + video.getName().split("\\.")[0]);
                        task.setType(MediaConstant.MEDIA_FORMAT_JPEG);
                        snapshot.obj = task;
                        mTaskHandler.sendMessage(snapshot);
                    }
                case MediaConstant.SMART_MEDIA_RTSP_TASK_EVENT_IMAGE_COMPLETE:
                    Log.i(TAG, "what: " + Integer.toHexString(what) + ", arg1: " + arg1 + ", arg2: " + arg2 + ", obj: " + obj);
                    Message cutFace = mApiHandler.obtainMessage();
                    cutFace.obj = obj;
                    cutFace.arg1 = apiCallCnt++;
                    cutFace.what = API_TASK.DETECT;
                    mApiHandler.sendMessage(cutFace);
                    break;
            }
        }
    };


    public void takeFrames(final String videoFolderPath){
        File videoFolder = new File(videoFolderPath);
        File allVideoFile[] = videoFolder.listFiles();
        for (File video: allVideoFile)
            if (video.getName().endsWith(".mp4") || video.getName().endsWith(".ts")) {
                length++;
                videos.add(video);
            }

        File video = videos.get(num);
        addSnapshotTask(video);
    }

    private void addSnapshotTask(File video) {
        if (video.getName().endsWith(".mp4") || video.getName().endsWith(".ts")) {
//                        List<String> command = new ArrayList<String>();
            File frameFolder = new File(video.getParentFile().getAbsolutePath() + File.separator + video.getName().split("\\.")[0]);
            if (!frameFolder.exists())
                frameFolder.mkdir();
            Message message = Message.obtain();
            message.what = TASK.SNAPSHOT;
//            message.arg1 = num++;
            SnapshotTask task = new SnapshotTask();
            task.setInterval(INTERVAL);
            task.setSrcPath(video.getAbsolutePath());
            task.setStorePath(frameFolder.getAbsolutePath());
            task.setType(MediaConstant.MEDIA_FORMAT_JPEG);
            message.obj  = task;
            mTaskHandler.sendMessage(message);
//                        Log.i(TAG, "Handler");
        }
    }
//    public void faceClassify(final String childVideoPath, final String childPath){
//        File childVideoFolder = new File(childVideoPath);
//        for (File childVideo : childVideoFolder.listFiles()) {
//            if (!childVideo.getName().contains(".")) {
//                identifyChild(childVideo.getAbsolutePath(), childPath,"tansuoTest");
//            }
//        }
//
//    }


    public void mkdirChildVideoFloder(final File childPhoto){
        String fileName[] = childPhoto.getName().split("\\.");
        String name = fileName[0];

        //初始化孩子信息
        Child child = new Child(name);
        mChildren.add(child);

        File childVideoFolder = new File(childPhoto.getParentFile().getAbsolutePath() + File.separator + name + VIDEO);
        Log.i(TAG, childVideoFolder.getAbsolutePath());
        if (!childVideoFolder.exists())
            childVideoFolder.mkdir();
    }

    public void mkdirVideoFramesFloder(final String videoFloder){
        File videosFloder = new File(videoFloder);
        for (File videos : videosFloder.listFiles()) {
            File framesFloder = new File(videos.getParentFile().getAbsolutePath() + File.separator + videos.getName().split("\\.")[0]);
            if (!framesFloder.exists())
                framesFloder.mkdir();
        }
    }

    public void faceClassify(final String classifyFolderPath, final String snapshotPath,final String group){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File classifyFolder = new File(classifyFolderPath);
                File snapshot = new File(snapshotPath);
                FaceIdentifyResult result = gson.fromJson(identifyUser(client, snapshotPath, group), FaceIdentifyResult.class);
                if (result != null && result.getResult() != null) {
                    for (IdentifyInfo info : result.getResult()) {
                        double topScore;
                        topScore = info.getScores().get(0);
                        Log.i(TAG, "sorce: " + topScore);
                        if (topScore >= 73) {
                            for (File face : classifyFolder.listFiles()) {
                                if (face.getName().split("\\.")[0].equals(info.getUser_info())) {
                                    File classifyVideo = new File(face.getParentFile().getAbsolutePath() + File.separator + face.getName().split("\\.")[0] + VIDEO);
                                    String originVideoPath = snapshot.getParentFile().getParentFile().getAbsolutePath() + ".mp4";
                                    String newVideoPath = classifyVideo.getAbsolutePath() + File.separator + snapshot.getParentFile().getParentFile().getName() + ".mp4";
                                    Log.i(TAG, "originVideoPath : " + originVideoPath + "  ---  newVideoPath : " + newVideoPath);
                                    if (!classifyVideo.exists())
                                        classifyFolder.mkdirs();
                                    Message message = mTaskHandler.obtainMessage();
                                    message.what = TASK.COPY_VIDEO;
                                    Bundle data = new Bundle();
                                    data.putString("originVideoPath", originVideoPath);
                                    data.putString("newVideoPath", newVideoPath);
                                    message.obj = data;
                                    mTaskHandler.sendMessage(message);
                                }
                            }

                        }
                    }
                }
            }
        }).start();
    }

    public void  detectFrames(String framePath){
        final File cutFaceFolder = new File(framePath.split("\\.")[0]);
        final File frame = new File(framePath);
        if (!cutFaceFolder.exists())
            cutFaceFolder.mkdirs();
//        Bitmap bm = BitmapUtil.getBitmapScaled(framePath, 640);
//        File bmToJpeg = BitmapUtil.saveBitmapToJpeg(bm, cutFaceFolder.getAbsolutePath(), frame.getName());
        detect(frame.getAbsolutePath(), cutFaceFolder.getAbsolutePath());
    }

    public void operateDetect(String localPhotoPath, String group){
        final File photoPath = new File(localPhotoPath);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
        for (File videoName : photoPath.listFiles()){
            File photoCollection = new File(videoName.getAbsolutePath() + File.separator + "Face");
            if (!photoCollection.exists())
                photoCollection.mkdir();
            if (!videoName.getAbsolutePath().contains(".DS_Store")) {
                for (File frame : videoName.listFiles()) {
                    if (frame.getAbsolutePath().endsWith(".jpg") ||
                            frame.getAbsolutePath().endsWith(".png") ||
                            frame.getAbsolutePath().endsWith(".jpeg")) {
                        detect(frame.getAbsolutePath(), photoCollection.getAbsolutePath());
                    }
                }
            }
        }
            }
        });
        thread.start();
    }

    public void detect(final String localImagePath, final String savePath){
        final String imagePath = localImagePath;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FaceDetectResult res = gson.fromJson(faceDetect(imagePath), FaceDetectResult.class);
                if (res != null && res.getResult() != null) {
                    for (FaceInfo info : res.getResult()){
                        cutFrameToImage(imagePath, info.getLocation(), savePath);
                    }
                }
            }
        }).start();

    }

    public void cutFrameToImage(final String image, Face face, String savePath){
        Bitmap bitmap = BitmapFactory.decodeFile(image);
        Bitmap faceBp = BitmapUtil.cutFace(bitmap, face, CUT_SCALE);
        String currentTime = System.currentTimeMillis() + "";
        BitmapUtil.saveBitmapToJpeg(faceBp, savePath, currentTime);
        Message classify = mApiHandler.obtainMessage();
        classify.what = API_TASK.CLASSIFY;
        classify.arg1 = apiCallCnt++;
        classify.obj  = savePath + File.separator + currentTime + BitmapUtil.MEDIA_FORMAT_JPEG;
        mApiHandler.sendMessage(classify);

    }

    public void operateGroup(){
//        final File weilaStorage = new File(SD_STORAGE + "weila");
        final File tansuoStorage = new File(CLASSIFY + "face");
//        Log.i(TAG, weilaStorage.getAbsolutePath());
//        Thread weilaThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (File weilaChild : weilaStorage.listFiles()){
//                    facesetAddUser(client, weilaChild.getAbsolutePath(),System.currentTimeMillis() + "", weilaChild.getName(), "weilaGroup");
//                }
//            }
//        });
//        weilaThread.start();

        Thread tansuoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (File tansuoChild : tansuoStorage.listFiles()){
                    if (tansuoChild.getName().endsWith(".png") ||
                            tansuoChild.getName().endsWith(".jpeg") ||
                            tansuoChild.getName().endsWith(".jpg"))
                    facesetAddUser(client, tansuoChild.getAbsolutePath(),System.currentTimeMillis() + "", tansuoChild.getName().split("\\.")[0], "tansuoTest");
                }
            }
        });
        tansuoThread.start();
    }


    public synchronized String identifyUser(final AipFace client,final String path,final String groupName) {
        HashMap<String, Object> options = new HashMap<String, Object>(1);
        options.put("user_top_num", 1);
        long beforeDetectTime = System.currentTimeMillis();
        JSONObject res = client.identifyUser(Arrays.asList(groupName), path, options);
        long identifyUserUseTime = System.currentTimeMillis() - beforeDetectTime;
        try {
            System.out.println(res.toString(2));
            System.out.println(path);
        }catch (Exception e){
            e.printStackTrace();
        }

//        Message message = mApiHandler.obtainMessage();
//        message.obj = identifyUserUseTime;
//        message.what = API_TASK.MONITOR;
//        mApiHandler.sendMessageAtFrontOfQueue(message);

//        if (detectUseTime <= 250) {
//            try {
//                Thread.sleep(250);
//            } catch (Exception e) {
//                Log.e(TAG, "Thread.sleep is worry");
//            }
//        }
        Log.i(TAG,  "identifyUser: " + identifyUserUseTime);

//        if (apiCallCount >= API_CALL_MAX) {
//            Message message = mApiHandler.obtainMessage();
//            message.obj = System.currentTimeMillis();
//            message.what = API_TASK.RESET;
//            mApiHandler.sendMessage(message);
//        }


//            apiCallCount = 0;

        return res.toString();
    }


    public synchronized String faceDetect(String imagePath) {
        // 自定义参数定义
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("max_face_num", "10");
        options.put("face_fields", "expression");
        long beforeDetectTime = System.currentTimeMillis();
        JSONObject response = client.detect(imagePath, options);
        long detectUseTime = System.currentTimeMillis() - beforeDetectTime;

//        Message message = mApiHandler.obtainMessage();
//        message.obj = detectUseTime;
//        message.what = API_TASK.MONITOR;
//        mApiHandler.sendMessageAtFrontOfQueue(message);

//        if (detectUseTime <= 250) {
//            try {
//                Thread.sleep(250);
//            } catch (Exception e) {
//                Log.e(TAG, "Thread.sleep is worry");
//            }
//        }

        System.out.println(response.toString());
        Log.i(TAG,  "detectUseTime: " + detectUseTime);

//        if (apiCallCount >= API_CALL_MAX) {
//            Message message = mApiHandler.obtainMessage();
//            message.obj = System.currentTimeMillis();
//            message.what = API_TASK.RESET;
//            mApiHandler.sendMessage(message);
//        }

//        if (apiCallCount == 5)
//            apiCallCount = 0;
        return response.toString();
    }

    public void facesetAddUser(final AipFace client,final String path,final String uid,final String userInfo,final String groupName) {
        HashMap<String, String> options = new HashMap<String, String>();
        JSONObject res = client.addUser(uid, userInfo, Arrays.asList(groupName), path, options);
        try {
            System.out.println(res.toString());
            System.out.println(userInfo);
        }catch (Exception e){
            e.printStackTrace();
        }
//        return res.toString();
    }

    public static void nioTransferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);

            inStream.close();
            in.close();
            outStream.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTestData(){
        setPermission();
        initAipFace();
        initChildFloder(CLASSIFY + "face");
        mkdirVideoFramesFloder(CLASSIFY + "tansuo_video");
        clearData();
    }


    private void test() {
        initTestData();
        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
                    FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
                    Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
                    FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
                    Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
//                Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
//                Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
                FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
                Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
                FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
                Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
                FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
                Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 20; i++){
//                    FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg", "tansuoTest"), FaceIdentifyResult.class);
                FaceIdentifyResult result2= gson.fromJson(faceDetect(CLASSIFY + "tansuo_video/2017_11_06_08点37分31秒0/snap00000006/1515247211809.jpeg"), FaceIdentifyResult.class);
//                    Log.i(TAG, result1.toString());
                Log.i(TAG, result2.toString());
//                }
//                FaceIdentifyResult result1 = gson.fromJson(identifyUser(client, CLASSIFY + "tansuo_video/2/snap00000003.jpeg", "tansuoTest"), FaceIdentifyResult.class);
//                Log.i(TAG, result1.toString());
            }
        }).start();

    }



}
