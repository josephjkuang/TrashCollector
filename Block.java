import java.awt.image.BufferedImage;
import java.awt.Image;
import java.lang.Math;

public class Block {
	private int score;
	private Image image;
	private Image wetImage;
	private Image cleanImage;
	private boolean moveable;
	private boolean grabeable;
	private int spawnTime;
	private boolean clean;
	private boolean dry;
	private Dispose disposeType;
	private int orientation;

	public Block() {
		score = 0;
		image = null;
		moveable = false;
		spawnTime = 0;
		grabeable = false;
		clean = true;
		dry = true;
		disposeType = Dispose.NONE;
		orientation = 2;
		wetImage = null;
		cleanImage = null;
	}

	public Block(Image image, boolean moveable) {
		this.score = 0;
		this.image = image;
		this.moveable = moveable;
		this.spawnTime = 0;
		this.grabeable = false;
		this.clean = true;
		this.dry = true;
		this.disposeType = Dispose.NONE;
		this.orientation = 2;
		this.wetImage = null;
		this.cleanImage = null;
	}

	public Block(Image image, int spawnTime, boolean clean, Dispose disposeType, Image wetImage, Image cleanImage) {
		this.score = 100;
		this.image = image;
		this.moveable = false;
		this.spawnTime = spawnTime;
		this.grabeable = true;
		this.clean = clean;
		this.dry = true;
		this.disposeType = disposeType;
		this.orientation = 2;
		this.wetImage = wetImage;
		this.cleanImage = cleanImage;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	public void decay(int time) {
		int bufferTime = time + 5; // Give users a 5 seconds to pick up garbage
		if (bufferTime < spawnTime) {
			this.score = Math.max(100 - 10 * (spawnTime - bufferTime), 10);
		}
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}

	public String toString() {
		if (score < 0) {
			return "" + score;
		}
		return "+" + score;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	public boolean getMoveable() {
		return moveable;
	}

	public void setSpawnTime(int spawnTime) {
		this.spawnTime = spawnTime;
	}

	public int getSpawnTime() {
		return spawnTime;
	}

	public void setGreable(boolean grabeable) {
		this.grabeable = grabeable;
	}

	public boolean getGrabeable() {
		return grabeable;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	} 

	public boolean getClean() {
		return clean;
	}

	public void setDry(boolean dry) {
		this.dry = dry;
	}

	public boolean getDry() {
		return dry;
	}

	public void setDisposeType(Dispose disposeType) {
		this.disposeType = disposeType;
	}

	public Dispose getDisposeType() {
		return disposeType;
	}

	public void setOrientation(int orientation) {
		if (orientation == -1) {
			return;
		}
		this.orientation = orientation;
	}

	public int getOrientation() {
		return orientation;
	}

	public Image getWetImage() {
		return wetImage;
	}

	public Image getCleanImage() {
		return cleanImage;
	}
}