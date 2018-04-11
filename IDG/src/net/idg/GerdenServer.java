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

import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import net.idg.bean.Config;
import net.idg.bean.Status;
import net.idg.manager.ScheduleManager;

public class GerdenServer {

	private static final Logger log = LogManager.getLogger(GerdenServer.class);

	private static ScheduleManager schedManager;
	private static Config cfg = null; 
	public static String wpaSupplicant = "/etc/wpa_supplicant/wpa_supplicant.conf"; //TODO change
	private static boolean configPresent = false; //control vars
	
    public final static int LCD_ROWS = 2;
    public final static int LCD_ROW_1 = 0;
    public final static int LCD_ROW_2 = 1;
    public final static int LCD_COLUMNS = 16;
    public final static int LCD_BITS = 4;
    
    private  static GpioLcdDisplay lcd = null;
	
	public static void main(String[] args) throws InterruptedException {
		logger();
		
		log.debug("Starting program");
		
		final GpioController gpio = GpioFactory.getInstance();
		lcd = new GpioLcdDisplay(LCD_ROWS,    // number of row supported by LCD
                LCD_COLUMNS,       // number of columns supported by LCD
                RaspiPin.GPIO_06, // LCD RS pin
				RaspiPin.GPIO_05, // LCD strobe pin
				RaspiPin.GPIO_04, // LCD data bit 1
				RaspiPin.GPIO_00, // LCD data bit 2
				RaspiPin.GPIO_01, // LCD data bit 3
				RaspiPin.GPIO_03); // LCD data bit 4		
		
		final GpioPinDigitalOutput lightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "Lights", PinState.LOW);
		final GpioPinDigitalOutput heatPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "heater", PinState.LOW);
		final GpioPinDigitalOutput fanPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "fan", PinState.LOW);
		
		lcd.clear();
		lcd.writeln(LCD_ROW_1, "Garden Monitor");
		lcd.writeln(LCD_ROW_1, "By Alex. B.");
		Thread.sleep(1000);
		
		schedManager = new ScheduleManager(heatPin, lightPin, fanPin);//pass gpio here for schedules ' l
		schedManager.startWebServer();
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
	public static synchronized void display(String linel, String line2){
//		lcd.clear();
//		log.debug("Writing to LCD");
		lcd.writeln(LCD_ROW_1, linel);
		lcd.writeln(LCD_ROW_2, line2);
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