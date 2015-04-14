package org.rajawali3d.vuforia;

import android.graphics.Color;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.util.RajLog;

public class RajawaliVuforiaRendererController {
    private IRajawaliVuforiaRendererControllerListener mListener;
    private RajawaliVuforiaController mController;

    private Vector3 mPosition;
    private Quaternion mOrientation;
    protected ScreenQuad mBackgroundQuad;
    protected RenderTarget mBackgroundRenderTarget;
    private double[] mModelViewMatrix;
    private RajawaliRenderer mRenderer;

    public RajawaliVuforiaRendererController(RajawaliVuforiaController controller) {
        mController = controller;
        initialize();
    }

    private void initialize() {
        mPosition = new Vector3();
        mOrientation = new Quaternion();
        mModelViewMatrix = new double[16];
    }

    public void setRenderer(RajawaliRenderer renderer) {
        mRenderer = renderer;
    }

    public void setRendererControllerListener(IRajawaliVuforiaRendererControllerListener listener) {
        mListener = listener;
    }

    public void onSurfaceChanged(int width, int height) {
        Vuforia.onSurfaceChanged(width, height);

//        mRenderer.getCurrentCamera().setProjectionMatrix(getFOV(), mController.getVideoWidth(),
//                mController.getVideoHeight());
        if(mBackgroundRenderTarget == null) {
            mBackgroundRenderTarget = new RenderTarget("rajVuforia", width, height);

            mRenderer.addRenderTarget(mBackgroundRenderTarget);
            Material material = new Material();
            material.setColor(Color.YELLOW);
            material.setColorInfluence(1);
            try {
                material.addTexture(mBackgroundRenderTarget.getTexture());
                Renderer.getInstance().setVideoBackgroundTextureID(mBackgroundRenderTarget.getTexture().getTextureId());
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            mBackgroundQuad = new ScreenQuad();
            if(!mController.isPortrait())
                mBackgroundQuad.setScaleY((float)height / (float)mController.getVideoHeight());
            else
                mBackgroundQuad.setScaleX((float)width / (float)mController.getVideoWidth());
            mBackgroundQuad.setMaterial(material);
            mBackgroundQuad.setColor(Color.BLUE);
            mBackgroundQuad.setDoubleSided(true);
            mRenderer.getCurrentScene().addChildAt(mBackgroundQuad, 0);
        }
    }

    public void onSurfaceCreated() {
        Vuforia.onSurfaceCreated();
    }

    public void initScene() {

    }

    public void onRender(final long elapsedRealtime, final double deltaTime) {
        //Renderer.getInstance().drawVideoBackground();
//        Renderer.getInstance().setVideoBackgroundTextureID(mBackgroundRenderTarget.getTexture().getTextureId());
//        State state = Renderer.getInstance().begin();
//        Renderer.getInstance().bindVideoBackground(0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBackgroundRenderTarget.getTexture().getTextureId());
//        Renderer.getInstance().drawVideoBackground();
//        Renderer.getInstance().end();
        Renderer.getInstance().begin();
        Renderer.getInstance().bindVideoBackground(0);
        Renderer.getInstance().setVideoBackgroundTextureID(mBackgroundRenderTarget.getTexture().getTextureId());
        Renderer.getInstance().drawVideoBackground();
        Renderer.getInstance().end();
    }


    protected float getFOV() {
        CameraCalibration cameraCalibration =
                CameraDevice.getInstance().getCameraCalibration();
        Vec2F size = cameraCalibration.getSize();
        Vec2F focalLength = cameraCalibration.getFocalLength();
        float fovRadians = 2 * (float)Math.atan(0.5f * size.getData()[0] / focalLength.getData()[1]);
        return fovRadians * 180.0f / (float)Math.PI;
    }
}
