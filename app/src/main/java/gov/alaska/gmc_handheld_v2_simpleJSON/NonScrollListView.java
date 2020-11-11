package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

// Directly From https://stackoverflow.com/a/45629226

public class NonScrollListView extends ListView {

	public NonScrollListView(Context context) {
		super(context);
	}

	public NonScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NonScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
				Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = getMeasuredHeight();
	}
}