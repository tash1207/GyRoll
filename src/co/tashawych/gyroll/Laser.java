package co.tashawych.gyroll;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Laser extends Sprite {
	private final PhysicsHandler mPhysicsHandler;
	private final float VELOCITY = 100.0f;
	
	public Laser(final float x, final float y, final ITextureRegion textureRegion, 
			VertexBufferObjectManager manager) {
        super(x, y, textureRegion, manager);
        this.mPhysicsHandler = new PhysicsHandler(this);
        this.registerUpdateHandler(this.mPhysicsHandler);
        this.setUserData("laser");
	}
	
    @Override
    protected void onManagedUpdate(final float pSecondsElapsed) {    	
		if (this.mY < 0) {
			GyRollGame.laserDone = true;
			this.detachSelf();
		} else {
			this.mPhysicsHandler.setVelocityY(-VELOCITY);
		}

        super.onManagedUpdate(pSecondsElapsed);
    }

}
