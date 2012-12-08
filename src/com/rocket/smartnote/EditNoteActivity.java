package com.rocket.smartnote;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.rocket.smartnote.db.NoteTable;
import com.rocket.smartnote.db.NotesDBAdapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
	protected TextView navTitle;
	protected ImageView navIcon;
	protected ImageView photoIcon;
	protected ImageView recordIcon;
	protected ImageView stopRecordIcon;
	protected ImageView playIcon;
	protected ImageView stopIcon;
	// display the phote taken by the camera
	protected ImageView iv;
	
	// set up media player
	private MediaPlayer  mediaPlayer;
	private MediaRecorder recorder;
	
	// base path for audio file (file with .3gpp format)
	private static final String AUDIO_PATH = "/sdcard/audio";
	private static final String PHOTO_PATH = "/sdcard/photo";
	private String audio_file, photo_file;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // initialize db adapter
        adapter = new NotesDBAdapter(this);
        adapter.open();
        
        // apply custom theme
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_list_note);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        
        setContentView(R.layout.activity_edit_note);
        
        // setup title bar
        navTitle = (TextView) findViewById(R.id.nav_title);
        navIcon = (ImageView) findViewById(R.id.icon);
        
        navTitle.setText("Edit");
        navIcon.setImageResource(R.drawable.navigation_back); 
        
        navIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        
        // setup toolbar
        iv = (ImageView) findViewById(R.id.imageView);
        photoIcon = (ImageView) findViewById(R.id.photo);
        photoIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// initialize photo file
				/*
				File _photoFile = new File(photo_file);
				try {
					if (!_photoFile.exists()) {
						_photoFile.createNewFile();
					}
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), "Create photo fails. Please try again.", 
							Toast.LENGTH_SHORT).show();
				}
				
				Uri _photoUri = Uri.fromFile(_photoFile);
				
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, _photoUri);
				*/
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, 0);
			}
        	
        });
                
        playIcon = (ImageView) findViewById(R.id.play);

        titleText = (EditText) findViewById(R.id.title);
        contentText = (EditText) findViewById(R.id.content);
        ImageButton saveButton = (ImageButton) findViewById(R.id.save);
                                             
        rowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NoteTable.COLUMN_ID);		
        if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null ? extras.getLong(NoteTable.COLUMN_ID) : null;
		}
        
        populateFields();
              
        // initailize audio path
        if (rowId == null) {
        	Long ts = System.currentTimeMillis();
        	audio_file = AUDIO_PATH + ts.toString() +".3gpp";
        	photo_file = PHOTO_PATH + ts.toString() + ".jpeg";
        }
                
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	setResult(RESULT_OK);          
                finish();
            }
        });
    }

	/** photo part
	 *  Callback from photo taking action
	 */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 0) {
    		Bitmap p = (Bitmap) data.getExtras().get("data");
    		iv.setImageBitmap(p);
    		savePhoto(scalePhoto(p), photo_file);    		   	
    	}
    }
	
    private Bitmap scalePhoto(Bitmap bm) {
    	int oldWidth = bm.getWidth();
    	int oldHeight = bm.getHeight();
    	int newWidth = 200;
    	int newHeight = 150;
    	float scaleWidth = ((float) newWidth) / oldWidth;
    	float scaleHeight = ((float) newHeight) / oldHeight;
    	Matrix matrix = new Matrix();
    	matrix.postScale(scaleWidth, scaleHeight);
    	Bitmap small = Bitmap.createBitmap(bm, 0, 0, oldWidth, oldHeight, matrix, true);
    	
    	return small;
    }
    
    private boolean savePhoto(Bitmap bm, String path) {
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	bm.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
    	File file = new File(path);
    	FileOutputStream fos = null;
    	try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			return false;
		}
    	try {
			fos.write(bytes.toByteArray());
			fos.close();
		} catch (IOException e) {
			return false;
		}
    	  	
    	return true;
    }
    
    private Bitmap readPhotoFromPath(String path) {
    	FileInputStream in;
    	BufferedInputStream buf;
    	Bitmap ret = null;
    	
    	try {
			in = new FileInputStream(path);
			buf = new BufferedInputStream(in);
			ret = BitmapFactory.decodeStream(buf);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return ret;
    }
    
	/** End of photo part */
		
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
		mediaPlayer.setDataSource(audio_file);
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
		File outFile = new File(audio_file);
		
		if (outFile.exists()) 
			outFile.delete();
		
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(audio_file);
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
            audio_file = note.getString(
            		note.getColumnIndexOrThrow(NoteTable.COLUMN_RECORD_PH));
            photo_file = note.getString(
            		note.getColumnIndexOrThrow(NoteTable.COLUMN_PHOTO_PH));
 
            Bitmap p = readPhotoFromPath(photo_file);
            if (p != null) {
            	iv.setImageBitmap(p);
            }
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
        	
            long id = adapter.createNote(title, content, audio_file, photo_file);
            if (id > 0) {
                rowId = id;
            }
        } else {
            adapter.updateNote(rowId, title, content);
        }
    }
       
}
