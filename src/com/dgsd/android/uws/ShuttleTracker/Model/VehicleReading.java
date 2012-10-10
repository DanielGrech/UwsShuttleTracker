package com.dgsd.android.uws.ShuttleTracker.Model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONObject;

public class VehicleReading implements Parcelable {

    public static final String JSON_KEY_NAME = "vehicle";
    public static final String JSON_KEY_LAT = "latitude";
    public static final String JSON_KEY_LON = "longitude";

    public long id = -1;
    public String name;
    public LatLon location;

    public VehicleReading() {

    }

    public VehicleReading(JSONObject json) {
        if(json == null)
            return;

        name = json.optString(JSON_KEY_NAME);

        location = new LatLon();
        location.latitude = json.optDouble(JSON_KEY_LAT, Double.MAX_VALUE);
        location.longitude = json.optDouble(JSON_KEY_LON, Double.MAX_VALUE);
    }

    public VehicleReading(Parcel in) {
        id = in.readLong();
        name = in.readString();
        location = in.readParcelable(LatLon.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeParcelable(location, 0);
    }

    public static final Creator<VehicleReading> CREATOR = new Creator<VehicleReading>() {
        public VehicleReading createFromParcel(Parcel in) {
            return new VehicleReading(in);
        }

        public VehicleReading[] newArray(int size) {
            return new VehicleReading[size];
        }
    };

}
