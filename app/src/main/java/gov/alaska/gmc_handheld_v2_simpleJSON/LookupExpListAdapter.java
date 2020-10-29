package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LookupExpListAdapter extends BaseExpandableListAdapter {

	final Context context;
	final List<String> inventoryLabels;
	final Map<String, List<SpannableStringBuilder>> inventoryDetailsDict;
	private String inventoryObjType = null;

	static class ParentViewHolder {
		TextView parentText;
	}


	public String getInventoryObjType() {
		return inventoryObjType;
	}

	public void setInventoryObjType(String inventoryObjType) {
		this.inventoryObjType = inventoryObjType;
	}

	public LookupExpListAdapter(Context context, List<String> inventoryLabels, Map<String, List<SpannableStringBuilder>> inventoryDetailsDict) {
		this.context = context;
		this.inventoryLabels = inventoryLabels;
		this.inventoryDetailsDict = inventoryDetailsDict;
	}

	@Override
	public int getGroupCount() {
		return inventoryLabels.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return inventoryDetailsDict.get(inventoryLabels.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return inventoryLabels.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return inventoryDetailsDict.get(inventoryLabels.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

		String expListParentLabel = (String) getGroup(groupPosition);
		ParentViewHolder parentHolder = new ParentViewHolder();

//		if (convertView == null) {
//			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			convertView = inflater.inflate(R.layout.exp_list_parent, null);
//		}
//
//		TextView txtParent = convertView.findViewById(R.id.txtParent);

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.exp_list_parent, null);
			parentHolder.parentText = convertView.findViewById(R.id.txtParent);
			convertView.setTag(parentHolder);
		}

		parentHolder = (ParentViewHolder) convertView.getTag();

		List<String> myList = Arrays.asList("Wells", "Outcrops", "Boreholes", "Shotpoints");
		int count = 0;
		for (String s : myList) {
			if (inventoryDetailsDict.toString().contains(s)) {
				count++;
				setInventoryObjType(s);
			}
		}

		if (inventoryObjType == null || count > 1) {
			setInventoryObjType("No Type");
		}

		switch (getInventoryObjType()) {
			case "Wells":
				parentHolder.parentText.setBackgroundColor(Color.parseColor("#ff92cbff"));
				break;
			case "Boreholes":
				parentHolder.parentText.setBackgroundColor(Color.parseColor("#ff63ba00")); //Green
				break;
			case "Outcrops":
				parentHolder.parentText.setBackgroundColor(Color.parseColor("#ffe6b101")); // yellow-orange
				break;
			case "Shotpoints":
				parentHolder.parentText.setBackgroundColor(Color.parseColor("#ffff8a86"));
				break;
			default:
				parentHolder.parentText.setBackgroundColor(Color.parseColor("#ffd9dddf"));
		}

		parentHolder.parentText.setText(expListParentLabel);
//		txtParent.setText(expListParentLabel);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		SpannableStringBuilder expListChildContents = (SpannableStringBuilder) getChild(groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.exp_list_child, null);
		}

		TextView txtChild = convertView.findViewById(R.id.txtChild);

		switch (getInventoryObjType()) {

			case "Boreholes":
			case "Prospect":
				if (childPosition % 2 != 0) {
					txtChild.setBackgroundColor(Color.parseColor("#ffa9d479")); // darker green
				} else {
					txtChild.setBackgroundColor(Color.parseColor("#ffbdd4a2")); //lighter green
				}
				break;
			case "Wells":
				if (childPosition % 2 != 0) {
					txtChild.setBackgroundColor(Color.parseColor("#ffb9e0ff")); //Light blue
				} else {
					txtChild.setBackgroundColor(Color.parseColor("#ffd2ebff")); //very light blue
				}
				break;
			case "Outcrops":
				if (childPosition % 2 != 0) {
					txtChild.setBackgroundColor(Color.parseColor("#ffe6cb71")); //ochre
				} else {
					txtChild.setBackgroundColor(Color.parseColor("#ffead698")); //pale ochre
				}
				break;
			case "Shotpoints":
				if (childPosition % 2 != 0) {
					txtChild.setBackgroundColor(Color.parseColor("#ffffcecd"));
				} else {
					txtChild.setBackgroundColor(Color.parseColor("#ffffbab9"));
				}
				break;
			case "No Type":
			default:
				if (childPosition % 2 != 0) {
					txtChild.setBackgroundColor(Color.parseColor("#fff3f6f8"));
				} else {
					txtChild.setBackgroundColor(Color.parseColor("#fffcfdfe"));
				}
				break;
		}



		txtChild.setText(expListChildContents);
		return convertView;

	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public static SpannableStringBuilder createIndentedText(SpannableStringBuilder text, int marginFirstLine, int marginNextLines) {
		//https://www.programmersought.com/article/45371641877/

		text.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines), 0, text.length(), 0);
		return text;
	}

	public static int getScreenWidth() {
		//https://stackoverflow.com/a/31377616
		return Resources.getSystem().getDisplayMetrics().widthPixels;
	}


}

// GMC-000197640