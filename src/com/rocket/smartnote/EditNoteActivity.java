package com.rocket.smartnote;

import com.rocket.smartnote.db.NoteTable;
import com.rocket.smartnote.db.NotesDBAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EditNoteActivity extends Activity {

	private EditText titleText;
	private EditText contentText;
	private Long rowId;
	private NotesDBAdapter adapter;
	protected TextView navTitle;
	protected ImageView icon;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        // apply custom theme
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_list_note);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        
        // setup title bar
        navTitle = (TextView) findViewById(R.id.nav_title);
        icon = (ImageView) findViewById(R.id.icon);
        
        navTitle.setText("Edit");
        icon.setImageResource(R.drawable.navigation_back); 
        
        this.icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//Intent intent = new Intent(EditNoteActivity.this, ListNoteActivity.class);
            	//startActivityForResult(intent, ACTIVITY_CREATE);
            	finish();
            }
        });
        
        // initialize db adapter
        adapter = new NotesDBAdapter(this);
        adapter.open();

        setContentView(R.layout.activity_edit_note);

        titleText = (EditText) findViewById(R.id.title);
        contentText = (EditText) findViewById(R.id.content);
        Button saveButton = (Button) findViewById(R.id.save);

        rowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NoteTable.COLUMN_ID);
		
        if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null ? extras.getLong(NoteTable.COLUMN_ID) : null;
		}

		populateFields();

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	setResult(RESULT_OK);          
                finish();
            }
        });
    }
	
	
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
}
