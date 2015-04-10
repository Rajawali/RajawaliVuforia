package org.rajawali3d.vuforia.trackers;

import org.rajawali3d.vuforia.VuforiaController;

public class RVImageTracker extends RVTracker {
    private String mDataSetFilePath;

    public RVImageTracker(String dataSetFilePath) {
        super(VuforiaController.RVTrackerType.Image);
        mDataSetFilePath = dataSetFilePath;
    }

    public String getDataSetFilePath() {
        return mDataSetFilePath;
    }
}
