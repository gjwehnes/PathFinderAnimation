import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MapBackground implements Background {

    private Image map;
    private int backgroundWidth = 0;
    private int backgroundHeight = 0;

    public MapBackground(String imagePath) {
    	try {
    		this.map = ImageIO.read(new File(imagePath));
    		backgroundWidth = map.getWidth(null);
    		backgroundHeight = map.getHeight(null);
    		
    	}
    	catch (IOException e) {
    		//System.out.println(e.toString());
    	}
    }
	
	public Tile getTile(int col, int row) {
		//row is an index of tiles, with 0 being the at the origin
		//col is an index of tiles, with 0 being the at the origin
		
		int x = (col * backgroundWidth);
		int y = (row * backgroundHeight);

		if (col == 0 && row == 0) {						
			return new Tile(map, x, y, backgroundWidth, backgroundHeight, false);
		}
		else {
			return new Tile(null, x, y, backgroundWidth, backgroundHeight, false);			
		}
		
	}
	
	public int getCol(double x) {
		//which col is x sitting at?
		int col = 0;
		if (backgroundWidth != 0) {
			col = (int) (x / backgroundWidth);
			if (x < 0) {
				return col - 1;
			}
			else {
				return col;
			}
		}
		else {
			return 0;
		}
	}
	
	public int getRow(double y) {
		//which row is y sitting at?
		int row = 0;
		
		if (backgroundHeight != 0) {
			row = (int) (y / backgroundHeight);
			if (y < 0) {
				return row - 1;
			}
			else {
				return row;
			}
		}
		else {
			return 0;
		}
	}
	
	@Override
	public double getShiftX() {
		return 0;
	}

	@Override
	public double getShiftY() {
		return 0;
	}

	@Override
	public void setShiftX(double shiftX) {
		//ignore
	}

	@Override
	public void setShiftY(double shiftY) {
		//ignore
	}

}


