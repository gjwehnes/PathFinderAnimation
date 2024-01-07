import java.awt.Image;
import java.util.ArrayList;

public class CityNode implements DisplayableSprite {

		protected String name = "";
		protected double latitude = 0;
		protected double longitude = 0;
		protected int population = 0;
		protected ArrayList<CityNode> neighbourNodes = new ArrayList<CityNode>();
		protected ArrayList<Double> neighbourDistances = new ArrayList<Double>();

		protected double centerX = 0;
		protected double centerY = 0;
		
		public CityNode() {			
		}
		
		public CityNode(String name, double latitude, double longitude) {
			this.name = name;
			this.latitude = latitude;
			this.longitude = longitude;
		}
		
		public String toString() {
			//return String.format("CityNode:%s (pop: %d)", this.name, this.population);			
			return this.name;			
		}

		@Override
		public Image getImage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean getVisible() {
			return false;
		}

		@Override
		public double getMinX() {
			return centerX;
		}

		@Override
		public double getMaxX() {
			return centerX;
		}

		@Override
		public double getMinY() {
			return centerY;
		}

		@Override
		public double getMaxY() {
			return centerY;
		}

		@Override
		public double getHeight() {
			return 0;
		}

		@Override
		public double getWidth() {
			return 0;
		}

		@Override
		public double getCenterX() {
			return centerX;
		}

		@Override
		public double getCenterY() {
			return centerY;
		}

		@Override
		public boolean getDispose() {
			return false;
		}

		@Override
		public void setDispose(boolean dispose) {
		}

		@Override
		public void update(Universe universe, KeyboardInput keyboard, long actual_delta_time) {
		}

	}
