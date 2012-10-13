package com.dgsd.android.uws.ShuttleTracker;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cyrilmottier.polaris.PolarisMapView;
import com.dgsd.android.uws.ShuttleTracker.Fragment.MapFragment;
import com.dgsd.android.uws.ShuttleTracker.Fragment.ShuttleListFragment;
import com.dgsd.android.uws.ShuttleTracker.Model.VehicleReading;
import com.dgsd.android.uws.ShuttleTracker.Util.OnGetMapViewListener;
import com.dgsd.android.uws.ShuttleTracker.Util.Prefs;

public class MainActivity extends SherlockFragmentActivity implements OnGetMapViewListener, ActionBar.TabListener, ShuttleListFragment.OnReadingClickListener {
    private static final String KEY_MAP_FRAGMENT = "_map_fragment";
    private static final String KEY_LIST_FRAGMENT = "_list_fragment";

    private static final String KEY_LAST_SELECTED_TAB = "_last_selected_tab";

    private ViewFlipper mViewFlipper;
    protected PolarisMapView mMapView;
    private MapFragment mMapFragment;
    private ShuttleListFragment mListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

        final FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mMapFragment = (MapFragment) fm.getFragment(savedInstanceState, KEY_MAP_FRAGMENT);
            mListFragment = (ShuttleListFragment) fm.getFragment(savedInstanceState, KEY_LIST_FRAGMENT);
        }

        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mMapFragment).commit();
        }

        if(mListFragment == null) {
            mListFragment = ShuttleListFragment.newInstance();
            fm.beginTransaction().replace(R.id.list_container, mListFragment).commit();
        }

        mMapFragment.setOnGetMapViewListener(this);
        mListFragment.setOnReadingClickListener(this);

        final ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.addTab(ab.newTab().setText("MAP").setTabListener(this));
        ab.addTab(ab.newTab().setText("LIST").setTabListener(this));

        ab.setSelectedNavigationItem(Prefs.getInstance(this).get(KEY_LAST_SELECTED_TAB, 0));
    }


    @Override
    public PolarisMapView onGetMapView() {
        if(mMapView == null) {
            mMapView = new PolarisMapView(this, getResources().getString(R.string.maps_api_key));
            mMapView.getController().setZoom(15);
            mMapView.setUserTrackingButtonEnabled(true);
            mMapView.setCurrentLocationMarker("Current location", null);
        }

        return mMapView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapFragment != null && mMapFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, KEY_MAP_FRAGMENT, mMapFragment);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Prefs.getInstance(this).set(KEY_LAST_SELECTED_TAB, getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewFlipper.setDisplayedChild(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onClick(VehicleReading reading) {
        getSupportActionBar().setSelectedNavigationItem(0);
        mMapFragment.selectAnnotationAt(reading.location.toGeoPoint());
    }
}
