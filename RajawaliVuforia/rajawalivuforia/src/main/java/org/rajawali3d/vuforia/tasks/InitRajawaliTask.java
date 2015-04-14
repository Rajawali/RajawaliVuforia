package org.rajawali3d.vuforia.tasks;

import org.rajawali3d.vuforia.IRajawaliVuforiaControllerListener;
import org.rajawali3d.vuforia.RajawaliVuforiaController;

public class InitRajawaliTask implements IRajawaliVuforiaTask {
    @Override
    public void execute(RajawaliVuforiaController controller) {
        IRajawaliVuforiaControllerListener vuforiaActivity = controller.getListener();
        vuforiaActivity.initRajawali();
        controller.taskComplete(this);
    }

    public void cancel() {}
}
