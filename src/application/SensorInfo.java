package application;

public class SensorInfo {
	public final static String TYPE_DUST = "DUST";
	public final static String TYPE_HUMI = "HUMI";
	
	int row_idx;
	String sensor_id;
	int sensor_type;
	String sensor_baud;
	String sensor_desc;
	
	public SensorInfo(String id, int type, String baud, String desc) {
		this.sensor_id = id;
		this.sensor_type = type;
		this.sensor_baud = baud;
		this.sensor_desc = desc;
	}
	
	public SensorInfo(String id, int type, String baud, String desc, int row_idx) {
		this.sensor_id = id;
		this.sensor_type = type;
		this.sensor_baud = baud;
		this.sensor_desc = desc;
		this.row_idx = row_idx;
	}
	
	public void setRowIdx(int row_idx) {
		this.row_idx = row_idx;
	}
	
	public int getRowIdx() {
		return row_idx;
	}
	public String getSensorId() {
		return sensor_id;
	}
	public int getSensorType() {
		return sensor_type;
	}
	public String getSensorBaud() {
		return sensor_baud;
	}
	public String getSensorDesc() {
		return sensor_desc;
	}
}
