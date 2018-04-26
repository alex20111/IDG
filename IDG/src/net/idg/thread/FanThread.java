package net.idg.thread;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import net.idg.GerdenServer;
import net.idg.bean.Status;
import net.idg.bean.Temperature;
import net.idg.db.entity.Config;

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
			Config cfg = GerdenServer.getConfig();
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
			log.debug("Turning fan on. Humidity TooHigh: " + hunidityTooHigh + " Heat Too High: " + heatTooHigh + " Air Circulation: " + airCirculation);
		}else if(!hunidityTooHigh && !heatTooHigh && !airCirculation && Status.fanOn){
			Status.fanOn = false;
			fanPin.low();
			log.debug("turning fan off");
		}
		
		
	} 
} 