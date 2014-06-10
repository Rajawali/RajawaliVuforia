package rajawali.vuforia;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.materials.Material;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.qualcomm.QCAR.QCAR;

public abstract class RajawaliVuforiaRenderer extends RajawaliRenderer {
	private Vector3 mPosition;
	private Quaternion mOrientation;
	protected ScreenQuad mBackgroundQuad;
	protected RenderTarget mBackgroundRenderTarget;
	private double[] mModelViewMatrix;
	private int mI = 0;
	private RajawaliVuforiaActivity mActivity;
	
	public native void initRendering();
	public native void updateRendering(int width, int height);
	public native void renderFrame(int frameBufferId, int frameBufferTextureId);
	public native void drawVideoBackground();
	public native float getFOV();
	public native int getVideoWidth();
	public native int getVideoHeight();

	public RajawaliVuforiaRenderer(Context context) {
		super(context);
		mActivity = (RajawaliVuforiaActivity)context;
		mPosition = new Vector3();
		mOrientation = new Quaternion();
		getCurrentCamera().setNearPlane(10);
		getCurrentCamera().setFarPlane(2500);
		mModelViewMatrix = new double[16];
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);
		initRendering();
		QCAR.onSurfaceCreated();
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);
		updateRendering(width, height);
		QCAR.onSurfaceChanged(width, height);
		getCurrentCamera().setProjectionMatrix(getFOV(), getVideoWidth(),
				getVideoHeight());
		if(mBackgroundRenderTarget == null) {
			mBackgroundRenderTarget = new RenderTarget("rajVuforia", width, height);
			
			addRenderTarget(mBackgroundRenderTarget);
			Material material = new Material();
			material.setColorInfluence(0);
			try {
				material.addTexture(mBackgroundRenderTarget.getTexture());
			} catch (TextureException e) {
				e.printStackTrace();
			}

			mBackgroundQuad = new ScreenQuad();
			if(mActivity.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				mBackgroundQuad.setScaleY((float)height / (float)getVideoHeight());
			else
				mBackgroundQuad.setScaleX((float)width / (float)getVideoWidth());
			mBackgroundQuad.setMaterial(material);
			getCurrentScene().addChildAt(mBackgroundQuad, 0);
		}
	}

	public void foundFrameMarker(int markerId, float[] modelViewMatrix) {
		synchronized (this) {
			transformPositionAndOrientation(modelViewMatrix);
			
			foundFrameMarker(markerId, mPosition, mOrientation);
		}
	}
	
	private void transformPositionAndOrientation(float[] modelViewMatrix) {
		mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
				-modelViewMatrix[14]);
		copyFloatToDoubleMatrix(modelViewMatrix, mModelViewMatrix);		
		mOrientation.fromRotationMatrix(mModelViewMatrix);
		
		if(mActivity.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
		{
			mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
					-modelViewMatrix[14]);
			mOrientation.y = -mOrientation.y;
			mOrientation.z = -mOrientation.z;
		}
		else
		{
			mPosition.setAll(-modelViewMatrix[13], -modelViewMatrix[12],
					-modelViewMatrix[14]);
			double orX = mOrientation.x;
			mOrientation.x = -mOrientation.y;
			mOrientation.y = -orX;
			mOrientation.z = -mOrientation.z;
		}
	}
	
	public void foundImageMarker(String trackableName, float[] modelViewMatrix) {
		synchronized (this) {
			transformPositionAndOrientation(modelViewMatrix);
			foundImageMarker(trackableName, mPosition, mOrientation);
		}
	}
	
	abstract protected void foundFrameMarker(int markerId, Vector3 position, Quaternion orientation);
	abstract protected void foundImageMarker(String trackableName, Vector3 position, Quaternion orientation);
	abstract public void noFrameMarkersFound();

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
	}

	@Override
	protected void onRender(final double deltaTime) {
		renderFrame(mBackgroundRenderTarget.getFrameBufferHandle(), mBackgroundRenderTarget.getTexture().getTextureId());
		super.onRender(deltaTime);
	}

	private void copyFloatToDoubleMatrix(float[] src, double[] dst)
	{
		for(mI = 0; mI < 16; mI++)
		{
			dst[mI] = src[mI];
		}
	}
}
