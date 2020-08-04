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
            System.out.println("Object: " + name + " " +  o);
            switch (name) {
                case "collection":
                    name = "Collection";
                    break;
                case "operators":
                    name = "Operators";
                    break;
                case "type":
                    name = "Type";
                    break;
                case "wells":
                    name = "Wells";
                    break;
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

        io.setDisplayWeight(1000);
        return io;
    }


//*********************************************************************************************

    private static InventoryObject handleArray(Object parent, String name, JSONArray a) throws Exception {
        if (name != null) {
            System.out.println("Array: " + name + " " + a);
            switch (name) {
                case "operators":
                    name = "Operators";
                    break;
                case "wells":
                    name = "Wells";
                    break;
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
                    return new InventoryObject("Keywords", sb.toString());
                }
            }
        }

        InventoryObject io = new InventoryObject(name);

        for (int i = 0; i < a.length(); i++) {
            io.addChild(parseTree(a, name, a.get(i)));
        }

        io.setDisplayWeight(1000);
        return io;
    }

//*********************************************************************************************

    private static InventoryObject handleSimple(Object parent, String name, Object o) throws JSONException {
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
            case "boreholes":
                return new InventoryObject("Boreholes", o);
            case "boxNumber":
                return new InventoryObject("Box Number", o, 950);
            case "class":
                return new InventoryObject("Class", o);
            case "collection":
                return new InventoryObject("Collection", o, 500);
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
            case "elevation":
//                    o.setName("Elevation");
                return new InventoryObject("Elevation", o, 900);
            case "federal":
                return new InventoryObject("Federal", o, 70);
            case "ID":
                return new InventoryObject("ID", o, 1000);
            case "intervalBottom":
                return new InventoryObject("Interval Bottom", o, 900);
            case "intervalTop":
                return new InventoryObject("Interval Top", o, 900);
            case "keywords":
                return new InventoryObject("Keywords", o, 600);
            case "measuredDepth":
//                    o.setName("Measured Depth");
//                    o.setDisplayWeight(75);
//                    JSONObject pjo = (JSONObject) parent;
//                    if (pjo.has("measuredDepthUnit")) {
//                        return getUnit(parent, name, o, "measuredDepthUnit");
//                    } else {
//                        return getUnit(parent, name, o, "unit"); //I think I ran across one test case where the measuredDepthUnit was missing, but unit was present.
//                    }
                return new InventoryObject("Measured Depth", o, 75);
            case "name":
                return new InventoryObject("Name", o, 100);
            case "number":
                return new InventoryObject("Number", o);
            case "onshore":
                return new InventoryObject("Onshore", o, 70);
            case "operators":
                return new InventoryObject("Operators", o, 50);
            case "outcrops":
                return new InventoryObject("Outcrops", o);
            case "permitStatus":
                return new InventoryObject("Permit Status", o, 70);
            case "prospect":
                return new InventoryObject("Prospect", o);
            case "remark":
                return new InventoryObject("Remark", o, 900);
            case "sampleNumber":
                return new InventoryObject("Sample Number", o);
            case "setNumber":
                return new InventoryObject("Set Number", o);
            case "shotline":
                return new InventoryObject("Shotline", o);
            case "shotpoints":
                return new InventoryObject("Shotpoints", o);
            case "spudDate":
                return new InventoryObject("Spud Date", o, 60);
            case "type":
                return new InventoryObject("Type", o);
            case "verticalDepth":
                return new InventoryObject("Vertical Depth", o, 80);
            case "wellNumber":
                return new InventoryObject("Well Number", o, 94);
            case "wells":
                return new InventoryObject("Wells", o, 100);
            default:
                return new InventoryObject(name, o);
        }

    }



//*********************************************************************************************

    // helper function that is used to append the abbr to depths.
    private static InventoryObject getUnit(Object parent, String name, Object o, String
            nameOfUnit) throws JSONException {
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

//    private static InventoryObject setInventoryObjectKeyOrValues(InventoryObject o) {
//
//
//        // Checks all of the children of the root.
//        if (!o.getChildren().isEmpty()) {
//            for (InventoryObject c : o.getChildren()) {
//                setInventoryObjectKeyOrValues(c);
//            }
//        }
//    }

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

                    if (n.getChildren().size() > 1) {
                        sb.append("\n");
                    } else if (sb.length() > 1) {
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

    private static void processForDisplay(InventoryObject
                                                  mRoot, ArrayList<SpannableStringBuilder> displayList) throws Exception {

        ArrayList<String> keyList = new ArrayList<>();  //list of all keys --> used with spannableStringBuilder to make all keys bold


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

    private static Map<String, List<SpannableStringBuilder>> fillDisplayDict(JSONObject
                                                                                     inputJson,
                                                                             ArrayList<SpannableStringBuilder> displayList,
                                                                             Map<String, List<SpannableStringBuilder>> mDisplayDict) throws JSONException {

        String barcode = inputJson.get("barcode").toString();
        String IDNumber = inputJson.get("ID").toString();

        String label = barcode + "-" + IDNumber;

        mDisplayDict.put(label, displayList);
        return mDisplayDict;
    }
}