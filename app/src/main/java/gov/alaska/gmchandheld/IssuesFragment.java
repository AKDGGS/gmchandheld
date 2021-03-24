package gov.alaska.gmchandheld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class IssuesFragment extends DialogFragment {

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

        builder.setTitle("Select")
               .setMultiChoiceItems(list, AddInventory.checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                       if (b) {
                           switch (list[i]) {
                               case "Needs Inventory":
                                   AddInventory.selectedItems.add("needs_inventory");
                                   break;
                               case "Unsorted":
                                   AddInventory.selectedItems.add("unsorted");
                                   break;
                               case "Radiation Risk":
                                   AddInventory.selectedItems.add("radiation_risk");
                                   break;
                               case "Material Damaged":
                                   AddInventory.selectedItems.add("material_damaged");
                                   break;
                               case "Box Damaged":
                                   AddInventory.selectedItems.add("box_damaged");
                                   break;
                               case "Missing":
                                   AddInventory.selectedItems.add("missing");
                                   break;
                               case "Needs Metadata":
                                   AddInventory.selectedItems.add("needs_metadata");
                                   break;
                               case "Barcode Missing":
                                   AddInventory.selectedItems.add("barcode_missing");
                                   break;
                               case "Label Obscured":
                                   AddInventory.selectedItems.add("label_obscured");
                                   break;
                               case "Insufficient Material":
                                   AddInventory.selectedItems.add("insufficient_material");
                                   break;
                           }
                           AddInventory.selectedItemsDisplayList.add(list[i]);
                       }else{
                           switch (list[i]) {
                               case "Needs Inventory":
                                   AddInventory.selectedItems.remove("needs_inventory");
                                   break;
                               case "Unsorted":
                                   AddInventory.selectedItems.remove("unsorted");
                                   break;
                               case "Radiation Risk":
                                   AddInventory.selectedItems.remove("radiation_risk");
                                   break;
                               case "Materials Damaged":
                                   AddInventory.selectedItems.remove("materials_damaged");
                                   break;
                               case "Box Damaged":
                                   AddInventory.selectedItems.remove("box_damaged");
                                   break;
                               case "Missing":
                                   AddInventory.selectedItems.remove("missing");
                                   break;
                               case "Needs Metadata":
                                   AddInventory.selectedItems.remove("needs_metadata");
                                   break;
                               case "Barcode Missing":
                                   AddInventory.selectedItems.remove("barcode_missing");
                                   break;
                               case "Label Obscured":
                                   AddInventory.selectedItems.remove("label_obscured");
                                   break;
                               case "Insufficient Material":
                                   AddInventory.selectedItems.remove("insufficient_material");
                                   break;
                           }
                           AddInventory.selectedItemsDisplayList.remove(list[i]);
                       }
                   }
               })
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       mListener.onPostitiveButtonClicked(list, AddInventory.selectedItems);
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                       mListener.onNegativebuttonClicked();
                   }
               });
        return builder.create();

    }
}
