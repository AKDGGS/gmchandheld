package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;

import org.json.JSONArray;
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

public class LookupLogicForDisplay {

	private List<String> keyList;
	private Map<String, List<SpannableStringBuilder>> displayDict;
	private int ID;
	private final NumberFormat nf = NumberFormat.getNumberInstance();
	private boolean radiationWarningFlag;
	private String barcodeQuery;


	public LookupLogicForDisplay() {
		keyList = new ArrayList<>();
		displayDict = new HashMap<>();
		radiationWarningFlag = false;
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);
	}

	public List<String> getKeyList() {
		return keyList;
	}

	public Map<String, List<SpannableStringBuilder>> getDisplayDict() {
		return displayDict;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public boolean getRadiationWarningFlag() {
		return radiationWarningFlag;
	}

	public void setBarcodeQuery(String barcodeQuery) {
		this.barcodeQuery = barcodeQuery;
	}

	public String getBarcodeQuery() {
		return barcodeQuery;
	}


	//*********************************************************************************************

	public void processRawJSON(String rawJSON) throws Exception {

		if (rawJSON.trim().charAt(0) == '[') {
			JSONArray inputJson = new JSONArray((rawJSON));  // check for jsonarray

			InventoryObject root = parseTree(null, null, inputJson);

			if (root != null) {
				getStringForDisplay(root, 0, null, null, getDisplayDict());
			}
		} else if (rawJSON.trim().charAt(0) == '{') {
			JSONObject inputJson = new JSONObject((rawJSON));  // check for jsonobject

			InventoryObject root = parseTree(null, inputJson.opt("barcode").toString(), inputJson);


			if (root != null) {
				getStringForDisplay(root, 1, null, null, getDisplayDict());
			}
		}
	}

//*********************************************************************************************

	private void getStringForDisplay(InventoryObject o, int depth, String currKey, LinkedList<SpannableStringBuilder> displayList, Map<String, List<SpannableStringBuilder>> dict) {

		if (depth - 1 == 0) {
			currKey = o.getName();
			getKeyList().add(currKey);
			displayList = new LinkedList<>();
		}

		Collections.sort(o.getChildren(), new SortInventoryObjectList());

		SpannableStringBuilder ssb = new SpannableStringBuilder();
		if (o.getName() != null && !o.getName().equals(currKey)) {

			//Barcode is not added to the displayList because it is in the Label
			if (!"Barcode".equals(o.getName())) {
				for (int i = 0; i < depth - 1; i++) {
					// Indentation is 3 spaces
					// If this changes, you might need to change LookupExpListAdapter getChildView
					// in order to deal with multiline indentations.
					ssb.append("   ");
				}
				int lengthOfSsb;

				lengthOfSsb = ssb.length();
				ssb.append(o.getName());
				ssb.append(" ");
				if (o.getValue() != null) {
					ssb.append(o.getValue().toString());
				}
				ssb.setSpan(new StyleSpan(BOLD), lengthOfSsb,
						lengthOfSsb + o.getName().length(), SPAN_EXCLUSIVE_EXCLUSIVE);


				//Arbitrary Value

				int indentationIncrement = 42;

				if (!Character.isWhitespace(ssb.charAt(3))) {
					ssb = createIndentedText(ssb, 3, indentationIncrement);
				} else if (!Character.isWhitespace(ssb.charAt(6))) {
					ssb = createIndentedText(ssb, 6, indentationIncrement * 2);
				} else if (!Character.isWhitespace(ssb.charAt(9))) {
					ssb = createIndentedText(ssb, 9, indentationIncrement * 3);
				}  else {
					ssb = createIndentedText(ssb, 0, indentationIncrement * 4);
				}
				displayList.add(ssb);
				dict.put(currKey, displayList);
			}
		}

		for (InventoryObject child : o.getChildren()) {

			if (child.getName() != null) {
				getStringForDisplay(child, depth + 1, currKey, displayList, dict);
			}
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
			if (!"".equals(o.optString("ID"))) {
				String newName = "ID " + o.optInt("ID");
				if (!"".equals(o.optString("barcode"))) {
					newName += " / " + o.optString("barcode");
				}
				io = new InventoryObject(newName);
			} else {
				io = new InventoryObject(name);
			}
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
					String newName = "Borehole";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject(newName, null, 100);
					break;
				}
				case "collection":
					if (o.has("name")) {
						return new InventoryObject("Collection", o.get("name"), 500);
					}
					return null;
//				case "issues": {
//					String id = o.optString("ID");
//					String newName = "Issue";
//					if (!"".equals(Integer.toString(ID))) {
//						newName += " ID " + id;
//					}
//					io = new InventoryObject(newName, null, 100);
//					break;
//				}
				case "operators": {
					String newName = "Operator";
					if (o.has("current")) {
						newName += (o.optBoolean("current") ? " (Current)" : " (Previous)");
						io = new InventoryObject(newName, null, 50);
					} else {
						io = new InventoryObject(newName, null, 50);
					}
					break;
				}
				case "outcrops": {
					String id = o.optString("ID");
					String newName = "Outcrop";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject(newName, null, 100);
					break;
				}
				case "prospect": {
					String id = o.optString("ID");
					String newName = "Prospect";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject(newName, null);
					break;
				}
				case "shotline": {
					String id = o.optString("ID");
					String newName = "Shotline";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject(newName, null);
					break;
				}
				case "shotpoints": {
					String id = o.optString("ID");
					String newName = "Shotpoints";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject(newName, null, 50);
					break;
				}
				case "qualities": {
					String id = o.optString("ID");
					String newName = "Quality";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject(newName, null, 100);
					break;
				}
				case "type": {
					if (o.has("name")) {
						return new InventoryObject("Type", o.get("name"), 500);
					}
					return null;
				}
				case "wells": {
					String id = o.optString("ID");
					String newName = "Well";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject(newName, null, 100);
					break;
				}
				default: {
					io = new InventoryObject(name);
				}
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
				case "issues":
					io = new InventoryObject("Issues", null, 50);
					break;
				case "operators":
					io = new InventoryObject("Operators", null, 50);
					break;
				case "outcrops":
					io = new InventoryObject("Outcrops", null, 100);
					break;
				case "qualities":
					io = new InventoryObject("Qualities", null, 100);
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

	private InventoryObject handleSimple(Object parent, String name, Object o) {

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
						String abbr;

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
				String newName = name;
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;
					if (pjo.has("onshore")) {
						return null;
					}
					if (o instanceof Boolean) {
						if ((boolean) o) {
							newName = "Federal";
						} else {
							newName = "Non-Federal";
						}
						o = null;
					}
				}
				return new InventoryObject(newName, o, 70);
			case "ID":
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;
					//checks if ID is in top level.
					if (pjo.has("barcode") && o instanceof Integer) {
						if (o != null) {
							setID((Integer) o);
							return new InventoryObject("ID", o, 1000);
						}
					}
				}
				return null;
			case "intervalBottom": {
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;
					if (pjo.has("intervalTop")) {
						return null;
					}
					if (o instanceof Double) {
						String val = nf.format(o);
						String abbr;

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
			case "issues":
				if ("radiation_risk".equals(o.toString())) {
					radiationWarningFlag = true;
				}
				return new InventoryObject("Issue", o, 600);
			case "keywords":
				return new InventoryObject("Keywords", o, 600);
			case "measuredDepth": {
				if (parent instanceof JSONObject) {
					JSONObject pjo = (JSONObject) parent;

					if (o instanceof Double) {
						String val = nf.format(o);
						String abbr;

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
						if ((boolean) o) {
							name = "Onshore";
						} else {
							name = "Offshore";
						}
						o = null;
					}
					boolean fed = pjo.optBoolean("Federal");
					if (fed) {
						name = name + " / Federal";
					} else {
						name = name + " /  Non-Federal";
					}
				}
				return new InventoryObject(name, o, 70);
			case "permitStatus":
				return new InventoryObject("Permit Status", o, 70);
			case "radiationMSVH":
				if (((Double) o).floatValue() > 0) {
					radiationWarningFlag = true;
				}
				return new InventoryObject("Radiation MSVH", o, 1200);
			case "remark":
				if (o.toString().contains("\n")) {
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
						String abbr;

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

	public static SpannableStringBuilder createIndentedText(SpannableStringBuilder text, int marginFirstLine, int marginNextLines) {
		//https://www.programmersought.com/article/45371641877/
		text.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines), 0, text.length(), 0);
		return text;
	}
}