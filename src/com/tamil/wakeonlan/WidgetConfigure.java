package com.tamil.wakeonlan;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import com.tamil.android.wakeonlan.R;

/**
 * This class is used to configure the home screen widget
 */

public class WidgetConfigure extends Activity {

	public static final String TAG = "WidgetConfigure";

	private HistoryListHandler historyListHandler;
	private int widget_id;
	private SharedPreferences settings;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.widget_configure);
		ListView lv = (ListView)findViewById(R.id.history);
		historyListHandler = new HistoryListHandler(this, lv);

		settings = getSharedPreferences(WakeOnLanActivity.TAG, 0);
		int sort_mode = settings.getInt("sort_mode", WakeOnLanActivity.CREATED);
		historyListHandler.bind(sort_mode);

		// add on click listener
		historyListHandler.addHistoryListClickListener(new HistoryListClickListener () {
			public void onClick(HistoryItem item) {
				selected(item);
			}
		});

		// get the widget id
		Intent intent = getIntent();
		widget_id = WidgetProvider.getWidgetId(intent);

		if(widget_id == AppWidgetManager.INVALID_APPWIDGET_ID) {
			// no valid widget id; bailing
			finish();
		}
	}

	private void selected(HistoryItem item) {
		// save selected item id to the settings.
		WidgetProvider.saveItemPref(settings, item, widget_id);	

		// configure the widget
		WidgetProvider.configureWidget(widget_id, item, this);

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
		setResult(RESULT_OK, resultValue);
		finish();
	}

}
