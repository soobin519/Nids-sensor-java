package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TooManyListenersException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.serial.*;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SensorManager {
	public static final int MAX_AMOUNT = 3;
	public static final String PORT_RASP_USB0 = "/dev/ttyUSB0";
	public static final String WIN_COM3 = "COM3";
	public static final String WIN_COM4 = "COM4";
	String auth = "";
	CommunicationUtil comm;

	ArrayList<SensorInfo> sensors;
	ArrayList<Thread> t_sensors;
	Main callbackinstance = null;

	public SensorManager() {
		sensors = new ArrayList<SensorInfo>();
	}

	public SensorManager(String auth) {
		this.auth = auth;
		sensors = new ArrayList<SensorInfo>();
	}

	public void setCallbackInstance(Main callbackinstance) {
		this.callbackinstance = callbackinstance;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public void addSensor(SensorInfo s_info) {
		sensors.add(s_info);
	}

	public void addSensor(List<SensorInfo> info) {
		for (int i = 0; i < info.size(); i++) {
			SensorInfo s = info.get(i);
			s.setRowIdx(i);
			sensors.add(s);
		}
	}

	public void removeAllSensor() {
		sensors.clear();
	}

	public SensorInfo getSensorInfo(int idx) {
		return sensors.get(idx);
	}

	public void removeSensor(int idx) {
		sensors.remove(idx);
	}

	public void removeSensorWithList(ArrayList<Integer> idx_list) {
		int amount = 0;
		for (Integer idx : idx_list) {
			sensors.remove(idx - amount);
			amount++;
		}
	}

	public void send() {
		if (sensors.size() > 0 && t_sensors.size() > 0) {
			System.out.println("Sender Start!");
			comm.start();
		}
	}

	public void readData() {
		if (sensors.size() > 0) {
			comm = new CommunicationUtil(this.auth);
			comm.setCallbackInstance(callbackinstance);
			t_sensors = new ArrayList<Thread>();
			for (int i = 0; i < sensors.size(); i++) {
				SensorInfo info = sensors.get(i);
				// Thread t = new Thread(new UARTSensor(info.getSensorId(),
				// info.getSensorType(), info.getSensorBaud()));
				// t_sensors.add(t);
				// t.start();
				if (info.getSensorType() == 0) {
					RxTxSerial w_ser = new RxTxSerial(PORT_RASP_USB0, info.getSensorId(), info.getSensorType(),
							info.getSensorBaud());

					if (w_ser.isConnected) {
						Thread t = new Thread(w_ser);
						t_sensors.add(t);
						t.start();
					}
				} else if (info.getSensorType() == 1) {
					I2CSensor i2c = new I2CSensor(info.getSensorId(), info.getSensorType());

					Thread i2ct = new Thread(i2c);
					i2ct.start();
				}
			}
		} else {
			System.out.println("no added sensor");
		}
	}

	public int getSensorAmount() {
		return this.sensors.size();
	}

	public void sendDataToServer() {
		if (comm != null) {

		} else {
			System.out.println("comm Obj is NULL");
		}
	}

	/*
	 * BLE Sensor
	 * 
	 */

	/*
	 * 
	 * RxTx Serial
	 * 
	 */

	public class RxTxSerial implements Runnable {

		String id;
		int type;
		int baud;
		HashMap<String, String> data;
		int row_idx = 0;

		InputStream in;
		BufferedReader buf_reader;
		boolean isConnected;
		ArrayList<String> tmp;

		int pool_size = 10;
		int avg_count = 5;

		ArrayList<Integer> pm10;
		ArrayList<Integer> pm25;
		ArrayList<Integer> pm01;

		SerialPort serialPort;

		public RxTxSerial(String port, String id, int type, String baud) {

			this.id = id;
			this.type = type;
			this.baud = Integer.parseInt(baud);
			data = new HashMap<String, String>();

			tmp = new ArrayList<String>();

			pm10 = new ArrayList<Integer>(pool_size);
			pm25 = new ArrayList<Integer>(pool_size);
			pm01 = new ArrayList<Integer>(pool_size);

			isConnected = false;
			try {
				connect(port);
			} catch (Exception e) {
				e.printStackTrace();
				isConnected = false;
			}
		}

		void connect(String portName) throws Exception {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				System.out.println("Error: Port is currently in use");
			} else {
				CommPort commPort = portIdentifier.open(this.getClass().getName(), 1000);

				if (commPort instanceof SerialPort) {
					serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					serialPort.enableReceiveThreshold(18);
					this.in = serialPort.getInputStream();
					buf_reader = new BufferedReader(new InputStreamReader(this.in));

					isConnected = true;
					System.out.println("Connection Success");
					// OutputStream out = serialPort.getOutputStream();

				} else {
					System.out.println("Error: Only serial ports are handled by this example.");
					isConnected = false;
				}
			}
		}

		public void run() {
			try {
				serialPort.notifyOnDataAvailable(true);
				serialPort.addEventListener(new SerialPortEventListener() {

					@Override
					public void serialEvent(SerialPortEvent arg0) {
						// TODO Auto-generated method stub
						try {
							/*
							 * System.out.println("read.."); String inputLine=null; if (buf_reader.ready())
							 * { inputLine = buf_reader.readLine().toString();
							 * System.out.println(inputLine); }
							 */
							byte[] buffer = new byte[18];
							int len = -1;
							while ((len = in.read(buffer)) > -1) {

								// System.out.print("len : " + String.valueOf(len) + " -- ");

								if (len != 0) {
									for (int i = 0; i < len; i++) {
										String one_data = String.format("%02x", buffer[i] & 0xff);
										/*
										 * if(i != len-1) System.out.print(one_data + ", "); else
										 * System.out.println(one_data);
										 */
										tmp.add(one_data);
									}
								}

								if (tmp.size() == 18) {
									StringBuilder sb = new StringBuilder();
									for (int i = 0; i < tmp.size(); i++) {
										if (i != tmp.size() - 1)
											sb.append(tmp.get(i) + ",");
										else
											sb.append(tmp.get(i));
									}
									tmp.clear();
									// System.out.println(sb.toString());
									String[] arr_str_hex = sb.toString().split(",");

									// System.out.print("PM1.0 : "+ String.valueOf(Integer.parseInt(arr_str_hex[11],
									// 16)) + ", ");
									// System.out.print("PM2.5 : "+ String.valueOf(Integer.parseInt(arr_str_hex[13],
									// 16)) + ", ");
									// System.out.println("PM10 : "+
									// String.valueOf(Integer.parseInt(arr_str_hex[15], 16)));

									data.put("PM1.0", String.valueOf(Integer.parseInt(arr_str_hex[11], 16)));
									data.put("PM2.5", String.valueOf(Integer.parseInt(arr_str_hex[13], 16)));
									data.put("PM10", String.valueOf(Integer.parseInt(arr_str_hex[15], 16)));

									SensorData s_data = new SensorData();
									s_data.setSensorID(id);
									s_data.setSensorType(type);
									s_data.setSensorData(data);

									comm.addDataToPool(s_data);
									// callbackinstance.changeSensorValue(row_idx, data);

								} else if (tmp.size() > 18) {
									tmp.clear();
								}

								// System.out.println(new String(buffer, 0, len));

								Thread.sleep(1000);
							}

						} catch (Exception e) {
							System.err.println(e.toString());
						}
					}

				});
			} catch (TooManyListenersException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * UART Sensor
	 * 
	 */
	public class UARTSensor implements Runnable {
		String id;
		int type;
		int baud;
		boolean stop_flag = false;
		HashMap<String, String> data;
		Serial serial;
		int row_idx;

		public UARTSensor(String id, int type, String baud) {
			this.id = id;
			this.type = type;
			this.baud = Integer.parseInt(baud);
			data = new HashMap<String, String>();
			serial = SerialFactory.createInstance();
		}

		public boolean isSerialOpen() {
			return serial.isOpen();
		}

		public void closeSerial() {
			if (serial.isOpen()) {
				try {
					serial.close();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			// create and register the serial data listener
			serial.addListener(new SerialDataEventListener() {
				@Override
				public void dataReceived(SerialDataEvent event) {

					// NOTE! - It is extremely important to read the data received from the
					// serial port. If it does not get read from the receive buffer, the
					// buffer will continue to grow and consume memory.

					// print out the data received to the console

					try {
						String str_hex = event.getHexByteString();
						String[] arr_str_hex = str_hex.split(",");
						ArrayList<Integer> arr_int = new ArrayList<Integer>();
						if (arr_str_hex.length > 16) {

							for (int i = 0; i < arr_str_hex.length; i++) {
								arr_int.add(Integer.parseInt(arr_str_hex[i], 16));
							}

							/*
							 * System.out.println("PC1.0 : " + arr_int.get(4) + ", " + arr_int.get(5) +
							 * " PC2.5 : " + arr_int.get(6) +", " + arr_int.get(7) + " PC10 : " +
							 * arr_int.get(8) +", " + arr_int.get(9) + " PM1.0 : " + arr_int.get(10) +", " +
							 * arr_int.get(11) + " PM2.5 : " + arr_int.get(12) +", " + arr_int.get(13) +
							 * " PM10 : " + arr_int.get(14) +", " + arr_int.get(15) );
							 */
							data.put("PM1.0", String.valueOf(arr_int.get(11)));
							data.put("PM2.5", String.valueOf(arr_int.get(13)));
							data.put("PM10", String.valueOf(arr_int.get(15)));

							SensorData s_data = new SensorData();
							s_data.setSensorID(id);
							s_data.setSensorType(type);
							s_data.setSensorData(data);

							comm.addDataToPool(s_data);
							// callbackinstance.changeSensorValue(row_idx, data);
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						// callbackinstance.addLog("IOException Occurred!");
						e.printStackTrace();
					}

					/*
					 * try { console.println("[HEX DATA]   " + event.getHexByteString());
					 * console.println("[ASCII DATA] " + event.getAsciiString()); } catch
					 * (IOException e) { e.printStackTrace(); }
					 */
				}
			});

			try {
				// create serial config object
				SerialConfig config = new SerialConfig();

				// set default serial settings (device, baud rate, flow control, etc)
				//
				// by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO
				// header)
				// NOTE: this utility method will determine the default serial port for the
				// detected platform and board/model. For all Raspberry Pi models
				// except the 3B, it will return "/dev/ttyAMA0". For Raspberry Pi
				// model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
				// environment configuration.
				/*
				 * config.device(SerialPort.getDefaultPort()) .baud(Baud._9600)
				 * .dataBits(DataBits._8) .parity(Parity.NONE) .stopBits(StopBits._1)
				 * .flowControl(FlowControl.NONE);
				 */
				if (baud == 9600)
					// uart direct
					// config.device("/dev/ttyAMA0").baud(Baud._9600);

					// uart to usb
					config.device("/dev/ttyUSB0").baud(Baud._9600);

				// parse optional command argument options to override the default serial
				// settings

				// open the default serial device/port with the configuration settings
				serial.open(config);

			} catch (IOException ex) {
				// console.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
				ex.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/*
	 * 
	 * I2C Sensor
	 * 
	 */
	public class I2CSensor implements Runnable {
		public static final int ADDRESS = 0x2A;
		String id;
		int type;

		HashMap<String, String> i2cData;
		ArrayList<Double> temp;
		ArrayList<Double> humi;

		public I2CSensor(String id, int type) {
			this.id = id;
			this.type = type;
			i2cData = new HashMap<String, String>();

			temp = new ArrayList<Double>();
			humi = new ArrayList<Double>();

		}

		@Override
		public void run() {
			try {
				// TODO Auto-generated method stub
				byte[] buffer = new byte[28];
				double[] info = new double[7];
				I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
				I2CDevice device = i2c.getDevice(ADDRESS);
				int result = 0;
				while (true) {
					try {
						Thread.sleep(5000);
						result = device.read(0, buffer, 0, buffer.length);
						System.out.println("read Success");
						for (int tmp = 0; tmp < 28; tmp++) {
							buffer[tmp] = (byte) (buffer[tmp] & 0xFF);
						}
						for (int index = 0; index < 28; index++) {
							if (index % 4 == 3) {
								info[index / 4] = (buffer[index - 3] & 0xFF) + (buffer[index - 2] & 0xFF) * 256
										+ (buffer[index - 1] & 0xFF) * (Math.pow(256, 2))
										+ (buffer[index] & 0xFF) * (Math.pow(256, 3));
							}
						}
						i2cData.put("temp", String.valueOf(info[0] / 100.0));
						i2cData.put("humi", String.valueOf(info[1] / 100.0));

						SensorData s_data = new SensorData();
						s_data.setSensorID(id);
						s_data.setSensorType(type);
						s_data.setSensorData(i2cData);

						comm.addDataToPool(s_data);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
