package net.idg.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Config {
	
	public static final String TBL_NM = "CONFIG";
	public static final String ENB_WIRELESS = "enbWireless";
	public static final String SSID = "ssidOption";
	public static final String PASS = "ssidPassword" ;
	public static final String ENABLE_TEMP = "enbTempMon";
	public static final String MAINTAIN_TEMP = "tempMonVal";
	public static final String ENABLE_FAN = "enbFanMon";
	public static final String ENABLE_LIGHTS = "enbLightsMon" ;
	public static final String LIGHT_START = "lightsStartTime";
	public static final String LIGHT_END = "lightsEndTime";
	public static final String ENB_THINK_SPEAK = "enbThinkspeak";
	public static final String TS_CHANNEL = "channel";
	public static final String TS_API_KEY = "apiKey";
	public static final String TS_FREQ = "freq";
	
	private boolean enableWireless = false;
	private String ssid = "";
	private String password = "";
	private boolean enableTempMon = false; //enable
	private int maintainTempAt = 16;
	private boolean enableFan = false;
	private boolean enableLights = false;
	private int lightsStartTime = 300;
	private int lightsStopTime = 390;
	private boolean enableThinkSpeak = false;
	private int thinkSpeakIntv = 5; //in minutes
	private String channel  ="";
	private String apiKey = "";
	private String saveBtn = "";
	
	public Config() {}
	public Config (ResultSet rs) throws SQLException {
		enableWireless = rs.getBoolean(ENB_WIRELESS);
		ssid = rs.getString(SSID);
		password = rs.getString(PASS);
		enableTempMon = rs.getBoolean(ENABLE_TEMP);; //enable
		maintainTempAt = rs.getInt(MAINTAIN_TEMP);;
		enableFan = rs.getBoolean(ENABLE_FAN);;
		enableLights = rs.getBoolean(ENABLE_LIGHTS);;
		lightsStartTime = rs.getInt(LIGHT_START);
		lightsStopTime = rs.getInt(LIGHT_END);
		enableThinkSpeak = rs.getBoolean(ENB_THINK_SPEAK);;
		thinkSpeakIntv = rs.getInt(TS_FREQ); //in minutes
		channel  =rs.getString(TS_CHANNEL);
		apiKey = rs.getString(TS_API_KEY);
		
	}
	
	
	public boolean isEnableWireless() {
		return enableWireless;
	}
	public void setEnableWireless(boolean enableWireless) {
		this.enableWireless = enableWireless;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isEnableTempMon() {
		return enableTempMon;
	}
	public void setEnableTempMon(boolean enableTempMon) {
		this.enableTempMon = enableTempMon;
	}
	public int getMaintainTempAt() {
		return maintainTempAt;
	}
	public void setMaintainTempAt(int maintainTempAt) {
		this.maintainTempAt = maintainTempAt;
	}
	public boolean isEnableFan() {
		return enableFan;
	}
	public void setEnableFan(boolean enableFan) {
		this.enableFan = enableFan;
	}
	public boolean isEnableLights() {
		return enableLights;
	}
	public void setEnableLights(boolean enableLights) {
		this.enableLights = enableLights;
	}
	public int getLightsStartTime() {
		return lightsStartTime;
	}
	public void setLightsStartTime(int lightsStartTime) {
		this.lightsStartTime = lightsStartTime;
	}
	public int getLightsStopTime() {
		return lightsStopTime;
	}
	public void setLightsStopTime(int lightsStopTime) {
		this.lightsStopTime = lightsStopTime;
	}
	public boolean isEnableThinkSpeak() {
		return enableThinkSpeak;
	}
	public void setEnableThinkSpeak(boolean enableThinkSpeak) {
		this.enableThinkSpeak = enableThinkSpeak;
	}
	public int getThinkSpeakIntv() {
		return thinkSpeakIntv;
	}
	public void setThinkSpeakIntv(int thinkSpeakIntv) {
		this.thinkSpeakIntv = thinkSpeakIntv;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getSaveBtn() {
		return saveBtn;
	}
	public void setSaveBtn(String saveBtn) {
		this.saveBtn = saveBtn;
	}
	
	public static String checkIfTableExist() {
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NM+"'";
	}
}
