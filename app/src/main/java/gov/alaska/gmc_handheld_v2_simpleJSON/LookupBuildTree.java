package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gov.alaska.gmc_handheld_v2_simpleJSON.comparators.SortInventoryObjectList;

import static android.graphics.Typeface.BOLD;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class LookupBuildTree {

	private List<String> KeyList;
	private Map<String, List<SpannableStringBuilder>> DisplayDict;
	private String barcode;
	private int ID;

	public LookupBuildTree() {
		KeyList = new ArrayList<>();
		DisplayDict = new HashMap<>();
	}

	public List<String> getKeyList() {
		return KeyList;
	}

	public Map<String, List<SpannableStringBuilder>> getDisplayDict() {
		return DisplayDict;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

//*********************************************************************************************

	public void processRawJSON(String rawJSON) throws Exception {

		JSONArray inputJson = new JSONArray((rawJSON));

		InventoryObject root = parseTree(null, null, inputJson);

		if (root != null) {
			//depth is set to -1 to prevent the an extra indentation for root.
					getStringForDisplay(root, -1, null, null, getDisplayDict());
					getDisplayDict();
		}
	}

//*********************************************************************************************

	private void getStringForDisplay(InventoryObject o, int depth, String currKey, LinkedList<SpannableStringBuilder> displayList, Map<String, List<SpannableStringBuilder>> dict) {

		if (depth == 0){
			currKey = o.getName();
			getKeyList().add(currKey);
			displayList = new LinkedList<>();
		}

		Collections.sort(o.getChildren(), new SortInventoryObjectList());

		SpannableStringBuilder ssb = new SpannableStringBuilder();
		if (o.getName() != null && o.getName() != currKey) {

			//Barcode is not added to the displayList because it is in the Label
			if (!"Barcode".equals(o.getName())) {
				for (int i = 0; i < depth; i++) {
					// Indentation is 3 spaces
					// If this changes, you might need to change LookupExpListAdapter getChildView
					// in order to deal with multiline indentations.
					ssb.append("   ");
				}
				int lengthOfSsb;
				if (o.getValue() != null) {

					lengthOfSsb = ssb.length();
					ssb.append(o.getName());
					ssb.append(" ");
					ssb.append(o.getValue().toString());
					ssb.append("\n");
					ssb.setSpan(new StyleSpan(BOLD), lengthOfSsb,
							lengthOfSsb + o.getName().length(), SPAN_EXCLUSIVE_EXCLUSIVE);
					displayList.add(ssb);
					dict.put(currKey, displayList);

				} else {
					lengthOfSsb = ssb.length();
					ssb.append(o.getName()).append("\n");
					ssb.setSpan(new StyleSpan(BOLD), lengthOfSsb,
							lengthOfSsb + o.getName().length(), SPAN_EXCLUSIVE_EXCLUSIVE);
					displayList.add(ssb);
					dict.put(currKey, displayList);
				}
			}
		}

		for (
				int i = 0; i < o.getChildren().
				size();
				i++) {
			InventoryObject child = o.getChildren().get(i);

			if (child.getName() != null) {
				// Don't display the ID. The ID is either irrelevant or
				// already displayed in the name
				if (!"ID".equals(child.getName())) {
					getStringForDisplay(child, depth + 1, currKey, displayList, dict);
				}
			}
		}

		//Removes a trailing "\n"
		if (ssb.length() > 0) {
			ssb.delete(ssb.length() - 1, ssb.length());
		}

	}

//*********************************************************************************************

	public InventoryObject parseTree(Object parent, String name, Object o) throws Exception {

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

		InventoryObject io;

		if (name == null) {
			name = "ID " + o.opt("ID") + " / " + o.opt("barcode");
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
				case "boreholes": {
					String id = o.optString("ID");
					if (id != null) {
						io = new InventoryObject("Borehole ID " + id, null, 100);
					} else {
						io = new InventoryObject("Borehole", 100);
					}
					break;
				}
				case "collection":
					if (o.has("name")) {
						return new InventoryObject("Collection", o.get("name"), 500);
					}
					return null;
				case "operators": {
					String newName;
					if (o.has("current")) {
						if (true == (boolean) o.get("current")) {
							newName = "Operator " + "(Current)";
						} else {
							newName = "Operator " + "(Previous)";
						}
						io = new InventoryObject(newName, null, 50);
					} else {
						io = new InventoryObject("Operator", null, 50);
					}
					break;
				}
				case "outcrops": {
					String id = o.optString("ID");
					if (id != null) {
						io = new InventoryObject("Outcrop ID " + id, null, 100);
					} else {
						io = new InventoryObject("Outcrop", null, 100);
					}
					break;
				}
				case "prospect": {
					String id = o.optString("ID");
					if (id != null) {
						io = new InventoryObject("Prospect ID " + id, null, 0);
					} else {
						io = new InventoryObject("Prospect", null, 0);
					}
					break;
				}
				case "shotline": {
					String id = o.optString("ID");
					if (id != null) {
						io = new InventoryObject("Shotline ID " + id);
					} else {
						io = new InventoryObject("Shotline");
					}
					break;
				}
				case "shotpoints": {
					String id = o.optString("ID");
					if (id != null) {
						io = new InventoryObject("Shotpoints ID " + id, null, 50);
					} else {
						io = new InventoryObject("Shotpoints", null, 50);
					}
					break;
				}
				case "type":
					if (o.has("name")) {
						return new InventoryObject("Type", o.get("name"), 500);
					}
					return null;
				case "wells":
					String id = o.optString("ID");
					if (id != null) {
						io = new InventoryObject("Well ID " + id, null, 1000);
					} else {
						io = new InventoryObject("Wells", null, 1000);
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

	private InventoryObject handleArray(Object parent, String name, JSONArray a) throws
			Exception {
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

	private InventoryObject handleSimple(Object parent, String name, Object o) throws
			JSONException {

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);

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
				setBarcode(o.toString());
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
				return new InventoryObject("Container", o, 1000);
			case "coreNumber":
				return new InventoryObject("Core Number", o, 900);
			case "current":
				return null;
			case "description":
				return new InventoryObject("Description", o, 600);
			case "elevation": {
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;

					if (o instanceof Double) {
						String val = nf.format(o);
						String abbr = "";

						JSONObject u = pjo.optJSONObject("elevationUnit");
						if (u == null) {
							u = pjo.optJSONObject("unit");
						}
						if (u != null) {
							abbr = u.optString("abbr");
							val += " " + abbr;
						}
						return new InventoryObject("Elevation", val, 75);
					}
				}
				return new InventoryObject("Elevation", o, 75);
			}
			case "federal":
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;
					if (pjo.has("onshore")) {
						return null;
					}
					if (o instanceof Boolean) {
						if (true == (boolean) o) {
							name = "Federal";
							o = null;
						} else {
							name = "Non-Federal";
							o = null;
						}
					}
				}
				return new InventoryObject(name, o, 70);
			case "ID":
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;
					if (pjo.has("barcode")) {
						setID((Integer) o);
					}
				}
				return new InventoryObject("ID", o, 1000);
			case "intervalBottom": {
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;
					if (pjo.has("intervalTop")) {
						return null;
					}
					if (o instanceof Double) {
						String val = nf.format(o);
						String abbr = "";

						JSONObject iu = pjo.optJSONObject("intervalUnit");
						if (iu != null) {
							abbr = iu.optString("abbr");
							val += " " + abbr;
							return new InventoryObject("Interval Bottom", val, 902);
						}
					}
				}
				return new InventoryObject("Interval Bottom", o, 902);
			}
			case "intervalTop": {
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;
					if (o instanceof Double) {
						String val = nf.format(o);
						String abbr = "";

						JSONObject iu = pjo.optJSONObject("intervalUnit");
						if (iu != null) {
							abbr = iu.optString("abbr");
							val += " " + abbr;
						}

						Double ib = pjo.optDouble("intervalBottom");
						if (!ib.isNaN()) {
							String valBot = nf.format(ib);
							val += " - " + valBot;
							if (!"".equals(abbr)) {
								val += " " + abbr;
							}
						}
						return new InventoryObject("Interval ", val, 902);
					}
				}
				return new InventoryObject("Interval Top", o, 902);
			}
			case "keywords":
				return new InventoryObject("Keywords", o, 600);
			case "measuredDepth": {
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;

					if (o instanceof Double) {
						String val = nf.format(o);
						String abbr = "";

						JSONObject u = pjo.optJSONObject("measuredDepthUnit");
						if (u == null) {
							u = pjo.optJSONObject("unit");
						}
						if (u != null) {
							abbr = u.optString("abbr");
							val += " " + abbr;
						}
						return new InventoryObject("Measured Depth", val, 75);
					}
				}
				return new InventoryObject("Measured Depth", o, 75);
			}
			case "name":
				return new InventoryObject("Name", o, 1000);
			case "number":
				return new InventoryObject("Number", o);
			case "onshore":
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;

					if (o instanceof Boolean) {
						if (true == (boolean) o) {
							name = "Onshore";
							o = null;
						} else {
							name = "Offshore";
							o = null;
						}
					}
					boolean fed = pjo.optBoolean("Federal");
					if (fed == true) {
						name = name + " / Federal";
					} else {
						name = name + " /  Non-Federal";
					}
				}
				return new InventoryObject(name, o, 70);
			case "permitStatus":
				return new InventoryObject("Permit Status", o, 70);
			case "remark":
				if(o.toString().contains("\n")){
					o = o.toString().replace("\n", " ");
				}
				return new InventoryObject("Remark", o, 900);
			case "sampleNumber":
				return new InventoryObject("Sample Number", o, 600);
			case "setNumber":
				return new InventoryObject("Set Number", o, 1000);
			case "spudDate":
				return new InventoryObject("Spud Date", o, 60);
			case "verticalDepth": {
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;

					if (o instanceof Double) {
						String val = nf.format(o);
						String abbr = "";

						JSONObject u = pjo.optJSONObject("unit");

						if (u != null) {
							abbr = u.optString("abbr");
							val += " " + abbr;
						}
						return new InventoryObject("Vertical Depth", val, 75);
					}
				}
				return new InventoryObject("Vertical Depth", o, 75);
			}
			case "wellNumber":
				return new InventoryObject("Well Number", o, 94);
			case "year":
				return new InventoryObject("Year", o);
			default:
				return new InventoryObject(name, o);
		}
	}
}