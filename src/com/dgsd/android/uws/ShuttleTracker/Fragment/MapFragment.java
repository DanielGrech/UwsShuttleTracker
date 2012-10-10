package com.dgsd.android.uws.ShuttleTracker.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.dgsd.android.uws.ShuttleTracker.BuildConfig;
import com.dgsd.android.uws.ShuttleTracker.Service.ApiService;
import com.dgsd.android.uws.ShuttleTracker.Util.OnGetMapViewListener;
import com.dgsd.android.uws.ShuttleTracker.View.ClickableMyLocationOverlay;
import com.google.android.maps.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Grech
 */
public class MapFragment extends SherlockFragment implements ClickableMyLocationOverlay.OnCurrentLocationClickListener {
    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int HANDLER_DELAY = 1000 * 60; // Every minute!

    private OnGetMapViewListener mOnGetMapViewListener;
    private ClickableMyLocationOverlay mCurrentLocationOverlay;
    private Handler mHandler;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getActivity() != null && MapFragment.this.isResumed())
                    ApiService.requestVehicleReading(getActivity());

                mHandler.postDelayed(this, HANDLER_DELAY);
            }
        }, HANDLER_DELAY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MapView mv = getMapView();
        if (mv != null && mv.getParent() != null && mv.getParent() instanceof ViewGroup) {
            try {
                //View already has a parent, better remove it!
                ((ViewGroup) mv.getParent()).removeView(mv);
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "Error removing MapView from parent", e);
            }
        }

        mCurrentLocationOverlay = new ClickableMyLocationOverlay(getActivity(), mv);
        mCurrentLocationOverlay.setOnCurrentLocationClickListener(this);
        mv.getOverlays().add(mCurrentLocationOverlay);

        return mv;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mOnGetMapViewListener != null) {
            ViewGroup parent = (ViewGroup) mOnGetMapViewListener.onGetMapView().getParent();
            if (parent != null) {
                parent.removeView(mOnGetMapViewListener.onGetMapView());
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        ApiService.requestVehicleReading(getActivity());

        animateToCurrentLocation();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentLocationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCurrentLocationOverlay.disableMyLocation();
    }

    public void animateToCurrentLocation() {
        final MapView mv = getMapView();
        if(mv == null)
            return;

        final GeoPoint p = mCurrentLocationOverlay.getMyLocation();
        if(p == null) {
            mCurrentLocationOverlay.notifyNextLocationUpdate(true);
            return;
        }

        mv.getController().animateTo(p);
    }

    public void setOnGetMapViewListener(OnGetMapViewListener onGetMapViewListener) {
        this.mOnGetMapViewListener = onGetMapViewListener;
    }

    private MapView getMapView() {
        if (mOnGetMapViewListener != null)
            return mOnGetMapViewListener.onGetMapView();
        else
            return ((OnGetMapViewListener) getActivity()).onGetMapView();
    }

    @Override
    public void onCurrentLocationClick(GeoPoint location) {

    }

    @Override
    public void onCurrentLocationLoaded(Location location) {
        animateToCurrentLocation();
    }
}
