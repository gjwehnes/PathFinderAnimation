import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PathFinder {
		
	protected ArrayList<Node> currentPath = new ArrayList<Node>();
	protected ArrayList<Node> optimalPath = new ArrayList<Node>();
	
	private final long STEP_DELAY_MS = 0;
	private final boolean OPTIMIZE = true;
	private final boolean VERBOSE = false;
			
	protected long steps = 0;
	private long currentTime = 0;
	private long endTime = Long.MAX_VALUE;
	private boolean abort = false;
	private boolean calculating = false;		
	private long timeLimit = 5000;
	
	public void abort() {
		this.abort = true;
	}
	
	public boolean isCalculating() {
		return calculating;
	}
	
	public long getSteps() {
		return this.steps;
	}
	
	public long getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}
		
	//Return the first solution, regardless of length and by visiting the neighbour nodes in given order
	public ArrayList<Node> findAnyPath(Node start, Node goal) {
		
		initialize(start);

		boolean result = findAnyPath(start, goal, currentPath);
		
		if (result == false || abort) {
			currentPath.clear();
		}
		
		abort = false;
		calculating = false;
		return currentPath;
	}

	private boolean findAnyPath(Node current, Node goal, ArrayList<Node> currentPath) {

		takeStep();
		printState(currentPath);

		double optimalPathLength = pathLength(optimalPath);
		double currentPathLength = pathLength(currentPath);

		if (abort) {
			return false;
		} else if ((optimalPathLength > 0) && (currentPathLength > optimalPathLength)) {
			return false;
		}else if (current == goal) {			
			System.out.println(String.format("Solution: have a cup of tea, you are already there :-)"));
			return true;
		}
		else if (current.neighbourNodes.contains(goal)) {
			//base case
			currentPath.add(goal);
			printSolution();
			if ((optimalPath.size() == 0) || (pathLength(currentPath) < pathLength(optimalPath))) {
				optimalPath = (ArrayList<Node>) currentPath.clone();
			}
			currentPath.remove(goal);
			return (OPTIMIZE == false);
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

		initialize(start);
		
		double additionalDistance = findDistance(start,goal) * (additionalDistancePercent / 100);		
		currentPath.add(start);

		boolean result = findAPathLengthLimited(start, goal, currentPath, findDistance(start,goal) + additionalDistance);
		
		if (result == false || abort) {
			currentPath.clear();
		}

		abort = false;
		calculating = false;
		return currentPath;
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
				printSolution();
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

		initialize(start);

		boolean result = findAPathNoRegression(start, goal, currentPath);
		
		if (result == false || abort) {
			currentPath.clear();
		}
		
		abort = false;
		calculating = false;
		return currentPath;
	}
	
	private boolean findAPathNoRegression(Node current, Node goal, ArrayList<Node> currentPath) {

		takeStep();
		printState(currentPath);

		double distanceOfCurrentPath = pathLength(currentPath);

		if (abort) {
			return false;
		}else if (current == goal) {			
			currentPath.add(goal);
			printSolution();
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

		initialize(start);

		boolean result = findAPathPrioritizeProgression(start, goal, currentPath);
		
		if (result == false || abort) {
			currentPath.clear();
		}
		
		abort = false;
		calculating = false;
		return currentPath;
	}

	private boolean findAPathPrioritizeProgression(Node current, Node goal, ArrayList<Node> currentPath) {

		takeStep();
		printState(currentPath);

		double optimalPathLength = pathLength(optimalPath);
		double currentPathLength = pathLength(currentPath);
		
		if (abort) {
			return false;
		} else if ((optimalPathLength > 0) && (currentPathLength > optimalPathLength)) {
			return false;
		}else if (current == goal) {			
			//base case
			currentPath.add(goal);
			printSolution();
			if ((optimalPath.size() == 0) || (pathLength(currentPath) < pathLength(optimalPath))) {
				optimalPath = (ArrayList<Node>) currentPath.clone();
			}
			currentPath.remove(goal);
			return (OPTIMIZE == false);
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
	
	private void initialize(Node start) {
		calculating = true;
		steps = 0;
		currentTime = System.currentTimeMillis();
		endTime = currentTime + timeLimit;
		currentPath.clear();
		currentPath.add(start);
	}
	
	private void takeStep() {		
		//sleep until the next refresh time
		long next_refresh_time = currentTime + STEP_DELAY_MS;

		currentTime = System.currentTimeMillis();
		if (currentTime > endTime) {
			abort = true;
		}
			
		//sleep until the next refresh time
		while (currentTime < next_refresh_time)
		{
			//allow other threads (i.e. the Swing thread) to do its work
			Thread.yield();

			try {
				Thread.sleep(1);
			}
			catch(Exception e) {    					
			} 

			//track current time
			currentTime = System.currentTimeMillis();
		}		
		this.steps++;
	}
	
	private void printState(ArrayList<Node> currentPath) {
		if (VERBOSE) {
			System.out.println(String.format("Step: calls: %8d;  time: %8d; length = %5.1f; path = %s" ,
					steps,
					endTime - currentTime,
					pathLength(currentPath),
					currentPath.toString()));
		}		
	}	
	
	private void printSolution() {
		System.out.println(String.format("Solution: calls: %8d;  time: %8d; length = %5.1f; path = %s", 
				steps,
				endTime - currentTime,
				pathLength(currentPath),
				currentPath.toString()));
	}
	
}
