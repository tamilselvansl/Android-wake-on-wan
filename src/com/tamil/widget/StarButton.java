
package com.tamil.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.tamil.android.wakeonlan.R;


/**
 *	Custom button type to implement Google-style favourite star
 *
 */
public class StarButton extends CompoundButton implements OnCheckedChangeListener {

	private static final String TAG = "StarButton";

	public boolean noRender = false;


	public StarButton(Context context) {
		super(context);
		init(context);
	}

	public StarButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public StarButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}


	private void init(Context context) {
		setOnCheckedChangeListener(this);
		render();
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		render();
	}

	public void render() {
		// render the icon on this button
		if (noRender) {
			setButtonDrawable(android.R.color.transparent);
		} else if (isChecked()) {
			setButtonDrawable(R.drawable.btn_star_big_on);
		} else {
			setButtonDrawable(R.drawable.btn_star_big_off);
		}
	}

}
