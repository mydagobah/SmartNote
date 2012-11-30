package com.rocket.smartnote;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomWindow extends ListActivity {

	protected TextView navTitle;
	protected TextView iconTitle;
	protected ImageView icon;
	
	/**
	 * Called when the activity is first created
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // apply custom theme
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_list_note);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        
        navTitle = (TextView) findViewById(R.id.nav_title);
        iconTitle = (TextView) findViewById(R.id.icon_title);
        icon = (ImageView) findViewById(R.id.icon);
    }
}
