package com.tamil.wakeonlan;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;
import com.tamil.android.wakeonlan.R;

/**
 * This class is used to setup the home screen widget, as well as handle click events
 *
 */

public class WidgetProvider extends AppWidgetProvider {

	public static final String TAG = "WidgetProvider";

	public static final String SETTINGS_PREFIX = "widget_";
	public static final String WIDGET_ONCLICK = "com.tamil.android.wakeonlan.WidgetOnClick";

	/**
	 * this method is called once when the WidgetHost starts (usually when the OS boots).
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		SharedPreferences settings = context.getSharedPreferences(WakeOnLanActivity.TAG, 0);

		final int N = appWidgetIds.length;
        for (int widget_id : appWidgetIds) {
            HistoryItem item = loadItemPref(context, settings, widget_id);
            if (item == null) {
                // item or prefrences missing.
                // TODO delete the widget probably (cant find a way to do this).
                // maybe set the title of the widget to ERROR
                continue;
            }
            configureWidget(widget_id, item, context);
        }
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if(intent.getAction().startsWith(WIDGET_ONCLICK)) {
			SharedPreferences settings = context.getSharedPreferences(WakeOnLanActivity.TAG, 0);

			// get the widget id
			int widget_id = getWidgetId(intent);
			if(widget_id == AppWidgetManager.INVALID_APPWIDGET_ID) {
				return;
			}

			// get the HistoryItem associated with the widget_id
			HistoryItem item = loadItemPref(context, settings, widget_id);

			// send the packet
			WakeOnLanActivity.sendPacket(context, item.title, item.mac, item.ip, item.port);
		}
	}

	@Override
	public void onDeleted(Context context, int[] id) {
		super.onDeleted(context, id);

		SharedPreferences settings = context.getSharedPreferences(WakeOnLanActivity.TAG, 0);

		final int N = id.length;
        for (int anId : id) {
            deleteItemPref(settings, anId);
        }
	}

	/**
	 * @desc	gets the widget id from an intent
	 */
	public static int getWidgetId(Intent intent) {
		Bundle extras = intent.getExtras();
		if(extras != null) {
			return extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		return AppWidgetManager.INVALID_APPWIDGET_ID;
	}

	/**
	 * @desc	configures a widget for the first time. Usually called when creating a widget
	 *				for the first time or initialising existing widgets when the AppWidgetManager
	 *				restarts (usually when the phone reboots).
	 */
	public static void configureWidget(int widget_id, HistoryItem item, Context context) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		views.setTextViewText(R.id.appwidget_text, item.title);

		// append id to action to prevent clearing the extras bundle
		views.setOnClickPendingIntent(R.id.appwidget_button, getPendingSelfIntent(context, widget_id, WIDGET_ONCLICK + widget_id));

		// tell the widget manager
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(widget_id, views);
	}

	private static PendingIntent getPendingSelfIntent(Context context, int widget_id, String action) {
		Intent intent = new Intent(context, WidgetProvider.class);
		intent.setAction(action);
		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
		intent.putExtras(bundle);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	/**
	 * @desc	saves the given history item/widget_id combination
	 */
	public static void saveItemPref(SharedPreferences settings, HistoryItem item, int widget_id) {
		SharedPreferences.Editor editor = settings.edit();

		// store HistoryItem details in settings
		editor.putInt(SETTINGS_PREFIX + widget_id, item.id);
		editor.putString(SETTINGS_PREFIX + widget_id + History.Items.TITLE, item.title);
		editor.putString(SETTINGS_PREFIX + widget_id + History.Items.MAC, item.mac);
		editor.putString(SETTINGS_PREFIX + widget_id + History.Items.IP, item.ip);
		editor.putInt(SETTINGS_PREFIX + widget_id + History.Items.PORT, item.port);
		editor.commit();
	}

	public static void deleteItemPref(SharedPreferences settings, int widget_id) {
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(SETTINGS_PREFIX + widget_id);
		editor.remove(SETTINGS_PREFIX + widget_id + History.Items.TITLE);
		editor.remove(SETTINGS_PREFIX + widget_id + History.Items.MAC);
		editor.remove(SETTINGS_PREFIX + widget_id + History.Items.IP);
		editor.remove(SETTINGS_PREFIX + widget_id + History.Items.PORT);
		editor.commit();
	}

	/**
	 * @desc	load the HistoryItem associated with a widget_id
	 */
	public static HistoryItem loadItemPref(Context context, SharedPreferences settings, int widget_id) {
		// get item_id
		int item_id = settings.getInt(SETTINGS_PREFIX + widget_id, -1);

		if(item_id == -1) {
			// No item_id found for given widget return null
			return null;
		}

		String title = settings.getString(SETTINGS_PREFIX + widget_id + History.Items.TITLE, "");
		String mac = settings.getString(SETTINGS_PREFIX + widget_id + History.Items.MAC, "");
		String ip = settings.getString(SETTINGS_PREFIX + widget_id + History.Items.IP, "");
		int port = settings.getInt(SETTINGS_PREFIX + widget_id + History.Items.PORT, -1);

		return new HistoryItem(item_id, title, mac, ip, port);
	}

}
