package net.idg.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import net.idg.bean.Sensor;
import net.idg.db.SensorSql;
import net.idg.db.entity.SensorStatus;
import net.idg.utils.ServerUtils;

@SuppressWarnings("restriction")
public class TableHandler implements HttpHandler {
	private static final Logger log = LogManager.getLogger(TableHandler.class);

	@Override
	public void handle(HttpExchange t) throws IOException {

		SensorSql sql = new SensorSql();
		StringBuilder dashBoard = new StringBuilder("error");

		Map<String,String> query = ServerUtils.queryToMap(t.getRequestURI().getQuery());

		try {
			List<SensorStatus> stats = new ArrayList<SensorStatus>();
			System.out.println("Query graph = " + query);
			if (query != null) {
				String dtR = query.get("dateRange");
				Date dt = new Date();
				if (dtR != null && dtR.trim().length() > 0) {
					dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(query.get("dateRange"));
				}
				stats = sql.loadAll(dt);
			}
			else {
				stats = sql.loadAll(new Date());
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			dashBoard = new StringBuilder("<!DOCTYPE html><body>");
			dashBoard.append("<form action='/graph' > <input type='text' name='dateRange' placeholder='yyyy-mm-dd'  value=''/> <input type='submit' value='Load' /> </form> <br/>");

			StringBuilder tempTable = new StringBuilder();
			StringBuilder heaterTable = new StringBuilder();

			//temp header
			tempTable.append("<b>Temperature reading</b><br/>");
			tempTable.append("<table><theader><tr><th>Temperature</th><th>Humidity</th><th>Recorded Date</th></tr></theader>");
			tempTable.append("<tbody>");

			//heater on/off
			heaterTable.append("<br/><b>Heater reading</b><br/>");
			heaterTable.append("<table><theader><tr><th>Heater Status</th><th>Recorded Date</th></tr></theader>");
			heaterTable.append("<tbody>");

			for(SensorStatus s: stats) {
				if (s.getSensor() == Sensor.TEMPERATURE) {
					tempTable.append("<tr>");
					tempTable.append("<td>"+s.getField1()+"</td>");
					tempTable.append("<td>"+s.getField2()+"</td>");
					tempTable.append("<td>" + sdf.format(s.getRecordedDate())+"</td>");
					tempTable.append("</tr>");
				}else if(s.getSensor() == Sensor.HEATER) {
					heaterTable.append("<tr>");
					heaterTable.append("<td>"+s.getField1()+"</td>");
					heaterTable.append("<td>" + sdf.format(s.getRecordedDate())+"</td>");
					heaterTable.append("</tr>");
				}
			}

			tempTable.append("</tbody></table><br/>");
			heaterTable.append("</tbody></table><br/>");


			dashBoard.append(tempTable.toString());
			dashBoard.append(heaterTable.toString());
			dashBoard.append("</body></html>");

		}catch(Exception ex) {
			log.error("Error while generating table", ex);
		}
		Headers h = t.getResponseHeaders(); 
		h.add("Content-Type", "text/html"); 
		t.sendResponseHeaders(200, dashBoard.length());
		OutputStream os = t.getResponseBody();
		os.write(dashBoard.toString().getBytes()); 
		os.close();
	}

}
