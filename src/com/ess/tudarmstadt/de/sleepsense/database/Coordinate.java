package com.ess.tudarmstadt.de.sleepsense.database;

/**
 * Store data in x,y-Axis for use in graph 
 * @author HieuHa
 *
 */
public class Coordinate {

	private String xAxis;
	private double yAxis;
	public Coordinate(String xAxis, double yAxis){
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	public String getX() {
		return xAxis;
	}
	public void setX(String xAxis) {
		this.xAxis = xAxis;
	}
	public double getY() {
		return yAxis;
	}
	public void setY(double yAxis) {
		this.yAxis = yAxis;
	}
	
	@Override
	public String toString(){
		return "[Coor:" + xAxis + "-"+ yAxis + "]";
	}
}
