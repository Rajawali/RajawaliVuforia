package org.rajawali3d.vuforia.tasks;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.WindowManager;

import org.rajawali3d.vuforia.IVuforiaActivity;
import org.rajawali3d.vuforia.VuforiaController;

public class InitActivityTask implements IRajawaliVuforiaTask {
    public void execute(VuforiaController controller) {
        IVuforiaActivity vuforiaActivity = controller.getVuforiaActivity();
        Activity activity = controller.getVuforiaActivity().getActivity();
        int screenOrientation = vuforiaActivity.getRequiredScreenOrientation();

        if((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)) {
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        }

        activity.setRequestedOrientation(screenOrientation);

        controller.updateActivityOrientation();
        controller.storeScreenDimensions();

        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        controller.taskComplete(this);
    }
}
