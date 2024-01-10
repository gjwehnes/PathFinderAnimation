import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PathFinder {
		
	CityNode origin;
	CityNode destination;
	protected ArrayList<CityNode> cities = new ArrayList<CityNode>();
	protected ArrayList<CityNode> path = new ArrayList<CityNode>();

	final long STEP_DELAY_MS = 0;
	final boolean VERBOSE = false;	

	final int MAP_WIDTH = 1000;
	final int MAP_HEIGHT = 750;
	final double MAP_NORTH_LATITUDE = 59.6;
	final double MAP_SOUTH_LATITUDE = 35.5;
	final double MAP_EAST_LONGITUDE = 33.75;
	final double MAP_WEST_LONGITUDE = -15.5;
	final double KM_PER_DEGREE_LATITUDE = 110;
	
	//minimal # of nodes
	final double MIN_POPULATION = 5000000;
	final boolean INCLUDE_CAPITALS = true;
	final int MAX_DISTANCE_BETWEEN_CITIES_KM = 5000;
	final double MAX_DISTANCE_OF_PATH = 2000;
	final double MAX_NEIGHBOURS = 4;
	
	//large # of nodes
//	final double MIN_POPULATION = 200000;
//	final boolean INCLUDE_CAPITALS = false;
//	final int MAX_DISTANCE_BETWEEN_CITIES_KM = 1000;
//	double MAX_DISTANCE_OF_PATH = 2000;
//	double MAX_NEIGHBOURS = 4;
	
	final double MAP_PIXELS_PER_DEGREE_LATITUDE = Math.abs(MAP_HEIGHT / (MAP_NORTH_LATITUDE - MAP_SOUTH_LATITUDE));
	final double MAP_PIXELS_PER_DEGREE_LONGITUDE = Math.abs(MAP_WIDTH / (MAP_WEST_LONGITUDE - MAP_EAST_LONGITUDE));

	protected long steps = 0;
	private long current_time = 0;
	private boolean abort = false;
	private boolean calculating = false;
	
	public PathFinder() {
		buildGraph();		
	}
	
	public ArrayList<CityNode> getNodes() {
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
	
	public ArrayList<CityNode> findAPath(CityNode start, CityNode goal) {

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
	private boolean findAPath(CityNode current, CityNode goal, ArrayList<CityNode> currentPath) {

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
			for (CityNode neighbour : current.neighbourNodes) {
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

	public ArrayList<CityNode> findAPath1(CityNode start, CityNode goal, double additionalDistancePercent) {

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
	private boolean findAPath1(CityNode current, CityNode goal, ArrayList<CityNode> currentPath, double distanceLeft) {

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
			for (CityNode neighbour : current.neighbourNodes) {
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

	public ArrayList<CityNode> findAPath2(CityNode start, CityNode goal) {

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
	private boolean findAPath2(CityNode current, CityNode goal, ArrayList<CityNode> currentPath) {

		takeStep();
		printState(currentPath);

		double distanceOfCurrentPath = pathLength(currentPath);

		if (abort) {
			return false;
		}else if (current.neighbourNodes.contains(goal)) {			
			//base case
			double distanceToGoal = findDistance(current, goal);
			if (distanceOfCurrentPath + distanceToGoal <= MAX_DISTANCE_OF_PATH) {
				currentPath.add(goal);
				System.out.println(String.format("Solution: calls: %8d; length = %5.1f; path = %s" , steps, pathLength(currentPath), currentPath.toString()));
				return true;
			}
			else {
				return false;
			}
		}
		else {		
			for (CityNode neighbour : current.neighbourNodes) {
				boolean haveNotVisited =currentPath.contains(neighbour) == false;
				//how far from neighbour to goal?
				double distanceFromNeigbour = findDistance(neighbour, goal);
				//how far from current to goal?
				double distanceFromCurrent = findDistance(current, goal);
				//how far from current to neighbour?
				double distanceToNeighbour = findDistance(current, neighbour);
				//have we visited this city yet?
				boolean haveDistanceRemaining = (distanceOfCurrentPath + distanceToNeighbour) <= MAX_DISTANCE_OF_PATH;
				//neighbour is actually closer to goal?
				boolean closerToGoal = distanceFromNeigbour < distanceFromCurrent;
				if (haveNotVisited && haveDistanceRemaining && closerToGoal) {
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


	private void buildGraph() {

		File file = new File("res/world-cities.csv");
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
					
					
					if (latitude >= MAP_SOUTH_LATITUDE && 
							latitude <= MAP_NORTH_LATITUDE &&
							longitude >= MAP_WEST_LONGITUDE &&
							longitude <= MAP_EAST_LONGITUDE &&						
							((population >= MIN_POPULATION) || (isCapital && INCLUDE_CAPITALS))) {
						CityNode city = new CityNode();
						city.name = fields[1];
						city.latitude = latitude;
						city.longitude = longitude;
						city.population = population;
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
		

		double unitsPerDegreeLatitude = translateLatitudeToLogicalY(MAP_SOUTH_LATITUDE) - translateLatitudeToLogicalY(MAP_SOUTH_LATITUDE + 1);
		double maxDistance = (unitsPerDegreeLatitude) * (MAX_DISTANCE_BETWEEN_CITIES_KM / KM_PER_DEGREE_LATITUDE);

		//add all neighbours within range
		for (int indexFrom = 0; indexFrom < cities.size(); indexFrom++) {
			CityNode from = (CityNode) cities.get(indexFrom);
			for (int indexTo = 0; indexTo < cities.size(); indexTo++) {
				try {
					CityNode to = (CityNode) cities.get(indexTo);
					double distance = findDistance(from, to);
					if (distance > 0 && distance <= maxDistance) {
						from.neighbourNodes.add(to);
						from.neighbourDistances.add(distance);
					}
				} catch (NumberFormatException e) {
				}
			}
			//reduce the number of neighbours to a maximum
			while (from.neighbourNodes.size() > MAX_NEIGHBOURS) {
				double furthestDistance = 0;
				int indexTarget = 0;
				for (int i = 0; i < from.neighbourDistances.size(); i++) {
					if (from.neighbourDistances.get(i) > furthestDistance) {
						furthestDistance = from.neighbourDistances.get(i);
						indexTarget = i;
					}
				}
				CityNode to = (CityNode) cities.get(cities.indexOf(from.neighbourNodes.get(indexTarget)));
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
			CityNode current = (CityNode) cities.get(indexCurrent);
			for (int indexNeighbour = 0; indexNeighbour < current.neighbourNodes.size(); indexNeighbour++) {
				CityNode neighbour = current.neighbourNodes.get(indexNeighbour);
				if (neighbour.neighbourNodes.indexOf(current) < 0) {
					neighbour.neighbourNodes.add(current);
				}
			}
			
		}
		
	}

	private double pathLength(ArrayList<CityNode> path) {
		double length = 0;
		for (int i = 0; i < path.size() - 1; i++) {
			CityNode city1 = path.get(i);
			CityNode city2 = path.get(i+1);
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
	
	private void printState(ArrayList<CityNode> currentPath) {
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
		return MAP_NORTH_LATITUDE;
	}

	public double getSouthLatitude() {
		return MAP_SOUTH_LATITUDE;
	}

	public double getEastLongitude() {
		return MAP_EAST_LONGITUDE;
	}

	public double getWestLongitude() {
		return MAP_WEST_LONGITUDE;
	}

	public int getMapWidth() {
		return 1050;
	}

	public int getMapHeight() {
		return 750;
	}
	
	private double findDistance(CityNode from, CityNode to) {
		double distanceX = from.getCenterX() - to.getCenterX();
		double distanceY = from.getCenterY() - to.getCenterY();					
		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);		
	}
	
	private double translateLongitudeToLogicalX(double longitude) {
		double offsetLongitude = longitude - this.getWestLongitude();
		return MAP_PIXELS_PER_DEGREE_LONGITUDE * offsetLongitude;
	}

	private double translateLatitudeToLogicalY(double latitude) {
		double length = WebMercator.latitudeToY(this.getNorthLatitude()) - WebMercator.latitudeToY(this.getSouthLatitude());
		double distance = WebMercator.latitudeToY(latitude) - WebMercator.latitudeToY(this.getSouthLatitude());
		double ratio = (distance / length);
		double pixel = this.getMapHeight() - (distance / length) * this.getMapHeight();
		return pixel;

	}
	
}
