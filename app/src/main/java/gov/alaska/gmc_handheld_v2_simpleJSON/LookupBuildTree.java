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
                processForDisplay(root, -1, displayList);
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

        InventoryObject io = null;
        if (name != null) {
            switch (name) {
                // Explicitly ignore these
                case "elevationUnit":
                case "intervalUnit":
                case "measuredDepthUnit":
                case "unit":
                    return null;

                //Create these nodes
                case "boreholes":
                    io = new InventoryObject("Boreholes", 100);
                    break;
                case "collection":
                    io = new InventoryObject("Collection", null, 500);
                    break;
                case "operators":
                    io = new InventoryObject("Operators", null, 50);
                    break;
                case "outcrops":
                    io = new InventoryObject("Outcrops", null, 100);
                    break;
                case "prospect":
                    io = new InventoryObject("Prospect");
                    break;
                case "shotline":
                    io =  new InventoryObject("Shotline", o);
                    break;
                case "shotpoints":
                    io = new InventoryObject("Shotpoints", null, 50);
                    break;
                case "type":
                    io = new InventoryObject("Type");
                    break;
                case "wells":
                    io = new InventoryObject("Wells", null, 100);
                    break;
                default:
                    io = new InventoryObject(name);
            }
        }

        if (name == null) {
            io = new InventoryObject(name);
        }

        for (Iterator<String> it = o.keys(); it.hasNext(); ) {
            String key = it.next();
            io.addChild(parseTree(o, key, o.get(key)));
        }

        return io;
    }


//*********************************************************************************************

    private static InventoryObject handleArray(Object parent, String name, JSONArray a) throws Exception {
        InventoryObject io = null;

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
                    return new InventoryObject("Keywords", sb.toString(), 800);
                }

                //Create these nodes
                case "boreholes":
                    io = new InventoryObject("Boreholes", null,100);
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

        if (name == null) {
            io = new InventoryObject(name);
        }



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
                } else {
                    return new InventoryObject("Elevation", o, 900);
                }
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
                } else {
                    return new InventoryObject("Interval Bottom", o, 902);
                }
            }
            case "intervalTop": {
                JSONObject pjo = (JSONObject) parent;
                if (pjo.has("intervalUnit") && pjo.getJSONObject("intervalUnit").has("abbr")) {
                    String val = o + " " + pjo.getJSONObject("intervalUnit").get("abbr");
                    return new InventoryObject("Interval Top", val, 902);
                } else {
                    return new InventoryObject("Interval Top", o, 902);
                }
            }
            case "keywords":
                return new InventoryObject("Keywords", o, 600);
            case "measuredDepth": {
                JSONObject pjo = (JSONObject) parent;
                if (pjo.has("measuredDepthUnit") && pjo.getJSONObject("measuredDepthUnit").has("abbr")) {
                    String val = o + " " + pjo.getJSONObject("measuredDepthUnit").get("abbr");
                    return new InventoryObject("Measured Depth", val, 75);
                } else if (pjo.has("unit") && pjo.getJSONObject("unit").has("abbr")) {
                    String val = o + " " + pjo.getJSONObject("unit").get("abbr");
                    return new InventoryObject("Measured Depth", val, 75);
                } else {
                    return new InventoryObject("Measured Depth", o, 75);
                }
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
            case "verticalDepth":
            {
                JSONObject pjo = (JSONObject) parent;
                if (pjo.has("unit") && pjo.getJSONObject("unit").has("abbr")) {
                    String val = o + " " + pjo.getJSONObject("unit").get("abbr");
                    return new InventoryObject("Vertical Depth", val, 80);
                } else {
                    return new InventoryObject("Vertical Depth", o, 80);
                }
            }
            case "wellNumber":
                return new InventoryObject("Well Number", o, 94);
            default:
                return new InventoryObject(name, o);
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
        if (sb.length() > 1 ) {
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

    public static SpannableStringBuilder makeBold(SpannableStringBuilder ssb, String s, int depth, int lengthOfIndent){
        int index = 0;
        index = s.indexOf(s, index);
        ssb.setSpan(new StyleSpan(BOLD), index + depth * lengthOfIndent,depth * lengthOfIndent + index + s.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }


//*********************************************************************************************

    private static void processForDisplay(InventoryObject o,
                                          int depth, ArrayList<SpannableStringBuilder> displayList)
    {

        int lengthOfIndent = 2;

        Collections.sort(o.getChildren(), new SortInventoryObjectList());

        SpannableStringBuilder ssb = new SpannableStringBuilder();

        if(o.getName() != null  && o.getChildren().size() == 0)
        {
            for (int i = 0; i < depth; i++)
            {
                ssb.append("  ");
            }
            ssb.append(o.getName()).append(" ").append(o.getValue().toString());

            displayList.add(makeBold(ssb, o.getName(), depth, lengthOfIndent));
        }else
            {
            if(o.getName() != null) {
                for (int i = 0; i < depth; i++)
                {
                    ssb.append("  ");
                }
                ssb.append(o.getName());
                displayList.add(makeBold(ssb, o.getName(), depth, lengthOfIndent));
            }
            for(InventoryObject child : o.getChildren())
            {
                processForDisplay(child, depth + 1, displayList);
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