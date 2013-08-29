package rajawali.vuforia;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;

import com.qualcomm.QCAR.QCAR;

public abstract class RajawaliVuforiaRenderer extends RajawaliRenderer {
	private Vector3 mPosition;
	private Quaternion mOrientation;
	private double[] mModelViewMatrix;
	private int mI = 0;
	
	public native void initRendering();
	public native void updateRendering(int width, int height);
	public native void renderFrame();
	public native float getFOV();
	public native int getVideoWidth();
	public native int getVideoHeight();

	public RajawaliVuforiaRenderer(Context context) {
		super(context);
		mPosition = new Vector3();
		mOrientation = new Quaternion();
		getCurrentScene().alwaysClearColorBuffer(false);
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
	}

	public void foundFrameMarker(int markerId, float[] modelViewMatrix) {
		mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
				-modelViewMatrix[14]);
		copyFloatToDoubleMatrix(modelViewMatrix, mModelViewMatrix);		
		mOrientation.fromRotationMatrix(mModelViewMatrix);
		mOrientation.y = -mOrientation.y;
		mOrientation.z = -mOrientation.z;
		
		foundFrameMarker(markerId, mPosition, mOrientation);
	}
	
	public void foundImageMarker(String trackableName, float[] modelViewMatrix) {
		mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
				-modelViewMatrix[14]);
		copyFloatToDoubleMatrix(modelViewMatrix, mModelViewMatrix);		
		mOrientation.fromRotationMatrix(mModelViewMatrix);
		mOrientation.y = -mOrientation.y;
		mOrientation.z = -mOrientation.z;
		
		foundImageMarker(trackableName, mPosition, mOrientation);
	}
	
	abstract protected void foundFrameMarker(int markerId, Vector3 position, Quaternion orientation);
	abstract protected void foundImageMarker(String trackableName, Vector3 position, Quaternion orientation);
	abstract public void noFrameMarkersFound();

	public void onDrawFrame(GL10 glUnused) {
		renderFrame();
		super.onDrawFrame(glUnused);
	}
	
	private void copyFloatToDoubleMatrix(float[] src, double[] dst)
	{
		for(mI = 0; mI < 16; mI++)
		{
			dst[mI] = src[mI];
		}
	}
}
