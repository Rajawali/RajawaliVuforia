package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.TrackerManager;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class DeinitTrackersTask implements IRajawaliVuforiaTask {
    @Override
    public void execute(RajawaliVuforiaController controller) {
        TrackerManager manager = TrackerManager.getInstance();
        manager.deinitTracker(ImageTracker.getClassType());
        manager.deinitTracker(MarkerTracker.getClassType());

        controller.taskComplete(this);
    }

    @Override
    public void cancel() {

    }
}
