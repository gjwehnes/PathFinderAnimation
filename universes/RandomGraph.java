import java.util.ArrayList;

public class RandomGraph implements Universe, Graph {

	private ArrayList<Background> backgrounds = new ArrayList<Background>();
	private ArrayList<DisplayableSprite> sprites = new ArrayList<DisplayableSprite>();
	private double centerX = 0;
	private double centerY = 0;
	private boolean complete;
	
	private final double MIN_X = -400;
	private final double MAX_X= 400;
	private final double MIN_Y = -300;
	private final double MAX_Y = 300;
	private final double DEVIATION_FACTOR = 0.5;

	private ArrayList<Node> nodes = new ArrayList<Node>();
	
	public RandomGraph() {
		buildRandomGraph(12, 16);
	}
	
	private void buildRandomGraph(int rows, int columns) {
		
		
		double spacingX = (MAX_X - MIN_X) / (columns - 1);
		double spacingY = (MAX_Y - MIN_Y) / (rows - 1);

		Node[][] nodeArray = new Node[rows][columns];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				Node node = new Node();
				node.centerX = MIN_X + (spacingX * col) + ( (Math.random() * 2- 1) * spacingX * DEVIATION_FACTOR);
				node.centerY = MIN_Y + (spacingY * row) + ( (Math.random() * 2- 1) * spacingY * DEVIATION_FACTOR);
				node.name = (char)('A' + col) + Integer.toString(row, 10);
				nodeArray[row][col] = node;
				nodes.add(node);
			}
		}
		
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				Node current = nodeArray[row][col];
				Node neighbour = null;
				if (row >= 1) {
					//above
//					neighbour = nodes[row-1][col];
//					current.neighbourNodes.add(neighbour);
//					current.neighbourDistances.add(findDistance(current, neighbour));
					if (col < columns - 1) {
						//above-right
						neighbour = nodeArray[row-1][col+1];
						current.neighbourNodes.add(neighbour);
						current.neighbourDistances.add(findDistance(current, neighbour));
					}
				}
				if (col < columns - 1) {
					//right
					neighbour = nodeArray[row][col+1];
					current.neighbourNodes.add(neighbour);
					current.neighbourDistances.add(findDistance(current, neighbour));
				}
				if (row < rows - 1) {
					if (col < columns - 1) {
						//right-down
						neighbour = nodeArray[row+1][col+1];
						current.neighbourNodes.add(neighbour);
						current.neighbourDistances.add(findDistance(current, neighbour));
					}
//					//down
//					neighbour = nodes[row+1][col];
//					current.neighbourNodes.add(neighbour);
//					current.neighbourDistances.add(findDistance(current, neighbour));
				}
			}
		}
		
	}
	
	private static double findDistance(Node from, Node to) {
		double distanceX = from.getCenterX() - to.getCenterX();
		double distanceY = from.getCenterY() - to.getCenterY();					
		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);		
	}
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}				
	
	public double getScale() {
		return 1.2;
	}

	public double getXCenter() {
		return this.centerX;
	}

	@Override
	public double getYCenter() {
		return this.centerY;
	}

	public void setXCenter(double xCenter) {		
	}

	public void setYCenter(double yCenter) {		
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
	}

	public DisplayableSprite getPlayer1() {
		return null;
	}

	public ArrayList<DisplayableSprite> getSprites() {
		return sprites;
	}

	@Override
	public ArrayList<Background> getBackgrounds() {
		return backgrounds;
	}

	@Override
	public void update(Animation animation, long actual_delta_time) {
	}
	
}
