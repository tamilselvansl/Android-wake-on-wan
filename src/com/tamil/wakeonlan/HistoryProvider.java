package com.tamil.wakeonlan;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 *	Custom ContentProvider to wrap underlying datastore
 */

public class HistoryProvider extends ContentProvider {

	private static final String TAG = "HistoryProvider";

	private static final String DATABASE_NAME = "wakeonlan_history.db";
	private static final int DATABASE_VERSION = 2;

	private static HashMap<String, String> sHistoryProjectionMap;

	private static final String HISTORY_TABLE_NAME = "history";

	private static final int HISTORY = 1;
	private static final int HISTORY_ID = 2;

	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE history ("
					+ History.Items._ID + " INTEGER PRIMARY KEY,"
					+ History.Items.TITLE + " TEXT,"
					+ History.Items.MAC + " TEXT,"
					+ History.Items.IP + " TEXT,"
					+ History.Items.PORT + " INTEGER,"
					+ History.Items.CREATED_DATE + " INTEGER,"
					+ History.Items.LAST_USED_DATE + " INTEGER,"
					+ History.Items.USED_COUNT + " INTEGER DEFAULT 1,"
					+ History.Items.IS_STARRED + " INTEGER DEFAULT 0"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if((oldVersion == 1) && (newVersion == 2)) {
				db.execSQL("ALTER TABLE history ADD COLUMN " + History.Items.USED_COUNT + " INTEGER DEFAULT 1;");
				db.execSQL("ALTER TABLE history ADD COLUMN " + History.Items.IS_STARRED + " INTEGER DEFAULT 0;");
			}
		}
	}

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(HISTORY_TABLE_NAME);
		qb.setProjectionMap(sHistoryProjectionMap);

		// if no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = History.Items.DEFAULT_SORT_ORDER;
		} else{
			orderBy = sortOrder;
		}

		// get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// tell the cursor what uri to watch, so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
			case HISTORY:
				return History.Items.CONTENT_TYPE;

			case HISTORY_ID:
				return History.Items.CONTENT_ITEM_TYPE;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// validate the requested uri
		if(sUriMatcher.match(uri) != HISTORY) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if(initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		Long now = System.currentTimeMillis();

		// make sure that the fields are all set
		if (!values.containsKey(History.Items.TITLE)) {
			values.put(History.Items.TITLE, "");
		}
		if (!values.containsKey(History.Items.MAC)) {
			values.put(History.Items.MAC, "");
		}
		if (!values.containsKey(History.Items.IP)) {
			values.put(History.Items.IP, "");
		}
		if (!values.containsKey(History.Items.PORT)) {
			values.put(History.Items.PORT, "");
		}
		if (!values.containsKey(History.Items.CREATED_DATE)) {
			values.put(History.Items.CREATED_DATE, now);
		}
		if (!values.containsKey(History.Items.LAST_USED_DATE)) {
			values.put(History.Items.LAST_USED_DATE, now);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		// insert record, 2nd param is NULLABLE field for if values is empty
		long rowId = db.insert(HISTORY_TABLE_NAME, History.Items.MAC, values);
		if (rowId > 0) {
			Uri histUri = ContentUris.withAppendedId(History.Items.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(histUri, null);
			return histUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
			case HISTORY:
				count = db.delete(HISTORY_TABLE_NAME, where, whereArgs);
				break;

			case HISTORY_ID:
				String histId = uri.getPathSegments().get(1);
				count = db.delete(HISTORY_TABLE_NAME, History.Items._ID + "=" + histId
						+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count;
		switch (sUriMatcher.match(uri)) {
			case HISTORY:
				count = db.update(HISTORY_TABLE_NAME, values, where, whereArgs);
				break;

			case HISTORY_ID:
				String historyId = uri.getPathSegments().get(1);
				count = db.update(HISTORY_TABLE_NAME, values, History.Items._ID + "=" + historyId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(History.AUTHORITY, "history", HISTORY);
		sUriMatcher.addURI(History.AUTHORITY, "history/#", HISTORY_ID);

		sHistoryProjectionMap = new HashMap<String, String>();
		sHistoryProjectionMap.put(History.Items._ID, History.Items._ID);
		sHistoryProjectionMap.put(History.Items.TITLE, History.Items.TITLE);
		sHistoryProjectionMap.put(History.Items.MAC, History.Items.MAC);
		sHistoryProjectionMap.put(History.Items.IP, History.Items.IP);
		sHistoryProjectionMap.put(History.Items.PORT, History.Items.PORT);
		sHistoryProjectionMap.put(History.Items.CREATED_DATE, History.Items.CREATED_DATE);
		sHistoryProjectionMap.put(History.Items.LAST_USED_DATE, History.Items.LAST_USED_DATE);
		sHistoryProjectionMap.put(History.Items.USED_COUNT, History.Items.USED_COUNT);
		sHistoryProjectionMap.put(History.Items.IS_STARRED, History.Items.IS_STARRED);
	}

}
