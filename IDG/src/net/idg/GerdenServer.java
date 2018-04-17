package net.idg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.pi4j.component.lcd.impl.I2CLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;

import net.idg.bean.Config;
import net.idg.bean.Status;
import net.idg.manager.ScheduleManager;

public class GerdenServer {

	private static final Logger log = LogManager.getLogger(GerdenServer.class);

	public static String wpaSupplicant = "/etc/wpa_supplicant/wpa_supplicant.conf"; //TODO change
	private static I2CLcdDisplay lcd = null;
	
	private static ScheduleManager schedManager;
	private static Config cfg = null;
	private static boolean configPresent = false; //control vars
		
	
	public static void main(String[] args) throws InterruptedException {
		logger();
		
		log.debug("Starting program");
		
		final GpioController gpio = GpioFactory.getInstance();

		try {
			lcd = new I2CLcdDisplay(2,16 , I2CBus.BUS_1, 0x27, 
					3, 0, 1, 2, 7, 6, 5, 4);
			lcd.clear();
			lcd.write(0, "Garden Monitor");
			lcd.write(1, "By Alex. B.");
			Thread.sleep(1000);
		} catch (Exception e) {
			log.error("Error initializing the LCD. " , e);
		}
//		
		final GpioPinDigitalOutput lightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "Lights", PinState.LOW);
		final GpioPinDigitalOutput heatPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "heater", PinState.LOW);
		final GpioPinDigitalOutput fanPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "fan", PinState.LOW);
		

		
		schedManager = new ScheduleManager(heatPin, lightPin, fanPin);//pass gpio here for schedules ' l
		schedManager.startWebServer();
		schedManager.startLcdMonitor();
		loadConfig(null);
		schedManager.startRestartSchedules();
		if (cfg != null ){//move config ssid in current SSID if config is not null
			Status.currentSSID = cfg.getSsid();
		} 
	}
	public static synchronized boolean isConfigPresent(){
		return configPresent; 
	}
	public static synchronized Config getConfig(){
		return cfg;
	} 
	public static synchronized ScheduleManager getSchedManager(){
		return schedManager;
	}
	public static synchronized I2CLcdDisplay getLcd() {
		return lcd;
	}
	public static synchronized void display(String line1, String line2){
		if (lcd != null) {
			lcd.clear();
			lcd.write(0, line1);
			lcd.write(1, line2);
		}
//		try {
//			Exec e = new Exec();
//			e.addCommand("python").handleQuoting(false).addCommand("/home/pi/i2clcda.py")
//			.addCommand(line1).addCommand(line2).timeout(4000);
//
//			int result = e.run();
//			if (result > 0) {
//				log.error("Result greather than 0 to display on LCD. " + e.getOutput());
//			}
//		} catch (IOException e1) {
//			log.error("Error writing to lcd", e1);
//		}

	}
	public static synchronized void loadConfig(Properties properties) {
		try{//load config file
			Properties prop = null;
			if(properties == null){
				File configFile = new File("cfgFile.cfg");
				if (configFile != null){
					prop = new Properties();
					InputStream input = new FileInputStream(configFile);
					prop.load(input);
				}
			}else{
				prop = properties;
			} 
			cfg = new Config();
			cfg.setApiKey(prop.getProperty(Config.TS_API_KEY));
			cfg.setChannel(prop.getProperty(Config.TS_CHANNEL));
			cfg.setEnableFan(Boolean.valueOf(prop.getProperty(Config.ENABLE_FAN)));
			cfg.setEnableLights(Boolean.valueOf(prop.getProperty(Config.ENABLE_LIGHTS)));
			cfg.setEnableTempMon(Boolean.valueOf(prop.getProperty(Config.ENABLE_TEMP)));
			cfg.setEnableThinkSpeak(Boolean.valueOf(prop.getProperty(Config.ENB_THINK_SPEAK)));
			cfg.setEnableWireless(Boolean.valueOf(prop.getProperty(Config.ENB_WIRELESS)));
			cfg.setMaintainTempAt(Integer.parseInt(prop.getProperty(Config.MAINTAIN_TEMP)));
			cfg.setPassword(prop.getProperty(Config.PASS));
			cfg.setSsid(prop.getProperty(Config.SSID));
			cfg.setThinkSpeakIntv(Integer.parseInt(prop.getProperty(Config.TS_FREQ)));
			cfg.setLightsStartTime(Integer.parseInt(prop.getProperty(Config.LIGHT_START)));
			cfg.setLightsStopTime(Integer.parseInt(prop.getProperty(Config.LIGHT_END)));
			configPresent = true; 
		}catch(IOException ex){ 
			log.error("Error in loadConfig", ex);
		}
		log.debug("loadConfig: " + cfg);
	}

private static void logger() {
	//This is the root logger provided by log4j
	Logger rootLogger = Logger.getRootLogger();
	rootLogger.setLevel(Level.DEBUG);
	 
	//Define log pattern layout
	PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
	 
	//Add console appender to root logger
	rootLogger.addAppender(new ConsoleAppender(layout));	
	
	
	
}}