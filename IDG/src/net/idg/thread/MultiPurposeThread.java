package net.idg.thread;

import java.io.IOException;
/*
 * Thread that does multiple function.
 */
import java.util.Calendar;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.idg.GerdenServer;
public class MultiPurposeThread implements Runnable {
	private static final Logger log = LogManager.getLogger(MultiPurposeThread.class);
	@Override
	public void run() {
		Calendar now = Calendar.getInstance();
		log.debug("Backlight on? " + GerdenServer.getLcd().isBacklight());
		
		if (now.get(Calendar.HOUR_OF_DAY) >= 21) {
			if (GerdenServer.getLcd().isBacklight()) {
				try {
					GerdenServer.getLcd().setBacklight(false, true);
				} catch (IOException e) {
					log.error("Error in LCD thread.Turn off backlight", e);
				}
			}
		}else if(now.get(Calendar.HOUR_OF_DAY) >= 6 && now.get(Calendar.HOUR_OF_DAY) < 21) {
			if (!GerdenServer.getLcd().isBacklight()) {
				try {
					GerdenServer.getLcd().setBacklight(true, true);
				} catch (IOException e) {
					log.error("Error in LCD thread. turn on backlight", e);
				}
			}
		}
		
		
	}

}
