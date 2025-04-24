import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PathFinder {
		
	private Node origin;
	private Node destination;
	private ArrayList<Node> cities = new ArrayList<Node>();
	protected ArrayList<Node> path = new ArrayList<Node>();

	private final long STEP_DELAY_MS = 1;
	private final boolean VERBOSE = false;

	double KM_PER_DEGREE_LATITUDE = 110;
	
	private double northLatitude;
	private double southLatitude;
	private double eastLongitude;
	private double westLongitude;
	
	//minimal # of nodes
	private double minPopulation = 5000000;
	private boolean includeCapitals = true;
	private double maxDistanceBetweenNeighbours = 5000;
	private int maxNeighbours = 4;

	private int mapWidth;
	private int mapHeight;
		
	//large # of nodes
//	final double MIN_POPULATION = 200000;
//	final boolean INCLUDE_CAPITALS = false;
//	final int MAX_DISTANCE_BETWEEN_CITIES_KM = 1000;
//	double MAX_DISTANCE_OF_PATH = 2000;
//	double MAX_NEIGHBOURS = 4;
	
	double unitsPerDegreeLatitude = Math.abs(mapHeight / (northLatitude - southLatitude));
	double unitsPerDegreeLongitude = Math.abs(mapWidth / (westLongitude - eastLongitude));

	protected long steps = 0;
	private long current_time = 0;
	private boolean abort = false;
	private boolean calculating = false;
	
	public PathFinder(double northLatitude, double southLatitude, double eastLongitude, double westLongitude, 
						double minPopulation, boolean includeCapitals, double maxDistanceBetweenNeighbours, int maxNeighbours,
						int mapWidth, int mapHeight, String mapPath) {
		
		this.northLatitude = northLatitude;
		this.southLatitude = southLatitude;		
		this.eastLongitude = eastLongitude;
		this.westLongitude = westLongitude;
		
		this.minPopulation = minPopulation;
		this.includeCapitals = includeCapitals;
		this.maxDistanceBetweenNeighbours = maxDistanceBetweenNeighbours;
		this.maxNeighbours = maxNeighbours;
		
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;

		unitsPerDegreeLatitude = Math.abs(mapHeight / (northLatitude - southLatitude));
		unitsPerDegreeLongitude = Math.abs(mapWidth / (westLongitude - eastLongitude));
		
		buildGraph(mapPath);		
	}
	
	public ArrayList<Node> getNodes() {
		return cities;
	}

	public void abort() {
		this.abort = true;
	}
	
	public boolean isCalculating() {
		return calculating;
	}
	
	public long getSteps() {
		return this.steps;
	}
	
	public ArrayList<Node> findAPath(Node start, Node goal) {

		calculating = true;
		steps = 0;
		path.clear();
		path.add(start);

		boolean result = findAPath(start, goal, path);
		
		if (result == false || abort) {
			path.clear();
		}
		
		abort = false;
		calculating = false;
		return path;
	}

	//BRUTE FORCE ALGORITHM
	//
	//Will find a solution, regardless of length
	private boolean findAPath(Node current, Node goal, ArrayList<Node> currentPath) {

		takeStep();
		printState(currentPath);

		if (abort) {
			return false;
		}
		else if (current == goal) {
			System.out.println(String.format("Solution: have a cup of tea, you are already there :-)"));
			return true;
		}
		else if (current.neighbourNodes.contains(goal)) {
			//base case
			currentPath.add(goal);
			System.out.println(String.format("Solution: calls: %8d; length = %5.1f; path = %s" , steps, pathLength(currentPath), currentPath.toString()));
			return true;
		}
		else {		
			for (Node neighbour : current.neighbourNodes) {
				boolean haveNotVisited =currentPath.contains(neighbour) == false;
				if (haveNotVisited) {
					currentPath.add(neighbour);
					boolean solved = findAPath(neighbour, goal, currentPath);
					if (solved) {
						return solved;
					}
					else {
						currentPath.remove(neighbour);
					}								
				}
			}
			return false;
		}		
	}


	//REFINED ALGORITHM #1
	//
	//Track the distance used, and compare it to a maximum; don't pursue paths that will be longer
	//This assumes that we know approximately how long the path should be!

	public ArrayList<Node> findAPath1(Node start, Node goal, double additionalDistancePercent) {

		calculating = true;
		steps = 0;
		path.clear();
		path.add(start);
		
		double additionalDistance = findDistance(start,goal) * (additionalDistancePercent / 100);		
		path.add(start);

		boolean result = findAPath1(start, goal, path, findDistance(start,goal) + additionalDistance);
		
		if (result == false || abort) {
			path.clear();
		}

		abort = false;
		calculating = false;
		return path;
	}
	private boolean findAPath1(Node current, Node goal, ArrayList<Node> currentPath, double distanceLeft) {

		takeStep();
		printState(currentPath);
		
		if (abort) {
			return false;
		}else if (current.neighbourNodes.contains(goal)) {			
			//base case
			double distanceToGoal = findDistance(current,goal);
			if (distanceLeft >= distanceToGoal) {
				currentPath.add(goal);
				System.out.println(String.format("Solution: calls: %8d; length = %5.1f; path = %s" , steps, pathLength(currentPath), currentPath.toString()));
				return true;
			}
			else {
				return false;
			}
		}
		else {
			for (Node neighbour : current.neighbourNodes) {
				boolean haveNotVisited =currentPath.contains(neighbour) == false;
				//how far from current to neighbour?
				double distanceToNeighbour = findDistance(current,neighbour);
				//have we visited this city yet?
				boolean haveDistanceRemaining = (distanceLeft >= distanceToNeighbour);
				if (haveNotVisited && haveDistanceRemaining) {
					currentPath.add(neighbour);
					boolean solved = findAPath1(neighbour, goal, currentPath, distanceLeft - distanceToNeighbour);
					if (solved) {
						return true;
					}
					else {
						currentPath.remove(neighbour);
					}								
				}
			}
			return false;
		}		
	}

	//REFINED ALGORITHM #2 - Track the distance left
	//
	//Track the distance used, and compare it to a maximum; don't pursue paths that will be longer
	//This assumes that we know approximately how long the path should be, and how far from each node to the destination

	public ArrayList<Node> findAPath2(Node start, Node goal) {

		calculating = true;
		steps = 0;
		path.clear();
		path.add(start);

		boolean result = findAPath2(start, goal, path);
		
		if (result == false || abort) {
			path.clear();
		}
		
		abort = false;
		calculating = false;
		return path;
	}
	private boolean findAPath2(Node current, Node goal, ArrayList<Node> currentPath) {

		takeStep();
		printState(currentPath);

		double distanceOfCurrentPath = pathLength(currentPath);

		if (abort) {
			return false;
		}else if (current == goal) {			
			currentPath.add(goal);
			System.out.println(String.format("Solution: calls: %8d; length = %5.1f; path = %s" , steps, pathLength(currentPath), currentPath.toString()));
			return true;
		}
		else {		
			for (Node neighbour : current.neighbourNodes) {
				boolean haveNotVisited =currentPath.contains(neighbour) == false;
				//how far from neighbour to goal?
				double distanceFromNeigbour = findDistance(neighbour, goal);
				//how far from current to goal?
				double distanceFromCurrent = findDistance(current, goal);
				//how far from current to neighbour?
				double distanceToNeighbour = findDistance(current, neighbour);
				//neighbour is actually closer to goal?
				boolean closerToGoal = distanceFromNeigbour < distanceFromCurrent;
				if (haveNotVisited && closerToGoal) {
					currentPath.add(neighbour);
					boolean solved = findAPath2(neighbour, goal, currentPath);
					if (solved) {
						return true;
					}
					else {
						currentPath.remove(neighbour);
					}								
				}
			}
			return false;
		}		
	}


	private void buildGraph(String mapPath) {

		File file = new File(mapPath);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			return;
		} 

		String input;
		try {
			//read header, not used
			br.readLine(); 
			//add every city within range
			while ((input = br.readLine()) != null) {
				String[] fields = input.split("\\s*,\\s*");
				try {
					double latitude = Double.parseDouble(fields[2]);
					double longitude = Double.parseDouble(fields[3]);
					int population = Integer.parseInt(fields[9]);
					boolean isCapital = fields[8].equals("primary");
					
					
					if (latitude >= southLatitude && 
							latitude <= northLatitude &&
							longitude >= westLongitude &&
							longitude <= eastLongitude &&						
							((population >= minPopulation) || (isCapital && includeCapitals))) {
						Node city = new Node();
						city.name = fields[1];
						city.size = (int) Math.max(2, Math.log(population / 100000) / Math.log(2));					
						city.centerX = translateLongitudeToLogicalX(longitude);
						city.centerY = translateLatitudeToLogicalY(latitude);
						cities.add(city);
//						System.out.println(city.name);
					}
				}
				catch (Exception e) {
					//ignore this row
				}
				
			}
		} catch (IOException e) {
		}
		

		double unitsPerDegreeLatitude = translateLatitudeToLogicalY(southLatitude) - translateLatitudeToLogicalY(southLatitude + 1);
		double maxDistance = (unitsPerDegreeLatitude) * (maxDistanceBetweenNeighbours / KM_PER_DEGREE_LATITUDE);

		//add all neighbours within range
		for (int indexFrom = 0; indexFrom < cities.size(); indexFrom++) {
			Node from = (Node) cities.get(indexFrom);
			for (int indexTo = 0; indexTo < cities.size(); indexTo++) {
				try {
					Node to = (Node) cities.get(indexTo);
					double distance = findDistance(from, to);
					if (distance > 0 && distance <= maxDistance) {
						from.neighbourNodes.add(to);
						from.neighbourDistances.add(distance);
					}
				} catch (NumberFormatException e) {
				}
			}
			//reduce the number of neighbours to a maximum
			while (from.neighbourNodes.size() > maxNeighbours) {
				double furthestDistance = 0;
				int indexTarget = 0;
				for (int i = 0; i < from.neighbourDistances.size(); i++) {
					if (from.neighbourDistances.get(i) > furthestDistance) {
						furthestDistance = from.neighbourDistances.get(i);
						indexTarget = i;
					}
				}
				Node to = (Node) cities.get(cities.indexOf(from.neighbourNodes.get(indexTarget)));
				from.neighbourNodes.remove(indexTarget);
				from.neighbourDistances.remove(indexTarget);
				int indexTo = to.neighbourNodes.indexOf(from);
				if (indexTo >=0) {
					to.neighbourNodes.remove(indexTo);
					to.neighbourDistances.remove(indexTo);
				}
			}
		}
		
		//check if all links are reciprocated
		for (int indexCurrent = 0; indexCurrent < cities.size(); indexCurrent++) {
			Node current = (Node) cities.get(indexCurrent);
			for (int indexNeighbour = 0; indexNeighbour < current.neighbourNodes.size(); indexNeighbour++) {
				Node neighbour = current.neighbourNodes.get(indexNeighbour);
				if (neighbour.neighbourNodes.indexOf(current) < 0) {
					neighbour.neighbourNodes.add(current);
				}
			}
			
		}
		
	}

	private double pathLength(ArrayList<Node> path) {
		double length = 0;
		for (int i = 0; i < path.size() - 1; i++) {
			Node city1 = path.get(i);
			Node city2 = path.get(i+1);
			length+= findDistance(city1, city2);
		}
		return length;
	}
	
	private void takeStep() {		
		//sleep until the next refresh time
		long next_refresh_time = current_time + STEP_DELAY_MS;

		//sleep until the next refresh time
		while (current_time < next_refresh_time)
		{
			//allow other threads (i.e. the Swing thread) to do its work
			Thread.yield();

			try {
				Thread.sleep(1);
			}
			catch(Exception e) {    					
			} 

			//track current time
			current_time = System.currentTimeMillis();
		}
		
		this.steps++;
	}
	
	private void printState(ArrayList<Node> currentPath) {
		if (VERBOSE) {
			System.out.println(String.format("Step: calls: %8d; length = %5.1f; path = %s" , steps, pathLength(currentPath), currentPath.toString()));
		}		
	}
	public static Object[][] importFromCSV(String fileName) {

		File file = new File(fileName);
		ArrayList<String> rows = new ArrayList<String>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			return null;
		} 

		String input;
		try {
			while ((input = br.readLine()) != null) {
				rows.add(input);
			}
		} catch (IOException e) {
		}			
		
		int rowCount = rows.size();
		if (rowCount == 0) {
			return null;
		}
		else {
			String[] fields = rows.get(0).split("\\s*,\\s*");
			int columnCount = fields.length;
			
			String[][] csv = new String[rowCount][columnCount];
			
			for (int row = 0; row < rowCount; row++) {
				fields = rows.get(row).split("\\s*,\\s*");
				for (int column = 0; column < columnCount; column++) {
					csv[row][column] = fields[column];
				}
			}
			return csv;
		}		
	}

	public double getNorthLatitude() {
		return northLatitude;
	}

	public double getSouthLatitude() {
		return southLatitude;
	}

	public double getEastLongitude() {
		return eastLongitude;
	}

	public double getWestLongitude() {
		return westLongitude;
	}

	public int getMapWidth() {
		return 1050;
	}

	public int getMapHeight() {
		return 750;
	}
	
	private double findDistance(Node from, Node to) {
		double distanceX = from.getCenterX() - to.getCenterX();
		double distanceY = from.getCenterY() - to.getCenterY();					
		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);		
	}
	
	private double translateLongitudeToLogicalX(double longitude) {
		double offsetLongitude = longitude - this.getWestLongitude();
		return unitsPerDegreeLongitude * offsetLongitude;
	}

	private double translateLatitudeToLogicalY(double latitude) {
		double length = WebMercator.latitudeToY(this.getNorthLatitude()) - WebMercator.latitudeToY(this.getSouthLatitude());
		double distance = WebMercator.latitudeToY(latitude) - WebMercator.latitudeToY(this.getSouthLatitude());
		double ratio = (distance / length);
		double pixel = this.getMapHeight() - (distance / length) * this.getMapHeight();
		return pixel;

	}
	
}
