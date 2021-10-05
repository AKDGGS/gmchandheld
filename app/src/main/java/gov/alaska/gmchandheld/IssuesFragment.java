package gov.alaska.gmchandheld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

public class IssuesFragment extends DialogFragment {
    private ArrayList<String> selectedItems;
    private ArrayList<String> selectedItemsDisplayList;
    private boolean[] checkedItems;
    private onMultiChoiceListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (onMultiChoiceListener) context;
        } catch (Exception e) {
            if (getActivity() != null) {
                throw new ClassCastException(getActivity().toString() + " onMultiChoiceListener must be implemented.");
            } else {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        SharedPreferences sp = this.getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        String issuesStr = sp.getString("issuesString", "needs_inventory");
        // creates two lists one for display the other for the databases
        String[] issuesListDisplay = WordUtils.capitalize(issuesStr, '"', '_')
                .replaceAll("[\"\\[\\]]", "")
                .replace('_', ' ').split(",");
        String[] issuesListDb = issuesStr.replaceAll("[\"\\[\\]]", "")
                .split(",");
        switch (getContext().getClass().getSimpleName()) {
            case "AddInventory":
                selectedItems = AddInventory.getSelectedItems();
                selectedItemsDisplayList = AddInventory.getSelectedItemsDisplayList();
                checkedItems = AddInventory.getCheckedItems();
                break;
            case "Quality":
                selectedItems = Quality.getSelectedItems();
                selectedItemsDisplayList = Quality.getSelectedItemsDisplayList();
                checkedItems = Quality.getCheckedItems();
                break;
        }
        builder.setTitle("Select")
                .setMultiChoiceItems(issuesListDisplay, checkedItems, ((dialogInterface, i, b) -> {
                    if (b) {
                        selectedItemsDisplayList.add(issuesListDisplay[i]);
                        selectedItems.add(issuesListDb[i]);
                    } else {
                        selectedItemsDisplayList.remove(issuesListDisplay[i]);
                        selectedItems.remove(issuesListDb[i]);
                    }
                }))
                .setPositiveButton("OK", (dialogInterface, i) ->
                        mListener.onPostitiveButtonClicked(issuesListDisplay, selectedItems))
                .setNegativeButton("Cancel", (dialogInterface, i) ->
                        mListener.onNegativebuttonClicked());
        return builder.create();
    }

    public interface onMultiChoiceListener {
        void onPostitiveButtonClicked(String[] list, ArrayList<String> selectedItems);

        void onNegativebuttonClicked();
    }
}
