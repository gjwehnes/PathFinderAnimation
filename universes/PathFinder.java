import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PathFinder {
	
	/*
	 * Solution to the path finding methods are stored within these two instance variables.
	 * In pure recursion, the solution is either the return value of the function or one of the
	 * parameters (as a mutated object). However, the intent is to see these functions in action.
	 * Thus, the recursive methods do not have a return type (they are void) and instead
	 * build / store their solutions paths in these instance variables. Thus, the (partial)
	 * solution is visible to other objects at any time. An object running on a separate thread
	 * (i.e. the animation frame) can provide a visualization of the solution in progress. 
	 */	
	protected ArrayList<Node> currentPath = new ArrayList<Node>();
	/*
	 * The currentPath is intended to be the working data structure (and will shrink / grow continuously)
	 * while the optimalPath is the best solution found (if any)
	 */
	protected ArrayList<Node> optimalPath = new ArrayList<Node>();
	
	/*
	 * constants to control behaviour of the pathfinding algorithms
	 */
	private long stepDelay = 0;
	private boolean stopAtSolution = false;	
	private boolean printSteps = false;
	private boolean printSolutions = true;
	private long timeLimit = 5000;
	
	/*
	 * variables to track current state of the pathfinding algorithms
	 */
	protected long steps = 0;
	private long currentTime = 0;
	private long endTime = Long.MAX_VALUE;
	private boolean calculating = false;		

	private boolean abort = false;

	public PathFinder(long stepDelay, boolean stopAtSolution, boolean printSteps, boolean printSolutions, long timeLimit) {
		super();
		this.stepDelay = stepDelay;
		this.stopAtSolution = stopAtSolution;
		this.printSteps = printSteps;
		this.printSolutions = printSolutions;
		this.timeLimit = timeLimit;
	}
	
	//accessors
	public boolean isCalculating() {
		return calculating;
	}
	
	public long getSteps() {
		return this.steps;
	}
	
	public long getTimeLimit() {
		return timeLimit;
	}

	//mutators
	public void abort() {
		this.abort = true;
	}
	
	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}
	
	// RECURSIVE PATH FINDING ALGORITHMS
	
	/*
	 * Find solution(s) regardless of length by recursively visiting the neighbour nodes in given order.
	 * This represents a simple "left-to-right traversal" of the solution tree
	 */
	
	public void findAnyPath(Node start, Node goal) {
		
		initialize(start);
		findAnyPath(start, goal, currentPath);
		terminate();
	}

	private void findAnyPath(Node current, Node goal, ArrayList<Node> currentPath) {

		double optimalPathLength = pathLength(optimalPath);
		double currentPathLength = pathLength(currentPath);

		takeStep();
		printState(currentPath);

		if (abort) {
			//external signal to abort calculation.
			//do not recurse, return
			return;
		} else if ((optimalPathLength > 0) && (currentPathLength >= optimalPathLength)) {
			//if solution has already been found and current path is already longer, abandon this path
			//do not recurse, return
			return;
		} else if (current == goal) {
			if ((optimalPath.size() == 0)
					|| (pathLength(currentPath) < pathLength(optimalPath))) {
				optimalPath = (ArrayList<Node>) currentPath.clone();
				printSolution(optimalPath);
			}
			//do not recurse, return			
			return;
		}
		else {
			//recursive case
			for (Node neighbour : current.neighbourNodes) {
				//do not move to a neighbour that has already been visited, to prevent circular paths
				if (currentPath.contains(neighbour) == false) {
					currentPath.add(neighbour);
					findAnyPath(neighbour, goal, currentPath);
					if (abort) {
						return;
					}
					else if (optimalPath.size() > 0 && stopAtSolution) {
						//found a solution and should stop at first solution
						return;
					}
					else {
						//do not return, continue loop and therefore continue recursion
						currentPath.remove(neighbour);
					}								
				}
			}
			return;
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
				printSolution(currentPath);
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
			printSolution(currentPath);
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
			printSolution(currentPath);
			if ((optimalPath.size() == 0) || (pathLength(currentPath) < pathLength(optimalPath))) {
				optimalPath = (ArrayList<Node>) currentPath.clone();
			}
			currentPath.remove(goal);
			return (stopAtSolution);
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
					boolean stop = findAPathPrioritizeProgression(neighbour, goal, currentPath);
					if (stop) {
						return stop;
					}
					else {
						currentPath.remove(neighbour);
					}								
				}
			}
			return false;
		}		
		
	}
	
	//utility functions
	
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
		optimalPath.clear();
		currentPath.add(start);
	}
	
	private void terminate() {
		abort = false;
		calculating = false;
		if (abort) {
			currentPath.clear();
			optimalPath.clear();
		}
		
	}
	
 	private void takeStep() {		
		//sleep until the next refresh time
		long next_refresh_time = currentTime + stepDelay;

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
	
	private void printState(ArrayList<Node> path) {
		if (printSteps) {
			System.out.println(String.format("Step: calls: %8d;  time remaining: %8d; length = %5.1f; path = %s" ,
					steps,
					endTime - currentTime,
					pathLength(currentPath),
					path.toString()));
		}		
	}	
	
	private void printSolution(ArrayList<Node> path) {
		if (printSolutions) {			
			System.out.println(String.format("Solution: calls: %8d;  time remaining: %8d; length = %5.1f; path = %s", 
					steps,
					endTime - currentTime,
					pathLength(path),
					path.toString()));
		}
	}	
}
