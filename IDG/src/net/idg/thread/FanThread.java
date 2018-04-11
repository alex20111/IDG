package net.idg.thread;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import net.idg.bean.Status;

public class FanThread implements Runnable {
	private static final Logger log = LogManager.getLogger(FanThread.class);
	
	private GpioPinDigitalOutput fanPin = null;
	
	public FanThread(){} 

	public FanThread(GpioPinDigitalOutput fanPin){
		this.fanPin = fanPin;
	}
	@Override
	public void run() { 
		log.debug("Fan Thread Started");
		
		if (Status.fanOn) {
			fanPin.low();
			Status.fanOn = true;
		}else {
			Status.fanOn = false;
			fanPin.high();
		}
		
		try {
			Thread.sleep(900000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
}
