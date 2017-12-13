package smart.facerecognition;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by jasonsam on 2017/12/12.
 */

public class Child {
    String name;
    int id;
    String sex;
    int grade;
    String className;
    Bitmap face;
    List<String> videoName;

    public Child(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Bitmap getFace() {
        return face;
    }

    public void setFace(Bitmap face) {
        this.face = face;
    }

    public List<String> getVideoName() {
        return videoName;
    }

    public void setVideoName(List<String> videoName) {
        this.videoName = videoName;
    }
}
