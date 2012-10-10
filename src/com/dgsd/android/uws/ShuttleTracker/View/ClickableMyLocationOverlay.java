package com.dgsd.android.uws.ShuttleTracker.View;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class ClickableMyLocationOverlay extends MyLocationOverlay {
    private boolean mAnimateOnNextLocationUpdate = false;
    private OnCurrentLocationClickListener mOnCurrentLocationClickListener;


    public ClickableMyLocationOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    @Override
    protected boolean dispatchTap() {
        GeoPoint p = this.getMyLocation();

        if(mOnCurrentLocationClickListener != null)
            mOnCurrentLocationClickListener.onCurrentLocationClick(p);
        return true;
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        if (mAnimateOnNextLocationUpdate) {
            mAnimateOnNextLocationUpdate = false;
            if(mOnCurrentLocationClickListener != null)
                mOnCurrentLocationClickListener.onCurrentLocationLoaded(location);
        }
    }

    public void notifyNextLocationUpdate(boolean notify) {
        mAnimateOnNextLocationUpdate = notify;
    }

    public void setOnCurrentLocationClickListener(OnCurrentLocationClickListener mOnCurrentLocationClickListener) {
        this.mOnCurrentLocationClickListener = mOnCurrentLocationClickListener;
    }

    public static interface OnCurrentLocationClickListener {
        public void onCurrentLocationClick(GeoPoint location);
        public void onCurrentLocationLoaded(Location location);
    }
}