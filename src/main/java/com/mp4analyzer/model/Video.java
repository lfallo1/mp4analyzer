package com.mp4analyzer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Video {

    private String name;
    private String path;
    private double frameRate;
    private List<ScreenCapture> screenCaptureList = new ArrayList<>();

    public Video() {
    }

    public Video(String name, String path, double frameRate) {
        this.name = name;
        this.path = path;
        this.frameRate = frameRate;
    }

    public Video(String name, String path, double frameRate, List<ScreenCapture> screenCaptureList) {
        this.name = name;
        this.path = path;
        this.frameRate = frameRate;
        this.screenCaptureList = screenCaptureList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }

    public List<ScreenCapture> getScreenCaptureList() {
        return screenCaptureList;
    }

    public void setScreenCaptureList(List<ScreenCapture> screenCaptureList) {
        this.screenCaptureList = screenCaptureList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(path, video.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
