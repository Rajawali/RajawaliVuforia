package org.rajawali3d.vuforia.trackers;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class RVMarkerTracker extends RVTracker {
    private int mMarkerId;
    private String mMarkerName;
    private float mWidth;
    private float mHeight;

    public RVMarkerTracker(int markerId, String markerName, float width, float height) {
        super(RajawaliVuforiaController.RVTrackerType.Marker);
        mMarkerId = markerId;
        mMarkerName = markerName;
        mWidth = width;
        mHeight = height;
    }

    public int getMarkerId() {
        return mMarkerId;
    }

    public String getMarkerName() {
        return mMarkerName;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }
}
