package com.rocket.smartnote.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NoteTable {
	
	private static final String TAG = "NoteTable";
	
	// Database table
	public static final String TABLE_NAME       = "notes";
	
	public static final String COLUMN_ID        = "_id";
	public static final String COLUMN_TITLE     = "title";	
	public static final String COLUMN_CONTENT   = "content";
	public static final String COLUMN_MONTH     = "month";
	public static final String COLUMN_DAY       = "day";
	public static final String COLUMN_YEAR      = "year";
	public static final String COLUMN_LOCATION  = "location";
	public static final String COLUMN_RECORD_PH = "record";
	
	// an array of all columns
	public static final String[] COLUMNS = new String[] {
		COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_MONTH, COLUMN_DAY, 
		COLUMN_YEAR, COLUMN_LOCATION, COLUMN_RECORD_PH };
	
	// Database creation SQL statement
	private static final String TABLE_CREATE = "create table " + TABLE_NAME + "("
			+ COLUMN_ID        + " integer primary key autoincrement, "
			+ COLUMN_TITLE     + " text not null, "
			+ COLUMN_CONTENT   + " text not null, "
			+ COLUMN_MONTH     + " integer not null, "
			+ COLUMN_DAY       + " integer not null, "
            + COLUMN_YEAR      + " integer, "
            + COLUMN_LOCATION  + " text, "
            + COLUMN_RECORD_PH + " text);";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version" + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
