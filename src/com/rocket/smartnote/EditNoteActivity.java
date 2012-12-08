package com.rocket.smartnote;

import java.io.File;

import com.rocket.smartnote.db.NoteTable;
import com.rocket.smartnote.db.NotesDBAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class EditNoteActivity extends Activity {

	private EditText titleText;
	private EditText contentText;
	private Long rowId;
	private NotesDBAdapter adapter;
	private LocationHandler locHandler;
	protected TextView navTitle;
	protected ImageView icon;
	protected ImageView photoIcon;
	protected ImageView recordIcon;
	protected ImageView playIcon;
		
	// set up media player
	private MediaPlayer  mediaPlayer;
	private MediaRecorder recorder;
	
	//private String OUTPUT_FILE;
	private static final String OUTPUT_FILE= "/sdcard/recordoutput.3gpp";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        // apply custom theme
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_list_note);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        
        // setup OUTPUT_FILE
        //OUTPUT_FILE = Environment.getExternalStorageDirectory()+"/audiorecorder.3gpp";
        
        // setup title bar
        navTitle = (TextView) findViewById(R.id.nav_title);
        icon = (ImageView) findViewById(R.id.icon);
        
        navTitle.setText("Edit");
        icon.setImageResource(R.drawable.navigation_back); 
        
        this.icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        // setup toolbar
        
        
        // initialize db adapter
        adapter = new NotesDBAdapter(this);
        adapter.open();

        setContentView(R.layout.activity_edit_note);
        
        titleText = (EditText) findViewById(R.id.title);
        contentText = (EditText) findViewById(R.id.content);
        
        ImageButton saveButton = (ImageButton) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	setResult(RESULT_OK);          
                finish();
            }
        });
        
        rowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NoteTable.COLUMN_ID);
		
        if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null ? extras.getLong(NoteTable.COLUMN_ID) : null;
		}

        // check location setting
        locHandler = new LocationHandler(this);
        if (!locHandler.gpsEnabled()) {
        	gpsAlertBox();
        }
        locHandler.captureLocation();
               
		populateFields();
    }
	
	/** audio part */
	/** buttonTapped method for recording buttons */
    public void buttonTapped(View view) {
    	switch(view.getId()) {
    	    case R.id.record:
    	    	try {
    	    		beginRecoding();
    	    	} catch(Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	break;
    	    case R.id.record_stop:
    	    	try {
    	    		stopRecoding();
    	    	} catch(Exception e) {
    	    		e.printStackTrace();
    	    	} 
    	    	break;
    	    case R.id.play:
    	    	try {
    	    		playRecoding();
    	    	} catch(Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	break;
    	    case R.id.stop:
    	    	try {
    	    		stopPlayback();
    	    	} catch(Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	break;
    	}
    }

    /** End of buttonTapped method for recording buttons */

	private void stopPlayback() {
		if (mediaPlayer != null) 
		    mediaPlayer.stop();
	}

	private void playRecoding() throws Exception {
		ditchMediaPlayer();
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(OUTPUT_FILE);
		mediaPlayer.prepare();
		mediaPlayer.start();
	}

	private void ditchMediaPlayer() {
		if (mediaPlayer != null) {
			try {
				mediaPlayer.release();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void stopRecoding() {
		if (recorder != null) {
			recorder.stop();
		}
	}

	private void beginRecoding() throws Exception {
		ditchMediaRecorder();
		File outFile = new File(OUTPUT_FILE);
		
		if (outFile.exists()) 
			outFile.delete();
		
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(OUTPUT_FILE);
		recorder.prepare();
		recorder.start();
		
	}

	private void ditchMediaRecorder() {
		if(recorder != null)
            recorder.release();
	}
	/** End of audio part */
	
	/**
	 * fill data in the fields if it is to edit an existing note
	 */
	private void populateFields() {
        if (rowId != null) {
            Cursor note = adapter.fetchNote(rowId);
            startManagingCursor(note);
            
            titleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NoteTable.COLUMN_TITLE)));
            contentText.setText(note.getString(
                    note.getColumnIndexOrThrow(NoteTable.COLUMN_CONTENT)));
        }
    }
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NoteTable.COLUMN_ID,rowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    private void saveState() {
        String title = titleText.getText().toString();
        String content = contentText.getText().toString();

        if (rowId == null) {
        	// if title is empty, do not save
        	if (title.isEmpty()) return;
        	
            long id = adapter.createNote(title, content);
            if (id > 0) {
                rowId = id;
            }
        } else {
            adapter.updateNote(rowId, title, content);
        }
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
     * Dialog to prompt users to enable GPS on the device.
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
}
