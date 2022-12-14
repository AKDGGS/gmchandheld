package gov.alaska.gmchandheld;

import static android.graphics.Typeface.BOLD;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

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

import gov.alaska.gmchandheld.comparators.SortInventoryObjectList;

public class SummaryLogicForDisplay {
    private final List<String> keyList;
    private final Map<String, List<SpannableStringBuilder>> displayDict;
    private final ArrayList<String> typeFlagList = new ArrayList<>();
    private int numberOfBoxes, ID;
    private String barcodeQuery;

    public SummaryLogicForDisplay() {
        keyList = new ArrayList<>();
        displayDict = new HashMap<>();
        numberOfBoxes = 0;
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(1);
    }

    public static void createIndentedText(SpannableStringBuilder text,
                                          int marginFirstLine,
                                          int marginNextLines) {
        //https://www.programmersought.com/article/45371641877/
        text.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines), 0,
                text.length(), 0);
    }

    public int getNumberOfBoxes() {
        return numberOfBoxes;
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

    public String getBarcodeQuery() {
        return barcodeQuery;
    }

    public void setBarcodeQuery(String barcodeQuery) {
        this.barcodeQuery = barcodeQuery;
    }

    public ArrayList<String> getTypeFlagList() {
        return typeFlagList;
    }

    public void setTypeFlag(String typeFlag) {
        this.typeFlagList.add(typeFlag);
    }

    public void processRawJSON(String rawJSON) throws Exception {
        if (rawJSON.trim().charAt(0) == '[') {
            JSONArray inputJson = new JSONArray((rawJSON));  // check for jsonarray
            InventoryObject root = parseTree(null, null, inputJson);
            if (root != null) {
                getStringForDisplay(root, 0, null, null, getDisplayDict());
            }
        } else if (rawJSON.trim().charAt(0) == '{') {
            JSONObject inputJson = new JSONObject((rawJSON));  // check for jsonobject
            InventoryObject root = parseTree(null, barcodeQuery, inputJson);
            if (root != null) {
                getStringForDisplay(root, 1, null, null, getDisplayDict());
            }
        }
    }

    private void getStringForDisplay(InventoryObject o, int depth, String currKey,
                                     LinkedList<SpannableStringBuilder> displayList,
                                     Map<String, List<SpannableStringBuilder>> dict) {
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
                    //Font spacing and size varies depending on the font chosen.
                    // The font used in this app works well with an arbitrary indentation of 3 spaces.
                    // 3 spaces was determined by trial and error.
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
                ssb.setSpan(new StyleSpan(BOLD), lengthOfSsb, lengthOfSsb + o.getName().length(),
                        SPAN_EXCLUSIVE_EXCLUSIVE);
                int indentationIncrement = 42; //Arbitrary value
                if (!Character.isWhitespace(ssb.charAt(3))) {
                    createIndentedText(ssb, 3, indentationIncrement);
                } else if (!Character.isWhitespace(ssb.charAt(6))) {
                    createIndentedText(ssb, 6, indentationIncrement * 2);
                } else if (!Character.isWhitespace(ssb.charAt(9))) {
                    createIndentedText(ssb, 9, indentationIncrement * 3);
                } else {
                    createIndentedText(ssb, 0, indentationIncrement * 4);
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

    private InventoryObject handleObject(Object parent, String name, JSONObject o) throws Exception {
        InventoryObject io;
        if (name == null) {
            if (!"".equals(o.optString("ID"))) {
                String newName = "ID " + o.optInt("ID");
                if (!"".equals(o.optString("barcode"))) {
                    newName += " / " + o.optString("Barcode");
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
                    setTypeFlag("Borehole");
                    break;
                }
                case "outcrops": {
                    String id = o.optString("ID");
                    String newName = "Outcrop";
                    if (!"".equals(Integer.toString(ID))) {
                        newName += " ID " + id;
                    }
                    io = new InventoryObject("Object " + newName, null, 100);
                    setTypeFlag("Outcrop");
                    break;
                }
                case "prospect": {
                    String id = o.optString("ID");
                    String newName = "Prospect";
                    if (!"".equals(Integer.toString(ID))) {
                        newName += " ID " + id;
                    }
                    io = new InventoryObject("Object " + newName, null);
                    setTypeFlag("Prospect");
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
                    setTypeFlag("Shotpoint");
                    break;
                }
                case "wells":
                    String id = o.optString("ID");
                    String newName = "Well";
                    io = new InventoryObject("Object " + newName, null, 100);
                    setTypeFlag("Well");
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

    private InventoryObject handleArray(Object parent, String name, JSONArray a) throws Exception {
        InventoryObject io;
        StringBuilder sb = new StringBuilder();
        if (name == null) {
            io = new InventoryObject(name);
        } else {
            switch (name) {
                //Create these nodes
                case "barcodes": {
                    for (int i = 0; i < a.length(); i++) {
                        if (a.get(i) instanceof String) {
                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            sb.append(a.get(i));
                        }
                    }
                    return new InventoryObject("Barcodes", sb.toString(), 0);
                }
                case "keywords": {
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
                case "collections": {
                    for (int i = 0; i < a.length(); i++) {
                        if (a.get(i) instanceof JSONObject) {
                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            if (((JSONObject)a.get(i)).has("collection")) {
                                sb.append(((JSONObject) a.get(i)).get("collection"));
                            }
                        }
                    }
                    return new InventoryObject("Collection", sb.toString(), 800);
                }
                case "containers": {
                    for (int i = 0; i < a.length(); i++) {
                        if (a.get(i) instanceof JSONObject) {
                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            if (((JSONObject)a.get(i)).has("container")) {
                                sb.append(((JSONObject) a.get(i)).get("container"));
                            }
                        }
                    }
                    return new InventoryObject("Containers", sb.toString(), 1000);
                }
                case "boreholes":
                    io = new InventoryObject("Boreholes", null, 50);
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

    private InventoryObject handleSimple(Object parent, String name, Object o) {
        if (name == null) {
            return null;
        }
        switch (name) {
            // Higher the displayWeight, the higher a priority an key has.
            // Items are sorted internally first, and the externally in processForDisplay()
            case "barcodes":
                numberOfBoxes++;
                return new InventoryObject("Barcodes", o, 0);
            case "barcodes_count":
                return new InventoryObject("Barcodes Count", o, 0);
            case "borehole":
                return new InventoryObject("Borehole", o, 900);
            case "keywords":
                return new InventoryObject("Keywords", o, 700);
            case "prospect":
                return new InventoryObject("Prospect", o, 900);
            case "total":
                return new InventoryObject("Total", o, 800);
            case "well":
                return new InventoryObject("Well", o, 900);
            default:
                return new InventoryObject(name, o);
        }
    }
}