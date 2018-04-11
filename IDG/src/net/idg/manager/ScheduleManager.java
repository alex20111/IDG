package net.idg.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import net.idg.GerdenServer;
import net.idg.bean.Config;
import net.idg.bean.Status;
import net.idg.thread.FanThread;
import net.idg.thread.LightsThread;
import net.idg.thread.ServerThread;
import net.idg.thread.TempThread;
import net.idg.thread.ThinkSpeakThread;

public class ScheduleManager {

	private static final Logger log = LogManager.getLogger(ScheduleManager.class);
	public static ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(5);
	public static ScheduledFuture<?> tempSched;
	public static ScheduledFuture<?> fanSched; 
	public static ScheduledFuture<?> lightSched;
	public static ScheduledFuture<?> thinkSpeakSched;
	
	private GpioPinDigitalOutput lightPin = null;
	private GpioPinDigitalOutput heatPin = null;
	private GpioPinDigitalOutput fanPin = null;
	int gpiocodehere;
	public ScheduleManager(GpioPinDigitalOutput heatPin, GpioPinDigitalOutput lightPin, GpioPinDigitalOutput fanPin){
		this.lightPin = lightPin;
		this.heatPin = heatPin;
		this.fanPin = fanPin;
	}
	public void startWebServer(){ 
		scheduledService.schedule(new ServerThread(), 0, TimeUnit.SECONDS); //Start web server
	}
	public void startLights(){
		log.debug("Start lights schedule");
		long delay = calculateLightDelay();
		lightSched = scheduledService.schedule(new LightsThread(lightPin), delay,TimeUnit.MILLISECONDS);
	} 
	public void stopLights(){
		log.debug("Stopping lights schedule");
		if (lightSched != null ){
			lightSched.cancel(true); 
		} }
	public void startTemperature(int initialDelaySeconds, int delaySeconds, boolean monitor){
		log.debug("Start Temperature schedule"); 
		tempSched = scheduledService.scheduleWithFixedDelay(new TempThread(monitor, heatPin), initialDelaySeconds,
				delaySeconds,TimeUnit.SECONDS);
	} 
	public void stopTemperature(){
		log.debug("Stopping Temperature schedule");
		if (tempSched != null){
			tempSched.cancel(true);
		} 
	}
	public void startThinkSpeak(int initialDelayMinutes, int delayMinutes){
		log.debug("Start ThinkSpeak schedule");
		int channel = 0;
		String channelStr = GerdenServer.getConfig().getChannel();
		
		if (channelStr != null && channelStr.trim().length() > 0) {
			channel = Integer.parseInt(channelStr);
		}
		
		
		thinkSpeakSched = scheduledService.scheduleWithFixedDelay(new ThinkSpeakThread(channel, GerdenServer.getConfig().getApiKey()),
				initialDelayMinutes, delayMinutes,TimeUnit.MINUTES); 
	}
	public void stopThinkSpeak(){ 
		log.debug("Stopping ThinkSpeak schedule");
		if (thinkSpeakSched != null){
			thinkSpeakSched.cancel(true);
		}
	}
	public void startFan(int initialDelayMinutes, int delayMinutes){
		log.debug("Start Fan schedule");
		fanSched = scheduledService.scheduleWithFixedDelay(new FanThread(fanPin),
				initialDelayMinutes, delayMinutes,TimeUnit.MINUTES);
	} 
	public void stopFan(){ 
		log.debug("Stopping Fan schedule");
		if (fanSched != null ){
			fanSched.cancel(true);
		}
	}
	
	
	public void startRestartSchedules(){
		Config config = GerdenServer.getConfig(); 
		Status.initLights = true;
		int tempRefreshIntervalSec = 5; 
		if (config != null){ 
			if (config.isEnableFan()){
				startFan(0, 10); }
			if (config.isEnableLights()){
				startLights(); }
			if (config.isEnableThinkSpeak()){
				startThinkSpeak(0, config.getThinkSpeakIntv()); 
			}
			//start temp but not the monitoring, this will be check in the thread.
			startTemperature(0, tempRefreshIntervalSec, config.isEnableTempMon());
		}
		else{ 
			//if no config, still display the-temp but do nothing else.
			startTemperature(0, tempRefreshIntervalSec,false);
		}
	}
	public void stopSchedules(){
		stopTemperature();//shutdown and wait for 1 sec
		stopFan(); 
		stopLights();
		stopThinkSpeak();
		if (Status.fanOn){
			Status.fanOn = false; ///TODO then turn them off through GPIO
			fanPin.low();
		} 
		if (Status.heaterOn){ 
			Status.heaterOn = false; //TODO then turn themoff
			heatPin.low();
		}
		if (Status.lightsOn){
			Status.lightsOn = false; //TODO then turn them off
			lightPin.low();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) { }
	}
	private long calculateLightDelay(){
		long delay = -1;
		if (GerdenServer.getConfig() != null){
			int startInMin = GerdenServer.getConfig().getLightsStartTime();
			int stopInMin = GerdenServer.getConfig().getLightsStopTime();
			Calendar now = Calendar.getInstance();
			Calendar start = Calendar.getInstance();
			start.set(Calendar.HOUR_OF_DAY, startInMin / 60);
			start.set(Calendar.MINUTE,startInMin % 60 );
			start.set(Calendar.SECOND, 00);
			start.set(Calendar.MILLISECOND, 00);
			Calendar stop = Calendar.getInstance();
			stop.set(Calendar.HOUR_OF_DAY, stopInMin / 60);
			stop.set(Calendar.MINUTE,stopInMin % 60 );
			stop.set(Calendar.SECOND, 00);
			stop.set(Calendar.MILLISECOND, 00); 
			if (now.after(start) && now.before(stop) || now.equals(start) ){
				if (Status.initLights){ //turn on and calculate the amount remaining before end
					delay = 0;
				}else{ 
					delay = stop.getTimeInMillis() - now.getTimeInMillis();
				}
				log.debug("Lights in between " );
			}else if(now.before(start)){
				log.debug("Lights before start date"); //sleep and wait to start
				delay = start.getTimeInMillis() - now.getTimeInMillis();
			}else if(now.after(stop) || now.equals(stop)){ 
				log.debug("Lights after date");//add a day and start next day.
			start.add(Calendar.DAY_OF_MONTH, 1);
			delay = start.getTimeInMillis() - now.getTimeInMillis();
			}
			Status.initLights = false;
			log.debug("Delay date: " + new Date(new Date().getTime() + delay));
		}
		return delay; 
	}
}




