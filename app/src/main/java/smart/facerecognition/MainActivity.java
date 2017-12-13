package smart.facerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.baidu.aip.face.AipFace;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import smart.facerecognition.gson.FaceDetectResult;
import smart.facerecognition.gson.FaceIdentifyResult;
import smart.facerecognition.gson.FaceInfo;
import smart.facerecognition.media.FFmpegCommand;
import smart.facerecognition.media.FFmpegMediaProcesser;
import smart.facerecognition.media.MediaConstant;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final String APP_ID = "10488002";
    public static final String API_KEY = "hChBmEUKShzdlxzv0Y3zXjQs";
    public static final String SECRET_KEY = "pC13xw0slQdizFBDsYVqSDbAgb5bLfdz";
    public static final String DATABASE = "Video";
    public static final double CUT_SCALE                      = 1.5;
    private List<Child> mChildren = new ArrayList<Child>();
    public AipFace client;
    private Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        task();
    }

    public void task(){
        takeFrames(Environment.getExternalStorageDirectory().getPath() + "/tansuo_video");
//        faceDetect();
//        faceIdentify();
//        faceClassify();
    }

    public void takeFrames(final String videoFolderPath){
        Thread takeFramesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                File videoFolder = new File(videoFolderPath);
                Log.i(TAG, "takeFrames, videoFolder:"+videoFolder.getAbsolutePath()
                        +", e:"+videoFolder.exists()+", w:"+videoFolder.canWrite()+", r:"+videoFolder.canRead()
                +", d:"+videoFolder.isDirectory());
                String[] files = videoFolder.list();
                Log.i(TAG, "takeFrames, files:"+files);
                Log.i(TAG, "takeFrames, files.length:"+files.length);
                for (String f : files) {
                    File video = new File(f);
                    FFmpegMediaProcesser ffmp = new FFmpegMediaProcesser();
                    if (video.getName().endsWith(".mp4") || video.getName().endsWith(".ts")) {
                        List<String> command = new ArrayList<String>();
                        File frameFolder = new File(videoFolder.getAbsolutePath() + File.separator + video.getName().split("\\.")[0]);
                        if (!frameFolder.exists())
                            frameFolder.mkdir();
                        command.addAll(FFmpegCommand.getKeyFrameIntervalCmd(video.getAbsolutePath(),
                                frameFolder.getAbsolutePath(),
                                  File.separator + "%08dframes",
                                10,
                                MediaConstant.MEDIA_FORMAT_JPEG));
                        ffmp.processFFmpegCmd(command);
                    }
                }
            }
        });
        takeFramesThread.start();
    }

    public void faceClassify(final String childPhotoPath, final String childVideoPath){
        File childPhotoFolder = new File(childPhotoPath);
        File childvideoFolder = new File(childVideoPath);
        for (File childPhoto : childPhotoFolder.listFiles()) {
            if (childPhoto.getAbsolutePath().endsWith(".jpg") ||
                    childPhoto.getAbsolutePath().endsWith(".png") ||
                    childPhoto.getAbsolutePath().endsWith(".jpeg")) {
                mkdirChildVideoFloder(childPhoto);
            }
        }

        for (File childvideo : childvideoFolder.listFiles()) {
            if (childvideo.getName().endsWith("Frames")) {
                identifyChild(childvideo.getName().split("//.")[0], childvideo.getAbsolutePath() + File.separator + "Face", "tansuoClass");
            }
        }

    }


    public void initAipFace(){
        client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
    }

    public void initData(){
        mChildren.clear();
        initAipFace();
//        mkdirChildVideoFloder(SD_STORAGE + "weila");
//        mkdirChildVideoFloder(SD_STORAGE + "tansuo");
    }

    public void mkdirChildVideoFloder(final File childPhoto){
        String fileName[] = childPhoto.getName().split("\\.");
        String name = fileName[0];

        //初始化孩子信息
        Child child = new Child(name);
        mChildren.add(child);

        File childVideoFolder = new File(childPhoto.getParentFile().getAbsolutePath() + File.separator + name + DATABASE);
        Log.i(TAG, childVideoFolder.getAbsolutePath());
        if (!childVideoFolder.exists())
            childVideoFolder.mkdir();
    }

    public void identifyChild(final String videoName, final String facePath,final String group){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                File photoPath = new File(facePath);
                for (File p : photoPath.listFiles()) {
//                    Log.i(TAG, "--start--");
                    double topScore;
                    FaceIdentifyResult result = gson.fromJson(identifyUser(client, p.getAbsolutePath(), group), FaceIdentifyResult.class);
                    if (result != null && result.getResult() != null) {
                        topScore = result.getResult().get(0).getScores().get(0);

                        Log.i(TAG, "sorce: " + topScore);
                        if (topScore >= 73){
                            int index = 0;
                            for (Child c : mChildren){
                                if (c.getName().equals(result.getResult().get(0).getUser_info())){
                                    mChildren.get(index).getVideoName().add(videoName);
                                }
                                index++;
                            }

                        }
                    }
//                    Log.i(TAG, "--end--");
                }
            }
        });
        thread.start();
    }

//    {
//        12-12 09:55:14.908 6703-6719/smart.facerecognition I/System.out:   "result": [
//        12-12 09:55:14.908 6703-6719/smart.facerecognition I/System.out:     {
//        12-12 09:55:14.908 6703-6719/smart.facerecognition I/System.out:       "uid": "1512722271850",
//                12-12 09:55:14.909 6703-6719/smart.facerecognition I/System.out:       "scores": [
//        12-12 09:55:14.909 6703-6719/smart.facerecognition I/System.out:         93.931541442871
//        12-12 09:55:14.909 6703-6719/smart.facerecognition I/System.out:       ],
//        12-12 09:55:14.909 6703-6719/smart.facerecognition I/System.out:       "group_id": "zhihuiGroup",
//                12-12 09:55:14.909 6703-6719/smart.facerecognition I/System.out:       "user_info": "3.jpg"
//        12-12 09:55:14.910 6703-6719/smart.facerecognition I/System.out:     }
//        12-12 09:55:14.910 6703-6719/smart.facerecognition I/System.out:   ],
//        12-12 09:55:14.910 6703-6719/smart.facerecognition I/System.out:   "result_num": 1,
//            12-12 09:55:14.910 6703-6719/smart.facerecognition I/System.out:   "log_id": 3411044881121209
//        12-12 09:55:14.911 6703-6719/smart.facerecognition I/System.out: }

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
        String imagePath = localImagePath;
        Log.i(TAG, "--------start-detect-------");
        FaceDetectResult r = gson.fromJson(faceDetect(imagePath), FaceDetectResult.class);
        Log.i(TAG, "--------end-detect-------");
        if (r != null && r.getResult() != null) {
            for (FaceInfo temp : r.getResult()){
                cutFrameToImage(imagePath, temp.getLocation(), savePath);
            }
        }

    }

    public void cutFrameToImage(final String image, Face face, String savePath){
        Bitmap bitmap = BitmapFactory.decodeFile(image);
//        Bitmap faceBp = Bitmap.createBitmap(bitmap, face.left, face.top, face.width, face.height, null, false);
        Bitmap faceBp = BitmapUtil.cutFace(bitmap, face, CUT_SCALE);
        BitmapUtil.saveBitmapToJpg(faceBp, savePath, System.currentTimeMillis() + "");
    }

    public void operateGroup(){
//        final File weilaStorage = new File(SD_STORAGE + "weila");
        final File tansuoStorage = new File(Environment.getExternalStorageDirectory().getPath() + "tansuo");
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
                    facesetAddUser(client, tansuoChild.getAbsolutePath(),System.currentTimeMillis() + "", tansuoChild.getName().split("\\.")[0], "tansuoClass");
                }
            }
        });
        tansuoThread.start();
    }


    public String identifyUser(final AipFace client,final String path,final String groupName) {
        HashMap<String, Object> options = new HashMap<String, Object>(1);
        options.put("user_top_num", 1);
        JSONObject res = client.identifyUser(Arrays.asList(groupName), path, options);
        try {
            System.out.println(res.toString(2));
            System.out.println(path);
        }catch (Exception e){
            e.printStackTrace();
        }
        return res.toString();
    }


    public String faceDetect(String imagePath) {
        // 自定义参数定义
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("max_face_num", "10");
        options.put("face_fields", "expression");

        JSONObject response = client.detect(imagePath, options);
        System.out.println(response.toString());
//        {"result_num":3,
//                "result":[
//
//            {"location":{"left":226,"top":599,"width":197,"height":167},"face_probability":1,"rotation_angle":-3,"yaw":0.89772152900696,"pitch":-1.8784337043762,"roll":-3.6630082130432,"expression":0,"expression_probablity":1},
//            {"location":{"left":1512,"top":604,"width":181,"height":164},"face_probability":1,"rotation_angle":-1,"yaw":9.5259103775024,"pitch":-5.2818622589111,"roll":-0.17514309287071,"expression":0,"expression_probablity":0.99999868869781},
//            {"location":{"left":739,"top":643,"width":181,"height":146},"face_probability":1,"rotation_angle":-3,"yaw":0.72101050615311,"pitch":5.6675357818604,"roll":-3.430915594101,"expression":0,"expression_probablity":1}
//            ],
//            "log_id":3971079120120718}

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



}
