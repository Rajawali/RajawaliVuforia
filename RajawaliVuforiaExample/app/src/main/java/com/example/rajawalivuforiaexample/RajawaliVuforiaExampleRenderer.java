package com.example.rajawalivuforiaexample;

import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import javax.microedition.khronos.opengles.GL10;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.vuforia.RajawaliVuforiaRenderer;
import android.content.Context;
import android.view.MotionEvent;

public class RajawaliVuforiaExampleRenderer extends RajawaliVuforiaRenderer {
	private DirectionalLight mLight;
	private SkeletalAnimationObject3D mBob;
	private Object3D mF22;
	private Object3D mAndroid;
	private RajawaliVuforiaExampleActivity activity;

    public RajawaliVuforiaExampleRenderer(Context context) {
		super(context);
		activity = (RajawaliVuforiaExampleActivity)context;
        //RajLog.setDebugEnabled(false);
	}

	protected void initScene() {
		mLight = new DirectionalLight(.1f, 0, -1.0f);
		mLight.setColor(1.0f, 1.0f, 0.8f);
		mLight.setPower(1);
		
		getCurrentScene().addLight(mLight);
		
		try {
			//
			// -- Load Bob (model by Katsbits
			// http://www.katsbits.com/download/models/)
			//

			LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this,
					R.raw.boblampclean_mesh);
			meshParser.parse();
			mBob = (SkeletalAnimationObject3D) meshParser
					.getParsedAnimationObject();
			mBob.setScale(2);

			LoaderMD5Anim animParser = new LoaderMD5Anim("dance", this,
					R.raw.boblampclean_anim);
			animParser.parse();
			mBob.setAnimationSequence((SkeletalAnimationSequence) animParser
					.getParsedAnimationSequence());

			getCurrentScene().addChild(mBob);

			mBob.play();
			mBob.setVisible(false);

			//
			// -- Load F22 (model by KuhnIndustries
			// http://www.blendswap.com/blends/view/67634)
			//

            final LoaderAWD parserF22 = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
            parserF22.parse();

			mF22 = parserF22.getParsedObject();
			mF22.setScale(30);
			getCurrentScene().addChild(mF22);
			
			Material f22Material = new Material();
			f22Material.enableLighting(true);
			f22Material.setDiffuseMethod(new DiffuseMethod.Lambert());
			f22Material.addTexture(new Texture("f22Texture", R.drawable.f22));
			f22Material.setColorInfluence(0);
			
			mF22.setMaterial(f22Material);
			
			//
			// -- Load Android
			//

            final LoaderAWD parserAndroid = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
            parserAndroid.parse();

            mAndroid = parserAndroid.getParsedObject();
			mAndroid.setScale(14);
			getCurrentScene().addChild(mAndroid);
			
			Material androidMaterial = new Material();
			androidMaterial.enableLighting(true);
			androidMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
			androidMaterial.setSpecularMethod(new SpecularMethod.Phong());
			mAndroid.setColor(0x00dd00);
			mAndroid.setMaterial(androidMaterial);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void foundFrameMarker(int markerId, Vector3 position,
			Quaternion orientation) {
		
		if (markerId == 0) {
			mBob.setVisible(true);
			mBob.setPosition(position);
			mBob.setOrientation(orientation);
		} else if (markerId == 1) {
			mAndroid.setVisible(true);
			mAndroid.setPosition(position);
			mAndroid.setOrientation(orientation);
		}
	}

	@Override
	protected void foundImageMarker(String trackableName, Vector3 position,
			Quaternion orientation) {
		if(trackableName.equals("SamsungGalaxyS4"))
		{
			mBob.setVisible(true);
			mBob.setPosition(position);
			mBob.setOrientation(orientation);
			RajLog.d(activity.getMetadataNative());
		}
		if(trackableName.equals("stones"))
		{
			mF22.setVisible(true);
			mF22.setPosition(position);
			mF22.setOrientation(orientation);
		}
		// -- also handle cylinder targets here
		// -- also handle multi-targets here
	}

	@Override
	public void noFrameMarkersFound() {
	}


    @Override
	public void onRenderFrame(GL10 glUnused) {
		mBob.setVisible(false);
		mF22.setVisible(false);
		mAndroid.setVisible(false);
		
		super.onRenderFrame(glUnused);
		
		if (!activity.getScanningModeNative())
		{
			activity.showStartScanButton();
		}
	}

    @Override
    public void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {

    }

    @Override
    public void onTouchEvent(MotionEvent motionEvent) {

    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        RajLog.i("boo onRenderSurfaceSizeChanged " + this.hashCode());
        super.onRenderSurfaceSizeChanged(gl, width, height);
    }
}
