package com.richitec.waveformmonitor.constants;

public enum BTConnectState {
	STATE_NONE(0), STATE_CONNECTING(1), STATE_CONNECTED(2), STATE_CONNECTION_LOST(3), STATE_CONNECT_FAILED(4);

	private int value;

	private BTConnectState(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
