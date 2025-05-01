
public class PathFinderAnimation implements Animation {

	private int universeCount = 0;
	private Universe current = null;
	private static int score = 0;
	private boolean animationComplete = false;
	private boolean universeSwitched = false;
	
	public PathFinderAnimation() {
		switchUniverse(null);
		universeSwitched = false;
	}
	
	public Universe switchUniverse(Object event) {

		universeCount++;
		
		if (universeCount == 1) {
			this.current = new RandomGraph();
		}
		else if (universeCount == 2) {
			this.current = new SearchTreeVisualization();
		}
		else if (universeCount == 3) {
			this.current = new NorthAmericaMap();
		}
		else if (universeCount == 4) {
			this.current = new EuropeMap();
		}
		else {
			this.current = null;
			this.animationComplete = true;
		}
		
		universeSwitched = true;
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
	
	@Override
	public boolean getUniverseSwitched() {
		return universeSwitched;
	}

	@Override
	public void acknowledgeUniverseSwitched() {
		this.universeSwitched = false;		
	}

	@Override
	public boolean isComplete() {
		return animationComplete;
	}

	@Override
	public void setComplete(boolean complete) {
		this.animationComplete = true;		
	}

	@Override
	public void update(AnimationFrame frame, long actual_delta_time) {
		if (KeyboardInput.getKeyboard().keyDownOnce(27)) {
			switchUniverse(null);
		}		
	}
	
}
