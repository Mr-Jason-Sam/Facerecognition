package smart.facerecognition.gson;

import java.util.List;

/**
 * Created by jasonsam on 2017/12/11.
 */

public class IdentifyInfo{
    String uid;
    List<Double> scores;
    String droup_id;
    String user_info;

    public IdentifyInfo(){}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Double> getScores() {
        return scores;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
    }

    public String getDroup_id() {
        return droup_id;
    }

    public void setDroup_id(String droup_id) {
        this.droup_id = droup_id;
    }

    public String getUser_info() {
        return user_info;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }
}
