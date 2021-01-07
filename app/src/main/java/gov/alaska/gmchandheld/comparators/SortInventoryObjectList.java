package gov.alaska.gmchandheld.comparators;

import java.util.Comparator;

import gov.alaska.gmchandheld.InventoryObject;

public class SortInventoryObjectList implements Comparator<InventoryObject> {
    @Override
    public int compare(InventoryObject o1, InventoryObject o2) {
        int comparison;

//        comparison = o1.getDisplayWeight().compareTo(o2.getDisplayWeight());
        comparison = o2.getDisplayWeight().compareTo(o1.getDisplayWeight());  //Changing the order reverses the ordering

        if (comparison == 0 && o1.getDisplayWeight() != null && o2.getDisplayWeight() != null) {
            comparison = o1.getDisplayWeight().compareTo(o2.getDisplayWeight());
        }

        return comparison;
    }

}