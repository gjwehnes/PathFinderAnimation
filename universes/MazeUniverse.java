import java.util.ArrayList;
import java.util.Random;

public class MazeUniverse implements Universe, Graph {

	final int ROWS = 15;
	final int COLS = 15;
	double COL_WIDTH = 0;
	double ROW_HEIGHT = 0;
	double HALF_COL_WIDTH = 0;
	double HALF_ROW_HEIGHT = 0;
	
	private boolean complete = false;
	private long elapsedTime = 0;

	private CollidingSprite player = null;
	private ArrayList<DisplayableSprite> sprites = new ArrayList<DisplayableSprite>();
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private Node goalNode = null;
			
	
	//	//require a separate list for sprites to be removed to avoid a concurence exception
	private ArrayList<DisplayableSprite> disposalList = new ArrayList<DisplayableSprite>();

	@Override
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	@Override
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;		
	}
	
	public MazeUniverse () {

		double screenMinX = PathFinderFrame.screenWidth / -2;
		double screenMinY = PathFinderFrame.screenHeight / -2;
		double screenMaxX = PathFinderFrame.screenWidth / 2;
		double screenMaxY = PathFinderFrame.screenHeight / 2;

		COL_WIDTH = (PathFinderFrame.screenWidth - 16) / (float)COLS; 
		ROW_HEIGHT = (PathFinderFrame.screenHeight - 16) / (float)ROWS;
		HALF_COL_WIDTH = COL_WIDTH / 2;
		HALF_ROW_HEIGHT = ROW_HEIGHT / 2;
				
		//create random maze
		final double BARRIER_FREQUENCY = 0.4;
		final double HALF_BARRIER_WIDTH = 4;


		Node[][] nodeArray = new Node[ROWS][COLS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				nodeArray[row][col] = new Node((char)('A' + col)+Integer.toString(row),5, screenMinX + (col * COL_WIDTH) + (COL_WIDTH / 2), screenMinY + (row * ROW_HEIGHT) + (ROW_HEIGHT / 2));
				nodes.add(nodeArray[row][col]);
			}
		}

		Random random = new Random();
		random.setSeed(1);
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				Node current = nodeArray[row][col];
				Node neighbour = null;
				double minX = screenMinX + col * COL_WIDTH;
				double minY = screenMinY + row * ROW_HEIGHT;
				if (col < COLS - 1) {
					//right
					neighbour = nodeArray[row][col+1];
					if (random.nextDouble() > BARRIER_FREQUENCY) {					
						current.neighbourNodes.add(neighbour);
						current.neighbourDistances.add(findDistance(current, neighbour));
						neighbour.neighbourNodes.add(current);
						neighbour.neighbourDistances.add(findDistance(current, neighbour));
					}
					else {
						sprites.add(new BarrierSprite(minX + COL_WIDTH - HALF_BARRIER_WIDTH, minY, minX + COL_WIDTH + HALF_BARRIER_WIDTH , minY + ROW_HEIGHT, true));						
					}
				}
				if (row < ROWS - 1) {
					//down
					neighbour = nodeArray[row+1][col];
					if (random.nextDouble() > BARRIER_FREQUENCY) {					
						current.neighbourNodes.add(neighbour);
						current.neighbourDistances.add(findDistance(current, neighbour));
						neighbour.neighbourNodes.add(current);
						neighbour.neighbourDistances.add(findDistance(current, neighbour));
					}
					else {
						sprites.add(new BarrierSprite(minX, minY + ROW_HEIGHT - HALF_BARRIER_WIDTH, minX + COL_WIDTH, minY + ROW_HEIGHT + HALF_BARRIER_WIDTH, true));						
					}
				}
			}
		}
		
		goalNode = nodes.get(nodes.size() - 1);
		
		this.sprites.add(new BlinkySprite(screenMinX + HALF_COL_WIDTH + COL_WIDTH * 0,
				screenMinY + HALF_ROW_HEIGHT, nodes, this, "inky"));
		this.sprites.add(new BlinkySprite(screenMinX + HALF_COL_WIDTH + COL_WIDTH * 5,
				screenMinY + HALF_ROW_HEIGHT, nodes, this, "pinky"));
		this.sprites.add(new BlinkySprite(screenMinX + HALF_COL_WIDTH + COL_WIDTH * 10,
				screenMinY + HALF_ROW_HEIGHT, nodes, this, "blinky"));
		this.sprites.add(new BlinkySprite(screenMinX + HALF_COL_WIDTH + COL_WIDTH * 15,
				screenMinY + HALF_ROW_HEIGHT, nodes, this, "sue"));
		
		player = new CollidingSprite(screenMinX + HALF_COL_WIDTH + COL_WIDTH * 15,
				screenMinY + HALF_ROW_HEIGHT + ROW_HEIGHT * 14);
		
		this.sprites.add(player);
	}
	
	private static double findDistance(Node from, Node to) {
		double distanceX = from.getCenterX() - to.getCenterX();
		double distanceY = from.getCenterY() - to.getCenterY();					
		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);		
	}
		

	public double getScale() {
		return 1;
	}

	public double getXCenter() {
		return 0;
	}

	public double getYCenter() {
		return 0;
	}

	public void setXCenter(double xCenter) {
	}

	public void setYCenter(double yCenter) {
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		System.out.println(String.format("time = %3d.%03d; thread = %s; universe complete",elapsedTime / 1000, elapsedTime % 1000,Thread.currentThread().getName()));					
		for (DisplayableSprite sprite : sprites) {
			sprite.setDispose(true);
		}
		this.complete = complete;
	}
	
	public ArrayList<Background> getBackgrounds() {
		return null;
	}

	public ArrayList<DisplayableSprite> getSprites() {
		return sprites;
	}

	public boolean centerOnPlayer() {
		return false;
	}		

		
	public void update(Animation animation, long actual_delta_time) {
				
		elapsedTime += actual_delta_time;				
		
		for (int i = 0; i < sprites.size(); i++) {
			DisplayableSprite sprite = sprites.get(i);
			sprite.update(this, actual_delta_time);
    	} 
		
		disposeSprites();
		
		for (Node node : nodes) {
			if ((Math.abs(node.getCenterX() - player.getCenterX()) < HALF_COL_WIDTH)
					&& (Math.abs(node.getCenterY() - player.getCenterY()) < HALF_ROW_HEIGHT)) {
				goalNode = node;
			}
		}		
	}
	
	public Node getGoalNode() {
		return goalNode;
	}
	

	public String toString() {
		return goalNode != null ? goalNode.toString() : "";
	}	
	
    protected void disposeSprites() {
        
    	//collect a list of sprites to dispose
    	//this is done in a temporary list to avoid a concurrent modification exception
		for (int i = 0; i < sprites.size(); i++) {
			DisplayableSprite sprite = sprites.get(i);
    		if (sprite.getDispose() == true) {
    			disposalList.add(sprite);
    		}
    	}
		
		//go through the list of sprites to dispose
		//note that the sprites are being removed from the original list
		for (int i = 0; i < disposalList.size(); i++) {
			DisplayableSprite sprite = disposalList.get(i);
			sprites.remove(sprite);
    	}
		
		//clear disposal list if necessary
    	if (disposalList.size() > 0) {
    		disposalList.clear();
    	}
    }	    
}
