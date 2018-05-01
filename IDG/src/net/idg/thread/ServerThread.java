package net.idg.thread;

import java.net.InetSocketAddress;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpServer;

import net.idg.handler.ConfigHandler;
import net.idg.handler.DashBoardHandler;
import net.idg.handler.TableHandler;

@SuppressWarnings("restriction")
public class ServerThread implements Runnable{
	private static final Logger log = LogManager.getLogger(ServerThread.class);

	@Override
	public void run() {
		try{ 
			log.debug("Web Server starting"); 
			HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
			server.createContext("/cfg", new ConfigHandler());
			server.createContext("/", new DashBoardHandler());
			server.createContext("/graph", new TableHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
		}catch(Throwable tr){
			log.error("Error starting server: ", tr );
		}
	}
} 