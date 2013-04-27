package com.richitec.waveformmonitor.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.richitec.waveformmonitor.R;
import com.richitec.waveformmonitor.constants.BTConnectState;
import com.richitec.waveformmonitor.constants.MonitorMessage;
import com.richitec.waveformmonitor.constants.SystemConstants;

public class BTConnectService {
	private BluetoothAdapter btAdapter;
	private Context context;
	private Handler handler;
	private BTConnectState state;

	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;

	private BluetoothDevice currentDevice;

	private final UUID DEV_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public BTConnectService(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		state = BTConnectState.STATE_NONE;
	}

	public synchronized void setState(BTConnectState state) {
		this.state = state;
		handler.obtainMessage(MonitorMessage.STATE_CHANGE.value(),
				state.value(), -1).sendToTarget();
	}

	public synchronized BTConnectState getState() {
		return state;
	}

	public synchronized BluetoothDevice getCurrentDevice() {
		return currentDevice;
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		setState(BTConnectState.STATE_CONNECTION_LOST);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		setState(BTConnectState.STATE_CONNECT_FAILED);
	}

	public synchronized void reConnect() {
		if (currentDevice != null) {
			connect(currentDevice);
		} else {
			handler.obtainMessage(
					MonitorMessage.MSG_NO_DEVICE_TO_RECONNECT.value())
					.sendToTarget();
		}
	}

	public synchronized void connect(BluetoothDevice device) {
		Log.d(SystemConstants.TAG, "connect to: " + device);
		currentDevice = device;
		// Cancel any thread attempting to make a connection
		if (state == BTConnectState.STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(BTConnectState.STATE_CONNECTING);
	}
	
	 /**
     * Stop all threads
     */
    public synchronized void stop() {
    	Log.d(SystemConstants.TAG, "stop btconnect service");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
      
        setState(BTConnectState.STATE_NONE);
    }

	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		Log.d(SystemConstants.TAG, "device connected");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		setState(BTConnectState.STATE_CONNECTED);
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(DEV_UUID);
			} catch (IOException e) {
				Log.e(SystemConstants.TAG, "Socket create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(SystemConstants.TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			btAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(SystemConstants.TAG,
							"unable to close() socket during connection failure",
							e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BTConnectService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(SystemConstants.TAG, "close() of connect socket failed",
						e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(SystemConstants.TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
			} catch (IOException e) {
				Log.e(SystemConstants.TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
		}

		public void run() {
			Log.i(SystemConstants.TAG, "BEGIN mConnectedThread");

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					int data = mmInStream.read();
					// Send the obtained bytes to the UI Activity
					handler.obtainMessage(MonitorMessage.MSG_READ.value(),
							data, -1).sendToTarget();
				} catch (IOException e) {
					Log.e(SystemConstants.TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(SystemConstants.TAG, "close() of connect socket failed",
						e);
			}
		}
	}

}
