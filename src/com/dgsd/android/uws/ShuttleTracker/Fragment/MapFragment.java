package com.dgsd.android.uws.ShuttleTracker.Fragment;

import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.dgsd.android.uws.ShuttleTracker.BuildConfig;
import com.dgsd.android.uws.ShuttleTracker.Data.DbField;
import com.dgsd.android.uws.ShuttleTracker.Data.Provider;
import com.dgsd.android.uws.ShuttleTracker.Service.ApiService;
import com.dgsd.android.uws.ShuttleTracker.Util.OnGetMapViewListener;
import com.google.android.maps.*;

/**
 * @author Daniel Grech
 */
public class MapFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MapFragment.class.getSimpleName();

    public static final int LOADER_ID_STOPS = 0x01;
    public static final int LOADER_ID_READINGS = 0x02;

    private static final int HANDLER_DELAY = 1000 * 60; // Every minute!

    private OnGetMapViewListener mOnGetMapViewListener;
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

        getLoaderManager().initLoader(LOADER_ID_READINGS, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch(id) {
            case LOADER_ID_READINGS:
                //TODO: Proper query: select * from readings where _id in (select max(_id) from readings group by _name);

                final Uri uri = Provider.READINGS_URI.buildUpon()
                    .appendQueryParameter(Provider.QUERY_PARAMETER_LIMIT, "1")
                    .build();
                return new CursorLoader(getActivity(), uri, null, null, null, DbField.TIME + " DESC");
            case LOADER_ID_STOPS:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //TODO!
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
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
}
