import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class EuropeMap implements Universe, Graph {
		
	private ArrayList<Background> backgrounds = new ArrayList<Background>();
	private final double VELOCITY = 200;

	private ArrayList<DisplayableSprite> sprites = new ArrayList<DisplayableSprite>();

	private final int MAP_WIDTH = 1000;
	private final int MAP_HEIGHT = 750;
	private final double MAP_NORTH_LATITUDE = 59.6;
	private final double MAP_SOUTH_LATITUDE = 35.5;
	private final double MAP_EAST_LONGITUDE = 33.75;
	private final double MAP_WEST_LONGITUDE = -15.5;

//  large # of nodes
	final double MIN_POPULATION = 200000;
	final boolean INCLUDE_CAPITALS = false;
	final int MAX_DISTANCE_BETWEEN_CITIES_KM = 1000;
	int MAX_NEIGHBOURS = 4;
	
	
	private final double MAP_PIXELS_PER_DEGREE_LATITUDE = Math.abs(MAP_HEIGHT / (MAP_NORTH_LATITUDE - MAP_SOUTH_LATITUDE));
	private final double MAP_PIXELS_PER_DEGREE_LONGITUDE = Math.abs(MAP_WIDTH / (MAP_WEST_LONGITUDE - MAP_EAST_LONGITUDE));
	
	private double centerX = MAP_WIDTH / 2;
	private double centerY = MAP_HEIGHT / 2;
	private boolean complete;

	private ArrayList<Node> nodes = new ArrayList<Node>();
	
	public EuropeMap() {
		
		nodes = GraphBuilder.buildGraphFromCitiesDataset(MAP_NORTH_LATITUDE, MAP_SOUTH_LATITUDE,	MAP_EAST_LONGITUDE,	MAP_WEST_LONGITUDE,
				MIN_POPULATION, INCLUDE_CAPITALS, MAX_DISTANCE_BETWEEN_CITIES_KM, MAX_NEIGHBOURS,
				MAP_WIDTH, MAP_HEIGHT, "res/world-cities.csv");

		MapBackground background = new MapBackground("res/europe-map.jpg");
		backgrounds.add(background);
		
		
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
		double velocityX = 0;
		double velocityY = 0;
		
		//set velocity based on current state of the keyboard
		
		//LEFT ARROW
		if (KeyboardInput.getKeyboard().keyDown(37)) {
			velocityX = -VELOCITY;
		}
		//UP ARROW
		if (KeyboardInput.getKeyboard().keyDown(38)) {
			velocityY = -VELOCITY;			
		}
		//RIGHT ARROW
		if (KeyboardInput.getKeyboard().keyDown(39)) {
			velocityX += VELOCITY;
		}
		// DOWN ARROW
		if (KeyboardInput.getKeyboard().keyDown(40)) {
			velocityY += VELOCITY;			
		}

		//calculate new position based on velocity and time elapsed
		this.centerX += actual_delta_time * 0.001 * velocityX;
		this.centerY += actual_delta_time * 0.001 * velocityY;
				
	}
		
}
