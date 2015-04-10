package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.vuforia.IVuforiaActivity;
import org.rajawali3d.vuforia.VuforiaController;
import org.rajawali3d.vuforia.trackers.RVImageTracker;
import org.rajawali3d.vuforia.trackers.RVMarkerTracker;
import org.rajawali3d.vuforia.trackers.RVTracker;

public class LoadTrackersDataTask implements IRajawaliVuforiaTask {
    private VuforiaController mController;

    @Override
    public void execute(VuforiaController controller) {
        mController = controller;
        IVuforiaActivity vuforiaActivity = controller.getVuforiaActivity();
        RVTracker[] trackers = vuforiaActivity.getRequiredTrackers();

        for(RVTracker tracker : trackers) {
            switch(tracker.getType()) {
                case Image:
                    RVImageTracker imageTracker = (RVImageTracker) tracker;
                    createImageTracker(imageTracker);
                    break;
                case Marker:
                    RVMarkerTracker markerTracker = (RVMarkerTracker) tracker;
                    createMarkerTracker(markerTracker);
                    break;
            }
        }

        Vuforia.registerCallback(controller);

        mController.taskComplete(this);
    }

    protected void createMarkerTracker(RVMarkerTracker t) {
        TrackerManager manager = TrackerManager.getInstance();
        MarkerTracker tracker = (MarkerTracker) manager.getTracker(MarkerTracker.getClassType());

        if (tracker == null) {
            mController.taskFail(this, "Couldn't create FrameMarker " + t.getMarkerName());
            return;
        }

        Marker marker = tracker.createFrameMarker(t.getMarkerId(), t.getMarkerName(), new Vec2F(t.getWidth(), t.getHeight()));

        if(marker == null) {
            mController.taskFail(this, "Failed to create FrameMarker " + t.getMarkerName());
        }
    }

    protected void createImageTracker(RVImageTracker t) {
        TrackerManager manager = TrackerManager.getInstance();
        ImageTracker tracker = (ImageTracker) manager.getTracker(ImageTracker.getClassType());

        if (tracker == null) {
            mController.taskFail(this, "Couldn't create ImageMarker");
            return;
        }

        DataSet dataSet = tracker.createDataSet();

        if (dataSet == null) {
            mController.taskFail(this, "Failed to create a new tracking data.");
            return;
        }

        if (!dataSet.load(t.getDataSetFilePath(), DataSet.STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            mController.taskFail(this, "Failed to load data set " + t.getDataSetFilePath());
            return;
        }

        if (!tracker.activateDataSet(dataSet)) {
            mController.taskFail(this, "Failed to activate data set.");
        }
    }
}
