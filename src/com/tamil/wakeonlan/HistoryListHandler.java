package com.tamil.wakeonlan;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 *	Class handles all functions of the history ListView
 */
public class HistoryListHandler implements OnItemClickListener {

	public static final String TAG = "HistoryListHandler";

	private Activity parent;
	private Cursor cursor;
	private HistoryAdapter adapter;
	private List<HistoryListClickListener> listeners;


	public static final String[] PROJECTION = new String[] {
			History.Items._ID,
			History.Items.TITLE,
			History.Items.MAC,
			History.Items.IP,
			History.Items.PORT,
			History.Items.LAST_USED_DATE,
			History.Items.USED_COUNT,
			History.Items.IS_STARRED
	};

	private ListView view = null;

	public HistoryListHandler(Activity parent, ListView view) {
		this.parent = parent;
		this.view = view;
		this.listeners = new ArrayList<>();
	}

	public void bind(int sort_mode) {
		String orderBy = null;
		switch (sort_mode) {
			case WakeOnLanActivity.CREATED:
				orderBy = History.Items.IS_STARRED+" DESC, "+History.Items.CREATED_DATE+" DESC";
				break;
			case WakeOnLanActivity.LAST_USED:
				orderBy = History.Items.IS_STARRED+" DESC, "+History.Items.LAST_USED_DATE+" DESC";
				break;
			case WakeOnLanActivity.USED_COUNT:
				orderBy = History.Items.IS_STARRED+" DESC, "+History.Items.USED_COUNT+" DESC";
				break;
		}

		// determine if we render the favourite star buttons
		boolean showStars = false;
		if(parent instanceof WakeOnLanActivity) {
			showStars = true;
		}

		// load History cursor via custom ResourceAdapter
		cursor = parent.getContentResolver().query(History.Items.CONTENT_URI, PROJECTION, null, null, orderBy);
		adapter = new HistoryAdapter(parent, cursor, showStars);

		// register self as listener for item clicks
		view.setOnItemClickListener(this);

		// bind to the supplied view
		view.setAdapter(adapter);
	}


	public void onItemClick(AdapterView av, View v, int position, long id) {
		if (position >= 0) {
			// extract item at position of click
			HistoryItem item = getItem(position);

			// fire onClick event to HistoryListListeners
			for(HistoryListClickListener l : listeners) {
				l.onClick(item);
			}
		}
	}

	public HistoryItem getItem(int position) {
		this.cursor.moveToPosition(position);
		return getItem(this.cursor);
	}

	public static HistoryItem getItem(Cursor cursor) {
		int idColumn = cursor.getColumnIndex(History.Items._ID);
		int titleColumn = cursor.getColumnIndex(History.Items.TITLE);
		int macColumn = cursor.getColumnIndex(History.Items.MAC);
		int ipColumn = cursor.getColumnIndex(History.Items.IP);
		int portColumn = cursor.getColumnIndex(History.Items.PORT);

		return new HistoryItem(cursor.getInt(idColumn), cursor.getString(titleColumn), cursor.getString(macColumn), cursor.getString(ipColumn), cursor.getInt(portColumn));
	}

	public void addToHistory(String title, String mac, String ip, int port) {
		boolean exists = false;

		// don't allow duplicates in history list
		if (cursor.moveToFirst()) {
			int macColumn = cursor.getColumnIndex(History.Items.MAC);
			int ipColumn = cursor.getColumnIndex(History.Items.IP);
			int portColumn = cursor.getColumnIndex(History.Items.PORT);

			do {
				if(mac.equals(cursor.getString(macColumn)) && ip.equals(cursor.getString(ipColumn)) && (port == cursor.getInt(portColumn))) {
					exists = true;
					break;
				}
			} while (cursor.moveToNext());
		}

		// create only if the item doesn't exist
		if (!exists) {
			ContentValues values = new ContentValues(4);
			values.put(History.Items.TITLE, title);
			values.put(History.Items.MAC, mac);
			values.put(History.Items.IP, ip);
			values.put(History.Items.PORT, port);
			this.parent.getContentResolver().insert(History.Items.CONTENT_URI, values);
		}
	}

	public void updateHistory(int id, String title, String mac, String ip, int port) {
		ContentValues values = new ContentValues(4);
		values.put(History.Items.TITLE, title);
		values.put(History.Items.MAC, mac);
		values.put(History.Items.IP, ip);
		values.put(History.Items.PORT, port);

		Uri itemUri = Uri.withAppendedPath(History.Items.CONTENT_URI, Integer.toString(id));
		this.parent.getContentResolver().update(itemUri, values, null, null);
	}

	public void incrementHistory(long id) {
		int usedCountColumn = cursor.getColumnIndex(History.Items.USED_COUNT);
		int usedCount = cursor.getInt(usedCountColumn);

		ContentValues values = new ContentValues(1);
		values.put(History.Items.USED_COUNT, usedCount+1);
		values.put(History.Items.LAST_USED_DATE, System.currentTimeMillis());

		Uri itemUri = Uri.withAppendedPath(History.Items.CONTENT_URI, Long.toString(id));
		this.parent.getContentResolver().update(itemUri, values, null, null);
	}

	public void deleteHistory(int id) {
		// use HistoryProvider to remove this row
		Uri itemUri = Uri.withAppendedPath(History.Items.CONTENT_URI, Integer.toString(id));
		this.parent.getContentResolver().delete(itemUri, null, null);
	}

	public void addHistoryListClickListener(HistoryListClickListener l) {
		this.listeners.add(l);
	}

	public void removeHistoryListClickListener(HistoryListClickListener l) {
		this.listeners.remove(l);
	}

}
