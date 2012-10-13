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
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.cyrilmottier.polaris.Annotation;
import com.cyrilmottier.polaris.MapCalloutView;
import com.cyrilmottier.polaris.PolarisMapView;
import com.dgsd.android.uws.ShuttleTracker.BuildConfig;
import com.dgsd.android.uws.ShuttleTracker.Data.DbField;
import com.dgsd.android.uws.ShuttleTracker.Data.Provider;
import com.dgsd.android.uws.ShuttleTracker.Model.LatLon;
import com.dgsd.android.uws.ShuttleTracker.Model.VehicleReading;
import com.dgsd.android.uws.ShuttleTracker.R;
import com.dgsd.android.uws.ShuttleTracker.Service.ApiService;
import com.dgsd.android.uws.ShuttleTracker.Util.Anim;
import com.dgsd.android.uws.ShuttleTracker.Util.OnGetMapViewListener;
import com.google.android.maps.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Grech
 */
public class ShuttleListFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, SimpleCursorAdapter.ViewBinder {
    private static final String TAG = ShuttleListFragment.class.getSimpleName();

    private SimpleCursorAdapter mAdapter;
    private ListView mList;
    private OnReadingClickListener mOnReadingClickListener;


    public static ShuttleListFragment newInstance() {
        return new ShuttleListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shuttle_list, container, false);

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_shuttle, null, new String[]{DbField.ID.name}, new int[]{R.id.container}, 0);
        mAdapter.setViewBinder(this);

        mList = (ListView) v.findViewById(R.id.list);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mList.setLayoutAnimation(Anim.getListViewDealAnimator());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(getActivity(), Provider.LATEST_READINGS_URI, null, null, null, DbField.NAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        if(mOnReadingClickListener != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if(holder != null && holder.reading != null)
                mOnReadingClickListener.onClick(holder.reading);
        }
    }

    public void setOnReadingClickListener(OnReadingClickListener l) {
        mOnReadingClickListener = l;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int col) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if(holder == null)
            holder = new ViewHolder(view);

        if(CursorCols.id < 0)
            CursorCols.init(cursor);

        holder.reading.name = cursor.getString(CursorCols.name);
        holder.reading.location = new LatLon(cursor.getDouble(CursorCols.lat), cursor.getDouble(CursorCols.lon));
        holder.reading.id = cursor.getLong(CursorCols.id);

        holder.name.setText(holder.reading.name);
        holder.location.setText("Currently at " + holder.reading.location);
        holder.time.setText("Last updated at " + DateUtils.formatDateTime(getActivity(), cursor.getLong(CursorCols.time),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_TIME));

        return true;
    }

    private static final class CursorCols {
        static int id = -1;
        static int name = -1;
        static int lat = -1;
        static int lon = -1;
        static int time = -1;

        static void init(Cursor cursor) {
            id = cursor.getColumnIndex(DbField.ID.name);
            name = cursor.getColumnIndex(DbField.NAME.name);
            lat = cursor.getColumnIndex(DbField.LAT.name);
            lon = cursor.getColumnIndex(DbField.LON.name);
            time = cursor.getColumnIndex(DbField.TIME.name);
        }
    }

    private static final class ViewHolder {
        TextView name;
        TextView time;
        TextView location;
        VehicleReading reading;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            time = (TextView) view.findViewById(R.id.time);
            location = (TextView) view.findViewById(R.id.location);
            reading = new VehicleReading();

            view.setTag(this);
        }
    }

    public static interface OnReadingClickListener {
        public void onClick(VehicleReading reading);
    }

}
