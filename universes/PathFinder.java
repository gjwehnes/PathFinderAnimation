import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PathFinder {
		
	private ArrayList<Node> nodes = new ArrayList<Node>();
	protected ArrayList<Node> path = new ArrayList<Node>();

	private final long STEP_DELAY_MS = 0;
	private final boolean VERBOSE = false;
			
	protected long steps = 0;
	private long current_time = 0;
	private boolean abort = false;
	private boolean calculating = false;		

	public void abort() {
		this.abort = true;
	}
	
	public boolean isCalculating() {
		return calculating;
	}
	
	public long getSteps() {
		return this.steps;
	}
		
	//Return the first solution, regardless of length and by visiting the neighbour nodes in given order
	public ArrayList<Node> findAnyPath(Node start, Node goal) {

		calculating = true;
		steps = 0;
		path.clear();
		path.add(start);

		boolean result = findAnyPath(start, goal, path);
		
		if (result == false || abort) {
			path.clear();
		}
		
		abort = false;
		calculating = false;
		return path;
	}

	private boolean findAnyPath(Node current, Node goal, ArrayList<Node> currentPath) {

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
					boolean solved = findAnyPath(neighbour, goal, currentPath);
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


	//Track the distance used, and compare it to a maximum; don't pursue paths that will be longer
	//This assumes that we can calculate how long the optimal path would be.
	public ArrayList<Node> findAPathLengthLimited(Node start, Node goal, double additionalDistancePercent) {

		calculating = true;
		steps = 0;
		path.clear();
		path.add(start);
		
		double additionalDistance = findDistance(start,goal) * (additionalDistancePercent / 100);		
		path.add(start);

		boolean result = findAPathLengthLimited(start, goal, path, findDistance(start,goal) + additionalDistance);
		
		if (result == false || abort) {
			path.clear();
		}

		abort = false;
		calculating = false;
		return path;
	}
	
	private boolean findAPathLengthLimited(Node current, Node goal, ArrayList<Node> currentPath, double distanceLeft) {

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
					boolean solved = findAPathLengthLimited(neighbour, goal, currentPath, distanceLeft - distanceToNeighbour);
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

	//Only visit neighbour nodes that themselves are closer to the goal.
	//This assumes that we know the distance from any node to the destination
	public ArrayList<Node> findAPathNoRegression(Node start, Node goal) {

		calculating = true;
		steps = 0;
		path.clear();
		path.add(start);

		boolean result = findAPathNoRegression(start, goal, path);
		
		if (result == false || abort) {
			path.clear();
		}
		
		abort = false;
		calculating = false;
		return path;
	}
	
	private boolean findAPathNoRegression(Node current, Node goal, ArrayList<Node> currentPath) {

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
					boolean solved = findAPathNoRegression(neighbour, goal, currentPath);
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

	//Visit first those neighbour nodes that themselves are closer to the goal.
	//This requires the algorithm to sort the list of neighbours
	public ArrayList<Node> findAPathPrioritizeProgression(Node start, Node goal) {

		calculating = true;
		steps = 0;
		path.clear();
		path.add(start);

		boolean result = findAPathPrioritizeProgression(start, goal, path);
		
		if (result == false || abort) {
			path.clear();
		}
		
		abort = false;
		calculating = false;
		return path;
	}

	private boolean findAPathPrioritizeProgression(Node current, Node goal, ArrayList<Node> currentPath) {

		takeStep();
		printState(currentPath);

		if (abort) {
			return false;
		}else if (current == goal) {			
			currentPath.add(goal);
			System.out.println(String.format("Solution: calls: %8d; length = %5.1f; path = %s" , steps, pathLength(currentPath), currentPath.toString()));
			return true;
		}
		else {
			//clone the neighbours, and compute their distances to the goal
			ArrayList<Node> orderedNeighbours = (ArrayList<Node>) current.neighbourNodes.clone();
			ArrayList<Double> orderedDistances = new ArrayList<Double>(orderedNeighbours.size());
			for (int i = 0; i < orderedNeighbours.size(); i++) {
				orderedDistances.add(findDistance(orderedNeighbours.get(i), goal));
			}
			//perform an insertion sort algorithm
			for (int i = 1; i < orderedDistances.size(); ++i) {
				Node node = orderedNeighbours.get(i);
				Double key = orderedDistances.get(i);
				int j = i - 1;
				while (j >= 0 && orderedDistances.get(j) > key) {
					orderedNeighbours.set(j + 1, orderedNeighbours.get(j));
					orderedDistances.set(j + 1, orderedDistances.get(j));
					j = j - 1;
				}
				orderedNeighbours.set(j + 1, node);
				orderedDistances.set(j + 1, key);
			}
			//now visit each node in order
			for (Node neighbour : orderedNeighbours) {
				boolean haveNotVisited =currentPath.contains(neighbour) == false;
				if (haveNotVisited) {
					currentPath.add(neighbour);
					boolean solved = findAPathPrioritizeProgression(neighbour, goal, currentPath);
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
	
	private double findDistance(Node from, Node to) {
		double distanceX = from.getCenterX() - to.getCenterX();
		double distanceY = from.getCenterY() - to.getCenterY();					
		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);		
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
	
}
