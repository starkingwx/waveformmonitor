package com.richitec.waveformmonitor.adapter;

import java.util.ArrayList;
import java.util.List;

import com.richitec.waveformmonitor.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BluetoothDeviceAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<BluetoothDevice> devices;
	
	public BluetoothDeviceAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		devices = new ArrayList<BluetoothDevice>();
	}
	
	public void add(BluetoothDevice device) {
		devices.add(device);
		notifyDataSetChanged();
	}
	
	public void removeAll() {
		devices.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return devices.size();
	}

	@Override
	public Object getItem(int position) {
		return devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.device_name, null);
			viewHolder.deviceNameTV = (TextView) convertView.findViewById(R.id.device_name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		BluetoothDevice device = (BluetoothDevice) getItem(position);
		viewHolder.deviceNameTV.setText(device.getName() + "\n" + device.getAddress());
		
		return convertView;
	}

	final class ViewHolder {
		TextView deviceNameTV;
	}
}
