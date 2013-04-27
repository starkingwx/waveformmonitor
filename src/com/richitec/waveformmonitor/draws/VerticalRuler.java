package com.richitec.waveformmonitor.draws;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class VerticalRuler {
	private float x;
	private int length;
	private int maxValue;
	private int markLen;
	private int sectionNum;
	private float[] markYArray;
	private int[] markNumArray;
	private Paint paint;
	private Paint textPaint;
	private int textSize;

	public VerticalRuler(float xPos, int length, int maxValue) {
		this.x = xPos;
		this.length = length;
		this.maxValue = maxValue;
		this.markLen = 4;
		this.sectionNum = 10;
		this.textSize = 16;
		markYArray = new float[sectionNum + 1];
		markNumArray = new int[sectionNum + 1];

		float secLen = length / sectionNum;
		int markDelta = maxValue / sectionNum;
		float y = 0;
		int markNum = maxValue;
		for (int i = 0; i < markYArray.length; i++) {
			markYArray[i] = y;
			y += secLen;

			markNumArray[i] = markNum;
			markNum -= markDelta;
		}

		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(2);
		paint.setStyle(Style.STROKE);

		textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(textSize);
	}

	public void draw(Canvas c) {
		// draw vertical line
		c.drawLine(x, 0, x, length, paint);

		// draw mark line
		for (int i = 0; i < markYArray.length; i++) {
			c.drawLine(x, markYArray[i], x + markLen, markYArray[i], paint);
			// draw mark number
			if (i == 0) {
				c.drawText("" + markNumArray[i], x + markLen + 2, markYArray[i]
						+ textSize, textPaint);
			} else {
				c.drawText("" + markNumArray[i], x + markLen + 2,
						markYArray[i], textPaint);
			}
		}
	}
}
