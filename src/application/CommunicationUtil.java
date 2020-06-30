package application;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CommunicationUtil{
	ArrayList<SensorData> data_pool;
	boolean stop_flag = false;
	Thread sender_t;
	String str_response = "";
	String auth = "";
	
	Main callbackinstance = null;
	
	public CommunicationUtil() {
		data_pool = new ArrayList<SensorData>();
	}
	
	public CommunicationUtil(Main callback) {
		this.callbackinstance = callback;
	}
	
	public CommunicationUtil(String auth) {
		data_pool = new ArrayList<SensorData>();
		this.auth = auth; 
	}
	
	public void setCallbackInstance(Main callback) {
		this.callbackinstance = callback;
	}
	
	public synchronized int addDataToPool(SensorData data)
	{
		if(data_pool.size() == 0) {
			//System.out.println("added - when no data");
			data_pool.add(data);
		}
		else {
			boolean exist = false;
			int idx = 0;
			for(int i=0; i<data_pool.size(); i++) {
				if(data_pool.get(i).getSensorID().equals(data.getSensorID())) {
					if(data_pool.get(i).getSensorType() == data.getSensorType())	{
						exist = true;
						idx = i;	
					}
				}
			}
			if(exist) {
				data_pool.set(idx, data);
				//System.out.println("replaced");
			}
			else {
				data_pool.add(data);
				//System.out.println("added - new data");
			}
		}
		return data_pool.size()-1;
	}
	
	public synchronized void setSensorData(int pool_num, SensorData data) {
		data_pool.set(pool_num, data);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized ArrayList<SensorData> getSensorData(){
		//System.out.println("getdata");
		return (ArrayList<SensorData>)data_pool.clone();
	}
	
	public void stop() {
		//stop_flag = true;
	}
	
	public void start() {
		if(sender_t != null) {
			System.out.println("thread init..");
			stop_flag = true;
			sender_t.interrupt();
			sender_t = null;
		}
			
		stop_flag = false;
		sender_t = new Thread(new Sender());
		sender_t.start();
		System.out.println("thread run!");
	}
	
	public void authUser(String auth_code) {
		Thread t = new Thread(new UserAuth(auth_code));
		t.start();
	}
	
	public void getSensorList(String auth, String command) {
		Thread t = new Thread(new SensorUtil(auth, command));
		t.start();
	}
	
	public void addSensor(String auth, String command, SensorInfo sensor) {
		Thread t = new Thread(new SensorUtil(auth, command, sensor));
		t.start();
	}
	
	public void echoServer() {
		Thread t = new Thread(new Echo());
		t.start();
	}
	
	public class Echo implements Runnable{
		
		public Echo() {
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			try
			{
				URI uri = new URI(Main.root_url_goorm + "/Echo"); 
				uri = new URIBuilder(uri).addParameter("echo", "hello")
										 .build();
				
				HttpClient httpClient = HttpClientBuilder.create().build(); 
				HttpResponse response = httpClient.execute(new HttpPost(uri));
				HttpEntity entity = response.getEntity(); 
				str_response = EntityUtils.toString(entity); 
				
				System.out.println(str_response);
				
				JSONParser parser = new JSONParser();
				JSONObject jsonObj = (JSONObject) parser.parse(str_response);
		
				boolean post_result = (boolean) jsonObj.get("result");
				callbackinstance.callbackEchoResult(post_result);

			}
			catch(Exception e) {
				e.printStackTrace();
				callbackinstance.callbackEchoResult(false);
			}
		}
		
	}
	
	public class SensorUtil implements Runnable{
		String auth;
		String command;
		
		String id;
		int type;
		String baud;
		String desc;
		
		public static final String COMMAND_ADD = "add";
		public static final String COMMAND_EDIT = "edit";
		public static final String COMMAND_REMOVE = "delete";
		public static final String COMMAND_LIST = "list";
		
		public SensorUtil(String auth, String command, String id, String type, String baud, String desc) {
			this.auth = auth;
			this.command = command;
			this.id = id;
			if(type.equals("Dust Sensor"))
				type = "0";
			else
				type = "1";
			this.baud = baud;
			this.desc = desc;
		}
		
		public SensorUtil(String auth, String command) {
			this.auth = auth;
			this.command = command;
		}
		
		public SensorUtil(String auth, String command, SensorInfo s_info) {
			this.auth = auth;
			this.command = command;
			this.id = s_info.getSensorId();
			this.type = s_info.getSensorType();
			this.baud = s_info.getSensorBaud();
			this.desc = s_info.getSensorDesc();
		}
		
		@Override
		public void run() {
			
			if(this.command.equals(SensorUtil.COMMAND_ADD))
			{
				try
				{
					URI uri = new URI(Main.root_url_goorm + "/DeviceUtil"); 
					uri = new URIBuilder(uri).addParameter("command", this.command)
											 .addParameter("auth", this.auth)
											 .addParameter("id", this.id)
											 .addParameter("type", String.valueOf(this.type))
											 .addParameter("baud", this.baud)
											 .addParameter("desc", this.desc)
											 .build();
					
					HttpClient httpClient = HttpClientBuilder.create().build(); 
					HttpResponse response = httpClient.execute(new HttpPost(uri));
					HttpEntity entity = response.getEntity(); 
					str_response = EntityUtils.toString(entity); 
					
					System.out.println(str_response);
					
					JSONParser parser = new JSONParser();
					JSONObject jsonObj = (JSONObject) parser.parse(str_response);
			
					boolean post_result = (boolean) jsonObj.get("result");
					
					System.out.println("post result : " + String.valueOf(post_result));
					
					if(this.command.equals(SensorUtil.COMMAND_ADD)){
						callbackinstance.callbackResultAddSensor(post_result);
					}
					else if(this.command.equals(SensorUtil.COMMAND_EDIT)) {
						
					}
					else if(this.command.equals(SensorUtil.COMMAND_REMOVE)) {
						
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			else if(this.command.equals(SensorUtil.COMMAND_LIST))
			{
				try
				{
					URI uri = new URI(Main.root_url_goorm + "/DeviceUtil"); 
					uri = new URIBuilder(uri).addParameter("command", this.command)
											 .addParameter("auth", this.auth)
//											 .addParameter("id", this.id)
//											 .addParameter("type", String.valueOf(this.type))
//											 .addParameter("baud", this.baud)
//											 .addParameter("desc", this.desc)
											 .build();
					
					HttpClient httpClient = HttpClientBuilder.create().build(); 
					HttpResponse response = httpClient.execute(new HttpPost(uri));
					HttpEntity entity = response.getEntity(); 
					str_response = EntityUtils.toString(entity); 
					
					System.out.println(str_response);
					
					JSONParser parser = new JSONParser();
					JSONObject jsonObj = (JSONObject) parser.parse(str_response);
					
			
					boolean post_result = (boolean) jsonObj.get("result");
					
					if(post_result)
					{
						ArrayList<SensorInfo> s_list = new ArrayList<SensorInfo>();
						
						JSONArray jArr_sensor = (JSONArray)jsonObj.get("data");
						
						if(jArr_sensor.size() > 0)
						{
							Iterator<Object> iterator = jArr_sensor.iterator();
		                    while(iterator.hasNext()){
		                        JSONObject jsonObject = (JSONObject) iterator.next();
		                        Iterator<String> iter = jsonObject.keySet().iterator();
		                        String id = "";
		                        int type = -1;
		                        String baud = "";
		                        String desc = "";
		                        while(iter.hasNext())
		                        {
		                            String keyname = iter.next();
		                            //System.out.println("key : "+keyname+" value : "+jsonObject.get(keyname));
		                            if(keyname.equals("id"))
		                            	id = (String)jsonObject.get(keyname);
		                            if(keyname.equals("type"))
		                            	type = Integer.parseInt(String.valueOf(jsonObject.get(keyname)));
		                            if(keyname.equals("baud"))
		                            	baud = String.valueOf(jsonObject.get(keyname));
		                            if(keyname.equals("desc"))
		                            	desc = (String)jsonObject.get(keyname);
		                        }
		                        SensorInfo info = new SensorInfo(id, type, baud, desc);
		                        s_list.add(info);
		                    }
						}
						
						callbackinstance.callbackResultSensorList(post_result, s_list);
					}
					else
					{
						callbackinstance.callbackResultSensorList(post_result, null);
					}
					
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public class UserAuth implements Runnable{
		String code;
		public UserAuth(String code) {
			this.code = code;
		}

		@Override
		public void run(){
			// TODO Auto-generated method stub
			try {
				URI uri = new URI(Main.root_url_goorm + "/UserUtil"); 
				uri = new URIBuilder(uri).addParameter("type", "auth")
										 .addParameter("auth", this.code)
										 .build();
				
				HttpClient httpClient = HttpClientBuilder.create().build(); 
				HttpResponse response = httpClient.execute(new HttpPost(uri));
				HttpEntity entity = response.getEntity(); 
				str_response = EntityUtils.toString(entity); 
				
				System.out.println(str_response);
				
				JSONParser parser = new JSONParser();
				JSONObject jsonObj = (JSONObject) parser.parse(str_response);
		
				boolean post_result = (boolean) jsonObj.get("auth");
				
				System.out.println("post result : " + String.valueOf(post_result));
				
				callbackinstance.callbackResultAuth(post_result);
				
			}
			catch(Exception e) {
				
			}
		}
	}
	
	
	public class Sender implements Runnable{
		
		int delay = 10000;
		
		public Sender() { }
		
		public Sender(int delay) {
			this.delay = delay;
		}
		
		public void send() throws Exception {
			//System.out.println("send ready");
			

			ArrayList<SensorData> current_pool = getSensorData(); 
			
			if(current_pool.size() > 0) 
			{
				DataPackager pack = new DataPackager();
				pack.setUserAuth(auth);
				
				for(int i=0; i<current_pool.size(); i++) {
					pack.addSensorData(current_pool.get(i));
				}
				
				System.out.println(pack.getJSONStringWithData());
				
				URI uri = new URI(Main.root_url_goorm + "/Upload"); 
				uri = new URIBuilder(uri).addParameter("type", "no_date")
										 .addParameter("data", pack.getJSONStringWithData())
										 .addParameter("auth", auth)
										 .addParameter("amount", pack.getDataAmountAsString())
										 .build();
				
				HttpClient httpClient = HttpClientBuilder.create().build(); 
				HttpResponse response = httpClient.execute(new HttpPost(uri));
				HttpEntity entity = response.getEntity(); 
				str_response = EntityUtils.toString(entity); 
				
				//System.out.println(str_response);
				
				JSONParser parser = new JSONParser();
				JSONObject jsonObj = (JSONObject) parser.parse(str_response);
		
				boolean post_result = (boolean) jsonObj.get("success");
				
				System.out.println("post result : " + String.valueOf(post_result));
				
				if(post_result)
				{
		
				}
			}
			else
			{
				//System.out.println("no data");
			}
			
			//System.out.println("send end");
		}
		
		@Override
		public void run() {
			System.out.println("sending..");
			while(!stop_flag) 
			{
				try {
					//System.out.println("running..");
					Thread.sleep(delay);
					send();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//System.out.println("InterruptedException");
					System.out.println("Sender - InterruptedException Occurred");
					//e.printStackTrace();
				}
				catch(Exception e) {
					//System.out.println("Exception");
					System.out.println("Sender - Exception Occurred");
					//e.printStackTrace();
				}
			}
			//System.out.println("thread end");
		}
	}
}
