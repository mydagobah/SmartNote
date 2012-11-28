package com.rocket.smartnote;

import com.rocket.smartnote.db.NoteTable;
import com.rocket.smartnote.db.NotesDBAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditNoteActivity extends Activity {

	private EditText titleText;
	private EditText contentText;
	private Long rowId;
	private NotesDBAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new NotesDBAdapter(this);
        adapter.open();

        setContentView(R.layout.activity_edit_note);
        setTitle(R.string.title_edit);

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
            long id = adapter.createNote(title, content);
            if (id > 0) {
                rowId = id;
            }
        } else {
            adapter.updateNote(rowId, title, content);
        }
    }
}
