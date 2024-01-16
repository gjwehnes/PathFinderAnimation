
public class PathFinderAnimation implements Animation {

	private int universeCount = 0;
	private Universe current = null;
	private static int score = 0;
	
	public static int getScore() {
		return score;
	}

	public static void setScore(int score) {
		PathFinderAnimation.score = score;
	}

	public static void addScore(int score) {
		PathFinderAnimation.score += score;
	}

	public int getLevel() {
		return universeCount;
	}
	
	public Universe getNextUniverse() {

		universeCount++;
		
		if (universeCount == 1) {
			this.current = new EuropeMap();
		}
		else if (universeCount == 2) {
			this.current = new NorthAmericaMap();
		}

		else {
			this.current = null;
		}
		
		return this.current;

	}

	public Universe getCurrentUniverse() {
		return this.current;
	}
		
	public void restart() {
		universeCount = 0;
		current = null;
		score = 0;		
	}
	
}
