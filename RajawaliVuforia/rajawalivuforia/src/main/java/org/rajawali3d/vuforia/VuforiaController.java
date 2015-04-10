package org.rajawali3d.vuforia;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.util.RajLog;
import org.rajawali3d.vuforia.tasks.IRajawaliVuforiaTask;
import org.rajawali3d.vuforia.tasks.InitActivityTask;
import org.rajawali3d.vuforia.tasks.InitTrackersTask;
import org.rajawali3d.vuforia.tasks.InitVuforiaTask;
import org.rajawali3d.vuforia.tasks.LoadTrackersDataTask;

import java.util.ArrayList;
import java.util.List;

public class VuforiaController implements Vuforia.UpdateCallbackInterface {
    public static enum RVTrackerType {
        Image, Marker
    }

    private IVuforiaActivity mActivity;
    private boolean mIsPortrait;
    private int mScreenWidth;
    private int mScreenHeight;
    private Object mShutdownLock = new Object();
    private List<IRajawaliVuforiaTask> mTasks;

    public VuforiaController(IVuforiaActivity activity) {
        mActivity = activity;

        initialize();
    }

    private void initialize() {
        mTasks = new ArrayList<>();
        mTasks.add(new InitActivityTask());
        mTasks.add(new InitVuforiaTask());
        mTasks.add(new InitTrackersTask());
        mTasks.add(new LoadTrackersDataTask());
        nextTask();
    }

    public void nextTask() {
        if(mTasks.size() > 0) {
            IRajawaliVuforiaTask task = mTasks.remove(0);
            task.execute(this);
        }
    }

    public void taskComplete(IRajawaliVuforiaTask task) {
        RajLog.d("Task complete: " + task);
        nextTask();
    }

    public void taskFail(IRajawaliVuforiaTask task, String message) {
        RajLog.e(message);
    }

    public void updateActivityOrientation()
    {
        Configuration config = mActivity.getActivity().getResources().getConfiguration();

        switch(config.orientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                mIsPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mIsPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }
    }

    public void storeScreenDimensions()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    @Override
    public void QCAR_onUpdate(State state) {

    }

    public Object getShutdownLock() {
        return mShutdownLock;
    }

    public IVuforiaActivity getVuforiaActivity() {
        return mActivity;
    }

    public Activity getActivity() {
        return mActivity.getActivity();
    }

    public int getVuforiaFlags() {
        return Vuforia.GL_20;
    }


}
