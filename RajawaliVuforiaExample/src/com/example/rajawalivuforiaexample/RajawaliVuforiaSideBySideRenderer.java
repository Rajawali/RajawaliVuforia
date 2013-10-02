package com.example.rajawalivuforiaexample;

import rajawali.materials.Material;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import rajawali.vuforia.RajawaliVuforiaRenderer;
import android.content.Context;

public class RajawaliVuforiaSideBySideRenderer extends RajawaliVuforiaRenderer {
	private int mViewportWidthHalf;
	private RenderTarget mLeftRenderTarget;
	private RenderTarget mRightRenderTarget;
	private RajawaliScene mUserScene;
	private RajawaliScene mSideBySideScene;
	private ScreenQuad mLeftQuad;
	private ScreenQuad mRightQuad;
	private Material mLeftQuadMaterial;
	private Material mRightQuadMaterial;
	private double mPupilDistance = .06;
	
	public RajawaliVuforiaSideBySideRenderer(Context context)
	{
		super(context);
	}
	
	public RajawaliVuforiaSideBySideRenderer(Context context, double pupilDistance)
	{
		this(context);
		mPupilDistance = pupilDistance;
	}


	@Override
	protected void foundFrameMarker(int markerId, Vector3 position,
			Quaternion orientation) {
	}

	@Override
	protected void foundImageMarker(String trackableName, Vector3 position,
			Quaternion orientation) {
	}

	@Override
	public void noFrameMarkersFound() {
	}
	
	@Override
	public void initScene() {
		getCurrentCamera().setFieldOfView(getFOV());
		
		setPupilDistance(mPupilDistance);

		mLeftQuadMaterial = new Material();
		mRightQuadMaterial = new Material();

		mSideBySideScene = new RajawaliScene(this);

		mLeftQuad = new ScreenQuad();
		mLeftQuad.setScaleX(.5);
		mLeftQuad.setX(-.25);
		mLeftQuad.setMaterial(mLeftQuadMaterial);
		mSideBySideScene.addChild(mLeftQuad);

		mRightQuad = new ScreenQuad();
		mRightQuad.setScaleX(.5);
		mRightQuad.setX(.25);
		mRightQuad.setMaterial(mRightQuadMaterial);
		mSideBySideScene.addChild(mRightQuad);

		addScene(mSideBySideScene);

		mViewportWidthHalf = (int) (mViewportWidth * .5f);

		mLeftRenderTarget = new RenderTarget(mViewportWidthHalf, mViewportHeight);
		mRightRenderTarget = new RenderTarget(mViewportWidthHalf, mViewportHeight);

		addRenderTarget(mLeftRenderTarget);
		addRenderTarget(mRightRenderTarget);

		try {
			mLeftQuadMaterial.addTexture(mLeftRenderTarget.getTexture());
			mRightQuadMaterial.addTexture(mRightRenderTarget.getTexture());
		} catch (TextureException e) {
			e.printStackTrace();
		}
		
		mUserScene = getCurrentScene();
	}
	
	@Override
	protected void onRender(final double deltaTime) {
		/*
		RajLog.i("____ RENDER SUPER");
		//
		// -- Renders the camera view into a texture
		//
		renderFrame(mBackgroundRenderTarget.getFrameBufferHandle(), mBackgroundRenderTarget.getTexture().getTextureId());
		RajLog.i("____ RENDER TARGET LEFT");
		//
		// -- Render the left eye texture
		//
		setRenderTarget(mLeftRenderTarget);
		// -- Set the quad size to half the video width
		setViewPort((int)(getVideoWidth() * .5), getVideoHeight());
		// -- Scale the quad so we don't get a squashed image
		mBackgroundQuad.setScaleX(2);
		// -- Shift the camera to the left
		getCurrentCamera().setX(-mPupilDistance * .5f);
		// -- render the left eye scene
		render(deltaTime);
		RajLog.i("____ RENDER TARGET RIGHT");
		//
		// -- Render the right eye texture
		//
		setRenderTarget(mRightRenderTarget);
		// -- Shift the camera to the right
		getCurrentCamera().setX(mPupilDistance * .5f);
		// -- render the right eye scene
		render(deltaTime);
		
		// -- reset the camera position
		getCurrentCamera().setX(0);
		// -- reset quad scale
		mBackgroundQuad.setScaleX(1);
		RajLog.i("____ RENDER TARGET SCREEN");
		//
		// -- Render the left and right eye screen quads
		//
		setRenderTarget(null);
		setViewPort(mViewportWidth, mViewportHeight);
		switchSceneDirect(mSideBySideScene);
		
		render(deltaTime);
		
		switchScene(mUserScene);
		*/
	}

	public void setPupilDistance(double pupilDistance)
	{
		mPupilDistance = pupilDistance;
	}

	public double getPupilDistance()
	{
		return mPupilDistance;
	}
}
