package com.richitec.waveformmonitor;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.richitec.waveformmonitor.adapter.BluetoothDeviceAdapter;
import com.richitec.waveformmonitor.constants.SystemConstants;

public class BluetoothDeviceListActivity extends Activity {
	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothAdapter mBtAdapter;
	private BluetoothDeviceAdapter pairedDevicesAdapter;
	private BluetoothDeviceAdapter newDevicesAdapter;

	private TextView titlePairedDevices;
	private TextView titleNewDevices;
	private Button scanButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		pairedDevicesAdapter = new BluetoothDeviceAdapter(this);
		ListView pairedList = (ListView) findViewById(R.id.paired_devices);
		pairedList.setAdapter(pairedDevicesAdapter);
		pairedList.setOnItemClickListener(onPairedDeviceClicked);

		newDevicesAdapter = new BluetoothDeviceAdapter(this);
		ListView newList = (ListView) findViewById(R.id.new_devices);
		newList.setAdapter(newDevicesAdapter);
		newList.setOnItemClickListener(onNewDeviceClicked);

		titlePairedDevices = (TextView) findViewById(R.id.title_paired_devices);
		titleNewDevices = (TextView) findViewById(R.id.title_new_devices);
		scanButton = (Button) findViewById(R.id.scan_device_bt);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		doDiscovery();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	public void onScanDeviceBt(View v) {
		doDiscovery();
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		Log.d(SystemConstants.TAG, "doDiscovery()");
		scanButton.setVisibility(View.GONE);

		pairedDevicesAdapter.removeAll();
		newDevicesAdapter.removeAll();

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			titlePairedDevices.setText(R.string.title_paired_devices);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesAdapter.add(device);
			}
		} else {
			titlePairedDevices.setText(R.string.no_paired_device_found);
		}

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					// mNewDevicesArrayAdapter.add(device.getName() + "\n"
					// + device.getAddress());
					newDevicesAdapter.add(device);
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (newDevicesAdapter.getCount() > 0) {
					titleNewDevices.setText(R.string.title_new_devices);
				} else {
					titleNewDevices.setText(R.string.no_new_device_found);
				}
				scanButton.setVisibility(View.VISIBLE);
			}
		}
	};

	private void returnDataBack(BluetoothDevice device) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_DEVICE_ADDRESS, device.getAddress());
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	private OnItemClickListener onPairedDeviceClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mBtAdapter.cancelDiscovery();
			BluetoothDevice device = (BluetoothDevice) pairedDevicesAdapter
					.getItem(position);
			returnDataBack(device);
		}
	};

	private OnItemClickListener onNewDeviceClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mBtAdapter.cancelDiscovery();
			BluetoothDevice device = (BluetoothDevice) newDevicesAdapter
					.getItem(position);
			returnDataBack(device);
		}
	};
}
