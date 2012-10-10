package com.dgsd.android.uws.ShuttleTracker.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import com.dgsd.android.uws.ShuttleTracker.BuildConfig;

import java.sql.SQLException;


public class Provider extends ContentProvider {
    private static final String TAG = Provider.class.getSimpleName();

    public static final String AUTHORITY = "com.dgsd.android.CheerMeOn.Data.Provider";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int READINGS = 0x1;
    public static final int STOPS = 0x2;

    public static final Uri READINGS_URI = Uri.withAppendedPath(BASE_URI, "readings");
    public static final Uri STOPS_URI = Uri.withAppendedPath(BASE_URI, "stops");

    private Db mDb;

    static {
        mURIMatcher.addURI(AUTHORITY, "readings", READINGS);
        mURIMatcher.addURI(AUTHORITY, "stops", STOPS);
    }

    @Override
    public boolean onCreate() {
        mDb = Db.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(final Uri uri) {
        if (mURIMatcher.match(uri) != UriMatcher.NO_MATCH) {
            return uri.toString();
        } else {
            return null;
        }
    }

    @Override
    public Cursor query(final Uri uri, String[] proj, final String sel, final String[] selArgs, final String sort) {
        final int type = mURIMatcher.match(uri);
        if(type == UriMatcher.NO_MATCH) {
            if(BuildConfig.DEBUG)
                Log.w(TAG, "No match for URI: " + uri);

            return null;
        }

        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

            switch (type) {
                case READINGS:
                    qb.setTables(DbTable.VEHICLE_READING.name);
                    break;
                case STOPS:
                    qb.setTables(DbTable.STOPS.name);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            Cursor cursor = qb.query(mDb.getReadableDatabase(), proj, sel, selArgs, null, null, sort, null);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

            return cursor;
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "Error querying data", e);

            return null;
        }
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        final int type = mURIMatcher.match(uri);
        try {
            final SQLiteDatabase db = mDb.getWritableDatabase();

            String table = null;
            switch (type) {
                case READINGS:
                    table = DbTable.VEHICLE_READING.name;
                    break;
                case STOPS:
                    table = DbTable.STOPS.name;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            long id = db.replaceOrThrow(table, null, values);
            if (id > 0) {
                Uri newUri = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(uri, null);
                return newUri;
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "Error inserting data", e);

            return null;
        }
    }

    @Override
    public int delete(final Uri uri, final String sel, final String[] selArgs) {
        final int type = mURIMatcher.match(uri);

        try {
            String table = null;
            switch (type) {
                case READINGS:
                    table = DbTable.VEHICLE_READING.name;
                    break;
                case STOPS:
                    table = DbTable.STOPS.name;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            int rowsAffected = mDb.getWritableDatabase().delete(table, sel, selArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return rowsAffected;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error deleting data", e);
            }
            return 0;
        }
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String sel, final String[] selArgs) {
        final int type = mURIMatcher.match(uri);

        try {
            String id = null;
            String table = null;
            switch (type) {
                case READINGS:
                    table = DbTable.VEHICLE_READING.name;
                    break;
                case STOPS:
                    table = DbTable.STOPS.name;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            final SQLiteDatabase db = mDb.getWritableDatabase();

            final int rowsAffected = db.update(table, values, sel, selArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return rowsAffected;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error deleting data", e);
            }
            return 0;
        }
    }
}