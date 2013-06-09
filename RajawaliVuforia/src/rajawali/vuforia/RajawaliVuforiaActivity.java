package rajawali.vuforia;

import rajawali.RajawaliActivity;
import rajawali.util.RajLog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.qualcomm.QCAR.QCAR;

public class RajawaliVuforiaActivity extends RajawaliActivity {
	protected static int TRACKER_TYPE_IMAGE = 0;
	protected static int TRACKER_TYPE_MARKER = 1;
		
    private static final int APPSTATUS_UNINITED         = -1;
    private static final int APPSTATUS_INIT_APP         = 0;
    private static final int APPSTATUS_INIT_QCAR        = 1;
    private static final int APPSTATUS_INIT_APP_AR      = 2;
    private static final int APPSTATUS_INIT_TRACKER     = 3;
    private static final int APPSTATUS_INITED           = 4;
    private static final int APPSTATUS_CAMERA_STOPPED   = 5;
    private static final int APPSTATUS_CAMERA_RUNNING   = 6;
    private static final int FOCUS_MODE_NORMAL = 0;
    private static final int FOCUS_MODE_CONTINUOUS_AUTO = 1;

	private static final String NATIVE_LIB_QCAR = "QCAR";
	private static final String NATIVE_LIB_RAJAWALI_VUFORIA = "RajawaliVuforia";
	
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mAppStatus = APPSTATUS_UNINITED;
    private int mFocusMode;
    private InitQCARTask mInitQCARTask;
    private Object mShutdownLock = new Object();
    
	static
	{
		loadLibrary(NATIVE_LIB_QCAR);
		loadLibrary(NATIVE_LIB_RAJAWALI_VUFORIA);
	}
	
    /** An async task to initialize QCAR asynchronously. */
    private class InitQCARTask extends AsyncTask<Void, Integer, Boolean>
    {
        private int mProgressValue = -1;

        protected Boolean doInBackground(Void... params)
        {
            synchronized (mShutdownLock)
            {
                QCAR.setInitParameters(RajawaliVuforiaActivity.this, QCAR.GL_20);

                do
                {
                    mProgressValue = QCAR.init();
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
                RajLog.d("InitQCARTask::onPostExecute: QCAR " +
                              "initialization successful");

                updateApplicationStatus(APPSTATUS_INIT_TRACKER);
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
		mDeferGLSurfaceViewCreation = true;
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
    };
	
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);

        storeScreenDimensions();

        if (QCAR.isInitialized() && (mAppStatus == APPSTATUS_CAMERA_RUNNING))
            setProjectionMatrix();
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
            mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED)
        {
            mInitQCARTask.cancel(true);
            mInitQCARTask = null;
        }

        synchronized (mShutdownLock) {
        	destroyTrackerData();
            deinitApplicationNative();
            deinitTracker();
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
                    mInitQCARTask = new InitQCARTask();
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

            case APPSTATUS_INIT_APP_AR:
                initApplicationAR();
                updateApplicationStatus(APPSTATUS_INITED);
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
                setProjectionMatrix();
                mFocusMode = FOCUS_MODE_CONTINUOUS_AUTO;
                if(!setFocusMode(mFocusMode))
                {
                    mFocusMode = FOCUS_MODE_NORMAL;
                    setFocusMode(mFocusMode);
                }

                break;

            default:
                throw new RuntimeException("Invalid application state");
        }
    }
	
    private void initApplication()
    {
        int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setRequestedOrientation(screenOrientation);
        setActivityPortraitMode(
            screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        storeScreenDimensions();

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    protected void setupTracker()
    {
    	updateApplicationStatus(APPSTATUS_INIT_APP_AR);
    }
    
    protected void initApplicationAR()
    {
        initApplicationNative(mScreenWidth, mScreenHeight);

        createSurfaceView();
    }
    
    protected native void initApplicationNative(int width, int height);
    protected native void setActivityPortraitMode(boolean isPortrait);
    protected native void deinitApplicationNative();
    public native int initTracker(int trackerType);
    protected native int createFrameMarker(int markerId, String markerName, float width, float height);
    protected native int createImageMarker(String dataSetFile);
    public native void deinitTracker();
    private native int destroyTrackerData();
    protected native void startCamera();
    protected native void stopCamera();
    protected native void setProjectionMatrix();
    protected native boolean autofocus();
    protected native boolean setFocusMode(int mode);

    /** A helper for loading native libraries stored in "libs/armeabi*". */
    public static boolean loadLibrary(String nLibName)
    {
        try
        {
            System.loadLibrary(nLibName);
            RajLog.i("Native library lib" + nLibName + ".so loaded");
            return true;
        }
        catch (UnsatisfiedLinkError ulee)
        {
            RajLog.e("The library lib" + nLibName +
                            ".so could not be loaded");
        }
        catch (SecurityException se)
        {
            RajLog.e("The library lib" + nLibName +
                            ".so was not allowed to be loaded");
        }

        return false;
    }
}
