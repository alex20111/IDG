package net.idg.db;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.DbClass;
import net.idg.bean.Constants;
import net.idg.db.entity.Config;

public class ConfigSql {
	
	private static final Logger log = LogManager.getLogger(ConfigSql.class);
	
	public boolean CreateConfigTable() throws ClassNotFoundException, SQLException, IOException {
		DBConnection con = null;
		boolean exist = false;
		try {
			con = new DBConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword, DbClass.H2 );
			
			ResultSet rs = con.createSelectQuery(Config.checkIfTableExist()).getSelectResultSet();
			
			exist = rs.next();
			
			if (!exist) {
				List<ColumnType> columns = new ArrayList<ColumnType>();
				columns.add(new ColumnType(Config.ENB_WIRELESS, false).Boolean());
				columns.add(new ColumnType(Config.ENABLE_FAN, false).Boolean());
				columns.add(new ColumnType(Config.ENABLE_LIGHTS, false).Boolean());
				columns.add(new ColumnType(Config.ENABLE_TEMP, false).Boolean());
				columns.add(new ColumnType(Config.ENB_THINK_SPEAK, false).Boolean());
				columns.add(new ColumnType(Config.LIGHT_END, false).INT());
				columns.add(new ColumnType(Config.LIGHT_START, false).INT());
				columns.add(new ColumnType(Config.MAINTAIN_TEMP, false).INT());
				columns.add(new ColumnType(Config.PASS, false).VarChar(30));
				columns.add(new ColumnType(Config.SSID, false).VarChar(30));
				columns.add(new ColumnType(Config.TS_API_KEY, false).VarChar(500));
				columns.add(new ColumnType(Config.TS_CHANNEL, false).VarChar(20));
				columns.add(new ColumnType(Config.TS_FREQ, false).INT());
				
				con.createTable(Config.TBL_NM, columns);
				
			}
		}finally {
			if (con != null) {
			con.close();
			}
		}
		
		return exist;
	}
	
	public void add(Config config) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		try {
			con = new DBConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword, DbClass.H2 );

			con.buildAddQuery(Config.TBL_NM)
			.setParameter(Config.ENABLE_FAN, config.isEnableFan())
			.setParameter(Config.ENABLE_LIGHTS, config.isEnableLights())
			.setParameter(Config.ENABLE_TEMP, config.isEnableTempMon())
			.setParameter(Config.ENB_THINK_SPEAK, config.isEnableThinkSpeak())
			.setParameter(Config.ENB_WIRELESS, config.isEnableWireless())
			.setParameter(Config.LIGHT_END, config.getLightsStopTime())
			.setParameter(Config.LIGHT_START, config.getLightsStartTime())
			.setParameter(Config.MAINTAIN_TEMP, config.getMaintainTempAt()) 
			.setParameter(Config.PASS, config.getPassword())
			.setParameter(Config.SSID, config.getSsid())
			.setParameter(Config.TS_API_KEY, config.getApiKey())
			.setParameter(Config.TS_CHANNEL, config.getChannel())
			.setParameter(Config.TS_FREQ, config.getThinkSpeakIntv())
			.add();
		

		}finally {
			con.close();
		}
	}
	
	public void update(Config config) throws ClassNotFoundException, SQLException {
		DBConnection con = null;
		try {
			con = new DBConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword, DbClass.H2 );

			int upd = con.buildUpdateQuery(Config.TBL_NM)
					.setParameter(Config.ENABLE_FAN, config.isEnableFan())
					.setParameter(Config.ENABLE_LIGHTS, config.isEnableLights())
					.setParameter(Config.ENABLE_TEMP, config.isEnableTempMon())
					.setParameter(Config.ENB_THINK_SPEAK, config.isEnableThinkSpeak())
					.setParameter(Config.ENB_WIRELESS, config.isEnableWireless())
					.setParameter(Config.LIGHT_END, config.getLightsStopTime())
					.setParameter(Config.LIGHT_START, config.getLightsStartTime())
					.setParameter(Config.MAINTAIN_TEMP, config.getMaintainTempAt()) 
					.setParameter(Config.PASS, config.getPassword())
					.setParameter(Config.SSID, config.getSsid())
					.setParameter(Config.TS_API_KEY, config.getApiKey())
					.setParameter(Config.TS_CHANNEL, config.getChannel())
					.setParameter(Config.TS_FREQ, config.getThinkSpeakIntv())
					.update();

			if (upd < 1) {

				throw new SQLException("Error updating config. " + upd);

			}

		}finally {
			con.close();
		}
	}
	public Config loadConfig() throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		Config config = null;
		try {
			con = new DBConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword, DbClass.H2 );

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + Config.TBL_NM).getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					config = new Config(rs);
				}
			}

		}finally {
			con.close();
		}

		return config;
	}
}
