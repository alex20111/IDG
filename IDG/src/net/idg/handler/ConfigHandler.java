package net.idg.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import home.misc.Exec;
import net.idg.IDGServer;
import net.idg.bean.HtmlPageText;
import net.idg.bean.Status;
import net.idg.db.ConfigSql;
import net.idg.db.entity.Config;
import net.idg.utils.ReadWrite;
import net.idg.utils.ServerUtils;

@SuppressWarnings("restriction")
public class ConfigHandler implements HttpHandler {
	
	private static final Logger log = LogManager.getLogger(ConfigHandler.class);
	
	@Override	public void handle(HttpExchange t) throws IOException {
		String configPage = "";
		try{
			Map<String,String> query = ServerUtils.queryToMap(t.getRequestURI().getQuery());
			log.debug("query: " + query);
			if (query == null){
				HtmlPageText cfgPage = new HtmlPageText();
				configPage = cfgPage.buildConfigPage(IDGServer.getConfig(),getSSIDs());
			}else if (query != null && 
			query.get(Config.ENB_WIRELESS) != null && !Status.currentSSID.equals(query.get(Config.SSID)) &&
			query.get(Config.SSID).length() > 0 &&
			query.get(Config.PASS) != null && query.get(Config.PASS).trim().length() == 0
			&& !query.get(Config.SSID).equals("nowifi"))
			{
				configPage ="<html><body>Error. New SSID, You need the password if the wireless IS enabled <br/>" 
						+ " <form action=\"/cfg\" > <input type=\"submit\" value=\"Ok\"/> </form> </body></html>";
				IDGServer.getConfig().setEnableWireless(true);
			}else{
				configPage = "<htm|><body>Success<br/><br/><form action=\"/cfg\" > <input type=\"submit\" value=\"Ok\"/> </form></body></html>";
				saveConfig(query); //save properties
				IDGServer.getSchedManager().stopSchedules();
				IDGServer.getSchedManager().startRestartSchedules();

				Config cfg = IDGServer.getConfig();
				if (cfg.isEnableWireless() &&
						!Status.currentSSID.equals(cfg.getSsid()) && !cfg.getSsid().equals("nowifi") ){
					log.debug("new wireless. SSID: " + cfg.getSsid()); //restart wireless
					ReadWrite r = new ReadWrite();
					if (Status.currentSSID.length() > 0){ //delete if we had one
						r.updateAddWireless(Status.currentSSID, null, true);
					}
					r.updateAddWireless(cfg.getSsid(), cfg.getPassword(), false);
					Status.currentSSID = cfg.getSsid();
				}else if (cfg.isEnableWireless() &&
						Status.currentSSID.equals(cfg.getSsid()) &&
						cfg.getPassword().trim().length() > 0){
					log.debug("Updating wireless");
					ReadWrite r = new ReadWrite();
					r.updateAddWireless(cfg.getSsid(), cfg.getPassword(), false);
				}else if (!cfg.isEnableWireless() && Status.currentSSID.length() > 0 ){
					ReadWrite r = new ReadWrite();
					log.debug("remove wireless");
					r.updateAddWireless(Status.currentSSID, null, true);
					Status.currentSSID = ""; } }
		}catch(Exception ex){
			ex.printStackTrace();
		}
		Headers h = t.getResponseHeaders(); 
		h.add("Content-Type", "text/html"); 
		t.sendResponseHeaders(200, configPage.length());
		OutputStream os = t.getResponseBody();
		os.write(configPage.getBytes()); 
		os.close();
}
	private void saveConfig(Map<String, String> query) throws IOException{
		
		Config cfg = new Config();
//		Properties prop = new Properties();
//		OutputStream output = new FileOutputStream("cfgFile.cfg"); 
		cfg.setEnableFan(query.get(Config.ENABLE_FAN) != null ? true : false);
		cfg.setEnableLights(query.get(Config.ENABLE_LIGHTS) != null ? true : false );
		cfg.setEnableTempMon(query.get(Config.ENABLE_TEMP) != null ? true : false);
		cfg.setEnableThinkSpeak(query.get(Config.ENB_THINK_SPEAK) != null ? true : false);
		cfg.setEnableWireless(query.get(Config.ENB_WIRELESS) != null ? true : false);
		cfg.setMaintainTempAt(Integer.parseInt(query.get(Config.MAINTAIN_TEMP)));
		cfg.setPassword(query.get(Config.PASS).trim().length() != 0 ? query.get(Config.PASS) : "");
		cfg.setSsid(query.get(Config.ENB_WIRELESS) != null ? query.get(Config.SSID): "");
		cfg.setLightsStartTime(Integer.parseInt(query.get(Config.LIGHT_START))); 
		cfg.setLightsStopTime(Integer.parseInt(query.get(Config.LIGHT_END)));
		cfg.setApiKey(query.get(Config.TS_API_KEY).trim().length() != 0 ? query.get(Config.TS_API_KEY) : "");
		cfg.setChannel(query.get(Config.TS_CHANNEL).trim().length() != 0 ? query.get(Config.TS_CHANNEL) : "");
		 cfg.setThinkSpeakIntv(Integer.parseInt(query.get(Config.TS_FREQ)));
//		prop.store(output, null);
//		output.close();
		 ConfigSql sql = new ConfigSql();
		 try {
			sql.update(cfg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 
		 
		IDGServer.loadConfig(); 
	}
	private List<String> getSSIDs(){

		List<String> SSIDs = new ArrayList<String>();
		try {
			Exec exec = new Exec();
			exec.addCommand("/home/pi/wifiScan.sh");
			exec.run();

			String result = exec.getOutput();

			if (result.contains("ESSID")) {
				String name[] = result.split("ESSID");
				for(String nm : name) {
					if (nm.indexOf("\"") > 0) {
						String n = nm.substring(nm.indexOf("\"") + 1 , nm.lastIndexOf("\"") );
						if (n.trim().length() > 0) {
							System.out.println("SSID: " + n);
							SSIDs.add(n);
						}

					}
				}
			}
		}catch(Exception ex) {
			log.error("Error in SSSID scan", ex);
		}
		return SSIDs; 
	} 
}