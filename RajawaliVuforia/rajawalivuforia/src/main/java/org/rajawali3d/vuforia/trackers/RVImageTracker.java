package org.rajawali3d.vuforia.trackers;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class RVImageTracker extends RVTracker {
    private String mDataSetFilePath;
    private boolean mEnableExtendedTracking;

    public RVImageTracker(String dataSetFilePath) {
        this(dataSetFilePath, false);
    }

    public RVImageTracker(String dataSetFilePath, boolean enableExtendedTracking) {
        super(RajawaliVuforiaController.RVTrackerType.Image);
        mDataSetFilePath = dataSetFilePath;
        mEnableExtendedTracking = enableExtendedTracking;
    }

    public String getDataSetFilePath() {
        return mDataSetFilePath;
    }

    public boolean enabledExtendedTracking() {
        return mEnableExtendedTracking;
    }
}
