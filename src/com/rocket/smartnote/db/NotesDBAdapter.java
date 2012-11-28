package com.rocket.smartnote.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class NotesDBAdapter {
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private final Context ctx;
	
	/**
	 * Constructor
	 * @param context - the context within which to work
	 */
	public NotesDBAdapter(Context context) {
		this.ctx = context;
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
    public long createNote(String title, String body) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(NoteTable.COLUMN_TITLE, title);
        initialValues.put(NoteTable.COLUMN_CONTENT, body);
        initialValues.put(NoteTable.COLUMN_TIMESTAMP, 1);
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
    public boolean updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(NoteTable.COLUMN_TITLE, title);
        args.put(NoteTable.COLUMN_CONTENT, body);

        return db.update(NoteTable.TABLE_NAME, args, 
        		NoteTable.COLUMN_ID + "=" + rowId, null) > 0;
    }
}
