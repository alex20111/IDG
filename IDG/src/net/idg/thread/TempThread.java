package net.idg.thread;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import home.misc.Exec;
import net.idg.GerdenServer;
import net.idg.bean.Config;
import net.idg.bean.Status;
import net.idg.bean.Temperature;
import net.idg.utils.ServerUtils;

public class TempThread implements Runnable { 
	private static final Logger log = LogManager.getLogger(TempThread.class);
	private boolean monitorTemp = false; 
	private static Temperature temp = new Temperature();
	private GpioPinDigitalOutput heatPin = null;
	
	private static Date prevHeaterReading = null;
	
	private static Date dispTempPrev = null;

	public TempThread(){}
	public TempThread(boolean monitor, GpioPinDigitalOutput heatPin){
		monitorTemp = monitor; 
		this.heatPin = heatPin;
	}
	@Override
	public void run() { 
		try{ 
			if (prevHeaterReading == null) {
				prevHeaterReading = new Date();
				dispTempPrev = new Date();
			}
			
			temp = queryTemperature();
			
			if (Status.heaterOn && temp == null || !temp.isTempValidValue()) {
				Date now = new Date();//if running more than 15 min with invalid temp, shut off
				if (now.getTime() - prevHeaterReading.getTime() > (60000 * 15)) {
					heatPin.low();
					Status.heaterOn = false;
					log.trace("More than 15 min and invalid value from temp sensor. Shutting down. " + temp.getTemp());
				}
			}
			
			GerdenServer.display("Temp: " + temp.getTemp() + "C" , "Humidity: "+temp.getHumidity()+"%"); //LCD display
			if (monitorTemp && temp != null && temp.isTempValidValue()){
				Config cfg = GerdenServer.getConfig();
				
				if (temp.getTempDouble() > (cfg.getMaintainTempAt() + 1) && Status.heaterOn ){
					log.trace("Heater off. temp: " + temp.getTemp());
					//turn heater off
					Status.heaterOn = false;
					heatPin.low();
					GerdenServer.display("Heat Off" , ""); //LCD display
				}else if (temp.getTempDouble() < (cfg.getMaintainTempAt() - 1) && !Status.heaterOn){
					log.trace("Heater on. temp: " + temp.getTemp());

					//turn heater on
					Status.heaterOn = true; 
					heatPin.high();
					GerdenServer.display("Heat On" , "");
				} 
			}
			if (!GerdenServer.isConfigPresent()){
				pause(5000);
				String line2 = "";
				String ip = ServerUtils.connectedToNetwork();
				if(ip.length() == 0){
					line2 = "No Network";
				}else{
					line2 = ip; 
				}
				GerdenServer.display("No Config", line2);
			}else if(ServerUtils.connectedToNetwork().length() == 0){
				pause(5000); 
				GerdenServer.display("No network", "");
			}
			
			dispDate();
			
		}catch(Exception ex){ 
			log.error("Error in temp thread", ex);
			heatPin.low();
		}
	}
	private void pause(int millis){
		try {Thread.sleep(millis);
		} catch (InterruptedException e) { }
	}
	private Temperature queryTemperature() {
		Temperature tmp = null;
		try {
			Exec exec = new Exec();
			exec.addCommand("python").addCommand("/home/pi/adafruitDht/Adafruit_Python_DHT-master/examples/AdafruitDHT.py")
			.addCommand("22").addCommand("4").timeout(4000);

			exec.run();
			String result = exec.getOutput();
			if (result != null && result.length() > 0) {

				if (result.contains("Failed") && result.length() > 0) {	
					log.debug("failed reading temp: " + result);
					tmp = new Temperature();
					tmp.setTempValidValue(false);
				}else {
					String th[] = result.split("\\s+");
					tmp = new Temperature();
					tmp.setTemp(th[0]);
					tmp.setHumidity(th[1]);
					tmp.setTempValidValue(true);
					prevHeaterReading = new Date();
					
				}
			}else {
				log.debug("failed reading temp. Result null. Could be timeout ");
				tmp = new Temperature();
				tmp.setTempValidValue(false);
			}
		}catch(IOException iex) {
			log.error("Erorr in tmp", iex);
		}
		return tmp;
	}
	public static Temperature getTemp(){
		return temp;
	}
	
	private void dispDate() {
		if (new Date().getTime() - dispTempPrev.getTime() > 60000) {
			log.debug("temp: " + temp.getTemp() + " " + temp.getHumidity());
			dispTempPrev = new Date();
		}
	}
}
