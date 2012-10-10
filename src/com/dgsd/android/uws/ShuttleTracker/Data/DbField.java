package com.dgsd.android.uws.ShuttleTracker.Data;

/**
 * @author Daniel Grech
 */
public class DbField {
    public static final DbField ID = new DbField("_id", "integer", "primary key");
    public static final DbField STOP_ID = new DbField("_stop_id", "text");
    public static final DbField NAME = new DbField("_name", "text");
    public static final DbField CAMPUS = new DbField("_campus", "text");
    public static final DbField LAT = new DbField("_lat", "real");
    public static final DbField LON = new DbField("_lon", "real");


    public String name;
	public String type;
	public String constraint;

	public DbField(String n, String t) {
		this(n, t, null);
	}

	public DbField(String n, String t, String c) {
		name = n;
		type = t;
		constraint = c;
	}

	@Override
	public String toString() {
		return name;
	}
}
