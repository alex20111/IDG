package net.idg.db.entity;

import java.util.Date;

import net.idg.bean.Sensor;

public class SensorStatus {

	public static final String TBL_NM 			= "SENSOR_STATUS";
	public static final String ID 				= "id";
	public static final String SENSOR 			= "sensor";
	public static final String RECORDED_DATE 	= "record_date";
	public static final String FIELD1 			= "field_1";
	public static final String FIELD2 			= "field_2";
	public static final String FILED3 			= "filed_3";
	public static final String COMMENT 			= "comment";
	
	private int id = -1;
	private Sensor sensor = null;
	private Date recordedDate = null;
	private String field1 = "";
	private String field2 = "";		
	private String field3 = "";
	private String comment = "";
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public Date getRecordedDate() {
		return recordedDate;
	}

	public void setRecordedDate(Date recordedDate) {
		this.recordedDate = recordedDate;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	public String getField3() {
		return field3;
	}

	public void setField3(String field3) {
		this.field3 = field3;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	public static String checkIfTableExist() {
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NM+"'";
	}
}
