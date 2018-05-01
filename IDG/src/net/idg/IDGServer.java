package net.idg;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.component.lcd.impl.I2CLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;

import net.idg.bean.Status;
import net.idg.db.ConfigSql;
import net.idg.db.CreateTables;
import net.idg.db.entity.Config;
import net.idg.manager.ScheduleManager;

public class IDGServer {

	private static final Logger log = LogManager.getLogger(IDGServer.class);

	public static String wpaSupplicant = "/etc/wpa_supplicant/wpa_supplicant.conf"; //TODO change
	private static I2CLcdDisplay lcd = null;
	
	private static ScheduleManager schedManager;
	private static Config cfg = null;
	private static boolean configPresent = false; //control vars
	
	private static GpioPinDigitalOutput lightPin = null;
	private static GpioPinDigitalOutput heatPin = null;
	private static GpioPinDigitalOutput fanPin = null;
		
	
	public static void main(String[] args) throws InterruptedException, ClassNotFoundException, SQLException, IOException {
//		logger();
		
		log.debug("Starting program");
		
		CreateTables crt = new CreateTables();
		crt.createDbTables();
		
		final GpioController gpio = GpioFactory.getInstance();

		try {
			lcd = new I2CLcdDisplay(2,16 , I2CBus.BUS_1, 0x27, 
					3, 0, 1, 2, 7, 6, 5, 4);
			lcd.clear();
			lcd.write(0, "Garden Monitor");
			lcd.write(1, "By Alex. B.");
			Thread.sleep(2000);
		} catch (Exception e) {
			log.error("Error initializing the LCD. " , e);
		}
		
		lightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "Lights", PinState.LOW);
	    heatPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "heater", PinState.LOW);
		fanPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "fan", PinState.LOW);	
		
		shutDownHook();
	
		loadConfig();
		
		schedManager = new ScheduleManager(heatPin, lightPin, fanPin);//pass gpio here for schedules ' l
		schedManager.startWebServer();
		schedManager.MultiPurposeThread();	
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

	}
	public static synchronized void loadConfig() {
		try{//load config file
//			Properties prop = null;
//			if(properties == null){
//				File configFile = new File("cfgFile.cfg");
//				if (configFile != null){
//					prop = new Properties();
//					InputStream input = new FileInputStream(configFile);
//					prop.load(input);
//				}
//			}else{
//				prop = properties;
//			} 
//			cfg = new Config();
//			cfg.setApiKey(prop.getProperty(Config.TS_API_KEY));
//			cfg.setChannel(prop.getProperty(Config.TS_CHANNEL));
//			cfg.setEnableFan(Boolean.valueOf(prop.getProperty(Config.ENABLE_FAN)));
//			cfg.setEnableLights(Boolean.valueOf(prop.getProperty(Config.ENABLE_LIGHTS)));
//			cfg.setEnableTempMon(Boolean.valueOf(prop.getProperty(Config.ENABLE_TEMP)));
//			cfg.setEnableThinkSpeak(Boolean.valueOf(prop.getProperty(Config.ENB_THINK_SPEAK)));
//			cfg.setEnableWireless(Boolean.valueOf(prop.getProperty(Config.ENB_WIRELESS)));
//			cfg.setMaintainTempAt(Integer.parseInt(prop.getProperty(Config.MAINTAIN_TEMP)));
//			cfg.setPassword(prop.getProperty(Config.PASS));
//			cfg.setSsid(prop.getProperty(Config.SSID));
//			cfg.setThinkSpeakIntv(Integer.parseInt(prop.getProperty(Config.TS_FREQ)));
//			cfg.setLightsStartTime(Integer.parseInt(prop.getProperty(Config.LIGHT_START)));
//			cfg.setLightsStopTime(Integer.parseInt(prop.getProperty(Config.LIGHT_END)));
//			
			configPresent = true; 
			
			ConfigSql sql = new ConfigSql();
			cfg = sql.loadConfig();
			
		}catch(Exception ex){ 
			log.error("Error in loadConfig", ex);
			cfg = new Config();
		}
		log.debug("loadConfig: " + cfg);
	}
	private static void shutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				log.debug("Shutting down!!");
				//clear lcd
				lcd.clear();
				try {
					lcd.setBacklight(false, true);
				} catch (IOException e) {}
				//all pins low.
				lightPin.low();
				heatPin.low();
				fanPin.low();
				
			}
			
			
		});
	}
//private static void logger() {
//	//This is the root logger provided by log4j
//	Logger rootLogger = Logger.getRootLogger();
//	rootLogger.setLevel(Level.DEBUG);
//	 
//	//Define log pattern layout
//	PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
//	 
//	//Add console appender to root logger
//	rootLogger.addAppender(new ConsoleAppender(layout));	
//	
//	
//	
//}
}