import javax.swing.*;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseMotionAdapter;


public class PathFinderFrame extends AnimationFrame{

	private enum State {
		  READY,
		  DRAGGING,
		  CALCULATING,
		  ABORTING
		  		}

	JComboBox cboStart;
	JComboBox cboEnd;
	JComboBox cboAlgorithm;
	Node current = null;
	Node start = null;
	Node end = null;
	long calculationStart = 0;
	long calculationTime = 0;
	ArrayList<Node> currentPath = null;
	ArrayList<Node> optimalPath = null;
	Thread calculationThread = null;	
	State state = State.READY;
	Graph graph = null;
	PathFinder pathfinder = null;
	
	public PathFinderFrame(Animation animation) {
		
		super(animation);
		
		lblTop.setForeground(Color.YELLOW);
		lblBottom.setForeground(Color.YELLOW);
		
		cboAlgorithm = new JComboBox();
		cboAlgorithm.setBounds(480,20,192,32);
		cboAlgorithm.addItem("Any Solution");
		cboAlgorithm.addItem("No Regression");
		cboAlgorithm.addItem("Prioritize Progression");		
		cboAlgorithm.addItem("Path Length Limited 200%");
		cboAlgorithm.addItem("Path Length Limited 180%");
		cboAlgorithm.addItem("Path Length Limited 160%");
		cboAlgorithm.addItem("Path Length Limited 140%");
		cboAlgorithm.addItem("Path Length Limited 120%");
		cboAlgorithm.setFocusable(false);
		getContentPane().add(cboAlgorithm);
		getContentPane().setComponentZOrder(cboAlgorithm, 0);
		
		cboAlgorithm.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (e.getStateChange() == ItemEvent.SELECTED) {
				}
			}
		});
		
		graph = (Graph)universe;
		pathfinder = new PathFinder(0, true, true, true, 5000);
	}
		
	protected void paintAnimationPanel(Graphics g) {
	
		graph = (Graph)universe;
		
		if (universe instanceof MazeUniverse) {
			optimalPath = null;
			optimalPath = null;
		}
		else {
			currentPath = pathfinder.currentPath;
			optimalPath = pathfinder.optimalPath;
		}
		
		for (Node sprite: graph.getNodes()) {
			Node city = (Node)sprite;
			int size = (int) (city.size * scale);
			g.setColor(Color.RED);
			g.fillOval(translateToScreenX(city.getCenterX()) - size / 2, translateToScreenY(city.getCenterY()) - size / 2, (int)(size), (int)(size));				
			Graphics2D g2 = (Graphics2D) g;
			g.setColor(Color.WHITE);
		    g2.drawString(city.name, translateToScreenX(city.getCenterX()) + 8, translateToScreenY(city.getCenterY()));

			g.setColor(Color.GRAY);
		    g2.setStroke(new BasicStroke(2));				
			for (Node neighbour: city.neighbourNodes) {
			    g2.drawLine(translateToScreenX(city.getCenterX()), translateToScreenY(city.getCenterY()), translateToScreenX(neighbour.getCenterX()), translateToScreenY(neighbour.getCenterY()));
				
			}
        	
        }

		g.setColor(Color.MAGENTA);
		
		if (currentPath != null && currentPath.size() >= 1) {
			try {
				//this code may fail as the list may be modified asynchronously
				for (int i = 0; i < currentPath.size() - 1; i++) {
					Node cityA = currentPath.get(i);
					Node cityB = currentPath.get(i+1);
					
					Graphics2D g2 = (Graphics2D) g;
				    g2.setStroke(new BasicStroke(5));				
				    g2.drawLine(translateToScreenX(cityA.getCenterX()), translateToScreenY(cityA.getCenterY()), translateToScreenX(cityB.getCenterX()), translateToScreenY(cityB.getCenterY()));
				    g2.drawString(cityA.name, translateToScreenX(cityA.getCenterX()), translateToScreenY(cityA.getCenterY()));
				}
			} catch (Exception e) {
				//abort this rendering
			}
		}

		g.setColor(Color.WHITE);
		
		if (optimalPath != null && optimalPath.size() >= 1) {
			try {
				//this code may fail as the list may be modified asynchronously
				for (int i = 0; i < optimalPath.size() - 1; i++) {
					Node cityA = optimalPath.get(i);
					Node cityB = optimalPath.get(i+1);
					
					Graphics2D g2 = (Graphics2D) g;
				    g2.setStroke(new BasicStroke(5));				
				    g2.drawLine(translateToScreenX(cityA.getCenterX()), translateToScreenY(cityA.getCenterY()), translateToScreenX(cityB.getCenterX()), translateToScreenY(cityB.getCenterY()));
				    g2.drawString(cityA.name, translateToScreenX(cityA.getCenterX()), translateToScreenY(cityA.getCenterY()));
				}
			} catch (Exception e) {
				//abort this rendering
			}
		}
		
	}
	
	protected void updateControls() {

		graph = (Graph)universe;
		
		current = null;
		double minDistanceSquared = Double.MAX_VALUE;
		
		//find the closes node to the mouse pointer
		for (Node sprite: graph.getNodes()) {
			double distanceX = sprite.getCenterX() - MouseInput.logicalX;
			double distanceY = sprite.getCenterY() - MouseInput.logicalY;
			double distanceSquared = distanceX * distanceX + distanceY * distanceY;
			
			if (distanceSquared < minDistanceSquared) {
				current = (Node) sprite;
				minDistanceSquared = distanceSquared;
			}
		}

		
		switch(state) {
			case READY:
				if (MouseInput.rightButtonDown) {
					this.currentPath.clear();
				}
				if (MouseInput.leftButtonDown) {
					state = State.DRAGGING;
					start = current;					
				}
				break;
			case DRAGGING:
				if (MouseInput.leftButtonDown == false) {
					state = State.CALCULATING;
					end = current;
					findPath(start, end);
				} else {
					//can we do a 'live' solution?
					//has the current changed?
					if (end != current) {
						if (pathfinder.isCalculating()) {
							pathfinder.abort();
						}
						else {
							end = current;
							findPath(start, end);
						}
					}
				}
				break;
			case CALCULATING:
				if (pathfinder.isCalculating() == false) {
					state = State.READY;
				}
				else {
					if (MouseInput.rightButtonDown) {
						state = State.ABORTING;
						pathfinder.abort();
					}
				}
				break;
			case ABORTING:
				if (pathfinder.isCalculating() == false) {
					state = State.READY;
				}
				else {
					state = State.ABORTING;
				}
				break;
			default:
				// should not happen
		}		
		
		setPathText();
		
		if (universe instanceof MazeUniverse) {
			this.lblBottom.setText(universe.toString());
		}
		
	}
	
	private void setSolutionText() {
		
		if (currentPath.size() == 0) {
			this.lblBottom.setText(String.format("SOLUTION %s to %s not found", 
					start != null ? start.name : "-", 
							end != null ? end.name : "-",
									(System.currentTimeMillis() - calculationStart) / 1000.0,
									pathfinder.getSteps()));
		}
		else if (currentPath.size() == 1) {
			this.lblBottom.setText("");
		}
		else {
			
			if (pathfinder.isCalculating() == false) {
				this.lblBottom.setText(String.format("SOLUTION %s to %s (%5.3f s, %d steps)", 
						start != null ? start.name : "-", 
								end != null ? end.name : "-",
										(calculationTime) / 1000.0,
										pathfinder.getSteps()));				
			}
			else {
				this.lblBottom.setText("CALCULATING");
			}
		}
		
	}
	
	private void setPathText() {
		
		switch(state) {
			case READY:
				this.lblTop.setText(String.format("READY %s", current != null ? current.name : "-"));
//				this.lblBottom.setText(this.lblBottom.getText());
				break;
			case DRAGGING:
				this.lblTop.setText(String.format("DRAGGING %s to %s", start != null ? start.name : "-", current != null ? current.name : "-"));					
//				this.lblBottom.setText(this.lblBottom.getText());
				break;
			case CALCULATING:
				this.lblTop.setText(String.format("CALCULATING %s to %s (%5.3f s)", start != null ? start.name : "-", end != null ? end.name : "-", (System.currentTimeMillis() - calculationStart) / 1000.0));
//				this.lblBottom.setText(this.lblBottom.getText());
				break;
			case ABORTING:
				this.lblTop.setText(String.format("ABORTING!!!"));
//				this.lblBottom.setText(this.lblBottom.getText());
				break;
			default:
				// should not happen
		}		
		
	}
	
	protected void findPath(Node start, Node end) {

		calculationThread = new Thread()
		{
			public void run()
			{
				if (pathfinder.isCalculating()) { 
					//calculation is still running....abort
					pathfinder.abort();
				}
								
				calculationStart = System.currentTimeMillis();
				if (cboAlgorithm.getSelectedIndex() == 0) {				
					pathfinder.findAnyPath(start,end);
				}				
				else if (cboAlgorithm.getSelectedIndex() == 1) {
					pathfinder.findAPathNoRegression(start,end);  //TEMP
				}
				else if (cboAlgorithm.getSelectedIndex() == 2) {
					pathfinder.findAPathPrioritizeProgression(start,end);  //TEMP
				}
				else if (cboAlgorithm.getSelectedIndex() >= 3 && cboAlgorithm.getSelectedIndex() <= 6) {
					int percentage = (8 - cboAlgorithm.getSelectedIndex()) * 20;
					pathfinder.findAPathLengthLimited(start,end, percentage);
				}
				calculationTime = System.currentTimeMillis() - calculationStart;
				setSolutionText();
			}
		};
	
		calculationThread.start();
				
	}

	protected void this_windowClosing(WindowEvent e) {
		pathfinder.abort();
		super.this_windowClosing(e);
	}
	
}
