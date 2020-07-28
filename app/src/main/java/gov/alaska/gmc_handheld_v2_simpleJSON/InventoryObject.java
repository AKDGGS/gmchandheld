package gov.alaska.gmc_handheld_v2_simpleJSON;


import java.util.ArrayList;

public class InventoryObject {

    private String key;
    private Object value;

    private String keyValueWithIndent;

    private InventoryObject parent = null;
    private final ArrayList<InventoryObject> children = new ArrayList<>();

    private int displayWeight = 9; //used to control display order



    public InventoryObject(String key, Object value) {
        this.key = key;
        this.value = value;

    }

    public InventoryObject(String key, Object value, InventoryObject parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
        if(parent != null){
            displayWeight = parent.getDisplayWeight();
        }
    }

    public InventoryObject(String key, Object value, InventoryObject parent, int displayWeight) {
        this.key = key;
        this.value = value;
        this.parent = parent;
        this.displayWeight = displayWeight;
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


    public void setParentChildRelationship( InventoryObject thisParent) {
        if (thisParent != null) {
            parent = thisParent;
            thisParent.addChild(this);
        }
    }

}
