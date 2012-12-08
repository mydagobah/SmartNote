package com.rocket.smartnote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	/**
	 * Migration history
	 * 1. initial create
	 * 2. add TIMESTAMP
	 * 3. remove TIMESTAMP
	 * 4. add MONTH, DAY, YEAR
	 * 5. add LOCAION
	 * 6. add RECORD_PH
	 * 7. add PHOTO_PH
	 */
	private static final int DATABASE_VERSION = 7;
	private static final String DATABASE_NAME = "notes.db";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		NoteTable.onCreate(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		NoteTable.onUpgrade(db, oldVersion, newVersion);
	}
}
