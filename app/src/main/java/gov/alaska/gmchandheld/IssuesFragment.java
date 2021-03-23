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

    public interface onMultiChoiceListener{
        void onPostitiveButtonClicked(String[] list, ArrayList<String> selectedItems);
        void onNegativebuttonClicked();
    }

    onMultiChoiceListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (onMultiChoiceListener) context;
        }catch (Exception e){
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
                           }
                       }
//                           AddInventory.selectedItems.add(list[i]);
//                       } else {
//                           AddInventory.selectedItems.remove(list[i]);
//                       }
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
