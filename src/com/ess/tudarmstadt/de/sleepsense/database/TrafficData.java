package com.ess.tudarmstadt.de.sleepsense.database;

public class TrafficData {
	private String trafficDate;
	private int trafficType;
	private int trafficId;
	private double xValue;
	private double yValue;
	private double extra;
	
	
	public TrafficData(String trafficDate, int trafficType, double xValue,
			double yValue) {
		this.trafficDate = trafficDate;
		this.trafficType = trafficType;
		this.xValue = xValue;
		this.yValue = yValue;
		this.extra = 0.0d;
	}
	public String getTrafficDate() {
		return trafficDate;
	}
	public void setTrafficDate(String trafficDate) {
		this.trafficDate = trafficDate;
	}
	public int getTrafficType() {
		return trafficType;
	}
	public void setTrafficType(int trafficType) {
		this.trafficType = trafficType;
	}
	public double getxValue() {
		return xValue;
	}
	public void setxValue(double xValue) {
		this.xValue = xValue;
	}
	public double getyValue() {
		return yValue;
	}
	public void setyValue(double yValue) {
		this.yValue = yValue;
	}
	@Override
	public String toString(){
		return "[" + trafficDate + "--" + trafficType + "--" + xValue + "--" + yValue + "; extra:" + extra + "]";
	}
	public double getExtra() {
		return extra;
	}
	public void setExtra(double extra) {
		this.extra = extra;
	}
	public int getTrafficId() {
		return trafficId;
	}
	public void setTrafficId(int trafficId) {
		this.trafficId = trafficId;
	}
}
