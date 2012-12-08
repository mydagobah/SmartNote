package com.rocket.smartnote;

import com.rocket.smartnote.db.NoteTable;
import com.rocket.smartnote.db.NotesDBAdapter;

import android.os.Bundle;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ListNoteActivity extends CustomWindow {
	
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	
	private NotesDBAdapter adapter;
	private LocationHandler locHandler;
	
	/**
	 * Called when the activity is first created
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // setup title bar
        this.navTitle.setText("Note List");
        this.icon.setImageResource(R.drawable.collections_labels);   
        
        this.icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(ListNoteActivity.this, EditNoteActivity.class);
            	startActivityForResult(intent, ACTIVITY_CREATE);
            }
        });       
        
        // db adapter initialize
        adapter = new NotesDBAdapter(this);
        try {
        	adapter.open();
        } catch(SQLException se) {
        	Log.w("Main","Failed to open adapter.");
        }
        
        // check location setting
        locHandler = new LocationHandler(this);
        if (!locHandler.gpsEnabled()) {
        	gpsAlertBox();
        }
        locHandler.captureLocation();
        
        
        // populate data
        fillData();
        registerForContextMenu(getListView());
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (adapter != null) adapter.close();
    }
    
    /**
     * Fetch all notes from DB and list their title in the list view
     */
    private void fillData() {
    	Cursor notesCursor = adapter.fetchAllNotes();
    	startManagingCursor(notesCursor);
    	
    	// Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{ NoteTable.COLUMN_TITLE, 
        							  NoteTable.COLUMN_MONTH, 
        							  NoteTable.COLUMN_DAY,
        							  NoteTable.COLUMN_LOCATION };

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.label, R.id.month, R.id.day, R.id.location };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.note_row, notesCursor, from, to);
        
        setListAdapter(notes); 	
    }
    
    /**
     * Create the menu based on the XML definition
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_new);
        return true;
    }
    
    /**
     * Reaction to the menu selection
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    		case INSERT_ID:
    			createNote();
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                adapter.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    /**
     * Helper createNote()
     */
    private void createNote() {
    	Intent intent = new Intent(this, EditNoteActivity.class);
    	startActivityForResult(intent, ACTIVITY_CREATE);
    }
    
    /**
     * Open the edit note activity if an entry is clicked
     */
    @Override
    protected void onListItemClick(ListView lv, View view, int position, long id) {
    	super.onListItemClick(lv, view, position, id);
    	Intent intent = new Intent(this, EditNoteActivity.class);
    	intent.putExtra(NoteTable.COLUMN_ID, id);
    
    	startActivityForResult(intent, ACTIVITY_EDIT);
    }
    
    /**
     * 
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	fillData();
    }
    
    /**
     * Dialog to prompt users to enable GPS on the device.
     */
    private void gpsAlertBox() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.enable_gps)
            .setMessage(R.string.enable_gps_dialog)
            .setCancelable(false)
            .setPositiveButton(R.string.gps_posBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enableGPSSettings();
                }
             })
             .setNegativeButton(R.string.gps_negBtn, new DialogInterface.OnClickListener() {					
            	@Override
            	public void onClick(DialogInterface dialog, int which) {
            		dialog.cancel();						
            	}
		     }).create().show();
     }
    
 // Method to launch GPS Settings
    private void enableGPSSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }
    
 // Method to launch Network Settings
    private void enableNetworkSettings() {
        Intent settingsIntent = new Intent(Intent.ACTION_MAIN);
        settingsIntent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
        startActivity(settingsIntent);
    }
    /**
     * Dialog to prompt users to enable Network on the device.
     */
    private void networkAlertBox() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.enable_network)
            .setMessage(R.string.enable_network_dialog)
            .setCancelable(false)
            .setPositiveButton(R.string.network_posBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enableNetworkSettings();
                }
             })
             .setNegativeButton(R.string.network_negBtn, new DialogInterface.OnClickListener() {					
            	@Override
            	public void onClick(DialogInterface dialog, int which) {
            		if (!locHandler.gpsEnabled())
            			gpsAlertBox();
            		
            		dialog.cancel();						
            	}
		     }).create().show();
     }
}