import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
	private static final long SEARCH_REFRESH_TIME = 5000;

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

	private ArrayList<Node> nodes = new ArrayList<Node>();
	private PathFinder pathfinder = null;
	private ArrayList<Node> currentPath = new ArrayList<Node>();
	private ArrayList<Node> nextPath = new ArrayList<Node>();
	private Node nextNode = null;
	private Node goalNode = null;	
	int blinkyDestinationIndex = 0;
	
	
	private Direction direction = Direction.RIGHT;
	
	public BlinkySprite(double centerX, double centerY, ArrayList<Node> nodes) {

		this.centerX =centerX;
		this.centerY = centerY;
		this.nodes = nodes;
		
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

		pathfinder = new PathFinder(0, false, false, false, 5000);
		getGoalNode();
		
		startPathFinding();
		
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

		getGoalNode();
		
		//if blinky does not currently have a destination node, retrieve from path
		if (nextNode == null) {
			//if current path is empty, load next path
			if (currentPath.size() == 0) {
				if (nextPath.size() != 0) {
					currentPath.addAll(nextPath);
					System.out.println(String.format("time = %3d.%03d; thread = %s; new path = %s",elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName(), nextPath.toString()));										
					nextPath.clear();
				}
			}
			//if there actually is a next node
			if (currentPath.size() != 0) {
				nextNode = currentPath.get(0);
				currentPath.remove(0);
		        System.out.println(String.format("time = %3d.%03d; thread = %s; Blinky next node: %s", elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName(), nextNode.name));				
			}
		}

		if (nextNode != null) {

			//check whether next node has been reached

			double deltaX =  nextNode.getCenterX() - this.getCenterX();
			double deltaY =  nextNode.getCenterY() - this.getCenterY();
			
			if ((Math.abs(nextNode.getCenterX() - this.getCenterX()) < 5)
					&& (Math.abs(nextNode.getCenterY() - this.getCenterY()) < 5)) {
				//within range of the destination, so move to the next destination
		        System.out.println(String.format("time = %3d.%03d; thread = %s; Blinky reached node: %s", elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName(), nextNode.name));
				if (currentPath.size() > 0) {
					nextNode = currentPath.get(0);
					currentPath.remove(0);
			        System.out.println(String.format("time = %3d.%03d; thread = %s; Blinky next node: %s", elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName(), nextNode.name));
				}
				else {
					nextNode = null;
				}
			}
		}
		
		if (nextNode == null) {
			this.setDirection(Direction.STOP);
		}
		else {
			double deltaX =  nextNode.getCenterX() - this.getCenterX();
			double deltaY =  nextNode.getCenterY() - this.getCenterY();
			if ( Math.abs(deltaX) > Math.abs(deltaY)) {
				//need to travel further in x dimension, so left or right
				if (deltaX < 0) {
					this.setDirection(Direction.LEFT);
				}
				else {
					this.setDirection(Direction.RIGHT);					
				}
			}
			else {
				//need to travel further in y dimension, so up or down
				if (deltaY < 0) {
					this.setDirection(Direction.UP);
				}
				else {
					this.setDirection(Direction.DOWN);					
				}
			}
		}						
		
		
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
	
	
	protected void startPathFinding() {

		Thread calculationThread = new Thread()
		{
			public void run()
			{
		        while (dispose == false) {
		        	
		        	//if the next path is non-zero length, it has not yet been made the current path
		        	while (nextPath.size() > 0) {
						//allow other threads (i.e. the Swing thread) to do its work
						Thread.yield();

						try {
							Thread.sleep(1);
						}
						catch(Exception e) {    					
						} 
		        	}
		        	
					Node startNode = currentPath.size() > 0 ?  currentPath.getLast() : getNearestNode();
					
					if (startNode != goalNode) {
				        System.out.println(String.format("time = %3d.%03d; thread = %s; start search;  %2s to %2s",elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName(), startNode.toString(),goalNode.toString()));
				        pathfinder.findAPathPrioritizeProgression(startNode, goalNode);
	
				        ArrayList<Node> tempPath = (ArrayList<Node>) pathfinder.optimalPath.clone();
						truncatePath(tempPath, SEARCH_REFRESH_TIME + 500);
						System.out.println(String.format("time = %3d.%03d; thread = %s; end search; next path = %s",elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName(), nextPath.toString()));
						nextPath = tempPath;
					}					
		        }
			}
		};
	
		calculationThread.start();
				
	}
	
	public void getGoalNode() {
				
		for (Node node : nodes) {
			if ((Math.abs(node.getCenterX() - MouseInput.logicalX  ) < 5)
					&& (Math.abs(node.getCenterY() - MouseInput.logicalY) < 5)) {
				if (node != goalNode) {
					goalNode = node;
					System.out.println(String.format("time = %3d.%03d; thread = %s; next goal = %s",elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName(), goalNode.toString()));
				}
			}
		}

		if (goalNode == null) {
			goalNode = nodes.get(nodes.size() - 1);
		}
		
	}

	/* 
	 * calculate which node is most proximate to current position
	 */
	public Node getNearestNode() {

		Node nearestNode = null;
		double distanceToNearest = Double.MAX_VALUE;
		for (Node nextNode : nodes) {
			double deltaX = nextNode.getCenterX() - this.getCenterX();
			double deltaY = nextNode.getCenterY() - this.getCenterY();
			double distanceToNext = Math.sqrt(deltaX * deltaX + deltaY + deltaY);
			if (distanceToNext < distanceToNearest) {
				nearestNode = nextNode;
				distanceToNearest = distanceToNext;
			}
		}
		return nearestNode;
	}
	
	public void truncatePath(ArrayList<Node> clonePath, double time) {
		
		int index = 1;
		Node current = null;
		Node next = null;
		
		/* 
		 * calculate up to which node (but not including) the sprite can travel in a given time
		 */		
		while (clonePath.size() > index && time > 0) {
			current = clonePath.get(index - 1);
			next = clonePath.get(index);
			double deltaX = current.getCenterX() - next.getCenterX();
			double deltaY = current.getCenterY() - next.getCenterY();
			double distanceToNext = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			double timeToNext = (distanceToNext / BlinkySprite.VELOCITY) * 1000;
			time -= timeToNext;
			index++;
		}
		
		while (clonePath.size() > index) {
			clonePath.remove(index);
		}
		
	}
	
}
