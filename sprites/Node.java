import java.awt.Image;
import java.util.ArrayList;

public class Node {

		protected String name = "";
		protected int size = 5;
		protected ArrayList<Node> neighbourNodes = new ArrayList<Node>();
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
		
		public Node() {			
		}
		
		public Node(String name) {
			this.name = name;
		}
		
		public String toString() {
			return this.name;			
		}
	}
