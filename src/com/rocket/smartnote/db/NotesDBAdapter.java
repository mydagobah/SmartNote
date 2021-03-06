package com.rocket.smartnote.db;

import java.util.Calendar;

import com.rocket.smartnote.LocationHandler;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class NotesDBAdapter {
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private final Context ctx;
	private LocationHandler location;
	
	/**
	 * Constructor
	 * @param context - the context within which to work
	 */
	public NotesDBAdapter(Context context) {
		this.ctx = context;
		location = new LocationHandler(ctx);
		location.captureLocation();
	}
	
	/**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
	public NotesDBAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(ctx);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		if (dbHelper != null)
			dbHelper.close();
	}
	
	/**
     * Create a new note using the title and note provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title - the title of the note
     * @param body - the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body, String audio_path, String photo_path, String loc) {
    	   	
        ContentValues initialValues = new ContentValues();
        Calendar cal = Calendar.getInstance();
        
        initialValues.put(NoteTable.COLUMN_TITLE, title);
        initialValues.put(NoteTable.COLUMN_CONTENT, body);   
        initialValues.put(NoteTable.COLUMN_MONTH, cal.get(Calendar.MONTH) + 1); 
        initialValues.put(NoteTable.COLUMN_DAY, cal.get(Calendar.DAY_OF_MONTH));
        initialValues.put(NoteTable.COLUMN_YEAR, cal.get(Calendar.YEAR));
        String autoLoc = location.getLocation();
        if ((autoLoc.equals("Unknow") || autoLoc.equals("Location not available")) && loc != null) {
        	initialValues.put(NoteTable.COLUMN_LOCATION, loc);
        }
        else {
        	initialValues.put(NoteTable.COLUMN_LOCATION, location.getLocation());
        }
        
        initialValues.put(NoteTable.COLUMN_RECORD_PH, audio_path);
        initialValues.put(NoteTable.COLUMN_PHOTO_PH, photo_path);
        
        return db.insert(NoteTable.TABLE_NAME, null, initialValues);
    }
    
    /**
     * Delete the note with the given rowId
     * 
     * @param rowId - id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return db.delete(NoteTable.TABLE_NAME, NoteTable.COLUMN_ID + "=" + rowId, null) > 0;
    }
    
    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {
        return db.query(NoteTable.TABLE_NAME, NoteTable.COLUMNS, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId - id of note to retrieve
     * @return Cursor - positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor cursor = db.query(true, NoteTable.TABLE_NAME, NoteTable.COLUMNS, 
        		NoteTable.COLUMN_ID + "=" + rowId, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    
    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId - id of note to update
     * @param title - value to set note title to
     * @param body - value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body, String loc) {
    	if ((title == null) || (title.equals(""))) return false;
    	
    	Calendar cal = Calendar.getInstance();
    	
        ContentValues args = new ContentValues();
        args.put(NoteTable.COLUMN_TITLE, title);
        args.put(NoteTable.COLUMN_CONTENT, body);
        args.put(NoteTable.COLUMN_MONTH, cal.get(Calendar.MONTH) + 1); 
        args.put(NoteTable.COLUMN_DAY, cal.get(Calendar.DAY_OF_MONTH));
        args.put(NoteTable.COLUMN_YEAR, cal.get(Calendar.YEAR));
        if (loc != null)
        	args.put(NoteTable.COLUMN_LOCATION, loc);

        return db.update(NoteTable.TABLE_NAME, args, 
        		NoteTable.COLUMN_ID + "=" + rowId, null) > 0;
    }
    
    
}
