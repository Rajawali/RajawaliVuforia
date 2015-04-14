package org.rajawali3d.vuforia.tasks;

import com.qualcomm.vuforia.CameraDevice;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class InitARTask implements IRajawaliVuforiaTask {
    private RajawaliVuforiaController mController;

    @Override
    public void execute(RajawaliVuforiaController controller) {
        mController = controller;

        if(mController.isCameraRunning()) {
            mController.taskFail(this, "Camera is already running.");
            return;
        }

        int cameraId = mController.getListener().getPreferredCameraDevice();

        if(!CameraDevice.getInstance().init(cameraId)) {
            mController.taskFail(this, "Unable to open camera device: " + cameraId);
            return;
        }

        controller.configureVideoBackground();

        if(!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT)) {
            mController.taskFail(this, "Unable to set video mode.");
            return;
        }

        if(!CameraDevice.getInstance().start()) {
            mController.taskFail(this, "Unable to start camera device: " + cameraId);
            return;
        }

        mController.isCameraRunning(true);

        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {
            CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }

        mController.taskComplete(this);
    }

    public void cancel() {}
}
