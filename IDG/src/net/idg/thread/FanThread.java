package net.idg.thread;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import net.idg.IDGServer;
import net.idg.bean.Sensor;
import net.idg.bean.Status;
import net.idg.bean.Temperature;
import net.idg.db.SensorSql;
import net.idg.db.entity.Config;
import net.idg.db.entity.SensorStatus;

public class FanThread implements Runnable {
	private static final Logger log = LogManager.getLogger(FanThread.class);
	
	private GpioPinDigitalOutput fanPin = null;
	
	private static boolean hunidityTooHigh = false;
	private static boolean heatTooHigh = false;
	private static boolean airCirculation = false;

	public FanThread(){} 

	public FanThread(GpioPinDigitalOutput fanPin){
		this.fanPin = fanPin;
	}
	@Override
	public void run() { 
		

		Temperature temp = TempThread.getTemp();

		//
		if (temp != null ) {

			
				if (temp.getHumidityDouble() > 50 ) {//&&  !Status.fanOn) {
					//turn fan on
//					Status.fanOn = true;
//					fanPin.high();
					hunidityTooHigh = true;
					log.debug("Humidity high: " + temp.getHumidity());
				}else if ( temp.getHumidityDouble() < 45 ) {//&& Status.fanOn ) {
//					Status.fanOn = false;
//					fanPin.low();
//					log.debug("Humidity normal : " + temp.getHumidity());
					hunidityTooHigh = false;
			
			}

			//check heat
			Config cfg = IDGServer.getConfig();
			if (cfg.isEnableTempMon() ) {
			
					if (temp.getTempDouble() >  (cfg.getMaintainTempAt() + 2) ) {//&& !Status.fanOn) {
//						Status.fanOn = true;
//						fanPin.high();
						log.debug("Temperature high: " + temp.getTemp());
						heatTooHigh = true;
					}else if(temp.getTempDouble() <  cfg.getMaintainTempAt() ) {//  && Status.fanOn){
//						Status.fanOn = false;
//						fanPin.low();
//						log.debug("Temperature normal: " + temp.getTemp());
						heatTooHigh = false;
					}
				}
			
		}


		if ( (hunidityTooHigh || heatTooHigh || airCirculation) && !Status.fanOn) {
			Status.fanOn = true;
			fanPin.high();
			String msg = "Turning fan on. Humidity TooHigh: " + hunidityTooHigh + " Heat Too High: " + heatTooHigh + " Air Circulation: " + airCirculation;
			addStatus(true,msg );
			log.debug(msg);
		}else if(!hunidityTooHigh && !heatTooHigh && !airCirculation && Status.fanOn){
			Status.fanOn = false;
			fanPin.low();
			String msg = "Turning fan Off";
			addStatus(false,msg );
			log.debug(msg);
		}
		
		
	} 
	private void addStatus(boolean on, String message ) {
		SensorStatus stat = new SensorStatus();
		SensorSql sql = new SensorSql();
		
		stat.setComment(message);
		stat.setSensor(Sensor.FAN);
		stat.setRecordedDate(new Date());
		stat.setField1(String.valueOf(on));
		
		try {
			sql.add(stat);
		} catch (Exception e) {
			log.error("Error writing to DB", e);
		} 
	}
	
} 