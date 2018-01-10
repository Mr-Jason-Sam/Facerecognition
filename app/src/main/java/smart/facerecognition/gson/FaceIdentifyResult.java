package smart.facerecognition.gson;

import java.util.List;

/**
 * Created by jasonsam on 2017/12/11.
 */

public class FaceIdentifyResult {
    private int result_num;
    private List<IdentifyInfo> result;
    private long log_id;

    public int getResult_num() {
        return result_num;
    }

    public void setResult_num(int result_num) {
        this.result_num = result_num;
    }

    public List<IdentifyInfo> getResult() {
        return result;
    }

    public void setResult(List<IdentifyInfo> result) {
        this.result = result;
    }

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }
}
