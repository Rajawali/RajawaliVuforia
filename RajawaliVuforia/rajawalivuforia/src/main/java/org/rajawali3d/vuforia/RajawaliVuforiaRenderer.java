package org.rajawali3d.vuforia;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.util.RajLog;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.opengl.GLES20;

import com.qualcomm.QCAR.QCAR;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.CylinderTarget;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.MultiTarget;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;

public abstract class RajawaliVuforiaRenderer extends RajawaliRenderer {
	private Vector3 mPosition;
	private Quaternion mOrientation;
	protected ScreenQuad mBackgroundQuad;
	protected RenderTarget mBackgroundRenderTarget;
	private double[] mModelViewMatrix;
	private int mI = 0;
    private int mVideoWidth;
    private int mVideoHeight;
	private RajawaliVuforiaActivity mActivity;
	
	public void updateRendering(int width, int height) {
        configureVideoBackground(width, height);
    }

    private void configureVideoBackground(int mScreenWidth, int mScreenHeight)
    {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize, ySize;
        if (mScreenHeight > mScreenWidth)
        {
            xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm.getWidth()));
            ySize = mScreenHeight;

            if (xSize < mScreenWidth)
            {
                xSize = mScreenWidth;
                ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm.getHeight()));
            }
        } else
        {
            xSize = mScreenWidth;
            ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm.getWidth()));

            if (ySize < mScreenHeight)
            {
                xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm.getHeight()));
                ySize = mScreenHeight;
            }
        }

        config.setSize(new Vec2I(xSize, ySize));

        mVideoWidth = config.getSize().getData()[0];
        mVideoHeight = config.getSize().getData()[1];

        RajLog.i("Configure Video Background : Video (" + vm.getWidth()
                + " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , "
                + mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");

        Renderer.getInstance().setVideoBackgroundConfig(config);

    }

    public void renderFrame(int frameBufferId, int frameBufferTextureId) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = Renderer.getInstance().begin();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                frameBufferTextureId, 0);

        Renderer.getInstance().drawVideoBackground();

        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose());

            /*
            if (mIsActivityInPortraitMode)
                Utils::rotatePoseMatrix(90.0f, 0, 1.0f, 0,
            &modelViewMatrix.data[0]);
            Utils::rotatePoseMatrix(-90.0f, 1.0f, 0, 0, &modelViewMatrix.data[0]);
            */

            if (trackable.isOfType(Marker.getClassType())) {
                foundFrameMarker(trackable.getId(), modelViewMatrix.getData());

            } else if (trackable.isOfType(CylinderTarget.getClassType())
                || trackable.isOfType(ImageTarget.getClassType())
                || trackable.isOfType(MultiTarget.getClassType())) {
                foundImageMarker(trackable.getName(), modelViewMatrix.getData());
            }
        }

        if (state.getNumTrackableResults() == 0) {
            noFrameMarkersFound();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Renderer.getInstance().end();
    }

	public float getFOV() {
        CameraCalibration cameraCalibration =
                CameraDevice.getInstance().getCameraCalibration();
        Vec2F size = cameraCalibration.getSize();
        Vec2F focalLength = cameraCalibration.getFocalLength();
        float fovRadians = 2 * (float)Math.atan(0.5f * size.getData()[0] / focalLength.getData()[1]);
        return fovRadians * 180.0f / (float)Math.PI;
    }

	public RajawaliVuforiaRenderer(Context context) {
		super(context);
		mActivity = (RajawaliVuforiaActivity)context;
		mPosition = new Vector3();
		mOrientation = new Quaternion();
		getCurrentCamera().setNearPlane(10);
		getCurrentCamera().setFarPlane(2500);
		mModelViewMatrix = new double[16];
	}
	
	public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        RajLog.i("onRenderSurfaceCreated");
		super.onRenderSurfaceCreated(config, gl, width, height);
		Vuforia.onSurfaceCreated();
	}

	public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        RajLog.i("onRenderSurfaceSizeChanged " + width + ", " + height);
		super.onRenderSurfaceSizeChanged(gl, width, height);
		updateRendering(width, height);
		QCAR.onSurfaceChanged(width, height);
		getCurrentCamera().setProjectionMatrix(getFOV(), mVideoWidth,
				mVideoHeight);
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
				mBackgroundQuad.setScaleY((float)height / (float)mVideoHeight);
			else
				mBackgroundQuad.setScaleX((float)width / (float)mVideoWidth);
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

	@Override
	protected void onRender(final long elapsedRealtime, final double deltaTime) {
		renderFrame(mBackgroundRenderTarget.getFrameBufferHandle(), mBackgroundRenderTarget.getTexture().getTextureId());
		super.onRender(elapsedRealtime, deltaTime);
	}

	private void copyFloatToDoubleMatrix(float[] src, double[] dst)
	{
		for(mI = 0; mI < 16; mI++)
		{
			dst[mI] = src[mI];
		}
	}
}
