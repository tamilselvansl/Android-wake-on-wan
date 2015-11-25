

package com.tamil.wakeonlan;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import com.tamil.android.wakeonlan.R;
import com.tamil.widget.StarButton;


/**
 *	Custom adapter to aid in UI binding
 */

public class HistoryAdapter extends ResourceCursorAdapter implements OnCheckedChangeListener {

	private static final String TAG = "HistoryAdapter";

	private Context context;
	private ContentResolver content;

	boolean showStars;


	public HistoryAdapter(Context context, Cursor cursor, boolean showStars) {
		super(context, R.layout.history_row, cursor);
		this.context = context;
		this.content = context.getContentResolver();
		this.showStars = showStars;
	}


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// load our column indexes
		int idColumn = cursor.getColumnIndex(History.Items._ID);
		int titleColumn = cursor.getColumnIndex(History.Items.TITLE);
		int macColumn = cursor.getColumnIndex(History.Items.MAC);
		int ipColumn = cursor.getColumnIndex(History.Items.IP);
		int portColumn = cursor.getColumnIndex(History.Items.PORT);
		int isStarredColumn = cursor.getColumnIndex(History.Items.IS_STARRED);

		TextView vtitle = (TextView) view.findViewById(R.id.history_row_title);
		TextView vmac = (TextView) view.findViewById(R.id.history_row_mac);
		TextView vip = (TextView) view.findViewById(R.id.history_row_ip);
		TextView vport = (TextView) view.findViewById(R.id.history_row_port);
		StarButton star = (StarButton) view.findViewById(R.id.history_row_star);

		// bind the cursor data to the form items
		vtitle.setText(cursor.getString(titleColumn));
		vmac.setText(cursor.getString(macColumn));
		vip.setText(cursor.getString(ipColumn));
		vport.setText(Integer.toString(cursor.getInt(portColumn)));

		if(this.showStars) {
			// remove click handler to prevent recursive calls
			star.setOnCheckedChangeListener(null);

			// change the star state if different
			boolean starred = (cursor.getInt(isStarredColumn) != 0);	// non-zero == true
			star.setChecked(starred);
			star.render();

			// add event listener to star button
			star.setOnCheckedChangeListener(this);

			// save our record _ID in the star's tag
			star.setTag(cursor.getInt(idColumn));

		} else{
			// disable the star button
			star.setClickable(false);
			star.noRender = true;
			star.render();
		}
	}


	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// extract record's _ID from tag
		int id = (Integer) buttonView.getTag();

		if (isChecked) {
			setIsStarred(id, 1);
		} else{
			setIsStarred(id, 0);
		}
	}

	private void setIsStarred(int id, int value) {
		// update history setting is_starred to value
		ContentValues values = new ContentValues(1);
		values.put(History.Items.IS_STARRED, value);

		Uri itemUri = Uri.withAppendedPath(History.Items.CONTENT_URI, Integer.toString(id));
		this.content.update(itemUri, values, null, null);
	}
}
