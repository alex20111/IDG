package net.idg.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import net.idg.bean.Status;
import net.idg.bean.Temperature;
import net.idg.thread.TempThread;

@SuppressWarnings("restriction")
public class DashBoardHandler implements HttpHandler {
	
	private static final Logger log = LogManager.getLogger(DashBoardHandler.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Override
	public void handle(HttpExchange t) throws IOException {
		
		Temperature tmp = TempThread.getTemp();
		
		
		String dashBoard = "<html><body><form action=\"/cfg\" > <input type=\"submit\" value=\"Config\"/> </form><br/><br/><b>Temp:</b>"
							+tmp.getTemp()+"<br/>"
							+ "<b>Humidity: </b>"+tmp.getHumidity()+"<br/>"							 
							+ "<b>Light status:</b> "+ (Status.lightsOn ? " ON " : " Off ") +" <br/>"
							+ "<b>Heater status:</b> "+(Status.heaterOn ? " On " : " Off ") + " <br/> "
							+ "<b>Last updated:</b> "+(tmp.getLastUpdated() != null ? sdf.format(tmp.getLastUpdated() ): "N/A")+" <br/>"
							+ " </body></html>";
		
		Headers h = t.getResponseHeaders(); 
		h.add("Content-Type", "text/html"); 
		t.sendResponseHeaders(200, dashBoard.length());
		OutputStream os = t.getResponseBody();
		os.write(dashBoard.getBytes()); 
		os.close();
		
	}

}
