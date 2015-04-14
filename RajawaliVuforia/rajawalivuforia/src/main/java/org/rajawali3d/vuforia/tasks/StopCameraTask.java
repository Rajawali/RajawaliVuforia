package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.CameraDevice;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class StopCameraTask implements IRajawaliVuforiaTask {
    @Override
    public void execute(RajawaliVuforiaController controller) {
        CameraDevice.getInstance().stop();
        CameraDevice.getInstance().deinit();
        controller.isCameraRunning(false);

        controller.taskComplete(this);
    }

    public void cancel() {}
}
