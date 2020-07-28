package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.alaska.gmc_handheld_v2_simpleJSON.comparators.SortInventoryObjectList;

import static android.graphics.Typeface.BOLD;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class LookupBuildTree {

    public static int count = 0;  //used to display the number of containers in a container of containers.

    public static Map<String, List<SpannableStringBuilder>> setupDisplay(JSONObject inputJson) throws JSONException {

        Map<String, List<SpannableStringBuilder>> displayDict = new HashMap<>();

        InventoryObject root = new InventoryObject(null, null);
        createNodes(inputJson, root);


//        buildDisplayGroups(inventoryObjectList);
//        fillDisplayDict(inventoryObjectList, displayDict);

        return displayDict;
    }

    //*********************************************************************************************

    private static void fillDisplayDict
            (ArrayList<InventoryObject> mInventoryObjectList, Map<String, List<SpannableStringBuilder>> mDisplayDict) {
        ArrayList<SpannableStringBuilder> displayList = new ArrayList<>();  //list of parent nodes with the value equaling all of their children (key + value)

        String barcode = "";
        String IDNumber = "";

        for (InventoryObject n : mInventoryObjectList) {
            if (n.getParent() == null && n.getDisplay()) {
                if (n.getGroupSSB().length() > 0 && n.getGroupSSB().charAt(n.getGroupSSB().length() - 1) == '\n')
                    n.getGroupSSB().delete(n.getGroupSSB().length() - 1, n.getGroupSSB().length());
                displayList.add(n.getGroupSSB());
            }

            if (n.getKey().equals("Barcode")) {
                barcode = n.getValue().toString();
            }
            if (n.getParent() == null && "ID".equals(n.getKey())) {
                IDNumber = n.getValue().toString();
            }

        }

        String label = barcode + "-" + IDNumber;

        mDisplayDict.put(label, displayList);

    }

    //*********************************************************************************************

    private static void buildDisplayGroups(ArrayList<InventoryObject> mInventoryObjectList) {

//        Collections.sort(mInventoryObjectList, new SortInventoryObjectList()); //sorts nodes internally
        Set<Integer> groups = new HashSet<>();
        String elevationAbbr = "";
        String intervalAbbr = "";
        String unitAbbr = "";

//
//        for (InventoryObject n : mInventoryObjectList) {
//            if (n.getParent() != null && n.getParent().getKey().equals("Unit") && n.getKey().equals("Abbr")) {
//                unitAbbr = n.getValue().toString();
//            }
//            if (n.getParent() != null && n.getParent().getKey().equals("Interval Unit") && n.getKey().equals("Abbr")) {
//                intervalAbbr = n.getValue().toString();
//            }
//
//            if (n.getParent() != null && n.getParent().getKey().equals("Elevation Unit") && n.getKey().equals("Abbr")) {
//                elevationAbbr = n.getValue().toString();
//            }
//        }


//        for (int i = 0; i < mInventoryObjectList.size(); i++) {
//
//            switch (mInventoryObjectList.get(i).getKey()) {
//                case "Vertical Depth":
//                case "Measured Depth":
//                    mInventoryObjectList.get(i).setValue(mInventoryObjectList.get(i).getValue() + " " + unitAbbr);
//                    System.out.println(mInventoryObjectList.get(i).getValue());
//                    break;
//                case "Interval Top":
//                case "Interval Bottom":
//                    mInventoryObjectList.get(i).setValue(mInventoryObjectList.get(i).getValue() + " " + intervalAbbr);
//                    break;
//                case "Elevation":
//                    mInventoryObjectList.get(i).setValue(mInventoryObjectList.get(i).getValue() + " " + elevationAbbr);
//                    break;
//            }
//        }

        for (InventoryObject n : mInventoryObjectList) {
//            System.out.println(unitAbbr);
            switch (n.getKey()) {
                case "Vertical Depth":
                case "Measured Depth":
                    n.setValue(n.getValue() + " " + unitAbbr);
//                    System.out.println(n.getValue());
                    break;
                case "Interval Top":
                case "Interval Bottom":
                    n.setValue(n.getValue() + " " + intervalAbbr);
                    break;
                case "Elevation":
                    n.setValue(n.getValue() + " " + elevationAbbr);
                    break;
            }
            if (n.getParent() == null) {
                combineKeyValueStr(n, 0);
            }
            if (n.getParent() == null) {
                groups.add(n.getGroupID());
            }
        }

        for (int group : groups) {
            SpannableStringBuilder spannableStr = new SpannableStringBuilder();
            for (InventoryObject n : mInventoryObjectList) {
                if (n.getDisplay()) {
                    if (n.getGroupID() == group) {
                        if (n.getValue() instanceof JSONArray) {
                            spannableStr.append(n.getKey()).append("\n");
                        } else if (!(n.getValue() instanceof JSONArray))
                            spannableStr.append(n.getKeyValueWithIndent()).append("\n");
                    }

                    if (n.getParent() == null && n.getGroupID() == group) {
                        n.setGroupSSB(spannableStr);
                    }
                }
            }
        }

        ArrayList<String> keyList = new ArrayList<>();
        for (InventoryObject n : mInventoryObjectList) {
            keyList.add(n.getKey());
        }

        for (InventoryObject n : mInventoryObjectList) {
            if (n.getGroupSSB() != null) {
                for (String s : keyList) {
                    if (n.getGroupSSB().toString().contains(s)) {
                        int index = 0;
                        while (index != -1) {
                            index = n.getGroupSSB().toString().indexOf(s, index);
                            if (index != -1) {
                                n.getGroupSSB().setSpan(new StyleSpan(BOLD), index,
                                        index + s.length() + 1, SPAN_EXCLUSIVE_EXCLUSIVE);
                                index++;

                            }
                        }
                    }
                }
            }
        }
    }


//*********************************************************************************************

    private static void createNodes(JSONObject mInputJson, InventoryObject root)
            throws JSONException {

        for (Iterator<String> it = mInputJson.keys(); it.hasNext(); ) {
            String keyObject = it.next();

            InventoryObject parent = root;
            Object val = mInputJson.get(keyObject);

            modifyNodes(keyObject, val, parent, mInputJson);

            switch (val.getClass().getSimpleName()) {
                case "JSONObject":
                    parent = modifyNodes(keyObject, val, parent, mInputJson);
                    createNodes((JSONObject) val, parent);
                    break;
                case "JSONArray":
                    if (!(val).toString().contains(":")) {  //checks if the array contains objects --> used for keywords
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

    private static void processForDisplay(InventoryObject mRoot, ArrayList<SpannableStringBuilder> displayList) {
        ArrayList<String> keyList = new ArrayList<>();

        for (InventoryObject n : mRoot.getChildren()) {
            combineKeyValueStr(n, 0);

            if (n.getChildren().size() > 0) {
                Collections.sort(n.getChildren(), new SortInventoryObjectList());  //sort items internally
            }
        }

        for (InventoryObject n : mRoot.getChildren()) {
            if (n.getChildren().size() == 0) {
                displayList.add(new SpannableStringBuilder(n.getKeyValueWithIndent()));
            } else {
                if (n.getParent().getKey().equals("root")) {
                    ArrayList<InventoryObject> tempArrList = new ArrayList<>();
                    tempArrList.add(n);
                    for (InventoryObject nChild : n.getChildren()) {
                        String endCondition = n.getChildren().get(n.getChildren().size() - 1).getKey();
//                        fillDisplayList(nChild, displayList, endCondition, tempArrList);
                    }
                }
            }
            Collections.sort(n.getChildren(), new SortInventoryObjectList()); //sort items externally.
        }

        for (InventoryObject n : mRoot.getChildren()) {
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

    private static void getDescendants(InventoryObject parent, ArrayList<String> keyList) {
        if (parent.getChildren().size() == 0) {
            keyList.add(parent.getKey());
        } else {
            keyList.add(parent.getKey());
            for (InventoryObject nChild : parent.getChildren())
                getDescendants(nChild, keyList);
        }
    }


//*********************************************************************************************

    //*********************************************************************************************

    private static String printIndent(int indent) {
        StringBuilder indentStr = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStr.append("    ");
        }
        return indentStr.toString();
    }

    //*********************************************************************************************

    public static void combineKeyValueStr(InventoryObject n, int indent) {  //needed because of recursion

        if (n.getKey().equals("Measured Depth"))
//            System.out.println(n.getKey() + " " + n.getValue());
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

    private static InventoryObject modifyNodes(String key, Object val, InventoryObject parent, JSONObject mInputJson) throws JSONException {
        InventoryObject temp;
        String unitAbbr = null;
        switch (key) {
            case "abbr":
                temp = new InventoryObject("Abbr", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "APINumber":
                temp = new InventoryObject("API Number", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "barcode":
                temp = new InventoryObject("Barcode", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "boreholes":
                temp = new InventoryObject("Boreholes", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "boxNumber":
                temp = new InventoryObject("Box Number", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "class":
                temp = new InventoryObject("Class", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "collection":
                temp = new InventoryObject("Collection", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "completionDate":
                temp = new InventoryObject("Completion Date", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "completionStatus":
                temp = new InventoryObject("Completion Status", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "containerPath":
                temp = new InventoryObject("Container Path", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "coreNumber":
                temp = new InventoryObject("Core Number", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "current":
                temp = new InventoryObject("Current", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "elevation":
                temp = new InventoryObject("Elevation", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "elevationUnit":
                temp = new InventoryObject("Elevation Unit", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "federal":
                temp = new InventoryObject("Federal", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "ID":
                temp = new InventoryObject("ID", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "intervalBottom":
                temp = new InventoryObject("Interval Bottom", val, parent);

                if (mInputJson.has("intervalUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("intervalUnit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("intervalUnit").get("abbr").toString();
                }

                if (unitAbbr != null)
                    temp.setValue(temp.getValue() + " " + unitAbbr);
                temp.setParentChildRelationship(parent);
                return temp;
//                if (mRootNode.has("intervalUnit"))
//                    return new InventoryObject("Interval Bottom", invObj.getValue() + " " + mRootNode.get("intervalUnit").get("abbr").asText(), invObj.getParent(), 1005);
//                else
//                    return new InventoryObject("Interval Bottom", invObj.getValue(), invObj.getParent(), 1005);
            case "intervalTop":
                temp = new InventoryObject("Interval Top", val, parent);
                if (mInputJson.has("intervalUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("intervalUnit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("intervalUnit").get("abbr").toString();
                }

                if (unitAbbr != null)
                    temp.setValue(temp.getValue() + " " + unitAbbr);

                temp.setParentChildRelationship(parent);
                return temp;
            case "intervalUnit":
                return null;
            case "keywords":
                temp = new InventoryObject("Keywords", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "measuredDepth":
//                if (mRootNode.has("unit"))
//                    return new InventoryObject("Measured Depth", invObj.getValue() + " " + mRootNode.get("unit").get("abbr").asText(), invObj.getParent(), 1005);
//                else if (mRootNode.has("measuredDepthUnit"))
//                    return new InventoryObject("Measured Depth", invObj.getValue() + " " + mRootNode.get("measuredDepthUnit").get("abbr").asText(), invObj.getParent(), 1005);
//                else
                temp = new InventoryObject("Measured Depth", val, parent);
                if (mInputJson.has("measuredUnit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("measuredUnit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("measuredUnit").get("abbr").toString();
                }else if(mInputJson.has("unit")){
                    JSONObject tempObj = mInputJson.getJSONObject("unit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("unit").get("abbr").toString();
                }

                if (unitAbbr != null)
                    temp.setValue(temp.getValue() + " " + unitAbbr);

                temp.setParentChildRelationship(parent);
                return temp;

            case "measuredDepthUnit":
                return null;
            case "name":
                temp = new InventoryObject("Name", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "number":
                temp = new InventoryObject("Number", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "onshore":
                temp = new InventoryObject("Onshore", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "operators":
                temp = new InventoryObject("Operators", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "outcrops":
                temp = new InventoryObject("Outcrops", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "permitStatus":
                temp = new InventoryObject("Permit Status", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "prospect":
                temp = new InventoryObject("Prospect", val, parent);
                temp.setParentChildRelationship(parent);
                return new InventoryObject("Prospect", val, parent);
            case "remark":
            case "remarks":
//                if (invObj.getParent() != null)
//                    return new InventoryObject("Remark",  val, parent);
//                else
                temp = new InventoryObject("Remark", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "setNumber":
                temp = new InventoryObject("Set Number", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "shotline":
                temp = new InventoryObject("Shotline", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "shotpoints":
                temp = new InventoryObject("Shotpoints", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "spudDate":
                temp = new InventoryObject("Spud Date", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "type":
                temp = new InventoryObject("Type", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "unit":
                return null;

            case "verticalDepth":
//                invObj.setKey("Vertical Depth");
//                if (mRootNode.has("unit"))
//                    return new InventoryObject("Vertical Depth", invObj.getValue() + " " + mRootNode.get("unit").get("abbr").asText(), invObj.getParent(), invObj.getDisplayWeight());
//                else
                temp = new InventoryObject("Vertical Depth", val, parent);

                if (mInputJson.has("unit")) {
                    JSONObject tempObj = mInputJson.getJSONObject("unit");
                    if (tempObj.has("abbr"))
                        unitAbbr = mInputJson.getJSONObject("unit").get("abbr").toString();
                }

                if (unitAbbr != null)
                    temp.setValue(temp.getValue() + " " + unitAbbr);
                temp.setParentChildRelationship(parent);
                return temp;
            case "wellNumber":
                temp = new InventoryObject("Well Number", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            case "wells":
                temp = new InventoryObject("Shotpoints", val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
            default:
                temp = new InventoryObject(key, val, parent);
                temp.setParentChildRelationship(parent);
                return temp;
        }

    }
}