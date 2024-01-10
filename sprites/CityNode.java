import java.awt.Image;
import java.util.ArrayList;

public class CityNode {

		protected String name = "";
		protected double latitude = 0;
		protected double longitude = 0;
		protected int population = 0;
		protected ArrayList<CityNode> neighbourNodes = new ArrayList<CityNode>();
		protected ArrayList<Double> neighbourDistances = new ArrayList<Double>();

		public double getCenterX() {
			return centerX;
		}

		public void setCenterX(double centerX) {
			this.centerX = centerX;
		}

		public double getCenterY() {
			return centerY;
		}

		public void setCenterY(double centerY) {
			this.centerY = centerY;
		}

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
	}
