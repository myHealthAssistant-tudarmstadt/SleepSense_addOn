package com.ess.tudarmstadt.de.sleepsense.database;

/**
 * Store four kind of data and the time value 
 * @author HieuHa
 *
 */
public class SuperFourCoordinate {

	private int db_id;
	private String timeAxis;
	private int firstId, secondId, thirdId, fourthId;
	private double firstValue, secondValue, thirdValue, fourthValue;
	
	public SuperFourCoordinate(int first, int second, int third, int fourth){
		this.firstId = first;
		this.secondId = second;
		this.thirdId = third;
		this.fourthId = fourth;
		
		firstValue = 0.0d;
		secondValue = 0.0d;
		thirdValue = 0.0d;
		fourthValue = 0.0d;
	}
	
	public SuperFourCoordinate(String time, double first, double second, double third, double fourth){
		this.timeAxis = time;
		this.firstValue = first;
		this.secondValue = second;
		this.thirdValue = third;
		this.fourthValue = fourth;

		firstValue = 0.0d;
		secondValue = 0.0d;
		thirdValue = 0.0d;
		fourthValue = 0.0d;
	}
	
	public String getTimeAxis() {
		return timeAxis;
	}
	public void setTimeAxis(String timeAxis) {
		this.timeAxis = timeAxis;
	}
	
	public void setId(int first, int second, int third, int fourth){
		this.firstId = first;
		this.secondId = second;
		this.thirdId = third;
		this.fourthId = fourth;
	}
	
	public double getValue(int id){
		if (id == firstId)  return firstValue;
		else if (id == secondId) return secondValue;
		else if (id == thirdId) return thirdValue;
		else if (id == fourthId) return fourthValue;
		
		return 0.0d;
	}
	
	public boolean setValue(int id, double value){
		if (id == firstId)  firstValue = value;
		else if (id == secondId) secondValue = value;
		else if (id == thirdId) thirdValue = value;
		else if (id == fourthId) fourthValue = value;
		else return false;
		return true;
	}
	
	@Override
	public String toString(){
		return "(" + firstId + ", " + firstValue + ")" +
				"(" + secondId + ", " + secondValue + ")"+
				"(" + thirdId + ", " + thirdValue + ")"+
				"(" + fourthId + ", " + fourthValue + ")"; 
	}

	public int getDb_id() {
		return db_id;
	}

	public void setDb_id(int db_id) {
		this.db_id = db_id;
	}
}
