package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.TrackerManager;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class UnloadTrackersDataTask implements IRajawaliVuforiaTask {
    @Override
    public void execute(RajawaliVuforiaController controller) {
        TrackerManager manager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) manager.getTracker(
                ImageTracker.getClassType()
        );

        if(imageTracker == null)
            return;

        imageTracker.deactivateDataSet(imageTracker.getActiveDataSet());

        controller.taskComplete(this);
    }

    @Override
    public void cancel() {

    }
}
