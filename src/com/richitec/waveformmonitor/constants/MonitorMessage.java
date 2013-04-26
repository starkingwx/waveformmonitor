package com.richitec.waveformmonitor.constants;

public enum MonitorMessage {
	STATE_CHANGE(0), MSG_READ(1), MSG_NO_DEVICE_TO_RECONNECT(2);

	private int value;

	private MonitorMessage(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
