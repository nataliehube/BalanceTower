package de.example.balancetower;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;


public class FullscreenActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnAreaTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 768;
	private static final int CAMERA_HEIGHT = 1280;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private TiledTextureRegion mBoxFaceTextureRegion;
//	private TiledTextureRegion mCircleFaceTextureRegion;
//	private TiledTextureRegion mTriangleFaceTextureRegion;
//	private TiledTextureRegion mHexagonFaceTextureRegion;

//	private int mFaceCount = 0;

	private PhysicsWorld mPhysicsWorld;

	private float mGravityX;
	private float mGravityY;

	private Scene mScene;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

//	public boolean isBodyContacted (final AnimatedSprite face, Body pBody, Contact pContact) {
////		if (face.getUserData().getBody().equals(pBody)
//		if (pContact.getFixtureA().getBody().equals(pBody)||
//				pContact.getFixtureB().getBody().equals(pBody))
//				return false;
//		
//		return true;
//	}
	
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "Balanciere den Turm.", Toast.LENGTH_LONG).show();

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 512, TextureOptions.BILINEAR);
		this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "box.png", 0, 0, 2, 1); // Auflösungen
//		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "circle.png", 0, 64, 2, 1); // 
//		this.mTriangleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "triangle.png", 0, 128, 2, 1); // 
//		this.mHexagonFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "hexagon.png", 0, 192, 2, 1); // 
		this.mBitmapTextureAtlas.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.mScene.setOnAreaTouchListener(this);
		
		
		//Erzeuge die Objekte auf dem Screen
		int numFaces = 6;
		
		for (int i=0; i < numFaces; i++){
			this.addFace(CAMERA_WIDTH/2 -130+(i*10), CAMERA_HEIGHT-130-(i*130));
		}

		

		return this.mScene;

		
	}
	
//	@Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_fullscreen);
//    }
//	
//	public void startChronometer(View view) {
//        ((Chronometer) findViewById(R.id.chronometer1)).start();
//    }
//
//    public void stopChronometer(View view) {
//        ((Chronometer) findViewById(R.id.chronometer1)).stop();
//    }

	@Override
	public boolean onAreaTouched( final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if(pSceneTouchEvent.isActionDown()) {
			final AnimatedSprite face = (AnimatedSprite) pTouchArea;
			this.jumpFace(face);
			return true;
		}

		return false;
	}

//	@Override
//	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
//		if(this.mPhysicsWorld != null) {
//			if(pSceneTouchEvent.isActionDown()) {
//				this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
//				return true;
//			}
//		}
//		return false;
//	}

	@Override
	public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
		this.mGravityX = pAccelerationData.getX();
		this.mGravityY = pAccelerationData.getY();

		final Vector2 gravity = Vector2Pool.obtain(this.mGravityX, this.mGravityY);
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void addFace(final float pX, final float pY) {
//		this.mFaceCount++;

		//final AnimatedSprite face;
		//final Body body;
		
		final AnimatedSprite face;
		final Body body;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

		face = new AnimatedSprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
		body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		
//		if(this.mFaceCount % 3 == 0) {
//			face = new AnimatedSprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
//			body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
//		} else if (this.mFaceCount % 4 == 1) {
//			face = new AnimatedSprite(pX, pY, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
//			body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
//		} else if (this.mFaceCount % 4 == 2) {
//			face = new AnimatedSprite(pX, pY, this.mTriangleFaceTextureRegion, this.getVertexBufferObjectManager());
//			body = FullscreenActivity.createTriangleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
//		} else {
//			face = new AnimatedSprite(pX, pY, this.mHexagonFaceTextureRegion, this.getVertexBufferObjectManager());
//			body = FullscreenActivity.createHexagonBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
//		}
		
		
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
	
		face.animate(new long[]{200,200}, 0, 1, true);
		face.setUserData(body);
		this.mScene.registerTouchArea(face);
		this.mScene.attachChild(face);
	
	}
//	private static Body createTriangleBody(final PhysicsWorld pPhysicsWorld, final IAreaShape pAreaShape, final BodyType pBodyType, final FixtureDef pFixtureDef) {
//		/* Remember that the vertices are relative to the center-coordinates of the Shape. */
//		final float halfWidth = pAreaShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
//		final float halfHeight = pAreaShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
//
//		final float top = -halfHeight;
//		final float bottom = halfHeight;
//		final float left = -halfHeight;
//		final float centerX = 0;
//		final float right = halfWidth;
//
//		final Vector2[] vertices = {
//				new Vector2(centerX, top),
//				new Vector2(right, bottom),
//				new Vector2(left, bottom)
//		};
//
//		return PhysicsFactory.createPolygonBody(pPhysicsWorld, pAreaShape, vertices, pBodyType, pFixtureDef);
//	}
	
//	
//	private static Body createHexagonBody(final PhysicsWorld pPhysicsWorld, final IAreaShape pAreaShape, final BodyType pBodyType, final FixtureDef pFixtureDef) {
//		/* Remember that the vertices are relative to the center-coordinates of the Shape. */
//		final float halfWidth = pAreaShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
//		final float halfHeight = pAreaShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
//
//		/* The top and bottom vertex of the hexagon are on the bottom and top of hexagon-sprite. */
//		final float top = -halfHeight;
//		final float bottom = halfHeight;
//
//		final float centerX = 0;
//
//		/* The left and right vertices of the heaxgon are not on the edge of the hexagon-sprite, so we need to inset them a little. */
//		final float left = -halfWidth + 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
//		final float right = halfWidth - 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
//		final float higher = top + 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;
//		final float lower = bottom - 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;
//
//		final Vector2[] vertices = {
//				new Vector2(centerX, top),
//				new Vector2(right, higher),
//				new Vector2(right, lower),
//				new Vector2(centerX, bottom),
//				new Vector2(left, lower),
//				new Vector2(left, higher)
//		};
//
//		return PhysicsFactory.createPolygonBody(pPhysicsWorld, pAreaShape, vertices, pBodyType, pFixtureDef);
//	}

	// Objekt hüpft
	private void jumpFace(final AnimatedSprite face) {
		final Body faceBody = (Body)face.getUserData();

		final Vector2 velocity = Vector2Pool.obtain(this.mGravityX * -50, this.mGravityY * -50);
		faceBody.setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================


//	public ContactListener contactListener(){
//        ContactListener contactListener = new ContactListener(){
//                @Override
//                public void beginContact(Contact contact)
//                {
//                	Fixture x1 = contact.getFixtureA();
//                    Fixture x2 = contact.getFixtureB();
//                    
//                    if (x2.getBody().getUserData().equals("box1") || x1.getBody().getUserData().equals("box2"))
//                    {                                              
//                            Log.i("CONTACT", "Lost!");
//                    }
//                }
//
//                @Override
//                public void endContact(Contact contact)
//                {
//                }
//
//                @Override
//                public void preSolve(Contact contact, Manifold oldManifold)
//                {
//                }
//
//                @Override
//                public void postSolve(Contact contact, ContactImpulse impulse)
//                {
//                }
//        };
//        return contactListener;
//       
//        
//	}
}