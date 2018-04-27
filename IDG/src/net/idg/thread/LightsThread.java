package net.idg.thread;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import net.idg.IDGServer;
import net.idg.bean.Sensor;
import net.idg.bean.Status;
import net.idg.db.SensorSql;
import net.idg.db.entity.SensorStatus;

public class LightsThread implements Runnable{

	private static final Logger log = LogManager.getLogger(LightsThread.class);
	
	private GpioPinDigitalOutput lightPin = null;
	
	public LightsThread( GpioPinDigitalOutput lightPin ) {
		this.lightPin = lightPin;
	}
	@Override
	public void run() {
		try{
			log.debug("Light thread start. Status: " + Status.lightsOn);
			if ( Status.lightsOn){
				lightPin.low();
				Status.lightsOn = false;
				IDGServer.display("Lights Off" , "");
				addStatus(false, "Lights off");
			}else{
				lightPin.high();
				Status.lightsOn = true;
				IDGServer.display("Lights On" , "");
				addStatus(true, "Lights ON");
			}
			log.debug("Light thread stop. Status: " + Status.lightsOn);
			IDGServer.getSchedManager().startLights();
		}catch (Throwable tr){
			log.error("Error in LightsThread.", tr);
			lightPin.low();
			Status.lightsOn = false;
			addStatus(false, "error in lights thread, Lights off");
		}
	}
	private void addStatus(boolean on, String message ) {
		SensorStatus stat = new SensorStatus();
		SensorSql sql = new SensorSql();
		
		stat.setComment(message);
		stat.setSensor(Sensor.LIGHT);
		stat.setRecordedDate(new Date());
		stat.setField1(String.valueOf(on));
		
		try {
			sql.add(stat);
		} catch (Exception e) {
			log.error("Error writing to DB", e);
		} 
	}
} 
