package devindow.GeoMeasure;

import java.io.FileNotFoundException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple Locations database access helper class. 
 * Defines the basic CRUD operations for the GeoDistance application, 
 * and gives the ability to list all Locations as well as
 * retrieve or modify a specific Location.
 */
public class LocationsDbAdapter {

    public static final String KEY_NAME="name";
    public static final String KEY_LATITUDE="latitude";
    public static final String KEY_LONGITUDE="longitude";
    public static final String KEY_ROWID="_id";
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table locations (_id integer primary key autoincrement, name text not null, latitude text not null, longitude text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "locations";
    private static final int DATABASE_VERSION = 3;

    private SQLiteDatabase mDb;
    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be opened/created
     * @param ctx the Context within which to work
     */
    public LocationsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the Locations database. If it cannot be opened, try to create a new instance of
     * the database. If it cannot be created, throw an exception to signal the failure
     * @return this (self reference, allowing this to be chained in an initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public LocationsDbAdapter open() throws SQLException {
        try {
            mDb = mCtx.openDatabase(DATABASE_NAME, null);
        } catch (FileNotFoundException e) {
            try {
                mDb =
                    mCtx.createDatabase(DATABASE_NAME, DATABASE_VERSION, 0,
                        null);
                mDb.execSQL(DATABASE_CREATE);
            } catch (FileNotFoundException e1) {
                throw new SQLException("Could not create database");
            }
        }
        return this;
    }

    public void close() {
        mDb.close();
    }

    /**
     * Create a new Location using the name, latitude, and longitude provided. 
     * If the Location is successfully created
     * 	return the new rowId for that location, 
     * 	otherwise return a -1 to indicate failure.
     * @param name the name of the Location
     * @param latitude the latitude of the Location
     * @param longitude the longitude of the Location
     * @return rowId or -1 if failed
     */
    public long createLocation(String name, double latitude, double longitude) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_LATITUDE, Double.toString(latitude));
        initialValues.put(KEY_LONGITUDE, Double.toString(longitude));
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the Location with the given rowId
     * @param rowId id of Location to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteLocation(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all Locations in the database
     * @return Cursor over all Locations
     */
    public Cursor fetchAllLocations() {
        return mDb.query(DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the Location that matches the given rowId
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching Location, if found
     * @throws SQLException if Location could not be found/retrieved
     */
    public Cursor fetchLocation(long rowId) throws SQLException {
        Cursor result = mDb.query(true, DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE}, KEY_ROWID + "=" + rowId, null, null, null, null);
        if ((result.count() == 0) || !result.first()) {
            throw new SQLException("No Location matching ID: " + rowId);
        }
        return result;
    }

    /**
     * Update the note using the details provided. The note to be updated is specified using
     * the rowId, and it is altered to use the title and body values passed in
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String name, double latitude, double longitude) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_LATITUDE, Double.toString(latitude));
        args.put(KEY_LONGITUDE, Double.toString(longitude));
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
