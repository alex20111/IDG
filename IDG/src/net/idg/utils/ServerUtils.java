package net.idg.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import home.misc.Exec;

public class ServerUtils {

	private static final Logger log = LogManager.getLogger(ServerUtils.class);
	private ServerUtils(){}

	public static Map<String, String> queryToMap(String query){
		if (query != null ){
			Map<String, String> result = new HashMap<String, String>();
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length>1) {
					result.put(pair[0], pair[1]);
				}else{
					result.put(pair[0], "");
				}
			}
			return result;
		}
		return null;
	}
	public static String connectedToNetwork() {
		
		String out = "";
		
		try {
		Exec exec = new Exec();
		
		exec.addCommand("hostname").addCommand("--all-ip-addresses").timeout(5000);
		
		exec.run();
		
		out = exec.getOutput();
		}catch (Exception ex) {
			log.debug("error", ex);
			
		}
		
		return out;
		
	}
	//check if connected to the network
	public static String connectedToNetwork2(){
		//TODO use exec to get : hostname --all-ip-addresses
		
		
		String ip;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
			System.out.println("IP ADDRESS: " + ip);
			String sp[] = ip.split("\\.");
			if (sp.length == 4 && !ip.contains("/") && !sp[0].equals("127")){
				return ip;
			}
		} catch (UnknownHostException e) { 
			log.error("Error in getting IP", e);
		}
		return "";
	} 
}