package smart.facerecognition.gson;

import android.location.Location;

import smart.facerecognition.Face;

/**
 * Created by jasonsam on 2017/12/7.
 */

public class FaceInfo {

    private Face location;
    private double face_probability;
    private int rotation_angle;
    private double yaw;
    private double pitch;
    private  double roll;
    private int expression;
    private double expression_probablity;

    public Face getLocation() {
        return location;
    }

    public void setLocation(Face location) {
        this.location = location;
    }

    public double getFace_probability() {
        return face_probability;
    }

    public void setFace_probability(double face_probability) {
        this.face_probability = face_probability;
    }

    public int getRotation_angle() {
        return rotation_angle;
    }

    public void setRotation_angle(int rotation_angle) {
        this.rotation_angle = rotation_angle;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public int getExpression() {
        return expression;
    }

    public void setExpression(int expression) {
        this.expression = expression;
    }

    public double getExpression_probablity() {
        return expression_probablity;
    }

    public void setExpression_probablity(double expression_probablity) {
        this.expression_probablity = expression_probablity;
    }
}
