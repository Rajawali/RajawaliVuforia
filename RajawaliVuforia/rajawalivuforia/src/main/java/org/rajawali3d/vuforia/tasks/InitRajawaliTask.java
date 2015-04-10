package org.rajawali3d.vuforia.tasks;

import org.rajawali3d.vuforia.IVuforiaActivity;
import org.rajawali3d.vuforia.VuforiaController;

public class InitRajawaliTask implements IRajawaliVuforiaTask {
    @Override
    public void execute(VuforiaController controller) {
        IVuforiaActivity vuforiaActivity = controller.getVuforiaActivity();
        vuforiaActivity.initRajawali();
        controller.taskComplete(this);
    }
}
