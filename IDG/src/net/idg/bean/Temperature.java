package net.idg.bean;

import java.util.Date;

public class Temperature {

	private boolean tempValidValue = false;
	private String temp = "0";
	private String humidity = "0";
	private Date lastUpdated = null;

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
	
	public double getTempDouble() {
		return this.temp != null && this.temp.length() > 0 ? Double.parseDouble(this.temp) : 0.0d; 
	}	
	public double getHumidityDouble() {
		return this.humidity != null && this.humidity.length() > 0 ? Double.parseDouble(this.humidity) : 0.0d; 
	}

	public boolean isTempValidValue() {
		return tempValidValue;
	}

	public void setTempValidValue(boolean tempValidValue) {
		this.tempValidValue = tempValidValue;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		return "Temperature [tempValidValue=" + tempValidValue + ", temp=" + temp + ", humidity=" + humidity + "]";
	}


}
