package com.mp4analyzer.model;

public class ScreenGrabMeta {

    private String path;
    private long frameLocation;
    private double frameRate;

    public ScreenGrabMeta() {
    }

    public ScreenGrabMeta(String path, long frameLocation, double frameRate) {
        this.path = path;
        this.frameLocation = frameLocation;
        this.frameRate = frameRate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getFrameLocation() {
        return frameLocation;
    }

    public void setFrameLocation(long frameLocation) {
        this.frameLocation = frameLocation;
    }

    public double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }

    @Override
    public String toString() {
        return "ScreenGrabMeta{" +
                "path='" + path + '\'' +
                ", frameLocation=" + frameLocation +
                ", frameRate=" + frameRate +
                '}';
    }
}
