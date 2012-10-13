package com.dgsd.android.uws.ShuttleTracker.Fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.cyrilmottier.polaris.Annotation;
import com.cyrilmottier.polaris.MapCalloutView;
import com.cyrilmottier.polaris.PolarisMapView;
import com.dgsd.android.uws.ShuttleTracker.BuildConfig;
import com.dgsd.android.uws.ShuttleTracker.Data.DbField;
import com.dgsd.android.uws.ShuttleTracker.Data.Provider;
import com.dgsd.android.uws.ShuttleTracker.R;
import com.dgsd.android.uws.ShuttleTracker.Service.ApiService;
import com.dgsd.android.uws.ShuttleTracker.Util.OnGetMapViewListener;
import com.google.android.maps.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Grech
 */
public class MapFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>,PolarisMapView.OnAnnotationSelectionChangedListener {
    private static final String TAG = MapFragment.class.getSimpleName();

    public static final int LOADER_ID_STOPS = 0x01;
    public static final int LOADER_ID_READINGS = 0x02;

    private static final int HANDLER_DELAY = 1000 * 60; // Every minute!

    private OnGetMapViewListener mOnGetMapViewListener;
    private Handler mHandler;
    private List<Annotation> mAnnotations = new ArrayList<Annotation>();

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
        PolarisMapView mv = getMapView();
        if (mv != null && mv.getParent() != null && mv.getParent() instanceof ViewGroup) {
            try {
                //View already has a parent, better remove it!
                ((ViewGroup) mv.getParent()).removeView(mv);
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "Error removing MapView from parent", e);
            }
        }

        mv.setOnAnnotationSelectionChangedListener(this);

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
                return new CursorLoader(getActivity(), Provider.LATEST_READINGS_URI, null, null, null, DbField.TIME + " DESC");
            case LOADER_ID_STOPS:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAnnotations.clear();
        if(cursor != null && cursor.moveToFirst()) {
            final int nameCol = cursor.getColumnIndex(DbField.NAME.name);
            final int latCol = cursor.getColumnIndex(DbField.LAT.name);
            final int lonCol = cursor.getColumnIndex(DbField.LON.name);
            final int timeCol = cursor.getColumnIndex(DbField.TIME.name);
            do {
                final String name = cursor.getString(nameCol);
                final double lat = cursor.getDouble(latCol);
                final double lon = cursor.getDouble(lonCol);
                final long time = cursor.getLong(timeCol);

                final String subtitle = "Last updated at " + DateUtils.formatDateTime(getActivity(), time,
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_TIME);
                final GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));

                mAnnotations.add(new Annotation(p, name, subtitle));
            } while(cursor.moveToNext());

            final PolarisMapView mv = getMapView();
            mv.setAnnotations(null, null);

            //Hacky way to make sure we remove any callouts..
            if(mv.getChildCount() > 1)
                mv.removeViewAt(0);

            mv.setAnnotations(mAnnotations, R.drawable.map_marker_bus);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    public void setOnGetMapViewListener(OnGetMapViewListener onGetMapViewListener) {
        this.mOnGetMapViewListener = onGetMapViewListener;
    }

    private PolarisMapView getMapView() {
        if (mOnGetMapViewListener != null)
            return mOnGetMapViewListener.onGetMapView();
        else
            return ((OnGetMapViewListener) getActivity()).onGetMapView();
    }

    @Override
    public void onAnnotationSelected(PolarisMapView mapView, MapCalloutView calloutView, int position, Annotation annotation) {
        calloutView.setDisclosureEnabled(true);
        calloutView.setClickable(true);
    }

    @Override
    public void onAnnotationDeselected(PolarisMapView mapView, MapCalloutView calloutView, int position, Annotation annotation) {

    }

    @Override
    public void onAnnotationClicked(PolarisMapView mapView, MapCalloutView calloutView, int position, Annotation annotation) {
        final GeoPoint p = annotation.getPoint();
        startStreetView(p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6);
    }

    public void startStreetView(double lat, double lon) {
        StringBuilder builder = new StringBuilder();
        builder.append("google.streetview:cbll=");
        builder.append(lat);
        builder.append(",");
        builder.append(lon);

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(builder.toString()));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        try {
            startActivity(intent);
        } catch(ActivityNotFoundException e) {
            if(BuildConfig.DEBUG)
                Log.w(TAG, "Couldnt find street view activity", e);
        }
    }

    public void selectAnnotationAt(final GeoPoint p) {
        final PolarisMapView mv = getMapView();
        mv.getController().setZoom(18);
        mv.getController().animateTo(p, new Runnable() {
            @Override
            public void run() {
                if(mAnnotations == null || mAnnotations.isEmpty())
                    return;

                for(int i = 0, size = mAnnotations.size(); i < size; i++) {
                    if(mAnnotations.get(i).getPoint().equals(p)) {
                        mv.setSelectedAnnotation(i);
                        return;
                    }
                }
            }
        });
    }
}
