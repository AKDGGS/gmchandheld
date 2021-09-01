package gov.alaska.gmchandheld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;

public class IssuesFragment extends DialogFragment {
    ArrayList<String> selectedItems;
    ArrayList<String> selectedItemsDisplayList;
    boolean[] checkedItems;
    public interface onMultiChoiceListener {
        void onPostitiveButtonClicked(String[] list, ArrayList<String> selectedItems);

        void onNegativebuttonClicked();
    }
    onMultiChoiceListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (onMultiChoiceListener) context;
        } catch (Exception e) {
            throw new ClassCastException(getActivity().toString() + " onMultiChoiceListener must be implemented.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] list = getActivity().getResources().getStringArray(R.array.choice_items);
        switch (getContext().getClass().getSimpleName()) {
            case "AddInventory":
                selectedItems = AddInventory.getSelectedItems();
                selectedItemsDisplayList = AddInventory.getSelectedItemsDisplayList();
                checkedItems = AddInventory.getCheckedItems();
                break;
            case "Quality":
                selectedItems = Quality.getSelectedItems();
                selectedItemsDisplayList = Quality.getSelectedItemsDisplayList();;
                checkedItems = Quality.getCheckedItems();
                break;
        }
        builder.setTitle("Select")
                .setMultiChoiceItems(list, checkedItems, (dialogInterface, i, b) -> {
                    if (b) {
                        switch (list[i]) {
                            case "Needs Inventory":
                                selectedItems.add("needs_inventory");
                                break;
                            case "Unsorted":
                                selectedItems.add("unsorted");
                                break;
                            case "Radiation Risk":
                                selectedItems.add("radiation_risk");
                                break;
                            case "Material Damaged":
                                selectedItems.add("material_damaged");
                                break;
                            case "Box Damaged":
                                selectedItems.add("box_damaged");
                                break;
                            case "Missing":
                                selectedItems.add("missing");
                                break;
                            case "Needs Metadata":
                                selectedItems.add("needs_metadata");
                                break;
                            case "Barcode Missing":
                                selectedItems.add("barcode_missing");
                                break;
                            case "Label Obscured":
                                selectedItems.add("label_obscured");
                                break;
                            case "Insufficient Material":
                                selectedItems.add("insufficient_material");
                                break;
                        }
                        selectedItemsDisplayList.add(list[i]);
                    } else {
                        switch (list[i]) {
                            case "Needs Inventory":
                                selectedItems.remove("needs_inventory");
                                break;
                            case "Unsorted":
                                selectedItems.remove("unsorted");
                                break;
                            case "Radiation Risk":
                                selectedItems.remove("radiation_risk");
                                break;
                            case "Materials Damaged":
                                selectedItems.remove("materials_damaged");
                                break;
                            case "Box Damaged":
                                selectedItems.remove("box_damaged");
                                break;
                            case "Missing":
                                selectedItems.remove("missing");
                                break;
                            case "Needs Metadata":
                                selectedItems.remove("needs_metadata");
                                break;
                            case "Barcode Missing":
                                selectedItems.remove("barcode_missing");
                                break;
                            case "Label Obscured":
                                selectedItems.remove("label_obscured");
                                break;
                            case "Insufficient Material":
                                selectedItems.remove("insufficient_material");
                                break;
                        }
                        selectedItemsDisplayList.remove(list[i]);
                    }
                })
                .setPositiveButton("OK", (dialogInterface, i) ->
                        mListener.onPostitiveButtonClicked(list, selectedItems))
                .setNegativeButton("Cancel", (dialogInterface, i) ->
                        mListener.onNegativebuttonClicked());
            return builder.create();
        }
}
