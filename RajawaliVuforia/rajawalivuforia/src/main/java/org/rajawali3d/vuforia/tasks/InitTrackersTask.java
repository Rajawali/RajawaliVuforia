package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;

import org.rajawali3d.util.RajLog;
import org.rajawali3d.vuforia.IVuforiaActivity;
import org.rajawali3d.vuforia.VuforiaController;

public class InitTrackersTask implements IRajawaliVuforiaTask {
    @Override
    public void execute(VuforiaController controller) {
        TrackerManager manager = TrackerManager.getInstance();
        IVuforiaActivity vuforiaActivity = controller.getVuforiaActivity();

        VuforiaController.RVTrackerType[] trackerTypes = vuforiaActivity.getRequiredTrackerTypes();

        if(trackerTypes == null || trackerTypes.length == 0) {
            controller.taskFail(this, "No tracker types specified.");
        }

        for(VuforiaController.RVTrackerType trackerType : trackerTypes) {
            if(trackerType == VuforiaController.RVTrackerType.Image) {
                Tracker tracker = manager.initTracker(ImageTracker.getClassType());
                if(tracker == null) {
                    controller.taskFail(this, "Failed to initialize ImageTracker.");
                    return;
                }

                RajLog.d("Successfully initialized ImageTracker.");
            } else if(trackerType == VuforiaController.RVTrackerType.Marker) {
                Tracker trackerBase = manager.initTracker(MarkerTracker.getClassType());
                MarkerTracker markerTracker = (MarkerTracker) trackerBase;

                if(markerTracker == null) {
                    controller.taskFail(this, "Failed to initialize MarkerTracker.");
                    return;
                }

                RajLog.d("Successfully initialized MarkerTracker.");
            }
        }

        controller.taskComplete(this);
    }
}
