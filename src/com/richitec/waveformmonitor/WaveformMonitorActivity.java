package com.richitec.waveformmonitor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.richitec.waveformmonitor.constants.BTConnectState;
import com.richitec.waveformmonitor.constants.MonitorMessage;
import com.richitec.waveformmonitor.constants.SystemConstants;
import com.richitec.waveformmonitor.draws.WaveformDraw;
import com.richitec.waveformmonitor.service.BTConnectService;

public class WaveformMonitorActivity extends Activity {
	private BluetoothAdapter bluetoothAdapter;
	private WakeLock wakeLock;
	private BTConnectService btConnectService;

	private static final int REQUEST_SCAN_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	private TextView currentDeviceTV;
	private TextView connectionStatusTV;

	private WaveformDraw waveformDraw;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waveform_monitor);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.no_bluetooth_found, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		PowerManager powerMan = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = powerMan.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
		wakeLock.acquire();

		btConnectService = new BTConnectService(this, mHandler);

		currentDeviceTV = (TextView) findViewById(R.id.current_device_name_tv);
		connectionStatusTV = (TextView) findViewById(R.id.connection_status_tv);

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.e(SystemConstants.TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (btConnectService.getState() != BTConnectState.STATE_CONNECTED) {
				startDeviceScanning();
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	private void startDeviceScanning() {
		Intent intent = new Intent(this, BluetoothDeviceListActivity.class);
		startActivityForResult(intent, REQUEST_SCAN_DEVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_waveform_monitor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(SystemConstants.TAG, "item id: " + item.getItemId());
		if (item.getItemId() == R.id.menu_scan_device) {
			startDeviceScanning();
		}
		return false;
	}

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MonitorMessage.STATE_CHANGE.value()) {
				if (msg.arg1 == BTConnectState.STATE_CONNECTING.value()) {
					BluetoothDevice device = btConnectService
							.getCurrentDevice();
					currentDeviceTV.setText(device.getName());
					connectionStatusTV.setText(R.string.connecting);
				} else if (msg.arg1 == BTConnectState.STATE_CONNECTED.value()) {
					connectionStatusTV.setText(R.string.connected);

					if (waveformDraw == null) {
						SurfaceView sfv = (SurfaceView) findViewById(R.id.waveform_canvas);
						waveformDraw = new WaveformDraw(sfv);
						waveformDraw.startDraw();
					}

				} else if (msg.arg1 == BTConnectState.STATE_CONNECT_FAILED
						.value()) {
					connectionStatusTV.setText(R.string.connect_failed);
					Toast.makeText(WaveformMonitorActivity.this,
							R.string.connect_failed, Toast.LENGTH_SHORT).show();
				} else if (msg.arg1 == BTConnectState.STATE_CONNECTION_LOST
						.value()) {
					connectionStatusTV.setText(R.string.connection_lost);
					Toast.makeText(WaveformMonitorActivity.this,
							R.string.connection_lost, Toast.LENGTH_SHORT)
							.show();
				}
			} else if (msg.what == MonitorMessage.MSG_READ.value()) {
				int data = msg.arg1;
				Log.d(SystemConstants.TAG, "read data: " + data);
				if (data != 7) {
					waveformDraw.add(data);
				}
			} else if (msg.what == MonitorMessage.MSG_NO_DEVICE_TO_RECONNECT
					.value()) {

			}
		}

	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(SystemConstants.TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled
				startDeviceScanning();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(SystemConstants.TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case REQUEST_SCAN_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String address = data
						.getStringExtra(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);

				BluetoothDevice device = bluetoothAdapter
						.getRemoteDevice(address);
				btConnectService.connect(device);
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		if (wakeLock != null) {
			wakeLock.release();
		}
		if (waveformDraw != null) {
			waveformDraw.stopDraw();
		}
		if (btConnectService != null) {
			btConnectService.stop();
		}
		super.onDestroy();
	}

}
