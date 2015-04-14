package org.rajawali3d.vuforia.trackers;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public abstract class RVTracker {
    protected RajawaliVuforiaController.RVTrackerType mType;

    public RVTracker(RajawaliVuforiaController.RVTrackerType type) {
        mType = type;
    }

    public RajawaliVuforiaController.RVTrackerType getType() {
        return mType;
    }
}
