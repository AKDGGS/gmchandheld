package gov.alaska.gmchandheld;

import java.util.ArrayList;

public class InventoryObject {
    private final ArrayList<InventoryObject> children;
    private String name;
    private Object value;
    private int displayWeight;

    public InventoryObject() {
        this(null, null, 0);
    }

    public InventoryObject(String name) {
        this(name, null, 0);
    }

    public InventoryObject(String name, Object value) {
        this(name, value, 0);
    }

    public InventoryObject(String name, Object value, int displayWeight) {
        this.name = name;
        this.value = value;
        this.displayWeight = displayWeight;
        children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ArrayList<InventoryObject> getChildren() {
        return children;
    }

    public void addChild(InventoryObject child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    public Integer getDisplayWeight() {
        return displayWeight;
    }

    public void setDisplayWeight(Integer displayWeight) {
        this.displayWeight = displayWeight;
    }
}