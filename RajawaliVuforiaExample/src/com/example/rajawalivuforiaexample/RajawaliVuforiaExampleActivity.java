package com.example.rajawalivuforiaexample;

import rajawali.util.RajLog;
import rajawali.vuforia.RajawaliVuforiaActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class RajawaliVuforiaExampleActivity extends RajawaliVuforiaActivity {
	private RajawaliVuforiaExampleRenderer mRenderer;
	private RajawaliVuforiaActivity mUILayout;
	private Button mStartScanButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		useCloudRecognition(true);
		setCloudRecoDatabase("a75960aa97c3b72a76eb997f9e40d210d5e40bf2",
				"aac883379f691a2550e80767ccd445ffbaa520ca");
		
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);
		
		ImageView logoView = new ImageView(this);
		logoView.setImageResource(R.drawable.rajawali_vuforia);
		ll.addView(logoView);
		
		addContentView(ll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
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
	protected void initApplicationAR() {
		super.initApplicationAR();

		createFrameMarker(1, "Marker1", 50, 50);
		createFrameMarker(2, "Marker2", 50, 50);

		createImageMarker("StonesAndChips.xml");

		// -- this is how you add a cylinder target:
		// https://developer.vuforia.com/resources/dev-guide/cylinder-targets
		// createImageMarker("MyCylinderTarget.xml");

		// -- this is how you add a multi-target:
		// https://developer.vuforia.com/resources/dev-guide/multi-targets
		// createImageMarker("MyMultiTarget.xml");
	}

	public void showStartScanButton() {
		this.runOnUiThread(new Runnable() {
			public void run() {
				if (mStartScanButton != null)
					mStartScanButton.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	protected void initRajawali() {
		super.initRajawali();
		mRenderer = new RajawaliVuforiaExampleRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
		super.setRenderer(mRenderer);

		// Add button for Cloud Reco:
		mStartScanButton = new Button(this);
		mStartScanButton.setText("Start Scanning CloudReco");
		mStartScanButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enterScanningModeNative();
				mStartScanButton.setVisibility(View.GONE);
			}
		});

		ToggleButton extendedTrackingButton = new ToggleButton(this);
		extendedTrackingButton.setTextOn("Extended Tracking On");
		extendedTrackingButton.setTextOff("Extended Tracking Off");
		extendedTrackingButton.setChecked(false);
		extendedTrackingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					if(!startExtendedTracking())
						RajLog.e("Could not start extended tracking");
				} else {
					if(!stopExtendedTracking())
						RajLog.e("Could not stop extended tracking");
				}
			}
		});

		mUILayout = this;
		LinearLayout ll = new LinearLayout(this);
		ll.addView(mStartScanButton);
		ll.addView(extendedTrackingButton);
		mUILayout.addContentView(ll, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
}
