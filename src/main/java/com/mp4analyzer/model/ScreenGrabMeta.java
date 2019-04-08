package com.mp4analyzer.model;

import java.util.Objects;

public class ScreenGrabMeta {

    private String path;
    private long frameLocation;

    public ScreenGrabMeta() {
    }

    public ScreenGrabMeta(String path, long frameLocation) {
        this.path = path;
        this.frameLocation = frameLocation;
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

    @Override
    public String toString() {
        return "ScreenGrabMeta{" +
                "path='" + path + '\'' +
                ", frameLocation=" + frameLocation +
                '}';
    }
}
