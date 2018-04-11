package net.idg.thread;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import net.idg.GerdenServer;
import net.idg.bean.Status;

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
				GerdenServer.display("Lights Off" , "");
			}else{
				lightPin.high();
				Status.lightsOn = true;
				GerdenServer.display("Lights On" , "");
			}
			log.debug("Light thread stop. Status: " + Status.lightsOn);
			GerdenServer.getSchedManager().startLights();
		}catch (Throwable tr){
			log.error("Error in LightsThread.", tr);
			lightPin.low();
			Status.lightsOn = false;
		}
	}
} 
