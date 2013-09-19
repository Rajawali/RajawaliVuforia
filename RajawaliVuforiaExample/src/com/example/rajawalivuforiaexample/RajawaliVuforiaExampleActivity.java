package com.example.rajawalivuforiaexample;

import rajawali.util.RajLog;
import rajawali.vuforia.RajawaliVuforiaActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class RajawaliVuforiaExampleActivity extends RajawaliVuforiaActivity {
	private RajawaliVuforiaExampleRenderer mRenderer;
	private RajawaliVuforiaActivity mUILayout;
    private Button mStartScanButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCloudRecoDatabase("a75960aa97c3b72a76eb997f9e40d210d5e40bf2", "aac883379f691a2550e80767ccd445ffbaa520ca");
		startVuforia();
	}

	@Override
	protected void setupTracker() {
		int result = initTracker(TRACKER_TYPE_MARKER);
		if (result == 1) {
			result = initTracker(TRACKER_TYPE_IMAGE);
			if (result == 1) {
				super.setupTracker();
			} else {
				RajLog.e("Couldn't initialize image tracker.");
			}
		} else {
			RajLog.e("Couldn't initialize marker tracker.");
		}
	}
	
	@Override
	protected void initApplicationAR()
	{
		super.initApplicationAR();
		
		createFrameMarker(1, "Marker1", 50, 50);
		createFrameMarker(2, "Marker2", 50, 50);
		
		createImageMarker("StonesAndChips.xml");
		
		mRenderer = new RajawaliVuforiaExampleRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
		super.setRenderer(mRenderer);
		
	    //Add button for Cloud Reco:
        mStartScanButton = new Button(this);
        mStartScanButton.setText("Start Scanning CloudReco");
         
        mStartScanButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    enterScanningModeNative();
                     mStartScanButton.setVisibility(View.GONE);
                 }
        });
        
        mUILayout = this;
        mUILayout.addContentView(mStartScanButton, 
            new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
     
	}
	
    public void showStartScanButton()
    {
        this.runOnUiThread(new Runnable() {
                public void run() {
                    if  (mStartScanButton != null)
                        mStartScanButton.setVisibility(View.VISIBLE);
                 }
         });
    }    
}
