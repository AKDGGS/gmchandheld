package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableStringBuilder;

import java.util.ArrayList;

public class InventoryObject {

    private String key;
    private Object value;

    private String keyValueWithIndent;
    private SpannableStringBuilder groupSSB;

    private InventoryObject parent = null;
    private final ArrayList<InventoryObject> children = new ArrayList<>();

    private int displayWeight = 9; //used to control display order

    private boolean display = true;  // controls if a parameter will be displayed within the concatenated group string.

    private int groupID;


    public InventoryObject(String key, Object value) {
        this.key = key;
        this.value = value;

    }

    public InventoryObject(String key, Object value, InventoryObject parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }


    public String getKeyValueWithIndent() { return keyValueWithIndent; }

    public void setKeyValueWithIndent(String keyValueWithIndent) { this.keyValueWithIndent = keyValueWithIndent; }

    public SpannableStringBuilder getGroupSSB() {
        return groupSSB;
    }

    public void setGroupSSB(SpannableStringBuilder groupSSB) {
        this.groupSSB = groupSSB;
    }

    public InventoryObject getParent() {
        return parent;
    }

    public void setParent(InventoryObject parent) {
        this.parent = parent;
    }


    public ArrayList<InventoryObject> getChildren() {
        return children;
    }

    public void addChild(InventoryObject child) {
        this.children.add(child);
    }


    public Integer getDisplayWeight() {
        return displayWeight;
    }

    public void setDisplayWeight(Integer displayWeight) {
        this.displayWeight = displayWeight;
    }


    public boolean getDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }


    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public void setParentChildRelationship( InventoryObject thisParent) {
        if (thisParent != null) {
            parent = thisParent;
            thisParent.addChild(this);
        }
    }

}
