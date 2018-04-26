package net.idg.db;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.idg.db.entity.Config;

public class CreateTables {

	private static final Logger log = LogManager.getLogger(CreateTables.class);
	
	public void createDbTables() throws ClassNotFoundException, SQLException, IOException {
		log.debug("Checking tables");
		ConfigSql cfgSql = new ConfigSql();
		
		boolean exist = cfgSql.CreateConfigTable();
		
		if (!exist) {
			log.debug("Table config does not exist, creating");
			//add default
			Config cfg = new Config();
			cfgSql.add(cfg);			
		}
		
		
	}
}
