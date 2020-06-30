package application;

import java.util.HashMap;

public class SensorData {
	private String sensor_id;
	private int sensor_type;
	private HashMap<String, String> data;
	
	public SensorData(){
		data = new HashMap<String, String>();
	}
	
	public SensorData(String id, int type) {
		data = new HashMap<String, String>();
		this.sensor_id = id;
		this.sensor_type = type;
	}
	
	public void setSensorID(String id) {
		this.sensor_id = id;
	}
	public String getSensorID() {
		return this.sensor_id;
	}
	
	public void setSensorType(int type) {
		this.sensor_type = type;
	}
	
	public void setSensorType(String type) {
		this.sensor_type = Integer.parseInt(type);
	}
	
	public int getSensorType() {
		return this.sensor_type;
	}
	
	public void setSensorData(String key, String val) {
		data.put(key, val);
	}
	
	public void setSensorData(HashMap<String, String> data) {
		this.data = data; 
	}
	
	public HashMap<String, String> getData()
	{
		if(data != null)
			return data;
		else
			return null;
	}
	
	public class SensorDataType{
		public static final int TYPE_DUST = 0;
		public static final int TYPE_TEMP_HUMI = 1;
	}
	
	public static class SensorKey{
		public static String[][] KEY_LIST = {{"PM10", "PM2.5", "PM1.0"}, {"temp", "humi"}};
	}
}
