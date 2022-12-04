import java.awt.image.BufferedImage;
import java.awt.Image;

public class Block {
	private int score;
	private Image image;
	private boolean moveable;
	private boolean grabeable;
	private int spawnTime;
	private boolean clean;
	private boolean dry;
	private Dispose disposeType;
	private int orientation;

	enum Dispose {
		TRASH,
		RECYCLE,
		COMPOST,
		NONE
	}

	public Block() {
		score = 0;
		image = null;
		moveable = false;
		spawnTime = 0;
		grabeable = false;
		clean = true;
		dry = true;
		disposeType = Dispose.NONE;
		this.orientation = 2;
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
	}

	public Block(Image image, int spawnTime, boolean clean, Dispose disposeType) {
		this.score = 100;
		this.image = image;
		this.moveable = false;
		this.spawnTime = spawnTime;
		this.grabeable = true;
		this.clean = clean;
		this.dry = true;
		this.disposeType = disposeType;
		this.orientation = 2;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
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

	public Dispose getDisposeType(Dispose disposeType) {
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
}