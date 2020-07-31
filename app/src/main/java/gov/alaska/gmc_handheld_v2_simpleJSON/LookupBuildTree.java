package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
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

    public static Map<String, List<SpannableStringBuilder>> setupDisplay(JSONArray inputJson) throws JSONException {

        ArrayList<SpannableStringBuilder> displayList = new ArrayList<>();  //used for the app display (expandable list)
        Map<String, List<SpannableStringBuilder>> displayDict = new HashMap<>(); //used for the app display (expandable list)


        try {
            JSONObject jo = (JSONObject) inputJson.get(0);
//            System.out.println(jo);
            InventoryObject root = parseTree(null, null, jo);


            setInventoryObject(root);
//
            sortInventoryObjects(root);


            Collections.sort(root.getChildren(), new SortInventoryObjectList());  //sorts externally


            for (InventoryObject n : root.getChildren()){
                System.out.println(printInventoryObject(n, 0));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }



//        processForDisplay(invetoryRoot, displayList, invetoryRoot);
//        fillDisplayDict(inputJson, displayList, displayDict);

        return displayDict;
    }


//*********************************************************************************************

    public static InventoryObject parseTree(Object parent, String name, Object o) throws Exception {

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
// This is private because it will only ever be called by parseTree
    private static InventoryObject handleObject(Object parent, String name, JSONObject o) throws Exception {
        // You can't switch on a null string, but the JSON structure allows
        // for nameless objects and arrays, thus this test

        if (name != null) {
            switch (name) {
                // Explicitly ignore these
                case "elevationUnit":
                case "intervalUnit":
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

    // This is private because it will only ever be called by parseTree
    private static InventoryObject handleArray(Object parent, String name, JSONArray a) throws Exception {
        // You can't switch on a null string, but the JSON structure allows
        // for nameless objects and arrays, thus this test
        if (name != null) {
            switch (name) {
                case "keywords":
                    // Use braces here to isolate the scope of each case statement,
                    // so you can reuse variable names
                {
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

    // This is private because it will only ever be called by parseTree
    private static InventoryObject handleSimple(Object parent, String name, Object o) throws JSONException {
        // Simple values should always have a name
        if (name == null) {
            return null;
        }

        switch (name) {
            case "abbr":
                return null;
            case "measuredDepth":
                // Use braces here to isolate the scope of each case statement,
                // so you can reuse variable names
            {
                if (o instanceof Double && parent instanceof JSONObject) {
                    Double value = (Double) o;
                    JSONObject pjo = (JSONObject) parent;
                    if (pjo.has("unit") && pjo.get("unit") instanceof JSONObject) {
                        JSONObject unit = (JSONObject) pjo.get("unit");
                        if (unit.has("abbr") && unit.get("abbr") instanceof String) {
                            String abbr = (String) unit.get("abbr");
                            return new InventoryObject(
                                    name, value.toString() + abbr
                            );
                        }
                    }
                }
            }

            default:
                return new InventoryObject(name, o);
        }
    }


    public static void setInventoryObject(InventoryObject o) {

        if (o.getName() != null) {
            switch (o.getName()) {

//                case "abbr":
//                    break;
                case "altNames":
                    o.setName("Alternative Names");
                    break;
                case "barcode":
                    o.setName("Barcode");
                    break;
                case "boreholes":
                    o.setName("Boreholes");
                    break;
                case "boxNumber":
                    o.setName("Box Number");
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
                    break;
                case "completionStatus":
                    o.setName("Completion Status");
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
                    break;
                case "ID":
                    o.setDisplayWeight(1000);
                    break;
                case "intervalBottom":
                    o.setName("Interval Bottom");
                    break;
                case "intervalTop":
                    o.setName("Interval Top");
                    break;
                case "keywords":
                    o.setName("Keywords");
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
                    break;

                case "outcrops":
                    o.setName("Outcrops");
                    break;
                case "permitStatus":
                    o.setName("Permit Status");
                    break;
                case "prospect":
                    o.setName("Prospect");
                    break;
                case "remark":
                case "remarks":
                    o.setName("Remarks");
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

                case "type":
                    o.setName("Type");
                    break;

                case "wells":
                    o.setName("Wells");
                    o.setDisplayWeight(100);
                    break;
                case "APINumber":
                    o.setName("API Number");
                    o.setDisplayWeight(95);
                    break;
                case "verticalDepth":
                    o.setName("Vertical Depth");
                    o.setDisplayWeight(80);
                    break;
                case "measuredDepth":
                    o.setName("Measured Depth");
                    o.setDisplayWeight(75);
                    break;
                case "wellNumber":
                    o.setName("Well Number");
                    o.setDisplayWeight(94);
                    break;
                case "operators":
                    o.setName("Operators");
                    o.setDisplayWeight(50);
                    break;
                case "spudDate":
                    o.setName("Spud Date");
                    o.setDisplayWeight(60);

                    break;
                default:

            }
        }
        if (!o.getChildren().isEmpty()) {
            for (InventoryObject c : o.getChildren()) {
                setInventoryObject(c);
            }
        }
    }


    public static void sortInventoryObjects(InventoryObject o) {

        if (!o.getChildren().isEmpty()) {
            Collections.sort(o.getChildren(), new SortInventoryObjectList());
            for (InventoryObject c : o.getChildren()) {
                sortInventoryObjects(c);
            }
        }
    }


    //*********************************************************************************************
    // Handy debug methods are exposed as public
    public static String printInventoryObject(InventoryObject o, int depth) throws Exception {


        StringBuilder sb = new StringBuilder();
        // Handle indentation
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
        sb.append(o.getName());
        sb.append(" = ");
        if (!o.getChildren().isEmpty()) {
            sb.append("[\n");
            for (InventoryObject c : o.getChildren()) {
                sb.append(printInventoryObject(c, depth + 1));
            }
            // Terminating brace needs to be indented at the same level
            for (int i = 0; i < depth; i++) {
                sb.append("    ");
            }
            sb.append("]");
        } else {
            sb.append(o.getValue());
        }
        sb.append("\n");
        return sb.toString();
    }

    ////*********************************************************************************************

//    private static void setDisplayWeight(InventoryObject o) {
//
//        switch (o.getName()) {
//
//            case "abbr":
//
//            case "altNames":
//
//            case "APINumber":
//
//            case "barcode":
//
//            case "boreholes":
//
//            case "boxNumber":
//
//            case "class":
//
//            case "collection":
//
//            case "completionDate":
//
//            case "completionStatus":
//
//            case "containerPath":
//
//            case "coreNumber":
//
//            case "current":
//
//            case "description":
//
//            case "elevation":
//
//            case "elevationUnit":
//
//            case "federal":
//
//            case "ID":
//
//            case "intervalBottom":
//
//            case "intervalTop":
//
//            case "intervalUnit":
//
//            case "keywords":
//
//            case "measuredDepth":
//
//            case "measuredDepthUnit":
//
//            case "name":
//                ;
//            case "number":
//
//            case "onshore":
//
//            case "operators":
//
//            case "outcrops":
//
//            case "permitStatus":
//
//            case "prospect":
//
//            case "remark":
//            case "remarks":
//
//            case "sampleNumber":
//
//            case "setNumber":
//
//            case "shotline":
//
//            case "shotpoints":
//
//            case "spudDate":
//
//            case "type":
//
//            case "unit":
//
//
//            case "verticalDepth":
//
//            case "wellNumber":
//
//            case "wells":
//
//            default:
//
//        }
//    }
//*********************************************************************************************

//    private static void processForDisplay(InventoryObject
//                                                  mRoot, ArrayList<SpannableStringBuilder> displayList, InventoryObject inventoryRoot) {
//
//        ArrayList<String> keyList = new ArrayList<>();  //list of all keys --> used with spannableStringBuilder to make all keys bold
//
//        Collections.sort(mRoot.getChildren(), new SortInventoryObjectList()); //sort externally
//
//        for (InventoryObject n : mRoot.getChildren()) {
//
//            combineKeyValueStr(n, 0);  //contains the indent + key + value as a string
//
//            if (n.getChildren().size() == 0) {
//                displayList.add(new SpannableStringBuilder(n.getKeyValueWithIndent()));
//
//            } else {
//                if (n.getParent() == inventoryRoot) {  //Groups children under the parent
//
//                    Collections.sort(n.getChildren(), new SortInventoryObjectList()); //sorts internally
//
//                    ArrayList<InventoryObject> tempArrList = new ArrayList<>();
//                    tempArrList.add(n);
//
//                    for (InventoryObject nChild : n.getChildren()) {
//                        String endCondition = n.getChildren().get(n.getChildren().size() - 1).getName();
//                        fillDisplayList(nChild, displayList, endCondition, tempArrList);
//                    }
//                }
//            }
//
//            keyList.add(n.getName());
//            if (n.getChildren().size() > 0) {
//                getDescendants(n, keyList);
//            }
//        }
//
//
//        for (SpannableStringBuilder s : displayList) {
//            for (String k : keyList) {
//                if (s.toString().contains(k)) {
//                    int index = 0;
//                    while (index != -1) {
//                        index = s.toString().indexOf(k, index);
//                        if (index != -1) {
//                            s.setSpan(new StyleSpan(BOLD), index,
//                                    index + k.length() + 1, SPAN_EXCLUSIVE_EXCLUSIVE);
//                            index++;
//                        }
//                    }
//                }
//            }
//        }
//    }
//
////*********************************************************************************************
//
//    private static void getDescendants(InventoryObject parent, ArrayList<String> keyList) {
//        if (parent.getChildren().size() == 0) {
//            keyList.add(parent.getName());
//        } else {
//            keyList.add(parent.getName());
//            for (InventoryObject nChild : parent.getChildren()) {
//                getDescendants(nChild, keyList);
//            }
//        }
//    }
//
////*********************************************************************************************
//
//    public static void combineKeyValueStr(InventoryObject n, int indent) {  //needed because of recursion
//
//        if (n.getChildren().size() == 0) {
//            n.setKeyValueWithIndent((printIndent(indent) + n.getName()) + ": " + n.getValue().toString());
//        } else {
//            n.setKeyValueWithIndent((printIndent(indent) + n.getName() + ":"));
//
//            for (InventoryObject nChild : n.getChildren()) {
//                combineKeyValueStr(nChild, indent + 1);
//            }
//        }
//    }
//
////*********************************************************************************************
//
//    private static String printIndent(int indent) {
//
//        StringBuilder indentStr = new StringBuilder();
//        for (int i = 0; i < indent; i++) {
//            indentStr.append("    ");
//        }
//        return indentStr.toString();
//    }
//
////*********************************************************************************************
//
//    private static void fillDisplayList(InventoryObject
//                                                n, ArrayList<SpannableStringBuilder> tmpStrList, String
//                                                endCondition, ArrayList<InventoryObject> tempArrList) {
//
//        tempArrList.add(n);
//
//        if (n.getName().equals(endCondition)) {
//            if (n.getChildren().size() > 0) {
//                for (int i = 0; i < n.getChildren().size(); i++) {
//                    fillDisplayList(n.getChildren().get(i), tmpStrList, endCondition, tempArrList);
//                }
//            }
//
//            SpannableStringBuilder sb = new SpannableStringBuilder();
//            for (int i = 0; i < tempArrList.size(); i++) {
//                sb.append(tempArrList.get(i).getKeyValueWithIndent()).append("\n");
//            }
//            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
//                sb.delete(sb.length() - 1, sb.length());
//            }
//
//            tmpStrList.add(sb);
//        } else {
//            if (n.getChildren().size() > 0) {
//                for (int i = 0; i < n.getChildren().size(); i++) {
//                    fillDisplayList(n.getChildren().get(i), tmpStrList, endCondition, tempArrList);
//                }
//            }
//        }
//    }

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