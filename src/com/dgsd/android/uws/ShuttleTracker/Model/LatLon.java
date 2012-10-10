/**
 *
 */
package com.dgsd.android.uws.ShuttleTracker.Model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.maps.GeoPoint;

/**
 * Class representing a Buzzhives Location
 *
 * @author Daniel Grech
 */
public class LatLon implements Parcelable {
    private static final int EARTH_RADIUS_IN_METERS = 6371 * 1000;

	public static final String KEY_NAME = "name";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LON = "lng";
    public static final String KEY_EXACT = "exact";

	public double latitude;
	public double longitude;

	public LatLon() {
		this(Double.MAX_VALUE, Double.MAX_VALUE);
	}

    public LatLon(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    private LatLon(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    private LatLon(GeoPoint p) {
        this.latitude = p.getLatitudeE6() / 1E6;
        this.longitude = p.getLongitudeE6() / 1E6;
    }

	@Override
	public String toString() {
        return latitude + ", " + longitude;
	}

	public GeoPoint toGeoPoint() {
		return new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
	}

	public boolean isValid() {
		return latitude != Double.MAX_VALUE && longitude != Double.MAX_VALUE;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;

		if(obj == null || !(obj instanceof LatLon))
			return false;

		LatLon loc = (LatLon) obj;
		return this.latitude == loc.latitude && this.longitude == loc.longitude;

	}

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
	}

	public static final Creator<LatLon> CREATOR = new Creator<LatLon>() {
		public LatLon createFromParcel(Parcel in) {
			return new LatLon(in);
		}

		public LatLon[] newArray(int size) {
			return new LatLon[size];
		}
	};

    /**
     * Get the distance between this and another point
     *
     * This implementation was pulled from:
     *  <a href="http://www.codecodex.com/wiki/Calculate_Distance_Between_Two_Points_on_a_Globe#Java">CodeCodex</a>
     *
     * @param that The location to compare to
     * @return The distance in meters between <code>this</code> and <code>that</code>
     *
     * @see <a href="http://en.wikipedia.org/wiki/Haversine_formula">Haversine Formula</a>
     *
     */
    public int distanceTo(LatLon that) {
        if(this == that || that == null || !that.isValid())
            return 0;

        double lat1 = this.latitude;
        double lon1 = this.longitude;
        double lat2 = that.latitude;
        double lon2 = that.longitude;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.asin(Math.sqrt(a));
        return (int) (EARTH_RADIUS_IN_METERS * c);
    }
}
