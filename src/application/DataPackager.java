package application;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JsonArray;
import org.json.simple.JsonObject;


public class DataPackager {
	String user_auth;
	ArrayList<String> id_list;
	ArrayList<Integer> type_list;
	ArrayList<HashMap<String, String>> data_list;
	
	public DataPackager()
	{
		id_list = new ArrayList<String>();
		type_list = new ArrayList<Integer>();
		data_list = new ArrayList<HashMap<String, String>>();
	}
	
	public String getDataAmountAsString() {
		return String.valueOf(type_list.size());
	}
	
	public void setUserAuth(String auth) {
		this.user_auth = auth;
	}
	
	public String getUserAuth() {
		return this.user_auth;
	}
	
	public void addSensorData(SensorData data)
	{
		id_list.add(data.getSensorID());
		type_list.add(data.getSensorType());
		data_list.add(data.getData());
	}
	
	public String getJSONStringWithData()
	{
		JsonObject result = new JsonObject();
		JsonArray json_arr = new JsonArray();
		if(data_list.size() > 0)
		{
			
			for(int i=0; i<data_list.size(); i++) 
			{
				JsonObject item = new JsonObject();
				
				item.put("id",id_list.get(i));
				JsonObject json_data = new JsonObject();
				int amount = SensorData.SensorKey.KEY_LIST[type_list.get(i)].length;
				//System.out.println("amount : " + String.valueOf(amount));
				for(int idx = 0; idx<amount; idx++) {
					String key = SensorData.SensorKey.KEY_LIST[type_list.get(i)][idx];
					String value = data_list.get(i).get(key);
					json_data.put(key, value);
				}
				item.put("data", json_data);
				json_arr.add(item);
			}
			result.put("raw", json_arr);
			return result.toJson();
		}
		else
		{
			return "";
		}
	}
	
}
