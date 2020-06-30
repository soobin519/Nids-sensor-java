package application;

import java.awt.Color;
import java.util.List;

public class Main {
	
	SensorManager s_manager;
	//public static final String auth_code = "C611F9AC747D217D206401AC2E5ABB77"; 
	//public static final String root_url_goorm = "https://spring-nids-kglhs.run.goorm.io";
	public static final String auth_code = "17557D24A4908324C6E43B16027392D7";
	public static final String root_url_goorm = "https://nids-spring-psdg.run.goorm.io/";
	//public static final String root_url_aws = "http://nidsprojtestapp.372fabauwi.us-east-1.elasticbeanstalk.com";
	public static final boolean automatic_login = false;
	
	public static void main(String[] args) {
		Main m = new Main();
		m.start();
	}
	
	public Main() {
		s_manager = new SensorManager();
		s_manager.setCallbackInstance(this);
		CommunicationUtil c_util = new CommunicationUtil(this);
		c_util.echoServer();
	}
	
	public void start() {
		this.doAuth();
	}

	public void callbackEchoResult(boolean result) {
		if(result) {
			//connected		
			System.out.println("connected!");
        	
		}
		else {
			//connection failed
			System.out.println("server connection failed..");
		}
	}
	
	public void callbackResultAddSensor(boolean result) {
		if(result)
		{

		}
	}
	
	public void callbackResultDeleteSensor(boolean result) {
		
	}
	
	public void doAuth() {
		CommunicationUtil c_util = new CommunicationUtil();
		c_util.setCallbackInstance(this);
		String auth_code = Main.auth_code;
		c_util.authUser(auth_code);
	}
	
	public void callbackResultAuth(boolean result) {
		if(result)
		{		
			System.out.println("auth success");
			System.out.println("request sensor list...");
			s_manager.setAuth(Main.auth_code);
			CommunicationUtil c_util = new CommunicationUtil(this);
			String auth_code = Main.auth_code;
			c_util.getSensorList(auth_code, CommunicationUtil.SensorUtil.COMMAND_LIST);
		}
		else
		{
			System.out.println("auth failed");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doAuth();
		}
	}
	
	public void callbackResultSensorList(boolean result, List<SensorInfo> s_list) {
		if(result)
		{
			System.out.println("download complete sensor list : " + String.valueOf(s_list.size()));
			
			if(s_manager.getSensorAmount() > 0) 
				s_manager.removeAllSensor();
			s_manager.addSensor(s_list);
			s_manager.readData();
			
			if(s_manager.getSensorAmount() > 0) {
				s_manager.send();
			}
		}
		else
		{
			System.out.println("cannot download sensor list!");
			         	
	    	
		}
	}
	
}
