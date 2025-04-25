import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GraphBuilder {	
	
	final static double KM_PER_DEGREE_LATITUDE = 110;
				
	public static ArrayList<Node> buildGraphFromCitiesDataset(double northLatitude, double southLatitude, double eastLongitude, double westLongitude, 
			double minPopulation, boolean includeCapitals, double maxDistanceBetweenNeighbours, int maxNeighbours,
			int mapWidth, int mapHeight, String datasetPath) {

		double unitsPerDegreeLatitude = translateLatitudeToLogicalY(southLatitude, northLatitude, southLatitude, mapHeight) - translateLatitudeToLogicalY(southLatitude + 1, northLatitude, southLatitude, mapHeight);
		double maxDistance = (unitsPerDegreeLatitude) * (maxDistanceBetweenNeighbours / KM_PER_DEGREE_LATITUDE);
		double unitsPerDegreeLongitude = Math.abs(mapWidth / (westLongitude - eastLongitude));
		
		ArrayList<Node> cities = new ArrayList<Node>();
		
		File file = new File(datasetPath);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			return cities;
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
					
					
					if (latitude >= southLatitude && 
							latitude <= northLatitude &&
							longitude >= westLongitude &&
							longitude <= eastLongitude &&						
							((population >= minPopulation) || (isCapital && includeCapitals))) {
						Node city = new Node();
						city.name = fields[1];
						city.size = (int) Math.max(2, Math.log(population / 100000) / Math.log(2));					
						city.centerX = translateLongitudeToLogicalX(longitude, westLongitude, unitsPerDegreeLongitude);
						city.centerY = translateLatitudeToLogicalY(latitude, northLatitude, southLatitude, mapHeight);
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
		


		//add all neighbours within range
		for (int indexFrom = 0; indexFrom < cities.size(); indexFrom++) {
			Node from = (Node) cities.get(indexFrom);
			for (int indexTo = 0; indexTo < cities.size(); indexTo++) {
				try {
					Node to = (Node) cities.get(indexTo);
					double distance = findDistance(from, to);
					if (distance > 0 && distance <= maxDistance) {
						from.neighbourNodes.add(to);
						from.neighbourDistances.add(distance);
					}
				} catch (NumberFormatException e) {
				}
			}
			//reduce the number of neighbours to a maximum
			while (from.neighbourNodes.size() > maxNeighbours) {
				double furthestDistance = 0;
				int indexTarget = 0;
				for (int i = 0; i < from.neighbourDistances.size(); i++) {
					if (from.neighbourDistances.get(i) > furthestDistance) {
						furthestDistance = from.neighbourDistances.get(i);
						indexTarget = i;
					}
				}
				Node to = (Node) cities.get(cities.indexOf(from.neighbourNodes.get(indexTarget)));
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
			Node current = (Node) cities.get(indexCurrent);
			for (int indexNeighbour = 0; indexNeighbour < current.neighbourNodes.size(); indexNeighbour++) {
				Node neighbour = current.neighbourNodes.get(indexNeighbour);
				if (neighbour.neighbourNodes.indexOf(current) < 0) {
					neighbour.neighbourNodes.add(current);
				}
			}
			
		}
		return cities;
		
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
	
	private static double translateLongitudeToLogicalX(double longitude, double westLongitude, double unitsPerDegreeLongitude) {
		double offsetLongitude = longitude - westLongitude;
		return unitsPerDegreeLongitude * offsetLongitude;
	}

	private static double translateLatitudeToLogicalY(double latitude, double northLatitude, double southLatitude, int mapHeight) {
		double length = WebMercator.latitudeToY(northLatitude) - WebMercator.latitudeToY(southLatitude);
		double distance = WebMercator.latitudeToY(latitude) - WebMercator.latitudeToY(southLatitude);
		double ratio = (distance / length);
		double pixel = mapHeight - (distance / length) * mapHeight;
		return pixel;

	}
	
	private static double findDistance(Node from, Node to) {
		double distanceX = from.getCenterX() - to.getCenterX();
		double distanceY = from.getCenterY() - to.getCenterY();					
		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);		
	}
}
