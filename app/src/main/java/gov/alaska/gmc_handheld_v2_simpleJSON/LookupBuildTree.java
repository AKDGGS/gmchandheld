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

        InventoryObject root = new InventoryObject("myRoot", null);

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

            if (!(val instanceof JSONArray) && !(val instanceof JSONObject))
                if (parent != null)
                    modifyNodes(keyObject, val, parent, mInputJson);

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
                invObj = new InventoryObject("Abbr", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "APINumber":
                invObj = new InventoryObject("API Number", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "barcode":
                invObj = new InventoryObject("Barcode", val, parent, 1002);
                invObj.addChildToParent(parent);
                return invObj;
            case "boreholes":
                invObj = new InventoryObject("Boreholes", val, parent, 100);
                invObj.addChildToParent(parent);
                return invObj;
            case "boxNumber":
                invObj = new InventoryObject("Box Number", val, parent, 1000);
                invObj.addChildToParent(parent);
                return invObj;
            case "class":
                invObj = new InventoryObject("Class", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "collection":
                invObj = new InventoryObject("Collection", val, parent, 900);
                invObj.addChildToParent(parent);
                return invObj;
            case "completionDate":
                invObj = new InventoryObject("Completion Date", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "completionStatus":
                invObj = new InventoryObject("Completion Status", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "containerPath":
                invObj = new InventoryObject("Container Path", val, parent, 1001);
                invObj.addChildToParent(parent);
                return invObj;
            case "coreNumber":
                invObj = new InventoryObject("Core Number", val, parent, 1003);
                invObj.addChildToParent(parent);
                return invObj;
            case "current":
                invObj = new InventoryObject("Current", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "elevation":
                invObj = new InventoryObject("Elevation", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "elevationUnit":
                invObj = new InventoryObject("Elevation Unit", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "federal":
                invObj = new InventoryObject("Federal", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "ID":
                if ("myRoot".equals(parent.getKey())) {
                    invObj = new InventoryObject("ID", val, parent, 10000);
                } else {
                    invObj = new InventoryObject("ID", val, parent);
                    invObj.setDisplayWeight(parent.getDisplayWeight());
                }
                invObj.addChildToParent(parent);
                return invObj;
            case "intervalBottom":
                invObj = new InventoryObject("Interval Bottom", val, parent, 1005);

                if (mInputJson.has("intervalUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("intervalUnit");
                    if (tempObj.has("abbr")) {
                        unitAbbr = mInputJson.getJSONObject("intervalUnit").get("abbr").toString();
                        invObj.setValue(invObj.getValue() + " " + unitAbbr);
                    }
                } else if (unitAbbr == null)
                    invObj.setValue(invObj.getValue());
                invObj.addChildToParent(parent);
                return invObj;
            case "intervalTop":
                invObj = new InventoryObject("Interval Top", val, parent, 1005);
                if (mInputJson.has("intervalUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("intervalUnit");
                    if (tempObj.has("abbr")) {
                        unitAbbr = mInputJson.getJSONObject("intervalUnit").get("abbr").toString();
                        invObj.setValue(invObj.getValue() + " " + unitAbbr);
                    }
                } else if (unitAbbr == null)
                    invObj.setValue(invObj.getValue());

                invObj.addChildToParent(parent);
                return invObj;
            case "intervalUnit":
                return null;
            case "keywords":
                invObj = new InventoryObject("Keywords", val, parent, 950);
                invObj.addChildToParent(parent);
                return invObj;
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

                if (unitAbbr != null)
                    invObj.setValue(invObj.getValue() + " " + unitAbbr);

                invObj.addChildToParent(parent);
                return invObj;

            case "measuredDepthUnit":
                return null;
            case "name":
                invObj = new InventoryObject("Name", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "number":
                invObj = new InventoryObject("Number", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "onshore":
                invObj = new InventoryObject("Onshore", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "operators":
                invObj = new InventoryObject("Operators", val, parent, parent.getDisplayWeight() - 10);
                invObj.addChildToParent(parent);
                return invObj;
            case "outcrops":
                invObj = new InventoryObject("Outcrops", val, parent, 100);
                invObj.addChildToParent(parent);
                return invObj;
            case "permitStatus":
                invObj = new InventoryObject("Permit Status", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "prospect":
                invObj = new InventoryObject("Prospect", val, parent);
                invObj.addChildToParent(parent);
                return new InventoryObject("Prospect", val, parent);
            case "remark":
            case "remarks":
                if ("myRoot".equals(parent.getKey())) {
                    invObj = new InventoryObject("Remark", val, parent, 950);
                    invObj.addChildToParent(parent);
                    return invObj;
                } else {
                    invObj = new InventoryObject("Remark", val, parent);
                    invObj.addChildToParent(parent);
                }
            case "setNumber":
                invObj = new InventoryObject("Set Number", val, parent, 950);
                invObj.addChildToParent(parent);
                return invObj;
            case "shotline":
                invObj = new InventoryObject("Shotline", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "shotpoints":
                invObj = new InventoryObject("Shotpoints", val, parent, 100);
                invObj.addChildToParent(parent);
                return invObj;
            case "spudDate":
                invObj = new InventoryObject("Spud Date", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "type":
                invObj = new InventoryObject("Type", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "unit":
                return null;

            case "verticalDepth":
                invObj = new InventoryObject("Vertical Depth", val, parent, parent.getDisplayWeight() - 1);

                if (mInputJson.has("unit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("unit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("unit").get("abbr").toString();
                }

                if (unitAbbr != null)
                    invObj.setValue(invObj.getValue() + " " + unitAbbr);
                invObj.addChildToParent(parent);
                return invObj;
            case "wellNumber":
                invObj = new InventoryObject("Well Number", val, parent);
                invObj.addChildToParent(parent);
                return invObj;
            case "wells":
                invObj = new InventoryObject("Wells", val, parent, 100);
                invObj.addChildToParent(parent);
                return invObj;
            default:
                invObj = new InventoryObject(key, val, parent);
                invObj.addChildToParent(parent);
                return invObj;
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
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n')
                sb.delete(sb.length() - 1, sb.length());

            tmpStrList.add(sb);
        } else {
            if (n.getChildren().size() > 0)
                for (int i = 0; i < n.getChildren().size(); i++) {
                    fillDisplayList(n.getChildren().get(i), tmpStrList, endCondition, tempArrList);
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