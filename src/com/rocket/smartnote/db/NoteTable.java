package com.rocket.smartnote.db;

import android.database.sqlite.SQLiteDatabase;

public class NoteTable {
	
	// Database table
	public static final String TABLE_NAME       = "notes";
	public static final String COLUMN_ID        = "_id";
	public static final String COLUMN_TITLE     = "title";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_NOTE      = "note";	
	
	// Database creation SQL statement
	private static final String TABLE_CREATE = "create table " + TABLE_NAME + "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_TITLE + " text not null, "
			+ COLUMN_TIMESTAMP + " integer not null, "
			+ COLUMN_NOTE + " text not null);";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
