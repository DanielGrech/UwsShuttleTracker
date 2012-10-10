package com.dgsd.android.uws.ShuttleTracker.Model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONObject;

public class BusStop implements Parcelable {

    public static final String JSON_KEY_STOP_ID = "StopId";
    public static final String JSON_KEY_CAMPUS = "Campus";
    public static final String JSON_KEY_NAME = "StopName";
    public static final String JSON_KEY_LAT = "Stoplat";
    public static final String JSON_KEY_LON = "Stoplng";

    public long id = -1;
    public int stopId;
    public String name;
    public String campusName;
    public LatLon location;

    public BusStop() {

    }

    public BusStop(JSONObject json) {
        if(json == null)
            return;

        stopId = json.optInt(JSON_KEY_STOP_ID);
        name = json.optString(JSON_KEY_NAME);
        campusName = json.optString(JSON_KEY_CAMPUS);

        location = new LatLon();
        location.latitude = json.optDouble(JSON_KEY_LAT, Double.MAX_VALUE);
        location.longitude = json.optDouble(JSON_KEY_LON, Double.MAX_VALUE);
    }

    public BusStop(Parcel in) {
        id = in.readLong();
        stopId = in.readInt();
        name = in.readString();
        campusName = in.readString();
        location = in.readParcelable(LatLon.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(stopId);
        dest.writeString(name);
        dest.writeString(campusName);
        dest.writeParcelable(location, 0);
    }

    public static final Creator<BusStop> CREATOR = new Creator<BusStop>() {
        public BusStop createFromParcel(Parcel in) {
            return new BusStop(in);
        }

        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };

}
