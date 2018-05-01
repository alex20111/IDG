package net.idg.thread;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import home.misc.Exec;
import net.idg.IDGServer;
import net.idg.bean.Sensor;
import net.idg.bean.Status;
import net.idg.bean.Temperature;
import net.idg.db.SensorSql;
import net.idg.db.entity.Config;
import net.idg.db.entity.SensorStatus;
import net.idg.utils.ServerUtils;

public class TempThread implements Runnable { 
	private static final Logger log = LogManager.getLogger(TempThread.class);
	private boolean monitorTemp = false; 
	private static Temperature temp = new Temperature();
	private GpioPinDigitalOutput heatPin = null;
	
	private static Date prevHeaterOn = null;	
	private static Date prevTempReading = null;
	private int delay = 60000 * 5; //in millis = 1 min. So this is 5 min

	public TempThread(){}
	public TempThread(boolean monitor, GpioPinDigitalOutput heatPin){
		monitorTemp = monitor; 
		this.heatPin = heatPin;
	}
	@Override
	public void run() { 
		try{ 
			if (prevHeaterOn == null) {
				prevHeaterOn = new Date();
				prevTempReading = new Date();
			}
			
			temp = queryTemperature();
			
			if (Status.heaterOn ) {
				Date now = new Date();//if running more than 15 min with invalid temp, shut off
				if (now.getTime() - prevHeaterOn.getTime() > (60000 * 30)) {
					heatPin.low();
					Status.heaterOn = false;
					String msg = "Heater has been running more than 30 min. Shutting down. " + temp.getTemp();
					addStatus(Sensor.HEATER, temp, false, msg);
					log.debug(msg);
				}
			}
			
			IDGServer.display("Temp: " + temp.getTemp() + "C" , "Humidity: "+temp.getHumidity()+"%"); //LCD display
			if (monitorTemp && temp != null && temp.isTempValidValue()){
				Config cfg = IDGServer.getConfig();
				
				if (temp.getTempDouble() > (cfg.getMaintainTempAt() + 1) && Status.heaterOn ){
					log.debug("Heater off. temp: " + temp.getTemp());
					//turn heater off
					Status.heaterOn = false;
					heatPin.low();
					IDGServer.display("Heat Off" , ""); //LCD display
					addStatus(Sensor.HEATER, temp, false, "Heater off");
				}else if (temp.getTempDouble() < (cfg.getMaintainTempAt() - 1) && !Status.heaterOn){
					log.debug("Heater on. temp: " + temp.getTemp());

					prevHeaterOn = new Date();//start timer
					//turn heater on
					Status.heaterOn = true; 
					heatPin.high();
					IDGServer.display("Heat On" , "");
					addStatus(Sensor.HEATER, temp, true, "Heater On");
				} 
			}else if (temp != null && (Status.heaterOn || heatPin.isHigh())) {
				//no monitor , just verify that the heater is really turned off
				Status.heaterOn = false;
				heatPin.low();
				addStatus(Sensor.HEATER, temp, false, "Safety heater off");
			}
			
			if (!IDGServer.isConfigPresent()){
				pause(5000);
				String line2 = "";
				String ip = ServerUtils.connectedToNetwork();
				if(ip.length() == 0){
					line2 = "No Network";
				}else{
					line2 = ip; 
				}
				IDGServer.display("No Config", line2);
			}else if(ServerUtils.connectedToNetwork().length() == 0){
				pause(5000); 
				IDGServer.display("No network", "");
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
					tmp.setLastUpdated(new Date());
				}else {
					String th[] = result.split("\\s+");
					tmp = new Temperature();
					tmp.setTemp(th[0]);
					tmp.setHumidity(th[1]);
					tmp.setTempValidValue(true);
					tmp.setLastUpdated(new Date());
					
				}
			}else {
				log.debug("failed reading temp. Result null. Could be timeout ");
				tmp = new Temperature();
				tmp.setTempValidValue(false);
				tmp.setLastUpdated(new Date());
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
		if (new Date().getTime() - prevTempReading.getTime() > delay) {
			log.debug("temp: " + temp.getTemp() + " " + temp.getHumidity());
			prevTempReading = new Date();
			addStatus(Sensor.TEMPERATURE, temp, false, "Temerature reading");
		}
	}
	
	private void addStatus(Sensor sensor, Temperature temp, boolean heaterOn, String message ) {
		SensorStatus stat = new SensorStatus();
		SensorSql sql = new SensorSql();
		
		stat.setComment(message);
		stat.setSensor(sensor);
		stat.setRecordedDate(new Date());
		stat.setField1(temp.getTemp());
		stat.setField2(temp.getHumidity());
		if (sensor == Sensor.HEATER) {
			stat.setField3(String.valueOf(heaterOn));
		}
		
		try {
			sql.add(stat);
		} catch (Exception e) {
			log.error("Error writing to DB", e);
		} 
	}
	
}
