package com.dgsd.android.uws.ShuttleTracker;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dgsd.android.uws.ShuttleTracker.Fragment.MapFragment;
import com.dgsd.android.uws.ShuttleTracker.Util.OnGetMapViewListener;
import com.dgsd.android.uws.ShuttleTracker.View.DoubleTapMapView;
import com.google.android.maps.MapView;

public class MainActivity extends SherlockFragmentActivity implements OnGetMapViewListener {
    private static final String KEY_MAP_FRAGMENT = "_map_fragment";

    protected MapView mMapView;
    private MapFragment mMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_single_fragment);

        final FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState != null)
            mMapFragment = (MapFragment) fm.getFragment(savedInstanceState, KEY_MAP_FRAGMENT);

        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance();
            fm.beginTransaction().replace(R.id.container, mMapFragment).commit();
        }

        mMapFragment.setOnGetMapViewListener(this);
    }


    @Override
    public MapView onGetMapView() {
        if(mMapView == null) {
            mMapView = new DoubleTapMapView(this, getResources().getString(R.string.maps_api_key));
            mMapView.setClickable(true);
            mMapView.setBuiltInZoomControls(false);
            mMapView.getController().setZoom(15);
        }

        return mMapView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMapFragment != null && mMapFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, KEY_MAP_FRAGMENT, mMapFragment);
    }
}
