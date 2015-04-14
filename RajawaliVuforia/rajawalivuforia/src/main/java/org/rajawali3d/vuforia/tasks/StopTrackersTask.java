package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class StopTrackersTask implements IRajawaliVuforiaTask {
    @Override
    public void execute(RajawaliVuforiaController controller) {
        TrackerManager manager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) manager.getTracker(
                MarkerTracker.getClassType()
        );
        if(markerTracker != null) {
            markerTracker.stop();
        }

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ImageTracker.getClassType()
        );
        if(imageTracker != null) {
            imageTracker.stop();
        }

        controller.taskComplete(this);
    }

    public void cancel() {}
}
