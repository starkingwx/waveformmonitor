package com.richitec.waveformmonitor.draws;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.richitec.waveformmonitor.constants.SystemConstants;

public class WaveformDraw implements SurfaceHolder.Callback {

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHodler;
	private int height;
	private int width;
	private int doubleWidth;
	private float[] dataArray;
	private static int MAX_VALUE = 200;

	private Paint paint;
	private Rect canvasRect;
	private DrawThread drawThread;

	public WaveformDraw(SurfaceView sfv) {

		this.surfaceView = sfv;
		surfaceHodler = surfaceView.getHolder();
		surfaceHodler.addCallback(this);

		width = surfaceView.getWidth();
		height = surfaceView.getHeight();
		doubleWidth = width * 2;

		Log.d(SystemConstants.TAG, "surface view width: " + width + " height: "
				+ height);

		dataArray = new float[doubleWidth];
		int j = 0;
		// init data array
		for (int i = 0; i < doubleWidth; i += 2) {
			dataArray[i] = j;
			dataArray[i + 1] = height - 0;
			j++;
		}

		canvasRect = new Rect(0, 0, width, height);

		paint = new Paint();
		paint.setColor(0xff54FF9F);
		paint.setStrokeWidth(1);
		paint.setAntiAlias(true);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Style.STROKE);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(SystemConstants.TAG, "surface destroyed");
		stopDraw();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(SystemConstants.TAG, "surface created");
		startDraw();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(SystemConstants.TAG, "surface surfaceChanged");
	}

	/**
	 * add data to fixed size buffer
	 * 
	 * @param data
	 */
	public void add(int data) {
		synchronized (surfaceHodler) {
			for (int i = 1; i < doubleWidth - 2; i += 2) {
				dataArray[i] = dataArray[i + 2];
			}
			dataArray[doubleWidth - 1] = height * (MAX_VALUE - data)
					/ MAX_VALUE;
		}
	}

	private void drawWave() {
		// Log.d(SystemConstants.TAG, "draw array: " +
		// Arrays.toString(dataArray));
		Canvas canvas = null;
		try {
			canvas = surfaceHodler.lockCanvas(canvasRect);

			synchronized (surfaceHodler) {
				if (canvas != null) {
					draw(canvas);
				}
			}
		} finally {
			if (canvas != null) {
				surfaceHodler.unlockCanvasAndPost(canvas);
			}
		}
	}

	private void draw(Canvas c) {
//		Log.d(SystemConstants.TAG, "do draw");
		
		c.drawColor(Color.BLACK);

		for (int i = 0; i < doubleWidth - 2; i += 2) {
			float x0 = dataArray[i];
			float y0 = dataArray[i + 1];
			float x1 = dataArray[i + 2];
			float y1 = dataArray[i + 3];
			c.drawLine(x0, y0, x1, y1, paint);
		}
		
//		 c.drawPoints(dataArray, paint);
	}

	public void startDraw() {
		stopDraw();

		drawThread = new DrawThread();
		drawThread.start();
	}

	public void stopDraw() {
		if (drawThread != null) {
			drawThread.setRuning(false);
		}
	}

	class DrawThread extends Thread {
		private boolean run = true;

		@Override
		public void run() {
			while (run) {
				drawWave();
			}
		}

		public void setRuning(boolean run) {
			this.run = run;
		}

		public boolean isRunning() {
			return run;
		}
	}

}
