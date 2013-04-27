package com.richitec.waveformmonitor.draws;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class VerticalRulerView extends View {

	private VerticalRuler vRuler;
	private int MAX_VALUE = 200;
	public VerticalRulerView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	@Override
	public void draw(Canvas canvas) {
		if (vRuler == null) {
			int height = getHeight();
			vRuler = new VerticalRuler(0, height, MAX_VALUE);
		}
		vRuler.draw(canvas);
	
	}


}
