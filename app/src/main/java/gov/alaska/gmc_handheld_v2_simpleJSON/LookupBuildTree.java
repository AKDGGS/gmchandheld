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

    public static Map<String, List<SpannableStringBuilder>> setupDisplay(JSONArray inputJson) {

        Map<String, List<SpannableStringBuilder>> displayDict = new HashMap<>(); //used for the app display (expandable list)


        try {


                for (int i = 0; i < inputJson.length(); i++) {
                    ArrayList<SpannableStringBuilder> displayList = new ArrayList<>();  //used for the app display (expandable list)
                    Map<String, List<SpannableStringBuilder>> displayDictTemp = new HashMap<>();
                    JSONObject inputJsonObject = (JSONObject) inputJson.get(i);
                    InventoryObject root = parseTree(null, null, inputJsonObject);
                    processForDisplay(root, displayList);
                    displayDict.putAll(fillDisplayDict(inputJsonObject, displayList, displayDictTemp));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return displayDict;
    }

//*********************************************************************************************

    private static InventoryObject parseTree(Object parent, String name, Object o) throws Exception {

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

    private static InventoryObject handleObject(Object parent, String name, JSONObject o) throws Exception {

        if (name != null) {
            switch (name) {
                // Explicitly ignore these
                case "elevationUnit":
                case "intervalUnit":
                case "measuredDepthUnit":
                case "unit":
                    return null;
            }
        }

        InventoryObject io = new InventoryObject(name);

        for (Iterator<String> it = o.keys(); it.hasNext(); ) {
            String key = it.next();
            io.addChild(parseTree(o, key, o.get(key)));
        }
        return io;
    }


//*********************************************************************************************

    private static InventoryObject handleArray(Object parent, String name, JSONArray a) throws Exception {
        if (name != null) {
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
                    return new InventoryObject(name, sb.toString());
                }
            }
        }

        InventoryObject io = new InventoryObject(name);

        for (int i = 0; i < a.length(); i++) {
            io.addChild(parseTree(a, name, a.get(i)));
        }
        return io;
    }

//*********************************************************************************************

    private static InventoryObject handleSimple(Object parent, String name, Object o) throws JSONException {
        // Simple values should always have a name
        if (name == null) {
            return null;
        }

        switch (name) {
            case "abbr":
                return null;
            case "elevation":
                return getUnit(parent, name, o, "elevationUnit");
            case "intervalBottom":
                return getUnit(parent, name, o, "intervalUnit");
            case "intervalTop":
                return getUnit(parent, name, o, "intervalUnit");
            case "measuredDepth":
                JSONObject pjo = (JSONObject) parent;
                if (pjo.has("measuredDepthUnit")) {
                    return getUnit(parent, name, o, "measuredDepthUnit");
                } else {
                    return getUnit(parent, name, o, "unit"); //I think I ran across one test case where the measuredDepthUnit was missing, but unit was present.
                }
            case "verticalDepth":
                return getUnit(parent, name, o, "unit");

            default:
                return new InventoryObject(name, o);
        }
    }

//*********************************************************************************************

    // helper function that is used to append the abbr to depths.
    private static InventoryObject getUnit(Object parent, String name, Object o, String nameOfUnit) throws JSONException {
        if (o instanceof Double && parent instanceof JSONObject) {
            Double value = (Double) o;
            JSONObject pjo = (JSONObject) parent;
            if (pjo.has(nameOfUnit) && pjo.get(nameOfUnit) instanceof JSONObject) {
                JSONObject unit = (JSONObject) pjo.get(nameOfUnit);
                if (unit.has("abbr") && unit.get("abbr") instanceof String) {
                    String abbr = (String) unit.get("abbr");
                    return new InventoryObject(
                            name, value.toString() + " " + abbr
                    );
                }
            }
        }

        return new InventoryObject(name, o.toString());

    }

//*********************************************************************************************

    private static void setInventoryObjectKeyOrValues(InventoryObject o) {
        if (o.getName() != null) {
            switch (o.getName()) {
                // Higher the displayWeight, the higher a priority an key has.
                // Items are sorted internally first, and the externally in processForDisplay()

                case "altNames":
                    o.setName("Alternative Names");
                    break;
                case "APINumber":
                    o.setName("API Number");
                    o.setDisplayWeight(95);
                    break;
                case "barcode":
                    o.setName("Barcode");
                    o.setDisplayWeight(1000);
                    break;
                case "boreholes":
                    o.setName("Boreholes");
                    break;
                case "boxNumber":
                    o.setName("Box Number");
                    o.setDisplayWeight(950);
                    break;
                case "class":
                    o.setName("Class");
                    break;
                case "collection":
                    o.setName("Collection");
                    o.setDisplayWeight(500);
                    break;
                case "completionDate":
                    o.setName("Completion Date");
                    o.setDisplayWeight(69);
                    break;
                case "completionStatus":
                    o.setName("Completion Status");
                    o.setDisplayWeight(69);
                    break;
                case "containerPath":
                    o.setName("Container Path");
                    o.setDisplayWeight(1000);
                    break;
                case "coreNumber":
                    o.setName("Core Number");
                    break;
                case "current":
                    o.setName("Current");
                    break;
                case "description":
                    o.setName("Description");
                    break;
                case "elevation":
                    o.setName("Elevation");
                    break;
                case "federal":
                    o.setName("Federal");
                    o.setDisplayWeight(70);
                    break;
                case "ID":
                    o.setDisplayWeight(1000);
                    break;
                case "intervalBottom":
                    o.setName("Interval Bottom");
                    o.setDisplayWeight(900);
                    break;
                case "intervalTop":
                    o.setName("Interval Top");
                    o.setDisplayWeight(900);
                    break;
                case "keywords":
                    o.setName("Keywords");
                    o.setDisplayWeight(600);
                    break;
                case "measuredDepth":
                    o.setName("Measured Depth");
                    o.setDisplayWeight(75);
                    break;
                case "name":
                    o.setName("Name");
                    o.setDisplayWeight(100);
                    break;
                case "number":
                    o.setName("Number");
                    break;
                case "onshore":
                    o.setName("Onshore");
                    o.setDisplayWeight(70);
                    break;
                case "operators":
                    o.setName("Operators");
                    o.setDisplayWeight(50);
                    break;
                case "outcrops":
                    o.setName("Outcrops");
                    break;
                case "permitStatus":
                    o.setName("Permit Status");
                    o.setDisplayWeight(70);
                    break;
                case "prospect":
                    o.setName("Prospect");
                    break;
                case "remark":
                    o.setName("Remark");
                    o.setDisplayWeight(900);
                    break;
                case "sampleNumber":
                    o.setName("Sample Number");
                    break;
                case "setNumber":
                    o.setName("Set Number");
                    break;
                case "shotline":
                    o.setName("Shotline");
                    break;
                case "shotpoints":
                    o.setName("Shotpoints");
                    break;
                case "spudDate":
                    o.setName("Spud Date");
                    o.setDisplayWeight(60);
                    break;
                case "type":
                    o.setName("Type");
                    break;
                case "verticalDepth":
                    o.setName("Vertical Depth");
                    o.setDisplayWeight(80);
                    break;
                case "wellNumber":
                    o.setName("Well Number");
                    o.setDisplayWeight(94);
                    break;
                case "wells":
                    o.setName("Wells");
                    o.setDisplayWeight(100);
                    break;
                default:
                    o.setDisplayWeight(0);
            }
        }

        // Checks all of the children of the root.
        if (!o.getChildren().isEmpty()) {
            for (InventoryObject c : o.getChildren()) {
                setInventoryObjectKeyOrValues(c);
            }
        }
    }

    //*********************************************************************************************
    public static void sortInventoryObjectsInternally(InventoryObject o) {

        if (!o.getChildren().isEmpty()) {
            Collections.sort(o.getChildren(), new SortInventoryObjectList());
            for (InventoryObject c : o.getChildren()) {
                sortInventoryObjectsInternally(c);
            }
        }
    }

    //*********************************************************************************************

    private static StringBuilder getStringForDisplay(InventoryObject n) throws Exception {

        StringBuilder sb = new StringBuilder();
        if (n.getChildren().size() > 0) {
            for (InventoryObject nChild : n.getChildren()) {
                // Checks if the array is an array of the parent type.
                // Is the array an array of one well or of many wells?
                String prefix = "";
                if (nChild.getName().equals(n.getName())) {
                    sb.append(n.getName()).append("\n");
                    for (InventoryObject nGrandChild : nChild.getChildren()) {
                        sb.append(printInventoryObject(nGrandChild, 1));  //depth is 1 since we know all of these are children.
                    }

                    if(n.getChildren().size() > 1) {
                        sb.append("\n");
                    }else if (sb.length() > 1) {
                        sb.setLength(sb.length() - 1);
                    }

                } else {
                    if (nChild.getName().equals(n.getChildren().get(0).getName())) {
                        sb.append(n.getName()).append("\n");
                    }
                    sb.append(printInventoryObject(nChild, 1)); //depth is 1 since we know all of these are children
                }
            }

        } else {
            sb.append(printInventoryObject(n, 0));
        }

        //removes the final newline character
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        return (sb);

    }


//*********************************************************************************************

    //Used in getStringForDisplay
    private static String printInventoryObject(InventoryObject o, int depth) {

        StringBuilder sb = new StringBuilder();
        // Handle indentation
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
        sb.append(o.getName());
        sb.append(" = ");
        if (!o.getChildren().isEmpty()) {
            sb.append("\n");
            for (InventoryObject c : o.getChildren()) {
                sb.append(printInventoryObject(c, depth + 1));
            }
        } else {
            sb.append(o.getValue()).append("\n");
        }
        return sb.toString();
    }


//*********************************************************************************************

    private static void processForDisplay(InventoryObject mRoot, ArrayList<SpannableStringBuilder> displayList) throws Exception {

        ArrayList<String> keyList = new ArrayList<>();  //list of all keys --> used with spannableStringBuilder to make all keys bold

        setInventoryObjectKeyOrValues(mRoot);
        sortInventoryObjectsInternally(mRoot);

        Collections.sort(mRoot.getChildren(), new SortInventoryObjectList()); //sort externally

        for (InventoryObject n : mRoot.getChildren()) {
            displayList.add(new SpannableStringBuilder(getStringForDisplay(n)));

            keyList.add(n.getName());
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

    //Helper method used to get descendant keys to set them all to bold in the SpannableStringBuilder
    private static void getDescendants(InventoryObject n, ArrayList<String> keyList) {
        if (n.getChildren().size() == 0) {
            keyList.add(n.getName());
        } else {
            keyList.add(n.getName());
            for (InventoryObject nChild : n.getChildren()) {
                getDescendants(nChild, keyList);
            }
        }
    }


    //*********************************************************************************************

    private static Map<String, List<SpannableStringBuilder>> fillDisplayDict(JSONObject inputJson,
                                        ArrayList<SpannableStringBuilder> displayList,
                                        Map<String, List<SpannableStringBuilder>> mDisplayDict) throws JSONException {

        String barcode = inputJson.get("barcode").toString();
        String IDNumber = inputJson.get("ID").toString();

        String label = barcode + "-" + IDNumber;

        mDisplayDict.put(label, displayList);
        return mDisplayDict;
    }
}