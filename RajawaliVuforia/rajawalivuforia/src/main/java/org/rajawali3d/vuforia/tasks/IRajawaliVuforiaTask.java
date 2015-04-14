package org.rajawali3d.vuforia.tasks;

import org.rajawali3d.vuforia.RajawaliVuforiaController;

/**
 * Created by dennis on 10/04/15.
 */
public interface IRajawaliVuforiaTask {
    public void execute(RajawaliVuforiaController controller);
    public void cancel();
}
