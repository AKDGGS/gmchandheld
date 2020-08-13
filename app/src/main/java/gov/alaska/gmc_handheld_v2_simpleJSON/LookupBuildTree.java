package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.alaska.gmc_handheld_v2_simpleJSON.comparators.SortInventoryObjectList;

import static android.graphics.Typeface.BOLD;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class LookupBuildTree{

	private final Context mContext;

	public LookupBuildTree(Context mContext) {
		this.mContext = mContext;
	}

//*********************************************************************************************

	public void buildLookupLayout(String rawJSON) throws Exception {

		// Constructs the layout for lookupBuildTree
		LinearLayout linearLayout = new LinearLayout(mContext);
		LinearLayout.LayoutParams linearLayoutParams = new LinearLayout
				.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		linearLayout.setLayoutParams(linearLayoutParams);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		// Constructs the expandableList
		LinearLayout.LayoutParams expListParams = new LinearLayout
				.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		ExpandableListView expandableListView = new ExpandableListView(mContext.getApplicationContext());
		expandableListView.setLayoutParams(expListParams);
		// removes the down arrow indicator for the expandable list
		expandableListView.setGroupIndicator(null);
		linearLayout.addView(expandableListView);

		Activity activity = (Activity) mContext;
		activity.setContentView(linearLayout);

		ArrayList<SpannableStringBuilder> displayList = new ArrayList<>();
		List<String> keyList = new ArrayList<>();
		Map<String, List<SpannableStringBuilder>> displayDict = new HashMap<>();

		JSONArray inputJson = new JSONArray((rawJSON));

		InventoryObject root = parseTree(null, null, inputJson);
		if(root != null) {
			processForDisplay(root, displayDict, displayList, keyList);
		}

		//What appears on the screen

		if(inputJson.getJSONObject(0).get("containerPath") != null) {
			((AppCompatActivity) mContext).getSupportActionBar().setTitle(inputJson.getJSONObject(0).get("containerPath").toString());

			if(inputJson.length() > 1) {
				((AppCompatActivity) mContext).getSupportActionBar().setSubtitle("Count: " + inputJson.length());
			}
		}
		ExpandableListAdapter listAdapter = new LookupExpListAdapter(mContext, keyList, displayDict);
		expandableListView.setAdapter(listAdapter);
	}

//*********************************************************************************************

	private SpannableStringBuilder getStringForDisplay(InventoryObject o,
													   SpannableStringBuilder ssb, int depth) {

		// This function deals with the children of the each container and their descendants.
		// So, GMC-000076260 has 12 children at the next depth.  And some of the 12 descendants have additional descendants.
		// And, each of the 32 containers in PAL-840 has 9 children at the next depth.
		// All descendants are grouped to immediate children of the container.

		if (o.getName() != null) {
			for (int i = 0; i < depth; i++) {
				ssb.append("  ");
			}
			int lengthOfSsb = ssb.length();
			if (o.getValue() != null) {

				ssb.append(o.getName());
				ssb.append(" ");
				ssb.append(o.getValue().toString());
				ssb.append("\n");
			} else {
				ssb.append(o.getName()).append("\n");
			}
			ssb.setSpan(new StyleSpan(BOLD), lengthOfSsb,
					lengthOfSsb + o.getName().length(), SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		for (int i = 0; i < o.getChildren().size(); i++) {
			// Sorts internally.
			Collections.sort(o.getChildren(), new SortInventoryObjectList());

			InventoryObject child = o.getChildren().get(i);

			// Adds a new line after the first element of an array of elements handled by handleObject().
			//Applies to Wells/Operators/etc...that have more than 1 element.
			//Used to improve readability.
			if (i > 0
					&& child.getName().contains(o.getName().substring(0, o.getName().length() - 1))
					&& (!o.getName().equals("ID"))){
				ssb.append("\n");
			}

			getStringForDisplay(o.getChildren().get(i), ssb, depth + 1);
		}

		return ssb;
	}

//*********************************************************************************************

	public void processForDisplay(InventoryObject n, Map<String, List<SpannableStringBuilder>> displayDict, ArrayList<SpannableStringBuilder> displayList, List<String> keyList) {
		// This function deals with the root level and the children of root.
		// The first two depths consist of null names and values, but both have children.
		// Root has the number of containers the in the container.
		// So, GMC-000076260 has 1 container while PAL-840 has 32 containers.

		String barcode = null;
		String ID = null;

		// sorts externally
		Collections.sort(n.getChildren(), new SortInventoryObjectList());
		
		for (InventoryObject ch : n.getChildren()) {
			//Used to define the label for the expandableList.
			if (ch.getName() != null && ch.getName().equals("Barcode")) {
				barcode = ch.getValue().toString();
			}

			if (n.getName() == null && ch.getName() != null && ch.getName().equals("ID")) {
				ID = ch.getValue().toString();
			}

			if (ch.getName() != null) {
				// Each container defined below has its own displayList.
				// Each non-null name at the first depth below the container gets a SpannableStringBuilder
				SpannableStringBuilder ssb = new SpannableStringBuilder();

				// getStringForDisplay() processes the string for display.
				// It makes keys bold and it groups children with parents.
				getStringForDisplay(ch, ssb, 0);

				//Removes a trailing "\n"
				if (ssb.length() > 0) {
					ssb.delete(ssb.length() - 1, ssb.length());
				}

				displayList.add(ssb);

			} else if (n.getName() == null & ch.getChildren().size() > 0) {
				// Creates a new displayList for each container.  GMC-000076260 consists of one container.  PAL-840 consists of 32 containers.
				displayList = new ArrayList<>();
				processForDisplay(ch, displayDict, displayList, keyList);
			}
		}

		if (barcode != null && ID != null) {
			keyList.add(barcode + "-" + ID);
			displayDict.put(barcode + "-" + ID, displayList);
		}else if(ID != null){
			keyList.add(ID);
			displayDict.put(ID, displayList);
		}
	}


//*********************************************************************************************

	private InventoryObject parseTree(Object parent, String name, Object o) throws Exception {

		switch (o.getClass().getName()) {
			case "org.json.JSONObject":
				return handleObject(parent, name, (JSONObject) o);

			case "org.json.JSONArray":
				return handleArray(parent, name, (JSONArray) o);

			case "java.lang.String":
			case "java.lang.Boolean":
			case "java.lang.Integer":
			case "java.lang.Double":
				return handleSimple(parent, name, o);

			default:
				System.out.println("Unhandled class: " + o.getClass().getName());
				return null;
		}
	}

	//*********************************************************************************************

	private InventoryObject handleObject(Object parent, String name, JSONObject o) throws Exception {

		String newName;
		InventoryObject io;
		if (name == null) {
			io = new InventoryObject(name);
		} else {
			switch (name) {
				// Explicitly ignore these
				case "elevationUnit":
				case "intervalUnit":
				case "measuredDepthUnit":
				case "unit":
					return null;

				//Create these nodes
				case "boreholes":
					if (o.has("ID")) {
						newName = "Borehole " + o.get("ID");
						io = new InventoryObject(newName, 100);
					} else {
						io = new InventoryObject("Boreholes", 100);
					}
					break;
				case "collection":
					io = new InventoryObject("Collection", null, 500);
					break;
				case "operators":
					if (o.has("ID")) {
						newName = "Operator " + o.get("ID");
						io = new InventoryObject(newName, null, 50);
					} else {
						io = new InventoryObject("Operator", null, 50);
					}
					break;
				case "outcrops":
					if (o.has("ID")) {
						newName = "Outcrop " + o.get("ID");
						io = new InventoryObject(newName, null, 100);
					} else {
						io = new InventoryObject("Outcrop", null, 100);
					}
					break;
				case "prospect":
					io = new InventoryObject("Prospect", null, 0);
					break;
				case "shotline":
					io = new InventoryObject("Shotline");
					break;
				case "shotpoints":
					if (o.has("ID")) {
						newName = "Shotpoint " + o.get("ID");
						io = new InventoryObject(newName, null, 50);
					} else {
						io = new InventoryObject("Shotpoints", null, 50);
					}
					break;
				case "type":
					io = new InventoryObject("Type");
					break;
				case "wells":
					if (o.has("ID")) {
						newName = "Well " + o.get("ID");
						io = new InventoryObject(newName, null, 100);
					} else {
						io = new InventoryObject("Wells", null, 100);
					}
					break;
				default:
					io = new InventoryObject(name);
			}
		}

		for (Iterator<String> it = o.keys(); it.hasNext(); ) {
			String key = it.next();
			io.addChild(parseTree(o, key, o.get(key)));
		}

		return io;
	}


//*********************************************************************************************

	private InventoryObject handleArray(Object parent, String name, JSONArray a) throws Exception {
		InventoryObject io;

		if (name == null) {
			io = new InventoryObject(name);
		} else {
			switch (name) {
				case "keywords": {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < a.length(); i++) {
						if (a.get(i) instanceof String) {
							if (sb.length() > 0) {
								sb.append(", ");
							}
							sb.append(a.get(i));
						}
					}
					return new InventoryObject("Keywords", sb.toString(), 800);
				}

				//Create these nodes
				case "boreholes":
					io = new InventoryObject("Boreholes", null, 100);
					break;
				case "operators":
					io = new InventoryObject("Operators", null, 50);
					break;
				case "outcrops":
					io = new InventoryObject("Outcrops", null, 100);
					break;
				case "shotpoints":
					io = new InventoryObject("Shotpoints", null, 100);
					break;
				case "wells":
					io = new InventoryObject("Wells", null, 100);
					break;
				default:
					io = new InventoryObject(name);
			}
		}

		for (int i = 0; i < a.length(); i++) {
			io.addChild(parseTree(a, name, a.get(i)));
		}
		return io;
	}

//*********************************************************************************************

	private InventoryObject handleSimple(Object parent, String name, Object o) throws JSONException {
		// Simple values should always have a name
		if (name == null) {
			return null;
		}

		switch (name) {
			// Higher the displayWeight, the higher a priority an key has.
			// Items are sorted internally first, and the externally in processForDisplay()
			case "abbr":
				return null;
			case "altNames":
				return new InventoryObject("Alternative Names", o);
			case "APINumber":
				return new InventoryObject("API Number", o, 95);
			case "barcode":
				return new InventoryObject("Barcode", o, 1000);
			case "boxNumber":
				return new InventoryObject("Box Number", o, 950);
			case "class":
				return new InventoryObject("Class", o);
			case "completionDate":
				return new InventoryObject("Completion Date", o, 69);
			case "completionStatus":
				return new InventoryObject("Completion Status", o, 69);
			case "containerPath":
				return new InventoryObject("Container Path", o, 1000);
			case "coreNumber":
				return new InventoryObject("Core Number", o, 900);
			case "current":
				return new InventoryObject("Current", o);
			case "description":
				return new InventoryObject("Description", o);
			case "elevation": {
				JSONObject pjo = (JSONObject) parent;
				if (pjo.has("elevationUnit") && pjo.getJSONObject("elevationUnit").has("abbr")) {
					String val = o + " " + pjo.getJSONObject("elevationUnit").get("abbr");
					return new InventoryObject("Elevation", val, 900);
				}
				return new InventoryObject("Elevation", o, 900);
			}
			case "federal":
				return new InventoryObject("Federal", o, 70);
			case "ID":
				return new InventoryObject("ID", o, 1000);
			case "intervalBottom": {
				JSONObject pjo = (JSONObject) parent;
				if (pjo.has("intervalUnit") && pjo.getJSONObject("intervalUnit").has("abbr")) {
					String val = o + " " + pjo.getJSONObject("intervalUnit").get("abbr");
					return new InventoryObject("Interval Bottom", val, 902);
				}
				return new InventoryObject("Interval Bottom", o, 902);
			}
			case "intervalTop": {
				JSONObject pjo = (JSONObject) parent;
				if (pjo.has("intervalUnit") && pjo.getJSONObject("intervalUnit").has("abbr")) {
					String val = o + " " + pjo.getJSONObject("intervalUnit").get("abbr");
					return new InventoryObject("Interval Top", val, 902);
				}
				return new InventoryObject("Interval Top", o, 902);
			}
			case "keywords":
				return new InventoryObject("Keywords", o, 600);
			case "measuredDepth": {
				JSONObject pjo = (JSONObject) parent;
				if (pjo.has("measuredDepthUnit") && pjo.getJSONObject("measuredDepthUnit").has(
						"abbr")) {
					String val = o + " " + pjo.getJSONObject("measuredDepthUnit").get("abbr");
					return new InventoryObject("Measured Depth", val, 75);
				} else if (pjo.has("unit") && pjo.getJSONObject("unit").has("abbr")) {
					String val = o + " " + pjo.getJSONObject("unit").get("abbr");
					return new InventoryObject("Measured Depth", val, 75);
				}
				return new InventoryObject("Measured Depth", o, 75);
			}
			case "name":
				return new InventoryObject("Name", o, 100);
			case "number":
				return new InventoryObject("Number", o);
			case "onshore":
				return new InventoryObject("Onshore", o, 70);
			case "permitStatus":
				return new InventoryObject("Permit Status", o, 70);
			case "remark":
				return new InventoryObject("Remark", o, 900);
			case "sampleNumber":
				return new InventoryObject("Sample Number", o);
			case "setNumber":
				return new InventoryObject("Set Number", o);
			case "spudDate":
				return new InventoryObject("Spud Date", o, 60);
			case "type":
				return new InventoryObject("Type", o);
			case "verticalDepth": {
				JSONObject pjo = (JSONObject) parent;
				if (pjo.has("unit") && pjo.getJSONObject("unit").has("abbr")) {
					String val = o + " " + pjo.getJSONObject("unit").get("abbr");
					return new InventoryObject("Vertical Depth", val, 80);
				}
				return new InventoryObject("Vertical Depth", o, 80);
			}
			case "wellNumber":
				return new InventoryObject("Well Number", o, 94);
			default:
				return new InventoryObject(name, o);
		}
	}
}