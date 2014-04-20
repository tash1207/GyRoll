package co.tashawych.gyroll;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * Based off of Nicolas Gramlich's AndEngine Physics Example
 */
public class GyRollGame extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	protected static final int CAMERA_WIDTH = 720;
	protected static final int CAMERA_HEIGHT = 480;

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

	// ===========================================================
	// Fields
	// ===========================================================

	// Sphere
	protected Sprite sphere;
	private BitmapTextureAtlas mBitmapTextureAtlasSphere;
	private ITextureRegion mSphereTextureRegion;
	
	// Turret
	private BitmapTextureAtlas mBitmapTextureAtlasTurret;
	private ITextureRegion mTurretTextureRegion;
	
	private Sprite turret;
	private boolean turretMovingLeft = true;
	private float turretX = CAMERA_WIDTH/2;
	private float turretY = CAMERA_HEIGHT - 110;
	
	Laser laser;
	protected static boolean laserDone = false;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;
	private boolean sphereVisible = false;
	
	CountDownTimer timer;
	protected static int gameLevel = 1;
	Text textTime;
	
	protected boolean gameOver = false;
	
    // Handler for callbacks to the UI thread
    final Handler mHandler = new Handler();

    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateResultsInUi();
        }
    };
    
    final Runnable mFinishGame = new Runnable() {
        public void run() {
            finishGame();
        }
    };
	

	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "Touch the screen to start the game.", Toast.LENGTH_SHORT).show();

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		// Turret
		this.mBitmapTextureAtlasTurret = new BitmapTextureAtlas(this.getTextureManager(), 123, 180, TextureOptions.BILINEAR);
		this.mTurretTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasTurret, this, "Turret.png", 0, 0);
		this.mBitmapTextureAtlasTurret.load();

		// Sphere
		this.mBitmapTextureAtlasSphere = new BitmapTextureAtlas(this.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
		this.mSphereTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasSphere, this, "sphere3.png", 0, 0);
		this.mBitmapTextureAtlasSphere.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		this.mScene.setOnSceneTouchListener(this);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		// Walls
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle roof = new Rectangle(0, CAMERA_HEIGHT - 50, CAMERA_WIDTH * 2, 1, vertexBufferObjectManager);
		final Rectangle ground = new Rectangle(0, 0, CAMERA_WIDTH * 2, 1, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 1, CAMERA_HEIGHT * 2 - 100, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 1, 0, 1, CAMERA_HEIGHT * 2 - 100, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		BitmapTextureAtlas fontTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, 
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		fontTextureAtlas.load();
		Font font = new Font(getFontManager(), fontTextureAtlas,Typeface.DEFAULT,45f,true,Color.WHITE);
		getEngine().getFontManager().loadFont(font);
		
		Text textScore = new Text(100, 450, font, "Score: ", vertexBufferObjectManager);
		this.mScene.attachChild(textScore);
		textTime = new Text(200, 450, font, "00", vertexBufferObjectManager);
		this.mScene.attachChild(textTime);
		
		// Turret
		turret = new Sprite(turretX, turretY, this.mTurretTextureRegion, vertexBufferObjectManager);
		turret.setScale(0.7f);
		this.mScene.attachChild(turret);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		return this.mScene;
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		// Start the game
		if(this.mPhysicsWorld != null && !sphereVisible) {
			if(pSceneTouchEvent.isActionDown()) {
				mHandler.post(mUpdateResults);
				this.placeSphere(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());

				BitmapTextureAtlas mBitmapTextureAtlasLaser = new BitmapTextureAtlas(
						this.getTextureManager(), 74, 290, TextureOptions.BILINEAR);
				ITextureRegion mLaserTextureRegion = BitmapTextureAtlasTextureRegionFactory
						.createFromAsset(mBitmapTextureAtlasLaser, this, "greenLaserRay.png", 0, 0);
				mBitmapTextureAtlasLaser.load();

				laser = new Laser(turretX, turretY - 85, mLaserTextureRegion, this.getVertexBufferObjectManager());
				laser.setScale(0.2f);
				mScene.attachChild(laser);
				
				return true;
			}
		}
		return false;
	}

	@Override
	public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(2f*pAccelerationData.getX(), 0);
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
	
	public void updateResultsInUi() {
		gameLevel = 0;
		// Start a timer that increases the "game level" every second
		timer = new CountDownTimer(60000, 1000) {
			public void onTick(long millisUntilFinished) {
				gameLevel++;
				textTime.setText(String.valueOf((60000 - millisUntilFinished)/1000 + 1));
			}
			
			public void onFinish() {
				textTime.setText("60");
				mHandler.post(mFinishGame);
			}
		};
		timer.start();
		
		this.mScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() { }

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				if (gameOver) return;
				
				if (sphere.collidesWith(laser)) {
					gameOver = true;
					mHandler.post(mFinishGame);
					return;
				}
				if (laserDone) {
					mScene.detachChild(laser);
					BitmapTextureAtlas mBitmapTextureAtlasLaser = new BitmapTextureAtlas(getTextureManager(), 74, 290, TextureOptions.BILINEAR);
					ITextureRegion mLaserTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBitmapTextureAtlasLaser, GyRollGame.this, "greenLaserRay.png", 0, 0);
					mBitmapTextureAtlasLaser.load();

					laser = new Laser(turretX, turretY - 85, mLaserTextureRegion, getVertexBufferObjectManager());
					laser.setScale(0.18f);
					mScene.attachChild(laser);

					laserDone = false;
				}
				float newTurretX = turretX;
				// Turret moves left
				if (turretMovingLeft && turretX > 42) {
					newTurretX = turretX - 2;
				}
				// If turret is all the way left, move right
				else if (turretX <= 42)  {
					turretMovingLeft = false;
					newTurretX = turretX + 2;
				}
				// Turret moves right
				else if (!turretMovingLeft && turretX <= CAMERA_WIDTH - 48) {
					newTurretX = turretX + 2;
				}
				// If turret is all the way right, move left
				else if (turretX > CAMERA_WIDTH - 48) {
					turretMovingLeft = true;
					newTurretX = turretX - 2;
				}
				// Motion for turret
				turret.registerEntityModifier(new MoveXModifier(0.03f - 0.0015f*(gameLevel/4), turretX, newTurretX) {
					@Override
			    	protected void onModifierStarted(IEntity pItem) {
			        	super.onModifierStarted(pItem);
					}

			        @Override
			        protected void onModifierFinished(IEntity pItem) {
			        	turretX = turret.getX();
			            super.onModifierFinished(pItem);
			        }
				});
			}
		});
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void placeSphere(final float pX, final float pY) {
		this.sphereVisible = true;

		final Body body;

		sphere = new Sprite(CAMERA_WIDTH/2, 0, this.mSphereTextureRegion, this.getVertexBufferObjectManager());
		sphere.setScale(0.2f);
		sphere.setUserData("sphere");
		body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, sphere, BodyType.DynamicBody, FIXTURE_DEF);

		this.mScene.attachChild(sphere);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sphere, body, true, true));
	}
	
	protected void finishGame() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (gameOver) {
			timer.cancel();
			builder.setMessage("GAME OVER");
		}
		else {
			builder.setMessage("You win! You lasted 60 seconds in the arena");
			gameOver = true;
		}
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        finish();
                }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
	}

}