package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

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

public class LookupBuildTree {

    public static Map<String, List<SpannableStringBuilder>> setupDisplay(JSONObject inputJson) throws JSONException {

        ArrayList<SpannableStringBuilder> displayList = new ArrayList<>();  //used for the app display (expandable list)
        Map<String, List<SpannableStringBuilder>> displayDict = new HashMap<>(); //used for the app display (expandable list)

        InventoryObject root = new InventoryObject(null, null);

        createNodes(inputJson, root);
        processForDisplay(root, displayList, root);
        fillDisplayDict(inputJson, displayList, displayDict);

        return displayDict;
    }

//*********************************************************************************************

    private static void createNodes(JSONObject mInputJson
            , InventoryObject root) throws JSONException {

        for (Iterator<String> it = mInputJson.keys(); it.hasNext(); ) {
            String keyObject = it.next();

            InventoryObject parent = root;
            Object val = mInputJson.get(keyObject);

            if (!(val instanceof JSONArray) && !(val instanceof JSONObject) && parent != null) {
                modifyNodes(keyObject, val, parent, mInputJson);
            }

            switch (val.getClass().getSimpleName()) {
                case "JSONObject":
                    parent = modifyNodes(keyObject, val, parent, mInputJson);
                    createNodes((JSONObject) val, parent);
                    break;
                case "JSONArray":
                    if (((JSONArray) val).get(0) instanceof String || ((JSONArray) val).get(0).getClass().isPrimitive()) {//checks if the array contains objects --> used for keywords
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < ((JSONArray) val).length(); i++) {
                            sb.append(((JSONArray) val).get(i));
                            if (i < ((JSONArray) val).length() - 1) {
                                sb.append(", ");
                            }
                        }

                        modifyNodes(keyObject, sb, parent, mInputJson);

                    } else {
                        parent = modifyNodes(keyObject, val, parent, mInputJson);
                        for (int i = 0; i < ((JSONArray) val).length(); i++) {
                            createNodes((JSONObject) ((JSONArray) val).get(i), parent);
                        }
                    }
                    break;
            }
        }
    }

//*********************************************************************************************

    private static InventoryObject modifyNodes(String key, Object val,
                                               InventoryObject parent,
                                               JSONObject mInputJson) throws JSONException {
        InventoryObject invObj;
        String unitAbbr = null;

        switch (key) {
            case "abbr":
                return new InventoryObject("Abbr", val, parent);
            case "APINumber":
                return new InventoryObject("API Number", val, parent);
            case "barcode":
                return new InventoryObject("Barcode", val, parent, 1002);
            case "boreholes":
                return new InventoryObject("Boreholes", val, parent, 100);
            case "boxNumber":
                return new InventoryObject("Box Number", val, parent, 1000);
            case "class":
                return new InventoryObject("Class", val, parent);
            case "collection":
                return new InventoryObject("Collection", val, parent, 900);
            case "completionDate":
                return new InventoryObject("Completion Date", val, parent);
            case "completionStatus":
                return new InventoryObject("Completion Status", val, parent);
            case "containerPath":
                return new InventoryObject("Container Path", val, parent, 1001);
            case "coreNumber":
                return new InventoryObject("Core Number", val, parent, 1003);
            case "current":
                return new InventoryObject("Current", val, parent);
            case "description":
                return new InventoryObject("Description", val, parent, 950);
            case "elevation":
                return new InventoryObject("Elevation", val, parent);
            case "elevationUnit":
                return new InventoryObject("Elevation Unit", val, parent);
            case "federal":
                return new InventoryObject("Federal", val, parent);
            case "ID":
                if (parent.getKey() == null) {
                    invObj = new InventoryObject("ID", val, parent, 10000);
                } else {
                    invObj = new InventoryObject("ID", val, parent);
                    invObj.setDisplayWeight(parent.getDisplayWeight());
                }
                return invObj;
            case "intervalBottom":
                invObj = new InventoryObject("Interval Bottom", val, parent, 1005);

                if (mInputJson.has("intervalUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("intervalUnit");
                    if (tempObj.has("abbr")) {
                        unitAbbr = mInputJson.getJSONObject("intervalUnit").get("abbr").toString();
                        invObj.setValue(invObj.getValue() + " " + unitAbbr);
                    }
                } else if (unitAbbr == null) {
                    invObj.setValue(invObj.getValue());
                }
                return invObj;
            case "intervalTop":
                invObj = new InventoryObject("Interval Top", val, parent, 1005);
                if (mInputJson.has("intervalUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("intervalUnit");
                    if (tempObj.has("abbr")) {
                        unitAbbr = mInputJson.getJSONObject("intervalUnit").get("abbr").toString();
                        invObj.setValue(invObj.getValue() + " " + unitAbbr);
                    }
                } else if (unitAbbr == null) {
                    invObj.setValue(invObj.getValue());
                }
                return invObj;
            case "intervalUnit":
                return null;
            case "keywords":
                return new InventoryObject("Keywords", val, parent, 950);
            case "measuredDepth":
                invObj = new InventoryObject("Measured Depth", val, parent, parent.getDisplayWeight() - 5);
                if (mInputJson.has("measuredUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("measuredUnit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("measuredUnit").get("abbr").toString();
                } else if (mInputJson.has("unit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("unit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("unit").get("abbr").toString();
                }

                if (unitAbbr != null) {
                    invObj.setValue(invObj.getValue() + " " + unitAbbr);
                }
                return invObj;
            case "measuredDepthUnit":
                return null;
            case "name":
                return new InventoryObject("Name", val, parent);
            case "number":
                return new InventoryObject("Number", val, parent);
            case "onshore":
                return new InventoryObject("Onshore", val, parent);
            case "operators":
                return new InventoryObject("Operators", val, parent, parent.getDisplayWeight() - 10);
            case "outcrops":
                return new InventoryObject("Outcrops", val, parent, 100);
            case "permitStatus":
                return new InventoryObject("Permit Status", val, parent);
            case "prospect":
                return new InventoryObject("Prospect", val, parent);
            case "remark":
            case "remarks":
                if (parent.getKey() == null) {
                    return new InventoryObject("Remark", val, parent, 950);
                } else {
                    return new InventoryObject("Remark", val, parent);
                }
            case "sampleNumber":
                return new InventoryObject("Sample Number", val, parent, 950);
            case "setNumber":
                return new InventoryObject("Set Number", val, parent, 950);
            case "shotline":
                invObj = new InventoryObject("Shotline", val, parent);
                return invObj;
            case "shotpoints":
                return new InventoryObject("Shotpoints", val, parent, 100);
            case "spudDate":
                return new InventoryObject("Spud Date", val, parent);
            case "type":
                return new InventoryObject("Type", val, parent);
            case "unit":
                return null;

            case "verticalDepth":
                invObj = new InventoryObject("Vertical Depth", val, parent, parent.getDisplayWeight() - 1);

                if (mInputJson.has("unit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("unit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("unit").get("abbr").toString();
                }

                if (unitAbbr != null) {
                    invObj.setValue(invObj.getValue() + " " + unitAbbr);
                }

                return invObj;
            case "wellNumber":
                return new InventoryObject("Well Number", val, parent);
            case "wells":
                return new InventoryObject("Wells", val, parent, 100);
            default:
                return new InventoryObject(key, val, parent);
        }
    }

//*********************************************************************************************

    private static void processForDisplay(InventoryObject mRoot, ArrayList<SpannableStringBuilder> displayList, InventoryObject root) {

        ArrayList<String> keyList = new ArrayList<>();  //list of all keys --> used with spannableStringBuilder to make all keys bold

        Collections.sort(mRoot.getChildren(), new SortInventoryObjectList()); //sort externally

        for (InventoryObject n : mRoot.getChildren()) {
            combineKeyValueStr(n, 0);  //contains the indent + key + value as a string
            if (n.getChildren().size() == 0) {
                displayList.add(new SpannableStringBuilder(n.getKeyValueWithIndent()));
            } else {
                if (n.getParent() == root) {  //Groups children under the parent
                    Collections.sort(n.getChildren(), new SortInventoryObjectList()); //sorts internally
                    ArrayList<InventoryObject> tempArrList = new ArrayList<>();
                    tempArrList.add(n);

                    for (InventoryObject nChild : n.getChildren()) {
                        String endCondition = n.getChildren().get(n.getChildren().size() - 1).getKey();
                        fillDisplayList(nChild, displayList, endCondition, tempArrList);
                    }
                }
            }

            keyList.add(n.getKey());
            if (n.getChildren().size() > 0) {
                getDescendants(n, keyList);
            }
        }


        for (SpannableStringBuilder s : displayList) {
            for (String k : keyList) {
                if (s.toString().contains(k)) {
                    int index = 0;
                    while (index != -1) {
                        index = s.toString().indexOf(k, index);
                        if (index != -1) {
                            s.setSpan(new StyleSpan(BOLD), index,
                                    index + k.length() + 1, SPAN_EXCLUSIVE_EXCLUSIVE);
                            index++;
                        }
                    }
                }
            }
        }
    }

//*********************************************************************************************

    private static void getDescendants(InventoryObject parent, ArrayList<String> keyList) {
        if (parent.getChildren().size() == 0) {
            keyList.add(parent.getKey());
        } else {
            keyList.add(parent.getKey());
            for (InventoryObject nChild : parent.getChildren()) {
                getDescendants(nChild, keyList);
            }
        }
    }

//*********************************************************************************************

    public static void combineKeyValueStr(InventoryObject n, int indent) {  //needed because of recursion

        if (n.getChildren().size() == 0) {
            n.setKeyValueWithIndent((printIndent(indent) + n.getKey()) + ": " + n.getValue().toString());
        } else {
            n.setKeyValueWithIndent((printIndent(indent) + n.getKey() + ":"));

            for (InventoryObject nChild : n.getChildren()) {
                combineKeyValueStr(nChild, indent + 1);
            }
        }
    }

//*********************************************************************************************

    private static String printIndent(int indent) {

        StringBuilder indentStr = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStr.append("    ");
        }
        return indentStr.toString();
    }

//*********************************************************************************************

    private static void fillDisplayList(InventoryObject n, ArrayList<SpannableStringBuilder> tmpStrList, String endCondition, ArrayList<InventoryObject> tempArrList) {

        tempArrList.add(n);

        if (n.getKey().equals(endCondition)) {
            if (n.getChildren().size() > 0) {
                for (int i = 0; i < n.getChildren().size(); i++) {
                    fillDisplayList(n.getChildren().get(i), tmpStrList, endCondition, tempArrList);
                }
            }

            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (int i = 0; i < tempArrList.size(); i++) {
                sb.append(tempArrList.get(i).getKeyValueWithIndent()).append("\n");
            }
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
                sb.delete(sb.length() - 1, sb.length());
            }

            tmpStrList.add(sb);
        } else {
            if (n.getChildren().size() > 0) {
                for (int i = 0; i < n.getChildren().size(); i++) {
                    fillDisplayList(n.getChildren().get(i), tmpStrList, endCondition, tempArrList);
                }
            }
        }
    }

    //*********************************************************************************************

    private static void fillDisplayDict(JSONObject inputJson,
                                        ArrayList<SpannableStringBuilder> displayList,
                                        Map<String, List<SpannableStringBuilder>> mDisplayDict) throws JSONException {

        String barcode = inputJson.get("barcode").toString();
        String IDNumber = inputJson.get("ID").toString();

        String label = barcode + "-" + IDNumber;

        mDisplayDict.put(label, displayList);
    }
}