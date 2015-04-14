package org.rajawali3d.vuforia;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.util.RajLog;
import org.rajawali3d.vuforia.tasks.DeinitTrackersTask;
import org.rajawali3d.vuforia.tasks.IRajawaliVuforiaTask;
import org.rajawali3d.vuforia.tasks.InitARTask;
import org.rajawali3d.vuforia.tasks.InitActivityTask;
import org.rajawali3d.vuforia.tasks.InitTrackersTask;
import org.rajawali3d.vuforia.tasks.InitVuforiaTask;
import org.rajawali3d.vuforia.tasks.LoadTrackersDataTask;
import org.rajawali3d.vuforia.tasks.StartTrackersTask;
import org.rajawali3d.vuforia.tasks.StopCameraTask;
import org.rajawali3d.vuforia.tasks.StopTrackersTask;
import org.rajawali3d.vuforia.tasks.UnloadTrackersDataTask;

import java.util.ArrayList;
import java.util.List;

public class RajawaliVuforiaController implements Vuforia.UpdateCallbackInterface {
    public static enum RVTrackerType {
        Image, Marker
    }

    private IRajawaliVuforiaControllerListener mListener;
    private RajawaliVuforiaRendererController mRendererController;
    private boolean mIsPortrait;
    private boolean mStarted;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mVideoWidth;
    private int mVideoHeight;

    private boolean mIsCamerarunning;
    private Object mShutdownLock = new Object();
    private List<IRajawaliVuforiaTask> mTasks;

    public RajawaliVuforiaController(IRajawaliVuforiaControllerListener listener) {
        mListener = listener;

        initialize();
    }

    private void initialize() {
        mRendererController = new RajawaliVuforiaRendererController(this);

        mTasks = new ArrayList<>();
        mTasks.add(new InitActivityTask());
        mTasks.add(new InitVuforiaTask());
        mTasks.add(new InitTrackersTask());
        mTasks.add(new LoadTrackersDataTask());
        mTasks.add(new StartTrackersTask());
        mTasks.add(new InitARTask());
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
        Configuration config = mListener.getActivity().getResources().getConfiguration();

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
        mListener.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    public void configureVideoBackground() {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode videoMode = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize, ySize;
        int screenHeight = getScreenHeight();
        int screenWidth = getScreenWidth();

        if(isPortrait()) {
            xSize = (int) (videoMode.getHeight() * (screenHeight / (float) videoMode.getWidth()));
            ySize = screenHeight;

            if (xSize < screenWidth)
            {
                xSize = screenWidth;
                ySize = (int) (screenWidth * (videoMode.getWidth() / (float) videoMode.getHeight()));
            }
        } else
        {
            xSize = screenWidth;
            ySize = (int) (videoMode.getHeight() * (screenWidth / (float) videoMode.getWidth()));

            if (ySize < screenHeight)
            {
                xSize = (int) (screenHeight * (videoMode.getWidth() / (float) videoMode.getHeight()));
                ySize = screenHeight;
            }
        }

        config.setSize(new Vec2I(xSize, ySize));
        mVideoWidth = xSize;
        mVideoHeight = ySize;

        RajLog.d("Configure Video Background : Video (" + videoMode.getWidth()
                + " , " + videoMode.getHeight() + "), Screen (" + screenWidth + " , "
                + screenHeight + "), mSize (" + xSize + " , " + ySize + ")");

        Renderer.getInstance().setVideoBackgroundConfig(config);
    }

    @Override
    public void QCAR_onUpdate(State state) {

    }

    public Object getShutdownLock() {
        return mShutdownLock;
    }

    public IRajawaliVuforiaControllerListener getListener() {
        return mListener;
    }

    public Activity getActivity() {
        return mListener.getActivity();
    }

    public void onPause() {
        Vuforia.onPause();
RajLog.d("Controller onPause " + mStarted);
        if(mStarted) {
            new StopTrackersTask().execute(this);
            new StopCameraTask().execute(this);
        }
    }

    public void onResume() {
        Vuforia.onResume();
RajLog.d("Controller onResume " + mStarted);
        if(mStarted) {
            new InitARTask().execute(this);
        }
    }

    public void onDestroy() {
        RajLog.d("Controller onDestroy");
        if(mTasks != null) {
            for(IRajawaliVuforiaTask task : mTasks) {
                task.cancel();
            }
            mTasks.clear();
        }

        new StopCameraTask().execute(this);
        new UnloadTrackersDataTask().execute(this);
        new DeinitTrackersTask().execute(this);

        Vuforia.deinit();
    }

    public void onConfigurationChanged() {
        updateActivityOrientation();
        storeScreenDimensions();

        if(hasStarted()) {
            configureVideoBackground();
        }
    }

    public int getVuforiaFlags() {
        return Vuforia.GL_20;
    }

    public boolean isCameraRunning() {
        return mIsCamerarunning;
    }

    public void isCameraRunning(boolean isRunning) {
        mIsCamerarunning = isRunning;
    }

    public boolean isPortrait() {
        return mIsPortrait;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public boolean hasStarted() {
        return mStarted;
    }

    public void hasStarted(boolean started) {
        mStarted = started;
    }

    public List<IRajawaliVuforiaTask> getTasks() {
        return mTasks;
    }

    public RajawaliVuforiaRendererController getRendererController() {
        return mRendererController;
    }
}
