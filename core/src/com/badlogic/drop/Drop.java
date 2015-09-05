package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;

	private OrthographicCamera camera;
	private SpriteBatch batch;

	private Rectangle bucket;
	private Array<Rectangle> raindrops;			// Array is libgdx utility to minimize garbage creation (so garbage collect doesn't have to be run)
	private long lastDropTime; 					// keep track of last time a raindrop was spawned (ns)

	Vector3 touchPos = new Vector3();
	
	@Override
	public void create () {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		// create a camera to make sure target resolution is 800x480 regardless of screen size
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// SpriteBatch is used to draw 2D images
		batch = new SpriteBatch();

		// create a bucket object
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		// create raindrop array
		raindrops = new Array<Rectangle>();
		spawnRaindrop();

	}

	// render is likely run every frame
	@Override
	public void render() {
		// clear the screen with a dark blue color
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);				// set the clear color
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);		// clear the screen

		// update the camera matrix (used for setting up coordinate system for rendering)
		camera.update();

		// render the bucket
		batch.setProjectionMatrix(camera.combined);		// tell the SpriteBatch to use the coordinate system (projection matrix) of the camera
		batch.begin();									// SpriteBatch will record all draw commands between begin() and end()
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();									// submit all drawing requests at once to speed up rendering

		// bucket movement controls
		if(Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		// create new raindrop if necessary
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		// move the raindrops. remove any that are beneath the bottom edge of the screen or that hit the bucket
		Iterator<Rectangle> iter = raindrops.iterator();
		while(iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0) iter.remove();
			// play a sound if drop hits the bucket
			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}

	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800 - 64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	// help out the OS and clean up the mess we created
	// any class that implements the Disposable interface needs to be cleaned up manually
	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}


	// placeholders
	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

}
