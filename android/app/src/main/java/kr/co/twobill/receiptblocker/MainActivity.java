package kr.co.twobill.receiptblocker;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DeviceCallback;
import me.aflak.bluetooth.DiscoveryCallback;

public class MainActivity extends AppCompatActivity {
	
	Bluetooth bluetooth = new Bluetooth(MainActivity.this);
	SensorManager sensorManager = null;
	
	TextView TV_bluetoothStatusText;
	TextView TV_magneticStatus;
	LinearLayout LL_log;
	TextView TV_log;
	Button BTN_fine;
	Button BTN_print;
	Button BTN_error;
	Button BTN_buzz;
	
	void initVar() {
		TV_bluetoothStatusText = findViewById(R.id.TV_bluetoothStatus);
		TV_magneticStatus = findViewById(R.id.TV_magneticStatus);
		LL_log = findViewById(R.id.LL_log);
		TV_log = findViewById(R.id.TV_log);
		BTN_fine = findViewById(R.id.BTN_fine);
		BTN_print = findViewById(R.id.BTN_print);
		BTN_error = findViewById(R.id.BTN_error);
		BTN_buzz = findViewById(R.id.BTN_buzz);
	}
	
	SensorEventListener sensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {
			synchronized (this) {
				if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					float x = sensorEvent.values[0];
					float y = sensorEvent.values[1];
					float z = sensorEvent.values[2];
					
					TV_magneticStatus.setText(((int) Math.sqrt(Math.pow(x, 2) + Math.pow(x, 2) + Math.pow(x, 2))) + "");
				}
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {
		
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initVar();
		
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		// Register magnetic sensor
		sensorManager.registerListener(sensorEventListener,
			sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
			SensorManager.SENSOR_DELAY_NORMAL);
		
		TedPermission.with(this)
			.setPermissionListener(new PermissionListener() {
				@Override
				public void onPermissionGranted() {
					start();
				}
				
				@Override
				public void onPermissionDenied(List<String> deniedPermissions) {
					finish();
				}
			})
			.setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
			.setPermissions(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
			.check();
	}
	
	void start() {
		setStatus("asdf", R.color.green);
		
		bluetooth.setBluetoothCallback(new BluetoothCallback() {
			@Override
			public void onBluetoothTurningOn() {
				log("1", "onBluetoothTurningOn");
				setStatus("onBluetoothTurningOn", R.color.orange);
			}
			
			@Override
			public void onBluetoothOn() {
				log("1", "onBluetoothOn");
				setStatus("onBluetoothTurningOn", R.color.blue);
			}
			
			@Override
			public void onBluetoothTurningOff() {
				log("1", "onBluetoothTurningOff");
				setStatus("onBluetoothTurningOn", R.color.orange);
			}
			
			@Override
			public void onBluetoothOff() {
				log("1", "onBluetoothOff");
				setStatus("onBluetoothOff", R.color.red);
			}
			
			@Override
			public void onUserDeniedActivation() {
				log("1", "onUserDeniedActivation");
				setStatus("onUserDeniedActivation", R.color.red);
			}
		});
		
		
		bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
			@Override
			public void onDiscoveryStarted() {
				log("2", "onDiscoveryStarted");
				//setStatus("onDiscoveryStarted", R.color.orange);
			}
			
			@Override
			public void onDiscoveryFinished() {
				log("2", "onDiscoveryFinished");
				//setStatus("onDiscoveryFinished", R.color.green);
			}
			
			@Override
			public void onDeviceFound(BluetoothDevice device) {
				log("2", "onDeviceFound");
				setStatus("onDeviceFound", R.color.green);
			}
			
			@Override
			public void onDevicePaired(BluetoothDevice device) {
				log("2", "onDevicePaired");
				setStatus("onDevicePaired", R.color.green);
			}
			
			@Override
			public void onDeviceUnpaired(BluetoothDevice device) {
				log("2", "onDeviceUnpaired");
				setStatus("onDeviceUnpaired", R.color.red);
			}
			
			@Override
			public void onError(String message) {
				log("2", "onError");
				setStatus("onError", R.color.red);
				
				
			}
		});
		
		bluetooth.setDeviceCallback(new DeviceCallback() {
			@Override
			public void onDeviceConnected(BluetoothDevice device) {
				log("3", "onDeviceConnected");
				setStatus("장치 연결됨", R.color.green);
			}
			
			@Override
			public void onDeviceDisconnected(BluetoothDevice device, String message) {
				log("3", "onDeviceDisconnected");
				setStatus("장치 연결 끊김", R.color.red);
			}
			
			@Override
			public void onMessage(String message) {
				try {
					String newMsg = new String(message.getBytes("euc-kr"), "utf-8");
					log("3", "onMessage");
					//setStatus("onMessage", R.color.orange);
					log("Receive", newMsg);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onError(String message) {
				log("3", "onError");
				setStatus("onError", R.color.red);
			}
			
			@Override
			public void onConnectError(BluetoothDevice device, String message) {
				log("3", "onConnectError");
				setStatus(device.getName() + " 장치 연결 실패", R.color.red);
			}
		});
		
		bluetooth.startScanning();
		setStatus("대기중", R.color.orange);
		
		BTN_fine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendToBluetooth("fine");
			}
		});
		BTN_print.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendToBluetooth("print");
			}
		});
		BTN_error.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendToBluetooth("error");
			}
		});
		BTN_buzz.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendToBluetooth("buzz");
			}
		});
	}
	
	void sendToBluetooth(String msg) {
		if (bluetooth.isConnected()) {
			bluetooth.send(msg);
			log("send", msg);
		} else
			log("system", "디바이스가 연결되어있지 않습니다.");
	}
	
	void selectDevice() {
		List<BluetoothDevice> devices = bluetooth.getPairedDevices();
		
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
		builderSingle.setTitle("블루투스 장치를 선택하세요");
		
		ArrayList<HashMap<String, String>> deviceList = new ArrayList<>();
		
		for (BluetoothDevice device : devices) {
			HashMap<String, String> item;
			item = new HashMap<>();
			item.put("name", device.getName());
			item.put("address", device.getAddress());
			deviceList.add(item);
		}
		final SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, deviceList, android.R.layout.simple_list_item_2,
			new String[]{"name", "address"}, new int[]{android.R.id.text1, android.R.id.text2});
		
		builderSingle.setAdapter(simpleAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int index) {
				HashMap<String, String> item = (HashMap<String, String>) simpleAdapter.getItem(index);
				//String name = item.get("name");
				String address = item.get("address");
				
				bluetooth.connectToAddress(address);
			}
		});
		builderSingle.show();
	}
	
	void setStatus(final String msg, final int colorRes) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TV_bluetoothStatusText.setText(msg);
				TV_bluetoothStatusText.setTextColor(getResources().getColor(colorRes));
			}
		});
	}
	
	
	// =========================================================================================
	
	void log(final String tag, final String msg) {
		Log.e("kr.co.twobill.log", tag + " : " +  msg);
		switch (tag) {
			case "1":
				break;
			case "2":
				break;
			case "3":
				break;
			default:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TV_log.append(tag + ": " + msg + "\n");
					}
				});
				break;
		}
		
	}
	
	
	// ========================================================================================= Menu
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		
		switch (id) {
			case R.id.menu_bluetooth:
				selectDevice();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	// ========================================================================================= Activity Cycle
	
	@Override
	protected void onStart() {
		super.onStart();
		bluetooth.onStart();
		bluetooth.enable();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		sensorManager.registerListener(sensorEventListener,
			sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
			SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(sensorEventListener);
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		bluetooth.onStop();
		sensorManager.unregisterListener(sensorEventListener);
	}
}
