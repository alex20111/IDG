package net.idg.bean;

import java.util.List;

public class HtmlPageText {
	private String configPageFooter = "</body> </html>";

	public String buildConfigPage(Config cfg, List<String> ssidNameList){
		boolean cfgEnb = true;
		if (cfg == null){
			cfgEnb = false;
		}
		StringBuilder configPage = new StringBuilder();
		StringBuilder configHead = new StringBuilder("<html><body>");
		configHead.append("<h1>Configuration</h1>");
		configHead.append("<hr></hr>");
		configHead.append("<h3>Wireless Information</h3>");
		configHead.append("<form>"); 
		configHead.append("<input type=\"checkbox\" name=\"enbWireless\"" + (cfgEnb && cfg.isEnableWireless()? "checked" : "") + " /> Enable Wireless? <br/>" ); 
		configHead.append("SSID: <select name=\"ssidOption\">");
		if (ssidNameList != null && !ssidNameList.isEmpty()){
			for(String ssid: ssidNameList){
				configHead.append("<option value=\""+ssid+"\">"+ssid+"</option>");
			}
		}else{
			configHead.append("<option value=\"nowifi\">No WIFI Found</option>");
		}
		
		StringBuilder configPageBody = new StringBuilder("</select> "); 
		configPageBody.append("<br/>Password: <input type=\"text\" name=\"ssidPassword\" />");
		configPageBody.append("<br/><h3>Options:</h3>");
		configPageBody.append("<small><i>*To enable a specific option, please check the	checkbox.</i></small> <br/>" );
		configPageBody.append("<input type=\"checkbox\" name=\"enbTempMon\"" + (cfgEnb && cfg.isEnableTempMon()? "checked" : "") + " /> Maintain temperature at <select name=\"tempMonVal\">");
		for(int i  = 15; i <= 25 ; i++){
			if (cfgEnb && cfg.getMaintainTempAt() == i){
				configPageBody.append("<option selected=\"selected\" value=\""+i+"\">"+i+"</option>");
			}else{ 
				configPageBody.append("<option value=\""+i+"\">"+i+"</option>");
			} 
		}
		configPageBody.append("</select> <br/>");
		configPageBody.append("<input type=\"checkbox\" name=\"enbFanMon\" " + (cfgEnb &&	cfg.isEnableFan()? "checked" : "") + " /> Enable Fan-funtion?<br/>");
		configPageBody.append("<input type=\"checkbox\" name=\"enbLightsMon\" " + (cfgEnb && cfg.isEnableLights()? "checked" : "") + "/> Lights on between <select name=\"lightsStartTime\">");
		int time = 0;
		for(int t = 0; t < 48; t ++){
			time += 30;
			if (cfgEnb && cfg.getLightsStartTime() == time){
				configPageBody.append("<option selected=\"selected\" value=\""+time+"\">"+convMinutesToHourMin(time)+"</option>");
			}else{
				configPageBody.append("<option	value=\""+time+"\">"+convMinutesToHourMin(time)+"</option>");
			}
		} 
		configPageBody.append("</select> and <select name=\"lightsEndTime\"> ");
		int timeEnd = 0;
		for(int t = 0; t < 48; t ++){ 
			timeEnd += 30;
			if (cfgEnb && cfg.getLightsStopTime() == timeEnd){
				configPageBody.append("<option selected=\"selected\" value=\""+timeEnd+"\">"+convMinutesToHourMin(timeEnd)+"</option>");
			}else{ 
				configPageBody.append("<option value=\""+timeEnd+"\">"+convMinutesToHourMin(timeEnd)+"</option>");
			} 
		}
		configPageBody.append("</select> <br/>");
		configPageBody.append("<input type=\"checkbox\" name=\"enbThinkspeak\" " + (cfgEnb &&	cfg.isEnableThinkSpeak()? "checked" : "") + "/> Save data on thingspeak? ");
		configPageBody.append("<br/> &nbsp; &nbsp; &nbsp; API Key: <input type=\"text\"	name=\"apiKey\" " + (cfgEnb && cfg.getApiKey().trim().length() > 0 ? " value=\"" + cfg.getApiKey() +"\"" : "") + " /> ");
		configPageBody.append("<br/> &nbsp; &nbsp; &nbsp; Channel: <input type=\"text\" name=\"channel\" " + (cfgEnb && cfg.getChannel().trim().length() > 0 ? " value=\"" + cfg.getChannel() +"\"": "") +"/>");
		configPageBody.append("<br/> &nbsp; &nbsp; &nbsp; Frequency: <select name=\"freq\">");
		for(int i = 5; i <= 60; i++ ){ 
			if (cfgEnb && cfg.getThinkSpeakIntv() == i){
				configPageBody.append("<option selected=\"selected\" value=\""+i+"\">"+i+"</option>"); 
			}else{ 
				configPageBody.append("<option value=\""+i+"\">"+i+"</option>");
			}
		} 
		configPageBody.append("</select> in minutes" );
		configPageBody.append("<br/><br/> <input type=\"submit\" value=\"save\"	name=\"save\"></form>"); 
		configPage.append(configHead);
		configPage.append(configPageBody);
		configPage.append(configPageFooter);
		return configPage.toString();
	}
	private String convMinutesToHourMin(int minutes){
		String formatted = " " + (minutes / 60) ;
		int min = (minutes % 60);
		if (min == 0){ 
			formatted += ":00";
		}else{ 
			formatted += ":" + min;
		}
		return formatted;
	} 
}