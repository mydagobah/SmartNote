package com.rocket.smartnote;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class ListNoteActivity extends ListActivity {

	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private SimpleCursorAdapter adapter;
	/**
	 * Called when the activity is first created
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_note);
    }

    /**
     * Create the menu based on the XML definiton
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_note, menu);
        return true;
    }
    
    /**
     * Reaction to the menu selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_newnote:
    		createNote();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    /**
     * Helper newNote()
     */
    private void createNote() {
    	Intent it = new Intent(this, CreateNoteActivity.class);
    	startActivityForResult(it, ACTIVITY_CREATE);
    }
    
    
    public void createNote(View view) {
    	Intent intent = new Intent(this, CreateNoteActivity.class);
    	startActivity(intent);
    }
    
    
    
}
