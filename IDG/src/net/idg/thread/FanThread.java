package net.idg.thread;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import net.idg.GerdenServer;
import net.idg.bean.Config;
import net.idg.bean.Status;
import net.idg.bean.Temperature;

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
		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//				if (Status.fanOn) {
//					fanPin.low();
//					Status.fanOn = false;
//				}else {
//					Status.fanOn = true;
//					fanPin.high();
//				}
		Temperature temp = TempThread.getTemp();

		if (temp != null) {
			if (temp.getHumidityDouble() > 50 &&  !Status.fanOn) {
				//turn fan on
				Status.fanOn = true;
				fanPin.high();
				log.debug("Humidity high, turning fan on");
			}else if ( temp.getHumidityDouble() < 45 && Status.fanOn ) {
				Status.fanOn = false;
				fanPin.low();
				log.debug("Humidity normal, turning fan off");
			}

			//check heat
			Config cfg = GerdenServer.getConfig();
			if (cfg.isEnableTempMon() ) {
				if (temp.getTempDouble() >  (cfg.getMaintainTempAt() + 2) && !Status.fanOn) {
					Status.fanOn = true;
					fanPin.high();
					log.debug("Temperature high, turning fan on");
				}else if(temp.getTempDouble() <  cfg.getMaintainTempAt()   && Status.fanOn){
					Status.fanOn = false;
					fanPin.low();
					log.debug("Temperature normal, turning fan off");
				}
			}
		}

		//		try {
		//			Thread.sleep(900000);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	} 
} 