package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class LookupExpListAdapter extends BaseExpandableListAdapter {

    final Context context;
    final List<String> inventoryLabels;
    final Map<String, List<SpannableStringBuilder>> inventoryDetailsDict;

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

        String lang = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.exp_list_parent, null);
        }

        TextView txtParent = convertView.findViewById(R.id.txtParent);

        if (inventoryDetailsDict.toString().contains("Wells")) {
            txtParent.setBackgroundColor(Color.parseColor("#ff92cbff"));
        } else if (inventoryDetailsDict.toString().contains("Boreholes") | inventoryDetailsDict.toString().contains("Prospect")) {
            txtParent.setBackgroundColor(Color.parseColor("#ffcddfce"));
        } else if (inventoryDetailsDict.toString().contains("Outcrops")) {
            txtParent.setBackgroundColor(Color.parseColor("#ffffffb4"));
        } else if (inventoryDetailsDict.toString().contains("Shotpoints")) {
            txtParent.setBackgroundColor(Color.parseColor("#ffff8a86"));
        } else {
            txtParent.setBackgroundColor(Color.TRANSPARENT);
        }

        txtParent.setText(lang);


        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SpannableStringBuilder topic = (SpannableStringBuilder) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.exp_list_child, null);
        }

        TextView txtChild = convertView.findViewById(R.id.txtChild);

        if (childPosition % 2 != 0) {
            txtChild.setBackgroundColor(Color.parseColor("#ffd1d4d6"));
        } else {
            txtChild.setBackgroundColor(Color.parseColor("#ffdee2e6"));
        }

//        if (topic.toString().contains("Boreholes") || topic.toString().contains("Outcrops") || topic.toString().contains("Shotpoints") || topic.toString().contains("Wells") ) {
//            topic.setSpan(new ForegroundColorSpan(Color.RED), 0, topic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            topic.setSpan(new BackgroundColorSpan(Color.RED), 0, topic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            if(childPosition % 2 != 0) {
//                txtChild.setBackgroundColor(Color.parseColor("#fff1f4f7"));
//            }
//            else{
//                txtChild.setBackgroundColor(Color.parseColor("#ffd1d4d6"));
//            }
//        } else {
//            txtChild.setBackgroundColor(Color.TRANSPARENT);
//        }

        int width = getScreenWidth();
//        System.out.println("Width: " + width );

        if (topic.length() > (80)) {
            txtChild.setText(createIndentedText(topic, 0, 30));
        } else {
            txtChild.setText(topic);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public static SpannableString createIndentedText(Spannable text, int marginFirstLine, int marginNextLines) {
        //https://www.programmersought.com/article/45371641877/
        SpannableString result = new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines), 0, text.length(), 0);
        return result;
    }

    public static int getScreenWidth() {
        //https://stackoverflow.com/a/31377616
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
}