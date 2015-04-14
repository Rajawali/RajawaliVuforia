package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;

import org.rajawali3d.util.RajLog;
import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class StartTrackersTask implements IRajawaliVuforiaTask {
    private RajawaliVuforiaController mController;

    @Override
    public void execute(RajawaliVuforiaController controller) {
        mController = controller;

        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
                .getTracker(MarkerTracker.getClassType());
        if (markerTracker != null) {
            boolean result = markerTracker.start();
            if(!result) {
                mController.taskFail(this, "Couldn't start MarkerTracker");
                return;
            }
        }

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ImageTracker.getClassType());
        if (imageTracker != null) {
            boolean result = imageTracker.start();
            if(!result) {
                mController.taskFail(this, "Couldn't start ImageTracker.");
                return;
            }
        }

        RajLog.d("Successfully started trackers.");

        mController.taskComplete(this);
    }

    public void cancel() {}
}
