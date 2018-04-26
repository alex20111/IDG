package net.idg.db;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.DbClass;
import home.db.PkCriteria;
import net.idg.bean.Constants;
import net.idg.bean.Sensor;
import net.idg.db.entity.Config;
import net.idg.db.entity.SensorStatus;

public class SensorSql {
	
	public boolean CreateSensorTable() throws ClassNotFoundException, SQLException, IOException {
		DBConnection con = null;
		boolean exist = false;
		try {
			con = new DBConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword, DbClass.H2 );
			
			ResultSet rs = con.createSelectQuery(SensorStatus.checkIfTableExist()).getSelectResultSet();
			
			exist = rs.next();
			
			if (!exist) {
				List<ColumnType> columns = new ArrayList<ColumnType>();
				columns.add(new ColumnType(SensorStatus.ID, true).INT().setPkCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(SensorStatus.RECORDED_DATE).TimeStamp());
				columns.add(new ColumnType(SensorStatus.COMMENT).VarChar(2000));
				columns.add(new ColumnType(SensorStatus.FIELD1).VarChar(50));
				columns.add(new ColumnType(SensorStatus.FIELD2).VarChar(50));
				columns.add(new ColumnType(SensorStatus.FILED3).VarChar(50));
				columns.add(new ColumnType(SensorStatus.SENSOR).VarChar(40));

				
				con.createTable(SensorStatus.TBL_NM, columns);
				
				//add index
				con.createSelectQuery("create INDEX c_dt on " + SensorStatus.TBL_NM +"("+SensorStatus.RECORDED_DATE+")");
				con.executeUpdate();
				
				con.createSelectQuery("create INDEX c_sensorIdx on " + SensorStatus.TBL_NM +"("+SensorStatus.SENSOR+")");
				con.executeUpdate();
				
			}
		}finally {
			if (con != null) {
			con.close();
			}
		}
		
		return exist;
	}
	
	public void add(SensorStatus sensorStat) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		try {
			con = new DBConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword, DbClass.H2 );

			con.buildAddQuery(SensorStatus.TBL_NM)
			.setParameter(SensorStatus.COMMENT, sensorStat.getComment())
			.setParameter(SensorStatus.FIELD1, sensorStat.getField1())
			.setParameter(SensorStatus.FIELD2, sensorStat.getField2())
			.setParameter(SensorStatus.FILED3, sensorStat.getField3())
			.setParameter(SensorStatus.RECORDED_DATE, sensorStat.getRecordedDate())
			.setParameter(SensorStatus.SENSOR, sensorStat.getSensor().name())
			

			.add();
		

		}finally {
			con.close();
		}
	}
	public Config loadAll(Date dt) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		Config config = null;
		
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(dt);
		startDate.set(Calendar.HOUR_OF_DAY, 00);
		startDate.set(Calendar.MINUTE,00);
		startDate.set(Calendar.SECOND, 00);
		Calendar endDate   = Calendar.getInstance();
		endDate.setTime(dt);
		endDate.set(Calendar.HOUR_OF_DAY, 23);
		endDate.set(Calendar.MINUTE,59);
		endDate.set(Calendar.SECOND, 59);
		
		try {
			con = new DBConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword, DbClass.H2 );

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + SensorStatus.TBL_NM + " WHERE " + SensorStatus.RECORDED_DATE + 
					" BETWEEN '" + new Timestamp(startDate.getTimeInMillis()) + "' AND '" +new Timestamp(endDate.getTimeInMillis())+ "'" ).getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
//					config = new Config(rs);
					System.out.println(rs.getInt(SensorStatus.ID));
				}
			}

		}finally {
			con.close();
		}

		return config;
	}
	

	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException {
		SensorSql s = new SensorSql();
		s.CreateSensorTable();
		
		SensorStatus s2 = new SensorStatus();
		s2.setComment("alalala");
		s2.setRecordedDate(new Date());
		s2.setSensor(Sensor.FAN);
		s.add(s2);
		
		s.loadAll(new Date());
		
		
		
	}

}
