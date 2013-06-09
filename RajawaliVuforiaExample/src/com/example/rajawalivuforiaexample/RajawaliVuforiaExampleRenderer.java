package com.example.rajawalivuforiaexample;

import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.SerializedObject3D;
import rajawali.animation.mesh.SkeletalAnimationObject3D;
import rajawali.animation.mesh.SkeletalAnimationSequence;
import rajawali.lights.DirectionalLight;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.PhongMaterial;
import rajawali.materials.textures.Texture;
import rajawali.math.Quaternion;
import rajawali.math.Vector3;
import rajawali.parser.md5.MD5AnimParser;
import rajawali.parser.md5.MD5MeshParser;
import rajawali.vuforia.RajawaliVuforiaRenderer;
import android.content.Context;

public class RajawaliVuforiaExampleRenderer extends RajawaliVuforiaRenderer {
	private DirectionalLight mLight;
	private SkeletalAnimationObject3D mBob;
	private BaseObject3D mF22;
	private BaseObject3D mAndroid;

	public RajawaliVuforiaExampleRenderer(Context context) {
		super(context);
	}

	protected void initScene() {
		mLight = new DirectionalLight(.1f, 0, -1.0f);
		mLight.setColor(1.0f, 1.0f, 0.8f);
		mLight.setPower(1);

		try {
			//
			// -- Load Bob (model by Katsbits
			// http://www.katsbits.com/download/models/)
			//

			MD5MeshParser meshParser = new MD5MeshParser(this,
					R.raw.boblampclean_mesh);
			meshParser.parse();
			mBob = (SkeletalAnimationObject3D) meshParser
					.getParsedAnimationObject();
			mBob.setScale(2);
			mBob.addLight(mLight);

			MD5AnimParser animParser = new MD5AnimParser("dance", this,
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

			GZIPInputStream gzi = new GZIPInputStream(mContext.getResources()
					.openRawResource(R.raw.f22));
			ObjectInputStream fis = new ObjectInputStream(gzi);
			SerializedObject3D serializedObj = (SerializedObject3D) fis
					.readObject();
			fis.close();

			mF22 = new BaseObject3D(serializedObj);
			mF22.setScale(30);
			mF22.addLight(mLight);
			addChild(mF22);
			
			DiffuseMaterial f22Material = new DiffuseMaterial();
			f22Material.addTexture(new Texture(R.drawable.f22));
			
			mF22.setMaterial(f22Material);
			
			//
			// -- Load Android
			//
			
			gzi = new GZIPInputStream(mContext.getResources()
					.openRawResource(R.raw.android));
			fis = new ObjectInputStream(gzi);
			serializedObj = (SerializedObject3D) fis
					.readObject();
			fis.close();
			
			mAndroid = new BaseObject3D(serializedObj);
			mAndroid.setScale(14);
			mAndroid.addLight(mLight);
			addChild(mAndroid);
			
			PhongMaterial androidMaterial = new PhongMaterial();
			androidMaterial.setUseSingleColor(true);
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
		if(trackableName.equals("stones"))
		{
			mF22.setVisible(true);
			mF22.setPosition(position);
			mF22.setOrientation(orientation);
		}
	}

	@Override
	public void noFrameMarkersFound() {
	}

	public void onDrawFrame(GL10 glUnused) {
		mBob.setVisible(false);
		mF22.setVisible(false);
		mAndroid.setVisible(false);

		super.onDrawFrame(glUnused);
	}
}
