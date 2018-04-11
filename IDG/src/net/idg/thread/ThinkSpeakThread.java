package net.idg.thread;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import home.thingSpeak.ThingSpeak;

//import com.angryelectron.thingspeak.Channel;
//import com.angryelectron.thingspeak.Entry;
//import com.angryelectron.thingspeak.ThingSpeakException;
//import com.mashape.unirest.http.exceptions.UnirestException;

import net.idg.bean.Temperature;

public class ThinkSpeakThread  implements Runnable{
	
	private static final Logger log = LogManager.getLogger(ThinkSpeakThread.class);
	
	private int tsChannel = 0;
	private String apiKey = "";
	
	public ThinkSpeakThread(int channel, String key) {
		this.tsChannel = channel;
		this.apiKey = key;
	}

	@Override
	public void run() {

		//		String apiWriteKey = "I3XZBHBDHJBQTOO7";

		Temperature temp = TempThread.getTemp();

		if (temp != null && temp.isTempValidValue()) {
			
			ThingSpeak t = new ThingSpeak(apiKey);
//			Channel channel = new Channel(tsChannel, apiKey);

			t.addField(1, temp.getTemp());
			try {
				t.write();
			} catch (Exception e) {
				log.error("Error writing to thingspeak" , e);
			}
//			Entry entry = new Entry();
//
//
//			entry.setField(1, TempThread.getTemp().getTemp());
//			try {
//				channel.update(entry);
//			} catch (UnirestException e) {
//				log.error("error UnirestException", e );
//
//			} catch (ThingSpeakException e) {
//				log.error("error ThingSpeakException", e );
//			}
		}else {
			log.debug("Temp not valid: " + temp);
		}
	}

}
