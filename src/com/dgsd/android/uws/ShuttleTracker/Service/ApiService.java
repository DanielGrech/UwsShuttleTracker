/**
 *
 */
package com.dgsd.android.uws.ShuttleTracker.Service;

import android.app.IntentService;
import android.content.*;
import android.util.Log;
import com.dgsd.android.uws.ShuttleTracker.BuildConfig;
import com.dgsd.android.uws.ShuttleTracker.Data.DbField;
import com.dgsd.android.uws.ShuttleTracker.Data.Provider;
import com.dgsd.android.uws.ShuttleTracker.Model.BusStop;
import com.dgsd.android.uws.ShuttleTracker.Model.VehicleReading;
import com.dgsd.android.uws.ShuttleTracker.UwsApi;

import java.util.ArrayList;
import java.util.List;

public class ApiService extends IntentService {
    public static final String TAG = ApiService.class.getSimpleName();

    public static final String EXTRA_REQUEST_TYPE = "com.dgsd.android.uws.ShuttleTracker.Service.ApiService._request_type";
    public static final String EXTRA_RESULT_TYPE = "com.dgsd.android.uws.ShuttleTracker.Service.ApiService._result_type";


    public ApiService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent inIntent) {
        try {
            final int requestType = inIntent.getIntExtra(EXTRA_REQUEST_TYPE, -1);
            switch(requestType) {
                case RequestType.BUS_STOPS:
                    List<BusStop> busStops = UwsApi.getStops();
                    if(busStops == null || busStops.isEmpty()) {
                        broadcast(requestType, ResultType.ERROR);
                    } else {
                        final ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(busStops.size());
                        for(BusStop stop : busStops) {
                            if(stop.location == null || !stop.location.isValid())
                                continue;

                            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(Provider.STOPS_URI);
                            b.withYieldAllowed(true);

                            b.withValue(DbField.STOP_ID.name, stop.stopId);
                            b.withValue(DbField.NAME.name, stop.name);
                            b.withValue(DbField.CAMPUS.name, stop.campusName);
                            b.withValue(DbField.LAT.name, stop.location.latitude);
                            b.withValue(DbField.LON.name, stop.location.longitude);

                            ops.add(b.build());
                        }

                        getContentResolver().applyBatch(Provider.AUTHORITY, ops);

                        broadcast(requestType, ResultType.SUCCESS);
                    }
                    break;
                case RequestType.VEHICLE_READING:
                    List<VehicleReading> readings = UwsApi.getVehicleReading();
                    if(readings == null || readings.isEmpty()) {
                        broadcast(requestType, ResultType.ERROR);
                    } else {
                        final long currentMillis = System.currentTimeMillis();
                        final ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(readings.size());
                        for(VehicleReading reading : readings) {
                            if(reading.location == null || !reading.location.isValid())
                                continue;

                            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(Provider.READINGS_URI);
                            b.withYieldAllowed(true);

                            b.withValue(DbField.NAME.name, reading.name);
                            b.withValue(DbField.LAT.name, reading.location.latitude);
                            b.withValue(DbField.LON.name, reading.location.longitude);
                            b.withValue(DbField.TIME.name, currentMillis);

                            ops.add(b.build());
                        }

                        getContentResolver().applyBatch(Provider.AUTHORITY, ops);

                        broadcast(requestType, ResultType.SUCCESS);
                    }
                    break;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Unexpected error when retrieving data", e);
            }
        }
    }

    private void broadcast(int requestType, int resultType) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REQUEST_TYPE, requestType);
        intent.putExtra(EXTRA_RESULT_TYPE, resultType);
        sendBroadcast(intent);
    }

    public static void requestVehicleReading(Context context) {
        final Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(EXTRA_REQUEST_TYPE, RequestType.VEHICLE_READING);
        context.startService(intent);
    }

    public static void requestBusStops(Context context) {
        final Intent intent = new Intent(context, ApiService.class);
        intent.putExtra(EXTRA_REQUEST_TYPE, RequestType.BUS_STOPS);
        context.startService(intent);
    }

    public static final class ResultType {
        public static final int SUCCESS = 0x1;
        public static final int ERROR = 0x2;
    }

    public static class RequestType {
        public static final int BUS_STOPS = 0x8008;
        public static final int VEHICLE_READING = 0x1337;
    }
}
