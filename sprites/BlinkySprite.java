import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/*
 * This class is an example of how to create an animation by alternating the image returned by the getImage() accessor
 * The sprite has a total of eight images - four directions x 2 frames (in the animation sense). The images are loaded
 * in the constructor, and the correct image is then calculated based on the sprite's movement and time elapsed
 */
public class BlinkySprite implements DisplayableSprite {

	protected static final double VELOCITY = 200;
	private static final int WIDTH = 50;
	private static final int HEIGHT = 50;
	private static final int PERIOD_LENGTH = 200;
	private static final int IMAGES_IN_CYCLE = 2;

	/*
	 * images are stored in an array... this usually corresponds to the numbering of the images
	 * from the source (e.g. a sprite sheet, or the frames in an animated GIF)
	 */
	private static Image[] images;
	/* 
	 * an alternate way of storing the images. if the sprite is simple and only has
	 * a few images, you may want to create explicit pointers to each image
	 */
	private static Image left0;
	private static Image right0;
	private static Image up0;
	private static Image down0;
	private static Image left1;
	private static Image right1;
	private static Image up1;
	private static Image down1;
	
	private long elapsedTime = 0;
		
	private double centerX = 0;
	private double centerY = 0;
	private double width = 50;
	private double height = 50;
	private boolean dispose = false;

	private Direction direction = Direction.RIGHT;
	
	public BlinkySprite(double centerX, double centerY) {

		this.centerX =centerX;
		this.centerY = centerY;	
		this.width = WIDTH;
		this.height = HEIGHT;
		
		if (images == null) {
			try {
				images = new Image[8];
				for (int i = 0; i < 8; i++) {
					String path = String.format("res/blinky/blinky-%d.gif", i);
					images[i] = ImageIO.read(new File(path));
				}
			}
			catch (IOException e) {
				System.err.println(e.toString());
			}		
		}

		/* use explicit instance variables for readability */
		down0 = images[0];
		down1= images[1];
		left0 = images[2];
		left1 = images[3];
		up0= images[4];
		up1= images[5];
		right0 = images[6];
		right1 = images[7];
		
	}
	
	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Image getImage() {
		/*
		 * Calculation for which image to display
		 * 1. calculate how many periods of time have elapsed since this sprite was instantiated?
		 * 2. calculate which image (aka 'frame') of the sprite animation should be shown out of the cycle of images
		 * 3. use some conditional logic to determine the right image for the current direction
		 */
		long period = elapsedTime / PERIOD_LENGTH;
		int image = (int) (period % IMAGES_IN_CYCLE);

		if (image == 0) {
			switch (direction) {
			case UP:
				return up0;
			case DOWN:
				return down0;
			case LEFT:
				return left0;
			case RIGHT:
				return right0;
			case STOP:
				return right0;
			}
		} else {
			switch (direction) {
			case UP:
				return up1;
			case DOWN:
				return down1;
			case LEFT:
				return left1;
			case RIGHT:
				return right1;
			case STOP:
				return right1;
			}
		}
		//code should never reach here; statement included to keep compiler happy
		return null;
	}
	
	//DISPLAYABLE
	
	public boolean getVisible() {
		return true;
	}
	
	public double getMinX() {
		return centerX - (width / 2);
	}

	public double getMaxX() {
		return centerX + (width / 2);
	}

	public double getMinY() {
		return centerY - (height / 2);
	}

	public double getMaxY() {
		return centerY + (height / 2);
	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public double getCenterX() {
		return centerX;
	};

	public double getCenterY() {
		return centerY;
	};
	
	
	public boolean getDispose() {
		return dispose;
	}

	public void setDispose(boolean dispose) {
		this.dispose = dispose;
	}


	public void update(Universe universe, long actual_delta_time) {
		
		elapsedTime += actual_delta_time;
		
		switch (direction) {
		case UP:
			this.centerY  -= actual_delta_time * 0.001 * VELOCITY;
			break;
		case DOWN:
			this.centerY  += actual_delta_time * 0.001 * VELOCITY;
			break;
		case LEFT:
			this.centerX -= actual_delta_time * 0.001 * VELOCITY;
			break;
		case RIGHT:
			this.centerX += actual_delta_time * 0.001 * VELOCITY;
			break;
		}

	}
}
