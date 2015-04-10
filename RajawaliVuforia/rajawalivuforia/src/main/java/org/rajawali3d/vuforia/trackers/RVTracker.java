package org.rajawali3d.vuforia.trackers;

import org.rajawali3d.vuforia.VuforiaController;

public abstract class RVTracker {
    protected VuforiaController.RVTrackerType mType;

    public RVTracker(VuforiaController.RVTrackerType type) {
        mType = type;
    }

    public VuforiaController.RVTrackerType getType() {
        return mType;
    }
}
