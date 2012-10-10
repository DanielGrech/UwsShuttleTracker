package com.dgsd.android.uws.ShuttleTracker.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.google.android.maps.MapView;

/**
 * @author Daniel Grech
 */
public class DoubleTapMapView extends MapView {
	private static final String TAG = DoubleTapMapView.class.getSimpleName();

	private long lastTouchTime = -1;

	public DoubleTapMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	public DoubleTapMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DoubleTapMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_DOWN) {
			long thisTime = System.currentTimeMillis();
			if(thisTime - lastTouchTime < 250) {
				// Double tap
				this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
				lastTouchTime = -1;
			} else {
				// Too slow :)
				lastTouchTime = thisTime;
			}
		}

		return super.onInterceptTouchEvent(ev);
	}

}
