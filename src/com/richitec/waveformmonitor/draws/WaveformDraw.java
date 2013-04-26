package com.richitec.waveformmonitor.draws;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.richitec.waveformmonitor.constants.SystemConstants;

import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaveformDraw {
	
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHodler;
	private int height;
	private int width;
	private float[] dataArray;
	private static int MAX_VALUE = 200;
	
	private Paint paint;
	public WaveformDraw(SurfaceView sfv) {
		this.surfaceView = sfv;
		surfaceHodler = surfaceView.getHolder();
		
		width = surfaceView.getWidth();
		height = surfaceView.getHeight();
		
		Log.d(SystemConstants.TAG, "surface view width: " + width + " height: " + height);
		
		dataArray = new float[width];
		Arrays.fill(dataArray, 0);
		
		paint = new Paint();
		paint.setColor(0xFF00FF00);
		paint.setStrokeWidth(1);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
	}
	
	/**
	 * add data to fixed size buffer
	 * @param data
	 */
	public void add(int data) {
		for (int i = 0; i < width - 1; i++) {
			dataArray[i] = dataArray[i + 1];
		}
		dataArray[width - 1] = height * (MAX_VALUE - data) / MAX_VALUE;
	}
	
	
	
}
