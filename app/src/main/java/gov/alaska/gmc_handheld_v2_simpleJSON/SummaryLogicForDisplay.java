package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableStringBuilder;
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

public class SummaryLogicForDisplay {
	private List<String> keyList;
	private Map<String, List<SpannableStringBuilder>> displayDict;
	private int ID;
	private final NumberFormat nf = NumberFormat.getNumberInstance();
	private String barcodeQuery;


	public SummaryLogicForDisplay() {
		keyList = new ArrayList<>();
		displayDict = new HashMap<>();
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

	public void setBarcodeQuery(String barcodeQuery){
		this.barcodeQuery = barcodeQuery;
	}

	public String getBarcodeQuery(){return barcodeQuery;}


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
		if (o.getName() != null && !o.getName().equals(currKey) && !(o.getName().contains("Object"))) {

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
					String newName = "Object Borehole";
					io = new InventoryObject(newName, null, 100);
					break;
				}
				case "containers":
					if (o.has("container")) {
						return new InventoryObject("Container", o.get("container"), 500);

					}
					return null;
				case "collections":
					if (o.has("collection")) {
						return new InventoryObject("Collection", o.get("collection"), 500);
					}
					return null;
				case "outcrops": {
					String id = o.optString("ID");
					String newName = "Outcrop";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject("Object " + newName, null, 100);
					break;
				}
				case "prospect": {
					String id = o.optString("ID");
					String newName = "Prospect";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject("Object " + newName, null);
					break;
				}
				case "shotline": {
					String id = o.optString("ID");
					String newName = "Shotline";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject("Object " + newName, null);
					break;
				}
				case "shotpoints": {
					String id = o.optString("ID");
					String newName = "Shotpoints";
					if (!"".equals(Integer.toString(ID))) {
						newName += " ID " + id;
					}
					io = new InventoryObject("Object " + newName, null, 50);
					break;
				}
				case "wells":
					String id = o.optString("ID");
					String newName = "Well";
					io = new InventoryObject("Object " + newName, null, 100);
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

				//Create these nodes
				case "boreholes":
					io = new InventoryObject("Boreholes", null, 50);
					break;
				case "collections":
					io = new InventoryObject("Collections", null, 100);
					break;
				case "containers":
					io = new InventoryObject("Containers", null, 1000);
					break;
				case "outcrops":
					io = new InventoryObject("Outcrops", null, 50);
					break;
				case "shotpoints":
					io = new InventoryObject("Shotpoints", null, 50);
					break;
				case "wells":
					io = new InventoryObject("Wells", null, 50);
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
			case "barcode":
				return new InventoryObject("Barcode", o, 1000);

			case "borehole":
//				Object newO = o;
//				if (parent instanceof JSONObject) {
//					JSONObject pjo = (JSONObject) parent;
//					if (pjo.has("total")) {
//						newO = newO + " Count: " + pjo.optString("total");
//					}
//				}
//				return new InventoryObject("Borehole", newO, 900);
				return new InventoryObject("Borehole", o, 900);

			case "keywords":
				return new InventoryObject("Keywords", o, 700);
			case "total":
				return new InventoryObject("Total", o, 800);
			case "well":
				return new InventoryObject("Well", o, 900);
			default:
				return new InventoryObject(name, o);
		}
	}
}