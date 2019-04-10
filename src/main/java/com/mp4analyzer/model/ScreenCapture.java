package com.mp4analyzer.model;

import java.util.Objects;

public class ScreenCapture {

    private byte[] imageBytes;
    private long frameLocation;

    public ScreenCapture() {
    }

    public ScreenCapture(byte[] imageBytes, long frameLocation) {
        this.imageBytes = imageBytes;
        this.frameLocation = frameLocation;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public long getFrameLocation() {
        return frameLocation;
    }

    public void setFrameLocation(long frameLocation) {
        this.frameLocation = frameLocation;
    }
}
