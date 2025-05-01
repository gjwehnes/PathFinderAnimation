import java.util.ArrayList;

public class SearchTreeVisualization implements Universe, Graph {

	private ArrayList<Background> backgrounds = new ArrayList<Background>();
	private ArrayList<DisplayableSprite> sprites = new ArrayList<DisplayableSprite>();
	private double centerX = 0;
	private double centerY = 0;
	private boolean complete;
	
	private final double ROOT_CENTERX = 0;
	private final double ROOT_CENTERY= -300;
	private final double MAXIMUM_HEIGHT = 600;
	private final double MAXIMUM_WIDTH = 800;

	private ArrayList<Node> nodes = new ArrayList<Node>();
	
	public SearchTreeVisualization() {
		
		Node root = new Node("A",10, ROOT_CENTERX, ROOT_CENTERY);
		nodes.add(root);
		buildSearchTree(3, 1, 6, root);
	}
	
	private void buildSearchTree(int branchesPerNode, int currentDepth, int remainingDepth, Node parentNode) {
		if (remainingDepth == 1) {
			return;
		}
		else {
			double spacingX = MAXIMUM_WIDTH / ( Math.pow(branchesPerNode, currentDepth) - 1);
			double spacingY = MAXIMUM_HEIGHT / (currentDepth + remainingDepth - 2);
			double leftX = parentNode.getCenterX() - (spacingX * (branchesPerNode - 1)) / 2.0;
			for (int i = 0; i < branchesPerNode; i++) {
				Node child = new Node();
				child.name = parentNode.name + (char)('A' + i);
				child.size = 10;
				//domain is from -500 to 500; range is from -400 to 400
				child.centerX = leftX + (i * spacingX);
				child.centerY = parentNode.centerY + spacingY;
				parentNode.neighbourNodes.add(child);
				parentNode.neighbourDistances.add(1.0);
				nodes.add(child);
				buildSearchTree(branchesPerNode, currentDepth + 1, remainingDepth - 1, child);
			}
			
			
		}
		
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
