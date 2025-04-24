import java.awt.Image;
import java.util.ArrayList;

public class CityNode {

		protected String name = "";
		protected int size = 5;
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
		
		public CityNode(String name) {
			this.name = name;
		}
		
		public String toString() {
			return this.name;			
		}
	}
