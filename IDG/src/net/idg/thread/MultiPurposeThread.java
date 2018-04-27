package net.idg.thread;

import java.io.IOException;
/*
 * Thread that does multiple function.
 */
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.idg.IDGServer;
import net.idg.bean.Sensor;
import net.idg.db.SensorSql;
import net.idg.db.entity.SensorStatus;
public class MultiPurposeThread implements Runnable {
	private static final Logger log = LogManager.getLogger(MultiPurposeThread.class);
	@Override
	public void run() {
		Calendar now = Calendar.getInstance();
		log.debug("Backlight on? " + IDGServer.getLcd().isBacklight());
		
		if (now.get(Calendar.HOUR_OF_DAY) >= 21) {
			if (IDGServer.getLcd().isBacklight()) {
				try {
					IDGServer.getLcd().setBacklight(false, true);
					addStatus(false, "Turning lcd off");
				} catch (IOException e) {
					log.error("Error in LCD thread.Turn off backlight", e);
				}
			}
		}else if(now.get(Calendar.HOUR_OF_DAY) >= 6 && now.get(Calendar.HOUR_OF_DAY) < 21) {
			if (!IDGServer.getLcd().isBacklight()) {
				try {
					IDGServer.getLcd().setBacklight(true, true);
					addStatus(true, "Turning lcd ON");
				} catch (IOException e) {
					log.error("Error in LCD thread. turn on backlight", e);
				}
			}
		}
		
		
	}
	private void addStatus(boolean on, String message ) {
		SensorStatus stat = new SensorStatus();
		SensorSql sql = new SensorSql();
		
		stat.setComment(message);
		stat.setSensor(Sensor.LCD);
		stat.setRecordedDate(new Date());
		stat.setField1(String.valueOf(on));
		
		try {
			sql.add(stat);
		} catch (Exception e) {
			log.error("Error writing to DB", e);
		} 
	}

}
