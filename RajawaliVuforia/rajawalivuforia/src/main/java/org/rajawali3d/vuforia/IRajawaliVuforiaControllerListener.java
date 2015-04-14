package org.rajawali3d.vuforia;

import android.app.Activity;
import android.support.annotation.IntegerRes;

import org.rajawali3d.vuforia.trackers.RVTracker;

public interface IRajawaliVuforiaControllerListener {
    public Activity getActivity();
    public void onInitVuforiaProgress(int progress);
    public void onFail(String message);
    public RajawaliVuforiaController.RVTrackerType[] getRequiredTrackerTypes();
    @IntegerRes
    public int getRequiredScreenOrientation();
    public RVTracker[] getRequiredTrackers();
    public void initRajawali();
    public int getPreferredCameraDevice();
}
