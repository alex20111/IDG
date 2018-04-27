package net.idg.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.idg.IDGServer;

public class ReadWrite {
	private static final Logger log = LogManager.getLogger(ReadWrite.class);
	
	public boolean updateAddWireless(String ssid, String passowrd, boolean delete) {
		try{
			Path path = Paths.get(IDGServer.wpaSupplicant);
			Charset charset = StandardCharsets.UTF_8; 
			String content = new String(Files.readAllBytes(path), charset);
			if (content.contains(ssid) || !content.contains("network=")){
				if (!content.contains("network=") && !delete){
					log.debug("Add it"); 
					content += "network={"+ System.getProperty("line.separator") +
							"ssid=\""+ssid+"\" "+System.getProperty("line.separator") +
							"psk=\""+passowrd+"\""+System.getProperty("line.separator") + "}";
				}else if (content.contains("network=") && !delete){ 
					//then find the right SSID and password and replace it ~
					int idx = content.indexOf(ssid);
					 //fast fon/vard A
					idx = content.indexOf("psk",idx) + 7 ;
					int idx2 = content.indexOf("\"", idx) ;
					String one = content.substring(0, idx);
					String two = content.substring( idx2, content.length() );
					content = one + passowrd + two;
				}else if (content.contains("network=") && delete){
					log.debug("delete it");
					//then find the right SSID and password and replace it
					int idx = content.indexOf(ssid); 
					String tempOne = content.substring(0, idx);
					String one = content.substring(0, tempOne.lastIndexOf("network"));
					String temp2 = content.substring(idx, content.length());
					String two = temp2.substring(temp2.indexOf("}") + 1,temp2.length() );
					content = one + two; 
				}
				Files.write(path, content.getBytes(charset));
			} 
		}catch(Exception ex){
			log.error("Error updating Wireless", ex);
			return false;
		}
		return true;
	}
}
