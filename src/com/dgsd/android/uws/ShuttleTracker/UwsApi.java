package com.dgsd.android.uws.ShuttleTracker;

import android.text.TextUtils;
import android.util.Log;
import com.dgsd.android.uws.ShuttleTracker.Model.BusStop;
import com.dgsd.android.uws.ShuttleTracker.Model.VehicleReading;
import com.dgsd.android.uws.ShuttleTracker.Util.Http;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class UwsApi {
    public static final String TAG = UwsApi.class.getSimpleName();

    public static final String VEHICLE_READING_URL = "http://gps-i.com//ms.php?user=uwsbusgps&p1=4352";
    public static final String STOPS_URL = "http://eresearch.uws.edu.au/pa1206/Prototype_7_Final/jsonBusStops.php";

    public static List<VehicleReading> getVehicleReading() {
        try {
            String response = Http.get(VEHICLE_READING_URL);

            List<VehicleReading> retval = null;
            if(!TextUtils.isEmpty(response)) {
                JSONArray array = new JSONArray(response);

                retval = new ArrayList<VehicleReading>(array.length());
                for(int i = 0, len = array.length(); i < len; i++)
                    retval.add(new VehicleReading(array.getJSONObject(i)));
            }

            return retval;
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                Log.e(TAG, "Error getting vehicle readings", e);
        }

        return null;
    }

    public static List<BusStop> getStops() {
        try {
            String response = Http.get(STOPS_URL);

            List<BusStop> retval = null;
            if(!TextUtils.isEmpty(response)) {
                JSONArray array = new JSONArray(response);

                retval = new ArrayList<BusStop>(array.length());
                for(int i = 0, len = array.length(); i < len; i++)
                    retval.add(new BusStop(array.getJSONObject(i)));
            }

            return retval;
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                Log.e(TAG, "Error getting stops", e);
        }

        return null;
    }
}
