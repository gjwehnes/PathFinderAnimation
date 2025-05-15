import java.util.ArrayList;
import java.util.Random;

public class MazeUniverse implements Universe, Graph {

	final int ROWS = 15;
	final int COLS = 15;
	double COL_WIDTH = 0;
	double ROW_HEIGHT = 0;
	double HALF_COL_WIDTH = 0;
	double HALF_ROW_HEIGHT = 0;
	final long SEARCH_REFRESH_TIME = 5000;
	
	private boolean complete = false;
	private ArrayList<DisplayableSprite> sprites = new ArrayList<DisplayableSprite>();
	BlinkySprite blinky	= null;
	private String status = "";
	private ArrayList<Node> nodes = new ArrayList<Node>();
	PathFinder pathfinder = new PathFinder();
	private long triggerSearch = SEARCH_REFRESH_TIME;

	ArrayList<Node> path = new ArrayList<Node>();
	Thread calculationThread = null;
	
	Node blinkyNextNode = null;
	Node blinkyGoalNode = null;
	int blinkyDestinationIndex = 0;
	
	
	//	//require a separate list for sprites to be removed to avoid a concurence exception
	private ArrayList<DisplayableSprite> disposalList = new ArrayList<DisplayableSprite>();

	public ArrayList<Node> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	public PathFinder getPathfinder() {
		return pathfinder;
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
		
		blinky = new BlinkySprite(screenMinX + ROW_HEIGHT,screenMinY + COL_WIDTH);
		this.sprites.add(blinky);

		//create random maze
		final double BARRIER_FREQUENCY = 0.2;
		final double HALF_BARRIER_WIDTH = 4;


		Node[][] nodeArray = new Node[ROWS][COLS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				nodeArray[row][col] = new Node((char)('A' + col)+Integer.toString(row),5, screenMinX + (col * COL_WIDTH) + (COL_WIDTH / 2), screenMinY + (row * ROW_HEIGHT) + (ROW_HEIGHT / 2));
				nodes.add(nodeArray[row][col]);
			}
		}

		Random random = new Random();
		random.setSeed(0);
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

	public Node findBlinkGoalNode() {
		for (Node node : nodes) {
			if ((Math.abs(node.getCenterX() - MouseInput.logicalX  ) < HALF_COL_WIDTH)
					&& (Math.abs(node.getCenterY() - MouseInput.logicalY) < HALF_ROW_HEIGHT)) {
				return node;
			}
		}
		return null;
	}

	/* 
	 * calculate which node is most proximate to current position
	 */
	public Node findBlinkyNextNode() {
		for (Node node : nodes) {
			if ((Math.abs(node.getCenterX() - blinky.getCenterX()) < HALF_COL_WIDTH)
					&& (Math.abs(node.getCenterY() - blinky.getCenterY()) < HALF_ROW_HEIGHT)) {
				return node;
			}
		}
		return null;
	}
	
	public Node findBlinkyNextNode(long time) {
		
		int index = 1;
		Node current = null;
		Node next = null;
		
		/* 
		 * calculate up to which node (but not including) the sprite can travel in a given time
		 */
		while (path.size() > index && time > 0) {
			current = path.get(index - 1);
			next = path.get(index);
			double deltaX = current.getCenterX() - next.getCenterX();
			double deltaY = current.getCenterY() - next.getCenterY();
			double distanceToNext = Math.sqrt(deltaX * deltaX + deltaY + deltaY);
			double timeToNext = distanceToNext / BlinkySprite.VELOCITY;
			time -= timeToNext;
		}
		
		if (next != null) {
			return next;
		}
		else {			
			return findBlinkyNextNode();
		}
	}
		
	public void update(Animation animation, long actual_delta_time) {
		
		triggerSearch -= actual_delta_time;
		if (triggerSearch < 0) {
			triggerSearch = SEARCH_REFRESH_TIME;
			blinkyGoalNode = findBlinkGoalNode();
			boolean goalChanged = blinkyGoalNode == null
					|| path.size() == 0
					|| path.get(path.size()-1) != blinkyGoalNode;
			if (goalChanged) {
				//we need to change the path!
				blinkyNextNode = findBlinkyNextNode(SEARCH_REFRESH_TIME);
				if (pathfinder.isCalculating()) {
					pathfinder.abort();
				}
				else {
					findPath(blinkyNextNode, blinkyGoalNode);
				}
				
			}
		}
		
		
		//if blinky does not yet have a destination node, set to the first node in the path
		if (blinkyNextNode == null && path.size() != 0) {
			blinkyDestinationIndex = 0;
			blinkyNextNode = path.get(blinkyDestinationIndex);
		}

		if (blinkyNextNode != null) {
			double deltaX =  blinkyNextNode.getCenterX() - blinky.getCenterX();
			double deltaY =  blinkyNextNode.getCenterY() - blinky.getCenterY();
			
			if ((Math.abs(blinkyNextNode.getCenterX() - blinky.getCenterX()) < 5)
					&& (Math.abs(blinkyNextNode.getCenterY() - blinky.getCenterY()) < 5)) {
				//within a pixel of the destination, so move to the next destination
				blinkyDestinationIndex++;
				if (blinkyDestinationIndex < path.size()) {
					blinkyNextNode = path.get(blinkyDestinationIndex);
					System.out.println(String.format("Blinky destination node: %s", blinkyNextNode.name));
				} else {
					blinkyNextNode = null;
					path.clear();
				}
			}
		}
		
		if (blinkyNextNode == null) {
			blinky.setDirection(Direction.STOP);
		}
		else {
			double deltaX =  blinkyNextNode.getCenterX() - blinky.getCenterX();
			double deltaY =  blinkyNextNode.getCenterY() - blinky.getCenterY();
			if ( Math.abs(deltaX) > Math.abs(deltaY)) {
				//need to travel further in x dimension, so left or right
				if (deltaX < 0) {
					blinky.setDirection(Direction.LEFT);
				}
				else {
					blinky.setDirection(Direction.RIGHT);					
				}
			}
			else {
				//need to travel further in y dimension, so up or down
				if (deltaY < 0) {
					blinky.setDirection(Direction.UP);
				}
				else {
					blinky.setDirection(Direction.DOWN);					
				}
			}
		}						
		
		for (int i = 0; i < sprites.size(); i++) {
			DisplayableSprite sprite = sprites.get(i);
			sprite.update(this, actual_delta_time);
    	} 
		
		disposeSprites();
		
	}

	public String toString() {
		return String.format(String.format("Next: %s; Goal: %s", 
				blinkyNextNode !=  null ? blinkyNextNode.name: "null",
				blinkyGoalNode !=  null ? blinkyGoalNode.name: "null"));
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
			System.out.println("Remove: " + sprite.toString());
    	}
		
		//clear disposal list if necessary
    	if (disposalList.size() > 0) {
    		disposalList.clear();
    	}
    }

	protected void findPath(Node start, Node end) {

		System.out.println(String.format("time = %5d; findAPath %s - %s", System.currentTimeMillis() % 10000,
				start.name, end.name));

		
		calculationThread = new Thread()
		{
			public void run()
			{
				
		        System.out.println(String.format("time = %5d; thread = %s; start thread", System.currentTimeMillis() % 10000, Thread.currentThread().getName()));
		        
				if (pathfinder.isCalculating()) { 
					//calculation is still running....abort
					pathfinder.abort();
				}
				
		        System.out.println(String.format("time = %5d; thread = %s; start search",System.currentTimeMillis() % 10000,Thread.currentThread().getName()));
				pathfinder.findAnyPath(start,end);
		        System.out.println(String.format("time = %5d; thread = %s; end search",System.currentTimeMillis() % 10000,Thread.currentThread().getName()));
				
				path = (ArrayList<Node>) pathfinder.optimalPath.clone();
				blinkyGoalNode = end;

		        System.out.println(String.format("time = %5d; thread = %s; end thread",System.currentTimeMillis() % 10000,Thread.currentThread().getName()));
			}
		};
	
		calculationThread.start();
				
	}
    
}
