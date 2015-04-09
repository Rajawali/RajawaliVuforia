package org.rajawali3d.vuforia;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.qualcomm.QCAR.QCAR;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;
import org.rajawali3d.util.RajLog;

public abstract class RajawaliVuforiaActivity extends Activity implements Vuforia.UpdateCallbackInterface {
    protected enum TrackerType {
        Image, Marker
    }

    private static final int APPSTATUS_UNINITED         = -1;
    private static final int APPSTATUS_INIT_APP         = 0;
    private static final int APPSTATUS_INIT_QCAR        = 1;
    private static final int APPSTATUS_INIT_APP_AR      = 2;
    private static final int APPSTATUS_INIT_TRACKER     = 3;
    private static final int APPSTATUS_INIT_CLOUDRECO 	= 4;
    private static final int APPSTATUS_INITED           = 5;
    private static final int APPSTATUS_CAMERA_STOPPED   = 6;
    private static final int APPSTATUS_CAMERA_RUNNING   = 7;
    private static final int FOCUS_MODE_NORMAL = 0;
    private static final int FOCUS_MODE_CONTINUOUS_AUTO = 1;

 // These codes match the ones defined in TargetFinder.h for Cloud Reco service
    static final int INIT_SUCCESS = 2;
    static final int INIT_ERROR_NO_NETWORK_CONNECTION = -1;
    static final int INIT_ERROR_SERVICE_NOT_AVAILABLE = -2;
    static final int UPDATE_ERROR_AUTHORIZATION_FAILED = -1;
    static final int UPDATE_ERROR_PROJECT_SUSPENDED = -2;
    static final int UPDATE_ERROR_NO_NETWORK_CONNECTION = -3;
    static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4;
    static final int UPDATE_ERROR_BAD_FRAME_QUALITY = -5;
    static final int UPDATE_ERROR_UPDATE_SDK = -6;
    static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
    static final int UPDATE_ERROR_REQUEST_TIMEOUT = -8;
    
    private RajawaliRenderer mRenderer;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mAppStatus = APPSTATUS_UNINITED;
    private int mFocusMode;
    private int mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    private boolean mIsActivityInPortraitMode;
    private boolean mIsExtendedTrackingActivated;
    private InitVuforiaTask mInitQCARTask;
    private final Object mShutdownLock = new Object();
    private boolean mUseCloudRecognition = false;
    //private InitCloudRecoTask mInitCloudRecoTask;
    
	 /** An async task to initialize cloud-based recognition asynchronously.
    private class InitCloudRecoTask extends AsyncTask<Void, Integer, Boolean>
    {
        // Initialize with invalid value
        private int mInitResult = -1;

        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap:
            synchronized (mShutdownLock)
            {
                // Init cloud-based recognition:
                mInitResult = initCloudReco();
                return mInitResult == INIT_SUCCESS;
            }
        }


        protected void onPostExecute(Boolean result)
        {
            RajLog.d("InitCloudRecoTask.onPostExecute: execution "
                    + (result ? "successful" : "failed"));

            if (result)
            {
                // Done loading the tracker, update application status:
                updateApplicationStatus(APPSTATUS_INITED);
             }
            else
            {
                updateApplicationStatus(APPSTATUS_INITED);
               // Create dialog box for display error:
                AlertDialog dialogError = new AlertDialog.Builder(
                        RajawaliVuforiaActivity.this).create();
 
                dialogError.setButton
                (
                    DialogInterface.BUTTON_POSITIVE,
                    "Close",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
  //                          System.exit(1);
                        }
                    }
                );


                String logMessage;

                if (mInitResult == QCAR.INIT_DEVICE_NOT_SUPPORTED)
                {
                    logMessage = "Failed to initialize QCAR because this " +
                        "device is not supported.";
                }
                else
                {
                    logMessage = "Failed to initialize CloudReco.";
                }

                RajLog.e("InitQCARTask::onPostExecute: " + logMessage +
                        " Exiting.");

                dialogError.setMessage(logMessage);
                dialogError.show();
            }
        }
    }
    */
    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
    {
        private int mProgressValue = -1;

        protected Boolean doInBackground(Void... params)
        {
            synchronized (mShutdownLock)
            {
                Vuforia.setInitParameters(RajawaliVuforiaActivity.this, Vuforia.GL_20);

                do
                {
                    mProgressValue = Vuforia.init();
                    publishProgress(mProgressValue);
                } while (!isCancelled() && mProgressValue >= 0
                         && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }

        protected void onProgressUpdate(Integer... values)
        {
        }

        protected void onPostExecute(Boolean result)
        {
            if (result)
            {
                RajLog.d("InitVuforiaTask::onPostExecute: Vuforia initialization successful");

                boolean setupTrackersResult = setupTracker();

                if(setupTrackersResult) {
                    boolean initARResult = initApplicationAR();

                    if(initARResult) {
                        Vuforia.registerCallback(RajawaliVuforiaActivity.this);
                    }
                }
            }
            else
            {
                AlertDialog dialogError = new AlertDialog.Builder(
                    RajawaliVuforiaActivity.this
                ).create();

                dialogError.setButton
                (
                    DialogInterface.BUTTON_POSITIVE,
                    "Close",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            System.exit(1);
                        }
                    }
                );

                String logMessage;

                if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED)
                {
                    logMessage = "Failed to initialize QCAR because this " +
                        "device is not supported.";
                }
                else
                {
                    logMessage = "Failed to initialize QCAR.";
                }

                RajLog.e("InitQCARTask::onPostExecute: " + logMessage +
                                " Exiting.");

                dialogError.setMessage(logMessage);
                dialogError.show();
            }
        }
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		storeScreenDimensions();
	}
	
	protected void startVuforia()
	{
		updateApplicationStatus(APPSTATUS_INIT_APP);
	}

	protected void onResume()
    {
        super.onResume();
        QCAR.onResume();

        if (mAppStatus == APPSTATUS_CAMERA_STOPPED)
        {
            updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
        }
    }
	
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);

        storeScreenDimensions();

//        if (QCAR.isInitialized() && (mAppStatus == APPSTATUS_CAMERA_RUNNING))
//            setProjectionMatrix();
    }
    
    protected void onPause()
    {
        super.onPause();
        
        if (mAppStatus == APPSTATUS_CAMERA_RUNNING)
        {
            updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
        }
        QCAR.onPause();
    }

    protected void onDestroy()
    {
        super.onDestroy();

        if (mInitQCARTask != null &&
            mInitQCARTask.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            mInitQCARTask.cancel(true);
            mInitQCARTask = null;
        }

//        if (mInitCloudRecoTask != null
//                && mInitCloudRecoTask.getStatus() != InitCloudRecoTask.Status.FINISHED)
//        {
//            mInitCloudRecoTask.cancel(true);
//            mInitCloudRecoTask = null;
//        }
        
        synchronized (mShutdownLock) {
        	destroyTrackerData();
            deinitTracker();
//            deinitCloudReco();
            QCAR.deinit();
        }

        System.gc();
    }
	
    private void storeScreenDimensions()
    {
        // Query display dimensions:
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }
    
    private synchronized void updateApplicationStatus(int appStatus)
    {
        if (mAppStatus == appStatus)
            return;

        mAppStatus = appStatus;

        switch (mAppStatus)
        {
            case APPSTATUS_INIT_APP:
                initApplication();
                updateApplicationStatus(APPSTATUS_INIT_QCAR);
                break;

            case APPSTATUS_INIT_QCAR:
                try
                {
                    mInitQCARTask = new InitVuforiaTask();
                    mInitQCARTask.execute();
                }
                catch (Exception e)
                {
                    RajLog.e("Initializing QCAR SDK failed");
                }
                break;

            case APPSTATUS_INIT_TRACKER:
            	setupTracker();                
                break;
                
            case APPSTATUS_INIT_CLOUDRECO:
            	if(mUseCloudRecognition)
            	{
	                try
	                {
//	                    mInitCloudRecoTask = new InitCloudRecoTask();
//	                    mInitCloudRecoTask.execute();
	                }
	                catch (Exception e)
	                {
	                    RajLog.e("Failed to initialize CloudReco");
	                }
            	}
            	else
            	{
            		updateApplicationStatus(APPSTATUS_INITED);
            	}
                break;
                
            case APPSTATUS_INIT_APP_AR:
                initApplicationAR();
                updateApplicationStatus(APPSTATUS_INIT_CLOUDRECO);
                break;

            case APPSTATUS_INITED:
                System.gc();
                updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
                break;

            case APPSTATUS_CAMERA_STOPPED:
                stopCamera();
                break;

            case APPSTATUS_CAMERA_RUNNING:
                startCamera();
//                setProjectionMatrix();
                mFocusMode = FOCUS_MODE_CONTINUOUS_AUTO;
                if(!setFocusMode(mFocusMode))
                {
                    mFocusMode = FOCUS_MODE_NORMAL;
                    setFocusMode(mFocusMode);
                }

                initRajawali();
                break;

            default:
                throw new RuntimeException("Invalid application state");
        }
    }
	
    private void initApplication()
    {
        if((mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
            mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;

        setRequestedOrientation(mScreenOrientation);

        updateActivityOrientation();

        storeScreenDimensions();

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
    }
    
    public void setScreenOrientation(final int screenOrientation)
    {
    	mScreenOrientation = screenOrientation;
    }
    
    public int getScreenOrientation()
    {
    	return mScreenOrientation;
    }
    
    protected abstract boolean setupTracker();

    protected abstract boolean initApplicationAR();

    private void updateActivityOrientation()
    {
        Configuration config = getResources().getConfiguration();

        switch (config.orientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                mIsActivityInPortraitMode = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mIsActivityInPortraitMode = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }
    }

    protected void initRajawali()
    {
        if(mRenderer == null) {
            RajLog.e("initRajawali(): You need so set a renderer first.");
        }

        final RajawaliSurfaceView surface = new RajawaliSurfaceView(this);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);
        surface.setSurfaceRenderer(mRenderer);

        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setRenderer(RajawaliRenderer renderer) {
        mRenderer = renderer;
    }
    
    protected void useCloudRecognition(boolean value)
    {
    	mUseCloudRecognition = value;
    }
  
    protected void setActivityPortraitMode(boolean isPortrait) {
        mIsActivityInPortraitMode = isPortrait;
    }

    public boolean initTracker(TrackerType trackerType) {
        TrackerManager manager = TrackerManager.getInstance();

        if(trackerType == TrackerType.Image) {
            Tracker tracker = manager.initTracker(ImageTracker.getClassType());
            if(tracker == null) {
                RajLog.e("Failed to initialize ImageTracker.");
                return false;
            }

            RajLog.d("Successfully initialized ImageTracker.");
        } else if(trackerType == TrackerType.Marker) {
            Tracker trackerBase = manager.initTracker(MarkerTracker.getClassType());
            MarkerTracker markerTracker = (MarkerTracker) trackerBase;

            if(markerTracker == null) {
                RajLog.e("Failed to initialize MarkerTracker.");
                return false;
            }

            RajLog.d("Successfully initialized MarkerTracker.");
        }

        return true;
    }


    protected boolean createFrameMarker(int markerId, String markerName, float width, float height) {
        TrackerManager manager = TrackerManager.getInstance();
        MarkerTracker tracker = (MarkerTracker) manager.getTracker(MarkerTracker.getClassType());

        if (tracker == null) {
            RajLog.e("Couldn't create FrameMarker " + markerName);
            return false;
        }

        Marker marker = tracker.createFrameMarker(markerId, markerName, new Vec2F(width, height));

        if(marker == null) {
            RajLog.e("Failed to create FrameMarker " + markerName);
            return false;
        }

        return true;
    }

    protected boolean createImageMarker(String dataSetFile) {
        TrackerManager manager = TrackerManager.getInstance();
        ImageTracker tracker = (ImageTracker) manager.getTracker(ImageTracker.getClassType());

        if (tracker == null) {
            RajLog.e("Couldn't create ImageMarker");
            return false;
        }

        DataSet dataSet = tracker.createDataSet();

        if (dataSet == null) {
            RajLog.e("Failed to create a new tracking data.");
            return false;
        }

        if (!dataSet.load(dataSetFile, DataSet.STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            RajLog.e("Failed to load data set " + dataSetFile);
            return false;
        }

        if (!tracker.activateDataSet(dataSet)) {
            RajLog.e("Failed to activate data set.");
            return false;
        }

        RajLog.d("Successfully loaded and activated data set.");

        return true;
    }

    public boolean deinitTracker() {
        TrackerManager trackerManager = TrackerManager.getInstance();
        if (trackerManager.getTracker(MarkerTracker.getClassType()) != null)
            trackerManager.deinitTracker(MarkerTracker.getClassType());
        if (trackerManager.getTracker(ImageTracker.getClassType()) != null)
            trackerManager.deinitTracker(ImageTracker.getClassType());

        return true;
    }

    private boolean destroyTrackerData() {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker)trackerManager.getTracker(ImageTracker.getClassType());
        if (imageTracker == null) {
            return false;
        }

        for (int tIdx = 0; tIdx < imageTracker.getActiveDataSetCount(); tIdx++) {
            DataSet dataSet = imageTracker.getActiveDataSet(tIdx);
            imageTracker.deactivateDataSet(dataSet);
            imageTracker.destroyDataSet(dataSet);
        }

        return true;
    }

    protected boolean startCamera() {
        int camera = CameraDevice.CAMERA.CAMERA_DEFAULT;

        if (!CameraDevice.getInstance().init(camera)) {
            RajLog.e("Unable to open camera device: " + camera);
            return false;
        }

        //configureVideoBackground();

        if (!CameraDevice.getInstance().selectVideoMode(
                CameraDevice.MODE.MODE_DEFAULT))
        {
            RajLog.e("Unable to set video mode.");
            return false;
        }

        if (CameraDevice.getInstance().start()) {
            RajLog.e("Unable to start camera device: " + camera);
            return false;
        }

        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker markerTracker = trackerManager.getTracker(MarkerTracker.getClassType());
        if (markerTracker != null)
            markerTracker.start();

        ImageTracker imageTracker = (ImageTracker) trackerManager.getTracker(ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();

        // Start cloud based recognition if we are in scanning mode:
//        if (scanningMode) {
//            QCAR::TargetFinder* targetFinder = imageTracker->getTargetFinder();
//            assert(targetFinder != 0);
//
//            targetFinder->startRecognition();
//        }
        return true;
    }

    protected void stopCamera() {
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker markerTracker = trackerManager.getTracker(
                MarkerTracker.getClassType());
        if (markerTracker != null)
            markerTracker.stop();

        ImageTracker imageTracker = (ImageTracker)trackerManager.getTracker(ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();

        CameraDevice.getInstance().stop();

        /*
        // Stop Cloud Reco:
        TargetFinder* targetFinder = imageTracker->getTargetFinder();
        assert(targetFinder != 0);

        targetFinder->stop();

        // Clears the trackables
        targetFinder->clearTrackables();
        */

        CameraDevice.getInstance().deinit();

//        initStateVariables();
    }

    protected boolean setFocusMode(int mode) {
        int qcarFocusMode;

        switch ((int) mode) {
            case 0:
                qcarFocusMode = CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL;
                break;

            case 1:
                qcarFocusMode = CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO;
                break;

            case 2:
                qcarFocusMode = CameraDevice.FOCUS_MODE.FOCUS_MODE_INFINITY;
                break;

            case 3:
                qcarFocusMode = CameraDevice.FOCUS_MODE.FOCUS_MODE_MACRO;
                break;

            default:
                return false;
        }

        return CameraDevice.getInstance().setFocusMode(qcarFocusMode);
    }

//    public int initCloudReco();
//    public void setCloudRecoDatabase(String kAccessKey, String kSecretKey);
//    public void deinitCloudReco();
//    public void enterScanningModeNative();
//    public int initCloudRecoTask();
//    public boolean getScanningModeNative();
//    public String getMetadataNative();
    protected boolean extendedTrackingEnabled(boolean enabled) {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager.getTracker(ImageTracker.getClassType());

        DataSet currentDataSet = imageTracker.getActiveDataSet();
        if (imageTracker == null || currentDataSet == null)
            return false;

        for (int tIdx = 0; tIdx < currentDataSet.getNumTrackables(); tIdx++)
        {
            Trackable trackable = currentDataSet.getTrackable(tIdx);
            if(!trackable.stopExtendedTracking()) {
                    return false;
            }
        }

        mIsExtendedTrackingActivated = enabled;

        return true;
    }

    public void onQCARUpdate(State state)
    {

    }
}